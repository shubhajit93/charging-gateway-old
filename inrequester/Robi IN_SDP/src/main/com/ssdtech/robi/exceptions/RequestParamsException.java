package com.ssdtech.robi.exceptions;

public class RequestParamsException extends Exception {
	private static final long serialVersionUID = 1L;
	private String errorCode = "-1"; // Invalid RequestParamsException.
	
	public RequestParamsException(String message, String errorCode) {
		super(message);
		this.errorCode = errorCode;
	}
	
	public String getErrorCode() {
		return this.errorCode;
	}
}
