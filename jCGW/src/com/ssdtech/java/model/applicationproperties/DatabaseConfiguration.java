package com.ssdtech.java.model.applicationproperties;

import java.util.Properties;
import java.util.StringJoiner;

import com.ssdtech.java.utils.Utility;

public class DatabaseConfiguration {
	private final PersistentDBConf persistentDBConf;
	private final MemDBConf memDBConf;
	
	public DatabaseConfiguration(Properties properties) {
		String host = Utility.ptrim(properties, "HOST");
		String port = Utility.ptrim(properties, "PORT");
		String user = Utility.ptrim(properties, "USER");
		String password = Utility.ptrim(properties, "PASSWORD");
		String dbName = Utility.ptrim(properties, "DB");
		int maxConn = Integer.valueOf(Utility.ptrim(properties, "MAX_CONN"));
		int maxRetry = Integer.valueOf(Utility.ptrim(properties, "MAX_RETRY"));
		
		String memHost = Utility.ptrim(properties, "MEM_HOST");
		String memPort = Utility.ptrim(properties, "MEM_PORT");
		String memPassword = Utility.ptrim(properties, "MEM_PASSWORD");
		String memDB = Utility.ptrim(properties, "MEM_DB");
		int memMaxConn = Integer.valueOf(Utility.ptrim(properties, "MEM_MAX_CONN"));
		int memMaxRetry = Integer.valueOf(Utility.ptrim(properties, "MEM_MAX_RETRY"));
		
		this.persistentDBConf = new PersistentDBConf(host, Integer.valueOf(port), user, password, dbName, maxConn, maxRetry);
		this.memDBConf = new MemDBConf(memHost, Integer.valueOf(memPort), "", memPassword, memDB, memMaxConn, memMaxRetry);
	}
	
	public PersistentDBConf getPersistentDBConf() {
		return this.persistentDBConf;
	}
	
	public MemDBConf getMemDBConf() {
		return this.memDBConf;
	}
	
	@Override
	public String toString() {
		return new StringJoiner("\r\n")
				       .add("\r\n\r\n[Database Configuration]")
				       .add(this.persistentDBConf.toString())
				       .add(this.memDBConf.toString())
				       .toString();
	}
	
	private class DBConf {
		public final String ip;
		public final int port;
		public final String user;
		public final String password;
		public final String database;
		public final int maxConn;
		public final int maxRetry;
		
		public DBConf(String ip, int port, String user, String password, String database, int maxConn, int maxRetry) {
			this.ip = ip;
			this.port = port;
			this.user = user;
			this.password = password;
			this.database = database;
			this.maxConn = maxConn;
			this.maxRetry = maxRetry;
		}
	}
	
	public class PersistentDBConf extends DBConf {
	
		public PersistentDBConf(String ip, int port, String user, String password, String database, int maxConn, int maxRetry) {
			super(ip, port, user, password, database, maxConn, maxRetry);
		}
		
		@Override 
		public String toString() {
			return new StringJoiner("\r\n")
					.add("[Persistent Database]")
					.add("HOST = " + this.ip)
					.add("PORT = " + this.port)
					.add("USER = " + this.user)
					.add("PASSWORD = " + this.password)
					.add("DB = " + this.database)
					.add("MAX_CONN = " + this.maxConn)
					.add("MAX_RETRY = " + this.maxRetry)
					.toString();
		}
	}
	
	public class MemDBConf extends DBConf {
		
		public MemDBConf(String ip, int port, String user, String password, String database, int maxConn, int maxRetry) {
			super(ip, port, user, password, database, maxConn, maxRetry);
		}
		
		@Override 
		public String toString() {
			return new StringJoiner("\r\n")
					.add("[In Memory Database]")
					.add("MEM_HOST = " + this.ip)
					.add("MEM_PORT = " + this.port)
					.add("MEM_USER = " + this.user)
					.add("MEM_PASSWORD = " + this.password)
					.add("MEM_DB = " + this.database)
					.add("MEM_MAX_CONN = " + this.maxConn)
					.add("MEM_MAX_RETRY = " + this.maxRetry)
					.toString();
		}
	}
}