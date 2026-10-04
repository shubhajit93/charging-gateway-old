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
public static int ThreadCount=0;
public static String dummycode="0";
public static String log="0";
public static int logfornumber=0;
public static String MsisdnList=""; 
public static String[] MsisdnArray;
public static String LogDirectory="";
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
/** my OwnProductName  */
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

public static String UcipLog="";
public static String UcipConfigFile ="";
public static String InsuffientBalanceError = "";
public static String ServiceContextId ="SCAP_V.2.0@ericsson.com";
public static long ServiceIdentifier =20;
public static int CdrLength =16;
public static HashMap <String,String> SPID_MAP ;
public static HashMap <String,Integer> Numberseries_MAP ;
public static boolean NumberSeriesEnable=false;

public static String PostpaidPackage="";
public static int count=1;
public static SimpleDateFormat dateFormatHourMinSec=  new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS"); // to add log time. 
public static SimpleDateFormat dateFormatHour=  new SimpleDateFormat("yyyy-MM-dd-HH");
public static Properties prop;

/** Timeout for event charging **/
public static boolean defaultTxTimeoutEnable;
public static int txTimeout;

/** Result codes log related **/
public static boolean enableResultCodeWiseLog;
public static long[] resultCodes;
public static Map<Long, CustomizedLogFileWriter> resultCodesWriterMapping; 


public static void write(String Logmsg,String callingfuncname) throws IOException  {	
		File file = new File(LogDirectory+dateFormatHour.format(new Date())+".txt");
		if(Global.log.equals("1"))
		{
		      BufferedWriter bw = null;
		      try {
		         bw = new BufferedWriter(new FileWriter(file, true));
			 bw.write(Logmsg+". Function: "+callingfuncname);			
			 bw.newLine();
			 bw.flush();
		      } catch (IOException ioe) {
			 ioe.printStackTrace();
		      } finally {                       
			 if (bw != null) try {
			    bw.close();
			 } catch (IOException ioe2) {
			    // just ignore it
			 }
		      } 
		}
	  }

public static void logForNumber(String msisdn,String Logmsg,String callingfuncname) throws IOException
{    
	List temp_MsisdnList = Arrays.asList(Global.MsisdnArray);

	if(Global.logfornumber ==1 && Global.MsisdnArray.length >0 && temp_MsisdnList.contains(msisdn))
	{     
		  
	      File file = new File(LogDirectory+dateFormatHour.format(new Date())+"_"+msisdn+".txt");
	      BufferedWriter bw = null;
	      try 
	      {
	         bw = new BufferedWriter(new FileWriter(file, true));
	         bw.write(Logmsg+". Function: "+callingfuncname);			
	         bw.newLine();
	         bw.flush();
	      }
	      catch (IOException ioe) 
	      {
		     ioe.printStackTrace();
	      }
	      finally 
	      {                       
	    	  if (bw != null) 
	    	  try 
	    	  { 
		         bw.close();
		      }
	          catch (IOException ioe2)
	         {
		    // just ignore it
		     }
	     } 
	
     }
}

public static void loadProperties(){
	
	
	FileInputStream fin = null;
	prop = new Properties();
	
	try
	{
		fin = new FileInputStream(new File(Global.UcipConfigFile));
		prop.load(fin);
		setProperties();
	}
	catch (Exception ex1)
	{   
		try
		{
			Global.write(Global.dateFormatHourMinSec.format(new Date())+Global.getStackTrace(ex1),"Exception in property file");
				}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		ex1.printStackTrace();
		
	}
	finally
	{
		if(null != fin)
		{
			try
			{
				fin.close();
			}
			catch (Exception e)
			{
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
	if(rawResultCodes.length() == 0) {
		resultCodes = new long[0];
	} else {
		splittedResultCodes = rawResultCodes.split(",");
		resultCodes = new long[splittedResultCodes.length];
	}

	for(int index = 0; index < resultCodes.length; index++) {
		resultCodes[index] = Long.parseLong(splittedResultCodes[index].trim());
	}
	resultCodesWriterMapping = new HashMap<Long, CustomizedLogFileWriter>();
}

public static void initializeResultCodesLogFileWriter() {
	if(enableResultCodeWiseLog) {
		File parentDirectory = new File("./ResultCodesLog");
		if(!parentDirectory.exists()) {
			parentDirectory.mkdir();
		}
		
		for(int index = 0; index < resultCodes.length; index++) {
			String directory = "./ResultCodesLog/" + resultCodes[index];
			File resultCodeDirectory = new File(directory);
			if(!resultCodeDirectory.exists()) {
				resultCodeDirectory.mkdir();
			}
			CustomizedLogFileWriter clfw = new CustomizedLogFileWriter(directory + "/log");
			resultCodesWriterMapping.put(resultCodes[index], clfw);
		}
	}
}

public static int configure()
{
	
	File fFile = new File("RateConfig.txt"); 	
	int i=0;
	int j=0;
	boolean error=true;
	
	Global.SPID_MAP = new HashMap<String,String> ();
	Scanner scanner = null;
    try 
    {
    	  scanner = new Scanner(new FileReader(fFile)); 
	      while (scanner.hasNextLine())
	      {   
	    	  i++;
	          StringBuffer bno_operatorid = new StringBuffer();
	          bno_operatorid.append(scanner.nextLine());
	          if(bno_operatorid.toString().contains(">>"))
	          {
	        	  String paramlist[]= bno_operatorid.toString().split(">>");
	        	  if(paramlist.length!=7)
	        	  {   
	        		  if(error==true) 
	        		  {
	        			  error=false;
	        			  j=i;
	        		  }
	        		  System.out.println("Error in rateconfig file. Number of parameters should be 6. Line number "+i);
	        		  Global.write("Error in rateconfig file. Number of parameters should be 6. Line number "+i,"config function");
	        		  continue;
	        	  }
	          }
	          else
	          {   
	        	  if(error==true) 
        		  {
        			  error=false;
        			  j=i;
        		  }
	        	  System.out.println("Error in rateconfig file. There is no splitting charecter(>>). Line number "+i);
	        	  Global.write("Error in rateconfig file. There is no splitting charecter(>>). Line number "+i,"config function");
	        	  continue;
	          }
	         
			  StringTokenizer st = new StringTokenizer(bno_operatorid.toString(), ">>");		
			  String rate_name = st.nextToken();
			  String  spcode = st.nextToken();
			  String param1=st.nextToken();
			  String param2=st.nextToken();
			  String param3=st.nextToken();
			  String param4=st.nextToken();
			  String param5=st.nextToken();
			  String params=spcode+">>"+param1+">>"+param2+">>"+param3+">>"+param4+">>"+param5;
			  Global.SPID_MAP.put(rate_name,params);   
	      }
	      
	      return j;
	  
     }
    catch(Exception ex)
    {   j=i;
    	System.out.println("Error in rateconfig file. There is no splitting charecter(>>). Line number "+i);
    	Global.write("Error in rateconfig file. There is no splitting charecter(>>). Line number "+i,"config function");
    	ex.printStackTrace();
    }
    finally
    {
    	scanner.close();
    	return j;
    }
    
}

public static void dmconfigure()
{
	File file = new File("Dmconfig.txt");
	BufferedReader reader = null;
	FileReader fr;
	try
	{
    	    fr = new FileReader(file);
    		reader = new BufferedReader(fr);
    		Global.Outdialdata = reader.readLine();
    		//System.out.println(Outdialdata);
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
    		//System.out.println(port);
    		Global.dummycode = reader.readLine();
    		//System.out.println(dummycode);
    		Global.LogDirectory = reader.readLine();
    		Global.log = reader.readLine();
    		//System.out.println(log);
    		Global.TimeZone = reader.readLine();
    		Global.commandPort = reader.readLine();
    		Global.logfornumber = Integer.parseInt(reader.readLine());
    		Global.MsisdnList = reader.readLine();
    		if(MsisdnList.contains(","))
    		Global.MsisdnArray=Global.MsisdnList.split(",");
    		Global.InsuffientBalanceError=reader.readLine();
    		Global.UcipConfigFile=reader.readLine();
    		Global.UcipLog=reader.readLine();
    		Global.ServiceContextId = reader.readLine();
    		Global.ServiceIdentifier = Integer.parseInt(reader.readLine());
    		Global.CdrLength =Integer.parseInt(reader.readLine());
    		Global.PostpaidPackage=reader.readLine();
    }
    catch(Exception ex)
    {
    	ex.printStackTrace();
    }
    finally
    {
    	try {
			reader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
}

public static int numberseriesconfig()
{
	File file = new File("numberseries.txt");
	Global.Numberseries_MAP=new HashMap<String,Integer> ();
	Scanner scan = null;
	boolean error=true;
	int i=0;
	int j=0;
	try
	{
		scan= new Scanner(new FileReader(file));
		while(scan.hasNextLine())
		{   
			i++;
			String line=scan.nextLine();
			Integer number ;
			if(line.contains(">>"))
			{
				String linesplit[] = line.split(">>");
				if( linesplit.length!=2)
				{    
					 if(error==true) 
	        		  {
	        			  error=false;
	        			  j=i;
	        		  }
					 System.out.println("Error in numberseries file. Line number "+i);
					 Global.write("Error in numberseries file. Line number "+i,"numberseriesconfig");
		        	  continue;
				}
				if(linesplit[0]==null || linesplit[0]=="" || linesplit[0]=="Not Mapped" )
				{    
					 if(error==true) 
	        		  {
	        			  error=false;
	        			  j=i;
	        		  }
					 System.out.println("Error in numberseries file. Line number "+i);
					 Global.write("Error in numberseries file. Line number "+i,"numberseriesconfig");
		        	  continue;
				}
				if(linesplit[1]==null || linesplit[1]=="" || linesplit[1]=="Not Mapped" )
				{    
					 if(error==true) 
	        		  {
	        			  error=false;
	        			  j=i;
	        		  }
					 System.out.println("Error in numberseries file. Line number "+i);
					 Global.write("Error in numberseries file. Line number "+i,"numberseriesconfig");
		        	  continue;
				}
				if(linesplit[1].toUpperCase().startsWith("ASDP")) number = new Integer(0);
				else if(linesplit[1].toUpperCase().startsWith("KSDP"))  number = new Integer(1);
				else if(linesplit[1].toUpperCase().startsWith("CSDP"))   number = new Integer(2);
				else
				{    
					 if(error==true) 
	        		  {
	        			  error=false;
	        			  j=i;
	        		  }
					 System.out.println("Error in numberseries file. Line number "+i);
					 Global.write("Error in numberseries file. Line number "+i,"numberseriesconfig");
		        	 continue;
				}
				Numberseries_MAP.put(linesplit[0],number);
				
			}
			else
			{    
				 if(error==true) 
       		     {
       			    error=false;
       			    j=i;
       		     }
				  System.out.println("Error in numberseries file. Line number "+i);
				  Global.write("Error in numberseries file. There is no splitting charecter(>>). Line number "+i,"numberseriesconfig");
	        	  continue;
			}
			
		}
		
		return j;
	}
	catch(Exception ex)
	{
		ex.printStackTrace();
		return j;
	}
}


public static String getStackTrace(Throwable throwable) {
	StringWriter writer = new StringWriter();
	PrintWriter printWriter = new PrintWriter(writer);
	throwable.printStackTrace(printWriter);
	return writer.toString();
}
}
