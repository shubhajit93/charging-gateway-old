package com.ssdtech.main.java.controller;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.concurrent.BlockingQueue;

import com.ssdtech.main.java.model.AppConstant;
import com.ssdtech.main.java.model.RequestCounter;

public class RequestHandler extends Thread{
    private BlockingQueue<Socket> tasks;
    private Random rand;
    
    public RequestHandler(BlockingQueue<Socket> tasks) {
        this.tasks = tasks;
        this.rand = new Random();
    }
    
    @Override
    public void run() {
    	while(true) {
    		try {
				Socket clientSocket = this.tasks.take();
				handleClientMessage(clientSocket);
			} catch (InterruptedException exception) {
				exception.printStackTrace();
			}
    	}
    }
    
    private void handleClientMessage(Socket clientSocket) {
    	try {
			BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			PrintStream out = new PrintStream(clientSocket.getOutputStream());
			
			String response = "";
			try {
				String message = in.readLine();
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
			}
			catch(IOException exception) {   
			}
			// System.out.println("Response: " + response);
			
			out.println(response);
			out.flush();
			
			in.close();
			out.close();
			
			clientSocket.close();
			RequestCounter.incrementCounter();
		} catch(IOException exception) {
			
		}
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
}
