package com.ssdtech.robi.model;

public class ServiceInfo {
    private final String serviceCode;
    private final String spID;
    private final String spPassword;
    private final String productID;
	
	public ServiceInfo(String serviceCode, String spID, String spPassword, String productID) {
		this.serviceCode = serviceCode.trim();
		this.spID = spID.trim();
		this.spPassword = spPassword.trim();
		this.productID = productID.trim();
	}
	
	public String getServiceCode() {
		return this.serviceCode;
	}
	
	public String getSPID() {
		return this.spID;
	}
	
	public String getSPPassword() {
		return this.spPassword;
	}
	
	public String getProductID() {
		return this.productID;
	}
}
