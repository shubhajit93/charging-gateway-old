package com.ssdtech.java.model;

import java.util.Optional;

public class ServiceInfo {
	private Optional<WalletConf> wConf;
	private Optional<RegionConf> rConf;
	private boolean contentGift;
	
	public ServiceInfo() {
		this.wConf = Optional.empty();
		this.rConf = Optional.empty();
		this.contentGift = false;
	}
	
	public Optional<WalletConf> getWalletConf() {
		return this.wConf;
	}
	
	public Optional<RegionConf> getRegionConf() {
		return this.rConf;
	}
	
	public boolean contentGiftingEnable() {
		return this.contentGift;
	}
	
	public void setWalletConfiguration(boolean hasServiceWallet, boolean hasCommonWallet, String walletId, String walletName) {
		this.wConf = Optional.of(new WalletConf(hasServiceWallet, hasCommonWallet, walletId, walletName));
	}
	
	public void setRegionConfiguration(String host, int port, String pathSegment) {
		this.rConf = Optional.of(new RegionConf(host, port, pathSegment));
	}
	
	public void enableContentGifting() {
		this.contentGift = true;
	}
	
	public class WalletConf {
		public final boolean hasServiceWallet;
		public final boolean hasCommonWallet;
		public final String walletId;
		public final String walletName;
		
		public WalletConf(final boolean hasServiceWallet, final boolean hasCommonWallet,
				              final String walletId, final String walletName) {
			this.hasServiceWallet = hasServiceWallet;
			this.hasCommonWallet = hasCommonWallet;
			this.walletId = walletId;
			this.walletName = walletName;
		}
	}
	
	public class RegionConf {
		public final String host;
		public final int port;
		public final String pathSegment;
		
		public RegionConf(final String host, final int port, final String pathSegment) {
			this.host = host;
			this.port = port;
			this.pathSegment = pathSegment;
		}
	}
}
