package com.ssdtech.java.controller.maintenance;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.ssdtech.cgwcommon.INRequester;
import com.ssdtech.java.controller.BillingInterfaceFactory;
import com.ssdtech.java.db.management.DBConnectionFactory;
import com.ssdtech.java.db.management.EmbededDB;
import com.ssdtech.java.model.RatePulse;
import com.ssdtech.java.model.applicationproperties.ApplicationProperties;
import com.ssdtech.java.utils.Authentication;
import com.ssdtech.java.utils.Authorization;
import com.ssdtech.java.utils.CallTypeChecker;
import com.ssdtech.java.utils.Tuple;

public class CommandRequestHandler extends Thread {
	private static final String HELP_MESSAGE = "[reload]|[tables]|[table(s) name]\r\n"
			                                 + "        |[conf]|[in]\r\n"
			                                 + "[show]|[data]|[ratecontext|(Service Id)|(SubscriptionGroupId)|(ChargingType)|(PackageId)]\r\n"
			                                 + "             |[rateid|(Rate Id)]\r\n"
			                                 + "[1(For graceful shutdown)]\r\n";
	private static final int TABLE_LOAD_RETRY_COUNTER = 10;
	
    private Socket socket;
	
	public CommandRequestHandler(Socket socket) {
		this.socket = socket;
	}
	
	@Override
	public void run() {
		try (InputStream in = this.socket.getInputStream();
				OutputStream out = this.socket.getOutputStream()) {
			while (true) {
				byte[] data = new byte[1000];
				int readableBytes = in.read(data);
				if (readableBytes <= 0) break;
				
				this.processRequest(new String(Arrays.copyOfRange(data, 0, readableBytes)), out);
			}
			
			this.socket.close();
		} catch (IOException exception) {}
	}
	
	private void processRequest(String request, OutputStream out) {
		if (request == null) {
			try {
				out.write(("Wrong command." + System.lineSeparator() + HELP_MESSAGE + System.lineSeparator()).getBytes());
			} catch (IOException e) {}
			return;
		}
		
		if (request.trim().equalsIgnoreCase("1")) {
			try {
				synchronized(ApplicationProperties.getShutdownController()) {
					ApplicationProperties.getShutdownController().notify();
				}
				out.write("Stopping CGW application. Wait for several minutes to stop.".getBytes());
			} catch (IOException e) {}
			return;
		}
		
	    String[] requestParts = request.trim().split("\\|");
	    if (requestParts.length < 3) {// [reload]|[tables]|[table(s) name]
	    	try {
				out.write(("Wrong command." + System.lineSeparator() + HELP_MESSAGE + System.lineSeparator()).getBytes());
			} catch (IOException e) {}
	    	return;
	    }
	    
	    boolean anyCommandFound = false;
	    
	    if ("reload".equalsIgnoreCase(requestParts[0])) {
		    if ("tables".equalsIgnoreCase(requestParts[1])) {
		    	anyCommandFound = true;
		    	
		    	StringBuilder message = new StringBuilder();
			    for (int index = 2; index < requestParts.length; index++) {
			    	String tableName = requestParts[index].trim();
			    	message.append("Loading table [").append(tableName).append("]. ");
			    	
			    	boolean loaded = false;
			    	for (int i = 0; !loaded && i < TABLE_LOAD_RETRY_COUNTER; i++) {
			    		loaded = this.loadTable(tableName);
			    	}
			        message.append(loaded ? "OK.\r\n" : "Error.\r\n");
			    }
			    
			    try {
					out.write((message + System.lineSeparator()).getBytes());
				} catch (IOException e) {}
		    } else if ("conf".equalsIgnoreCase(requestParts[1])) {
		    	if ("in".equalsIgnoreCase(requestParts[2])) {
		    		anyCommandFound = true;
		    		
		    		Optional<INRequester> requester = ApplicationProperties.getINRequester();
		    		try {
			    		if (requester.isPresent()) {
			    			out.write((requester.get().executeCommand("reload") + System.lineSeparator()).getBytes());
			    		} else {
			    			out.write(("IN Requester Object can not be initialized." + System.lineSeparator()).getBytes());
			    		}
		    		} catch (IOException exception) {}
		    	}
		    }
	    } else if ("show".equalsIgnoreCase(requestParts[0])) {
	    	if ("data".equalsIgnoreCase(requestParts[1])) {
	    		anyCommandFound = true;

		    	String key = requestParts[2].trim();
		        String message = "Unsupported key[" + key + "]. Error."; // Let's all be pessimistic!.
		        int desiredValues = requestParts.length - 3;
		        
		        if ("ratecontext".equalsIgnoreCase(key)) {
		        	if (desiredValues >= 4) {
			        	EmbededDB edb = DBConnectionFactory.getEmbededDataSource();
			        	
			        	Map<String, Object> serviceInfo = new HashMap<>();
			        	serviceInfo.put("subscriptionGroupIds", edb.getSubscriptionGroupsFor(requestParts[3]));
			        	serviceInfo.put("serviceInfo", edb.getServiceInfo(requestParts[3]));
			        	serviceInfo.put("walletOption", edb.walletOption(requestParts[3]));
			        	serviceInfo.put("rateContexts", edb.getRateContextsFor(requestParts[3], requestParts[4], requestParts[5], requestParts[6]));
			        	
			        	ObjectMapper objectMapper = new ObjectMapper();
			        	try {
							message = objectMapper.writeValueAsString(serviceInfo);
						} catch (IOException e) {
							message = "Data to JSON conversion error. Error.";
						}
		        	} else {
		        		message = "More values needed. Expected: [Service Id]|[SubscriptionGroupId]|[ChargingType]|[PackageId]. Error";
		        	}
		        } else if ("rateid".equalsIgnoreCase(key)) {
		        	if (desiredValues >= 1) {
		        		String value = requestParts[3];
			        	List<RatePulse> ratePulses = new ArrayList<>();
			        	Tuple<Integer, RatePulse> ratingInfo = DBConnectionFactory.getEmbededDataSource().getRatingInfo(value, 0);
			        	if (ratingInfo != null) {
			        		ratePulses.add(ratingInfo.second);
			        		for (int stepNo = 1; stepNo < ratingInfo.first; stepNo++) {
			        			ratePulses.add(DBConnectionFactory.getEmbededDataSource().getRatingInfo(value, stepNo).second);
			        		}
			        	}
			        	ObjectMapper objectMapper = new ObjectMapper();
			        	try {
							message = objectMapper.writeValueAsString(ratePulses);
						} catch (IOException e) {
							message = "Data to JSON conversion error. Error.";
						}
		        	} else {
		        		message = "More values needed. Expected: [RateId]. Error";
		        	}
		        }
		        
		        try {
					out.write((message + System.lineSeparator()).getBytes());
				} catch (IOException e) {}
	    	}
	    }
	    
	    if (!anyCommandFound) {
	    	try {
				out.write(("Wrong command." + System.lineSeparator() + HELP_MESSAGE + System.lineSeparator()).getBytes());
			} catch (IOException e) {}
	    }
	}
	
	private boolean loadTable(String tableName) {
		if ("applicationinfo".equalsIgnoreCase(tableName)) {
			return Authentication.loadApplicationInfo();
		} else if ("tps".equalsIgnoreCase(tableName)) {
			return Authorization.loadTPSInfo();
		} else if ("billingnode".equalsIgnoreCase(tableName)) {
			return BillingInterfaceFactory.loadBillingInterfaceInformation();
		} else if ("timeslot".equalsIgnoreCase(tableName)) {
			return DBConnectionFactory.getEmbededDataSource().loadTimeSlotInformation();
		} else if ("ratemaster".equalsIgnoreCase(tableName)) {
			return DBConnectionFactory.getEmbededDataSource().loadRateContextInformation();
		} else if ("ratepulse".equalsIgnoreCase(tableName)) {
			return DBConnectionFactory.getEmbededDataSource().loadRatingInfo();
		} else if ("subscriptiongroup".equalsIgnoreCase(tableName)) {
			return DBConnectionFactory.getEmbededDataSource().loadSubscriptionGroupInformation();
		} else if ("calltype".equalsIgnoreCase(tableName)) {
			return CallTypeChecker.loadCallTypeInfo();
		}
		
		return false;
	}
}
