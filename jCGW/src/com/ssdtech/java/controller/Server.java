package com.ssdtech.java.controller;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ssdtech.java.model.applicationproperties.ApplicationProperties;

public class Server extends Thread {
	private static final Logger logger = LogManager.getLogger(Server.class.getName());
	
    private ServerSocket serverSocket;
	private ExecutorService threadPool;
	
	private static Server server;
	
	private Server(int port) throws IOException {
		this.serverSocket = new ServerSocket(port);

		this.threadPool = Executors.newFixedThreadPool(ApplicationProperties.getServerSpecificConfiguration().noOfThreads);
	}
	
	public static synchronized Server getInstance() throws IOException {
		if (server == null) {
			server = new Server(ApplicationProperties.getServerSpecificConfiguration().listeningPort);
		}
		
		return server;
	}
	
	@Override
	public void run() {
		System.out.println("Server started at port [" + this.serverSocket.getLocalPort() + "]. Waiting for Client!!");
		
		while (!this.serverSocket.isClosed()) {
			try {
				Socket clientSocket = this.serverSocket.accept();
				System.out.println("New Request is accepted for client socket: "+clientSocket);
				this.threadPool.execute(new RequestHandler(clientSocket));
			} catch (IOException | RejectedExecutionException e) {
				logger.error("Exception in Server's client accept processing.", e);
			}
		}
	}
	
	public boolean shutdown() {
		try {
			this.serverSocket.close();
		    RequestHandler.stopProcessingFurtherRequests();
			this.threadPool.shutdown();
			this.threadPool.awaitTermination(1, TimeUnit.DAYS); // This is just a BIG value to treat as INFINITY.
			return true;
		} catch (IOException | InterruptedException e) {}
		
		return false;
	}
}
