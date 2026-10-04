package com.ssdtech.java.log.transaction;

import java.io.FileWriter;
import java.io.IOException;
import java.sql.BatchUpdateException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

//import com.mysql.jdbc.Statement;
import java.sql.Statement;
import com.ssdtech.java.db.management.DBConnectionFactory;
import com.ssdtech.java.model.applicationproperties.ApplicationProperties;
import com.ssdtech.java.model.applicationproperties.CDRConfiguration;
import com.ssdtech.java.model.applicationproperties.ServerSpecificConfiguration;
import com.ssdtech.java.utils.RedisKey;

import redis.clients.jedis.Jedis;

public class SimpleTransactionLogGenerator implements Runnable {
	private static final Logger logger = LogManager.getLogger(SimpleTransactionLogGenerator.class.getName());
	
	private long id;
	
    private static final String cdrFileFormat = "%s/CDR%s%04d%02d%02d%02d%02d%02d.txt";
  
    private static final String LOG_GENERATOR_PROCESSED_PREFIX = "last_cdr_generator_processed_";
    private static final String LOG_GENERATOR_FAILED_PREFIX = "last_cdr_generator_failed_processing_";
    private static final int OFFSET = 5;
    
    private CDRConfiguration cdrProperties;
    private ServerSpecificConfiguration serverSpecificProperties;
    private ScheduledExecutorService cdrProcessor;
    private ScheduledExecutorService failedCdrProcessor;
    
	private SimpleTransactionLogGenerator(long id) {
		this.id = id;		
		this.serverSpecificProperties = ApplicationProperties.getServerSpecificConfiguration();
		this.cdrProperties = ApplicationProperties.getCDRConfiguration();
		
		try (Jedis redis = DBConnectionFactory.getInMemoryConnection()) {
			long lastCdrProcessingTime = Long.valueOf(redis.time().get(0)); 
			redis.setnx(LOG_GENERATOR_PROCESSED_PREFIX + this.id, String.valueOf(lastCdrProcessingTime)); // If key not found, then retrieve it.
		} catch (Exception exception) {
			logger.error("Exception while setting last cdr processing time.", exception);
		}
	}

	public static SimpleTransactionLogGenerator getInstance(long id){
		return new SimpleTransactionLogGenerator(id);
	}

	@Override
	public void run() {
		this.cdrProcessor = Executors.newScheduledThreadPool(1);
		this.cdrProcessor.scheduleAtFixedRate(() -> {
			try (Jedis redis = DBConnectionFactory.getInMemoryConnection()) {
				long currentEpochSeconds = Long.valueOf(redis.time().get(0));
				long lastCdrProcessingTime = Long.valueOf(redis.get(LOG_GENERATOR_PROCESSED_PREFIX + this.id));
				long epochSecondPointer = currentEpochSeconds - OFFSET;
				
				while (epochSecondPointer >= lastCdrProcessingTime) {
					if (redis.setnx(RedisKey.getKeyForCdrProcessing(epochSecondPointer), "locked") > 0) {
						List<String> contents = redis.lrange(RedisKey.getKeyForCdrRetrieval(epochSecondPointer), 0, -1);
						redis.del(RedisKey.getKeyForCdrRetrieval(epochSecondPointer));
						this.saveTransactions(contents, epochSecondPointer);
						redis.del(RedisKey.getKeyForCdrProcessing(epochSecondPointer));
					} else { // Set timeout if not set/key deleted.
						long ttl = redis.ttl(RedisKey.getKeyForCdrProcessing(epochSecondPointer));
						long len = redis.llen(RedisKey.getKeyForCdrRetrieval(epochSecondPointer));
						if (ttl < 0 && len <= 0) { // We can set timed out both.
							redis.expire(RedisKey.getKeyForCdrRetrieval(epochSecondPointer), 60); // 60 Seconds.
							redis.expire(RedisKey.getKeyForCdrProcessing(epochSecondPointer), 60); // 60 Seconds.
						} else if (ttl < 0) { // List has some CDRs. Unlock it so that it can be processed again.
							redis.expire(RedisKey.getKeyForCdrProcessing(epochSecondPointer), 60); // 60 Seconds.
							currentEpochSeconds = epochSecondPointer; // So that this list will be included in processing range.
						}
					}
					
					epochSecondPointer = epochSecondPointer - 1;
				}
				
				redis.set(LOG_GENERATOR_PROCESSED_PREFIX + this.id, String.valueOf(currentEpochSeconds - OFFSET));
			} catch (Exception e) { 
				logger.error("Exception while saving CDR.", e);
			}
		}, 0, this.cdrProperties.cdrFileCreationInterval, TimeUnit.SECONDS);
		
		this.failedCdrProcessor = Executors.newScheduledThreadPool(1);
		this.failedCdrProcessor.scheduleAtFixedRate(() -> {
			List<String> contents = new ArrayList<>();
        	try (Jedis redis = DBConnectionFactory.getInMemoryConnection()) {
				String content = redis.lpop(RedisKey.getKeyForCdrFailed(this.id));
				while (content != null) {
					contents.clear();
					contents.add(content);
					this.saveTransactions(contents, Instant.now().getEpochSecond());
					content = redis.lpop(RedisKey.getKeyForCdrFailed(this.id));
				}
				redis.set(LOG_GENERATOR_FAILED_PREFIX + this.id, String.valueOf(Instant.now().getEpochSecond()));
			} catch (Exception e) {
				logger.error("Exception while saving failed CDR.", e);
			}
		}, 0, 10, TimeUnit.SECONDS);
	}

	public void storeTransactionLog(String serializedTransaction) {		
		List<String> contents = new ArrayList<>();
		contents.add(serializedTransaction);
		this.saveTransactions(contents, Instant.now().getEpochSecond());
	}
	
	public boolean isCdrProcessingFinished() {
		try (Jedis redis = DBConnectionFactory.getInMemoryConnection()) {
			return redis.keys(RedisKey.getWildKeyPatternForCdrRetrieval()).isEmpty()
					&& redis.llen(RedisKey.getKeyForCdrFailed(id)) == 0;
		} catch (Exception e) {}
		
		return false;
	}
	
	public void shutdownCdrProcessors() {
		this.cdrProcessor.shutdown();
		
		try {
			this.cdrProcessor.awaitTermination(1, TimeUnit.DAYS); // This is just a BIG value to treat as INFINITY.
		} catch (InterruptedException e) {}
		
		this.failedCdrProcessor.shutdown();
		
		try {
			this.failedCdrProcessor.awaitTermination(1, TimeUnit.DAYS); // This is just a BIG value to treat as INFINITY.
		} catch (InterruptedException e) {}
	}
	
	private void saveTransactions(List<String> contents, long currentEpochSeconds) {
		if (this.cdrProperties.cdrWriteInFileEnable) {
			this.saveTransactionsToFile(contents);
		} else {
			this.saveTransactionsToDBWithFailSafe(contents, currentEpochSeconds);
		}
	}
	
	private void saveTransactionsToFile(List<String> contents) {
		if (contents.isEmpty()) return;
		
		Calendar calendar = Calendar.getInstance(); // Locale should be set and it should be global.
		calendar.setTime(new Date());
		
		String cdrFileName = String.format(cdrFileFormat,
								            this.cdrProperties.cdrDirectory,
								            this.serverSpecificProperties.cgwId,
								            calendar.get(Calendar.YEAR),
								            calendar.get(Calendar.MONTH) + 1,
								            calendar.get(Calendar.DAY_OF_MONTH),
								            calendar.get(Calendar.HOUR_OF_DAY),
								            calendar.get(Calendar.MINUTE),
								            calendar.get(Calendar.SECOND) / this.cdrProperties.cdrFileCreationInterval);
		
		StringBuilder builder = new StringBuilder();
		for (String content : contents) {
			builder.append(content).append(this.cdrProperties.lineSeparator);
		}
		
		try (FileWriter out = new FileWriter(cdrFileName, true)) {
			out.write(builder.toString());
		} catch (IOException e) {
			logger.error("Exception while saving CDR data to file.", e);
		}
	}
	
	private void saveTransactionsToDBWithFailSafe(List<String> contents, long currentEpochSeconds) {
		if (contents.isEmpty()) return;
		
		List<TransactionLog> validSerializedTransactions = new ArrayList<>();
		List<String> invalidSerializedTransactions = new ArrayList<>();

		for (String content : contents) {
			try {
				TransactionLog tLog = TransactionLog.deserialize(content);
				validSerializedTransactions.add(tLog);
			} catch (TransactionLogException exception) {
				invalidSerializedTransactions.add(content);
			}
		}
		
		ExecutionStatus[] processedStatus = this.saveTransactionsToDB(validSerializedTransactions);
		for (int index = 0; index < processedStatus.length; index++) {
			if (processedStatus[index].equals(ExecutionStatus.FAILED)) {
				this.setIntoRedis(RedisKey.getKeyForCdrFailed(this.id), validSerializedTransactions.get(index).serialize()); // Ensure fail safety.
			}
		}
		
		for (int index = 0; index < invalidSerializedTransactions.size(); index++) {
			this.setIntoRedis(RedisKey.getKeyForCdrInvalid(currentEpochSeconds), contents.get(index)); // Ensure fail safety.
		}
	}
	
	private ExecutionStatus[] saveTransactionsToDB(List<TransactionLog> contents) {
		if (contents.isEmpty()) return new ExecutionStatus[0];
		
		ExecutionStatus[] status = new ExecutionStatus[contents.size()];
		Arrays.fill(status, ExecutionStatus.FAILED);
		
		String insertQuery = "INSERT INTO cdrintegrated(CDRID,ServiceID,ChargingType,CGWID,Ano,Bno,Direction,RateID,ResultCode,Amount,StartTime,EndTime,Purpose,PackageID,BillingNode,UserID,LastUpdate,SubscriptionStatus,TotalConsumedUnit,BalanceBefore) " + 
	             "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?);";

		try (
			Connection connection = DBConnectionFactory.getPersistentConnection();
			PreparedStatement preparedStatement = connection.prepareStatement(insertQuery)) {
			
			int currentBatchSize = 0;
			int offset = 0;
			
			for (int index = 0; index < contents.size(); index++) {
				TransactionLog log = contents.get(index);
                
				preparedStatement.setString(1, log.getCdrId());
				preparedStatement.setString(2, log.getServiceId());
				preparedStatement.setString(3, log.getChargingType());
				preparedStatement.setString(4, log.getCgwId());
				preparedStatement.setString(5, log.getAno());
				preparedStatement.setString(6, log.getBno());
				preparedStatement.setInt(7, log.getDirection());
				preparedStatement.setString(8, log.getRateId());
				preparedStatement.setString(9, log.getResultCode());
				preparedStatement.setLong(10, log.getAmount());
				preparedStatement.setString(11, log.getStartTime());
				preparedStatement.setString(12, log.getEndTime());
				preparedStatement.setString(13, log.getPurpose());
				preparedStatement.setString(14, log.getPackageId());
				preparedStatement.setString(15, String.valueOf(log.getBillingPort()));
				preparedStatement.setString(16, log.getUserId());
				preparedStatement.setString(17, log.getLastUpdate());
				preparedStatement.setString(18, log.getSubscriptionStatus());
				preparedStatement.setLong(19, log.getTotalConsumedUnit());
				preparedStatement.setLong(20, log.getBalanceBefore());
				
				preparedStatement.addBatch();
				++currentBatchSize;
				
				if (currentBatchSize == this.cdrProperties.batchSize || (index == contents.size() - 1)) {
					try {
						offset = this.updateExecutionStatus(status, offset, preparedStatement.executeBatch());
					} catch (BatchUpdateException e) {
						offset = this.updateExecutionStatus(status, offset, e.getUpdateCounts());
					}
				    
				    currentBatchSize = 0;
				}
			}
		} catch (SQLException e) { 
			logger.error("Exception while saving CDR into Database.", e);
		}
		
		return status;
	}
	
	private int updateExecutionStatus(ExecutionStatus[] status, int offset, int[] updateCounts) {
		for (int resultIndex = 0; resultIndex < updateCounts.length; resultIndex++) {
	    	status[offset + resultIndex] = ExecutionStatus.getExecutionStatus(updateCounts[resultIndex]); 
	    }
		
		return offset + updateCounts.length;
	}
	
	private void setIntoRedis(String key, String value) {
		for (int retryCounter = 0;
				retryCounter < ApplicationProperties.getDatabaseConfiguration().getMemDBConf().maxRetry;
				retryCounter++) {
			try (Jedis redis = DBConnectionFactory.getInMemoryConnection()) {
				redis.rpush(key, value);
				break;
			} catch (Exception e) {
				logger.error("Exception while saving failed CDR data into key[{}]. retry counter[{}].", key, retryCounter, e);
			}
		}
	}
	
	enum ExecutionStatus {
		SUCCESS,
		FAILED,
		INVALID;
		
		public static ExecutionStatus getExecutionStatus(int numericValue) {
			switch(numericValue) {
				case Statement.EXECUTE_FAILED: // Constant value : -3
					return ExecutionStatus.FAILED;
				case Statement.SUCCESS_NO_INFO:  // Constant value : -2
					return ExecutionStatus.SUCCESS;
				case -1:
					return ExecutionStatus.INVALID;
				case 0:
					return ExecutionStatus.FAILED;
				case Statement.RETURN_GENERATED_KEYS:
					return ExecutionStatus.SUCCESS;
				default:
					return ExecutionStatus.FAILED;
			}
		}
	}
}
