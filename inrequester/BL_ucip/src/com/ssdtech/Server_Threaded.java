package com.ssdtech;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.Map.Entry;



import com.ericsson.pps.diameter.rfcapi.base.avp.Avp;
import com.ericsson.pps.diameter.rfcapi.base.avp.avpdatatypes.OctetString;
import com.ericsson.bean.*;
//import com.ericsson.pps.diameter.api.base.PeerState;
import com.ericsson.pps.diameter.rfcapi.base.impl.statemachine.PeerState;


public class Server_Threaded {

	String host;
	ListenServer listen;
	CommandServer command;
	
	 
	StateChange sc;
	public static String myMSISDN = "";
	CallGarbageCollector callGarbageCollector;
	private static File fFile;
    private static HashMap  <String,RateConfigurations> rateConf;
	private static RateConfigurations rateInfo;
	public Server_Threaded ()
	{
		
		File file = new File("Dmconfig.txt");
		BufferedReader reader = null;
		try
		{  
			FileReader fr = new FileReader(file);
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
			Global.MsisdnArray=Global.MsisdnList.split(",");
			Global.InsuffientBalanceError=reader.readLine();
			Global.UcipConfigFile=reader.readLine();
			Global.UcipLog=reader.readLine();
			Global.ServiceContextId = reader.readLine();
			Global.ServiceIdentifier = Integer.parseInt(reader.readLine());
			Global.CdrLength =Integer.parseInt(reader.readLine());
			Global.PostpaidPackage=reader.readLine();
			if(Global.myURI.contains("|"))
				Global.NumberSeriesEnable=true;

		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
			try {
				Global.write(Global.getStackTrace(e), "Exception in serverthreader");
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		} catch (IOException e)
		{
			e.printStackTrace();
			try {
				Global.write(Global.getStackTrace(e), "Exception in serverthreader");
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		} 
		finally
		{
			try
			{
				if (reader != null)
				{
					reader.close();
				}
			} 
			catch (IOException e)
			{
				e.printStackTrace();
			}
			try
			{
				try
				{ 
					System.out.println(Global.dateFormatHourMinSec.format(new Date())+"RateConfig text file try to read");
					Global.write(Global.dateFormatHourMinSec.format(new Date())+"RateConfig Text file try to read","ServerThreaded Contructor");
					Global.configure();
					Global.numberseriesconfig();
					Global.loadProperties();
					Global.loadResultCodeProperties();
					Global.initializeResultCodesLogFileWriter();
					System.out.println(Global.dateFormatHourMinSec.format(new Date())+"RateConfig text file read successfull");
					Global.write(Global.dateFormatHourMinSec.format(new Date())+"RateConfig text file read successfull","ServerThreaded Contructor");
				}
				catch(Exception ex)
				{
					System.out.println(Global.dateFormatHourMinSec.format(new Date())+"RateConfig Text file can not be read");
					Global.write(Global.dateFormatHourMinSec.format(new Date())+"RateConfig Text file can not be read","ServerThreaded Contructor");
					try {
						Global.write(Global.getStackTrace(ex), "Exception in serverthreader");
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			}
			catch(Exception ex)
			{
				ex.printStackTrace();
				try {
					Global.write(Global.getStackTrace(ex), "Exception in serverthreader");
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
			
			try
			{
				EventCharging temp_instance= new EventCharging();
			}
			catch(Exception ex)
			{
				ex.printStackTrace();
				try {
					Global.write(Global.getStackTrace(ex), "Exception in open Eventcharging");
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
			try
			{
				this.listen = ListenServer.getInstance();
            	System.out.println("ListenServer and CommandServer  Starting.......");
				try{
					Global.write(Global.dateFormatHourMinSec.format(new Date())+" Listener Starting......." ,"ServerThreaded Contructor");
				}
				catch(Exception ex1)
				{
					ex1.printStackTrace();
				}
				try {
					/*
					this.command =  HandleCommandRequest.getInstance();
					this.command.run();
				this.command.setSource(this);
				*/
					this.command = new CommandServer(this);
					this.command.start();
					//main.simulPrinter = new SimulationPrinter();
					//main.simulPrinter.start();
					this.listen.run();							
					this.listen.setSource(this);
						
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			catch(Exception lisEx)
			{
				lisEx.printStackTrace();
				try {
					Global.write(Global.getStackTrace(lisEx), "Exception in serverthreader");
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		}
		
		
	}  // end constructor
	protected void stop()
	{
		System.exit(0);
		
	}
	/**
	 * @throws Exception
	 */
	private void startListener()
	{
		try
		{
			callGarbageCollector = new CallGarbageCollector();
			callGarbageCollector.setPriority(Thread.MIN_PRIORITY);
			callGarbageCollector.start();
		}
		catch(Exception lisEx)
		{
			
		}
	}
	public static void main ( String [] args ) throws Exception
	{
            Server_Threaded server = new Server_Threaded ();           
	}  // end main

}  // end Server_Threaded


