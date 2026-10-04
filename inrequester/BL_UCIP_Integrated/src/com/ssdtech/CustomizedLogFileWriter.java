package com.ssdtech;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.RollingFileAppender;

public class CustomizedLogFileWriter {
	private Logger logger;

	public CustomizedLogFileWriter(String filePath) {
		PatternLayout layout = new PatternLayout();
		String conversionPattern = "%d{yyyy-MM-dd HH:m:ss}|%m";
		layout.setConversionPattern(conversionPattern);

		// creates file appender
		RollingFileAppender fileAppender = new RollingFileAppender();
		fileAppender.setFile(filePath);
		fileAppender.setLayout(layout);
		fileAppender.setMaxFileSize("10MB");
		fileAppender.setMaxBackupIndex(10);
		fileAppender.activateOptions();

		// configures the root logger
		this.logger = Logger.getRootLogger();
		this.logger.setLevel(Level.INFO);
		this.logger.addAppender(fileAppender);
	}

	public void storeContent(String content) {
		this.logger.info(content + "\r\n");
	}
}
