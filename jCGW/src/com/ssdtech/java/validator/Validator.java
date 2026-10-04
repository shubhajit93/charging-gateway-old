package com.ssdtech.java.validator;

public class Validator {
	
	public static void validateNumeric(String propertyName, String number) throws IllegalArgumentException {
		if (number == null || number.trim().isEmpty()) {
			throw new IllegalArgumentException("Property [" + propertyName + "] :: Number is empty or null");
		}
		
		for(byte digit : number.trim().getBytes()) {
			if (digit < 48 && digit > 57) {
				throw new IllegalArgumentException("Property [" + propertyName + "] :: " + number + " is not a valid number");
			}
		}
	}
	
	public static void validateRange(String propertyName, int value, int minValue, int maxValue) throws IllegalArgumentException {
		if (value < minValue || value > maxValue) {
			throw new IllegalArgumentException("Property [" + propertyName + "] :: Value " + value + " is not in range [" + minValue + ", " + maxValue + "]");
		}
	}
	
	public static void validateRange(String propertyName, long value, long minValue, long maxValue) throws IllegalArgumentException {
		if (value < minValue || value > maxValue) {
			throw new IllegalArgumentException("Property [" + propertyName + "] :: Value " + value + " is not in range [" + minValue + ", " + maxValue + "]");
		}
	}
	
	public static void validateRange(String propertyName, double value, double minValue, double maxValue) throws IllegalArgumentException {
		if (value < minValue || value > maxValue) {
			throw new IllegalArgumentException("Property [" + propertyName + "] :: Value " + value + " is not in range [" + minValue + ", " + maxValue + "]");
		}
	}
}
