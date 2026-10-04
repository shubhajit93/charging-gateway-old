package com.ssdtech.java.controller.rateengine;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.ssdtech.java.db.client.DBClient;
import com.ssdtech.java.db.management.EmbededDB;
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
import com.ssdtech.java.model.applicationproperties.ApplicationProperties;
import com.ssdtech.java.utils.Tuple;

public enum RateEngineForSpecificEvent implements Event {	
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
	        
	        //RateMaster rm = RateEngineUtility.getRateContext(rateContexts, subscriberInfo.first, subscriberInfo.second, searchOn);
			List<RateMaster> rateContextsPriorityList = RateEngineUtility.getRateContextPriorityList(rateContexts, subscriberInfo.first, subscriberInfo.second, searchOn);


			if(rateContextsPriorityList.isEmpty()) {
				params.setFlagIfErrorOccured(ResultCode.Error_DB_BlankRateId);
				return;
			}

			params.setRateId(rateContextsPriorityList.get(0).hasCampaignRate ? DBClient.getCampaignRateId(params.getMSISDN(), rateContextsPriorityList.get(0).rateId):rateContextsPriorityList.get(0).rateId);
			//for multiple priority set multiple billing node

			for(int i = 0; i < rateContextsPriorityList.size(); i++)
			{
				params.setBillingNodeIdList(rateContextsPriorityList.get(i).billingNodeId);
			}

			Optional<RatingInfo> ratingInfo = this.getRatingInfo(params);
			if (!ratingInfo.isPresent()) {
				params.setFlagIfErrorOccured(ResultCode.Error_DB_GetRateAndBillingNode_BlankAmount);
				return;
			}
			else if(ratingInfo.get().getPulseSize() == -1)
			{
				params.setFlagIfErrorOccured(ResultCode.Error_Invalid_Specific_Charging_Amount_Range);
				return;
			}
			else {
				params.setRatingInfo(ratingInfo.get());
			}

			
//			Optional<RatingInfo> ratingInfo = this.getRatingInfo(params);
//	        if (!ratingInfo.isPresent()) {
//	        	params.setFlagIfErrorOccured(ResultCode.Error_DB_GetRateAndBillingNode_BlankAmount);
//	        } else {
//	        	params.setRatingInfo(ratingInfo.get());
//	        }
		}
				
	}, WITH_SUBSCRIPTION {
		@Override
		public void process(ProcedureParams params) {
            List<RateMaster> rateContexts = DBClient.getRateContexts(params);
			//System.out.println("inside with subscription, list:"+rateContexts);
			if (rateContexts.isEmpty()) {
				params.setFlagIfErrorOccured(ResultCode.Error_DB_BlankRateId);
				return;
			}
						
	    	boolean[] searchOn = RateEngineUtility.getSearchOn(rateContexts, true, params.getRequest().isSubscriptionGroupIdApplicable());
	    	
	        Tuple<SubscriberService, Set<String>> subscriberInfo = RateEngineUtility.getSubscriptionServicesInformation(params, searchOn);
	         
	        params.setSubscriberService(subscriberInfo.first);
	        params.setRegisteredSubscriptionGroupIds(subscriberInfo.second);
	        
	        //params.addLoggableMessage(DebugPoint.SUBSCRIPTION_INFO, subscriberInfo.first);
	        
	        RateMaster rm = RateEngineUtility.getRateContext(rateContexts, subscriberInfo.first, subscriberInfo.second, searchOn);
			List<RateMaster> rateContextsPriorityList = RateEngineUtility.getRateContextPriorityList(rateContexts, subscriberInfo.first, subscriberInfo.second, searchOn);


			if(rateContextsPriorityList.isEmpty()) {
				params.setFlagIfErrorOccured(ResultCode.Error_DB_BlankRateId);
				return;
			}
			
			//params.setRateId(rm.hasCampaignRate ? DBClient.getCampaignRateId(params.getMSISDN(), rm.rateId) : rm.rateId);
			//params.setBillingNodeId(rm.billingNodeId);
			params.setRateId(rateContextsPriorityList.get(0).hasCampaignRate ? DBClient.getCampaignRateId(params.getMSISDN(), rateContextsPriorityList.get(0).rateId):rateContextsPriorityList.get(0).rateId);

			for(int i = 0; i < rateContextsPriorityList.size(); i++)
			{
				params.setBillingNodeIdList(rateContextsPriorityList.get(i).billingNodeId);
			}

			Optional<RatingInfo> ratingInfo = this.getRatingInfo(params);
	        if (!ratingInfo.isPresent()) {
	        	params.setFlagIfErrorOccured(ResultCode.Error_DB_GetRateAndBillingNode_BlankAmount);
	        }
			else if(ratingInfo.get().getPulseSize() == -1)
			{
				params.setFlagIfErrorOccured(ResultCode.Error_Invalid_Specific_Charging_Amount_Range);
				return;
			}
	        else {
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

			//System.out.println("initial Ratecontext: "+rateContexts);
			//System.out.println();
			
			if (rateContexts.isEmpty()) {
				params.setFlagIfErrorOccured(ResultCode.Error_DB_BlankRateId);
				return;
			}
            boolean[] searchOn = RateEngineUtility.getSearchOn(rateContexts, true, params.getRequest().isSubscriptionGroupIdApplicable());

	        Tuple<SubscriberService, Set<String>> subscriberInfo = RateEngineUtility.getSubscriptionServicesInformation(params, searchOn);

	        long remainingBalance = 0;
	        boolean isInFreeServicePeriod = false;
	        WalletConf conf = params.getServiceInfo().getWalletConf().get();
	        Tuple<Integer, Optional<Wallet>> walletInfo = new Tuple<>(-1, Optional.of(Wallet.getDummyWallet()));
			//System.out.println("value of search:"+searchOn);
			//System.out.println("value of search1:"+searchOn[1]);
	        if (searchOn[1]) { // If we do not have to search on SubscriptionStatus, then remaining balance will not change it.

	        	walletInfo = RateEngineUtility.getWalletInformation(conf, params.getMSISDN(), params.getRealBNO(), params.getRequest().getServiceId(), params.getRequest().getproductcode());
	        	
	        	remainingBalance = !walletInfo.second.isPresent() ? 0L : walletInfo.second.get().getBalance();
	        	isInFreeServicePeriod = walletInfo.first == 0 && walletInfo.second.isPresent() && walletInfo.second.get().isInFreeServicePeriod();
	        }
        	
        	subscriberInfo.first.updateMapping(true, isInFreeServicePeriod, remainingBalance);
        	
        	params.setSubscriberService(subscriberInfo.first);
	        params.setRegisteredSubscriptionGroupIds(subscriberInfo.second);
	        
	        params.addLoggableMessage(DebugPoint.SUBSCRIPTION_INFO, subscriberInfo.first);

	        List<RateMaster> rateContextsPriorityList = RateEngineUtility.getRateContextPriorityList(rateContexts, subscriberInfo.first, subscriberInfo.second, searchOn);

			//System.out.println("priority order context: "+rateContextsPriorityList);
			//System.out.println();


			if(rateContextsPriorityList.isEmpty()) {
	        	params.setFlagIfErrorOccured(ResultCode.Error_DB_BlankRateId);
				return;
			}

	        /*
	        RateMaster rm = RateEngineUtility.getRateContext(rateContexts, subscriberInfo.first, subscriberInfo.second, searchOn);

			if (rm == null) {
				params.setFlagIfErrorOccured(ResultCode.Error_DB_BlankRateId);
				return;
			}

			*/

	        params.setRateId(rateContextsPriorityList.get(0).hasCampaignRate ? DBClient.getCampaignRateId(params.getMSISDN(), rateContextsPriorityList.get(0).rateId):rateContextsPriorityList.get(0).rateId);
			//params.setRateId(rm.hasCampaignRate ? DBClient.getCampaignRateId(params.getMSISDN(), rm.rateId) : rm.rateId);

			//for multiple priority set multiple billing node

			//System.out.println("RateID:"+params.getRateId());

			for(int i = 0; i < rateContextsPriorityList.size(); i++)
			{
				params.setBillingNodeIdList(rateContextsPriorityList.get(i).billingNodeId);
			}
			//params.setBillingNodeId(rm.billingNodeId);
			
			Optional<RatingInfo> ratingInfo = this.getRatingInfo(params);
	        if (!ratingInfo.isPresent()) {
	        	params.setFlagIfErrorOccured(ResultCode.Error_DB_GetRateAndBillingNode_BlankAmount);
	        	return;
	        }
	        else if(ratingInfo.get().getPulseSize() == -1)
			{
				params.setFlagIfErrorOccured(ResultCode.Error_Invalid_Specific_Charging_Amount_Range);
				return;
			}
	        else {
	        	params.setRatingInfo(ratingInfo.get());
	        }
			//System.out.println("initial wallet..."+remainingBalance+"|"+isInFreeServicePeriod+"|"+params.getRatingInfo().getPulseSize());
	        if (ApplicationProperties.walletDeductibleStatus.contains(params.getSubscriberService().getNumericSubscriptionStatus())
					&& remainingBalance > 0  
					&& !isInFreeServicePeriod
					&& params.getRatingInfo().getPulseSize() > 0) {

	        	WalletContainer wc = RateEngineUtility.getUpdatedWalletInfo(walletInfo.second.get(), walletInfo.first, ratingInfo.get().getPulseSize(), conf, params.getMSISDN(), params.getRealBNO(), params.getRequest().getproductcode());
	        	ratingInfo.get().setPulseSize(wc.getUpdatedPulseSize());
	        	boolean success = DBClient.updateWalletInformation(params.getMSISDN(), params.getRequest().getServiceId(), wc.getUpdatedWallet(), wc.getUpdatedWalletIds(), wc.getCroppedBalances(),params.getRequest().getproductcode());
	        	if (!success) params.setFlagIfErrorOccured(ResultCode.ERROR_DB_Query_Failed);
	        }
		}
	};
	
	public Optional<RatingInfo> getRatingInfo(ProcedureParams params) {
		int minPulseSize = ApplicationProperties.getRatePlanConf().minPulseSize;
		int subCycle;
		long amount;


		subCycle = params.getRequest().getSubscriptionCycle();
		if(subCycle < 1)
		{
			String subscriptionCycle = DBClient.getSubsCycleInfo(params.getRequest().getSubscriptionGroupId());
			if(subscriptionCycle != null)
			{
				subCycle = Integer.valueOf(subscriptionCycle);
			}
			else
				subCycle = 1;
		}


		Tuple<Integer, RatePulse> rateInfo = DBClient.getRatingInfo(params.getRateId(), 0);
		if (rateInfo == null) return Optional.empty();

		RatePulse ratePulse = rateInfo.second;


		long pulseSize = Math.max(minPulseSize, ratePulse.stepPulse); //30
		amount = (long) (ratePulse.stepPulseRate * Math.floor(pulseSize / ratePulse.stepPulse) * subCycle);

		if(params.getRequest().getfixedAmount() != -1)
		{
			long tmppulseSize = params.getRequest().getfixedAmount();

			if(ratePulse.minAmount >= 0 && ratePulse.maxAmount >= 0 && tmppulseSize >= ratePulse.minAmount && tmppulseSize <= ratePulse.maxAmount)
			{
				pulseSize = tmppulseSize;
				amount = pulseSize;
			}
			else
			{
				pulseSize = -1;
			}

		}

		return Optional.of(new RatingInfo(rateInfo.first, ratePulse.stepDuration, ratePulse.stepPulse, ratePulse.stepPulseRate, amount, pulseSize, 0L));
	}
}
