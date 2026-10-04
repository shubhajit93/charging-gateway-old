package com.ssdtech.java.model;

public class PackageInfo {
	private String packageId;
	private String packageIdFromOperator;
	private String nodeId;
	private long subscriberBalance;
	private ResultCode resultCode;
	
	public PackageInfo() {
		// Set optional values. 
		// [When ApplicationProperties.getBillingInterfaceConf().packageCheckWithBalance is False.]
		this.packageIdFromOperator = "";
		this.subscriberBalance = 0L;
	}
	
	public PackageInfo(String packageIdFromOperator, long subscriberBalance, ResultCode resultCode) {
		this.packageIdFromOperator = packageIdFromOperator;
		this.subscriberBalance = subscriberBalance;
		this.resultCode = resultCode;
	}
	
    public String getPackageId() {
    	return this.packageId;
    }
    
    public String getPackageIdFromOperator() {
    	return this.packageIdFromOperator;
    }
    
    public String getNodeId() {
    	return this.nodeId;
    }
    
    public long getSubscriberBalance() {
    	return this.subscriberBalance;
    }
    
    public ResultCode getResultCode() {
    	return this.resultCode;
    }
    
    public void setPackageId(String packageId) {
    	this.packageId = packageId.toLowerCase();
    }
    
    public void setNodeId(String nodeId) {
    	this.nodeId = nodeId.toLowerCase();
    }
    
    public void setResultCode(ResultCode resultCode) {
    	this.resultCode = resultCode;
    }
    
    public void setResultCode(ResultCode resultCode, String alphaNumericCode) {
    	this.resultCode = resultCode;
    	this.resultCode.setAlphaNumericCode(alphaNumericCode);
    }
}
