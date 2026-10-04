package com.ssdtech;

import java.io.File;
import java.io.FileInputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import com.ericsson.ucip.UCIPConnector;
import com.ericsson.ucip.UCIPController;
import com.ericsson.ucip.UCIPProcessor;

public class AIRGetBalance 
{ 
	
	
		// TODO Auto-generated method stub

	    private String projectName;
	    private DateFormat dateValue;
	    private String timeStampValue;
	    private String logFileName;
	    private Logger logger ;
	    private FileHandler fh;
	    private Integer Number;
	    private String Msisdn;
		public AIRGetBalance(String Name,Integer number,String msisdn)
		{   
		   
			 projectName = Name;
			 dateValue = new SimpleDateFormat("yyyyMMddHHmmss");
			 timeStampValue = dateValue.format(Calendar.getInstance().getTime());
			 logFileName = projectName+timeStampValue+".log";
		     logger = Logger.getLogger(projectName+timeStampValue);
			 fh = null ; 
			 this.Number=number;
			 this.Msisdn=msisdn;
		}
		public String RequestToGetBalance(String Msisdn)
		{   
			String Responce ="";
			String Wrong_responce="invalid|-1";
			try
	    	{   
				if(Global.dummycode.equals("1"))
				{
					fh=new FileHandler(Global.LogDirectory+logFileName, true);
					logger.addHandler(fh);
				    logger.setLevel(Level.ALL);
				    SimpleFormatter formatter = new SimpleFormatter();
				    fh.setFormatter(formatter);  
				}
				else
				  logger.setLevel(Level.OFF);  
	    	}
	    	catch (Exception ex)
	    	{
	    		
	    		ex.printStackTrace();
	    		try
	    		{   
	    			if(fh!=null)
	    			fh.close();
	    			Global.write(Global.dateFormatHourMinSec.format(new Date())+Global.getStackTrace(ex),"Exception in Log file");
					Global.logForNumber(Msisdn,Global.dateFormatHourMinSec.format(new Date())+Global.getStackTrace(ex),"Exception in Log file");
	    		}
	    		catch (Exception e1)
	    		{
	    			e1.printStackTrace();
	    		}
	    		
	    	}
			
		
			Properties prop = Global.prop;
		
			
			
			UCIPConnector ucipConnect = null;
			UCIPProcessor ucipProcess = null;
			UCIPController ucipControl = null;
			
			try
			{
				ucipConnect = new UCIPConnector(prop,logger,this.Number,this.Msisdn);
				logger.log(Level.INFO, "[MAIN] UCIPConnector created.\n");
				ucipProcess = new UCIPProcessor(ucipConnect,prop,logger);
				logger.log(Level.INFO, "[MAIN] UCIPProcessor created.\n");
				ucipControl = new UCIPController(ucipProcess,prop,logger);
				logger.log(Level.INFO, "[MAIN] UCIPController created.\n");
			}
			catch (Exception e)
			{
				e.printStackTrace();
				try
				{
					Global.write(Global.dateFormatHourMinSec.format(new Date())+Global.getStackTrace(e),"Exception in Ucip Connector");
					Global.logForNumber(Msisdn,Global.dateFormatHourMinSec.format(new Date())+Global.getStackTrace(e),"Exception in Ucip Connector");
				}
				catch(Exception ex)
				{
					ex.printStackTrace();
				}
			}
			
			String tsValue = dateValue.format(Calendar.getInstance().getTime());
			String transactionId = tsValue+"112233";
			//logger.log(Level.INFO, "[MAIN] INFO:: Processing contract " + co_id+ " DirNum :: "+dirNum);
			//boolean flag = ucipControl.testGetAccountDetails(transactionId, "60187897891", logger);
			boolean flag = ucipControl.testGetAccountDetails(transactionId,Msisdn, logger);
			if (flag)
				logger.log(Level.INFO, "[MAIN] Request completed.\n");
			else
				logger.log(Level.INFO, "[MAIN] Request not completed.\n");
			String ResultCode = ucipControl.getResultcode();
			long Amount = ucipControl.getAmount(); 
			if(Amount !=-1) Amount=Amount*10000;
			if(ResultCode==null || ResultCode =="") Responce="invalid|";
			else if(ResultCode.equals("0") || ResultCode.equals("1")  || ResultCode.equals("2")  ) Responce="prepaid|"; 
			else if(ResultCode.equals("102")) Responce= Global.PostpaidPackage+"|";
			else Responce="invalid|";  
			Responce+=Long.toString(Amount);
			 
			if (null != ucipConnect)
			{
				ucipConnect.disConnect();
				logger.log(Level.INFO, "[MAIN] UCIPConnector disconnected.\n");
			}
			
			
			logger.log(Level.INFO, "[MAIN] Ending Execution.\n");
			try
			{   
				if(fh!=null)
				 fh.close();
			}
			catch (Exception e1)
			{ 
				e1.printStackTrace();
				try
				{
					Global.write(Global.dateFormatHourMinSec.format(new Date())+Global.getStackTrace(e1),"Exception in Log file");
					Global.logForNumber(Msisdn,Global.dateFormatHourMinSec.format(new Date())+Global.getStackTrace(e1),"Exception in Log file");
				}
				catch(Exception ex)
				{
					ex.printStackTrace();
				}
			}
			return Responce; 
		}
}
		
			




