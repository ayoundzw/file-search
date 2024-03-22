package org.ayound.nas.file.search;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;

public class StartScanThread extends Thread {

	@Override
	public void run() {
		ProcessService.getInstance().startIndexing();
		try {
			File tikaConfigFile = new File("tika-config.xml");
			Scanner scanner = new Scanner(tikaConfigFile);
			String sources = Configuration.getSourceFilePaths();
			String[] list = sources.split(",");

			for (String s : list) {
				if (ProcessService.getInstance().isFinished()) {
					break;
				}
				File sourceDirectory = new File(s);
				Indexer indexer = new Indexer(scanner);
				indexer.indexDirectory(sourceDirectory);
			}
			// delete document where not found file
			Directory indexDirectory = IndexService.getInstance().getIndexDirectory();
			if (indexDirectory.listAll().length > 1) {
				DirectoryReader reader = DirectoryReader.open(indexDirectory);
				List<String> deleted = new ArrayList<String>();
				int maxDoc = reader.maxDoc();
				for (int i = 0; i < maxDoc; i++) {
					Document doc = reader.document(i);
					String fileName = doc.get("path");
					Path filePath = Paths.get(fileName);
					if (!Files.exists(filePath)) {
						deleted.add(fileName);
					}
				}
				reader.close();
				IndexWriter writer = IndexService.getInstance().newIndexWriter();
				for (String fileName : deleted) {
					writer.deleteDocuments(new Term("path", fileName));
				}
				writer.commit();
				writer.close();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			ProcessService.getInstance().endIndexing();
		}

	}

}
