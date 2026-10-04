package com.ssdtech.java.model.applicationproperties;

import java.util.Properties;
import java.util.StringJoiner;

public class SyncAppConfiguration {
    public final String syncAppQueueName;
    
    public final String subscriptionInfoSeparator;
        
    public final String subscriptionInfoKeyPrefix;
    public final String subscriptionInfoKeySeparator;
    
    public final String walletInfoSeparator;
    
    public final String walletInfoKeyPrefix;
    public final String walletInfoKeySeparator;
    
    public final String secondaryWalletInfoKeyPrefix;
    public final String secondaryWalletInfoKeySeparator;
    
    public SyncAppConfiguration(Properties properties) {
    	String syncAppQueueName = properties.getProperty("SYNC_APP_QUEUE_NAME");
    	
    	String subscriptionInfoSeparator = properties.getProperty("SUBSCRIPTION_INFO_SEPARATOR", "\\|");
    	String subscriptionInfoKeyPrefix = properties.getProperty("SUBSCRIPTION_INFO_KEY_PREFIX", "ss");
    	String subscriptionInfoKeySeparator = properties.getProperty("SUBSCRIPTION_INFO_KEY_SEPARATOR", ":");
    	
    	String walletInfoSeparator = properties.getProperty("WALLET_INFO_VALUE_SEPARATOR", "\\|");
    	String walletInfoKeyPrefix = properties.getProperty("WALLET_INFO_KEY_PREFIX", "w");
    	String walletInfoKeySeparator = properties.getProperty("WALLET_INFO_KEY_SEPARATOR", ":");
    	
    	String secondaryWalletInfoKeyPrefix = properties.getProperty("SECONDARY_WALLET_INFO_KEY_PREFIX", "ws");
    	String secondaryWalletInfoKeySeparator = properties.getProperty("SECONDARY_WALLET_INFO_KEY_SEPARATOR", ":");
    	
    	this.syncAppQueueName = syncAppQueueName;
    	this.subscriptionInfoSeparator = subscriptionInfoSeparator;
    	this.subscriptionInfoKeyPrefix = subscriptionInfoKeyPrefix;
    	this.subscriptionInfoKeySeparator = subscriptionInfoKeySeparator;
    	
    	this.walletInfoSeparator = walletInfoSeparator;
    	this.walletInfoKeyPrefix = walletInfoKeyPrefix;
    	this.walletInfoKeySeparator = walletInfoKeySeparator;
    	this.secondaryWalletInfoKeyPrefix = secondaryWalletInfoKeyPrefix;
    	this.secondaryWalletInfoKeySeparator = secondaryWalletInfoKeySeparator;
    }
    
    @Override
    public String toString() {
    	return new StringJoiner("\r\n")
    			       .add("\r\n\r\n[MySQL-Redis SYNC application configuration]")
    		           .add("SYNC_APP_QUEUE_NAME = " + this.syncAppQueueName)
    		           .add("SUBSCRIPTION_INFO_SEPARATOR = " + this.subscriptionInfoSeparator)
    		           .add("SUBSCRIPTION_INFO_KEY_PREFIX = " + this.subscriptionInfoKeyPrefix)
    		           .add("SUBSCRIPTION_INFO_KEY_SEPARATOR = " + this.subscriptionInfoKeySeparator)
    	               .add("WALLET_INFO_VALUE_SEPARATOR = " + this.walletInfoSeparator)
    	               .add("WALLET_INFO_KEY_PREFIX = " + this.walletInfoKeyPrefix)
    	               .add("WALLET_INFO_KEY_SEPARATOR = " + this.walletInfoKeySeparator)
    	               .add("SECONDARY_WALLET_INFO_KEY_PREFIX = " + this.secondaryWalletInfoKeyPrefix)
    	               .add("SECONDARY_WALLET_INFO_KEY_SEPARATOR = " + this.secondaryWalletInfoKeySeparator)
    	               .toString();
    }
}
