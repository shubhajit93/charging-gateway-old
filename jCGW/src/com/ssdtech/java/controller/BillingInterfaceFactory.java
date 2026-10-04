package com.ssdtech.java.controller;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

import com.ssdtech.java.main.CGW;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.ResultSet;

import com.ssdtech.java.db.client.DBClient;
import com.ssdtech.java.db.management.DBConnectionFactory;
import com.ssdtech.java.model.BillingNode;
import com.ssdtech.java.model.PackageInfo;
import com.ssdtech.java.model.applicationproperties.ApplicationProperties;

public class BillingInterfaceFactory {
	private static final int MINIMUM_BILLING_PORT = 0;
    private static volatile Map<Long, BillingNode> _nodeInfoWRTId;
    private static volatile Map<String, BillingNode> _nodeInfoWRTNodeId;
    
    private static final Logger logger = LogManager.getLogger(BillingInterfaceFactory.class.getName());

    static {
    	_nodeInfoWRTId = new HashMap<>();
    	_nodeInfoWRTNodeId = new HashMap<>();
        loadBillingInterfaceInformation();
        System.out.println(ApplicationProperties.getLogConfiguration().instance + "|" + CGW.formatter.format(new Date()) + "|Loaded billing information into application memory.");
    }

    private BillingInterfaceFactory(){}
	
	public static boolean loadBillingInterfaceInformation() {
		Map<Long, BillingNode> nodeInfoWRTId = new HashMap<>();
		Map<String, BillingNode> nodeInfoWRTNodeId = new HashMap<>();
		
		String searchQuery = generateBillingNodeQuery();

		System.out.println(ApplicationProperties.getLogConfiguration().instance + "|" + CGW.formatter.format(new Date()) + "|Billing query: " + searchQuery);
		
		try (Connection connection = DBConnectionFactory.getPersistentConnection();
				Statement statement = connection.createStatement()) {
			ResultSet resultSet = statement.executeQuery(searchQuery);
			while (resultSet.next()) {
				Long id = resultSet.getLong("id");
				String nodeId = resultSet.getString("NodeId");
				String billingInterface = resultSet.getString("Interface");
				int port = resultSet.getInt("Port");
				String url = resultSet.getString("URL");
				String billingHost = resultSet.getString("BillingHost");
				int billingPort = resultSet.getInt("Billingport");
				
				nodeInfoWRTId.put(id, new BillingNode(id, nodeId, billingInterface, billingHost, url, billingPort, port));
				nodeInfoWRTNodeId.put(nodeId, new BillingNode(id, nodeId, billingInterface, billingHost, url, billingPort, port));
			}
			resultSet.close();
			
			_nodeInfoWRTId = nodeInfoWRTId;
			_nodeInfoWRTNodeId = nodeInfoWRTNodeId;
			return true;
		} catch (SQLException exception) {
			logger.error("Error while loading [billingnode].", exception);
		}
		
		return false;
	}
	
	public static BillingInterface getInstance(long billingNodeId, PackageInfo pi) {
		BillingNode ni = (billingNodeId == ApplicationProperties.INVALID_BILLING_NODE_ID) 
				             ? _nodeInfoWRTNodeId.get(pi.getNodeId()) : _nodeInfoWRTId.get(billingNodeId);
				             
		if (ni == null) return new ApplicationBillingInterface(
				                   ApplicationProperties.getBillingInterfaceConf().defaultBillingHost, 
				                   ApplicationProperties.getBillingInterfaceConf().defaultBillingPort
				               );
		
		if (ni.controllerPort < MINIMUM_BILLING_PORT) {
			return new DummyBillingInterface();
		}
		
		String host = ni.host;
		int port = ni.port;
		
		if (host == null || host.trim().isEmpty()) {
			host = ApplicationProperties.getBillingInterfaceConf().defaultBillingHost;
			port = ApplicationProperties.getBillingInterfaceConf().defaultBillingPort;
		}
		
		if (ni.billingInterface.equalsIgnoreCase("application")) {			
			return new ApplicationBillingInterface(host, port);
		} else if(ni.billingInterface.equalsIgnoreCase("webservice")) {
			return new WebServiceBillingInterface(host, ni.pathSegment, port);
		} else {
			return new ApplicationBillingInterface(host, port);
		}
	}
	
	private static String generateBillingNodeQuery() {
		List<String> columns = new ArrayList<>();
		columns.add("BillingHost");
		columns.add("Billingport");
		
		boolean isIdColumnExists = DBClient.isColumnExists("billingnode", "id");
		boolean isSeparateBillingInfoColumnExists = DBClient.isAllColumnExists("billingnode", columns);
		
		if (isIdColumnExists && isSeparateBillingInfoColumnExists) {
			return "SELECT id,LOWER(NodeId) AS NodeId,LOWER(Interface) AS Interface,PORT,URL,BillingHost,Billingport FROM billingnode";
		} else if (isIdColumnExists) {
			return "SELECT id,LOWER(NodeId) AS NodeId,LOWER(Interface) AS Interface,PORT,SUBSTRING(URL, LOCATE('/', URL), CHAR_LENGTH(URL)) AS URL,SUBSTRING_INDEX(URL, '/',  1) AS BillingHost,PORT AS Billingport FROM billingnode";
		} else if (isSeparateBillingInfoColumnExists) {
			return "SELECT " + ApplicationProperties.INVALID_BILLING_NODE_ID + " AS id,LOWER(NodeId) AS NodeId,LOWER(Interface) AS Interface,PORT,URL,BillingHost,Billingport FROM billingnode";
		} else {
			return "SELECT " + ApplicationProperties.INVALID_BILLING_NODE_ID + " AS id,LOWER(NodeId) AS NodeId,LOWER(Interface) AS Interface,PORT,SUBSTRING(URL, LOCATE('/', URL), CHAR_LENGTH(URL)) AS URL,SUBSTRING_INDEX(URL, '/',  1) AS BillingHost,PORT AS Billingport FROM billingnode";
		}
	}
}
