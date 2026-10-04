package com.ssdtech.java.db.management;

import java.sql.Connection;
import java.sql.SQLException;

import org.apache.tomcat.jdbc.pool.DataSource;
import org.apache.tomcat.jdbc.pool.PoolProperties;

import com.ssdtech.java.model.applicationproperties.ApplicationProperties;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Protocol;

public class DBConnectionFactory {
    private static DataSource persistentDataSource;
	private static JedisPool inMemoryDataSource;
	private static EmbededDB embededDataSource;

	private DBConnectionFactory(){}
	
	public static void initializePersistentDataSource() {
		PoolProperties p = getPersistentConnectionPoolProperties();
		
		persistentDataSource = new DataSource();
		persistentDataSource.setPoolProperties(p);
	}

	public static void initializeInMemoryDataSource() {
		JedisPoolConfig p = getInMemoryConnectionPoolProperties();
		
		inMemoryDataSource = new JedisPool(
				                      p, 
				                      ApplicationProperties.getDatabaseConfiguration().getMemDBConf().ip,
				                      ApplicationProperties.getDatabaseConfiguration().getMemDBConf().port,
				                      Protocol.DEFAULT_TIMEOUT,
				                      ApplicationProperties.getDatabaseConfiguration().getMemDBConf().password,
				                      Integer.parseInt(ApplicationProperties.getDatabaseConfiguration().getMemDBConf().database)
				                  );
	}

	public static void initializeEmbededDataSource() {
		embededDataSource = new EmbededDB();
		embededDataSource.initialize();
	}
	
	public static Connection getPersistentConnection() throws SQLException {
		return persistentDataSource.getConnection();
	}
	
	public static Jedis getInMemoryConnection() {
		return inMemoryDataSource.getResource();
	}
	
	public static EmbededDB getEmbededDataSource() {
		return embededDataSource;
	}
	
	public static void closeAllDataSources() {
		closePersistentDataSource();
		closeInMemoryDataSource();
	}
	
	private static void closePersistentDataSource() {
		persistentDataSource.close();
	}
	
	private static void closeInMemoryDataSource() {
		inMemoryDataSource.close();
	}
	
	private static PoolProperties getPersistentConnectionPoolProperties() {
		// Initialize Pool Configuration.
		PoolProperties p = new PoolProperties();
		
		String connectionUrl = "jdbc:mysql://" + 
		                       ApplicationProperties.getDatabaseConfiguration().getPersistentDBConf().ip + ":" +
				               ApplicationProperties.getDatabaseConfiguration().getPersistentDBConf().port + "/" +
		                       ApplicationProperties.getDatabaseConfiguration().getPersistentDBConf().database;
		
		p.setUrl(connectionUrl);
        p.setDriverClassName("com.mysql.jdbc.Driver");
        p.setUsername(ApplicationProperties.getDatabaseConfiguration().getPersistentDBConf().user);
        p.setPassword(ApplicationProperties.getDatabaseConfiguration().getPersistentDBConf().password);
        p.setJmxEnabled(false);
        p.setTestWhileIdle(true);
        p.setTestOnBorrow(true);
        p.setValidationQuery("SELECT 1");
        p.setTestOnReturn(true);
        p.setValidationInterval(30000);
        p.setTimeBetweenEvictionRunsMillis(30000); 
        p.setMaxActive(ApplicationProperties.getDatabaseConfiguration().getPersistentDBConf().maxConn);
        p.setInitialSize(10);
        p.setMaxWait(10000);
        p.setRemoveAbandonedTimeout(60);
        p.setMinEvictableIdleTimeMillis(30000);
        p.setMinIdle(10);
        p.setLogAbandoned(true);
        p.setRemoveAbandoned(true);
        p.setJdbcInterceptors(
          "org.apache.tomcat.jdbc.pool.interceptor.ConnectionState;"+
          "org.apache.tomcat.jdbc.pool.interceptor.StatementFinalizer");
		        
		return p;
	}
	
	private static JedisPoolConfig getInMemoryConnectionPoolProperties() {
		JedisPoolConfig poolConfig = new JedisPoolConfig();
		poolConfig.setMaxTotal(ApplicationProperties.getDatabaseConfiguration().getMemDBConf().maxConn);
		
		return poolConfig;
	}
}
