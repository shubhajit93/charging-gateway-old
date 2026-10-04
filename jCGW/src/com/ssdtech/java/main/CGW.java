package com.ssdtech.java.main;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;

import com.ssdtech.cgwcommon.INRequesterInitializer;
import com.ssdtech.java.controller.BillingInterfaceFactory;
import com.ssdtech.java.controller.PackageLoader;
import com.ssdtech.java.controller.Server;
import com.ssdtech.java.controller.maintenance.CommandServer;
import com.ssdtech.java.db.client.DBClient;
import com.ssdtech.java.db.management.DBConnectionFactory;
import com.ssdtech.java.log.monitoring.StateMonitoring;
import com.ssdtech.java.log.transaction.SimpleTransactionLogGenerator;
import com.ssdtech.java.model.applicationproperties.ApplicationProperties;
import com.ssdtech.java.utils.Authentication;
import com.ssdtech.java.utils.Authorization;
import com.ssdtech.java.utils.CallTypeChecker;
import com.ssdtech.java.utils.Tuple;

import redis.clients.jedis.Jedis;

public class CGW {
	public static final DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");	
	public static List<SimpleTransactionLogGenerator> logGenerators = new ArrayList<SimpleTransactionLogGenerator>();
	
	public static void main(String[] args) {
		try {
			ApplicationProperties.initialize();
			System.out.println(ApplicationProperties.getLogConfiguration().instance + "|" + 
			                   formatter.format(new Date()) + "|Configuration Initialized.");
			
			initializeLogger();
			ApplicationProperties.dumpConfigurationToLogFile();
			
			DBConnectionFactory.initializePersistentDataSource();
			try (Connection dummyConnForCheckPurpose = DBConnectionFactory.getPersistentConnection()) {} 
			System.out.println(ApplicationProperties.getLogConfiguration().instance + "|" + 
	                   formatter.format(new Date()) + "|Persistent database Initialized.");
			
			DBConnectionFactory.initializeInMemoryDataSource();
			try (Jedis dummyRedisConnForCheckPurpose = DBConnectionFactory.getInMemoryConnection()) {}
			System.out.println(ApplicationProperties.getLogConfiguration().instance + "|" + 
	                   formatter.format(new Date()) + "|In memory database Initialized.");
			
			DBConnectionFactory.initializeEmbededDataSource();
			System.out.println(ApplicationProperties.getLogConfiguration().instance + "|" + 
	                   formatter.format(new Date()) + "|Loaded rating data into application memory.");
			
			BillingInterfaceFactory.loadBillingInterfaceInformation();
			
			Authentication.loadApplicationInfo();
		    Authorization.loadTPSInfo();
		    
		    if (ApplicationProperties.getRatePlanConf().callTypeIdCheck) {
		    	CallTypeChecker.loadCallTypeInfo();
		    }
		    
		    System.out.println(ApplicationProperties.getLogConfiguration().instance + "|" + 
	                   formatter.format(new Date()) + "|Loaded billing and application related information into application memory.");
			
		    ApplicationProperties.usingOldWallet = DBClient.isUsingOldWalletTable();
		    if (ApplicationProperties.usingOldWallet) {
		    	System.out.println(ApplicationProperties.getLogConfiguration().instance + "|" + 
		                   formatter.format(new Date()) + "|Using Old Wallet table<Exclude product_name, priority>.");
		    } else {
		    	System.out.println(ApplicationProperties.getLogConfiguration().instance + "|" + 
		                   formatter.format(new Date()) + "|Using New Wallet table<Include product_name, priority>.");
		    }
		    
			PackageLoader pl = PackageLoader.getInstance();
            boolean packageTableLoadSuccess = false;
			
			do {
				if (ApplicationProperties.getServerSpecificConfiguration().loadPackageTable) {
					// Now check if table already loaded.
					Tuple<Boolean, Optional<String>> tablePrefix = DBClient.getCurrentPackageTablePrefix();
					if (!tablePrefix.second.isPresent()) {
						System.out.println(ApplicationProperties.getLogConfiguration().instance + "|" + 
				                   formatter.format(new Date()) + "|Loading `package` table into REDIS server.");
						packageTableLoadSuccess = pl.loadPackageInformation();
						if (!packageTableLoadSuccess) break;
						
						System.out.println(ApplicationProperties.getLogConfiguration().instance + "|" + 
				                   formatter.format(new Date()) + "|Loaded `package` table into REDIS server.");
				    } else { // Already loaded; change to current prefix.
				    	pl.changePackageKeyPrefix(tablePrefix.second.get());
				    	packageTableLoadSuccess = true;
				    	
				    	System.out.println(ApplicationProperties.getLogConfiguration().instance + "|" + 
				                   formatter.format(new Date()) + "|`package` table already loaded. Setted current package table prefix.");
				    }
				} else {
					System.out.println(ApplicationProperties.getLogConfiguration().instance + "|" + 
			                   formatter.format(new Date()) 
					          + "|Configured not to load `package` table. Setting current package table prefix.");
					
					packageTableLoadSuccess = pl.checkIfPrefixNeedToBeChange();
					if (!packageTableLoadSuccess) break;
					
					System.out.println(ApplicationProperties.getLogConfiguration().instance + "|" + 
			                   formatter.format(new Date()) + "|Setted current package table prefix.");
				}
			} while (false);
			
			if (!packageTableLoadSuccess) {
				System.out.println(ApplicationProperties.getLogConfiguration().instance + "|" + 
		                          formatter.format(new Date()) + "|`package` table loading failed. See app.log file under <Root directory>/logs/ for details.");
				System.exit(-1);
			}
			
			pl.start();  //package loading thread is started
			
			if (ApplicationProperties.getBillingInterfaceConf().isINIntegrated && ApplicationProperties.IN_INITIALIZER != null) {
				System.out.println(ApplicationProperties.getLogConfiguration().instance + "|" + 
		                   formatter.format(new Date()) + "|Starting IN Requester [**" + ApplicationProperties.IN_REQUESTER_NAME + "**]");
				
				try {
					INRequesterInitializer inInitializer = (INRequesterInitializer) ApplicationProperties.IN_INITIALIZER.newInstance();
					inInitializer.initialize();
				} catch (InstantiationException | IllegalAccessException e) {
					e.printStackTrace();
					System.exit(-1);
				}
			}
			

			for (int i = 0; i < ApplicationProperties.getCDRConfiguration().numberOfCdrProcessingThreads; i++) {
				SimpleTransactionLogGenerator stlg = SimpleTransactionLogGenerator.getInstance(i);
				Thread transactionGenerator = new Thread(stlg);
				transactionGenerator.start();
				
				logGenerators.add(stlg);
			}
			
		    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			System.out.printf("##########  Release date: %s  #########%s", formatter.format(new Date()), System.lineSeparator());
			
			Server server = Server.getInstance();
			server.start(); //server thread started
			CommandServer cServer = new CommandServer(ApplicationProperties.getServerSpecificConfiguration().commandListeningPort);
			cServer.start();
	
			if (ApplicationProperties.getLogServerConfiguration().enable) {
				StateMonitoring.getInstance().startMonitoring();
			}
			
			synchronized(ApplicationProperties.getShutdownController()) {
				ApplicationProperties.getShutdownController().wait();
				stopApplication();
				System.exit(0);
			}
		} catch (IOException | ClassNotFoundException | SQLException e) {
			e.printStackTrace();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		
		System.exit(-1);
	}
	
    private static void stopApplication() {
        System.out.print(ApplicationProperties.getLogConfiguration().instance + "|" +
                CGW.formatter.format(new Date()) + "|Stopping Listeing Server ... ");
        try {
            Server.getInstance().shutdown();
        } catch (IOException e) {
            System.out.println("Exception happened while trying to shutdown server.");
        }
        System.out.println("Ok.");

        System.out.print(ApplicationProperties.getLogConfiguration().instance + "|" +
                CGW.formatter.format(new Date()) + "|Stopping package loader ... ");
        PackageLoader.getInstance().stopLoader();
        System.out.println("Ok.");

        System.out.print(ApplicationProperties.getLogConfiguration().instance + "|" +
                CGW.formatter.format(new Date()) + "|Stopping CDR generators ... ");
        while (true) {
            boolean allFinished = CGW.logGenerators.stream().allMatch(SimpleTransactionLogGenerator::isCdrProcessingFinished);

            if (allFinished) {
                CGW.logGenerators.forEach(SimpleTransactionLogGenerator::shutdownCdrProcessors);
                break;
            }

            try { Thread.sleep(1000); } catch (InterruptedException exception) {
                System.out.println("Exception happened while trying to sleep.");
            }
        }
        System.out.println("Ok.");

        System.out.print(ApplicationProperties.getLogConfiguration().instance + "|" +
                CGW.formatter.format(new Date()) + "|Stopping Log Server ... ");
        if (ApplicationProperties.getLogServerConfiguration().enable) {
            StateMonitoring.getInstance().shutdownMonitoring();
        }
        System.out.println("Ok.");

        System.out.print(ApplicationProperties.getLogConfiguration().instance + "|" +
                CGW.formatter.format(new Date()) + "|Clearing all resources ... ");
        DBConnectionFactory.closeAllDataSources();
        ApplicationProperties.clearHttpClient();
        System.out.println("Ok.");
    }
	
	private static void initializeLogger() {
		LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
		Configuration config = ctx.getConfiguration();
		LoggerConfig loggerConfig = config.getLoggerConfig(CGW.class.getName());
		
		loggerConfig.setLevel(getLevel());
		ctx.updateLoggers();
	}
	
	private static Level getLevel() {
		switch (ApplicationProperties.getLogConfiguration().level) {
			case 0:
				return Level.OFF;
			case 1:
				return Level.FATAL;
			case 2:
				return Level.ERROR;
			case 3:
				return Level.WARN;
			case 4:
				return Level.INFO;
			case 5:
				return Level.DEBUG;
			case 6:
				return Level.ALL;
			default:
				return Level.DEBUG;
		}
	}
}
