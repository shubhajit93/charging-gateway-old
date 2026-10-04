package com.ssdtech.java.controller.maintenance;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class CommandServer extends Thread {
	private ServerSocket commandServer;
	
	public CommandServer(int port) throws IOException {
		this.commandServer = new ServerSocket(port);
	}
	
	@Override
	public void run() {
		System.out.println("Command Server started at port [" + this.commandServer.getLocalPort() + "]. Waiting for Command!!");
		
		while (true) {
			try {
				Socket socket = this.commandServer.accept();
				new CommandRequestHandler(socket).start();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
