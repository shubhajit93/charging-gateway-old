package com.ssdtech.main.java.controller;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;

import com.ssdtech.main.java.model.AppConstant;

public class Listener {
	private static Listener listener;
	private BlockingQueue<Socket> tasks;
	
	private Listener(BlockingQueue<Socket> tasks) {
		this.tasks = tasks;
	}
	
	public void startServer() {
		ServerSocket server = null;
		try {
			server = new ServerSocket(AppConstant.port);
			System.out.println(AppConstant.ANSI_GREEN 
					         + "Started server to accept request on port: " + AppConstant.port 
					         + AppConstant.ANSI_RESET);
		} catch (IOException exception) {
			System.out.println(AppConstant.ANSI_RED + "Server can not be started;" + AppConstant.ANSI_RESET
					          + " Cause: " + exception.getMessage());
		}
		
		while(true) {
			try {
				Socket clientSocket = server.accept();
		
				try {
					this.tasks.put(clientSocket);
				} catch (InterruptedException exception) {
					exception.printStackTrace();
				}				
			} catch (IOException exception) {
				exception.printStackTrace();
			}
		}
	}
	
	public static Listener getInstance(BlockingQueue<Socket> tasks) {
		if(listener == null) {
			synchronized(Listener.class) {
				if(listener == null) {
					listener = new Listener(tasks);
				}
			}
		}
		return listener;
	}
}
