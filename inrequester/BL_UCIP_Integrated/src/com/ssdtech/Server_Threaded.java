package com.ssdtech;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Date;

import com.ssdtech.cgwcommon.INRequesterInitializer;

public class Server_Threaded  implements INRequesterInitializer {

	@Override
	public void initialize() {

		File file = new File("ConfigurationFiles/in/Dmconfig.txt");
		BufferedReader reader = null;
		try {
			FileReader fr = new FileReader(file);
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
			Global.MsisdnArray = Global.MsisdnList.split(",");
			Global.InsuffientBalanceError = reader.readLine();
			Global.UcipConfigFile = reader.readLine();
			Global.UcipLog = reader.readLine();
			Global.ServiceContextId = reader.readLine();
			Global.ServiceIdentifier = Integer.parseInt(reader.readLine());
			Global.CdrLength = Integer.parseInt(reader.readLine());
			Global.PostpaidPackage = reader.readLine();
			if (Global.myURI.contains("|"))
				Global.NumberSeriesEnable = true;

		} catch (FileNotFoundException e) {
			e.printStackTrace();
			try {
				Global.write(Global.getStackTrace(e), "Exception in serverthreader");
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		} catch (IOException e) {
			e.printStackTrace();
			try {
				Global.write(Global.getStackTrace(e), "Exception in serverthreader");
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		} finally {
			try {
				if (reader != null) {
					reader.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				try {
					System.out.println(
							Global.dateFormatHourMinSec.format(new Date()) + "RateConfig text file try to read");
					Global.write(Global.dateFormatHourMinSec.format(new Date()) + "RateConfig Text file try to read",
							"ServerThreaded Contructor");
					Global.configure();
					Global.numberseriesconfig();
					Global.loadProperties();
					Global.loadResultCodeProperties();
					Global.initializeResultCodesLogFileWriter();
					System.out.println(
							Global.dateFormatHourMinSec.format(new Date()) + "RateConfig text file read successfull");
					Global.write(
							Global.dateFormatHourMinSec.format(new Date()) + "RateConfig text file read successfull",
							"ServerThreaded Contructor");
				} catch (Exception ex) {
					System.out.println(
							Global.dateFormatHourMinSec.format(new Date()) + "RateConfig Text file can not be read");
					Global.write(
							Global.dateFormatHourMinSec.format(new Date()) + "RateConfig Text file can not be read",
							"ServerThreaded Contructor");
					try {
						Global.write(Global.getStackTrace(ex), "Exception in serverthreader");
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			} catch (Exception ex) {
				ex.printStackTrace();
				try {
					Global.write(Global.getStackTrace(ex), "Exception in serverthreader");
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}

			try {
				EventCharging temp_instance = new EventCharging();
			} catch (Exception ex) {
				ex.printStackTrace();
				try {
					Global.write(Global.getStackTrace(ex), "Exception in open Eventcharging");
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		}

	}
}
