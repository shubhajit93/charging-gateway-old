package com.ssdtech.java.model;

import java.util.regex.Pattern;

public class CallType {
    private final Pattern patternForAno;
    private final Pattern patternForBno;
    
    private final String callTypeId;
    
    private final int priority;
    
    public CallType(String patternForAno, String patternForBno, String callTypeId, int priority) {
    	this.patternForAno = Pattern.compile(patternForAno);
    	this.patternForBno = Pattern.compile(patternForBno);
    	this.callTypeId = callTypeId;
    	this.priority = priority;
    }
    
    public int getPriority() {
    	return this.priority;
    }
    
    public boolean equals(String ano, String bno) {
    	return this.patternForAno.matcher(ano).matches() && this.patternForBno.matcher(bno).matches();
    }
    
    public String getCallTypeId() {
    	return this.callTypeId;
    }
    
    @Override
    public String toString() {
    	return "Pattern for Ano: " + this.patternForAno.pattern() + ", " +
               "Pattern for Bno: " + this.patternForBno.pattern() + ", " + 
    		   "Call type id: " + this.callTypeId + ", " + 
               "Priority: " + this.priority;
    }
}
