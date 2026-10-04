package com.ssdtech.java.db.management;

import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ssdtech.java.db.client.DBClient;
import com.ssdtech.java.model.ProcedureParams;
import com.ssdtech.java.model.RateMaster;
import com.ssdtech.java.model.RatePulse;
import com.ssdtech.java.model.ServiceInfo;
import com.ssdtech.java.model.SubscriptionGroup;
import com.ssdtech.java.model.TimeSlotInfo;
import com.ssdtech.java.model.applicationproperties.ApplicationProperties;
import com.ssdtech.java.utils.Tuple;
import com.ssdtech.java.utils.Utility;

import redis.clients.jedis.Jedis;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

public class EmbededDB {
    private static final Logger logger = LogManager.getLogger(EmbededDB.class.getName());
	
	private volatile List<TimeSlotInfo> timeSlotIds;
	private volatile List<ArrayList<ArrayList<Integer>>> timeSlotInformation;
	private volatile List<TimeSlotInfo> currentTimeSlotInfo;

	private volatile Map<String, SubscriptionGroup> subscriptionGroupInformation;
	private volatile List<String> shortCodes;
	private volatile Map<String, Set<String>> subscriptionGroupsWRTServiceId;
	private volatile Map<String, Tuple<String, Boolean>> rootSubscriptionInformation;

	private volatile Map<String, ServiceInfo> serviceInformation;

	private volatile RateContext rateContext;
	private volatile Map<String, List<RatePulse>> ratingInfo;
	private volatile Map<String, String> subCycleWRTsubgroupID;
	
	private volatile Timer timer;
	
	private volatile AtomicLong epochSeconds;

	public void initialize() {
        this.loadTimeSlotInformation();
        this.loadSubscriptionGroupInformation();
        this.loadRateContextInformation();
        this.loadRatingInfo();
        this.startEpochCounter();
	}

	public boolean loadTimeSlotInformation() {
		Tuple<List<TimeSlotInfo>,List<ArrayList<ArrayList<Integer>>>> t = this.getTimeSlotInformation();
		if (t.first == null) return false;
		
		this.timeSlotIds = t.first;
		this.timeSlotInformation = t.second;
		this.setCurrentTimeSlotInformation();
		
		if (this.timer != null) {
			this.timer.cancel();
		}
		
		this.timer = new Timer();
		this.timer.schedule(new ScheduledTask(), 0, 60000);
		
		return true;
	}
	
	private Tuple<List<TimeSlotInfo>,List<ArrayList<ArrayList<Integer>>>> getTimeSlotInformation() {
		List<TimeSlotInfo> timeSlotIds = new ArrayList<>();
		List<ArrayList<ArrayList<Integer>>> timeSlotInformation = new ArrayList<>(7);
		for (int i = 0; i < 7; i++) {
			timeSlotInformation.add(new ArrayList<>(1440));
		    for (int j = 0; j < 1440; j++) {
			    timeSlotInformation.get(i).add(new ArrayList<>());
		    }
		}

		String searchQuery = "SELECT LOWER(TimeSlotID) AS TimeSlotID, StartDay, EndDay, StartTime, EndTime FROM timeslot";

		try (Connection connection = DBConnectionFactory.getPersistentConnection();
				Statement statement = connection.createStatement();
				ResultSet resultSet = statement.executeQuery(searchQuery)) {
			int timeSlotIdIndex = 0;
			while (resultSet.next()) {
				String timeSlotId = resultSet.getString("TimeSlotID");

				// Adding 'anytime' if timeSlotId is NULL.
				timeSlotId = (timeSlotId == null) ? "anytime" : timeSlotId;
				
				int startDay = resultSet.getInt("StartDay");
				int endDay = resultSet.getInt("EndDay") + 1;
				int sTime = resultSet.getInt("StartTime");
				int eTime = resultSet.getInt("EndTime");

				int weight = (endDay - startDay) + (eTime - sTime);
				timeSlotIds.add(timeSlotIdIndex, new TimeSlotInfo(timeSlotId, weight));

				for (int day = startDay; day < endDay; day++) {
					for (int minute = sTime; minute < eTime; minute++) {
						timeSlotInformation.get(day).get(minute).add(timeSlotIdIndex);
					}
				}

				timeSlotIdIndex = timeSlotIdIndex + 1;
			}
			return new Tuple<>(timeSlotIds, timeSlotInformation);
		} catch (SQLException e) {
			logger.error("Exception while loading table [timeslot].", e);
		}
		
		return new Tuple<>(null, null);
	}
	
	public List<TimeSlotInfo> getCurrentTimeSlotInformation() {
		return this.currentTimeSlotInfo;
	}
	
	private void setCurrentTimeSlotInformation() {
		LocalDate date = LocalDate.now();
	    LocalTime time = LocalTime.now();
	    int dayOfWeek = date.getDayOfWeek().getValue() - 1;
		int minutesPassed = time.getHour() * 60 + time.getMinute();
		
		List<TimeSlotInfo> timeSlotInfo = new ArrayList<>();
		List<Integer> timeSlotIds = this.timeSlotInformation.get(dayOfWeek).get(minutesPassed);
		for (Integer id : timeSlotIds) {
			timeSlotInfo.add(this.timeSlotIds.get(id));
		}
		
		this.currentTimeSlotInfo = timeSlotInfo;
	}

	public boolean loadSubscriptionGroupInformation() {
		Map<String, SubscriptionGroup> tSubscriptionGroupInformation = new HashMap<>();
		Map<String, Set<String>> tSubscriptionGroupsWRTServiceId = new HashMap<>();
		Map<String, Tuple<String, Boolean>> tRootSubscriptionInformation = new HashMap<>();
		Map<String, ServiceInfo> serviceInformation = new HashMap<>();
		List<String> tShortCodes = new ArrayList<>();
		Map<String, String> tsubCycleWRTsubgroupID = new HashMap<>();

		String searchQuery = "SELECT LOWER(SubscriptionGroupID) AS SubscriptionGroupID, LOWER(ParentID) AS ParentID, LOWER(ServiceID) AS ServiceID, "
				+ "BNI, CASE WHEN has_balance_option = '1' THEN '1' ELSE '0' END AS has_balance_option, subscriptionCycle "
				+ "FROM subscriptiongroup ORDER BY LENGTH(BNI) DESC, BNI";

		try (Connection connection = DBConnectionFactory.getPersistentConnection();
				Statement statement = connection.createStatement();
				ResultSet resultSet = statement.executeQuery(searchQuery)) {
			while (resultSet.next()) {
				String subscriptionGroupId = resultSet.getString("SubscriptionGroupID");
				String parentId = resultSet.getString("ParentID");
				String serviceId = resultSet.getString("ServiceID");
				String bni = resultSet.getString("BNI");
				boolean hasBalanceOption = resultSet.getBoolean("has_balance_option");
				String subsCycle = resultSet.getString("subscriptionCycle");
                
				// Adding default value in case selected fields return NULL.
				subscriptionGroupId = (subscriptionGroupId == null) ? "na" : subscriptionGroupId;
				parentId = (parentId == null) ? "na" : parentId;
				serviceId = (serviceId == null) ? "na" : serviceId;
				
				if (parentId.equals("root")) {
					tRootSubscriptionInformation.put(serviceId, new Tuple<>(subscriptionGroupId, hasBalanceOption));  //checkchange
					serviceInformation.put(serviceId, new ServiceInfo());
				}


				if(!tsubCycleWRTsubgroupID.containsKey(subscriptionGroupId))
					tsubCycleWRTsubgroupID.put(subscriptionGroupId, subsCycle);

				if (tSubscriptionGroupsWRTServiceId.containsKey(serviceId)) {
					tSubscriptionGroupsWRTServiceId.get(serviceId).add(subscriptionGroupId);
				} else {
					Set<String> subscriptionGroups = new HashSet<>();
					subscriptionGroups.add(subscriptionGroupId);
					tSubscriptionGroupsWRTServiceId.put(serviceId, subscriptionGroups);
				}

				SubscriptionGroup sg = new SubscriptionGroup(subscriptionGroupId, parentId, serviceId, hasBalanceOption);
				tSubscriptionGroupInformation.put(subscriptionGroupId, sg);
				if (bni != null && !tShortCodes.contains(bni)) {
					tShortCodes.add(bni);
				}
			}

			this.subscriptionGroupInformation = tSubscriptionGroupInformation;
			this.shortCodes = tShortCodes;
			this.subscriptionGroupsWRTServiceId = tSubscriptionGroupsWRTServiceId;
			this.rootSubscriptionInformation = tRootSubscriptionInformation;
			this.subCycleWRTsubgroupID = tsubCycleWRTsubgroupID;
			
			this.loadAggregatedServiceInformation(serviceInformation);
			this.serviceInformation = serviceInformation;
			//System.out.println("after insertion:"+subCycleWRTsubgroupID.get());
			
			return true;
		} catch (SQLException e) {
			logger.error("Exception while loading table [subscriptiongroup].", e);
		}
        
		return false;
	}
	
	private void loadAggregatedServiceInformation(Map<String, ServiceInfo> serviceInfo) {
		this.loadServiceInformation(serviceInfo);
		this.loadRegionUrlInformation(serviceInfo);
		this.setIfContentGiftingEnabled(serviceInfo);
	}

	private void loadServiceInformation(Map<String, ServiceInfo> serviceInformation) {
		// [TODO] serviceInformation initialization and loading should not be scattered.
		
		String searchQuery = "SELECT LOWER(serviceinfo.serviceid) AS serviceid,"
				+ "CASE serviceinfo.hasServiceWallet WHEN 'yes' THEN '1' WHEN 'no' THEN '0' ELSE '0' END hasServiceWallet, "
				+ "CASE serviceinfo.hasCommonWallet WHEN  'yes' THEN '1' WHEN 'no' THEN '0' ELSE '0' END AS hasCommonWallet, "
				+ "walletinfo.walletid, LOWER(walletinfo.walletname) AS walletname " 
				+ "FROM serviceinfo " 
				+ "JOIN walletinfo "
				+ "ON serviceinfo.walletid = walletinfo.walletid "
				+ "WHERE walletinfo.walletname <> 'credit'";

		try (Connection connection = DBConnectionFactory.getPersistentConnection();
				Statement statement = connection.createStatement();
				ResultSet resultSet = statement.executeQuery(searchQuery)) {
			while (resultSet.next()) {
				String serviceId = resultSet.getString("serviceid");
				boolean hasServiceWallet = resultSet.getBoolean("hasServiceWallet");
				boolean hasCommonWallet = resultSet.getBoolean("hasCommonWallet");
				String walletId = resultSet.getString("walletid");
				String walletName = resultSet.getString("walletname");

				// Adding default value in case selected fields return NULL.
				serviceId = (serviceId == null) ? "na" : serviceId;
				walletId = (walletId == null) ? "-1" : walletId;
				walletName = (walletName == null) ? "na" : walletName;
				
				ServiceInfo serviceInfo = serviceInformation.getOrDefault(serviceId, new ServiceInfo());
				serviceInfo.setWalletConfiguration(hasServiceWallet, hasCommonWallet, walletId, walletName);
				serviceInformation.put(serviceId, serviceInfo);
			}
		} catch (SQLException e) {
			logger.error("Exception while loading table [serviceinfo].", e);
		}
	}
	
	private void loadRegionUrlInformation(Map<String, ServiceInfo> serviceInformation) {
		String searchQuery = "SELECT LOWER(serviceid) AS serviceid, hostname, PORT, url FROM regionurl";

		try (Connection connection = DBConnectionFactory.getPersistentConnection();
				Statement statement = connection.createStatement();
				ResultSet resultSet = statement.executeQuery(searchQuery)) {
			while (resultSet.next()) {
				String serviceId = resultSet.getString("serviceid");
				String host = resultSet.getString("hostname");
				int port = resultSet.getInt("port");
				String pathSegment = resultSet.getString("url");
				
				serviceId = (serviceId == null) ? "na" : serviceId;

				ServiceInfo si = serviceInformation.getOrDefault(serviceId, new ServiceInfo());
				si.setRegionConfiguration(host, port, pathSegment);
				serviceInformation.put(serviceId, si);
			}
		} catch (SQLException e) {
			logger.error("Exception while loading table [regionurl].", e);
		}
	}
	
	private void setIfContentGiftingEnabled(Map<String, ServiceInfo> serviceInformation) {
		String searchQuery = "SELECT DISTINCT LOWER(serviceid) AS serviceid FROM servicepayer";

		try (Connection connection = DBConnectionFactory.getPersistentConnection();
				Statement statement = connection.createStatement();
				ResultSet resultSet = statement.executeQuery(searchQuery)) {
			while (resultSet.next()) {
				String serviceId = resultSet.getString("serviceid");
				serviceId = (serviceId == null) ? "na" : serviceId;
				
				ServiceInfo si = serviceInformation.getOrDefault(serviceId, new ServiceInfo());
				si.enableContentGifting();
				serviceInformation.put(serviceId, si);
			}
		} catch (SQLException e) {
			logger.error("Exception while loading table [servicepayer].", e);
		}
	}

	public boolean loadRateContextInformation() {
		boolean billingNodeColumnExists = DBClient.isColumnExists("ratemaster", "BillingNodeId");
		
		String searchQuery = "SELECT LOWER(ServiceID) AS ServiceID, REPLACE(LOWER(ChargingType),'%','*') AS ChargingType, LOWER(TimeSlotID) AS TimeSlotID, LOWER(CallTypeID) AS CallTypeID, LOWER(SubscriptionGroupID) AS SubscriptionGroupID, "
							+ "CASE SubscriptionStatus " 
							    + "WHEN 'Registered' THEN 2 " 
							    + "WHEN 'Deregistered' THEN 1 "
							    + "WHEN 'RegisteredNoBalance' THEN 3 " 
							    + "WHEN 'NotRegisteredBalance' THEN 4 "
							    + "WHEN 'InGracePeriod' THEN 5 " 
							    + "WHEN 'NA' THEN 0 " 
							    + "ELSE 0 "
							+ "END AS SubscriptionStatus, Priority, LOWER(PackageID) AS PackageID, RateID, LOWER(region) AS region, LOWER(channel) AS channel, "
							+ "LOWER(RegisteredService) AS RegisteredService, HasCampaignRate, "
							+ (billingNodeColumnExists ? "BillingNodeId " : "-1 AS BillingNodeId ")
							+ "FROM ratemaster "
							+ "WHERE ActivationStart <= NOW() AND ActivationEnd >= NOW()";

		List<String> possibleSubscriptionStatus = new ArrayList<>();
		possibleSubscriptionStatus.add("0");
		possibleSubscriptionStatus.add("1");
		possibleSubscriptionStatus.add("2");
		possibleSubscriptionStatus.add("3");
		possibleSubscriptionStatus.add("4");
		possibleSubscriptionStatus.add("5");

		List<String> possibleChargingType = new ArrayList<>();
		possibleChargingType.add("session_start");
		possibleChargingType.add("session_interim");
		possibleChargingType.add("session_stop");
		possibleChargingType.add("specific_charge");

		RateContext rateContext = new RateContext();
		
		try (Connection connection = DBConnectionFactory.getPersistentConnection();
				Statement statement = connection.createStatement();
				ResultSet resultSet = statement.executeQuery(searchQuery)) {

			int index = 0;
			while (resultSet.next()) {
				String serviceId = resultSet.getString("ServiceID");
				String chargingType = resultSet.getString("ChargingType");
				String subscriptionGroupId = resultSet.getString("SubscriptionGroupID");
				String subscriptionStatus = resultSet.getString("SubscriptionStatus");
				
				String packageId = resultSet.getString("PackageID");
				String timeSlotId = resultSet.getString("TimeSlotID");
				String callTypeId = resultSet.getString("CallTypeID");
				String region = resultSet.getString("region");

				String rateId = resultSet.getString("RateID");
				if (rateId == null) continue; // No point to add it. 
				
				boolean hasCampaignRate = resultSet.getBoolean("HasCampaignRate");
				int priority = resultSet.getInt("Priority");
				String channel = resultSet.getString("channel");
				String registeredService = resultSet.getString("RegisteredService");
				long billingNodeId = resultSet.getLong("BillingNodeId");
				
				// Adding default value in case selected fields return NULL.
				serviceId = (serviceId == null) ? "na" : serviceId;
				chargingType = (chargingType == null) ? "na" : chargingType;
				subscriptionGroupId = (subscriptionGroupId == null) ? "na" : subscriptionGroupId;
				subscriptionStatus = (subscriptionStatus == null) ? "0" : subscriptionStatus;
				packageId = (packageId == null) ? "na" : packageId;
				timeSlotId = (timeSlotId == null) ? "anytime" : timeSlotId;
				callTypeId = (callTypeId == null) ? "anycall" : callTypeId;
				region = (region == null) ? "na" : region;
				channel = (channel == null) ? "na" : channel;
				registeredService = (registeredService == null) ? "na" : registeredService;

				List<String> wildCardSubscriptionGroupId = new ArrayList<>();
				List<String> wildCardSubscriptionStatus = new ArrayList<>();

				List<String> wildCardChargingType = new ArrayList<>(this.getPatternMatchedChargingTypes(chargingType, possibleChargingType));
				
				if (subscriptionGroupId.equals("na")
						&& this.subscriptionGroupsWRTServiceId.containsKey(serviceId)) {
					wildCardSubscriptionGroupId.add("na");
					wildCardSubscriptionGroupId.addAll(this.subscriptionGroupsWRTServiceId.get(serviceId));
				} else {
					wildCardSubscriptionGroupId.add(subscriptionGroupId);
				}

				if (subscriptionStatus.equals("0")) {
					wildCardSubscriptionStatus.addAll(possibleSubscriptionStatus);
				} else {
					wildCardSubscriptionStatus.add(subscriptionStatus);
				}

				for (String cType : wildCardChargingType) {
					for (String sgId : wildCardSubscriptionGroupId) {
						for (String ss : wildCardSubscriptionStatus) {
							rateContext.storeRateIndexForServiceId(serviceId, index);
							rateContext.storeRateIndexForSubscriptionGroupId(sgId, index);
							rateContext.storeRateIndexForChargingType(cType, index);
							rateContext.storeRateIndexForPackageId(packageId, index);
							rateContext.storeRateIndexForSubscriptionStatus(ss, index);
							rateContext.storeRateIndexForTimeSlotId(timeSlotId, index);
							rateContext.storeRateIndexForCallTypeId(callTypeId, index);
							rateContext.storeRateIndexForRegion(region, index);

							rateContext.storeRateIdInformation(
									index, new RateMaster(wildCardSubscriptionStatus.size(), rateId, hasCampaignRate, billingNodeId, priority, channel, registeredService, sgId, ss)
									);
							index = index + 1;
						}
					}
				}
			}
			
			this.rateContext = rateContext;
			return true;
		} catch (SQLException e) {
			logger.error("Exception while loading table [ratemaster].", e);
		}

		return false;
	}
	
	private List<String> getPatternMatchedChargingTypes(String pattern, List<String> allowedChargingTypes) {
		Pattern p = Pattern.compile(pattern);
		
		List<String> matchedChargingTypes =  allowedChargingTypes.stream()
				                                                 .filter(chargingType -> p.matcher(chargingType).find())
				                                                 .collect(Collectors.toList());	
		if (matchedChargingTypes.isEmpty()) {
			matchedChargingTypes.add(pattern);
		}
		
		return matchedChargingTypes;
	}

	public boolean loadRatingInfo() {
		Map<String, List<RatePulse>> ratingInfo = new HashMap<>();

		String searchQuery = "SELECT rateid, stepduration, steppulse, steppulserate, minAmount, maxAmount "
		                  + "FROM ratepulse "
				          + "WHERE rateid IS NOT NULL AND ActivationStart <= NOW() AND ActivationEnd >= NOW() "
				          + "ORDER BY rateid, stepno ASC";

		try (Connection connection = DBConnectionFactory.getPersistentConnection();
				Statement statement = connection.createStatement();
				ResultSet resultSet = statement.executeQuery(searchQuery)) {
			while (resultSet.next()) {
				String rateId = resultSet.getString("rateid");
				
				int stepDuration = resultSet.getInt("stepDuration");
				int stepPulse = resultSet.getInt("steppulse");
				long stepPulseRate = resultSet.getLong("steppulserate");
				int minAmount = resultSet.getInt("minAmount");
				int maxAmount = resultSet.getInt("maxAmount");

				RatePulse ri = new RatePulse(stepDuration, stepPulse, stepPulseRate, minAmount, maxAmount);

				if (ratingInfo.containsKey(rateId)) {
					ratingInfo.get(rateId).add(ri);
				} else {
					List<RatePulse> ratePulses = new ArrayList<>();
					ratePulses.add(ri);
					ratingInfo.put(rateId, ratePulses);
				}
			}
			
			this.ratingInfo = ratingInfo;
			return true;
		} catch (SQLException e) {
			logger.error("Exception while loading table [ratepulse].", e);
		}

		return false;
	}
	
	public long getEpochCounter() {
		return this.epochSeconds.get();
	}

	public List<RateMaster> getRateContexts(ProcedureParams params) {
		List<RateMaster> rateContexts = new ArrayList<>();

		//System.out.println("serviceID inside specific config:"+params.getRequest().getServiceId());

		if (this.rateContext.getRateIndexesForServiceId(params.getRequest().getServiceId()) == null) {
			//System.out.println("case1");
			return rateContexts;
		}

		if (!this.rateContext.checkForRateIndexAvailability(params)) {
			//System.out.println("case2");
			return rateContexts;
		}


		for (int index : this.rateContext.getRateIndexesForServiceId(params.getRequest().getServiceId())) {
			//System.out.println("Inside loop, service id:"+params.getRequest().getServiceId());
			//System.out.println("case3");
			if (this.rateContext.checkAvailabilityForRateIndex(params, index)) {
				rateContexts.add(this.rateContext.getRateIdInformationFor(index));
			}
		}

		return rateContexts;
	}
	
	public List<RateMaster> getRateContextsFor(String serviceId, String subscriptionGroupId, String chargingType, String packageId) {
		List<RateMaster> rateContexts = new ArrayList<>();

		if (!this.rateContext.checkForRateIndexAvailabilityFor(serviceId, subscriptionGroupId, chargingType, packageId))
			return new ArrayList<>();

		List<Integer> indexes = this.rateContext.getRateIndexesForServiceId(serviceId)
					                .stream()
					                .filter(index -> this.rateContext.checkAvailabilityForRateIndexFor(subscriptionGroupId, chargingType, packageId, index))
					                .collect(Collectors.toList());
        indexes.forEach(index -> rateContexts.add(this.rateContext.getRateIdInformationFor(index)));
		return rateContexts;
	}

	public Tuple<Integer, RatePulse> getRatingInfo(String rateId, int stepNo) {
		if (this.ratingInfo.containsKey(rateId) && this.ratingInfo.get(rateId).size() > stepNo) {
			return new Tuple<>(this.ratingInfo.get(rateId).size(), this.ratingInfo.get(rateId).get(stepNo));
		}

		return null;
	}

	public ServiceInfo getServiceInfo(String serviceId) {
		serviceId = serviceId.toLowerCase();
		return this.serviceInformation.containsKey(serviceId) ?
				this.serviceInformation.get(serviceId) : new ServiceInfo();
	}

	public Set<String> getSubscriptionGroupsFor(String serviceId) {
		serviceId = serviceId.toLowerCase();
		if (this.subscriptionGroupsWRTServiceId.containsKey(serviceId)) {
			return this.subscriptionGroupsWRTServiceId.get(serviceId);
		}

		return null;
	}

	public String getsubCycleInfo(String SubgroupID) {
		if(subCycleWRTsubgroupID.containsKey(SubgroupID.toLowerCase()))
			return this.subCycleWRTsubgroupID.get(SubgroupID.toLowerCase());
		return null;
	}

	public int walletOption(String serviceId) {
		serviceId = serviceId.toLowerCase();
		if (this.rootSubscriptionInformation.containsKey(serviceId)) {
			return this.rootSubscriptionInformation.get(serviceId).second ? 1 : 0;
		}

		return -1;
	}



	public String getSubscriptionRoot(String serviceId) {
		serviceId = serviceId.toLowerCase();
		if (this.rootSubscriptionInformation.containsKey(serviceId)) {
			return this.rootSubscriptionInformation.get(serviceId).first;
		}

		return "na";
	}
	
	public String getActualBNO(String requestedBNO) {
		return this.shortCodes.stream()
				              .filter(shortCode -> requestedBNO.indexOf(shortCode) > -1)
				              .findFirst()
				              .orElse(requestedBNO);
	}
	
	private void startEpochCounter() {
		this.epochSeconds = new AtomicLong();
		
		try (Jedis redis = DBConnectionFactory.getInMemoryConnection()) {
			this.epochSeconds.set(Long.valueOf(redis.time().get(0)));
		}
		
		Executors.newScheduledThreadPool(1).scheduleAtFixedRate(() -> this.epochSeconds.getAndIncrement(), 0, 1000, TimeUnit.MILLISECONDS);
	}
	
	private class ScheduledTask extends TimerTask {
		
		public void run() {			
			setCurrentTimeSlotInformation();
		}
	}
}

class RateContext {
	private List<RateMaster> rateIds;
	private Map<String, List<Integer>> serviceId;
	private Map<String, List<Integer>> subscriptionGroupId;
	private Map<String, List<Integer>> chargingType;
	private Map<String, List<Integer>> packageId;
	private Map<String, List<Integer>> timeSlotId;
	private Map<String, List<Integer>> callTypeId;
	private Map<String, List<Integer>> subscriptionStatus;
	private Map<String, List<Integer>> region;

	RateContext() {
		this.rateIds = new ArrayList<>();
		this.serviceId = new HashMap<>();
		this.subscriptionGroupId = new HashMap<>();
		this.chargingType = new HashMap<>();
		this.packageId = new HashMap<>();
		this.timeSlotId = new HashMap<>();
		this.callTypeId = new HashMap<>();
		this.subscriptionStatus = new HashMap<>();
		this.region = new HashMap<>();
	}

	public boolean checkForRateIndexAvailability(ProcedureParams params) {
		// Check for all parameters for availability.
		return (!params.getRequest().isSubscriptionGroupIdApplicable() || this.subscriptionGroupId.containsKey(params.getRequest().getSubscriptionGroupId().toLowerCase()))
				&& this.chargingType.containsKey(params.getMappedChargedType().toLowerCase())
				&& this.packageId.containsKey(params.getPackageInformation().getPackageId().toLowerCase())
				&& this.timeSlotId.containsKey(params.getTPSInformation().getTimeSlotId().toLowerCase())
				&& (!ApplicationProperties.getRatePlanConf().callTypeIdCheck || this.callTypeId.containsKey(params.getCallTypeId().toLowerCase()))
				&& this.region.containsKey(params.getRegion().toLowerCase());
	}
	
	public boolean checkForRateIndexAvailabilityFor(String serviceId, String subscriptionGroupId, String chargingType, String packageId) {
		// Check for all parameters for availability.
		return this.serviceId.containsKey(serviceId.toLowerCase())
				&& this.subscriptionGroupId.containsKey(subscriptionGroupId.toLowerCase())
				&& this.chargingType.containsKey(chargingType.toLowerCase())
				&& this.packageId.containsKey(packageId.toLowerCase());
	}

	public boolean checkAvailabilityForRateIndex(ProcedureParams params, int expectedIndex) {
		// Check for all parameters for availability.
		return (!params.getRequest().isSubscriptionGroupIdApplicable() || Utility.binarySearch(this.subscriptionGroupId.get(params.getRequest().getSubscriptionGroupId().toLowerCase()), expectedIndex))
				&& Utility.binarySearch(this.chargingType.get(params.getMappedChargedType().toLowerCase()), expectedIndex)
				&& Utility.binarySearch(this.packageId.get(params.getPackageInformation().getPackageId().toLowerCase()), expectedIndex)
				&& Utility.binarySearch(this.timeSlotId.get(params.getTPSInformation().getTimeSlotId().toLowerCase()), expectedIndex)
				&& (!ApplicationProperties.getRatePlanConf().callTypeIdCheck || Utility.binarySearch(this.callTypeId.get(params.getCallTypeId().toLowerCase()), expectedIndex))
				&& Utility.binarySearch(this.region.get(params.getRegion().toLowerCase()), expectedIndex);
	}


	public boolean checkAvailabilityForRateIndexFor(String subscriptionGroupId, String chargingType, String packageId, int expectedIndex) {
		// Check for all parameters for availability.
		return Utility.binarySearch(this.subscriptionGroupId.get(subscriptionGroupId.toLowerCase()), expectedIndex)
				&& Utility.binarySearch(this.chargingType.get(chargingType.toLowerCase()), expectedIndex)
				&& Utility.binarySearch(this.packageId.get(packageId.toLowerCase()), expectedIndex);
	}

	public List<Integer> getRateIndexesForServiceId(String serviceId) {
		return this.serviceId.get(serviceId.toLowerCase());
	}

	public void storeRateIdInformation(int index, RateMaster rateIdInfo) {
		this.rateIds.add(index, rateIdInfo);
	}

	public RateMaster getRateIdInformationFor(int index) {
		if (index >= 0 && index < this.rateIds.size())
			return this.rateIds.get(index);
		return null;
	}

	public void storeRateIndexForServiceId(String serviceId, int index) {	
		if (this.serviceId.containsKey(serviceId)) {
			this.serviceId.get(serviceId).add(index);
		} else {
			List<Integer> container = new ArrayList<>();
			container.add(index);
			this.serviceId.put(serviceId, container);
		}
	}

	public void storeRateIndexForSubscriptionGroupId(String subscriptionGroupId, int index) {		
		if (this.subscriptionGroupId.containsKey(subscriptionGroupId)) {
			this.subscriptionGroupId.get(subscriptionGroupId).add(index);
		} else {
			List<Integer> container = new ArrayList<>();
			container.add(index);
			this.subscriptionGroupId.put(subscriptionGroupId, container);
		}
	}

	public void storeRateIndexForChargingType(String chargingType, int index) {
		if (this.chargingType.containsKey(chargingType)) {
			this.chargingType.get(chargingType).add(index);
		} else {
			List<Integer> container = new ArrayList<>();
			container.add(index);
			this.chargingType.put(chargingType, container);
		}
	}

	public void storeRateIndexForPackageId(String packageId, int index) {
		if (this.packageId.containsKey(packageId)) {
			this.packageId.get(packageId).add(index);
		} else {
			List<Integer> container = new ArrayList<>();
			container.add(index);
			this.packageId.put(packageId, container);
		}
	}

	public void storeRateIndexForTimeSlotId(String timeSlotId, int index) {
		if (this.timeSlotId.containsKey(timeSlotId)) {
			this.timeSlotId.get(timeSlotId).add(index);
		} else {
			List<Integer> container = new ArrayList<>();
			container.add(index);
			this.timeSlotId.put(timeSlotId, container);
		}
	}
	
	public void storeRateIndexForCallTypeId(String callTypeId, int index) {
		if (this.callTypeId.containsKey(callTypeId)) {
			this.callTypeId.get(callTypeId).add(index);
		} else {
			List<Integer> container = new ArrayList<>();
			container.add(index);
			this.callTypeId.put(callTypeId, container);
		}
	}

	public void storeRateIndexForSubscriptionStatus(String subscriptionStatus, int index) {		
		if (this.subscriptionStatus.containsKey(subscriptionStatus)) {
			this.subscriptionStatus.get(subscriptionStatus).add(index);
		} else {
			List<Integer> container = new ArrayList<>();
			container.add(index);
			this.subscriptionStatus.put(subscriptionStatus, container);
		}
	}

	public void storeRateIndexForRegion(String region, int index) {		
		if (this.region.containsKey(region)) {
			this.region.get(region).add(index);
		} else {
			List<Integer> container = new ArrayList<>();
			container.add(index);
			this.region.put(region, container);
		}
	}
}
