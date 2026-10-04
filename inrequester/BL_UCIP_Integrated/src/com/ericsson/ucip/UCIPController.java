package com.ericsson.ucip;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ericsson.ucip.UCIPController;
import com.ericsson.ucip.UCIPProcessor;
import com.ericsson.util.XMLParser;
//import com.ericsson.ucip.UCIPSnippet;
import com.ssdtech.Global;

public class UCIPController {
	// private static UCIPController instance = new UCIPController();
	// private Logger logger = Logger.getLogger(UCIPController.class);
	private UCIPProcessor ucipP = null;
	String originNode = null;
	String oroginHost = null;
	String timeZone = null;
	String Resultcode = "";
	long Amount = -1;

	public UCIPController(UCIPProcessor ucip, Properties prop, Logger logger) {
		ucipP = ucip;
		originNode = prop.getProperty("ORIGIN_NODE");
		oroginHost = prop.getProperty("ORIGIN_HOST");
		timeZone = prop.getProperty("TimeZone");
		logger.log(Level.INFO, "[UCIPController] UCIPController() Object Created.\n");

	}

	public String getResultcode() {
		return this.Resultcode;
	}

	public long getAmount() {
		return this.Amount;
	}

	public String getTime() {
		String formattedDate = null;
		Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
		Date date = calendar.getTime();

		DateFormat df = new SimpleDateFormat("yyyyMMdd hh:mm:ss");
		df.setTimeZone(TimeZone.getTimeZone("GMT"));
		formattedDate = df.format(date);

		return formattedDate.replace(' ', 'T') + timeZone;
	}

	public boolean testGetAccountDetails(String transId, String subsDirNum, Logger logger) {
		boolean flag = true;
		String result = ucipP.doGetBalanceAndDate(originNode, oroginHost, transId, getTime(), "", subsDirNum, "", "",
				logger);
		// logger.debug("\n\n#######################################################################################");
		logger.log(Level.INFO,
				"\n\n#######################################################################################");
		// logger.debug("Result from UCIP: " + result);
		logger.log(Level.INFO, "Result from UCIP: " + result);
		// logger.debug("#######################################################################################\n\n");
		logger.log(Level.INFO,
				"#######################################################################################\n\n");

		if (result == null)
			flag = false;

		// Getting response code or fault code

		try {
			XMLParser xp = new XMLParser();
			String data = null;
			String balance = null;
			long balance_paisa = 0;
			String[] faultData = new String[2];
			if (result.contains("fault")) {
				flag = false;
				faultData = xp.parseFaultCode(result, "faultCode", logger);
				logger.log(Level.INFO, "Fault code sent from AIR. ");
				logger.log(Level.INFO, "Faultcode :: " + faultData[0]);
				logger.log(Level.INFO, "FaultString :: " + faultData[1]);
			} else {
				data = xp.parseCode(result, "responseCode", logger);
				this.Resultcode = data;
				if (data.equalsIgnoreCase("0"))
					logger.log(Level.INFO, "Request Completed successfully. ");
				else if (data.equalsIgnoreCase("1")) {
					// flag = false;
					logger.log(Level.INFO, "Returncode :: 1");
					logger.log(Level.INFO, "Ok but supervision period exceeded");
				} else if (data.equalsIgnoreCase("2")) {
					logger.log(Level.INFO, "Returncode :: 2");
					logger.log(Level.INFO, "Ok but service fee period exceeded");
				} else if (data.equalsIgnoreCase("100")) {
					flag = false;
					logger.log(Level.INFO, "Returncode :: 100");
					logger.log(Level.INFO, "Other Error");
				} else if (data.equalsIgnoreCase("102")) {
					flag = false;
					logger.log(Level.INFO, "Returncode :: 102");
					logger.log(Level.INFO, "Subscriber not found");
				} else if (data.equalsIgnoreCase("197")) {
					flag = false;
					logger.log(Level.INFO, "Returncode :: 197");
					logger.log(Level.INFO, "Periodic account management evaluation failed");
				} else if (data.equalsIgnoreCase("260")) {
					flag = false;
					logger.log(Level.INFO, "Returncode :: 260");
					logger.log(Level.INFO, "Capability not available");
				} else if (data.equalsIgnoreCase("999")) {
					flag = false;
					logger.log(Level.INFO, "Returncode :: 999");
					logger.log(Level.INFO, "Other Error No Retry");
				} else {
					flag = false;
				}
				if (data.equalsIgnoreCase("0") || data.equalsIgnoreCase("1") || data.equalsIgnoreCase("2")) {
					if (result.contains("accountValue1")) {
						balance = xp.GetBalance();
						if (balance == null || balance == "")
							balance_paisa = -1;
						else
							balance_paisa = Integer.parseInt(balance);
						this.Amount = balance_paisa;
					}
				}

			}

		} catch (Exception e) {
			logger.log(Level.SEVERE, "Error in ucip responce " + e.getMessage());
			try {
				Global.write(Global.dateFormatHourMinSec.format(new Date()) + Global.getStackTrace(e),
						"Exception in testGetAccountDetails");
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

		return flag;
	}

	public void testGetBalanceAndDate(String transId, String subsDirNum, Logger logger) {
		String result = ucipP.doGetBalanceAndDate(originNode, oroginHost, transId, getTime(), "", subsDirNum, "", "",
				logger);
		// logger.info("\n\n#######################################################################################");
		logger.log(Level.INFO,
				"\n\n#######################################################################################");
		// logger.info("Result from UCIP: " + result);
		logger.log(Level.INFO, "Result from UCIP: " + result);
		// logger.info("#######################################################################################\n\n");
		logger.log(Level.INFO,
				"#######################################################################################\n\n");
	}

	public boolean testUpdateBalanceAndDate(String transId, String subsDirNum, String amount, Logger logger) {
		boolean flag = true;
		String result = ucipP.doUpdateBalanceAndDate(originNode, oroginHost, transId, getTime(), subsDirNum, amount,
				logger);
		// logger.info("\n\n#######################################################################################");
		logger.log(Level.INFO,
				"\n\n#######################################################################################");
		// logger.info("Result from UCIP: " + result);
		logger.log(Level.INFO, "Result from UCIP: " + result);
		// logger.info("#######################################################################################\n\n");
		logger.log(Level.INFO,
				"#######################################################################################\n\n");

		if (result == null)
			flag = false;

		// Getting response code or fault code

		try {
			XMLParser xp = new XMLParser();
			String data = null;
			String[] faultData = new String[2];
			if (result.contains("fault")) {
				faultData = xp.parseFaultCode(result, "faultCode", logger);
				logger.log(Level.INFO, "Fault code sent from AIR. ");
				logger.log(Level.INFO, "Faultcode :: " + faultData[0]);
				logger.log(Level.INFO, "FaultString :: " + faultData[1]);
				flag = false;
			} else {
				data = xp.parseCode(result, "responseCode", logger);
				if (data.equalsIgnoreCase("0"))
					logger.log(Level.INFO, "Request Completed successfully. ");
				else if (data.equalsIgnoreCase("1")) {
					// flag = false;
					logger.log(Level.INFO, "Returncode :: 1");
					logger.log(Level.INFO, "Ok but supervision period exceeded");
				} else if (data.equalsIgnoreCase("2")) {
					logger.log(Level.INFO, "Returncode :: 2");
					logger.log(Level.INFO, "Ok but service fee period exceeded");
				} else if (data.equalsIgnoreCase("100")) {
					flag = false;
					logger.log(Level.INFO, "Returncode :: 100");
					logger.log(Level.INFO, "Other Error");
				} else if (data.equalsIgnoreCase("102")) {
					flag = false;
					logger.log(Level.INFO, "Returncode :: 102");
					logger.log(Level.INFO, "Subscriber not found");
				} else if (data.equalsIgnoreCase("197")) {
					flag = false;
					logger.log(Level.INFO, "Returncode :: 197");
					logger.log(Level.INFO, "Periodic account management evaluation failed");
				} else if (data.equalsIgnoreCase("260")) {
					flag = false;
					logger.log(Level.INFO, "Returncode :: 260");
					logger.log(Level.INFO, "Capability not available");
				} else if (data.equalsIgnoreCase("999")) {
					flag = false;
					logger.log(Level.INFO, "Returncode :: 999");
					logger.log(Level.INFO, "Other Error No Retry");
				} else {
					logger.log(Level.INFO, "Returncode :: " + data);
					flag = false;
				}

			}
		} catch (Exception e) {

		}

		return flag;
	}
}
