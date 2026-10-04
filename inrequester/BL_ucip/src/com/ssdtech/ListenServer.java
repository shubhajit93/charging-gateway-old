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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class ListenServer 
{
	Server_Threaded source;
	ServerSocket listenSocket;
	int port1;
	Socket connection;
	static RateConfigurations rateTemp;
	
	private static ListenServer listenServer;
	protected ExecutorService threadPool;

	private ListenServer() {	
		super();
		Integer noOfThreads = Integer.valueOf(Global.prop.getProperty("NoOfThreads"));
		this.threadPool = Executors.newFixedThreadPool(noOfThreads);
		System.out.println("Maximum number of active threads: " + noOfThreads);
	}
	
	public synchronized static ListenServer getInstance()
	{
		if(listenServer == null)
			{
				listenServer  = new ListenServer();
				//rateTemp = new RateConfigurations();
				
			}

		return listenServer;
	}

	public Server_Threaded getSource() {
		return source;
	}
	public void setSource(Server_Threaded source) {
		this.source = source;
	}
	
	public void run()    throws IOException{
		try{
			listenSocket = new ServerSocket ( Integer.parseInt(Global.port ));				
			System.out.println(Global.dateFormatHourMinSec.format(new Date())+ " Request Server started:  listening on port " + Global.port);
			Global.write(Global.dateFormatHourMinSec.format(new Date())+ " Request Server started:  listening on port " + Global.port, "Inside run");
			this.runClient();
		}
		catch(Exception ex)
		{
			    ex.printStackTrace();
		}
	}
	private void runClient()
	{
		while(true)
		{
			try 
			{
				connection = listenSocket.accept();			
				this.threadPool.execute(new HandleRequest(connection, source));				
			}
			catch (IOException ex) 
			{
				try {					
					connection.close();
					connection = null;
					Global.write(Global.dateFormatHourMinSec.format(new Date())+Global.getStackTrace(ex), "exception in request accept");
					
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			catch (Exception ex) {
				ex.printStackTrace();
				try
				{
					Global.write(Global.dateFormatHourMinSec.format(new Date())+Global.getStackTrace(ex), "exception in request accept");
				}
				catch(Exception ex1)
				{
					ex1.printStackTrace();
				}
				
				
				
			}
		}
	}

}  