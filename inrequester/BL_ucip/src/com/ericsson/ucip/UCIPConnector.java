package com.ericsson.ucip;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ericsson.util.XMLParser;
import com.ssdtech.Global;

public class UCIPConnector {
	private URL url = null;
	private HttpURLConnection conn = null;
	private XMLParser parser = new XMLParser();
	Properties prop_temp;
	
	//Log purpose.
	private String MSISDN;
	private String urlName;
	
	//initiate connection
	public UCIPConnector (Properties prop, Logger logger,Integer number,String Msisdn) 
			throws IOException {  
		try	{
			this.prop_temp=prop;
			String AIRurl=  prop.getProperty("AIRurl");
			String urlname;
			if(AIRurl.contains("|")) {
				String[] temparray=AIRurl.split("\\|");
				if((temparray.length-1)<=number) {
				    Global.write(Global.dateFormatHourMinSec.format(new Date())+"AIRurl configure value do not have appropreate value","From UCIPConnector Constructor ");
					urlname=temparray[1];
				} else {
				    urlname=temparray[number+1];
				}  
			} else {
				urlname=AIRurl;
			}
			
			//Set log purposed variable
			this.MSISDN = Msisdn;
			this.urlName = urlname;
			
			Global.write(Global.dateFormatHourMinSec.format(new Date())+"[Msisdn,Airurl] ["+Msisdn+","+urlname+"]","From UCIPConnector Constructor ");
			Global.logForNumber(Msisdn,Global.dateFormatHourMinSec.format(new Date())+"[Msisdn,Airurl] ["+Msisdn+","+urlname+"]","From UCIPConnector Constructor ");
			url = new URL(urlname);
	        conn = (HttpURLConnection) url.openConnection();       
	        conn.setDoInput(true);
	        conn.setDoOutput(true);
	        conn.setUseCaches(true);
	        conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type", "text/xml");
			conn.setConnectTimeout(5000);
			//conn.setRequestProperty("Content-Length", "1247");
			//conn.setRequestProperty("Host", prop.getProperty("HostKey"));
			conn.setRequestProperty("CacheControl", "no-cache");
			conn.setRequestProperty("Pragma", "no-cache");
			conn.setRequestProperty("User-Agent", prop.getProperty("UserAgent"));
			conn.setRequestProperty("Keep-Alive", "true");
			conn.setRequestProperty("Authorization", prop.getProperty("AuthorizationKey"));
			
			logger.log(Level.INFO, "[UCIPConnector] UCIPConnector Initiated.");
		} catch(Exception ex) {
			ex.printStackTrace();
			try {
				Global.write(Global.dateFormatHourMinSec.format(new Date())+Global.getStackTrace(ex),"Exception in Log file");
				Global.logForNumber(Msisdn,Global.dateFormatHourMinSec.format(new Date())+Global.getStackTrace(ex),"Exception in Log file");
			} catch(Exception ex1) {
				ex1.printStackTrace();
			}
		}
	}

	public String sendRequest(String xmlRequest, Logger logger) 
			throws IOException {   
		String responseStr = "";
		if(Global.dummycode.equals("1")) {
		    writeFile(xmlRequest,"request.xml");
		}
		
		logger.log(Level.INFO, "[UCIPConnector] sendRequest started.");
		conn.setRequestProperty("Content-Length",xmlRequest.getBytes().length + "");
		logger.log(Level.INFO, "[UCIPConnector] Content-Length set to ::"+xmlRequest.getBytes().length);
		logger.log(Level.INFO, "[UCIPConnector] Opening OutputStream.");
		
		boolean isRetryNeeded = false;
		int retryCounter = 0;
		String ANSI_RESET = "\u001B[0m";
		String ANSI_RED = "\u001B[31m";
		String ANSI_GREEN = "\u001B[32m";
		String ANSI_YELLOW = "\u001B[33m";	
		while(true) {
			try {
				OutputStream out = conn.getOutputStream();
				logger.log(Level.INFO, "[UCIPConnector] OutputStream successful.");
				if(isRetryNeeded) {
				    System.out.println(ANSI_GREEN 
				    		           + "Connection successfully established after trying " 
				                       + retryCounter + " time(s) [MSISDN: " + this.MSISDN
				                       + ", AIR URL: " + this.urlName + "]"
				                       + ANSI_RESET);	
				}
				
				out.write(xmlRequest.getBytes());
				out.flush();
				out.close();
				conn.getOutputStream().close();
				break;
			} catch(IOException exception) {
				logger.log(Level.INFO, "[UCIPConnector] Opening OutputStream - Unsuccessful. Retry.");
				System.out.println(ANSI_RED
						          + "Can not connect to AIR: " + this.urlName
						          + " for MSISDN: " + this.MSISDN
						          + ANSI_RESET);
				System.out.println(ANSI_YELLOW 
								+ "Trying to re establishing connection "
								+ "[MSISDN: " + this.MSISDN + ", AIR URL: " + this.urlName + "]"
								+ ANSI_RESET);
				isRetryNeeded = true;
				++retryCounter;
			}
		}

		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String temp;
		    while((temp=in.readLine())!=null) {   
		    	responseStr+=temp;
		    }
		    if(Global.dummycode.equals("1")) {
		        writeFile(responseStr,"response.xml");
		    }
		} catch(Exception e) {
		    e.printStackTrace();
		    try {
				Global.write(Global.dateFormatHourMinSec.format(new Date())+Global.getStackTrace(e),"Exception in Sending ucip message");
			} catch(Exception ex) {
				ex.printStackTrace();
			}
		}
		
		conn.getInputStream().close();
		return responseStr;
	}
	
	public void disConnect() {
		conn.disconnect();
	}
	
	public String writeAccountDetailResponse() {
		return null;
	}
	
	public String writeAccountBalanceResponse()	{
		return null;
	}

	public void writeFile(String yourXML,String name) {
	    BufferedWriter out=null;
		try {
			out = new BufferedWriter(new FileWriter(name));
		    try { 
		        out.write(yourXML);
		    } catch (Exception e) {
		        e.printStackTrace();
		    } finally {
		        out.close();
		    }
        } catch(Exception e1) {
			// TODO Auto-generated catch block
		    e1.printStackTrace();
	    }
	}
}