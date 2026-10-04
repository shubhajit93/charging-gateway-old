package com.ssdtech.java.controller;

import java.util.Optional;

import com.ssdtech.java.db.client.DBClient;
import com.ssdtech.java.model.DebugPoint;
import com.ssdtech.java.model.ProcedureParams;
import com.ssdtech.java.model.RatingInfo;
import com.ssdtech.java.model.SessionInfo;
import com.ssdtech.java.model.Wallet;
import com.ssdtech.java.model.applicationproperties.ApplicationProperties;
import com.ssdtech.java.utils.Utility;

public class SessionManagement {
		
	public static Optional<SessionInfo> loadSessionInfo(ProcedureParams params)  {
		String sessionInfo = DBClient.getAggregatedSessionInfo(params.getRequest().getCDRId());
		if (sessionInfo == null) {
			return Optional.empty();
		}
		
		String[] sessionInfoParts = sessionInfo.split("::");
		if (sessionInfoParts.length < 15) {
			return Optional.empty();
		}
	
		SessionInfo si = new SessionInfo()
					             .setRateId(sessionInfoParts[0])
					             .setTotalConsumedUnit(Long.valueOf(sessionInfoParts[1]))
					             .setLastReservedUnit(Long.valueOf(sessionInfoParts[2]))
					             .setTotalChargedAmount(Long.valueOf(sessionInfoParts[3]))
					             .setLastChargedAmount(Long.valueOf(sessionInfoParts[4]))
					             .setSubscriptionStatus(sessionInfoParts[5])
					             .setNextStepNo(Integer.valueOf(sessionInfoParts[6]))
					             .setPulseLimitOfCurrentStepNo(Long.valueOf(sessionInfoParts[7]))
					             .setSessionStartTime(sessionInfoParts[8])
					             .setCurrentWalletType(Integer.valueOf(sessionInfoParts[9]));
		
		if (!ApplicationProperties.getServerSpecificConfiguration().retrieveSubscriptionInfoUsingInMemDB 
				&& ApplicationProperties.getServerSpecificConfiguration().cacheEnabledForWallet) {
			try {
				Wallet wallet = new Wallet(sessionInfoParts[10], 
						                          params.getServiceInfo().getWalletConf().isPresent() ?
						                        		  params.getServiceInfo().getWalletConf().get().walletId : "-1",
						                          params.getRealBNO(),
						                          Long.valueOf(sessionInfoParts[11]),
						                          Wallet.DEFAULT_PRODUCT_NAME,
						                          Utility.getEpochTime(sessionInfoParts[12]),
						                          sessionInfoParts[13].equals("1"),
						                          Utility.getEpochTime(sessionInfoParts[14]),
						                          params.getRequest().getproductcode()
						                          );
				
				if (!wallet.hasInvalidId()) {
					si.setWallet(wallet);
				} else {
					si.setWallet(Wallet.getDummyWallet());
				}
			} catch (NumberFormatException e) {}
		} else {
			si.setWallet(Wallet.getDummyWallet());
		}
		
		return Optional.of(si);
	}
	
	public static void updateAndSetSessionInfo(ProcedureParams params) {
		if (params.errorOccured() || params.getMappedChargedType().equalsIgnoreCase("SESSION_STOP")) { // No need to maintain session information.
			DBClient.deleteSessionInfo(params.getRequest().getCDRId());
			params.setFlagIfCdrDataToBeStored();
			return;
		}

		if(!params.getSessionInfo().isPresent()) return;
		
		SessionInfo sessionInfo = params.getSessionInfo().get();
		RatingInfo ratingInfo = params.getRatingInfo();
		
		long truncConsumedUnits = params.getRequest().getConsumedUnitSinceLastRequest();
		
		if (params.getServiceInfo().getWalletConf().isPresent()) { // If wallet option is present.
			String walletName = params.getServiceInfo().getWalletConf().get().walletName;
			if (walletName.equalsIgnoreCase("talktime") 
					&& (truncConsumedUnits % ApplicationProperties.getRatePlanConf().minPulseSize) < 10) {
				truncConsumedUnits = truncConsumedUnits - (truncConsumedUnits % ApplicationProperties.getRatePlanConf().minPulseSize);
			}
		}
		
		long totalConsumedUnits = sessionInfo.getTotalConsumedUnit() + truncConsumedUnits;
		long totalChargedAmount = sessionInfo.getTotalChargedAmount() + ratingInfo.getAmount();
		
		int nextStepNo = sessionInfo.getNextStepNo();
		long pulseLimitOfCurrentStepNo = sessionInfo.getPulseLimitOfCurrentStepNo();
		if (totalConsumedUnits == 0) {
			pulseLimitOfCurrentStepNo = pulseLimitOfCurrentStepNo + ratingInfo.getStepDuration();
		} else if (totalConsumedUnits >= pulseLimitOfCurrentStepNo
				&& (sessionInfo.getNextStepNo() + 1) < ratingInfo.getTotalSteps()) {
			nextStepNo = nextStepNo + 1;
			pulseLimitOfCurrentStepNo = pulseLimitOfCurrentStepNo + ratingInfo.getStepDuration();
		}
		
		sessionInfo.setRateId(params.getRateId())
				   .setTotalConsumedUnit(totalConsumedUnits)
				   .setLastReservedUnit(ratingInfo.getPulseSize())
				   .setTotalChargedAmount(totalChargedAmount)
				   .setLastChargedAmount(ratingInfo.getAmount())
				   .setSubscriptionStatus(params.getSubscriberService().getNumericSubscriptionStatus())
				   .setNextStepNo(nextStepNo)
				   .setPulseLimitOfCurrentStepNo(pulseLimitOfCurrentStepNo);
		
		params.setSessionInfo(Optional.of(sessionInfo));
		params.addLoggableMessage(DebugPoint.SESSION_INFO, params.getSessionInfo());
		
		DBClient.updateSessionInformation(params);
	}
}
