package com.ssdtech.java.model;

public class SubscriptionGroup {
    private String subscriptionGroupId;
    private String parentId;
    private String serviceId;
    private boolean balanceOption;
    
    public SubscriptionGroup(String subscriptionGroupId, String parentId, String serviceId, boolean balanceOption) {
    	this.subscriptionGroupId = subscriptionGroupId;
    	this.parentId = parentId;
    	this.serviceId = serviceId;
    	this.balanceOption = balanceOption;
    }
    
    public String getSubscriptionGroupId() {
    	return this.subscriptionGroupId;
    }
    
    public String getParentId() {
    	return this.parentId;
    }
    
    public String getServiceId() {
    	return this.serviceId;
    }
    
    public boolean hasBalanceOption() {
    	return this.balanceOption;
    }

}
