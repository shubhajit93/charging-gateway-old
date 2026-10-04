package com.ssdtech.java.model;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.ssdtech.java.model.SessionInfo;

public class SessionInfo {
	private boolean dummySession;
	
	private String rateId;
	private int currentWalletType;

	private long totalConsumedUnit; // Total allocated pulse.
	private long lastReservedUnit; // Last allocated pulse.

	private long totalchargedAmount;
	private long lastChargedAmount;

	private String subscriptionStatus;

	private int nextStepNo;
	private long pulseLimitOfCurrentStepNo;

	private String sessionStartTime;
	
	private Wallet wallet; 
	
	
	public boolean isDummySession() {
		return this.dummySession;
	}
	
	public String getRateId() {
		return this.rateId;
	}
	
	public int getCurrentWalletType() {
		return this.currentWalletType;
	}

	public long getTotalConsumedUnit() {
		return this.totalConsumedUnit;
	}

	public long getLastReservedUnit() {
		return this.lastReservedUnit;
	}

	public long getTotalChargedAmount() {
		return this.totalchargedAmount;
	}
	
	public long getLastChargedAmount() {
		return this.lastChargedAmount;
	}

	public String getSubscriptionStatus() {
		return this.subscriptionStatus;
	}

	public int getNextStepNo() {
		return this.nextStepNo;
	}

	public long getPulseLimitOfCurrentStepNo() {
		return this.pulseLimitOfCurrentStepNo;
	}

	public String getSessionStartTime() {
		return this.sessionStartTime;
	}
	
	public Wallet getWallet() {
		return this.wallet;
	}
	
	@Override
	public String toString() {
		return "Rate id: " + this.rateId + ", " +
	           "Current wallet: " + (this.currentWalletType == 0 ? "SERVICE" : "COMMON") + ", " +
			   "Total consumed unit: " + this.totalConsumedUnit + ", " +
	           "Last reserved unit: " + this.lastReservedUnit + ", " +
			   "Total charged amount: " + this.totalchargedAmount + ", " +
	           "Last charged amount: " + this.lastChargedAmount + ", " +
			   "Subscription status: " + this.subscriptionStatus + ", " +
	           "Next step no: " + this.nextStepNo + ", " +
			   "Pulse limit of current step no: " + this.pulseLimitOfCurrentStepNo + ", " +
	           "Session start time: " + this.sessionStartTime + ", " +
			   "Wallet id: " + this.wallet.getId() + ", " +
	           "Wallet balance: " + this.wallet.getBalance() + ", " +
			   "Free wallet: " + this.wallet.isFreeWallet() + ", " +
	           "Free wallet enddate: " + this.wallet.getFreeWalletEndDate();
	}
	
	public static SessionInfo getDummySession() {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		
		return new SessionInfo()
	             .setTotalConsumedUnit(0L)
	             .setLastReservedUnit(0L)
	             .setTotalChargedAmount(0L)
	             .setLastChargedAmount(0L)
	             .setSubscriptionStatus("0")
	             .setNextStepNo(0)
	             .setPulseLimitOfCurrentStepNo(0)
	             .setSessionStartTime(formatter.format(new Date()))
	             .setCurrentWalletType(-1)
	             .setWallet(Wallet.getDummyWallet());
	}
	
	public SessionInfo setAsDummySession() {
		this.dummySession = true;
		return this;
	}
	
	public SessionInfo setRateId(String rateId) {
		this.rateId = rateId;
		return this;
	}
	
	public SessionInfo setCurrentWalletType(int walletType) {
		this.currentWalletType = walletType;
		return this;
	}
	
	public SessionInfo setTotalConsumedUnit(long totalConsumedUnit) {
		this.totalConsumedUnit = totalConsumedUnit;
		return this;
	}

	public SessionInfo setLastReservedUnit(long lastReservedUnit) {
		this.lastReservedUnit = lastReservedUnit;
		return this;
	}
	
	public SessionInfo setTotalChargedAmount(long totalchargedAmount) {
		this.totalchargedAmount = totalchargedAmount;
		return this;
	}
	
	public SessionInfo setLastChargedAmount(long lastChargedAmount) {
		this.lastChargedAmount = lastChargedAmount;
		return this;
	}
	
	public SessionInfo setSubscriptionStatus(String subscriptionStatus) {
		this.subscriptionStatus = subscriptionStatus;
		return this;
	}
	
	public SessionInfo setNextStepNo(int nextStepNo) {
		this.nextStepNo = nextStepNo;
		return this;
	}
	
	public SessionInfo setPulseLimitOfCurrentStepNo(long pulseLimitOfCurrentStepNo) {
		this.pulseLimitOfCurrentStepNo = pulseLimitOfCurrentStepNo;
		return this;
	}
	
	public SessionInfo setSessionStartTime(String sessionStartTime) {
		this.sessionStartTime = sessionStartTime;
		return this;
	}
	
    public SessionInfo setWallet(Wallet wallet) {
    	this.wallet = wallet;
    	return this;
    }
}
