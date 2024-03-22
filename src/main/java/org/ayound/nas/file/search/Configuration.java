package org.ayound.nas.file.search;

import org.springframework.stereotype.Component;

import cn.keking.config.ConfigConstants;

@Component
public class Configuration {

	public static String getSourceFilePaths() {
		return ConfigConstants.getSourceFilePaths();
	}

	public static String getIndexFilePath() {
		return ConfigConstants.getIndexFilePath();
	}

}