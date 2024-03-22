package org.ayound.nas.file.search;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.util.IOUtils;
import org.apache.tika.config.TikaConfig;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.ayound.nas.file.search.filters.BinaryFilterReader;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;


class Scanner {
    private static Logger log = LogManager.getLogger(Scanner.class);

    private static final String DEFAULT_SOURCE_CHARACTER_ENCODING = "utf-8";

    private static final String CONTENT_TYPE_RE = "((?:[a-z][a-z0-9_]*))\\/((?:[a-z][a-z0-9_]*))((;.*?(charset)=((?:[a-z][a-z0-9_\\-]+)))*)";
    private static final Pattern contentTypePattern = Pattern.compile(CONTENT_TYPE_RE, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    public interface ScanRunnable {
        boolean run(Path path, String contentType, String major, String minor, String charset, String reader) throws IOException;
    }

    private final Parser parser;

    Scanner(File tikaConfigFile) throws Exception {
    	TikaConfig config  = new TikaConfig();
    	if(tikaConfigFile.exists()) {
    		config = new TikaConfig(tikaConfigFile);
    	}
        // Detector detector = config.getDetector();

        parser = new AutoDetectParser(config);
    }

    /**
     * Prepares configuration.
     * <p/>
     * @return true if configuration did exist and we may continue
     */
    public static boolean prepare(File tikaConfigFile) {
        // Check if config file exists and if not initiate it from template
        if (!tikaConfigFile.exists()) {
            String info = "No existing configuration - first time running?\n";
            info += "Creating an initial TIKA configuration...";
            System.out.println(info);

			try (InputStream is = Scanner.class.getResourceAsStream("tika-config-template.xml")) {
                if (null != is) {
                    tikaConfigFile.createNewFile();

                    try (FileOutputStream os = new FileOutputStream(tikaConfigFile)) {
                        byte[] buffer = new byte[1024];
                        int bytesRead;
                        while ((bytesRead = is.read(buffer)) != -1) {
                            os.write(buffer, 0, bytesRead);
                        }
                    } catch (FileNotFoundException ignore) {
                    	ignore.printStackTrace();
                    }
                }
            }
            catch (IOException ignore) {
            	ignore.printStackTrace();
            }

            info = "Please refer to and adjust the configuration file and then run again:\n" + tikaConfigFile;
            System.out.println(info);

            return false; // Configuration did not exist
        }
        return true; // Configuration existed
    }


    private String getBody(InputStream inputStream, Metadata metadata) throws IOException, TikaException, SAXException {
        ParseContext context = new ParseContext();

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            ContentHandler handler = new BodyContentHandler(outputStream);
            IOUtils.setByteArrayMaxOverride(Integer.MAX_VALUE);
            parser.parse(inputStream, handler, metadata, context);

            String bodyContent = outputStream.toString(DEFAULT_SOURCE_CHARACTER_ENCODING);
            return bodyContent;
        }
    }

    private boolean scanFile(
          File file,
          Set<String> observedContentTypes,
          final ScanRunnable runnable
          ) throws IOException {

        final Path path = file.toPath();
        long lastModified = file.lastModified();
        ProcessService.getInstance().setMessage("正在处理：" + file.getAbsolutePath());
        if(Searcher.getInstance().indexed(file.getAbsolutePath(), lastModified)) {
        	log.info("File is no change [" + file.getAbsolutePath() + "]");
        	return false;
        }
        try (InputStream is = Files.newInputStream(path)) {

            if (log.isDebugEnabled()) {
                String info = "Indexing " + file.getCanonicalPath();
                log.debug(info);
            }

            Metadata metadata = new Metadata();
            final String reader = getBody(is, metadata);

            String contentType = metadata.get("Content-Type");
            observedContentTypes.add(contentType);

            Matcher ctm = contentTypePattern.matcher(contentType.toLowerCase());
            if (ctm.matches()) {
                String major = ctm.group(1);
                String minor = ctm.group(2);
                String _contentType = major + "/" + minor;

                String charset = ctm.group(6);
                if (null == charset || charset.isEmpty()) {
                    charset = DEFAULT_SOURCE_CHARACTER_ENCODING;
                }

                return runnable.run(path, _contentType, major, minor, charset, reader);
            }
        }
        catch (TikaException tikae) {
            String info = "TIKA could not process file \"" + file.getAbsolutePath() + "\": " + tikae.getMessage();
            log.info(info);
        }
        catch (SAXException saxe) {
            String info = "Parse error: " + saxe.getMessage();
            log.warn(info);
        }
        catch (Throwable t) {
            String info = "Failed to index file \"" + file.getAbsolutePath() + "\":  " + t.getMessage();
            log.info(info);
        }
        return false;
    }


    public long scanDirectory(
            File directoryToIndex,
            Set<String> observedContentTypes,
            final ScanRunnable runnable
    ) throws IOException {

        long fileCount = 0L;

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(directoryToIndex.toPath())) {
            for (Path entryPath : stream) {
            	if(ProcessService.getInstance().isFinished()) {
					break;
				}
                File entry = entryPath.toFile();
                if (entry.isFile() && entry.canRead() && entry.length() > 0
                ) {
                    try {
                        if (scanFile(entry, observedContentTypes, runnable)) {
                            fileCount++;
                        }
                        ProcessService.getInstance().setCurrentCount(fileCount);
                        try {
							Thread.sleep(40);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
                    } catch (IOException ioe) {
                        String info = "Failed to rollback index: " + ioe.getMessage();
                        log.warn(info);
                    }
                }
                else if (entry.isDirectory()) {
                    fileCount += scanDirectory(entry, observedContentTypes, runnable);
                }
            }
        }

        return fileCount;
    }
}
