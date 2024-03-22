package org.ayound.nas.file.search;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Converter {
	private static Logger log = LogManager.getLogger(Converter.class);

	private final Scanner scanner;

	Converter(Scanner scanner) {
		this.scanner = scanner;
	}

	void convertDirectory(File directoryToConvert) {
		Set<String> observedContentTypes = new HashSet<>();
		Set<String> processedContentTypes = new HashSet<>();
		Set<String> ignoredContentTypes = new HashSet<>();

		try {
			final Long[] fileCount = { 0L };
			fileCount[0] += scanner.scanDirectory(directoryToConvert, observedContentTypes,
					(path, contentType, major, minor, charset, reader) -> {
						String filename = path.getFileName().toString();

						switch (major) {
						case "audio":
						case "video":
						case "img": // incorrect spelling!
						case "image":
						case "font":
							ignoredContentTypes.add(contentType);
							break;
						default:
							processedContentTypes.add(contentType);

							break;
						}

						return true;
					});

			

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
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}
}
