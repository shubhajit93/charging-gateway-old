package com.ssdtech.java.model;

public class ApplicationInfo {
	private final String id;
	private final String name;
	private final String password;
	private final boolean requirePassword;
	private final boolean requireOAuth;
	
	public ApplicationInfo(final String id, final String name, final String password, 
			                   final boolean requirePassword, final boolean requireOAuth) {
		this.id = id;
		this.name = name;
		this.password = password;
		this.requirePassword = requirePassword;
		this.requireOAuth = requireOAuth;
	}
	
	public String getId() {
		return this.id;
	}
	
	public String getName() {
		return this.name;
	}
	
	public String getPassword() {
		return this.password;
	}
	
	public boolean isRequirePassword() {
		return this.requirePassword;
	}
	
	public boolean isRequireOAuth() {
		return this.requireOAuth;
	}
}