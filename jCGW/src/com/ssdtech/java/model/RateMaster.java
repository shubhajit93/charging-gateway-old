package com.ssdtech.java.model;

public class RateMaster {
	public final int cardinality;
    public final String rateId;
    public final boolean hasCampaignRate;
    public final long billingNodeId;
    public final int priority;
    public final String channel;
    public final String registeredService;
    public final String subscriptionGroupId;
    public final String subscriptionStatus;
    
    public RateMaster(final int cardinality, final String rateId, final boolean hasCampaignRate, final long billingNodeId, final int priority, 
    		final String channel, final String registeredService, final String subscriptionGroupId, final String subscriptionStatus) {
    	this.cardinality = cardinality;
    	this.rateId = rateId;
    	this.hasCampaignRate = hasCampaignRate;
    	this.billingNodeId = billingNodeId;
    	this.priority = priority;
    	this.channel = channel;
    	this.registeredService = registeredService;
    	this.subscriptionGroupId = subscriptionGroupId;
    	this.subscriptionStatus = subscriptionStatus;
    }

	public int getPriority() {
		return priority;
	}

	@Override
    public String toString() {
    	return "Rate id: " + this.rateId + ", " +
    	       "Has Campaign rate: " + this.hasCampaignRate + ", " +
    		   "Billing Node Id: " + this.billingNodeId + ", " + 
               "Priority: " + this.priority + ", " +
               "Channel: " + this.channel + ", " +
               "Registered Service: " + this.registeredService + ", " +
               "Subscription Group Id: " + this.subscriptionGroupId + ", " +
               "Subscription Status: " + this.subscriptionStatus + ", " +
               "Cardinality: " + this.cardinality;
    }
}
