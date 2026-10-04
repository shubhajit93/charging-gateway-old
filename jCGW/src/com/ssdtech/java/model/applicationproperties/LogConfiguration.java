package com.ssdtech.java.model.applicationproperties;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.StringJoiner;

import com.ssdtech.java.utils.Utility;

public class LogConfiguration {
	public final int level;
	public final boolean logForAnyMSISDN;
	public final int destination;
	public final String ip;
	public final int port;
	public final String component;
	public final String instance;
	public final Set<String> loggableMSISDN;
	public final String directory;
	public final long logCreationFileInterval;
	public final String lineSeparator;
	
	public LogConfiguration(Properties properties) {
		String level = Utility.ptrim(properties, "LOG_LEVEL");
		String logForAnyMSISDN = Utility.ptrim(properties, "LOG_FOR_ANY_MSISDN");
		String destination = Utility.ptrim(properties, "LOG_DESTINATION", "10");
		String ip = Utility.ptrim(properties, "LOG_HOST", "localhost");
		String port = Utility.ptrim(properties, "LOG_PORT", "3470");
		String component = Utility.ptrim(properties, "LOG_COMPONENT", "CGW");
		String instance = Utility.ptrim(properties, "LOG_INSTANCE", "CGW1");
		String loggableMSISDN = Utility.ptrim(properties, "LOG_MSISDN");
		String directory = Utility.ptrim(properties, "LOG_DIRECTORY");
		String logCreationFileInterval = Utility.ptrim(properties, "LOG_FILE_CREATION_INTERVAL");
		String lineSeparator = properties.getProperty("LOG_FILE_LINE_SEPARATOR", "\r\n");
		
		this.level = Integer.valueOf(level);
		this.logForAnyMSISDN = logForAnyMSISDN.equals("1");
		this.destination = Integer.valueOf(destination);
		this.ip = ip;
		this.port = Integer.valueOf(port);
		this.component = component;
		this.instance = instance;
		this.directory = directory;
		this.logCreationFileInterval = Long.valueOf(logCreationFileInterval);
		this.lineSeparator = lineSeparator;
		
		this.loggableMSISDN = new HashSet<>();
		String[] msisdns = loggableMSISDN.split(",");
		for (String msisdn : msisdns) {
			this.loggableMSISDN.add(msisdn.trim());
		}
	}
	
	@Override
	public String toString() {
		List<Integer> lp = new ArrayList<>();
		for (Byte b : this.lineSeparator.getBytes()) {
			lp.add((int) b);
		}
		
		return new StringJoiner("\r\n")
				       .add("\r\n\r\n[Log Configuration]")
				       .add("LOG_LEVEL = " + this.level)
				       .add("LOG_FOR_ANY_MSISDN = " + Utility.getEncodedValueForBooleanProperties(this.logForAnyMSISDN))
				       .add("LOG_DESTINATION = " + this.destination)
				       .add("LOG_HOST = " + this.ip)
				       .add("LOG_PORT = " + this.port)
				       .add("LOG_COMPONENT = " + this.component)
				       .add("LOG_INSTANCE = " + this.instance)
				       .add("LOG_MSISDN = " + this.loggableMSISDN)
				       .add("LOG_DIRECTORY = " + this.directory)
				       .add("LOG_FILE_CREATION_INTERVAL = " + this.logCreationFileInterval)
				       .add("LOG_FILE_LINE_SEPARATOR = " + lp)
				       .toString();
	}
}
