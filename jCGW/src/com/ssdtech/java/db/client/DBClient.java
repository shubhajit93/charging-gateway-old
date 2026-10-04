package com.ssdtech.java.db.client;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.ssdtech.java.model.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import com.ssdtech.java.controller.PackageLoader;
import com.ssdtech.java.db.management.DBConnectionFactory;
import com.ssdtech.java.db.management.EmbededDB;
import com.ssdtech.java.log.transaction.TransactionLog;
import com.ssdtech.java.model.applicationproperties.ApplicationProperties;
import com.ssdtech.java.utils.RedisKey;
import com.ssdtech.java.utils.Tuple;
import com.ssdtech.java.utils.Utility;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Transaction;

public final class DBClient {
	private static final Logger logger = LogManager.getLogger(DBClient.class.getName());

	private DBClient(){}
	
	public static List<RateMaster> getRateContexts(ProcedureParams params) {
		return DBConnectionFactory.getEmbededDataSource().getRateContexts(params);
	}
	
	public static String getCampaignRateId(String msisdn, String rateId) {
		String campaignRateId = null;
		String searchQuery = "SELECT applyrateid AS applyrateid FROM campaignrate WHERE uid=? AND rateid=? AND activationenddate>=NOW()";
		
		for (int retryCounter = 0; 
				retryCounter < ApplicationProperties.getDatabaseConfiguration().getPersistentDBConf().maxRetry; 
				retryCounter++) {
			try (Connection connection = DBConnectionFactory.getPersistentConnection();
					PreparedStatement statement = connection.prepareStatement(searchQuery)) {
				statement.setString(1, msisdn);
				statement.setString(2, rateId);
				try (ResultSet resultSet = statement.executeQuery()) {
					if (resultSet.next()) {
						campaignRateId = resultSet.getString("applyrateid");
					}
					break;
				} catch (SQLException exception) {
					logger.error("Exception in retrieving data. Query [{}]. retry counter[{}].", statement, retryCounter, exception);
				}
			} catch (SQLException e) {
				logger.error("Exception while searching for campaign rate id. retry counter[{}].", retryCounter, e);
			}
		}
		
		return campaignRateId == null ? rateId : campaignRateId;
	}
	
	public static Tuple<Integer, RatePulse> getRatingInfo(String rateId, int stepNo) {
		return DBConnectionFactory.getEmbededDataSource().getRatingInfo(rateId, stepNo);
	}

	public static String getSubsCycleInfo(String SubgroupID)
	{
		return DBConnectionFactory.getEmbededDataSource().getsubCycleInfo(SubgroupID);
	}

	public static Optional<String> searchPackageInfoFor(String msisdn, String searchingSlot) {
		for (int retryCounter = 0; 
				retryCounter < ApplicationProperties.getDatabaseConfiguration().getMemDBConf().maxRetry;
				retryCounter++) {
			try (Jedis redis = DBConnectionFactory.getInMemoryConnection()) {
				com.ssdtech.java.model.PackageKeyPrefix prefix = PackageLoader.getPackageKeyPrefix();
				if (ApplicationProperties.getRatePlanConf().packageRangeCheck) {
					Set<String> set = redis.zrevrangeByLex(prefix.getPrefix() , "[" + searchingSlot + msisdn + "\\xff",
							                                   "(" + searchingSlot + "0000000000\\xff", 0, 1);
					if (!set.isEmpty()) {
						return Optional.of(set.iterator().next());
					}
					return Optional.empty();
				} else {
					return Optional.ofNullable(redis.get(prefix.getPrefix() + "." + searchingSlot + msisdn));
				}
			} catch (Exception e) {
				logger.error("Exception while searching for package id. MSISDN [{}], Searching slot [{}], retry counter[{}]", msisdn, searchingSlot, retryCounter, e);
			}
		}
		return Optional.empty();
	}
	
	public static String getAggregatedSessionInfo(String cdrId) {
		for (int retryCounter = 0; 
				retryCounter < ApplicationProperties.getDatabaseConfiguration().getMemDBConf().maxRetry;
				retryCounter++) {
			try (Jedis redis = DBConnectionFactory.getInMemoryConnection()) {
				return redis.get("sei." + cdrId);
			} catch (Exception e) {
				logger.error("Exception while searching for session info. CDR Id [{}], retry counter[{}]", cdrId, retryCounter, e);
			}
		}
		
		return null;
	}
	
	public static void updateSessionInformation(ProcedureParams params) {
		String valueSeparator = "::";
		for (int retryCounter = 0; 
				retryCounter < ApplicationProperties.getDatabaseConfiguration().getMemDBConf().maxRetry;
				retryCounter++) {
			try (Jedis redis = DBConnectionFactory.getInMemoryConnection()) {
				if(!params.getSessionInfo().isPresent()) break;
				SessionInfo si = params.getSessionInfo().get();
				Wallet wallet = si.getWallet();
				
				String updatedSessionInfo = params.getRateId() + valueSeparator +
	                    String.valueOf(si.getTotalConsumedUnit()) + valueSeparator +
	                    String.valueOf(si.getLastReservedUnit()) + valueSeparator +
	                    String.valueOf(si.getTotalChargedAmount()) + valueSeparator +
	                    String.valueOf(si.getLastChargedAmount()) + valueSeparator +
	                    params.getSubscriberService().getNumericSubscriptionStatus() + valueSeparator +
	                    String.valueOf(si.getNextStepNo()) + valueSeparator +
	                    String.valueOf(si.getPulseLimitOfCurrentStepNo()) + valueSeparator +
	                    si.getSessionStartTime() + valueSeparator + 
	                    si.getCurrentWalletType() + valueSeparator +
	                    wallet.getId() + valueSeparator + 
	                    wallet.getBalance() + valueSeparator +
	                    wallet.getWalletExpirationDate() + valueSeparator +
	                    (wallet.isFreeWallet() ? "1" : "0") + valueSeparator +
	                    wallet.getFreeWalletEndDate();
				
	            redis.set("sei." + params.getRequest().getCDRId(), updatedSessionInfo);
	            break;
			} catch (Exception e) {
				logger.error("Exception while updating session information. CDR Id [{}], retry counter[{}]", params.getRequest().getCDRId(), retryCounter, e);
			}
		}
	}
	
	public static void deleteSessionInfo(String cdrId) {
		for (int retryCounter = 0; 
				retryCounter < ApplicationProperties.getDatabaseConfiguration().getMemDBConf().maxRetry;
				retryCounter++) {
			try (Jedis redis = DBConnectionFactory.getInMemoryConnection()) {
				redis.del("sei." + cdrId);
				break;
			} catch (Exception e) {
				logger.error("Exception while deleting session info. CDR Id [{}], retry counter[{}].", cdrId, retryCounter, e);
			}
		}
	}
	
	public static int walletOption(String serviceId) {
		return DBConnectionFactory.getEmbededDataSource().walletOption(serviceId);
	}
	
	public static Optional<SubscriberService> getSubscriberInformation(String msisdn, String serviceId, String requestedSubGroupId, String requestedChannel) {
		return ApplicationProperties.getServerSpecificConfiguration().retrieveSubscriptionInfoUsingInMemDB ? 
				getSubscriberInformationUsingInMemDB(msisdn, serviceId, requestedSubGroupId, requestedChannel) :
					getSubscriberInformationUsingPersistentDB(msisdn, serviceId, requestedSubGroupId, requestedChannel);
	}
	
	public static Optional<SubscriberService> getSubscriberInformationUsingPersistentDB(String msisdn, String serviceId, String requestedSubGroupId, String requestedChannel) {
		String searchQuery = "SELECT LOWER(SubscriptionGroupID) AS SubscriptionGroupID, LOWER(status) AS status, LOWER(channel) AS channel FROM subscriberservices " 
                           + "WHERE msisdn=? AND SubscriptionGroupID IN (SELECT SubscriptionGroupID FROM subscriptiongroup WHERE ServiceID=?) " 
		                   + "ORDER BY RegistrationDate DESC LIMIT 1";

		for (int retryCounter = 0;
				retryCounter < ApplicationProperties.getDatabaseConfiguration().getPersistentDBConf().maxRetry;
				retryCounter++) {
			try (
				Connection dbConnection = DBConnectionFactory.getPersistentConnection();
				PreparedStatement statement = dbConnection.prepareStatement(searchQuery)
			    ) {
				statement.setString(1, msisdn);
				statement.setString(2, serviceId);
				
				try (ResultSet resultSet = statement.executeQuery()) {
					if (resultSet.next()) {
						String subscriptionGroupId = resultSet.getString("SubscriptionGroupID");
						String status = resultSet.getString("status");
						String channel = resultSet.getString("channel");

						if (subscriptionGroupId != null && !subscriptionGroupId.trim().isEmpty() 
								&& (requestedSubGroupId.trim().isEmpty() || requestedSubGroupId.equalsIgnoreCase("na"))) {
							requestedSubGroupId = subscriptionGroupId;
						}
						
						if (channel != null && !channel.trim().isEmpty()) {
							requestedChannel = channel;
						}
						
						return Optional.of(new SubscriberService(requestedSubGroupId, status, requestedChannel));
					}
				} catch (SQLException e) {
					logger.error("Exception while retrieving subscription information [Reason: {}, Query: {}]", e.getMessage(), statement);
				}
				
			} catch (Exception e) {
				logger.error("Exception while searching for subscription information [Reason: {}, MSISDN: {}, ServiceId: {}]", e.getMessage(), msisdn, serviceId);
			}
		}
		
		return Optional.empty();
	}
	
	public static Optional<SubscriberService> getSubscriberInformationUsingInMemDB(String msisdn, String serviceId, String requestedSubGroupId, String requestedChannel) {
		String subscriptionRoot = DBConnectionFactory.getEmbededDataSource().getSubscriptionRoot(serviceId);
		
		for (int retryCounter = 0;
				retryCounter < ApplicationProperties.getDatabaseConfiguration().getMemDBConf().maxRetry;
				retryCounter++) {
			try (Jedis redis = DBConnectionFactory.getInMemoryConnection()) {
				String serializedSubscriptionInfo = redis.get(RedisKey.getKeyForSubscriptionInformation(msisdn, subscriptionRoot));		
				return SubscriberService.getInstance(serializedSubscriptionInfo, requestedSubGroupId, requestedChannel);
			} catch (Exception e) {}
		}
		
		return Optional.empty();
	}
	
	public static Set<String> getRegisteredServices(String msisdn) {
		Set<String> registeredServices = new HashSet<>();
		registeredServices.add("na");
		
		String searchQuery = "SELECT LOWER(SubscriptionGroupID) AS SubscriptionGroupID FROM subscriberservices WHERE msisdn=? AND STATUS<>'DeRegistered'";
		
		for (int retryCounter = 0;
				retryCounter < ApplicationProperties.getDatabaseConfiguration().getPersistentDBConf().maxRetry;
				retryCounter++) {
			try (Connection dbConnection = DBConnectionFactory.getPersistentConnection();
				PreparedStatement statement = dbConnection.prepareStatement(searchQuery)
			    ) {
				statement.setString(1, msisdn);
				
				try (ResultSet resultSet = statement.executeQuery()) {
					while (resultSet.next()) {
						registeredServices.add(resultSet.getString("SubscriptionGroupID"));
					}
					break;
				} catch (SQLException e) {}
			} catch (Exception e) {}
		}
		return registeredServices;
	}
	
	public static boolean isUsingOldWalletTable() {
		List<String> columnNames = new ArrayList<>();
		columnNames.add("product_name");
		columnNames.add("priority");
		
        return !isAllColumnExists("wallet", columnNames);
	}
	
	public static Wallet getServiceWalletInformation(String msisdn, String bno, String walletId, String serviceId, String productcode) {
		return ApplicationProperties.getServerSpecificConfiguration().retrieveSubscriptionInfoUsingInMemDB ?
				   getWalletInformationUsingInMemDB(msisdn, bno, walletId, serviceId) :
				       getServiceWalletInformationUsingPersistentDB(msisdn, bno, walletId, serviceId, productcode);
	}
	
	private static Wallet getServiceWalletInformationUsingPersistentDB(String msisdn, String bno, String walletId, String serviceId, String productcode) {
		Wallet serviceWallet = null;
		
		String searchQuery = "SELECT id,bno,balance,IF(walletexpiry IS NULL, UNIX_TIMESTAMP('2037-12-31 23:59:59'), UNIX_TIMESTAMP(walletexpiry)) AS walletexpiry,freewallet,"
				           + "UNIX_TIMESTAMP(freewallet_enddate) AS freewallet_enddate, productcode "
	                       + "FROM wallet " 
				           + "WHERE uid=? "
				           + "AND walletid=? " 
				           + "AND serviceid=? "
				           + "AND productcode=? "
				           + "AND ? LIKE bno "
				           + "AND (walletexpiry IS NULL OR walletexpiry >= NOW()) "
				           + (ApplicationProperties.usingOldWallet ?
				        		   "ORDER BY LENGTH(bno) DESC " :
				        		   "AND balance > 0 ORDER BY priority ASC, walletexpiry ASC, LENGTH(bno) DESC ")
				           + "LIMIT 0,1";
		
		for (int retryCounter = 0; 
				retryCounter < ApplicationProperties.getDatabaseConfiguration().getPersistentDBConf().maxRetry;
				retryCounter++) {
			try (Connection connection = DBConnectionFactory.getPersistentConnection();
					PreparedStatement preparedStatement = connection.prepareStatement(searchQuery)) {
				
				preparedStatement.setString(1, msisdn);
				preparedStatement.setString(2, walletId);
				preparedStatement.setString(3, serviceId);
				preparedStatement.setString(4, productcode);
				preparedStatement.setString(5, bno);

				//System.out.println(preparedStatement);
	
				try (ResultSet resultSet = preparedStatement.executeQuery()) {
					//System.out.println("get wallet query:"+preparedStatement);
					if (resultSet.next()) {
						serviceWallet = new Wallet(
								                resultSet.getString("id"), 
								                walletId,
								                resultSet.getString("bno"), 
								                resultSet.getLong("balance"),
								                Wallet.DEFAULT_PRODUCT_NAME,
								                resultSet.getLong("walletexpiry"),
								                resultSet.getBoolean("freewallet"), 
								                resultSet.getLong("freewallet_enddate"),
						                        resultSet.getString("productcode"));
					}
					break;
				} catch (SQLException exp) {
					logger.error("Exception while retrieving service wallet information. [Reason: {}, Query: {}]", exp.getMessage(), preparedStatement);
				}
			} catch (SQLException exception) {
				logger.error("Exception while searching for service wallet information. [Reason: {}, MSISDN: {}, BNO: {}, WalletId: {}, ServiceId: {}, productcode: {}]", exception.getMessage(), msisdn, bno, walletId, serviceId, productcode);
			}
		}
		
		return serviceWallet;
	}
	
	public static Wallet getCommonWalletInformation(String msisdn, String bno, String walletId, String productcode) {
		return ApplicationProperties.getServerSpecificConfiguration().retrieveSubscriptionInfoUsingInMemDB ?
				   getWalletInformationUsingInMemDB(msisdn, bno, walletId, "na") :
					   getCommonWalletInformationUsingPersistentDB(msisdn, bno, walletId, productcode);
	}
	
	private static Wallet getCommonWalletInformationUsingPersistentDB(String msisdn, String bno, String walletId, String productcode) {
		Wallet commonWallet = null;
		
		String searchQuery = "SELECT id,bno,balance,IF(walletexpiry IS NULL, UNIX_TIMESTAMP('2037-12-31 23:59:59'), UNIX_TIMESTAMP(walletexpiry)) AS walletexpiry,freewallet," 
				           + "UNIX_TIMESTAMP(freewallet_enddate) AS freewallet_enddate, productcode "
	                       + "FROM wallet " 
				           + "WHERE uid=? "
				           + "AND walletid=? " 
				           + "AND serviceid=? "
				           + "AND productcode=? "
				           + "AND ? LIKE bno " 
				           + "AND (walletexpiry IS NULL OR walletexpiry >= NOW()) "
				           + (ApplicationProperties.usingOldWallet ?
				        		   "ORDER BY LENGTH(bno) DESC " :
				        		   "AND balance > 0 ORDER BY priority ASC, walletexpiry ASC, LENGTH(bno) DESC ")
				           + "LIMIT 0,1";
		
		for (int retryCounter = 0; 
				retryCounter < ApplicationProperties.getDatabaseConfiguration().getPersistentDBConf().maxRetry;
				retryCounter++) {
			try (Connection connection = DBConnectionFactory.getPersistentConnection();
					PreparedStatement preparedStatement = connection.prepareStatement(searchQuery)) {
				
				preparedStatement.setString(1, msisdn);
				preparedStatement.setString(2, walletId);
				preparedStatement.setString(3, "NA");
				preparedStatement.setString(4, productcode);
				preparedStatement.setString(5, bno);
	
				try (ResultSet resultSet = preparedStatement.executeQuery()) {
					if (resultSet.next()) {
						commonWallet = new Wallet(
								                resultSet.getString("id"), 
								                walletId,
								                resultSet.getString("bno"), 
								                resultSet.getLong("balance"),
								                Wallet.DEFAULT_PRODUCT_NAME,
								                resultSet.getLong("walletexpiry"),
								                resultSet.getBoolean("freewallet"), 
								                resultSet.getLong("freewallet_enddate"),
								                resultSet.getString("productcode"));
					}
					break;
				} catch (SQLException exception) {
					logger.error("Exception while retrieving common wallet information. [Reason: {}, Query: {}]", exception.getMessage(), preparedStatement);
				}
			} catch (SQLException exception) {
				logger.error("Exception while searching for common wallet information. [Reason: {}, MSISDN: {}, BNO: {}, WalletId: {}, productcode: {}]", exception.getMessage(), msisdn, bno, walletId, productcode);
			}
		}
			
		return commonWallet;
	}
	
	private static Wallet getWalletInformationUsingInMemDB(String msisdn, String bno, String walletId, String serviceId) {
		return ApplicationProperties.usingOldWallet ?
				getWalletInfoUsingOldWalletStruct(msisdn, bno, walletId, serviceId) :
					getWalletInfoUsingNewWalletStruct(msisdn, bno, walletId, serviceId);
	}
	
	private static Wallet getWalletInfoUsingNewWalletStruct(String msisdn, String bno, String walletId, String serviceId) {
		int RETRY_AMPLIFIER = 3;
		Wallet wallet = null;
		Set<String> productNames = null;
		
		for (int retry = 0; retry < (RETRY_AMPLIFIER * ApplicationProperties.getDatabaseConfiguration().getMemDBConf().maxRetry); retry++) {
			try (Jedis redis = DBConnectionFactory.getInMemoryConnection()) {
				if (productNames == null) {
				    productNames = redis.zrange(RedisKey.getKeyForMultipleWalletRetrieval(msisdn, serviceId, walletId, bno), 0, 0);
				    if (productNames.isEmpty()) return null;
				}
				
				if (wallet == null) {
					String productName = productNames.iterator().next();
				    String serializedWalletInfo = redis.get(RedisKey.getKeyForUniqueWalletRetrieval(msisdn, serviceId, walletId, bno, productName));
					wallet = Wallet.deserialize(walletId, bno, productName, serializedWalletInfo);
					if (wallet.getBalance() > 0 && !wallet.isWalletExpired()) return wallet;
				}
				
				if (redis.zrem(RedisKey.getKeyForMultipleWalletRetrieval(msisdn, serviceId, walletId, bno), wallet.getProductName()) > 0) {
					wallet = null;
					productNames = null;
				}
			} catch (Exception e) {}
		}
		
		return wallet;
	}
	
	private static Wallet getWalletInfoUsingOldWalletStruct(String msisdn, String bno, String walletId, String serviceId) {
		Wallet wallet = null;
		
		for (int retry = 0; retry < ApplicationProperties.getDatabaseConfiguration().getMemDBConf().maxRetry; retry++) {
			try (Jedis redis = DBConnectionFactory.getInMemoryConnection()) {
				String serializedWalletInfo = redis.get(RedisKey.getKeyForMultipleWalletRetrieval(msisdn, serviceId, walletId, bno));
				wallet = Wallet.deserialize(walletId, bno, Wallet.DEFAULT_PRODUCT_NAME, serializedWalletInfo);
				break;
			} catch (Exception e) {}
		}
		
		return wallet;
	}
	
	public static ServiceInfo getServiceInfo(String serviceId) {
		return DBConnectionFactory.getEmbededDataSource().getServiceInfo(serviceId);
	}
	
	public static boolean updateWalletInformation(String msisdn, String serviceId, Wallet updatedWallet, List<String> walletIds, List<Long> croppedBalances, String productcode) {
		return ApplicationProperties.getServerSpecificConfiguration().retrieveSubscriptionInfoUsingInMemDB ?
				updateWalletInformationUsingInMemDB(msisdn, serviceId, updatedWallet) : 
					updateWalletInformationUsingPersistentDB(walletIds, croppedBalances);
	}
	
	private static boolean updateWalletInformationUsingPersistentDB(List<String> walletIds, List<Long> croppedBalances) {
		if (walletIds == null || croppedBalances == null 
				|| walletIds.isEmpty() || (walletIds.size() != croppedBalances.size())) return false;
		
        String updateQuery = "UPDATE wallet SET balance=balance-? WHERE id=? AND (balance-?) >= 0";
		System.out.println(updateQuery);
        for (int retryCounter = 0; 
        		retryCounter < ApplicationProperties.getDatabaseConfiguration().getPersistentDBConf().maxRetry;
        		retryCounter++) {
			try (
				Connection connection = DBConnectionFactory.getPersistentConnection();
				PreparedStatement preparedStatement = connection.prepareStatement(updateQuery)) {
				for (int index = 0; index < walletIds.size(); index++) {
					preparedStatement.setLong(1, croppedBalances.get(index));
					preparedStatement.setString(2, walletIds.get(index));
					preparedStatement.setLong(3, croppedBalances.get(index));
					preparedStatement.addBatch();
				}

							
			    for (int ret : preparedStatement.executeBatch()) {
			    	if (ret >= 1) return true; // If at least one update query is successful, then it is being assumed as successful.
			    }
				System.out.println("wallet udpate query:"+preparedStatement);
			    return false;
			} catch (SQLException e) {
				logger.error("Exception while updating wallet table. [Reason: {}, Wallet id(s): {}]", e.getMessage(), String.join(",", walletIds));
			}
        }
		
		return false;
	}
	
	private static boolean updateWalletInformationUsingInMemDB(String msisdn, String serviceId, Wallet updatedWallet) {
		for (int retryCounter = 0; 
        		retryCounter < ApplicationProperties.getDatabaseConfiguration().getPersistentDBConf().maxRetry;
        		retryCounter++) {
			try (Jedis redis = DBConnectionFactory.getInMemoryConnection()) {
				Pipeline pipeline = redis.pipelined();
				if (!ApplicationProperties.usingOldWallet) {
					String key = RedisKey.getKeyForUniqueWalletRetrieval(msisdn, serviceId, updatedWallet.getWalletId(), updatedWallet.getBNO(), updatedWallet.getProductName()); 					         
					pipeline.set(key, updatedWallet.serialize());
				} else { 
				    String key = RedisKey.getKeyForMultipleWalletRetrieval(msisdn, serviceId, updatedWallet.getWalletId(), updatedWallet.getBNO());
				    pipeline.set(key, updatedWallet.serialize());
				}
				pipeline.lpush(ApplicationProperties.getSyncAppConfiguration().syncAppQueueName, getUpdateQuery(msisdn, serviceId, updatedWallet));
			    pipeline.sync();
				
			    return true;
			} catch (Exception e) {}
        }
		
		return false;
	}
	
	private static String getUpdateQuery(String msisdn, String serviceId, Wallet updatedWallet) {
		return ApplicationProperties.usingOldWallet ?
				String.format("UPDATE wallet SET balance=%d WHERE uid='%s' AND serviceid='%s' AND walletid='%s' AND bno='%s'", 
						updatedWallet.getBalance(), msisdn, serviceId, updatedWallet.getWalletId(), updatedWallet.getBNO()
						) :
				String.format("UPDATE wallet SET balance=%d WHERE uid='%s' AND serviceid='%s' AND product_name='%s' AND walletid='%s' AND bno='%s'",
		                updatedWallet.getBalance(), msisdn, serviceId, updatedWallet.getProductName(), updatedWallet.getWalletId(), updatedWallet.getBNO()
		               );
	}
	
	public static boolean tpsExceed(String applicationId, String timeSlotId, long tpsLimit) {
		if (tpsLimit <= 0) return false;
		
		long currentAbsSeconds = System.currentTimeMillis() / 1000;
		String key = "ctps." + applicationId + "." + timeSlotId + "." + currentAbsSeconds;
		
		try (Jedis redis = DBConnectionFactory.getInMemoryConnection()) {
			String currentTPS = redis.get(key);
	
			if (currentTPS != null && Integer.valueOf(currentTPS) > tpsLimit) {
				return false;
			} else {
				Transaction t = redis.multi();
				t.incr(key);
				t.expire(key, 10);
				t.exec();
				return true;
			}
		} catch (Exception e) {/* Silently skip it. As TPS check is not critical operation. */}
		
		return true;
	}
	
	public static void checkDuplicateCDRId(ProcedureParams params) {
		try (Jedis redis = DBConnectionFactory.getInMemoryConnection()) {
			String serializedResponse = redis.get("dcdr." + params.getRequest().getCDRId());
			if (serializedResponse != null) {
				params.setFlagIfErrorOccured(ResultCode.ERROR_DUPLICATE_CDR_DETECT);
				params.setPrevSerializedResponse(serializedResponse);
			}
		} catch (Exception e) { /* As per previous CGW, it will silently ignore the exception */ }
	}
	
	public static void storeCDRId(ProcedureParams params) {
		try (Jedis redis = DBConnectionFactory.getInMemoryConnection()) {
			Transaction t = redis.multi();
			t.set("dcdr." + params.getRequest().getCDRId(), Response.serialize(params));
			t.expire("dcdr." + params.getRequest().getCDRId(), 86400); // Please use this expiration time as property value.
			t.exec();
		} catch (Exception e) { /* As per previous CGW, it will silently ignore the exception */ }
	}
	
	public static List<TimeSlotInfo> getTimeSlotInformation() {
		EmbededDB embededDB = DBConnectionFactory.getEmbededDataSource();
		return embededDB.getCurrentTimeSlotInformation();
	}
	
	public static String getPayerMSISDN(String msisdn, String serviceId) {
		String searchQuery = "SELECT payer FROM servicepayer "
				           + "WHERE uid=? AND serviceid=? AND paymentenddate>=NOW() "
				           + "ORDER BY servicepayer.id DESC "
				           + "LIMIT 1";
		String payerMsisdn = null;
		try (Connection connection = DBConnectionFactory.getPersistentConnection();
				PreparedStatement statement = connection.prepareStatement(searchQuery)) {
			statement.setString(1, msisdn);
			statement.setString(2, serviceId);
			ResultSet resultSet = statement.executeQuery();
			payerMsisdn = resultSet.getString("payer");
			resultSet.close();
		} catch (SQLException e) {
			logger.error("Exception while searching for service payer for MSISDN [{}], Service Id [{}]", msisdn, serviceId, e);
		}
		
		return (payerMsisdn != null) ? payerMsisdn : msisdn;
	}
	
	public static boolean isAllColumnExists(String tableName, List<String> columnNames) {
		return columnNames.stream().allMatch(col -> DBClient.isColumnExists(tableName, col));
	}
	
	public static boolean isColumnExists(String tableName, String columnName) {
		for (int retryCounter = 0; 
        		retryCounter < ApplicationProperties.getDatabaseConfiguration().getPersistentDBConf().maxRetry;
        		retryCounter++) {
			try (Connection connection = DBConnectionFactory.getPersistentConnection()) {
			    
				DatabaseMetaData  metaData = connection.getMetaData();
				try (ResultSet resultSet = metaData.getColumns(null, null, tableName, columnName)) {
					return resultSet.next();
				}
			} catch (SQLException e) {
				logger.error("Exception while searching for column [{}] in table [{}].", columnName, tableName, e);
			}
        }
		
		return false;
	}
	
	public static void saveCDRData(ProcedureParams params) {
		boolean isSuccess = !params.errorOccured() && params.getBillingResponse().isSuccessCode();
		
		int streamIndex = ApplicationProperties.getCDRConfiguration().getCdrStreamIndex(isSuccess);
		switch (streamIndex) {
			case Constant.CDR_STREAM_DB:
				saveCDRDataIntoDB(params);
				break;
			case Constant.CDR_STREAM_FILE:
				saveCDRDataIntoFile(params);
				break;
			case Constant.CDR_STREAM_IN_MEMORY_DB:
				saveCDRDataIntoInMemory(params);
				break;
			default:
				saveCDRDataIntoFile(params);
				break;
		}
	}
	
	private static void saveCDRDataIntoInMemory(ProcedureParams params) {
		Date currentTime = new Date();
		Request request = params.getRequest();
		
		String chargingType = (params.getMappedChargedType() != null) ? params.getMappedChargedType() : request.getChargingType();  
		String numericStatus = (params.getSubscriberService() != null) ? params.getSubscriberService().getNumericSubscriptionStatus() : "0";
		String msisdn = params.getMSISDN() != null ? params.getMSISDN() : request.getANO();
		String bno = params.getRealBNO() != null ? params.getRealBNO() : request.getBNO();
		String productcode = params.getRequest().getproductcode();
		String resultCode = params.errorOccured() ? 
				                params.getError().getAlphaNumericCode() : params.getBillingResponse().getResultCode();
	    
		PackageInfo pi = params.getPackageInformation();
		String packageId = "";
		long subscriberBalance = 0;
		if (pi != null) { // In case of TPS exceed and authentication failed.
			packageId = pi.getPackageId();
			subscriberBalance = pi.getSubscriberBalance();
		}
		
		long totalConsumedUnits = 0L;
		long totalChargedAmount = 0L;
		Optional<String> formattedSessionStartTime = Optional.empty();
		if (params.getSessionInfo().isPresent()) {
			SessionInfo si = params.getSessionInfo().get();
			totalConsumedUnits = si.getTotalConsumedUnit() + request.getConsumedUnitSinceLastRequest();
			totalChargedAmount = si.getTotalChargedAmount();
			formattedSessionStartTime = Optional.of(si.getSessionStartTime());
		} else if (params.getRatingInfo() != null) {
			totalChargedAmount = params.getRatingInfo().getAmount();
		}
		
		TransactionLog.Builder builder = new TransactionLog.Builder(
				request.getCDRId(), 
				request.getServiceId(), 
				chargingType, 
				ApplicationProperties.getServerSpecificConfiguration().cgwId, 
				msisdn, 
				bno, 
				request.getDirection(), 
                resultCode, 
                request.getApplicationId()
		);
		
        builder.setRateId(params.getRateId());
        builder.setAmount(totalChargedAmount);
        if (formattedSessionStartTime.isPresent()) {
        	builder.setStartTime(formattedSessionStartTime.get());
        } else {
        	builder.setStartTime(currentTime);
        }
        
        builder.setEndTime(currentTime);
        builder.setPackageId(packageId);
        builder.setBillingNode((int) params.getBillingNodeId());
        builder.setCDRWriteTime(currentTime);
        builder.setCDRFilePath(ApplicationProperties.getCDRConfiguration().cdrDirectory);
        builder.setCDRWriteStatus("QUE");
        builder.setCDRIdFile("0000");
        builder.setSerialNo("0000");
        builder.setUserId(ApplicationProperties.getServerSpecificConfiguration().userId);
        builder.setLastUpdate(currentTime);
        builder.setSubscriptionStatus(numericStatus);
        builder.setRefundAmount("\\N");
        builder.setTotalConsumedUnit(totalConsumedUnits);
        builder.setDevice("\\N"); 
        builder.setBalanceBefore(subscriberBalance);
        builder.setProductcode(productcode);
        
        String serializedCdrData = builder.build().serialize();
        
        for (int retryCounter = 0;
        		retryCounter < ApplicationProperties.getDatabaseConfiguration().getMemDBConf().maxRetry;
        		retryCounter++) {
	        try (Jedis redis = DBConnectionFactory.getInMemoryConnection()) {
	        	long currentEpochSecond = DBConnectionFactory.getEmbededDataSource().getEpochCounter();
	        	redis.rpush(RedisKey.getKeyForCdrRetrieval(currentEpochSecond), serializedCdrData);
	        	break;
	        } catch (Exception e) {
	        	logger.error("Exception while saving CDR Id [{}] into in memory. Retry counter [{}]", request.getCDRId(), retryCounter, e);
	        }
        }
	}
	
	private static void saveCDRDataIntoDB(ProcedureParams params) {
		DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Calendar calendar = Calendar.getInstance(); // Locale should be set and it should be global.
		
		Date date = new Date();
		calendar.setTime(date);
		String currentDate = dateFormatter.format(date);
		
		String chargingType = (params.getMappedChargedType() != null) ? params.getMappedChargedType() : params.getRequest().getChargingType();  
		String numericStatus = (params.getSubscriberService() != null) ? params.getSubscriberService().getNumericSubscriptionStatus() : "0";
		
		PackageInfo pi = params.getPackageInformation();
		String packageId = "";
		long subscriberBalance = 0;
		if (pi != null) { // In case of TPS exceed and authentication failed.
			packageId = pi.getPackageId();
			subscriberBalance = pi.getSubscriberBalance();
		}
		
		long totalConsumedUnits = 0L;
		long totalChargedAmount = 0L;
		String sessionStartTime = currentDate;
		if (params.getSessionInfo().isPresent()) {
			SessionInfo si = params.getSessionInfo().get();
			totalConsumedUnits = si.getTotalConsumedUnit() + params.getRequest().getConsumedUnitSinceLastRequest();
			totalChargedAmount = si.getTotalChargedAmount();
			sessionStartTime = si.getSessionStartTime();
		} else if (params.getRatingInfo() != null) {
			totalChargedAmount = params.getRatingInfo().getAmount();
		}
		
		String insertQuery = "INSERT INTO cdrintegrated(CDRID,ServiceID,ChargingType,CGWID,Ano,Bno,Direction,RateID,ResultCode,Amount,StartTime,EndTime,Purpose,PackageID,BillingNode,UserID,LastUpdate,SubscriptionStatus,TotalConsumedUnit,BalanceBefore, productcode) " +
				             "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?);";
		
		for (int retryCounter = 0; 
				retryCounter < ApplicationProperties.getDatabaseConfiguration().getPersistentDBConf().maxRetry;
				retryCounter++) {
			try (
				Connection connection = DBConnectionFactory.getPersistentConnection();
				PreparedStatement preparedStatement = connection.prepareStatement(insertQuery)) {
			
				preparedStatement.setString(1, params.getRequest().getCDRId());
				preparedStatement.setString(2, params.getRequest().getServiceId());
				preparedStatement.setString(3, chargingType);
				preparedStatement.setString(4, ApplicationProperties.getServerSpecificConfiguration().cgwId);
				preparedStatement.setString(5, (params.getMSISDN() != null ? params.getMSISDN() : params.getRequest().getANO()));
				preparedStatement.setString(6, (params.getRealBNO() != null ? params.getRealBNO() : params.getRequest().getBNO()));
				preparedStatement.setInt(7, params.getRequest().getDirection());
				preparedStatement.setString(8, params.getRateId());
				preparedStatement.setString(9, (params.errorOccured() ? 
		                    		            params.getError().getAlphaNumericCode() : params.getBillingResponse().getResultCode()));
				preparedStatement.setLong(10, totalChargedAmount);
				preparedStatement.setString(11, sessionStartTime);
			    preparedStatement.setString(12, currentDate);
			    preparedStatement.setString(13,  params.getRequest().getApplicationId());
			    preparedStatement.setString(14, packageId);
			    preparedStatement.setString(15, String.valueOf(params.getBillingNodeId()));
			    preparedStatement.setString(16, ApplicationProperties.getServerSpecificConfiguration().userId);
			    preparedStatement.setString(17, currentDate);
			    preparedStatement.setString(18, numericStatus);
			    preparedStatement.setLong(19, totalConsumedUnits);
			    preparedStatement.setLong(20, subscriberBalance);
			    preparedStatement.setString(21, params.getRequest().getproductcode());
			    
			    preparedStatement.execute();
			    return;
			} catch (SQLException e) {}
		}
		
		logger.error("Exception while saving CDR Id [{}] into DB.", params.getRequest().getCDRId());
		params.setFlagIfErrorOccured(ResultCode.ERROR_DB_Query_Failed);
	}
	
	private static void saveCDRDataIntoFile(ProcedureParams params) {
		String cdrFileFormat = "%s/CDR%s%04d%02d%02d%02d%02d%02d.txt";
		
		DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Calendar calendar = Calendar.getInstance(); // Locale should be set and it should be global.
		
		Date date = new Date();
		calendar.setTime(date);
		String currentDate = dateFormatter.format(date);
		
		String chargingType = (params.getMappedChargedType() == null) ? params.getRequest().getChargingType() : params.getMappedChargedType(); 
		String numericStatus = (params.getSubscriberService() != null) ? params.getSubscriberService().getNumericSubscriptionStatus() : "0";
		
		PackageInfo pi = params.getPackageInformation();
		String packageId = "";
		long subscriberBalance = 0;
		if (pi != null) { // In case of TPS exceed and authentication failed.
			packageId = pi.getPackageId();
			subscriberBalance = pi.getSubscriberBalance();
		}
		
		long totalConsumedUnits = 0L;
		long totalChargedAmount = 0L;
		String sessionStartTime = currentDate;
		if (params.getSessionInfo().isPresent()) {
			SessionInfo si = params.getSessionInfo().get();
			totalConsumedUnits = si.getTotalConsumedUnit() + params.getRequest().getConsumedUnitSinceLastRequest();
			totalChargedAmount = si.getTotalChargedAmount();
			sessionStartTime = si.getSessionStartTime();
		} else if (params.getRatingInfo() != null) {
			totalChargedAmount = params.getRatingInfo().getAmount();
		}
			
		String cdrData = params.getRequest().getCDRId() + "|" +
	                     params.getRequest().getServiceId() + "|" +
	                     chargingType + "|" +
	                     ApplicationProperties.getServerSpecificConfiguration().cgwId + "|" +
	                     (params.getMSISDN() != null ? params.getMSISDN() : params.getRequest().getANO()) + "|" +
				         (params.getRealBNO() != null ? params.getRealBNO() : params.getRequest().getBNO()) + "|" +
	                     params.getRequest().getDirection() + "|" +
				         params.getRateId() + "|" + 
	                     (params.errorOccured() ? 
	                    		 params.getError().getAlphaNumericCode() : params.getBillingResponse().getResultCode())  + "|" +
				         String.valueOf(totalChargedAmount) + "|" +
	                     sessionStartTime + "|" +
	                     currentDate + "|" +
	                     params.getRequest().getApplicationId() + "|" +
				         packageId + "|" +
	                     String.valueOf(params.getBillingNodeId()) + "|" + 
				         currentDate + "|" + 
	                     ApplicationProperties.getCDRConfiguration().cdrDirectory + "|" +
	                     "QUE|0000|0000|" +
				         ApplicationProperties.getServerSpecificConfiguration().userId + "|" +
				         currentDate + "|" + 
				         numericStatus + "|" + "\\N|" +
				         String.valueOf(totalConsumedUnits) + "|" + "\\N|" +
				         String.valueOf(subscriberBalance) + "|" +
						 params.getRequest().getproductcode();

	    
		String cdrFileName = String.format(cdrFileFormat,
				                               ApplicationProperties.getCDRConfiguration().cdrDirectory,
				                               ApplicationProperties.getServerSpecificConfiguration().cgwId,
				                               calendar.get(Calendar.YEAR),
				                               calendar.get(Calendar.MONTH) + 1,
				                               calendar.get(Calendar.DAY_OF_MONTH),
				                               calendar.get(Calendar.HOUR_OF_DAY),
				                               calendar.get(Calendar.MINUTE),
				                               calendar.get(Calendar.SECOND) / ApplicationProperties.getCDRConfiguration().cdrFileCreationInterval);
		Utility.writeToFile(cdrFileName, cdrData, ApplicationProperties.getCDRConfiguration().lineSeparator);
	}
	
	public static void deleteKeyUsingPattern(int numberOfKeys, String pattern) {
		String script = "for i, name in ipairs(redis.call('KEYS', ARGV[1])) do redis.call('DEL', name); end";
		
		for (int retryCounter = 0;
				retryCounter < ApplicationProperties.getDatabaseConfiguration().getMemDBConf().maxRetry;
				retryCounter++) {
			try (Jedis redis = DBConnectionFactory.getInMemoryConnection()) {
				redis.eval(script, numberOfKeys, pattern);
				break;
			} catch (Exception e) {
				logger.error("Exception while deleting keys using pattern [{}]", pattern, e);
			}
		}
	}
	
	public static Tuple<Boolean, Optional<String>> getCurrentPackageTablePrefix() {
		for (int retryCounter = 0;
				retryCounter < ApplicationProperties.getDatabaseConfiguration().getMemDBConf().maxRetry;
				retryCounter++) {
			try (Jedis redis = DBConnectionFactory.getInMemoryConnection()) {
				return new Tuple<>(true, Optional.ofNullable(redis.get("pkg_tbl_prefix")));
			} catch (Exception e) {
				logger.error("Exception while getting current package table prefix. Retry counter [{}].", retryCounter, e);
			}
		}
		
		return new Tuple<>(false, Optional.empty());
	}
	
	public static boolean setCurrentPackageTablePrefix(String currentPrefix) {
		for (int retryCounter = 0;
				retryCounter < ApplicationProperties.getDatabaseConfiguration().getMemDBConf().maxRetry;
				retryCounter++) {
			try (Jedis redis = DBConnectionFactory.getInMemoryConnection()) {
				redis.set("pkg_tbl_prefix", currentPrefix);
				return true;
			} catch (Exception e) {
				logger.error("Exception while setting package table prefix [{}]. Retry counter [{}].", currentPrefix, retryCounter, e);
			}
		}
		
		return false;
	}

	public static PriceLimit getUserPriceLimitInfo(String userId) {
		String searchQuery = "SELECT id, uid, curr_month_index, last_yearly_reset_date, curr_monthly_limit, curr_yearly_limit FROM price_limit WHERE uid = ?";

		for (int retryCounter = 0;
			 retryCounter < ApplicationProperties.getDatabaseConfiguration().getPersistentDBConf().maxRetry;
			 retryCounter++) {
			try (Connection connection = DBConnectionFactory.getPersistentConnection();
				 PreparedStatement statement = connection.prepareStatement(searchQuery)) {
				statement.setString(1, userId);
				ResultSet resultSet = statement.executeQuery();
				if (resultSet.next()) {
					long id = resultSet.getLong("id");
					String uid = resultSet.getString("uid");
					int monthIndex = resultSet.getInt("curr_month_index");
					String lastYearlyResetDate = resultSet.getString("last_yearly_reset_date");
					long monthlyLimit = resultSet.getLong("curr_monthly_limit");
					long yearlyLimit = resultSet.getLong("curr_yearly_limit");

					resultSet.close();

					return new PriceLimit(
							id,
							uid,
							monthIndex,
							LocalDateTime.parse(lastYearlyResetDate, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S")),
							monthlyLimit,
							yearlyLimit
					);
				}
				resultSet.close();
				return null;
			} catch (SQLException e) {
				logger.error("Exception while searching for price-limit for UID [{}]", userId, e);
			}
		}

		return null;
	}

	public static long createUserPriceLimitInfo(PriceLimit priceLimit) throws Exception {
		String createQuery = "INSERT INTO price_limit(uid, curr_month_index, last_yearly_reset_date, curr_monthly_limit, curr_yearly_limit) VALUES(?, ?, NOW(), ?, ?)";

		for (int retryCounter = 0;
			 retryCounter < ApplicationProperties.getDatabaseConfiguration().getPersistentDBConf().maxRetry;
			 retryCounter++) {
			try (Connection connection = DBConnectionFactory.getPersistentConnection();
				 PreparedStatement statement = connection.prepareStatement(createQuery, Statement.RETURN_GENERATED_KEYS)) {
				statement.setString(1, priceLimit.getUid());
				statement.setInt(2, priceLimit.getMonthIndex());
				statement.setLong(3, priceLimit.getMonthlyLimit());
				statement.setLong(4, priceLimit.getYearlyLimit());

				int affectedRows = statement.executeUpdate();
				if (affectedRows == 0) break;

				try(ResultSet generatedKeys = statement.getGeneratedKeys()) {
					if (generatedKeys.next()) return generatedKeys.getLong(1);
				}
			} catch (SQLException e) {
				logger.error("Exception while creating price-limit record for UID [{}]", priceLimit.getUid(), e);
			}
		}

		throw new Exception("System can not create price-limit record for UID: " + priceLimit.getUid());
	}

	public static void resetUserPriceLimitInfo(PriceLimit priceLimit) {
		String resetQuery = "UPDATE price_limit SET curr_month_index = ?, last_yearly_reset_date = ?, curr_monthly_limit = ?, curr_yearly_limit = ? WHERE id = ?";

		for (int retryCounter = 0;
			 retryCounter < ApplicationProperties.getDatabaseConfiguration().getPersistentDBConf().maxRetry;
			 retryCounter++) {
			try (Connection connection = DBConnectionFactory.getPersistentConnection();
				 PreparedStatement statement = connection.prepareStatement(resetQuery)) {
				statement.setInt(1, priceLimit.getMonthIndex());
				statement.setDate(2, java.sql.Date.valueOf(priceLimit.getLastYearlyResetDate().toLocalDate()));
				statement.setLong(3, priceLimit.getMonthlyLimit());
				statement.setLong(4, priceLimit.getYearlyLimit());
				statement.setLong(5, priceLimit.getId());
				statement.executeUpdate();
			} catch (SQLException e) {
				logger.error("Exception while resetting price-limit record for UID [{}]", priceLimit.getUid(), e);
			}
		}
	}

	public static boolean updateUserMonthlyPriceLimit(String userId, long amount) {
		String updateLimitQuery =
				"UPDATE price_limit SET curr_monthly_limit = curr_monthly_limit - ? WHERE uid = ? AND (curr_monthly_limit - ?) >= 0";

		for (int retryCounter = 0;
			 retryCounter < ApplicationProperties.getDatabaseConfiguration().getPersistentDBConf().maxRetry;
			 retryCounter++) {
			try (Connection connection = DBConnectionFactory.getPersistentConnection();
				 PreparedStatement statement = connection.prepareStatement(updateLimitQuery)) {
				statement.setLong(1, amount);
				statement.setString(2, userId);
				statement.setLong(3, amount);
				int affectedRows = statement.executeUpdate();
				return affectedRows > 0;
			} catch (SQLException e) {
				logger.error("Exception while updating price-limit record for UID [{}]", userId, e);
			}
		}

		return false;
	}
}
