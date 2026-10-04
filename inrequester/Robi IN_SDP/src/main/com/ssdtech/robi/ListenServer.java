package com.ssdtech.robi;

/**
 * @author Md. Shahriar Kamal
 */
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;

class ListenServer {
	Server_Threaded source;
	ServerSocket listenSocket;
	Socket connection;

	private static ListenServer listenServer;
	private Logger logger = Logger.getLogger(ListenServer.class.getName());
	protected ExecutorService threadPool = Executors.newFixedThreadPool(1000);

	private ListenServer() {
		super();
	}

	public synchronized static ListenServer getInstance() {
		if (listenServer == null) {
			listenServer = new ListenServer();
		}
		return listenServer;
	}

	public Server_Threaded getSource() {
		return source;
	}

	public void setSource(Server_Threaded source) {
		this.source = source;
	}

	public void run() throws IOException {
		try {
			listenSocket = new ServerSocket(Global.LISTEN_PORT);
			logger.info(Global.dateFormatHourMinSec.format(new Date()) + " Request Server started:  listening on port "
					+ Global.LISTEN_PORT);
			this.runClient();
		} catch (Exception ex) {
			logger.error(Global.dateFormatHourMinSec.format(new Date()) + " Request Server Unable to listen on port "
					+ Global.LISTEN_PORT + "\n\n");
			System.exit(ResultCode.FATAL_EXIT_CODE);
		}
	}

	private void runClient() {
		while (true) {
			try {
				connection = listenSocket.accept();
				this.threadPool.execute(new HandleRequest(connection, source));
			} catch (IOException ex) {
				try {
					connection.close();
					connection = null;

				} catch (IOException e) {
					e.printStackTrace();
				}
			} catch (Exception ex) {

			}
		}
	}
}