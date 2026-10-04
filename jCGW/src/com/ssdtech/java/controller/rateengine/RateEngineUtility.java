package com.ssdtech.java.controller.rateengine;

import java.util.*;

import com.ssdtech.java.db.client.DBClient;
import com.ssdtech.java.model.ProcedureParams;
import com.ssdtech.java.model.RateMaster;
import com.ssdtech.java.model.SubscriberService;
import com.ssdtech.java.model.Wallet;
import com.ssdtech.java.model.WalletContainer;
import com.ssdtech.java.model.ServiceInfo.WalletConf;
import com.ssdtech.java.utils.Tuple;


public class RateEngineUtility {
    
	public static WalletContainer getUpdatedWalletInfo(Wallet wallet, int currentWalletType, long pulseSize, WalletConf conf, String msisdn, String bno, String productcode) {
		if (wallet.getBalance() >= pulseSize) {
			return new WalletContainer()
					.setUpdatedWallet(new Wallet(wallet.getId(), wallet.getWalletId(), wallet.getBNO(), wallet.getBalance() - pulseSize, wallet.getProductName(), wallet.getWalletExpirationDate(), wallet.isFreeWallet(), wallet.getFreeWalletEndDate(), productcode))
					.addWalletId(wallet.getId())
					.addCroppedBalance(pulseSize)
					.setUpdatedPulseSize(pulseSize)
					.setWalletType(currentWalletType);
		} else { 
			return new WalletContainer()
					.setUpdatedWallet(new Wallet(wallet.getId(), wallet.getWalletId(), wallet.getBNO(), 0, wallet.getProductName(), wallet.getWalletExpirationDate(), wallet.isFreeWallet(), wallet.getFreeWalletEndDate(), productcode))
					.addWalletId(wallet.getId())
					.addCroppedBalance(wallet.getBalance())
					.setUpdatedPulseSize(wallet.getBalance())
					.setWalletType(currentWalletType);
		}
	}
	
	public static Tuple<Integer, Optional<Wallet>> getWalletInformation(WalletConf conf, String msisdn, String bno, String serviceId, String productcode) {
    	Optional<Wallet> wallet = Optional.empty();
    	int walletType = -1;
    	
		if (conf.hasServiceWallet) {
			wallet = Optional.ofNullable(DBClient.getServiceWalletInformation(msisdn, bno, conf.walletId, serviceId, productcode));
			walletType = 0;
			
			if ((!wallet.isPresent() || wallet.get().getBalance() == 0) && conf.hasCommonWallet) {
			    wallet = Optional.ofNullable(DBClient.getCommonWalletInformation(msisdn, bno, conf.walletId, productcode));
			    walletType = 1;
			}
		} else if (conf.hasCommonWallet) {
			wallet = Optional.ofNullable(DBClient.getCommonWalletInformation(msisdn, bno, conf.walletId, productcode));
			walletType = 1;
		}
    	
    	return new Tuple<>(walletType, wallet);
    }
	
	public static boolean[] getSearchOn(List<RateMaster> rateContexts, boolean searchOnSpecificSubscriptionInfo, boolean subscriptionGroupIdApplicable) {
		boolean[] searchOn = new boolean[] {false, false, false, false};		
		
		for (RateMaster rm : rateContexts) {
	    	if (!subscriptionGroupIdApplicable && searchOnSpecificSubscriptionInfo && !searchOn[0] && !rm.subscriptionGroupId.equalsIgnoreCase("na")) {
	    		searchOn[0] = true;
	    	}
	    	
	    	if (searchOnSpecificSubscriptionInfo && !searchOn[1]  && !(rm.cardinality > 1) && !rm.subscriptionStatus.equalsIgnoreCase("0")) {
	    		searchOn[1] = true;	
	    	}
	    	
	        if (!searchOn[2] && !rm.registeredService.equalsIgnoreCase("na")) {
	        	searchOn[2] = true;
	        }
	        
	        if (!searchOn[3] && !rm.channel.equalsIgnoreCase("%") && !rm.channel.equalsIgnoreCase("na")) {
	        	searchOn[3] = true;
	        }
	    }
		
		return searchOn;
	}
	
	public static Tuple<SubscriberService, Set<String>> getSubscriptionServicesInformation(ProcedureParams params, boolean[] searchOn) {
    	Optional<SubscriberService> ss;
    	Set<String> registeredServices;
    	
    	if (searchOn[0] && searchOn[1] && searchOn[2] && searchOn[3]) {
			//System.out.println("test1");
    		ss = DBClient.getSubscriberInformation(params.getMSISDN(), params.getRequest().getServiceId(), params.getRequest().getSubscriptionGroupId(), params.getRequest().getChannel());
    		registeredServices = DBClient.getRegisteredServices(params.getMSISDN());
    	} else if(!searchOn[2] && (searchOn[0] || searchOn[1] || searchOn[3])) {
    		ss = DBClient.getSubscriberInformation(params.getMSISDN(), params.getRequest().getServiceId(), params.getRequest().getSubscriptionGroupId(), params.getRequest().getChannel());
    		registeredServices = new HashSet<>();
    	    registeredServices.add("na");
    	} else {
    	    ss = Optional.of(new SubscriberService(params.getRequest().getSubscriptionGroupId(), params.getRequest().getChannel()));
    	    registeredServices = new HashSet<>();
    	    registeredServices.add("na");
    	}
    	
    	if (!ss.isPresent()) {
    		ss = Optional.of(new SubscriberService(params.getRequest().getSubscriptionGroupId(), params.getRequest().getChannel()));
    	}
    	
    	return new Tuple<>(ss.get(), registeredServices);
    }

    public static List<RateMaster> getRateContextPriorityList(List<RateMaster> rateContexts, SubscriberService ss, Set<String> registeredServices, boolean[] searchOn) {

		List<RateMaster> rateContextList = new ArrayList<>();

		for(RateMaster context : rateContexts)
		{
			//System.out.println();
			if (searchOn[0] && !context.subscriptionGroupId.equalsIgnoreCase(ss.subscriptionGroupId)) continue;
			if (searchOn[1] && !context.subscriptionStatus.equalsIgnoreCase(ss.getNumericSubscriptionStatus())) continue;
			if (searchOn[2]) {
				boolean found = false;
				for (String registeredService : registeredServices) {
					if (registeredService.equalsIgnoreCase("na") || registeredService.equalsIgnoreCase(context.registeredService)) break;
				}
				if (!found) continue;
			}

			if (searchOn[3] && !context.channel.equalsIgnoreCase("na") && !context.channel.equalsIgnoreCase("%") && !context.channel.equalsIgnoreCase(ss.channel)) continue;

			rateContextList.add(context);

		}

		rateContextList.sort(new PrioritySorter());
		return  rateContextList;
	}


	//eleminate
	/*
    public static RateMaster getRateContext(List<RateMaster> rateContexts, SubscriberService ss, Set<String> registeredServices, boolean[] searchOn)
	{
    	RateMaster rateContext = null;
		int priority = Integer.MAX_VALUE;
		int cardinality = Integer.MAX_VALUE;
		//int i =1;

		List<RateMaster> rateContextList = new ArrayList<>();

		for (RateMaster context : rateContexts) {
			if (searchOn[0] && !context.subscriptionGroupId.equalsIgnoreCase(ss.subscriptionGroupId)) continue;
			
			if (searchOn[1] && !context.subscriptionStatus.equalsIgnoreCase(ss.getNumericSubscriptionStatus())) continue;
			
			if (searchOn[2]) {
				boolean found = false;
				for (String registeredService : registeredServices) {
					if (registeredService.equalsIgnoreCase("na") || registeredService.equalsIgnoreCase(context.registeredService)) break;
				}
				if (!found) continue;
			}
			
			if (searchOn[3] && !context.channel.equalsIgnoreCase("na") && !context.channel.equalsIgnoreCase("%") && !context.channel.equalsIgnoreCase(ss.channel)) continue;

			//System.out.println("inside loop:"+i+":and context priority:"+context.priority+":priority:"+priority+":context cardinality:"+context.cardinality+":cardinality"+cardinality);

			//add new
			rateContextList.add(context);
			//end new
			if (context.priority < priority) {
				rateContext = context;
				//System.out.println("different priority, rate_context:"+context);
				priority = context.priority;
				cardinality = context.cardinality;
			} else if (context.priority == priority && context.cardinality < cardinality) {
				rateContext = context;
				//System.out.println("same priority, rate_context:"+context);
				priority = context.priority;
				cardinality = context.cardinality;
			}
			//i++;
		}

		//rateContextList.sort(new PrioritySorter());

		//System.out.println("sorted context: "+rateContextList);
		
		return rateContext;
		//return  rateContextList;
    }
    */

	public static RateMaster getRateContext(List<RateMaster> rateContexts, SubscriberService ss, Set<String> registeredServices, boolean[] searchOn) {
		RateMaster rateContext = null;
		int priority = Integer.MAX_VALUE;
		int cardinality = Integer.MAX_VALUE;

		for (RateMaster context : rateContexts) {
			if (searchOn[0] && !context.subscriptionGroupId.equalsIgnoreCase(ss.subscriptionGroupId)) continue;

			if (searchOn[1] && !context.subscriptionStatus.equalsIgnoreCase(ss.getNumericSubscriptionStatus())) continue;

			if (searchOn[2]) {
				boolean found = false;
				for (String registeredService : registeredServices) {
					if (registeredService.equalsIgnoreCase("na") || registeredService.equalsIgnoreCase(context.registeredService)) break;
				}
				if (!found) continue;
			}

			if (searchOn[3] && !context.channel.equalsIgnoreCase("na") && !context.channel.equalsIgnoreCase("%") && !context.channel.equalsIgnoreCase(ss.channel)) continue;

			if (context.priority < priority) {
				rateContext = context;
				priority = context.priority;
				cardinality = context.cardinality;
			} else if (context.priority == priority && context.cardinality < cardinality) {
				rateContext = context;
				priority = context.priority;
				cardinality = context.cardinality;
			}
		}

		return rateContext;
	}

}
