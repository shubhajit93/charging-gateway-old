package com.ssdtech.java.model.applicationproperties;

import java.util.Properties;
import java.util.StringJoiner;

import com.ssdtech.java.utils.Utility;

public class ServerSpecificConfiguration {
	public final String cgwId;
	public final String userId;
	public final int listeningPort;
	public final int commandListeningPort;
	public final boolean loadPackageTable;
	public final int noOfThreads;
	public final boolean parallelBillingRequest;
	public final boolean outputSubscriptionStatus;
	public final boolean cacheEnabledForWallet;
	public final boolean retrieveSubscriptionInfoUsingInMemDB;
	
	public ServerSpecificConfiguration(Properties properties) {
		String cgwId = Utility.ptrim(properties, "CGWID");
		String userId = Utility.ptrim(properties, "UserID", "AdminISMP2.0V0.1.0");
		String listeningPort = Utility.ptrim(properties, "LISTENING_PORT");
		String commandListeningPort = Utility.ptrim(properties, "COMMAND_LISTENING_PORT");
		String loadPackageTable = Utility.ptrim(properties, "LOAD_PACKAGE_TABLE");
		String noOfThreads = Utility.ptrim(properties, "NO_OF_THREADS");
		String parallelBillingRequest = Utility.ptrim(properties, "PARALLEL_BILLING_REQUEST", "1");
		String outputSubscriptionStatus = Utility.ptrim(properties, "REGISTRATION_OUTPUT_ENABLED", "0");
		String cacheEnabledForWallet = Utility.ptrim(properties, "CACHE_ENABLED_FOR_WALLET", "1");
		String retrieveSubscriptionInfoUsingInMemDB = Utility.ptrim(properties, "RETRIEVE_SUBSCRIPTION_INFO_USING_IN_MEM_DB");
		
		this.cgwId = cgwId;
		this.userId = userId;
		this.listeningPort = Integer.valueOf(listeningPort);
		this.commandListeningPort = Integer.valueOf(commandListeningPort);
		this.loadPackageTable = loadPackageTable.equals("1");
		this.noOfThreads = Integer.valueOf(noOfThreads);
		this.parallelBillingRequest = (parallelBillingRequest == null) || parallelBillingRequest.trim().equals("1");
		this.outputSubscriptionStatus = (outputSubscriptionStatus != null) && outputSubscriptionStatus.trim().equals("1");
		this.cacheEnabledForWallet = cacheEnabledForWallet.equals("1");
		this.retrieveSubscriptionInfoUsingInMemDB = retrieveSubscriptionInfoUsingInMemDB.trim().equals("1");
	}
	
	@Override
	public String toString() {
		return new StringJoiner("\r\n")
				       .add("\r\n[Server Specific configuration]")
				       .add("CGWID = " + this.cgwId)
				       .add("UserID = " + this.userId)
				       .add("LISTENING_PORT = " + this.listeningPort)
				       .add("COMMAND_LISTENING_PORT = " + this.commandListeningPort)
				       .add("LOAD_PACKAGE_TABLE = " + Utility.getEncodedValueForBooleanProperties(this.loadPackageTable))
				       .add("NO_OF_THREADS = " + this.noOfThreads)
				       .add("PARALLEL_BILLING_REQUEST = " + Utility.getEncodedValueForBooleanProperties(this.parallelBillingRequest))
				       .add("REGISTRATION_OUTPUT_ENABLED = " + Utility.getEncodedValueForBooleanProperties(this.outputSubscriptionStatus))
				       .add("CACHE_ENABLED_FOR_WALLET = " + Utility.getEncodedValueForBooleanProperties(this.cacheEnabledForWallet))
				       .add("RETRIEVE_SUBSCRIPTION_INFO_USING_IN_MEM_DB = " + Utility.getEncodedValueForBooleanProperties(this.retrieveSubscriptionInfoUsingInMemDB))
				       .toString();
	}
}
