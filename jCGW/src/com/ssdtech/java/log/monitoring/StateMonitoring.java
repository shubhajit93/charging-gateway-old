package com.ssdtech.java.log.monitoring;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.ssdtech.java.model.applicationproperties.ApplicationProperties;

public class StateMonitoring {
    private BlockingQueue<StateContext> stateContexts;
    private ExecutorService concurrentSenders;
    private boolean shutdown;
    
    private static StateMonitoring instance;
	
	public StateMonitoring() {
		this.stateContexts = new ArrayBlockingQueue<>(ApplicationProperties.getLogServerConfiguration().maxContexts);
		this.concurrentSenders = Executors.newFixedThreadPool(ApplicationProperties.getLogServerConfiguration().concurrentSenders);
		this.shutdown = false;
	}
	
	public void addStateContext(StateContext stateContext) {
		if (stateContext.isInValidStateContext()) return;
		
		instance.stateContexts.offer(stateContext);
	}
	
	public void startMonitoring() {
		for (int i = 0; i < ApplicationProperties.getLogServerConfiguration().concurrentSenders; i++) {
    	    instance.concurrentSenders.execute(() -> {
    	    	boolean ioErrorOccured = false;
    	    	while (!shutdown) {
    	    		if (ioErrorOccured) try { Thread.sleep(ApplicationProperties.getLogServerConfiguration().reconnectInterval); } catch (InterruptedException e) {}
	    	        try (Socket socket = new Socket(ApplicationProperties.getLogServerConfiguration().ip, 
	    	        		                        ApplicationProperties.getLogServerConfiguration().port);
	    	        	 OutputStream out = socket.getOutputStream()) {
	    	        	this.sendStateContext(out);
	    	        } catch (IOException e) { 
	    	        	ioErrorOccured = true;
	    	        }
    	    	}
    	    });
		}
	}
	
	public void shutdownMonitoring() {
		shutdown = true;
		Thread.currentThread().interrupt();
		concurrentSenders.shutdownNow();
	}
	
	private void sendStateContext(OutputStream out) throws IOException {
		while(!shutdown) {
    		try {
    			StateContext context = instance.stateContexts.take();
    			out.write((context.serialize() + ApplicationProperties.getLogServerConfiguration().contextSeparator).getBytes());
		    } catch (InterruptedException e) {}
    	}
	}
	
	public static StateMonitoring getInstance() {
		if (instance == null) {
			instance = new StateMonitoring();
		}
		
		return instance;
	}
}
