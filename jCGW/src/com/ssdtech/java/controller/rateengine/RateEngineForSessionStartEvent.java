package com.ssdtech.java.controller.rateengine;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.ssdtech.java.db.client.DBClient;
import com.ssdtech.java.model.DebugPoint;
import com.ssdtech.java.model.ProcedureParams;
import com.ssdtech.java.model.RateMaster;
import com.ssdtech.java.model.RatePulse;
import com.ssdtech.java.model.RatingInfo;
import com.ssdtech.java.model.ResultCode;
import com.ssdtech.java.model.SubscriberService;
import com.ssdtech.java.model.Wallet;
import com.ssdtech.java.model.WalletContainer;
import com.ssdtech.java.model.ServiceInfo.WalletConf;
import com.ssdtech.java.model.SessionInfo;
import com.ssdtech.java.model.applicationproperties.ApplicationProperties;
import com.ssdtech.java.utils.Tuple;

public enum RateEngineForSessionStartEvent implements Event {	
    WITHOUT_SUBSCRIPTION {
		@Override
		public void process(ProcedureParams params) {
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
			
			SessionInfo si = SessionInfo.getDummySession();
	        si.setRateId(params.getRateId());
	        params.setSessionInfo(Optional.of(si));
			
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
			
			SessionInfo si = SessionInfo.getDummySession();
	        si.setRateId(params.getRateId());
	        params.setSessionInfo(Optional.of(si));
			
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
			if (!params.getServiceInfo().getWalletConf().isPresent()) {
				params.setFlagIfErrorOccured(ResultCode.Error_No_Wallet_Conf_Found);
				return;
			}

            List<RateMaster> rateContexts = DBClient.getRateContexts(params);
			
			if (rateContexts.isEmpty()) {
				params.setFlagIfErrorOccured(ResultCode.Error_DB_BlankRateId);
				return;
			}

			//System.out.println("Inside session start event, rateContext:"+rateContexts);


			
            boolean[] searchOn = RateEngineUtility.getSearchOn(rateContexts, true, params.getRequest().isSubscriptionGroupIdApplicable());
	        Tuple<SubscriberService, Set<String>> subscriberInfo = RateEngineUtility.getSubscriptionServicesInformation(params, searchOn);
			
			WalletConf conf = params.getServiceInfo().getWalletConf().get();

        	Tuple<Integer, Optional<Wallet>> walletInfo = RateEngineUtility.getWalletInformation(conf, params.getMSISDN(), params.getRealBNO(), params.getRequest().getServiceId(), params.getRequest().getproductcode());
        	
        	long remainingBalance = walletInfo.second.map(Wallet::getBalance).orElse(0L);
        	boolean isInFreeServicePeriod = walletInfo.first == 0 && walletInfo.second.isPresent() && walletInfo.second.get().isInFreeServicePeriod();
        	
        	subscriberInfo.first.updateMapping(true, isInFreeServicePeriod, remainingBalance);
        	
        	params.setSubscriberService(subscriberInfo.first);
	        params.setRegisteredSubscriptionGroupIds(subscriberInfo.second);
	        //set product id
	        params.addLoggableMessage(DebugPoint.SUBSCRIPTION_INFO, subscriberInfo.first);
	        

			/*
			List<RateMaster> rateContextsPriorityList = RateEngineUtility.getRateContextPriorityList(rateContexts, subscriberInfo.first, subscriberInfo.second, searchOn);

			System.out.println("Priority rateContext, session start: "+rateContextsPriorityList);

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

			*/

			RateMaster rm = RateEngineUtility.getRateContext(rateContexts, subscriberInfo.first, subscriberInfo.second, searchOn);

			if (rm == null) {
				params.setFlagIfErrorOccured(ResultCode.Error_DB_BlankRateId);
				return;
			}

			
			params.setRateId(rm.hasCampaignRate ? DBClient.getCampaignRateId(params.getMSISDN(), rm.rateId) : rm.rateId);
			params.setBillingNodeId(rm.billingNodeId);


			SessionInfo si = SessionInfo.getDummySession();
	        si.setRateId(params.getRateId());
	        params.setSessionInfo(Optional.of(si));
			
			Optional<RatingInfo> ratingInfo = this.getRatingInfo(params);
	        if (!ratingInfo.isPresent()) {
	        	params.setFlagIfErrorOccured(ResultCode.Error_DB_GetRateAndBillingNode_BlankAmount);
				System.out.println("rating info not found");
	        	return;
	        } else {
	        	params.setRatingInfo(ratingInfo.get());
	        }

	        //
	        if (ApplicationProperties.walletDeductibleStatus.contains(params.getSubscriberService().getNumericSubscriptionStatus()) 
					&& remainingBalance > 0
					&& !isInFreeServicePeriod
					&& params.getRatingInfo().getPulseSize() > 0) {
	        	WalletContainer wc = RateEngineUtility.getUpdatedWalletInfo(walletInfo.second.get(), walletInfo.first, ratingInfo.get().getPulseSize(), conf, params.getMSISDN(), params.getRealBNO(), params.getRequest().getproductcode());
	        	ratingInfo.get().setPulseSize(wc.getUpdatedPulseSize());
	        	si.setWallet(wc.getUpdatedWallet());
	        	si.setCurrentWalletType(wc.getWalletType());
	        	boolean success = DBClient.updateWalletInformation(params.getMSISDN(), params.getRequest().getServiceId(), wc.getUpdatedWallet(), wc.getUpdatedWalletIds(), wc.getCroppedBalances(), params.getRequest().getproductcode());
	        	if (!success) params.setFlagIfErrorOccured(ResultCode.ERROR_DB_Query_Failed);
	        }
		}
	};
		
	public Optional<RatingInfo> getRatingInfo(ProcedureParams params) {
		int minPulseSize = ApplicationProperties.getRatePlanConf().minPulseSize;
		
		Tuple<Integer, RatePulse> rateInfo = DBClient.getRatingInfo(params.getRateId(), 0);
		if (rateInfo == null) return Optional.empty();

		RatePulse ratePulse = rateInfo.second;
		long pulseSize = Math.max(minPulseSize, ratePulse.stepPulse);
		long amount = (long) (ratePulse.stepPulseRate * Math.floor(pulseSize / ratePulse.stepPulse));
					
		return Optional.of(new RatingInfo(rateInfo.first, ratePulse.stepDuration, ratePulse.stepPulse, ratePulse.stepPulseRate, amount, pulseSize, 0L));
	}
}
