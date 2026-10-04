package com.ssdtech.java.utils;

import java.util.StringJoiner;

import com.ssdtech.java.model.applicationproperties.ApplicationProperties;

public class RedisKey {
	private static final String CDR_RETRIEVAL_PREFIX = "cdr";
    private static final String CDR_PROCESSING_PREFIX = "cdr_processing";
	private static final String CDR_FAILED_PREFIX = "cdr_failed";
	private static final String CDR_INVALID_PREFIX = "cdr_invalid";
	private static final String CDR_PREFIX_SEPARATOR = ".";	
	
	public static String getKeyForCdrRetrieval(long epochSecond) {
		return CDR_RETRIEVAL_PREFIX + CDR_PREFIX_SEPARATOR + epochSecond;
	}
	
	public static String getWildKeyPatternForCdrRetrieval() {
		return CDR_RETRIEVAL_PREFIX + CDR_PREFIX_SEPARATOR + "*";
	}
	
	public static String getKeyForCdrProcessing(long epochSecond) {
		return CDR_PROCESSING_PREFIX + CDR_PREFIX_SEPARATOR + epochSecond;
	}
	
	public static String getKeyForCdrFailed(long id) {
		return CDR_FAILED_PREFIX + CDR_PREFIX_SEPARATOR + id;
	}
	
	public static String getKeyForCdrInvalid(long epochSecond) {
		return CDR_INVALID_PREFIX + CDR_PREFIX_SEPARATOR + epochSecond;
	}
	
	public static String getKeyForMultipleWalletRetrieval(String msisdn, String serviceId, String walletId, String bno) {
		StringJoiner joiner = new StringJoiner(ApplicationProperties.getSyncAppConfiguration().secondaryWalletInfoKeySeparator);
		return joiner.add(ApplicationProperties.getSyncAppConfiguration().secondaryWalletInfoKeyPrefix).add(msisdn).add(serviceId.toLowerCase()).add(walletId).add(bno).toString();
	}
	
	public static String getKeyForUniqueWalletRetrieval(String msisdn, String serviceId, String walletId, String bno, String productName) {
		StringJoiner joiner = new StringJoiner(ApplicationProperties.getSyncAppConfiguration().walletInfoKeySeparator);
		return joiner.add(ApplicationProperties.getSyncAppConfiguration().walletInfoKeyPrefix).add(msisdn).add(serviceId.toLowerCase()).add(walletId).add(bno).add(productName.toLowerCase()).toString();
	}
	
	public static String getKeyForSubscriptionInformation(String msisdn, String subscriptionRoot) {
		String prefix = ApplicationProperties.getSyncAppConfiguration().subscriptionInfoKeyPrefix;
		String separator = ApplicationProperties.getSyncAppConfiguration().subscriptionInfoKeySeparator;
		
		return prefix + separator + msisdn + separator + subscriptionRoot; 
	}
}
