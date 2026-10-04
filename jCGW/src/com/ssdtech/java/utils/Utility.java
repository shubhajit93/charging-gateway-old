package com.ssdtech.java.utils;

import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ssdtech.java.model.applicationproperties.ApplicationProperties;

public class Utility {
	private static final Logger logger = LogManager.getLogger(Utility.class.getName());
	
	public static String ptrim(Properties properties, String key, String defaultValue) {
		String propertyValue = properties.getProperty(key, defaultValue);
	    if (propertyValue != null) {
	    	return propertyValue.trim();
	    }
	    
	    return null;
	}
	
	public static String ptrim(Properties properties, String key) {
	    String propertyValue = properties.getProperty(key);
	    if (propertyValue != null) {
	    	return propertyValue.trim();
	    }
	    
	    return null;
	}
	
	public static boolean binarySearch(List<Integer> a, int key) {
		if (a == null || a.isEmpty()) return false;
		
        int lo = 0;
        int hi = a.size() - 1;
        while (lo <= hi) {
            int mid = lo + (hi - lo) / 2;
            if      (key < a.get(mid)) hi = mid - 1;
            else if (key > a.get(mid)) lo = mid + 1;
            else return true;
        }
        return false;
    }
	
	public static boolean isNumeric(String number) {
		if (number == null) return false;
		
		for(byte digit : number.trim().getBytes()) {
			if (digit < 48 || digit > 57) {
				return false;
			}
		}
		
		return true;
	}
	
	public static void writeLogToFile(String msisdn, String content) {
		String logFileFormat = "%s/log%s_%04d%02d%02d%02d%02d%02d.txt";
		
		Calendar calendar = Calendar.getInstance(); // Locale should be set and it should be global.
		calendar.setTimeInMillis((System.currentTimeMillis() / (ApplicationProperties.getLogConfiguration().logCreationFileInterval * 1000)) * (ApplicationProperties.getLogConfiguration().logCreationFileInterval * 1000));
		
		String logFileName = String.format(logFileFormat,
							                ApplicationProperties.getLogConfiguration().directory,
							                ApplicationProperties.getLogConfiguration().logForAnyMSISDN ? "" : msisdn,
							                calendar.get(Calendar.YEAR),
							                calendar.get(Calendar.MONTH) + 1,
							                calendar.get(Calendar.DAY_OF_MONTH),
							                calendar.get(Calendar.HOUR_OF_DAY),
							                calendar.get(Calendar.MINUTE),
				                            calendar.get(Calendar.SECOND));
		Utility.writeToFile(logFileName, content, ApplicationProperties.getLogConfiguration().lineSeparator);
	}
	
	public static void writeToFile(String fileName, String fileContents, String lineSeparator) {
		try {
			FileWriter out = new FileWriter(fileName, true);
			out.write(fileContents + lineSeparator);
			out.close();
		} catch (IOException e) {
			logger.error("Exception while writing [{}] into file [{}]", fileContents, fileName, e);
		}
	}
	
	public static Optional<Connection> getRawMySQLConnection() {
		String dbURL = "jdbc:mysql://" + ApplicationProperties.getDatabaseConfiguration().getPersistentDBConf().ip + ":" 
	                                   + ApplicationProperties.getDatabaseConfiguration().getPersistentDBConf().port + "/" 
				                       + ApplicationProperties.getDatabaseConfiguration().getPersistentDBConf().database;
		
		try {
			Class.forName ("com.mysql.jdbc.Driver").newInstance();
			DriverManager.setLoginTimeout(100000);
			Connection connection = DriverManager.getConnection(
									                dbURL, 
									                ApplicationProperties.getDatabaseConfiguration().getPersistentDBConf().user,
									                ApplicationProperties.getDatabaseConfiguration().getPersistentDBConf().password);
			connection.createStatement().executeQuery("SET wait_timeout = 600");
			connection.createStatement().executeQuery("SET interactive_timeout = 600");
			
			return  Optional.of(connection);
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException | SQLException e) {
			logger.error("Exception while acquiring DB connection.", e);
		}
		
		return Optional.empty();
	}
	
	public static long getEpochTime(String str) {
		long value = 0;
		
		try {
			value = Long.parseLong(str);
		} catch (NumberFormatException e) {
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			try {
		        Date date = df.parse(str);
		        value = date.getTime();
		    } catch (ParseException e1) {}
		}
		
		return value < 0 ? 0 : value;
	}
	
	public static String getEncodedValueForBooleanProperties(boolean value) {
		return value ? "1" : "0";
	}
}
