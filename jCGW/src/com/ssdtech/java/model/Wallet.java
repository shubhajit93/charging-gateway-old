package com.ssdtech.java.model;

import java.time.Instant;

import com.ssdtech.java.model.applicationproperties.ApplicationProperties;

public class Wallet {
	public static final String DEFAULT_PRODUCT_NAME = "na";
	
	private static final int TOTAL_NO_OF_SERIALIZED_FIELDS = 4;

	private static final int BALANCE_INDEX = 0;
	private static final int WALLET_EXPIRY_INDEX = 1;
	private static final int FREE_WALLET_INDEX = 2;
	private static final int FREE_WALLET_END_DATE_INDEX = 3;
    
	private final String id;
	private final String walletId;
	private final String bno;
	private final long balance;
	private final String productName;
	private  final String productcode;
	
	private final long walletExpirationDate;
	private boolean walletExpired;
	
	private final boolean freeWallet;
	private final long freeWalletEnddate;
	
	private boolean freeServicePeriod;

	public Wallet(final String id, final String walletId, final String bno, final long balance, final String productName, 
			          final long expirationDate, final boolean freeWallet, final long freeWalletEnddate, final String productcode) {
		this.id = id;
		this.walletId = walletId;
		this.bno = bno;
		this.balance = balance;
		this.productName = productName;
		
		long currentEpochSeconds = Instant.now().getEpochSecond();
		
		this.walletExpirationDate = expirationDate;
		this.walletExpired = currentEpochSeconds > expirationDate;
		
		this.freeWallet = freeWallet;
		this.freeWalletEnddate = freeWalletEnddate;
        this.freeServicePeriod = currentEpochSeconds < freeWalletEnddate;
        this.productcode = productcode;

	}
	
	public boolean hasInvalidId() {
		return this.id.equalsIgnoreCase("-1");
	}
	
	public String serialize() {
		String separator = ApplicationProperties.getSyncAppConfiguration().walletInfoSeparator.replace("\\", "");
		return this.balance + separator + this.walletExpirationDate + separator + (this.freeWallet ? "1" : "0") + separator + this.freeWalletEnddate;
	}
	
	public static Wallet getDummyWallet() {
		return new Wallet("-1", "-1", "-1", 0L, "na", 0L, false, 0L, "na");
	}
	
	public static Wallet deserialize(String walletId, String bno, String productName, String serializedWalletInfo) {
		if (serializedWalletInfo == null) return getDummyWallet();
		
		String[] parts = serializedWalletInfo.split(ApplicationProperties.getSyncAppConfiguration().walletInfoSeparator);
		if (parts.length < TOTAL_NO_OF_SERIALIZED_FIELDS) return getDummyWallet();
		
	    long balance = Long.valueOf(parts[BALANCE_INDEX]);
	    long walletExpiry = Long.valueOf(parts[WALLET_EXPIRY_INDEX]);
	    boolean freeWallet = parts[FREE_WALLET_INDEX].equals("1");
	    long freeWalletEndDate = Long.valueOf(parts[FREE_WALLET_END_DATE_INDEX]);
	    
	    return new Wallet("-1", walletId, bno, balance, productName, walletExpiry, freeWallet, freeWalletEndDate, "na");
	}
	
	public String getId() {
		return id;
	}
	
	public String getWalletId() {
		return this.walletId;
	}

	public String getBNO() {
		return this.bno;
	}

	public String getProductcode() {
		return this.productcode;
	}

	
	public long getBalance() {
		return this.balance;
	}
	
	public String getProductName() {
		return this.productName;
	}
	
	public boolean isFreeWallet() {
		return this.freeWallet;
	}
	
	public long getFreeWalletEndDate() {
		return this.freeWalletEnddate;
	}

	public boolean isInFreeServicePeriod() {
		return this.freeServicePeriod;
	}
	
	public long getWalletExpirationDate() {
		return this.walletExpirationDate;
	}
	
	public boolean isWalletExpired() {
		return this.walletExpired;
	}
}