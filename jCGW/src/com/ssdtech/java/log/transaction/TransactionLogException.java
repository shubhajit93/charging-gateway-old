package com.ssdtech.java.log.transaction;

public class TransactionLogException extends Exception {
	private static final long serialVersionUID = 1L;

	public TransactionLogException(String message, Throwable t) {
		super(message, t);
	}
}
