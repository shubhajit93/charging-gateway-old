package com.ssdtech.robi;

import java.io.*;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.StringTokenizer;

import com.ssdtech.robi.model.ServiceInfo;
import com.ssdtech.robi.requestgenerator.PaymentSOAPMessageGenerator;
import com.ssdtech.robi.requestgenerator.RenewalSOAPMessageGenerator;
import com.ssdtech.robi.requestgenerator.SOAPMessageGenerator;
import com.ssdtech.robi.responseparser.PaymentSOAPResponseParser;
import com.ssdtech.robi.responseparser.RenewalSOAPResponseParser;
import com.ssdtech.robi.responseparser.SOAPResponseParser;

public class Global {
	public static final String RATECONFIG_FILE_PATH = "./ConfigurationFiles/in/rateConfig.txt";
	
	public static String LogDirectory = "";
	static SimpleDateFormat formatter = new SimpleDateFormat("EEE, dd-MMM-yyyy HH:mm:ss:S");
	public static int LISTEN_PORT;
	public static String SOAPENV_URL = "";
	public static String V2_URL = "";
	public static String LOC_URL = "";
	public static String LOC_URL1;
	public static String LOC_URL2;

	public static String PREFIX = "";
	public static int LOGENABLE;
	public static int MSISDN_LENTH;
	
	public static Map<String, String> PAYMENT_SPECIFIC_DATA;
	
	public static Map<String, String> RENEWAL_SPECIFIC_DATA;
	public static Set<String> RENEWAL_SUCCESS_CODES_MAPPING;
	public static String RENEWAL_CGW_SUCCESS_CODE;
	public static Set<String> RENEWAL_FAILURE_CODES_MAPPING;
	public static String RENEWAL_CGW_FAILURE_CODE;
	public static String RENEWAL_DEFAULT_RESULT_CODE;
		
	// Property for Operator SDP request generator type.
	public static Map<String, SOAPMessageGenerator> REQUEST_GENERATOR_TYPE;
	
	// Property for Operator SDP response generator type.
	public static Map<String, SOAPResponseParser> RESPONSE_PARSER_TYPE;
	
	public static HashMap<String, String> ServiceID_MAP;
	public static HashMap<String, String> SPID_MAP;
	public static HashMap<String, String> SPPASSWORD_MAP;
	
	public volatile static Map<String, ServiceInfo> rateidServiceInfoMapping;

	public static SimpleDateFormat dateFormatHourMinSec = new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss"); // to
																										// add
																										// log
																										// time.
	public static SimpleDateFormat dateFormatHour = new SimpleDateFormat("yyyy-MM-dd-HH");

	public static void write(String Logmsg, String callingfuncname) throws IOException {
		if (Global.LOGENABLE == 1) {
			File file = new File(LogDirectory + dateFormatHour.format(new Date()) + ".txt");
			BufferedWriter bw = null;
			try {
				bw = new BufferedWriter(new FileWriter(file, true));
				bw.write(Logmsg + ". Function: " + callingfuncname);
				bw.newLine();
				bw.flush();
			} catch (Exception ioe) {
				ioe.printStackTrace();
			} finally {
				if (bw != null)
					try {
						bw.close();
					} catch (Exception ioe2) {
						// just ignore it
					}
			}
		}
	}

	public static void configure() {
		Configuration cfg = null;
		try {
			cfg = new Configuration();
		} catch (IOException e1) {
			e1.printStackTrace();
			System.exit(ResultCode.FATAL_EXIT_CODE);
		}
		
		loadPaymentInformation(cfg);
		loadRenewalInformation(cfg);
		loadGeneralInformation(cfg);
		registerSOAPProcessorType(cfg);
		
		BufferedReader serviceConfigReader = null;
		try {
			serviceConfigReader = new BufferedReader(
	                                 new InputStreamReader(
	                                     new FileInputStream(RATECONFIG_FILE_PATH)));
			loadServiceConfiguration(serviceConfigReader);
		} catch (IOException exception) {
			exception.printStackTrace();
			System.exit(ResultCode.FATAL_EXIT_CODE);
		} catch(Exception exception) {
			exception.printStackTrace();
			System.exit(ResultCode.FATAL_EXIT_CODE);
		}
		
		if (serviceConfigReader != null) {
			try {
				serviceConfigReader.close();
			} catch (IOException e) {}
		}
	}
	
	public static String loadCodeMapping(Configuration conf, String propertyKey, Set<String> codeMapping, String[] defaultCodes, String defaultCGWCode) {
        String successCodeMapping = conf.getProperty(propertyKey, "");
		
		String[] successCodes = defaultCodes;
		String cgwMappedCode = defaultCGWCode;
		
		if (!successCodeMapping.isEmpty()) {
			String[] mappingParts = successCodeMapping.split("->");
			
			if (!mappingParts[0].trim().isEmpty()) {
				successCodes = mappingParts[0].split(",");
			}
			
			if (mappingParts.length >= 2) {
				cgwMappedCode = mappingParts[1].trim();
			}
		}
		
		for (int index = 0; index < successCodes.length; index++) {
			codeMapping.add(successCodes[index].trim());
		}
		
		return cgwMappedCode;
	}
	
	public static void loadPaymentInformation(Configuration conf) {
		PAYMENT_SPECIFIC_DATA = new HashMap<String, String>();
		
		Global.PAYMENT_SPECIFIC_DATA.put("url", conf.getProperty("PAYMENT_URL", ""));
		Global.PAYMENT_SPECIFIC_DATA.put("description", conf.getProperty("DESCRIPTION", "charging information"));
		Global.PAYMENT_SPECIFIC_DATA.put("currency", conf.getProperty("CURRENCY", "BDT"));
	}
	
	public static void loadRenewalInformation(Configuration conf) {
		Global.RENEWAL_SPECIFIC_DATA = new HashMap<String, String>();
		
		Global.RENEWAL_SPECIFIC_DATA.put("url", conf.getProperty("RENEWAL_URL", ""));
		Global.RENEWAL_SPECIFIC_DATA.put("actionType", conf.getProperty("RENEWAL_ACTION_TYPE", "1"));
		Global.RENEWAL_SUCCESS_CODES_MAPPING = new HashSet<String>();
		Global.RENEWAL_CGW_SUCCESS_CODE = loadCodeMapping(conf, "RENEWAL_SUCCESS_CODE_MAPPING", 
	                                                        Global.RENEWAL_SUCCESS_CODES_MAPPING, 
	                                                        new String[] {"1"}, ResultCode.SUCCESS_CODE);
		Global.RENEWAL_FAILURE_CODES_MAPPING = new HashSet<String>();
		Global.RENEWAL_CGW_FAILURE_CODE = loadCodeMapping(conf, "RENEWAL_FAILURE_CODE_MAPPING",
				                                            Global.RENEWAL_FAILURE_CODES_MAPPING,
				                                            new String[0], ResultCode.RENEWAL_FAILURE_CODE);
		Global.RENEWAL_DEFAULT_RESULT_CODE = conf.getProperty("RENEWAL_DEFAULT_RESULT_CODE", ResultCode.RENEWAL_DEFAULT_RESULT_CODE);
	}
	
	public static void loadGeneralInformation(Configuration conf) {
		Global.LOGENABLE = Integer.parseInt(conf.getProperty("LOGENABLE", "1"));
		Global.LISTEN_PORT = Integer.parseInt(conf.getProperty("LISTEN_PORT", "3650"));
		Global.SOAPENV_URL = conf.getProperty("SOAPENV_URL", "");
		Global.V2_URL = conf.getProperty("V2_URL", "");
		Global.LOC_URL = conf.getProperty("LOC_URL", "");
		Global.LOC_URL1 = conf.getProperty("LOC_URL1", "");
		Global.LOC_URL2 = conf.getProperty("LOC_URL2", "");
		
		Global.PREFIX = conf.getProperty("PREFIX", "880");
		Global.MSISDN_LENTH = Integer.parseInt(conf.getProperty("MSISDN_LENGTH", "10"));
	}
	
	public static void loadServiceConfiguration(BufferedReader serviceConfigReader) throws IOException {
        // Ignore first line.
		String line = null;
		
		Map<String, ServiceInfo> rateidServiceInfoMapping = new HashMap<String, ServiceInfo>();
		serviceConfigReader.readLine();
		
		int lineCounter = 1;
		while((line = serviceConfigReader.readLine()) != null) {
			if (line.trim().isEmpty()) {
				continue;
			}
			
			String[] serviceInfo = line.split(">>");
			
			if (serviceInfo.length != 5) {
			    throw new IllegalArgumentException(
			    		"Service Configuration format error at line : " + lineCounter);
			}
			
			String rateid = serviceInfo[0].trim();
			String serviceID = serviceInfo[1].trim();
			String spId = serviceInfo[2].trim();
			String spPassword = serviceInfo[3].trim();
			String productId = serviceInfo[4].trim();
			
			ServiceInfo si = new ServiceInfo(serviceID, spId, spPassword, productId);
			rateidServiceInfoMapping.put(rateid, si);
			++lineCounter;
		}
		
		Global.rateidServiceInfoMapping = rateidServiceInfoMapping;
	}
	
	public static void registerSOAPProcessorType(Configuration conf) {
		REQUEST_GENERATOR_TYPE = new HashMap<String, SOAPMessageGenerator>();
		RESPONSE_PARSER_TYPE = new HashMap<String, SOAPResponseParser>();
		
		REQUEST_GENERATOR_TYPE.put("Payment", new PaymentSOAPMessageGenerator(Global.PAYMENT_SPECIFIC_DATA.get("url")));
		RESPONSE_PARSER_TYPE.put("Payment", new PaymentSOAPResponseParser());
		
		REQUEST_GENERATOR_TYPE.put("Renewal", new RenewalSOAPMessageGenerator(Global.RENEWAL_SPECIFIC_DATA.get("url")));
		RESPONSE_PARSER_TYPE.put("Renewal", new RenewalSOAPResponseParser());
	}

	public static String getMD5(String input) {
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			byte[] messageDigest = md.digest(input.getBytes());
			BigInteger number = new BigInteger(1, messageDigest);
			String hashtext = number.toString(16);
			while (hashtext.length() < 32) {
				hashtext = "0" + hashtext;
			}
			return hashtext;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
