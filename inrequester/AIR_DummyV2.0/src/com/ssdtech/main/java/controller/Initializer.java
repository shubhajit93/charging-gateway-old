package com.ssdtech.main.java.controller;

import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.PropertyConfigurator;

import java.net.Socket;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import com.ssdtech.main.java.model.AppConstant;
import com.ssdtech.main.java.controller.Listener;
import com.ssdtech.main.java.controller.WorkerManagement;

public class Initializer {
	
	public void loadConfiguration(String configurationFilePathName, String logConfigurationFilePathName) {
		System.out.println("Loading configuration files.");
		PropertyConfigurator.configure(logConfigurationFilePathName);
		Properties properties = new Properties();
		try {
			properties.load(new FileInputStream(configurationFilePathName));
		} catch(FileNotFoundException exception) {
			System.out.println(AppConstant.ANSI_RED 
								+ "Configuration file not found" 
					         + AppConstant.ANSI_RESET);
		} catch(IOException exception) {
			System.out.println(AppConstant.ANSI_RED 
					           + "I/O exception" 
		                     + AppConstant.ANSI_RESET);
		}
		
		AppConstant.port = Integer.parseInt(properties.getProperty("ListenPort"));
		AppConstant.noOfThreads = Integer.parseInt(properties.getProperty("NoOfThreads"));
		AppConstant.logEnable = Integer.parseInt(properties.getProperty("LogEnable"));
		AppConstant.returnedAmountOnSuccess = Integer.parseInt(properties.getProperty("ReturnedAmountOnSuccess"));
		
		String[] operatorINDelayLimits = properties.getProperty("OperatorINDelay").split(",");
		AppConstant.operatorINDelayLowerLimit = Integer.parseInt(operatorINDelayLimits[0].trim());
		AppConstant.operatorINDelayUpperLimit = Integer.parseInt(operatorINDelayLimits[1].trim());
		AppConstant.operatorINDelayLimitDiff = AppConstant.operatorINDelayUpperLimit 
				                                   - AppConstant.operatorINDelayLowerLimit;
		
		String[] airDelayLimits = properties.getProperty("AIRDelay").split(",");
		AppConstant.airDelayLowerLimit = Integer.parseInt(airDelayLimits[0].trim());
		AppConstant.airDelayUpperLimit = Integer.parseInt(airDelayLimits[1].trim());
		AppConstant.airDelayLimitDiff = AppConstant.airDelayUpperLimit - AppConstant.airDelayLowerLimit;
		
		String[] ratio = properties.getProperty("Ratio").split(":");
		AppConstant.successfulPercentage = Integer.parseInt(ratio[0].trim());
		AppConstant.unsuccessfulPercentage = Integer.parseInt(ratio[1].trim());		
		
		AppConstant.successDistribution = new Boolean[100];
		Arrays.fill(AppConstant.successDistribution, false);
		for(int i = 0; i < AppConstant.successfulPercentage; i++) {
			AppConstant.successDistribution[i] = true;
		}
		
		System.out.println(AppConstant.ANSI_GREEN + "Done." + AppConstant.ANSI_RESET);
	}
	
	public void createThreadPool(BlockingQueue<Socket> tasks) {
		WorkerManagement workerManagement = new WorkerManagement(tasks);
		workerManagement.startWorkers();
	}
	
	public void startListener(BlockingQueue<Socket> tasks) {
		Listener listener = Listener.getInstance(tasks);
		listener.startServer();
	}
	
	public static void main(String[] args)  {
		String configurationFilePathName = "./ConfigurationFiles/config.conf";
		String logConfigurationFilePathName = "./ConfigurationFiles/log4j.properties";
		
		Initializer initializer = new Initializer();
		initializer.loadConfiguration(configurationFilePathName, logConfigurationFilePathName);
		BlockingQueue<Socket> tasks = new LinkedBlockingQueue<Socket>();
		initializer.createThreadPool(tasks);
		initializer.startListener(tasks);
	}
}
