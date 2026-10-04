package com.ssdtech.java.model;

public enum PackageKeyPrefix {
	PRIMARY_KEY("pkg1"),
	SECONDARY_KEY("pkg2"),
	INVALID_KEY("pkg_invalid"),
	SETTER_KEY;
	
	private String prefix;
	PackageKeyPrefix(String prefix) {
		this.prefix = prefix;
	}
	
	PackageKeyPrefix() {}
	
	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}
	
	public String getPrefix() {
		return this.prefix;
	}
	
	public boolean equals(PackageKeyPrefix prefix) {
		return this.prefix.equals(prefix.getPrefix());
	}
}
