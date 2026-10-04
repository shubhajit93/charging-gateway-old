package com.ssdtech.main.java.controller;

import com.ssdtech.main.java.model.RequestCounter;

public class RequestServedCounter extends Thread {

	@Override
	public void run() {
		while(true) {
			try {
				Thread.sleep(1000);
			} catch(InterruptedException exception) {
				
			}
			long requestCounter = 0;
			synchronized(this) {
				requestCounter = RequestCounter.getCounter();
			}
			if(requestCounter != 0) {
				System.out.println("Request served per seconds: " + requestCounter);
			}
		}
	}
}
