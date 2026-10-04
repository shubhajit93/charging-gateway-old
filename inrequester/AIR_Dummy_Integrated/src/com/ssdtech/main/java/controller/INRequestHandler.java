package com.ssdtech.main.java.controller;

import java.util.Random;
import java.util.StringTokenizer;

import com.ssdtech.cgwcommon.INRequester;
import com.ssdtech.main.java.model.AppConstant;
import com.ssdtech.main.java.model.RequestCounter;

public class INRequestHandler implements INRequester {
    private Random rand;
    
    public INRequestHandler() {
        this.rand = new Random();
    }
    
	@Override
	public String chargeSubscriber(String message) {
		return handleClientMessage(message);
	}

	@Override
	public String queryBalance(String message) {
		return handleClientMessage(message);
	}
    
    private String handleClientMessage(String message) {
    	String response = "";
		if(message != null && !(message.trim().length() == 0)) {
			StringTokenizer tokens = new StringTokenizer(message, "|");
			int parametersCount = tokens.countTokens();
			String firstToken = tokens.nextToken();
			
			if(parametersCount == 2 && firstToken.toUpperCase().equals("PACKAGE")) {
				response = getAIRBalanceResponse(RequestCounter.isSuccess());
			} else if(parametersCount > 1) {
				response = getOperatorINResponse(firstToken, tokens);
			}
		}
		
		return response;
    }
    
    private String getAIRBalanceResponse(boolean isSuccess) {
    	String response="Prepaid|0|3002";
    		
    	if(isSuccess) {
    		response = "Prepaid|" + AppConstant.returnedAmountOnSuccess + "|3002";	
    	} 
    	
    	try {
    		long sleepTime = this.rand.nextInt(AppConstant.airDelayLimitDiff)
    				             + AppConstant.airDelayLowerLimit;
    		Thread.sleep(sleepTime);
    	} catch(InterruptedException exception) {}
    	
    	return response;
    }
    
    private String getOperatorINResponse(String sessionId, StringTokenizer tokens) {
		int accountingRecordNumber = Integer.parseInt(tokens.nextToken());
		
		int accountingRecordType = 1;
		String requestType = tokens.nextToken();
		if(requestType.equals("SESSION_START")) accountingRecordType = 2; 
		else if(requestType.equals("SESSION_INTERIM")) accountingRecordType = 3;
		else if(requestType.equals("SESSION_STOP")) accountingRecordType = 4;
		else if(requestType.equals("SPECIFIC_CHARGE")) accountingRecordType = 1; 
	
		try {
			long sleepTime = this.rand.nextInt(AppConstant.operatorINDelayLimitDiff) 
					             + AppConstant.operatorINDelayLowerLimit;
			Thread.sleep(sleepTime);
		} catch(InterruptedException exception) {
			
		}
		
	    return sessionId + " 2001 " + accountingRecordType + " " + accountingRecordNumber + " 180";
    }

	@Override
	public String executeCommand(String command) {
		return "Dummy BI contains no loadable configuration.";
	}
}
