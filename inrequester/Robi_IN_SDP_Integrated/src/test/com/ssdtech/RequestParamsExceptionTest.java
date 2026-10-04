package com.ssdtech;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.runner.RunWith;

import com.ssdtech.robi.exceptions.RequestParamsException;

@RunWith(JUnitParamsRunner.class)
public class RequestParamsExceptionTest {
    public static String DEFAULT_ERROR_CODE = "-1";
	public static String DEFAULT_ERROR_MESSAGE = "Default message";
    
	public static Object[] getErrorMessage() {
		return new Object[] {
				null,
				"",
				" ",
				"   ",
				"Custom message",
		};
	}
	
	public static Object[] getErrorCode() {
		return new Object[] {
				null,
				"",
				" ",
				"   ",
				"31415"
		};
	}
	
	@Test
	@Parameters(method = "getErrorMessage")
	public void checkIfMessageIsSetCorrectly(String message) {
		RequestParamsException rpe = new RequestParamsException(message, DEFAULT_ERROR_CODE);
		assertEquals("Custom error message doesn't be set", message, rpe.getMessage());
	}
	
	@Test
	@Parameters(method = "getErrorCode")
	public void checkIfErrorCodeSetCorrectly(String errorCode) {
		RequestParamsException rpe = new RequestParamsException(DEFAULT_ERROR_MESSAGE, errorCode);
		assertEquals("Error code can not be set", errorCode, rpe.getErrorCode());
	}
}
