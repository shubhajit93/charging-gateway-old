package com.ssdtech.java.model.applicationproperties;

import java.util.Properties;
import java.util.StringJoiner;

import com.ssdtech.java.utils.Utility;

public class BillingInterfaceConfiguration {
	public final boolean isINIntegrated;
	
	public final String defaultBillingHost;
	public final int defaultBillingPort;
	
	public final String packageHostIp;
	public final int packageHostPort;
	
	public final boolean packageCheckForSession;
	public final boolean packageCheckForSpecific;
	
	public final boolean packageCheckWithBalance;
	public final int noOfRetryForPackageCheckWithBalance;
	
	public final String prepaidIp;
	public final int prepaidPort;
	
	public final String postpaidIp;
	public final int postpaidPort;
	
	public final boolean useRemarks;
	
	public final long receiveTimeout;
	
    public BillingInterfaceConfiguration(Properties properties) {
    	String isINIntegrated = Utility.ptrim(properties, "IN_INTEGRATED");
    	String defaultBillingHost = Utility.ptrim(properties, "DEFAULT_BILLING_HOST");
    	String defaultBillingPort = Utility.ptrim(properties, "DEFAULT_BILLING_PORT");
    	String packageHostIp = Utility.ptrim(properties, "PACKAGE_HOST_IP");
    	String packageHostPort = Utility.ptrim(properties, "PACKAGE_HOST_PORT");
    	String packageCheckForSession = Utility.ptrim(properties, "PACKAGE_CHECK_ENABLED_SESSION");
    	String packageCheckForSpecific = Utility.ptrim(properties, "PACKAGE_CHECK_ENABLED_SPECIFIC");
    	String packageCheckWithBalance = Utility.ptrim(properties, "PACKAGE_CHECK_ENABLED_WITH_BALANCE");
    	String noOfRetryForPackageCheckWithBalance = Utility.ptrim(properties, "NUMBER_OF_RETRY_TO_FIND_PACKAGE");
    	String prepaidIp = Utility.ptrim(properties, "PREPAID_HOST_IP", "10.175.33.97");
    	String prepaidPort = Utility.ptrim(properties, "PREPAID_HOST_PORT", "10010");
    	String postpaidIp = Utility.ptrim(properties, "POSTPAID_HOST_IP", "10.10.17.92");
    	String postpaidPort = Utility.ptrim(properties, "POSTPAID_HOST_PORT", "3240");
    	String useRemarks = Utility.ptrim(properties, "SHOULD_USE_REMARKS", "0");
    	String receiveTimeout = Utility.ptrim(properties, "RECV_TIMEOUT");
    	
    	this.isINIntegrated = isINIntegrated.equals("1");
    	this.defaultBillingHost = defaultBillingHost;
    	this.defaultBillingPort = Integer.valueOf(defaultBillingPort);
    	this.packageHostIp = packageHostIp;
    	this.packageHostPort = Integer.valueOf(packageHostPort);
    	this.packageCheckForSession = packageCheckForSession.equals("1");
    	this.packageCheckForSpecific = packageCheckForSpecific.equals("1");
    	this.packageCheckWithBalance = packageCheckWithBalance.equals("1");
    	this.noOfRetryForPackageCheckWithBalance = Integer.valueOf(noOfRetryForPackageCheckWithBalance);
    	this.prepaidIp = prepaidIp;
    	this.prepaidPort = Integer.valueOf(prepaidPort);
    	this.postpaidIp = postpaidIp;
    	this.postpaidPort = Integer.valueOf(postpaidPort);
    	this.useRemarks = useRemarks.equals("1");
    	this.receiveTimeout = Integer.valueOf(receiveTimeout);
    }
    
    @Override
    public String toString() {
    	return new StringJoiner("\r\n")
    			   .add("\r\n\r\n[Billing Interface Configuration]")
    			   .add("IN_INTEGRATED = " + Utility.getEncodedValueForBooleanProperties(this.isINIntegrated))
	    	       .add("DEFAULT_BILLING_HOST = " + this.defaultBillingHost)
	    	       .add("DEFAULT_BILLING_PORT = " + this.defaultBillingPort)
	    	       .add("PACKAGE_HOST_IP = " + this.packageHostIp)
	    	       .add("PACKAGE_HOST_PORT = " + this.packageHostPort)
	    	       .add("PACKAGE_CHECK_ENABLED_SESSION = " + Utility.getEncodedValueForBooleanProperties(this.packageCheckForSession))
	    	       .add("PACKAGE_CHECK_ENABLED_SPECIFIC = " + Utility.getEncodedValueForBooleanProperties(this.packageCheckForSpecific))
	    	       .add("PACKAGE_CHECK_ENABLED_WITH_BALANCE = " + Utility.getEncodedValueForBooleanProperties(this.packageCheckWithBalance))
	    	       .add("NUMBER_OF_RETRY_TO_FIND_PACKAGE = " + this.noOfRetryForPackageCheckWithBalance)
	    	       .add("PREPAID_HOST_IP = " + this.prepaidIp)
	    	       .add("PREPAID_HOST_PORT = " + this.prepaidPort)
	    	       .add("POSTPAID_HOST_IP = " + this.postpaidIp)
	    	       .add("POSTPAID_HOST_PORT = " + this.postpaidPort)
	    	       .add("SHOULD_USE_REMARKS = " + Utility.getEncodedValueForBooleanProperties(this.useRemarks)) 
	    	       .add("RECV_TIMEOUT = " + this.receiveTimeout)
	    	       .toString();
    }
}
