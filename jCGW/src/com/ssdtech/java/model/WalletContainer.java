package com.ssdtech.java.model;

import java.util.ArrayList;
import java.util.List;

public class WalletContainer {
    private Wallet updatedWallet;
    private List<String> updatedWalletIds;
    private List<Long> croppedBalances;
    private long updatedPulseSize;
    private int walletType;
    
    public WalletContainer() {
    	this.updatedWalletIds = new ArrayList<>();
    	this.croppedBalances = new ArrayList<>();
    }
    
    public WalletContainer setUpdatedWallet(Wallet updatedWallet) {
    	this.updatedWallet = updatedWallet;
    	return this;
    }
    
    public WalletContainer addWalletId(String walletId) {
    	this.updatedWalletIds.add(walletId);
    	return this;
    }
    
    public WalletContainer addCroppedBalance(long balance) {
    	this.croppedBalances.add(balance);
    	return this;
    }
    
    public WalletContainer setUpdatedPulseSize(long pulseSize) {
    	this.updatedPulseSize = pulseSize;
    	return this;
    }
    
    public WalletContainer setWalletType(int walletType) {
    	this.walletType = walletType;
    	return this;
    }
    
    public Wallet getUpdatedWallet() {
    	return this.updatedWallet;
    }
    
    public List<String> getUpdatedWalletIds() {
    	return this.updatedWalletIds;
    }
    
    public List<Long> getCroppedBalances() {
    	return this.croppedBalances;
    }
    
    public long getUpdatedPulseSize() {
    	return this.updatedPulseSize;
    }
    
    public int getWalletType() {
    	return this.walletType;
    }
}
