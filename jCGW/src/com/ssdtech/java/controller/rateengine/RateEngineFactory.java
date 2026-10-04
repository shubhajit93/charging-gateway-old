package com.ssdtech.java.controller.rateengine;

public class RateEngineFactory {

	public static Event getRateEngine(String chargingType, int walletOption) {
		System.out.println("chargingType :"+chargingType+"|walletOption: "+walletOption);
		if (chargingType.equalsIgnoreCase("session_start")) {
			switch(walletOption) {
			case 1:
				return RateEngineForSessionStartEvent.WITH_WALLET;
			case 0:
				return RateEngineForSessionStartEvent.WITH_SUBSCRIPTION;
			default:
				return RateEngineForSessionStartEvent.WITHOUT_SUBSCRIPTION;
			}
		} else if (chargingType.equalsIgnoreCase("session_interim")) {
			switch(walletOption) {
			case 1:
				return RateEngineForSessionInterimEvent.WITH_WALLET;
			case 0:
				return RateEngineForSessionInterimEvent.WITH_SUBSCRIPTION;
			default:
				return RateEngineForSessionInterimEvent.WITHOUT_SUBSCRIPTION;
			}
		} else if (chargingType.equalsIgnoreCase("session_stop")) {
			switch(walletOption) {
			case 1:
				return RateEngineForSessionStopEvent.WITH_WALLET;
			case 0:
				return RateEngineForSessionStopEvent.WITH_SUBSCRIPTION;
			default:
				return RateEngineForSessionStopEvent.WITHOUT_SUBSCRIPTION;
			}
		} else {
			switch(walletOption) {
			case 1:
				return RateEngineForSpecificEvent.WITH_WALLET;
			case 0:
				return RateEngineForSpecificEvent.WITH_SUBSCRIPTION;
			default:
				return RateEngineForSpecificEvent.WITHOUT_SUBSCRIPTION;
			}
		}
	}
}
