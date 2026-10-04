package com.ssdtech.robi;

import java.io.IOException;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

/**
 * Bismillahir rahmanir rahim
 * 
 * @author Md. Shahriar Kamal
 *
 */
public class Server_Threaded {
	private static final String LOG_PROPERTIES_FILE_PATH = "./ConfigurationFiles/in/log4j.properties";
	String host, service_name, service_code;
	static ListenServer listen;
	private Logger logger;

	private void logConfigure() {
		String renewalSuccessCodes = this.aggregateResultCodes(Global.RENEWAL_SUCCESS_CODES_MAPPING.iterator());
		String renewalFailureCodes = this.aggregateResultCodes(Global.RENEWAL_FAILURE_CODES_MAPPING.iterator());
		
        String configurationProperties = String.format(
        		"\r\nLOGENABLE : %d\r\n" +
        		"LISTEN_PORT : %d\r\n" +
        		"SOAPENV_URL : %s\r\n" +
        		"V2_URL : %s\r\n" +
        		"LOC_URL : %s\r\n" +
        		"LOC_URL1 : %s\r\n" +
        		"PREFIX : %s\r\n" +
        		"MSISDN_LENTH : %d\r\n" +
        		"Renewal properties : \r\n" +
        		"\tRENEWAL_URL : %s\r\n" +
        		"\tRENEWAL_SUCCESS_CODE_MAPPING : %s -> %s\r\n" +
        		"\tRENEWAL_FAILURE_CODE_MAPPING : %s -> %s\r\n" +
        		"\tRENEWAL_DEFAULT_RESULT_CODE : %s\r\n" +
        		"Payment properties : \r\n" +
        		"\tPAYMENT_URL = %s\r\n" +
        		"\tDESCRIPTION : %s\r\n" +
        		"\tCURRENCY : %s\r\n"
        		, Global.LOGENABLE, Global.LISTEN_PORT, Global.SOAPENV_URL, Global.V2_URL, Global.LOC_URL, Global.LOC_URL1,
        		Global.PREFIX, Global.MSISDN_LENTH,
        		Global.RENEWAL_SPECIFIC_DATA.get("url"),
        		renewalSuccessCodes, Global.RENEWAL_CGW_SUCCESS_CODE, 
        		renewalFailureCodes, Global.RENEWAL_CGW_FAILURE_CODE,
        		Global.RENEWAL_DEFAULT_RESULT_CODE,
        		Global.PAYMENT_SPECIFIC_DATA.get("url"), Global.PAYMENT_SPECIFIC_DATA.get("description"),
        		Global.PAYMENT_SPECIFIC_DATA.get("currency"));
        this.logger.info(configurationProperties);
	}
	
	private String aggregateResultCodes(Iterator<String> iterator) {
		String resultCodes = "";
		if (iterator.hasNext()) resultCodes += iterator.next();
		while (iterator.hasNext()) {
			resultCodes += ", " + iterator.next();
		}
		return resultCodes;
	}

	public Server_Threaded() throws IOException {
		PropertyConfigurator.configure(LOG_PROPERTIES_FILE_PATH);
		this.logger = Logger.getLogger(Server_Threaded.class.getName());
		Global.configure();
		logConfigure();
	}

	private void CallListenServer() {
		listen = ListenServer.getInstance();
		logger.info("Starting Listern Server...");
		try {
			listen.run();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws Exception {
		Server_Threaded server = new Server_Threaded();
		server.CallListenServer();
	}
}
