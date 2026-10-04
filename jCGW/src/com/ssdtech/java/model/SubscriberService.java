package com.ssdtech.java.model;

import java.util.Optional;

import com.ssdtech.java.model.applicationproperties.ApplicationProperties;
import com.ssdtech.java.utils.SubscriptionStatusMapping;
import com.ssdtech.java.utils.Tuple;

public class SubscriberService {
	private static final int TOTAL_NO_OF_SERIALIZED_FIELDS = 4;

	private static final int STATUS_INDEX = 0;
	private static final int REGISTRATION_DATE_INDEX = 1;
	private static final int SUBSCRIPTION_GROUP_ID_INDEX = 2;
	private static final int CHANNEL_INDEX = 3;
	
	public final String subscriptionGroupId;
	public final String status;
	private Tuple<String, String> mappedSubscriptionStatus;
	public final String channel;

	public SubscriberService(String subscriptionGroupId, String status, String channel) {
		this.subscriptionGroupId = subscriptionGroupId;
		this.status = status;
		this.channel = channel;
		this.mappedSubscriptionStatus = SubscriptionStatusMapping.getMapping(status);
	}
	
	public SubscriberService(String subscriptionGroupId, String channel) {
		this.subscriptionGroupId = subscriptionGroupId;
		this.status = "na";
		this.channel = channel;
		this.mappedSubscriptionStatus = new Tuple<>("na", "0");
	}
	
	public String getModifiedSubscriptionStatus() {
		return this.mappedSubscriptionStatus.first;
	}
	
	public String getNumericSubscriptionStatus() {
		return this.mappedSubscriptionStatus.second;
	}
	
	@Override
	public String toString() {
		return "Status: " + this.status + ", " +
	           "Subscription Group ID: " + this.subscriptionGroupId + ", " +
	           "Channel: " + this.channel + ", " +
	           "Mapped status: " + this.mappedSubscriptionStatus.first + ", " +
	           "Mapped numeric status: " + this.mappedSubscriptionStatus.second;
	}
	
	public void updateMapping(boolean hasBalanceOption, boolean isInFreeServicePeriod, long remainingBalance) {
		String modifiedSubscriptionStatus = this.status;
		if (hasBalanceOption && !isInFreeServicePeriod) {
		    if (remainingBalance > 0) {
		    	if (this.status == null ||
		    			this.status.equals("na") ||
		    			this.status.equals("deregistered") || 
		    			this.status.equals("renewalfailed")) {
		    		modifiedSubscriptionStatus = "notregisteredbalance";
		    	}
		    } else {
		    	if (this.status != null && 
		    			!this.status.equals("na") &&
		    			!this.status.equals("deregistered") && 
		    			    !this.status.equals("renewalfailed")) {
		    		modifiedSubscriptionStatus = "registerednobalance";
		    	}
		    }
		}
		
		this.mappedSubscriptionStatus = SubscriptionStatusMapping.getMapping(modifiedSubscriptionStatus);
	}
	
	public static Optional<SubscriberService> getInstance(String serializedSubscriptionInfo, String requestedSubGroupId, String requestedChannel) {
		if (serializedSubscriptionInfo == null) return Optional.empty();
		
		String[] parts = serializedSubscriptionInfo.split(ApplicationProperties.getSyncAppConfiguration().subscriptionInfoSeparator);
		if (parts.length < TOTAL_NO_OF_SERIALIZED_FIELDS) return Optional.empty();
		
		String subscriptionGroupId = parts[SUBSCRIPTION_GROUP_ID_INDEX].toLowerCase();
		String status = parts[STATUS_INDEX].toLowerCase();
		String channel = parts[CHANNEL_INDEX].toLowerCase();
		
		if (!subscriptionGroupId.trim().isEmpty()
				&& (requestedSubGroupId.trim().isEmpty() || requestedSubGroupId.equalsIgnoreCase("na"))) {
			requestedSubGroupId = subscriptionGroupId;
		}
		
		if (!channel.trim().isEmpty()) {
			requestedChannel = channel;
		}
		
		return Optional.of(new SubscriberService(requestedSubGroupId, status, requestedChannel));
	}
}