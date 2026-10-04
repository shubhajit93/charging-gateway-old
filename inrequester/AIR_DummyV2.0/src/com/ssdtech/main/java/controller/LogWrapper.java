package com.ssdtech.main.java.controller;

import org.apache.log4j.Logger;

import com.ssdtech.main.java.model.AppConstant;

public class LogWrapper {
	private static final int ENABLED = 1;
	private Logger logger;
	private String loggedClassName;
	
	public LogWrapper(String loggedClassName) {
		this.loggedClassName = loggedClassName;
		this.logger = Logger.getLogger(loggedClassName);
	}
	
	public Logger getLogger() {
		return this.logger;
	}
	
	public String getLoggedClassName() {
		return this.loggedClassName;
	}
	
	public void debug(String message) {
		if(AppConstant.logEnable == ENABLED) {
			this.logger.debug(message);
		}
	}
	
	public void debug(String message, Throwable exception) {
		if(AppConstant.logEnable == ENABLED) {
			this.logger.debug(message, exception);
		}
	}
	
	public void error(String message) {
		if(AppConstant.logEnable == ENABLED) {
			this.logger.error(message);
		}
	}
	
	public void error(String message, Throwable exception) {
		if(AppConstant.logEnable == ENABLED) {
			this.logger.error(message, exception);
		}
	}
	
	public void info(String message) {
		if(AppConstant.logEnable == ENABLED) {
			this.logger.info(message);
		}
	}
	
	public void info(String message, Throwable exception) {
		if(AppConstant.logEnable == ENABLED) {
			this.logger.info(message, exception);
		}
	}
}
