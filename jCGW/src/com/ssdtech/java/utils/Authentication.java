package com.ssdtech.java.utils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.sql.Connection;
import java.sql.Statement;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ssdtech.java.db.management.DBConnectionFactory;
import com.ssdtech.java.model.ApplicationInfo;
import com.ssdtech.java.model.ProcedureParams;
import com.ssdtech.java.model.ResultCode;
import com.ssdtech.java.model.applicationproperties.ApplicationProperties;

public class Authentication {
	private static final Logger logger = LogManager.getLogger(Authentication.class.getName());
	
	private static volatile Map<String, ApplicationInfo> _applicationInfo;

	static {
		loadApplicationInfo();
	}

	private Authentication(){}
	
	public static boolean loadApplicationInfo() {
		Map<String, ApplicationInfo> applicationInfo = new HashMap<>();
		
		String searchQuery = "SELECT applicationid, applicationname, passwd, requirepass, requireoauth FROM applicationinfo";
		try (Connection connection = DBConnectionFactory.getPersistentConnection();
				Statement statement = connection.createStatement()) {
			ResultSet resultSet = statement.executeQuery(searchQuery);
			
			while (resultSet.next()) {
				applicationInfo.put(
						resultSet.getString("applicationid"), 
						new ApplicationInfo(
								resultSet.getString("applicationid"),
								resultSet.getString("applicationname"),
								resultSet.getString("passwd"),
								resultSet.getString("requirepass").equals("TRUE"),
								resultSet.getString("requireoauth").equals("TRUE")
								)
						);
			}
			
			resultSet.close();
			_applicationInfo = applicationInfo;
			
			return true;
		} catch (SQLException e) {
			logger.error("Exception while loading table [applicationinfo].", e);
		}
		
		return false;
	}
	
	public static void authenticate(ProcedureParams params) {
		boolean authenticated =  Optional.ofNullable(_applicationInfo.get(params.getRequest().getApplicationId()))
		                                       .filter(appInfo -> !appInfo.isRequirePassword() || appInfo.getPassword().equals(params.getRequest().getApplicationPassword()))
		                                       .isPresent();
		
		if (!authenticated) {
			params.setFlagIfErrorOccured(
					ResultCode.Authentication_Failed,
					ApplicationProperties.getCDRConfiguration().cdrForAuthentication
				);
		}
	}
	
	public static boolean authenticate(String applicationId, String applicationPassword) {
		return Optional.ofNullable(_applicationInfo.get(applicationId))
		          .filter(appInfo -> !appInfo.isRequirePassword() || appInfo.getPassword().equals(applicationPassword))
		          .isPresent();
	}
}
