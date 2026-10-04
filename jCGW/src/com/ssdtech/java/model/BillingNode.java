package com.ssdtech.java.model;

public class BillingNode {
	public final long id;
    public final String nodeId;
    public final String billingInterface;
    public final String host;
    public final String pathSegment;
    public final int port;
    public final int controllerPort;
    
    public BillingNode(long id, String nodeId, String billingInterface, String host, String pathSegment, int port, int controllerPort) {
    	this.id = id;
    	this.nodeId = nodeId;
    	this.billingInterface = billingInterface;
    	this.host = host;
    	this.pathSegment = pathSegment;
    	this.port = port;
    	this.controllerPort = controllerPort;
    }
}
