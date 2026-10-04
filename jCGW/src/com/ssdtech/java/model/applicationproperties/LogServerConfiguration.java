package com.ssdtech.java.model.applicationproperties;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.StringJoiner;

import com.ssdtech.java.utils.Utility;

public class LogServerConfiguration {
	public final boolean enable;
    public final String ip;
    public final int port;
    public final long connectTimeout;
    public final long reconnectInterval;
    public final int concurrentSenders;
    public final int batchSize;
    public final String contextSeparator;
    public final int maxContexts;
    
    public LogServerConfiguration(Properties properties) {
    	String enable = Utility.ptrim(properties, "LOG_SERVER_ENABLE", "0");
    	String ip = Utility.ptrim(properties, "LOG_SERVER_IP");
    	String port = Utility.ptrim(properties, "LOG_SERVER_PORT");
    	String connectTimeout = Utility.ptrim(properties, "LOG_SERVER_CONNECT_TIMEOUT", "1000");
    	String reconnectInterval = Utility.ptrim(properties, "LOG_SERVER_RECONNECT_INTERVAL", "1000");
    	String concurrentSenders = Utility.ptrim(properties, "LOG_SERVER_CONCURRENT_SENDER", "0");
    	String batchSize = Utility.ptrim(properties, "LOG_SERVER_BATCH_SIZE", "10");
    	String contextSeparator = properties.getProperty("LOG_SERVER_CONTEXT_SEPARATOR", "||");
    	String maxContexts = properties.getProperty("LOG_SERVER_MAX_CONTEXTS", "2000");
    	
    	this.enable = enable.equals("1");
    	this.ip = ip;
    	this.port = Integer.valueOf(port);
    	this.connectTimeout = Long.valueOf(connectTimeout);
    	this.reconnectInterval = Long.valueOf(reconnectInterval);
    	this.concurrentSenders = Integer.valueOf(concurrentSenders);
    	this.batchSize = Integer.valueOf(batchSize);
    	this.contextSeparator = contextSeparator;
    	this.maxContexts = Integer.valueOf(maxContexts);
    }
    
    @Override
    public String toString() {
    	List<Integer> cp = new ArrayList<>();
		for (Byte b : this.contextSeparator.getBytes()) {
			cp.add((int) b);
		}
		
    	return new StringJoiner("\r\n")
			       .add("\r\n\r\n[Log Server configuration]")
			       .add("LOG_SERVER_ENABLE = " + Utility.getEncodedValueForBooleanProperties(this.enable))
			       .add("LOG_SERVER_IP = " + this.ip)
			       .add("LOG_SERVER_PORT = " + this.port)
			       .add("LOG_SERVER_CONNECT_TIMEOUT = " + this.connectTimeout)
			       .add("LOG_SERVER_RECONNECT_INTERVAL = " + this.reconnectInterval)
			       .add("LOG_SERVER_CONCURRENT_SENDER = " + this.concurrentSenders)
			       .add("LOG_SERVER_BATCH_SIZE = " + this.batchSize)
			       .add("LOG_SERVER_CONTEXT_SEPARATOR = " + cp)
			       .add("LOG_SERVER_MAX_CONTEXTS = " + this.maxContexts)
			       .toString();
    }
}
