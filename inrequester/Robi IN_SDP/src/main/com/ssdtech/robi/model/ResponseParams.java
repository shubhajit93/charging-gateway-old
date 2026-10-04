package com.ssdtech.robi.model;

public class ResponseParams {
    private final String cdrWithServiceType;
    private final String resultCode;
    private final int sequenceNo;
    private final String commandId;
    private final String reservedUnits;
    
    public ResponseParams(String resultCode) {
    	this.resultCode = resultCode;
    	this.cdrWithServiceType = "cdr[service_id]";
    	this.sequenceNo = 0;
    	this.commandId = "REQUEST_TYPE";
    	this.reservedUnits = "0";
    }
    
    public ResponseParams(String cdrWithServiceType, String resultCode, int sequenceNo, String commandId, String reservedUnits) {
    	this.cdrWithServiceType = cdrWithServiceType;
    	this.resultCode = resultCode;
    	this.sequenceNo = sequenceNo;
    	this.commandId = commandId;
    	this.reservedUnits = reservedUnits;
    }
    
    public String generateResponseString() {
    	return String.format("%s %s %d %s %s", this.cdrWithServiceType, 
    			                               this.resultCode, 
    			                               this.sequenceNo, 
    			                               this.commandId, 
    			                               this.reservedUnits);
    }
}
