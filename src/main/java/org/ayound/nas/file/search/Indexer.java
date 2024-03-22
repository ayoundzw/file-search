package org.ayound.nas.file.search;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.NumericDocValuesField;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.codelibs.jhighlight.tools.FileUtils;

class Indexer {
	private static Logger log = LogManager.getLogger(Indexer.class);

	private final Scanner scanner;

	Indexer(Scanner scanner) {
		this.scanner = scanner;
	}

	void indexDirectory(File directoryToIndex) throws InterruptedException {
		Set<String> observedContentTypes = new HashSet<>();
		Set<String> processedContentTypes = new HashSet<>();
		Set<String> ignoredContentTypes = new HashSet<>();

		try {
			final Long[] fileCount = { 0L };
			IndexWriter indexWriter = IndexService.getInstance().newIndexWriter();
			try {
				fileCount[0] += scanner.scanDirectory(directoryToIndex, observedContentTypes,
						(path, contentType, major, minor, charset, reader) -> {
							if (ProcessService.getInstance().isFinished()) {
								return false;
							}
							String filename = path.getFileName().toString();
							String absolutePath = path.toAbsolutePath().toString();
							File file = new File(absolutePath);
							long time = file.lastModified();
							long size = Files.size(path);
							//
							Document doc = new Document();
							// Things to index: filename, path, content-type, content
							doc.add(new StringField("name", filename, Field.Store.YES));
							doc.add(new StringField("path", absolutePath, Field.Store.YES));

							doc.add(new StoredField("size", size));
							doc.add(new NumericDocValuesField("s_size", size));
//							doc.add(new LongField("time", time,Field.Store.YES));
							doc.add(new StoredField("time", time));
							doc.add(new NumericDocValuesField("s_time", time));
							doc.add(new StringField("type", FileUtils.getExtension(filename), Field.Store.YES));

							switch (major) {
							case "audio":
							case "video":
							case "img": // incorrect spelling!
							case "image":
							case "font":
								ignoredContentTypes.add(contentType);
								break;

							default:
								doc.add(new TextField("content", reader.toString(), Field.Store.YES));
								processedContentTypes.add(contentType);

								break;
							}

							indexWriter.addDocument(doc);
							indexWriter.flush();
							indexWriter.commit();
							log.info("File [" + absolutePath + "] is indexed.");
							try {
								Thread.sleep(100);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							return true;
						});

				//

				//
				if (!observedContentTypes.isEmpty()) {
					List<String> _observedContentTypes = new LinkedList<>(observedContentTypes);
					Collections.sort(_observedContentTypes);
				}

				//
				if (!processedContentTypes.isEmpty()) {
					List<String> _processedContentTypes = new LinkedList<>(processedContentTypes);
					Collections.sort(_processedContentTypes);
				}

				//
				if (!ignoredContentTypes.isEmpty()) {
					List<String> _ignoredContentTypes = new LinkedList<>(ignoredContentTypes);
					Collections.sort(_ignoredContentTypes);
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				indexWriter.close();
			}
		} catch (IOException e) {
			String info = "Failed to close index: " + e.getMessage();
			log.warn(info, e);
		}
	}
}
