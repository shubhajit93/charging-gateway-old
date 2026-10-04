package com.ssdtech.java.log.monitoring;

import java.util.StringJoiner;

import com.ssdtech.java.model.applicationproperties.ApplicationProperties;

public class StateContext {
	private static final String JOINER = "|";
	
    private String cdrId;
    private long epochMilliSeconds;
    private TransactionState state;
    
    public StateContext(String cdrId, TransactionState state) {
    	this.cdrId = cdrId;
    	this.epochMilliSeconds = System.currentTimeMillis();
    	this.state = state;
    }
    
    public boolean isInValidStateContext() {
    	return this.state.equals(TransactionState.NA);
    }
    
    public String serialize() {
    	return new StringJoiner(JOINER)
    			       .add(this.cdrId)
    			       .add(ApplicationProperties.getServerSpecificConfiguration().cgwId)
    			       .add(this.state.name())
    			       .add(String.valueOf(this.epochMilliSeconds)).toString();
    }
}
