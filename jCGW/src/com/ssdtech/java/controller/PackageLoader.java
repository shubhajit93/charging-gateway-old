package com.ssdtech.java.controller;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Connection;
import java.sql.Statement;
import java.time.LocalTime;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ssdtech.java.db.client.DBClient;
import com.ssdtech.java.db.management.DBConnectionFactory;
import com.ssdtech.java.model.PackageKeyPrefix;
import com.ssdtech.java.model.applicationproperties.ApplicationProperties;
import com.ssdtech.java.utils.Tuple;
import com.ssdtech.java.utils.Utility;

import redis.clients.jedis.Jedis;

public class PackageLoader extends Thread {
	private static final int RETRY = 5;
    private static volatile PackageKeyPrefix prefix = PackageKeyPrefix.INVALID_KEY;
    private static volatile int slot = -1;
    private static final Logger logger = LogManager.getLogger(PackageLoader.class.getName());
    
    private static boolean packageTableLoadSuccess = false;
    private static boolean specificPackageTableLoadSuccess = false;
    private static volatile boolean stop = false;
    
    private static PackageLoader pkgLoader;
    
    private PackageLoader() {}
    
    public static synchronized PackageLoader getInstance() {
    	if (pkgLoader == null) {
    		pkgLoader = new PackageLoader();
    	}
    	
    	return pkgLoader;
    }
	
    @Override
    public void run() {
    	while (ApplicationProperties.getServerSpecificConfiguration().loadPackageTable) {
	    	this.sleepUntil(this.sleepDuration());
	    	
	    	if (stop) return;
	    	this.loadPackageInformation();
    	}
    	
    	Executors.newScheduledThreadPool(1).scheduleAtFixedRate(() -> {
    		logger.info("Checking if package table prefix :: Current prefix: {}.", prefix.getPrefix());
			
			if (this.checkIfPrefixNeedToBeChange()) {
				logger.info("package table prefix updated :: Updated prefix: {}.", prefix.getPrefix());
			}
    	}, 0, 60, TimeUnit.SECONDS); 
    }
    
    public boolean checkIfPrefixNeedToBeChange() {
    	Tuple<Boolean, Optional<String>> pkgPrefixInfo = new Tuple<Boolean, Optional<String>>(false, Optional.empty());
    	do {
    		pkgPrefixInfo = DBClient.getCurrentPackageTablePrefix();
    	} while (!pkgPrefixInfo.first || !pkgPrefixInfo.second.isPresent());
    	
    	logger.info("Current package table prefix: {}.", pkgPrefixInfo.second.get());
    	
    	if (pkgPrefixInfo.second.get().equals(prefix.getPrefix())) {
    		return false;
    	}
    	if (!this.isValidPrefix(pkgPrefixInfo.second.get())) {
    		return false;
    	}
    	
    	this.changePackageKeyPrefix(pkgPrefixInfo.second.get());
    	return true;
    }
    
    public void changePackageKeyPrefix(String prefixKey) {
    	PackageKeyPrefix newPrefix = PackageKeyPrefix.SETTER_KEY;
		newPrefix.setPrefix(prefixKey);
		prefix = newPrefix;
		slot = this.getSlot(newPrefix);
    }
    
    public boolean loadPackageInformation() {
    	int retryCounter = 0;    	

    	while (retryCounter < RETRY) {
    		try {
    			logger.info("Loading package table :: Current prefix: {}, Current retry counter: {}", prefix.getPrefix(), retryCounter);
		    	
    			packageTableLoadSuccess = specificPackageTableLoadSuccess = false;
    			if (ApplicationProperties.getRatePlanConf().packageRangeCheck) {
		    		this.loadPackageInformationWithRange((slot + 1) % 2);
		    	} else {
		    		this.loadPackageInformationWithoutRange((slot + 1) % 2);
		    	}
		    	
		    	slot = (slot + 1) % 2;
		    	PackageKeyPrefix prePrefix = prefix;
		    	prefix = (slot == 0) ? PackageKeyPrefix.PRIMARY_KEY : PackageKeyPrefix.SECONDARY_KEY;
		    	
		    	logger.info("Loaded package table :: Current prefix: {}.", prefix.getPrefix());
		    	logger.info("Setting prefix [{}].", prefix.getPrefix());
		    	
		    	boolean success = false;
		    	do {
		    	    success = DBClient.setCurrentPackageTablePrefix(prefix.getPrefix());
		    	} while (!success);
		    	
		    	logger.info("Setting Prefix [{}] successful.", prefix.getPrefix());
		    	logger.info("Deleting Previous Prefix [{}].", prePrefix.getPrefix());
		    	
		    	DBClient.deleteKeyUsingPattern(0, prePrefix.getPrefix() + "*");
                
		    	logger.info("Deleting Previous Prefix [{}] successful.", prePrefix.getPrefix());
		    	
		    	return true;
    		} catch (SQLException exception) {
    			logger.error("Exception while loading package table :: Current prefix: {}, Current retry counter: {}.", prefix.getPrefix(), retryCounter, exception);
    		} catch (Exception e) {
    			logger.error("Exception while loading package table :: Current prefix: {}, Current retry counter: {}.", prefix.getPrefix(), retryCounter, e);
    		}
    		
    		retryCounter = retryCounter + 1;
    	}
    	
    	return false;
    }
    
    public static PackageKeyPrefix getPackageKeyPrefix() {
    	return prefix;
    }
    
    public void stopLoader() {
    	stop = true;
    	Thread.currentThread().interrupt();
    }
    
    private boolean isValidPrefix(String prefix) {
    	if (PackageKeyPrefix.PRIMARY_KEY.getPrefix().equals(prefix)
    			|| PackageKeyPrefix.SECONDARY_KEY.getPrefix().equals(prefix)) {
    		return true;
    	}
    	
    	return false;
    }
    
    private int getSlot(PackageKeyPrefix prefix) {
    	if (prefix.equals(PackageKeyPrefix.PRIMARY_KEY)) {
    		return 0;
    	} else if (prefix.equals(PackageKeyPrefix.SECONDARY_KEY)) {
    		return 1;
    	} else {
    		return -1;
    	}
    }
    
	private void loadPackageInformationWithRange(int slot) throws SQLException, Exception {
		PackageKeyPrefix prefix = (slot == 0) ? PackageKeyPrefix.PRIMARY_KEY : PackageKeyPrefix.SECONDARY_KEY;
		
		String searchQuery = "SELECT StartMSISDN, (EndMSISDN - StartMSISDN) + 1 AS len, BillingNodeID, PackageID "
				+ "FROM package " + "ORDER BY StartMSISDN ASC";
		
		if (!packageTableLoadSuccess) {
			Optional<Connection> connection = Utility.getRawMySQLConnection();
			if (!connection.isPresent()) return;
			
			try (Jedis redis = DBConnectionFactory.getInMemoryConnection();
					Statement statement = connection.get().createStatement();
					ResultSet resultSet = statement.executeQuery(searchQuery)) {
				
				while (resultSet.next()) {
					String ano = resultSet.getString("StartMSISDN");
					String len = resultSet.getString("len");
					String billingNodeId = resultSet.getString("BillingNodeID");
					String packageId = resultSet.getString("PackageID");
	
					redis.zadd(prefix.getPrefix(), 0.0, "0" + ano + ":" + len + ":" + billingNodeId + ":" + packageId);
				}
			}
			connection.get().close();
			packageTableLoadSuccess = true;
		}

		searchQuery = "SELECT StartMSISDN, (EndMSISDN - StartMSISDN) + 1 AS len, BillingNodeID, PackageID "
				+ "FROM specificpackage " + "ORDER BY StartMSISDN ASC";

		if (!specificPackageTableLoadSuccess) {
			Optional<Connection> connection = Utility.getRawMySQLConnection();
			if (!connection.isPresent()) return;
			
			try (Jedis redis = DBConnectionFactory.getInMemoryConnection();
					Statement statement = connection.get().createStatement();
					ResultSet resultSet = statement.executeQuery(searchQuery)) {
				
				while (resultSet.next()) {
					String ano = resultSet.getString("StartMSISDN");
					String len = resultSet.getString("len");
					String billingNodeId = resultSet.getString("BillingNodeID");
					String packageId = resultSet.getString("PackageID");
	
					redis.zadd(prefix.getPrefix(), 0.0, "1" + ano + ":" + len + ":" + billingNodeId + ":" + packageId);
				}
			}
			connection.get().close();
			specificPackageTableLoadSuccess = true;
		}
	}
	
	private void loadPackageInformationWithoutRange(int slot) throws SQLException, Exception {
		PackageKeyPrefix prefix = (slot == 0) ? PackageKeyPrefix.PRIMARY_KEY : PackageKeyPrefix.SECONDARY_KEY;
		
		String searchQuery = "SELECT StartMSISDN, BillingNodeID, PackageID "
				+ "FROM package " + "ORDER BY StartMSISDN ASC";
		
		if (!packageTableLoadSuccess) {
			Optional<Connection> connection = Utility.getRawMySQLConnection();
			if (!connection.isPresent()) return;
			
			try (Jedis redis = DBConnectionFactory.getInMemoryConnection();
					Statement statement = connection.get().createStatement();
					ResultSet resultSet = statement.executeQuery(searchQuery)) {
				
				while (resultSet.next()) {
					String ano = resultSet.getString("StartMSISDN");
					String billingNodeId = resultSet.getString("BillingNodeID");
					String packageId = resultSet.getString("PackageID");
	
					redis.set(prefix.getPrefix() + ".0" + ano, billingNodeId + ":" + packageId);
				}
			}
			connection.get().close();
			packageTableLoadSuccess = true;
		}

		searchQuery = "SELECT StartMSISDN, (EndMSISDN - StartMSISDN) + 1 AS len, BillingNodeID, PackageID "
				+ "FROM specificpackage " + "ORDER BY StartMSISDN ASC";

		if (!specificPackageTableLoadSuccess) {
			Optional<Connection> connection = Utility.getRawMySQLConnection();
			if (!connection.isPresent()) return;
			
			try (Jedis redis = DBConnectionFactory.getInMemoryConnection();
					Statement statement = connection.get().createStatement();
					ResultSet resultSet = statement.executeQuery(searchQuery)) {
				
				while (resultSet.next()) {
					String ano = resultSet.getString("StartMSISDN");
					String billingNodeId = resultSet.getString("BillingNodeID");
					String packageId = resultSet.getString("PackageID");
	
					redis.set(prefix.getPrefix() + ".1" + ano, billingNodeId + ":" + packageId);
				}
			}
			connection.get().close();
			specificPackageTableLoadSuccess = true;
		}
	}
	
	private void sleepUntil(long sleepTime) {
		if (sleepTime < 0) return;
		
		try {
			Thread.sleep(sleepTime);
		} catch (InterruptedException e) {
			if (!stop) this.sleepUntil(this.sleepDuration());
		}
	}
	
	private long sleepDuration() {
		LocalTime time = LocalTime.now();
    	int totalPassedMinutes = time.getHour() * 60 + time.getMinute();
    	int remainingMinutesOfToday = 1440 - totalPassedMinutes;
    	return (remainingMinutesOfToday + ApplicationProperties.getRatePlanConf().packageLoadAfter) * 60000;
	}
}
