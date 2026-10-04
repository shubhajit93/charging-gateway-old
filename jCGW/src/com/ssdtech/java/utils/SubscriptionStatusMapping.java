package com.ssdtech.java.utils;

import java.util.HashMap;
import java.util.Map;

public class SubscriptionStatusMapping {
	private final static Map<String, String> subscriptionStatusMapping;
	static {
		subscriptionStatusMapping = new HashMap<>();
		subscriptionStatusMapping.put("na", "0");
		subscriptionStatusMapping.put("deregistered", "1");
		subscriptionStatusMapping.put("registered", "2");
		subscriptionStatusMapping.put("registerednobalance", "3");
		subscriptionStatusMapping.put("notregisteredbalance", "4");
		subscriptionStatusMapping.put("ingraceperiod", "5");
	}
	
	public static Tuple<String, String> getMapping(String subscriptionStatus) {
		String numericSubscriptionStatus = "0";
		if (subscriptionStatusMapping.containsKey(subscriptionStatus)) {
			numericSubscriptionStatus = subscriptionStatusMapping.get(subscriptionStatus);
		}
	    return new Tuple<>(subscriptionStatus, numericSubscriptionStatus);
	}
}
