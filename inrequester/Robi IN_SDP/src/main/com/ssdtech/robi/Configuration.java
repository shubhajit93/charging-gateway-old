/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ssdtech.robi;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class Configuration {
	public static final String CONFIG_FILE_PATH = "./ConfigurationFiles/in/config.cfg";
	private Properties prop = new Properties();

	public Configuration() throws IOException {
		this.prop.load(new FileInputStream(CONFIG_FILE_PATH));
	}

	public String getProperty(String key) {
		String value = this.prop.getProperty(key);
		return value != null ? value.trim() : value;
	}
	
	public String getProperty(String key, String defaultValue) {
		String value = this.prop.getProperty(key);
		return value != null && !value.trim().isEmpty() ? value.trim() : defaultValue;
	}
}
