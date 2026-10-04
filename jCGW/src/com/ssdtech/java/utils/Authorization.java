package com.ssdtech.java.utils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.Statement;
import com.ssdtech.java.db.client.DBClient;
import com.ssdtech.java.db.management.DBConnectionFactory;
import com.ssdtech.java.model.ProcedureParams;
import com.ssdtech.java.model.ResultCode;
import com.ssdtech.java.model.TPS;
import com.ssdtech.java.model.TimeSlotInfo;
import com.ssdtech.java.model.applicationproperties.ApplicationProperties;

public class Authorization {
	private static final Logger logger = LogManager.getLogger(Authorization.class.getName());
	
    private static volatile Map<String, List<TPS>> _tpsInfo = new HashMap<>();

    static {
		loadTPSInfo();
	}

    private Authorization(){}
	
	public static boolean loadTPSInfo() {
		Map<String, List<TPS>> tpsInfo = new HashMap<>();
		
		String searchQuery = "SELECT appid, LOWER(timeslot) AS timeslot, VALUE FROM tps";
		
		try (Connection connection = DBConnectionFactory.getPersistentConnection();
				Statement statement = connection.createStatement()) {
			ResultSet resultSet = statement.executeQuery(searchQuery);
			
			while (resultSet.next()) {
				String applicationId = resultSet.getString("appid");
				
				List<TPS> tps;
				if (tpsInfo.containsKey(applicationId)) {
					tps = tpsInfo.get(applicationId);
				} else {
					tps = new ArrayList<>();
					tpsInfo.put(applicationId, tps);
				}
				
				tps.add(new TPS(
							    resultSet.getString("appid"),
							    resultSet.getString("timeslot"),
							    resultSet.getLong("value")
					    ));
			}
			
			resultSet.close();
			_tpsInfo = tpsInfo;
			return true;
		} catch (SQLException exception) {
			logger.error("Exception while loading table [tps].", exception);
		}
		
		return false;
	}
	
	public static void checkTPS(ProcedureParams params) {
		String applicationId = params.getRequest().getApplicationId();
		
		TPS tps = findTPSInformation(applicationId);
		params.setTPSInformation(tps);
		
		if (!DBClient.tpsExceed(applicationId, tps.getTimeSlotId(), tps.getTPSLimit())) {
			params.setFlagIfErrorOccured(
					ResultCode.ERROR_EXCEEED_TPS_LIMIT, 
					ApplicationProperties.getCDRConfiguration().cdrForTPSError
				);
		}
	}
	
	private static TPS findTPSInformation(String applicationId) {		
		if (_tpsInfo.containsKey(applicationId)) {
			List<TimeSlotInfo> timeSlotInfo = DBClient.getTimeSlotInformation();
			List<TPS> appAllowedTPSInfo = _tpsInfo.get(applicationId);
			
			TPS desiredTPS = null;
			int weight = Integer.MAX_VALUE;
			
			for (TPS tps : appAllowedTPSInfo) {
				for (TimeSlotInfo ts : timeSlotInfo) {
					if (ts.timeSlotId.equals(tps.getTimeSlotId()) && ts.weight < weight) {
						weight = ts.weight;
						desiredTPS = tps;
					}
				}
			}
			
            if (desiredTPS != null) return desiredTPS;
		}
		
		return new TPS(
				       applicationId, 
				       ApplicationProperties.getRatePlanConf().defaultTimeSlotId,
				       ApplicationProperties.getRatePlanConf().defaultTPS
				   );
	}
}
