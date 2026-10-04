package com.ssdtech;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;
import java.util.StringTokenizer;

public class Global {
	public static int ThreadCount = 0;
	public static String dummycode = "0";
	public static String log = "0";
	public static int logfornumber = 0;
	public static String MsisdnList = "";
	public static String[] MsisdnArray;
	public static String LogDirectory = "";
	static SimpleDateFormat formatter = new SimpleDateFormat("EEE, dd-MMM-yyyy HH:mm:ss:S");
	public static String myDestinationRealm = "";
	/** The URI for this client */
	public static String myURI = "";
	/** The realm for this client */
	public static String myRealm = "";
	/** The destination host */
	public static String myDestinationHost = "";
	/** my own Ip address */
	public static String OwnIPAddress = "";
	/** my OwnProductName */
	public static String OwnProductName = "";
	/** my OwnVendorID address */
	public static String OwnVendorID = "";
	/** port address */
	public static String port = "";
	/** Outdial Data */
	public static String Outdialdata = "";
	/** Response Delay */
	public static double ServerResponseDelay = 0.0;
	public static String TimeZone = "";

	public static String commandPort = "";

	public static int ThreadSwitchOption = 0;// 1 for Single Thread 2 for MultiThread

	public static String UcipLog = "";
	public static String UcipConfigFile = "";
	public static String InsuffientBalanceError = "";
	public static String ServiceContextId = "SCAP_V.2.0@ericsson.com";
	public static long ServiceIdentifier = 20;
	public static int CdrLength = 16;
	public volatile static HashMap<String, String> SPID_MAP = new HashMap<String, String>();
	public static HashMap<String, Integer> Numberseries_MAP = new HashMap<String, Integer>();
	public static boolean NumberSeriesEnable = false;

	public static String PostpaidPackage = "";
	public static int count = 1;
	public static SimpleDateFormat dateFormatHourMinSec = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS"); // to add log
																											// time.
	public static SimpleDateFormat dateFormatHour = new SimpleDateFormat("yyyy-MM-dd-HH");
	public static Properties prop;

	/** Timeout for event charging **/
	public static boolean defaultTxTimeoutEnable;
	public static int txTimeout;

	/** Result codes log related **/
	public static boolean enableResultCodeWiseLog;
	public static long[] resultCodes;
	public static Map<Long, CustomizedLogFileWriter> resultCodesWriterMapping;

	public static void write(String Logmsg, String callingfuncname) throws IOException {
		File file = new File(LogDirectory + dateFormatHour.format(new Date()) + ".txt");
		if (Global.log.equals("1")) {
			BufferedWriter bw = null;
			try {
				bw = new BufferedWriter(new FileWriter(file, true));
				bw.write(Logmsg + ". Function: " + callingfuncname);
				bw.newLine();
				bw.flush();
			} catch (IOException ioe) {
				ioe.printStackTrace();
			} finally {
				if (bw != null)
					try {
						bw.close();
					} catch (IOException ioe2) {
						// just ignore it
					}
			}
		}
	}

	public static void logForNumber(String msisdn, String Logmsg, String callingfuncname) throws IOException {
		List temp_MsisdnList = Arrays.asList(Global.MsisdnArray);

		if (Global.logfornumber == 1 && Global.MsisdnArray.length > 0 && temp_MsisdnList.contains(msisdn)) {

			File file = new File(LogDirectory + dateFormatHour.format(new Date()) + "_" + msisdn + ".txt");
			BufferedWriter bw = null;
			try {
				bw = new BufferedWriter(new FileWriter(file, true));
				bw.write(Logmsg + ". Function: " + callingfuncname);
				bw.newLine();
				bw.flush();
			} catch (IOException ioe) {
				ioe.printStackTrace();
			} finally {
				if (bw != null)
					try {
						bw.close();
					} catch (IOException ioe2) {
						// just ignore it
					}
			}

		}
	}

	public static void loadProperties() {

		FileInputStream fin = null;
		prop = new Properties();

		try {
			fin = new FileInputStream(new File(Global.UcipConfigFile));
			prop.load(fin);
			setProperties();
		} catch (Exception ex1) {
			try {
				Global.write(Global.dateFormatHourMinSec.format(new Date()) + Global.getStackTrace(ex1),
						"Exception in property file");
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			ex1.printStackTrace();

		} finally {
			if (null != fin) {
				try {
					fin.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

		}
	}

	private static void setProperties() {
		String rawTxTimeout = prop.getProperty("TxTimeout");
		defaultTxTimeoutEnable = false;
		if (rawTxTimeout == null || rawTxTimeout.trim().isEmpty() || (Integer.valueOf(rawTxTimeout.trim()) <= 0)) {
			defaultTxTimeoutEnable = true;
		} else {
			txTimeout = Integer.valueOf(rawTxTimeout);
		}
	}

	public static void loadResultCodeProperties() {
		enableResultCodeWiseLog = Boolean.parseBoolean(prop.getProperty("EnableResultCodeWiseLog").trim());
		String rawResultCodes = prop.getProperty("EnableLogForResultCodes").trim();
		String[] splittedResultCodes = null;
		if (rawResultCodes.length() == 0) {
			resultCodes = new long[0];
		} else {
			splittedResultCodes = rawResultCodes.split(",");
			resultCodes = new long[splittedResultCodes.length];
		}

		for (int index = 0; index < resultCodes.length; index++) {
			resultCodes[index] = Long.parseLong(splittedResultCodes[index].trim());
		}
		resultCodesWriterMapping = new HashMap<Long, CustomizedLogFileWriter>();
	}

	public static void initializeResultCodesLogFileWriter() {
		if (enableResultCodeWiseLog) {
			File parentDirectory = new File("./ResultCodesLog");
			if (!parentDirectory.exists()) {
				parentDirectory.mkdir();
			}

			for (int index = 0; index < resultCodes.length; index++) {
				String directory = "./ResultCodesLog/" + resultCodes[index];
				File resultCodeDirectory = new File(directory);
				if (!resultCodeDirectory.exists()) {
					resultCodeDirectory.mkdir();
				}
				CustomizedLogFileWriter clfw = new CustomizedLogFileWriter(directory + "/log");
				resultCodesWriterMapping.put(resultCodes[index], clfw);
			}
		}
	}

	public static int configure() {
		HashMap<String, String> spIdMapping = new HashMap<String, String>();
		boolean error = false;
		int currentLineNumber = 0;
		BufferedReader reader = null;
		
		try {
			reader = new BufferedReader(new FileReader("ConfigurationFiles/in/RateConfig.txt"));
			
			for (String line; (line = reader.readLine()) != null; ) {
				currentLineNumber = currentLineNumber + 1;
				
				String[] parts = line.split(">>");
				if (parts.length != 7) {
					error = true;
					Global.write("Error in rateconfig file. Number of parameters should be 7. Line number " + currentLineNumber,
							     "config function");
					break;
				}
				
				spIdMapping.put(parts[0], parts[1] + ">>" + parts[2] + ">>" + parts[3] + ">>" + parts[4] + ">>" + parts[5] + ">>" + parts[6]);
			}
			
			reader.close();
			
			if (!error) {
				currentLineNumber = 0;
				Global.SPID_MAP = spIdMapping;
			}
		} catch (IOException e) {
			System.out.println("Error in reading rateconfig file: " + getStackTrace(e));
			try {
				if (reader != null) reader.close();
				Global.write("Error in reading rateconfig file: " + getStackTrace(e), "config function");
			} catch (IOException e1) {}
		}
		
		return currentLineNumber;
	}

	public static void dmconfigure() {
		File file = new File("Dmconfig.txt");
		BufferedReader reader = null;
		FileReader fr;
		try {
			fr = new FileReader(file);
			reader = new BufferedReader(fr);
			Global.Outdialdata = reader.readLine();
			// System.out.println(Outdialdata);
			Global.myURI = reader.readLine();
			System.out.println(Global.myURI);
			Global.myRealm = reader.readLine();
			System.out.println(Global.myRealm);
			Global.myDestinationRealm = reader.readLine();
			System.out.println(Global.myDestinationRealm);
			Global.myDestinationHost = reader.readLine();
			System.out.println(Global.myDestinationHost);
			Global.OwnIPAddress = reader.readLine();
			System.out.println(Global.OwnIPAddress);
			Global.OwnProductName = reader.readLine();
			System.out.println(Global.OwnProductName);
			Global.OwnVendorID = reader.readLine();
			System.out.println(Global.OwnVendorID);
			Global.port = reader.readLine();
			// System.out.println(port);
			Global.dummycode = reader.readLine();
			// System.out.println(dummycode);
			Global.LogDirectory = reader.readLine();
			Global.log = reader.readLine();
			// System.out.println(log);
			Global.TimeZone = reader.readLine();
			Global.commandPort = reader.readLine();
			Global.logfornumber = Integer.parseInt(reader.readLine());
			Global.MsisdnList = reader.readLine();
			if (MsisdnList.contains(","))
				Global.MsisdnArray = Global.MsisdnList.split(",");
			Global.InsuffientBalanceError = reader.readLine();
			Global.UcipConfigFile = reader.readLine();
			Global.UcipLog = reader.readLine();
			Global.ServiceContextId = reader.readLine();
			Global.ServiceIdentifier = Integer.parseInt(reader.readLine());
			Global.CdrLength = Integer.parseInt(reader.readLine());
			Global.PostpaidPackage = reader.readLine();
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			try {
				reader.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public static int numberseriesconfig() {
		HashMap<String, Integer> numberSeriesMapping = new HashMap<String, Integer>();
		boolean error = false;
		int currentLineNumber = 0;
		BufferedReader reader = null;
		
		try {
			reader = new BufferedReader(new FileReader("ConfigurationFiles/in/numberseries.txt"));
			
			for (String line; (line = reader.readLine()) != null; ) {
				currentLineNumber = currentLineNumber + 1;
				
				if (line == null || line.trim().isEmpty()) {
					continue;
				}
				
				String[] parts = line.split(">>");
				if (parts.length != 2 || parts[0].trim().isEmpty() || parts[1].trim().isEmpty()) {
					error = true;
					Global.write("Error in numberseries file. Number of parameters should be 2. Line number " + currentLineNumber,
							     "numberseries config function");
					break;
				}
				
				Integer number = new Integer(-1);
				String prefix = parts[0].trim();
				String ccn = parts[1].trim();
				
				if (ccn.toUpperCase().startsWith("ASDP")) {
					number = new Integer(0);
				} else if (ccn.toUpperCase().startsWith("KSDP")) {
					number = new Integer(1);
				} else if (ccn.toUpperCase().startsWith("CSDP")) {
					number = new Integer(2);
				}
				
				if (number == -1) {
					error = true;
					Global.write("Error in numberseries file. CCN prefix [" + ccn + "] not found among {ASDP, KSDP, CSDP}. Line number " + currentLineNumber,
							     "numberseries config function");
					break;
				}
				
				numberSeriesMapping.put(prefix, number);
			}
			
			reader.close();
			
			if (!error) {
				Global.Numberseries_MAP = numberSeriesMapping;
				currentLineNumber = 0;
			}
		} catch (IOException e) {
			System.out.println("Error in reading numberseries file: " + getStackTrace(e));
			try {
				if (reader != null) reader.close();
				Global.write("Error in reading numberseries file: " + getStackTrace(e), "numberseries config function");
			} catch (IOException e1) {}
		}
		
		return currentLineNumber;		
	}

	public static String getStackTrace(Throwable throwable) {
		StringWriter writer = new StringWriter();
		PrintWriter printWriter = new PrintWriter(writer);
		throwable.printStackTrace(printWriter);
		return writer.toString();
	}
}
