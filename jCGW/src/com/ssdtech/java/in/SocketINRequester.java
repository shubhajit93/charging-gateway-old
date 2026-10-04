package com.ssdtech.java.in;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Arrays;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ssdtech.cgwcommon.INRequester;

public class SocketINRequester implements INRequester {
	private static final Logger logger = LogManager.getLogger(SocketINRequester.class.getName());
	
	private static final int MAX_FRAME_LENGTH = 1500;
	private static final int CONNECT_TIMEOUT = 5000;
	private static final String PAYLOAD_TERMINATOR = "\r\n";
    private Socket socket;
    private int numberOfRetry;
    
    public SocketINRequester(Socket socket, int numberOfRetry) {
    	this.socket = socket;
    	this.numberOfRetry = numberOfRetry;
    }
    
	@Override
	public String chargeSubscriber(String chargingRequest) {
		return this.sendPayloadWithRetry(chargingRequest);
	}

	@Override
	public String executeCommand(String command) {
		return "Not implemented.";
	}

	@Override
	public String queryBalance(String balanceQuery) {
		return this.sendPayloadWithRetry(balanceQuery);
	}
	
	private String sendPayloadWithRetry(String payload) {
		String response = "";
		for (int retryCounter = 0; retryCounter < this.numberOfRetry && response.isEmpty(); retryCounter++) {
			response = this.sendPayload(payload);
			try {
				this.socket.close();
			} catch (IOException e) {}
		}
		
		return response;
	}
	
	private String sendPayload(String payload) {
		try (OutputStream out = this.socket.getOutputStream();
			 InputStream in = this.socket.getInputStream()) {
			byte[] buffer = new byte[MAX_FRAME_LENGTH];
			out.write((payload + PAYLOAD_TERMINATOR).getBytes());
			int readableBytes = in.read(buffer);
			return (readableBytes > 0) ? new String(Arrays.copyOfRange(buffer, 0, readableBytes)) : "";
		} catch (IOException e) {
			logger.error("Exception while sending/receiving payload.", e);
		}
		
		return "";
	}
	
	public static Optional<INRequester> getInstance(String ip, int port, int timeout, int numberOfRetry) {
		for (int retryCounter = 0; retryCounter < numberOfRetry; retryCounter++) {
			try {
				Socket socket = new Socket();
				socket.setSoTimeout(timeout);
				socket.connect(new InetSocketAddress(ip, port), CONNECT_TIMEOUT);
				return Optional.of(new SocketINRequester(socket, numberOfRetry));
			} catch (IOException e) {
				logger.error("Exception while connecting to IN Requester: IP [{}], Port [{}]", ip, port, e);
			}
		}
		
		return Optional.empty();
	}
}
