package com.ssdtech.java.model.applicationproperties;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ssdtech.cgwcommon.INRequester;

public final class ApplicationProperties {
	public static String IN_REQUESTER_NAME;
	public static Class<?> IN_INITIALIZER;
	private static Class<?> IN_REQUESTER;

	private static final Logger logger = LogManager.getLogger(ApplicationProperties.class.getName());
	private static final String CGW_APP_PROPERTY_FILENAME = "ConfigurationFiles/cgw/cgw.ini";
	private static Properties applicationProperties;
	static {
		applicationProperties = new Properties();
	}

	public static final Set<String> duplicationCheckEnabledRequestType = new HashSet<>();
	static {
		duplicationCheckEnabledRequestType.add("session_start");
		duplicationCheckEnabledRequestType.add("specific_charge");
	}

	public static final Set<String> duplicationInsertEnabledRequestType = new HashSet<>();
	static {
		duplicationInsertEnabledRequestType.add("specific_charge");
		duplicationInsertEnabledRequestType.add("session_stop");
	}

	public static final Set<String> walletDeductibleStatus = new HashSet<>();
	static {
		walletDeductibleStatus.add("2");
		walletDeductibleStatus.add("4");
	}

	public static final long INVALID_BILLING_NODE_ID = -1L;

	private static ServerSpecificConfiguration serverSpecificConf;
	private static RequestTransformationConfiguration reqTransConf;
	private static RatePlanConfiguration ratePlanConf;
	private static DatabaseConfiguration dbConf;
	private static BillingInterfaceConfiguration billingInterfaceConf;
	private static LogConfiguration logConf;
	private static SNMPConfiguration snmpConf;
	private static CDRConfiguration cdrConf;
	private static SyncAppConfiguration syncAppConfiguration;
	private static LogServerConfiguration logServerConfiguration;
	private static PriceLimitConfiguration priceLimitConfiguration;

	private static Object shutdownController;
	
	private static okhttp3.OkHttpClient httpClient;

	public static boolean usingOldWallet;

	private ApplicationProperties(){}

	public static void initialize() throws IOException, ClassNotFoundException {
		loadINClassPath();
		loadApplicationPropertyFile();
		loadServerSpecificConfiguration();
		loadReqTransConf();
		loadRatePlanConfiguration();
		loadDatabaseConfiguration();
		loadBillingInterfaceConfiguration();
		loadLogConfiguration();
		loadSNMPConfiguration();
		loadCDRConfiguration();
		loadSyncAppConfiguration();
		loadLogServerConfiguration();
		loadPriceLimitConfiguration();

		initializeShutdownController();
		initializeHttpClient();
	}

    public static Level getLevel() {
        switch (ApplicationProperties.getLogConfiguration().level) {
            case 0:
                return Level.OFF;
            case 1:
                return Level.FATAL;
            case 2:
                return Level.ERROR;
            case 3:
                return Level.WARN;
            case 4:
                return Level.INFO;
            case 5:
                return Level.DEBUG;
            case 6:
                return Level.ALL;
            default:
                return Level.DEBUG;
        }
    }

	private static void loadINClassPath() throws ClassNotFoundException, IOException {
		try (InputStream in = ClassLoader.getSystemResourceAsStream("cgw.properties")) {
			Properties properties = new Properties();
			properties.load(in);
			String inRequesterName = properties.getProperty("IN_REQUESTER_NAME", "");
			Optional<String> inInitializerClassPath = Optional.ofNullable(properties.getProperty("CLASS_PATH_IN_INITIALIZER"));
			Optional<String> inRequesterClassPath = Optional.ofNullable(properties.getProperty("CLASS_PATH_IN_REQUESTER"));

			if (inInitializerClassPath.isPresent() && !inInitializerClassPath.get().trim().isEmpty()
					&& inRequesterClassPath.isPresent() && !inRequesterClassPath.get().isEmpty()) {
				IN_REQUESTER_NAME = inRequesterName;
				IN_INITIALIZER = Class.forName(inInitializerClassPath.get().trim());
				IN_REQUESTER = Class.forName(inRequesterClassPath.get().trim());
			}
		}
	}

	public static Optional<INRequester> getINRequester() {
		Optional<INRequester> inRequester = Optional.empty();

		if (IN_REQUESTER != null) {
			try {
				inRequester = Optional.of((INRequester) IN_REQUESTER.newInstance());
			} catch (IllegalAccessException | InstantiationException | ExceptionInInitializerError | SecurityException e) {
				if(logger.isFatalEnabled()) logger.fatal("Exception while getting an instance of INRequester", e);
			}
		}

		return inRequester;
	}

	private static void loadApplicationPropertyFile() throws IOException {
		applicationProperties.load(new FileInputStream(CGW_APP_PROPERTY_FILENAME));
	}

	public static ServerSpecificConfiguration getServerSpecificConfiguration() {
		return serverSpecificConf;
	}

	private static void loadServerSpecificConfiguration() {
		serverSpecificConf = new ServerSpecificConfiguration(applicationProperties);
	}

	public static RequestTransformationConfiguration getReqTransConf() {
		return reqTransConf;
	}

	private static void loadReqTransConf() {
		reqTransConf = new RequestTransformationConfiguration(applicationProperties);
	}

	public static RatePlanConfiguration getRatePlanConf() {
		return ratePlanConf;
	}

	private static void loadRatePlanConfiguration() {
		ratePlanConf = new RatePlanConfiguration(applicationProperties);
		if (ratePlanConf.regionCheck) {
			initializeHttpClient();
		}
	}

	public static DatabaseConfiguration getDatabaseConfiguration() {
		return dbConf;
	}

	private static void loadDatabaseConfiguration() {
		dbConf = new DatabaseConfiguration(applicationProperties);
	}

	public static BillingInterfaceConfiguration getBillingInterfaceConf() {
		return billingInterfaceConf;
	}

	private static void loadBillingInterfaceConfiguration() {
		billingInterfaceConf = new BillingInterfaceConfiguration(applicationProperties);
	}

	public static LogConfiguration getLogConfiguration() {
		return logConf;
	}

	private static void loadLogConfiguration() {
		logConf = new LogConfiguration(applicationProperties);
	}

	private static SNMPConfiguration getSNMPConfiguration() {
		return snmpConf;
	}

	private static void loadSNMPConfiguration() {
		snmpConf = new SNMPConfiguration(applicationProperties);
	}

	public static CDRConfiguration getCDRConfiguration() {
		return cdrConf;
	}

	private static void loadCDRConfiguration() {
		cdrConf = new CDRConfiguration(applicationProperties);
	}

	public static SyncAppConfiguration getSyncAppConfiguration() {
		return syncAppConfiguration;
	}

	private static void loadSyncAppConfiguration() {
		syncAppConfiguration = new SyncAppConfiguration(applicationProperties);
	}

	public static LogServerConfiguration getLogServerConfiguration() {
		return logServerConfiguration;
	}

	private static void loadLogServerConfiguration() {
		logServerConfiguration = new LogServerConfiguration(applicationProperties);
	}

	private static void loadPriceLimitConfiguration() {
		priceLimitConfiguration = new PriceLimitConfiguration(applicationProperties);
	}

	public static PriceLimitConfiguration getPriceLimitConfiguration() {
		return priceLimitConfiguration;
	}

	public static okhttp3.OkHttpClient getHttpClient() {
		return httpClient;
	}
	
	public static Object getShutdownController() {
		return shutdownController;
	}

	public static void clearHttpClient() {
		httpClient.dispatcher().executorService().shutdown();
		httpClient.connectionPool().evictAll();
	}

	public static void dumpConfigurationToLogFile() {
		logger.info(
				getServerSpecificConfiguration().toString() +
				getReqTransConf().toString() +
				getRatePlanConf().toString() +
				getDatabaseConfiguration().toString() +
				getBillingInterfaceConf().toString() +
				getLogConfiguration().toString() +
				getSNMPConfiguration().toString() +
				getCDRConfiguration().toString() +
				getSyncAppConfiguration().toString() +
				getLogServerConfiguration().toString() +
				getPriceLimitConfiguration().toString());
	}

	private static void initializeHttpClient() {
		httpClient = new okhttp3.OkHttpClient.Builder()
				         .readTimeout(ApplicationProperties.getBillingInterfaceConf().receiveTimeout, TimeUnit.SECONDS)
	                     .build();
	}
	
	private static void initializeShutdownController() {
		shutdownController = new Object();
	}
}