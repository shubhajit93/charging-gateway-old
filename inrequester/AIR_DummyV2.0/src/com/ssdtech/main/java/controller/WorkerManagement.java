package com.ssdtech.main.java.controller;

import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.ssdtech.main.java.model.AppConstant;
import com.ssdtech.main.java.controller.RequestHandler;
import com.ssdtech.main.java.controller.RequestServedCounter;

public class WorkerManagement {
	private BlockingQueue<Socket> tasks;
	private ExecutorService executor;
	
	public WorkerManagement(BlockingQueue<Socket> tasks) {
		this.executor = Executors.newFixedThreadPool(AppConstant.noOfThreads + 1);
		this.tasks = tasks;
	}
	
    public void startWorkers() {
    	System.out.printf("Starting workers: ");
    	for(int workerIndex = 0; workerIndex < AppConstant.noOfThreads; workerIndex++) {
    		RequestHandler handler = new RequestHandler(this.tasks);
    		this.executor.execute(handler);
    		System.out.printf("%s.%s", AppConstant.ANSI_GREEN, AppConstant.ANSI_RESET);
    	}
    	
    	RequestServedCounter rsc = new RequestServedCounter();
    	this.executor.execute(rsc);
    	
    	System.out.println("Done");
    }
}
