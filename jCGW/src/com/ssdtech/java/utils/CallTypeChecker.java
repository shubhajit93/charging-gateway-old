package com.ssdtech.java.utils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ssdtech.java.db.management.DBConnectionFactory;
import com.ssdtech.java.model.CallType;
import com.ssdtech.java.model.applicationproperties.ApplicationProperties;

public class CallTypeChecker {
	private static final Logger logger = LogManager.getLogger(CallTypeChecker.class.getName());
	
	private static volatile List<CallType> _callTypeInfo;

	static {
		loadCallTypeInfo();
	}

	private CallTypeChecker(){}
	
	public static boolean loadCallTypeInfo() {
		List<CallType> callTypeInfo = new ArrayList<>();
		
		String searchQuery = "SELECT REPLACE(LOWER(Ano),'%','\\\\w*') AS Ano, REPLACE(LOWER(Bno),'%','\\\\w*') AS Bno, LOWER(CallTypeID) AS CallTypeID, LENGTH(Ano)+LENGTH(Bno) AS priority FROM calltype";
		try (Connection connection = DBConnectionFactory.getPersistentConnection();
				Statement statement = connection.createStatement()) {
			ResultSet resultSet = statement.executeQuery(searchQuery);
			
			while (resultSet.next()) {
				callTypeInfo.add( 
						new CallType(
								resultSet.getString("Ano"),
								resultSet.getString("Bno"),
								resultSet.getString("CallTypeID"),
								resultSet.getInt("priority")
								)
						);
			}
			
			resultSet.close();
			_callTypeInfo = callTypeInfo;
			
			return true;
		} catch (SQLException e) {
			logger.error("Exception while loading table [calltype].", e);
		}
		
		return false;
	}
	
	public static String getCallTypeId(String ano, String bno) {
		Optional<CallType> possibleCallType = Optional.empty();
		int currentPriority = Integer.MIN_VALUE;
		
		for (CallType callType : _callTypeInfo) {
			if (callType.equals(ano, bno) && callType.getPriority() > currentPriority) {
				possibleCallType = Optional.of(callType);
				currentPriority = callType.getPriority();
			}
		}

		return possibleCallType.map(CallType::getCallTypeId).orElseGet(() -> ApplicationProperties.getRatePlanConf().defaultCallTypeId);
	}
}
