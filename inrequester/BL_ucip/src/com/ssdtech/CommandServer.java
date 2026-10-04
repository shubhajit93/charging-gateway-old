package com.ssdtech;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.StringTokenizer;

//import com.ericsson.pps.diameter.api.base.AvpDataException;

public class CommandServer extends Thread
{
	
	Server_Threaded serverThreaded;
	ServerSocket listenSocket;
	int port1;
	Socket connection;
	InputStream inStream;
	DataInputStream inDataStream;
	DataOutputStream outDataStream;
	PrintWriter     pw   = null;
	BufferedReader  br   = null;
	String message;
	String result;
	CommandServer ( Server_Threaded obj)
	{
		super ();
		serverThreaded = (Server_Threaded ) obj;		
	}
	@Override
	public void run() {
		// TODO Auto-generated method stub
		//super.run();
		try{
			listenSocket = new ServerSocket ( Integer.parseInt(Global.commandPort ));				
			System.out.println(Global.dateFormatHourMinSec.format(new Date())+ " Command Server started:  listening on port " + Global.commandPort);
			Global.write(Global.dateFormatHourMinSec.format(new Date())+ " Command Server started:  listening on port " + Global.commandPort, "Inside run");
		}
		catch(Exception ex)
		{
			System.out.println(Global.dateFormatHourMinSec.format(new Date())+ " Command Server Unable to listen on port " + Global.commandPort + "\n\n");
			try {
                Global.write(Global.dateFormatHourMinSec.format(new Date())+" Command Server Unable to open Server Socket. Port No: "+Global.commandPort,"run");
            } catch (Exception el) {
                System.out.println("Log Write Fail!!");
            }                
		}
	
		try {
			connection = listenSocket.accept();
			br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			pw = new PrintWriter(connection.getOutputStream(), true);
		}
		catch (IOException ex) {
			try {
				br.close();
				pw.close();
				connection.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		while (true)
		{
			try{					
				message = br.readLine();
				//System.out.print(message);
				if(message==null)
				{   
					connection.close();
					br.close();
					pw.close();
					connection = listenSocket.accept();
					br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
					pw = new PrintWriter(connection.getOutputStream(), true);
					continue;
				}
			}
			catch(Exception ex)
			{
				ex.printStackTrace();
				try{						
					connection = listenSocket.accept();
					br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
					pw = new PrintWriter(connection.getOutputStream(), true);
					message = br.readLine();
				}
				catch(Exception e)
				{
					e.printStackTrace();
					try{
						 Global.write(Global.dateFormatHourMinSec.format(new Date())+e.getStackTrace(),"run Accept Connection in Command Server.");
					}
					catch(Exception ex1)
					{
						System.out.println(Global.dateFormatHourMinSec.format(new Date())+" Log Write Fail in accept client of Command Server");
					}
				}
			}
			try
			{
				if(message.equals("hi"))
				{
					result="hello";
				}
				else if(message.equals("reload"))
				{   
					int line_number=0,line_number2 = 0;
					try
					{
						try
						{ 
							System.out.println(Global.dateFormatHourMinSec.format(new Date())+"RateConfig text file try to read");
							Global.write(Global.dateFormatHourMinSec.format(new Date())+"RateConfig Text file try to read","Commandserver rum method");
							line_number=Global.configure();
							
							System.out.println(Global.dateFormatHourMinSec.format(new Date())+"RateConfig text file read successfull");
							Global.write(Global.dateFormatHourMinSec.format(new Date())+"RateConfig text file read successfull","Commandserver rum method");
							System.out.println(Global.dateFormatHourMinSec.format(new Date())+"DmConfig text file try to read");
							Global.write(Global.dateFormatHourMinSec.format(new Date())+"DmConfig Text file try to read","Commandserver rum method");
							Global.dmconfigure();
							line_number2=Global.numberseriesconfig();
							System.out.println(Global.dateFormatHourMinSec.format(new Date())+"DmConfig text file read successfull");
							Global.write(Global.dateFormatHourMinSec.format(new Date())+"DmConfig text file read successfull","Commandserver rum method");
						}
						catch(Exception ex)
						{
							System.out.println(Global.dateFormatHourMinSec.format(new Date())+"Config Text file can not be read");
							Global.write(Global.dateFormatHourMinSec.format(new Date())+"Config Text file can not be read","Commandserver rum method");
						}
					}
					catch(Exception ex)
					{
						ex.printStackTrace();
					}
					if(line_number!=0) result="problem in rateconfig file. See log file. Line number "+line_number;
					else if(line_number2!=0) result="problem in numberseries file. See log file. Line number "+line_number2; 
					else	
					result="Configuration Reloaded.";
				}
				else if(message.equals("stop"))
				{   
					result="Diameter stop.";
					pw.println(result);
					serverThreaded.stop();
					
				}			 
				else
				{
					result = "Not proper command.";
				}
				 
				pw.println(result);
				try {
	               Global.write(Global.dateFormatHourMinSec.format(new Date())+" message [" + message + "] Result:[" + result + "]","In Command Server");                		
	             
	             } catch (Exception ex) {
	             	System.out.println(Global.dateFormatHourMinSec.format(new Date())+" Log Write Fail!!!");
	             }
				result ="";
			}
			catch(Exception ex)
			{
				try
				{
					connection.close();
					br.close();
					pw.close();
					connection = listenSocket.accept();
					br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
					pw = new PrintWriter(connection.getOutputStream(), true);
					continue;
				}
				catch(Exception ex1)
				{
					ex1.printStackTrace();
				}
				
			}
			
	}
}
}
