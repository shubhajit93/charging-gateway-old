package com.ssdtech.java.controller.rateengine;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.ssdtech.java.controller.SessionManagement;
import com.ssdtech.java.db.client.DBClient;
import com.ssdtech.java.model.DebugPoint;
import com.ssdtech.java.model.ProcedureParams;
import com.ssdtech.java.model.RateMaster;
import com.ssdtech.java.model.RatePulse;
import com.ssdtech.java.model.RatingInfo;
import com.ssdtech.java.model.ResultCode;
import com.ssdtech.java.model.SubscriberService;
import com.ssdtech.java.model.Wallet;
import com.ssdtech.java.model.ServiceInfo.WalletConf;
import com.ssdtech.java.model.SessionInfo;
import com.ssdtech.java.model.applicationproperties.ApplicationProperties;
import com.ssdtech.java.utils.Tuple;

public enum RateEngineForSessionStopEvent implements Event {	
    WITHOUT_SUBSCRIPTION {
		@Override
		public void process(ProcedureParams params) {
			Optional<SessionInfo> sessionInfo = SessionManagement.loadSessionInfo(params);
			if (!sessionInfo.isPresent()) {
				params.setFlagIfErrorOccured(ResultCode.Error_No_Session_Info_Found);
				return;
			}
			
			params.setSessionInfo(sessionInfo);
	        SessionInfo si = sessionInfo.get();
	        
			List<RateMaster> rateContexts = DBClient.getRateContexts(params);
			
			if (rateContexts.isEmpty()) {
				params.setFlagIfErrorOccured(ResultCode.Error_DB_BlankRateId);
				return;
			}
						
	    	boolean[] searchOn = RateEngineUtility.getSearchOn(rateContexts, false, params.getRequest().isSubscriptionGroupIdApplicable());
	    	
	        Tuple<SubscriberService, Set<String>> subscriberInfo = RateEngineUtility.getSubscriptionServicesInformation(params, searchOn);
	         
	        params.setSubscriberService(subscriberInfo.first);
	        params.setRegisteredSubscriptionGroupIds(subscriberInfo.second);
	        
	        params.addLoggableMessage(DebugPoint.SUBSCRIPTION_INFO, subscriberInfo.first);
	        
	        RateMaster rm = RateEngineUtility.getRateContext(rateContexts, subscriberInfo.first, subscriberInfo.second, searchOn);
					
			if (rm == null) {
				params.setFlagIfErrorOccured(ResultCode.Error_DB_BlankRateId);
				return;
			}
			
			params.setRateId(rm.hasCampaignRate ? DBClient.getCampaignRateId(params.getMSISDN(), rm.rateId) : rm.rateId);
			params.setBillingNodeId(rm.billingNodeId);
			
			si.setRateId(params.getRateId());
			
			Optional<RatingInfo> ratingInfo = this.getRatingInfo(params);
	        if (!ratingInfo.isPresent()) {
	        	params.setFlagIfErrorOccured(ResultCode.Error_DB_GetRateAndBillingNode_BlankAmount);
	        } else {
	        	params.setRatingInfo(ratingInfo.get());
	        }
		}
			
	}, WITH_SUBSCRIPTION {
		@Override
		public void process(ProcedureParams params) {
			Optional<SessionInfo> sessionInfo = SessionManagement.loadSessionInfo(params);
			if (!sessionInfo.isPresent()) {
				params.setFlagIfErrorOccured(ResultCode.Error_No_Session_Info_Found);
				return;
			}
			
			params.setSessionInfo(sessionInfo);
	        SessionInfo si = sessionInfo.get();
			
            List<RateMaster> rateContexts = DBClient.getRateContexts(params);
			
			if (rateContexts.isEmpty()) {
				params.setFlagIfErrorOccured(ResultCode.Error_DB_BlankRateId);
				return;
			}
						
	    	boolean[] searchOn = RateEngineUtility.getSearchOn(rateContexts, true, params.getRequest().isSubscriptionGroupIdApplicable());
	    	
	        Tuple<SubscriberService, Set<String>> subscriberInfo = RateEngineUtility.getSubscriptionServicesInformation(params, searchOn);
	         
	        params.setSubscriberService(subscriberInfo.first);
	        params.setRegisteredSubscriptionGroupIds(subscriberInfo.second);
	        
	        params.addLoggableMessage(DebugPoint.SUBSCRIPTION_INFO, subscriberInfo.first);
	        
	        RateMaster rm = RateEngineUtility.getRateContext(rateContexts, subscriberInfo.first, subscriberInfo.second, searchOn);
					
			if (rm == null) {
				params.setFlagIfErrorOccured(ResultCode.Error_DB_BlankRateId);
				return;
			}
			
			params.setRateId(rm.hasCampaignRate ? DBClient.getCampaignRateId(params.getMSISDN(), rm.rateId) : rm.rateId);
			params.setBillingNodeId(rm.billingNodeId);
			
			si.setRateId(params.getRateId());
			
			Optional<RatingInfo> ratingInfo = this.getRatingInfo(params);
	        if (!ratingInfo.isPresent()) {
	        	params.setFlagIfErrorOccured(ResultCode.Error_DB_GetRateAndBillingNode_BlankAmount);
	        } else {
	        	params.setRatingInfo(ratingInfo.get());
	        }
		}
	
	}, WITH_WALLET {
		@Override
		public void process(ProcedureParams params) {
			Optional<SessionInfo> sessionInfo = SessionManagement.loadSessionInfo(params);
			if (!sessionInfo.isPresent()) {
				params.setFlagIfErrorOccured(ResultCode.Error_No_Session_Info_Found);
				return;
			}
			
			params.setSessionInfo(sessionInfo);
	        SessionInfo si = sessionInfo.get();
			
			if (!params.getServiceInfo().getWalletConf().isPresent()) {
				params.setFlagIfErrorOccured(ResultCode.Error_No_Wallet_Conf_Found);
				return;
			}
			
            List<RateMaster> rateContexts = DBClient.getRateContexts(params);
			
			if (rateContexts.isEmpty()) {
				params.setFlagIfErrorOccured(ResultCode.Error_DB_BlankRateId);
				return;
			}
			
            boolean[] searchOn = RateEngineUtility.getSearchOn(rateContexts, true, params.getRequest().isSubscriptionGroupIdApplicable());
	        Tuple<SubscriberService, Set<String>> subscriberInfo = RateEngineUtility.getSubscriptionServicesInformation(params, searchOn);
			
			WalletConf conf = params.getServiceInfo().getWalletConf().get();
			
			long remainingBalance = si.getWallet().isWalletExpired() ? 0L : si.getWallet().getBalance();
			boolean isInFreeServicePeriod = si.getCurrentWalletType() == 0 && si.getWallet().isInFreeServicePeriod();
			if (remainingBalance == 0) {
	        	Tuple<Integer, Optional<Wallet>> walletInfo = RateEngineUtility.getWalletInformation(conf, params.getMSISDN(), params.getRealBNO(), params.getRequest().getServiceId(), params.getRequest().getproductcode());
	        	
	        	remainingBalance = walletInfo.second.map(Wallet::getBalance).orElse(0L);
	        	isInFreeServicePeriod = walletInfo.first == 0 && walletInfo.second.isPresent() && walletInfo.second.get().isInFreeServicePeriod();
	        	
	        	si.setWallet(walletInfo.second.orElseGet(Wallet::getDummyWallet));
			}
        	subscriberInfo.first.updateMapping(true, isInFreeServicePeriod, remainingBalance);
        	
        	params.setSubscriberService(subscriberInfo.first);
	        params.setRegisteredSubscriptionGroupIds(subscriberInfo.second);
	        
	        params.addLoggableMessage(DebugPoint.SUBSCRIPTION_INFO, subscriberInfo.first);

			//added new
			/*
			List<RateMaster> rateContextsPriorityList = RateEngineUtility.getRateContextPriorityList(rateContexts, subscriberInfo.first, subscriberInfo.second, searchOn);

			System.out.println("Priority rateContext, session stop: "+rateContextsPriorityList);

			if(rateContextsPriorityList.isEmpty()) {
				params.setFlagIfErrorOccured(ResultCode.Error_DB_BlankRateId);
				return;
			}

			params.setRateId(rateContextsPriorityList.get(0).hasCampaignRate ? DBClient.getCampaignRateId(params.getMSISDN(), rateContextsPriorityList.get(0).rateId):rateContextsPriorityList.get(0).rateId);

			for(int i = 0; i < rateContextsPriorityList.size(); i++)
			{
				params.setBillingNodeIdList(rateContextsPriorityList.get(i).billingNodeId);
			}
			rateContextsPriorityList.clear();
			System.out.println("RateID:"+params.getRateId());
*/

			//end added new

	        RateMaster rm = RateEngineUtility.getRateContext(rateContexts, subscriberInfo.first, subscriberInfo.second, searchOn);
					
			if (rm == null) {
				params.setFlagIfErrorOccured(ResultCode.Error_DB_BlankRateId);
				return;
			}
			
			params.setRateId(rm.hasCampaignRate ? DBClient.getCampaignRateId(params.getMSISDN(), rm.rateId) : rm.rateId);
			params.setBillingNodeId(rm.billingNodeId);




			Optional<RatingInfo> ratingInfo = this.getRatingInfo(params);
	        if (!ratingInfo.isPresent()) {
	        	params.setFlagIfErrorOccured(ResultCode.Error_DB_GetRateAndBillingNode_BlankAmount);
	        } else {
	        	params.setRatingInfo(ratingInfo.get());
	        }
	        
	        long addBalance = si.getLastReservedUnit() - params.getRequest().getConsumedUnitSinceLastRequest();
	        if (!isInFreeServicePeriod && si.getRateId().regionMatches(true, 0, "zero", 0, 4) && addBalance > 0) {
				String walletName = params.getServiceInfo().getWalletConf().get().walletName;
				if (walletName.equalsIgnoreCase("talktime")) {
					addBalance = addBalance - (addBalance % ApplicationProperties.getRatePlanConf().minPulseSize);
				}
				
				Wallet oldWallet = si.getWallet();
				
				List<String> walletIds = new ArrayList<>();
				List<Long> croppedBalances = new ArrayList<>();
				walletIds.add(oldWallet.getId());
				croppedBalances.add(-1 * addBalance);
				
				Wallet updatedWallet = new Wallet(oldWallet.getId(), oldWallet.getWalletId(), oldWallet.getBNO(), 
						                          oldWallet.getBalance() + addBalance, oldWallet.getProductName(), 
						                          oldWallet.getWalletExpirationDate(), oldWallet.isFreeWallet(), 
						                          oldWallet.getFreeWalletEndDate(), oldWallet.getProductcode());
				
				boolean success = DBClient.updateWalletInformation(params.getMSISDN(), params.getRequest().getServiceId(), updatedWallet, walletIds, croppedBalances, params.getRequest().getproductcode());
				if (!success) params.setFlagIfErrorOccured(ResultCode.ERROR_DB_Query_Failed);
	        }
		}
	};
	
	public Optional<RatingInfo> getRatingInfo(ProcedureParams params) {
		int minPulseSize = ApplicationProperties.getRatePlanConf().minPulseSize;
		if(!params.getSessionInfo().isPresent()) return Optional.empty();
		SessionInfo si = params.getSessionInfo().get();
		
		Tuple<Integer, RatePulse> rateInfo = DBClient.getRatingInfo(params.getRateId(), si.getNextStepNo());
		if (rateInfo == null) return Optional.empty();

		RatePulse ratePulse = rateInfo.second;
		long pulseSize = Math.max(minPulseSize, ratePulse.stepPulse);
		long amount = (long) (ratePulse.stepPulseRate * Math.floor(si.getLastReservedUnit() / ratePulse.stepPulse));
		
		long refundAmount = si.getLastChargedAmount() - amount;
		
		return Optional.of(new RatingInfo(rateInfo.first, ratePulse.stepDuration, ratePulse.stepPulse, ratePulse.stepPulseRate, amount, pulseSize, refundAmount));
	}
}
