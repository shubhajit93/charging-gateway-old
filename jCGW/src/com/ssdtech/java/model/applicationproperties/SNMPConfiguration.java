package com.ssdtech.java.model.applicationproperties;

import java.util.Properties;
import java.util.StringJoiner;

import com.ssdtech.java.utils.Utility;

public class SNMPConfiguration {
	public final boolean enabled;
	public final String instanceName;
	public final String managerIP;
	public final int managerPort;
	public final String localIp;
	public final int localPort;
	public final String agentIp;
	
	public SNMPConfiguration(Properties properties) {
		String enabled = Utility.ptrim(properties, "SNMP_ENABLED", "0");
		String instanceName = Utility.ptrim(properties, "SNMP_INSTANCE_NAME", "CGW1");
		String managerIp = Utility.ptrim(properties, "SNMP_MANAGER_HOST_IP", "localhost");
		String managerPort = Utility.ptrim(properties, "SNMP_MANAGER_PORT_NO", "3888");
		String localIp = Utility.ptrim(properties, "SNMP_LOCAL_IP", "0.0.0.0");
		String localPort = Utility.ptrim(properties, "SNMP_LOCAL_PORT", "0");
		String agentIp = Utility.ptrim(properties, "SNMP_AGENT_IP", "localhost");
		
		this.enabled = enabled.equals("1");
		this.instanceName = instanceName;
		this.managerIP = managerIp;
		this.managerPort = Integer.valueOf(managerPort);
		this.localIp = localIp;
		this.localPort = Integer.valueOf(localPort);
		this.agentIp = agentIp;
	}
	
	@Override
	public String toString() {
		return new StringJoiner("\r\n")
				       .add("\r\n\r\n[SNMP Configuration]")
				       .add("SNMP_ENABLED = " + Utility.getEncodedValueForBooleanProperties(this.enabled))
				       .add("SNMP_INSTANCE_NAME = " + this.instanceName)
				       .add("SNMP_MANAGER_HOST_IP = " + this.managerIP)
				       .add("SNMP_MANAGER_PORT_NO = " + this.managerPort)
				       .add("SNMP_LOCAL_IP = " + this.localIp)
				       .add("SNMP_LOCAL_PORT = " + this.localPort)
				       .add("SNMP_AGENT_IP = " + this.agentIp)
				       .toString();
	}
}
