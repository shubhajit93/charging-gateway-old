package com.ericsson.ucip;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.ericsson.bean.*;
import com.ericsson.bean.UCIPAccountDetailBean;
import com.ericsson.bean.UCIPUpdAccountBalanceBean;

public class XMLBuilder 
{
	
	
	
	public String generateUpdateRequest(String method_name, UCIPUpdAccountBalanceBean bean, Logger logger)
	{
		//logger.info("[XMLBuilder] generateXMLRequest()");
		logger.log(Level.INFO, "[XMLBuilder] generateXMLRequest()");
		StringBuffer xmlString = new StringBuffer();
		
		xmlString.append("<?xml version=\"1.0\"?>");
		xmlString.append("<methodCall>");
		xmlString.append("<methodName>");
		xmlString.append(method_name);
		xmlString.append("</methodName>");
		xmlString.append("<params>");
		xmlString.append("<param>");
		xmlString.append("<value>");
		xmlString.append("<struct>");
		
		xmlString.append("<member>");
		xmlString.append("<name>");
		xmlString.append(bean.getOriginNodeTypeParamName());
		xmlString.append("</name>");
		xmlString.append("<value><string>");
		xmlString.append(bean.getOriginNodeTypeValue());
		xmlString.append("</string></value>");
		xmlString.append("</member>");
		
		xmlString.append("<member>");
		xmlString.append("<name>");
		xmlString.append(bean.getOriginHostNameParamName());
		xmlString.append("</name>");
		xmlString.append("<value><string>");
		xmlString.append(bean.getOriginHostNameValue());
		xmlString.append("</string></value>");
		xmlString.append("</member>");
		
		xmlString.append("<member>");
		xmlString.append("<name>");
		xmlString.append(bean.getOriginTransactionIDParamName());
		xmlString.append("</name>");
		xmlString.append("<value><string>");
		xmlString.append(bean.getOriginTransactionIDValue());
		xmlString.append("</string></value>");
		xmlString.append("</member>");
		
		xmlString.append("<member>");
		xmlString.append("<name>");
		xmlString.append(bean.getOriginTimeStampParamName());
		xmlString.append("</name>");
		xmlString.append("<value>");
		xmlString.append("<dateTime.iso8601>");
		xmlString.append(bean.getOriginTimeStampValue());
		xmlString.append("</dateTime.iso8601>");
		xmlString.append("</value>");
		xmlString.append("</member>");
		
		
		xmlString.append("<member>");
		xmlString.append("<name>");
		xmlString.append(bean.getSubscriberNumberParamName());
		xmlString.append("</name>");
		xmlString.append("<value><string>");
		xmlString.append(bean.getSubscriberNumberValue());
		xmlString.append("</string></value>");
		xmlString.append("</member>");
		
		xmlString.append("<member>");
		xmlString.append("<name>");
		xmlString.append(bean.getTransactionCurrencyParamName());
		xmlString.append("</name>");
		xmlString.append("<value><string>");
		xmlString.append(bean.getTransactionCurrencyValue());
		xmlString.append("</string></value>");
		xmlString.append("</member>");
		
		xmlString.append("<member>");
		xmlString.append("<name>");
		xmlString.append(bean.getAdjustmentAmountRelativeParamName());
		xmlString.append("</name>");
		xmlString.append("<value><string>");
		xmlString.append(bean.getAdjustmentAmountRelativeValue());
		xmlString.append("</string></value>");
		xmlString.append("</member>");
		
		xmlString.append("<member>");
		xmlString.append("<name>");
		xmlString.append(bean.getExternalData2());
		xmlString.append("</name>");
		xmlString.append("<value><string>");
		xmlString.append(bean.getExternalData2Value());
		xmlString.append("</string></value>");
		xmlString.append("</member>");
		
		
		xmlString.append("</struct>");
		xmlString.append("</value>");
		xmlString.append("</param>");
		xmlString.append("</params>");
		xmlString.append("</methodCall>");
		
		//logger.info("XMLRequest String: \n" + xmlString.toString() + "\n============================================================\n\n\n");
		logger.log(Level.INFO, "XMLRequest String: \n" + xmlString.toString() + "\n============================================================\n\n\n");
		
		return xmlString.toString();
	}
	
	public String generateGetAccountDetRequest(String method_name, UCIPAccountDetailBean bean, Logger logger)
	{
		//logger.info("[UCIPConnector] generateGetAccountDetRequest()");
		logger.log(Level.INFO, "[XMLBuilder] generateGetAccountDetRequest()");
		StringBuffer xmlString = new StringBuffer();
		
		xmlString.append("<?xml version=\"1.0\" encoding='utf-8' ?>");
		xmlString.append("<methodCall>");
		xmlString.append("<methodName>");
		xmlString.append(method_name);
		xmlString.append("</methodName>");
		xmlString.append("<params>");
		xmlString.append("<param>");
		xmlString.append("<value>");
		xmlString.append("<struct>");
		
		xmlString.append("<member>");
		xmlString.append("<name>");
		xmlString.append("originNodeType");
		xmlString.append("</name>");
		xmlString.append("<value><string>");
		xmlString.append(bean.getOriginNodeType());
		xmlString.append("</string></value>");
		xmlString.append("</member>");
		
		xmlString.append("<member>");
		xmlString.append("<name>");
		xmlString.append("originHostName");
		xmlString.append("</name>");
		xmlString.append("<value><string>");
		xmlString.append(bean.getOriginHostName());
		xmlString.append("</string></value>");
		xmlString.append("</member>");
		
		xmlString.append("<member>");
		xmlString.append("<name>");
		xmlString.append("originTransactionID");
		xmlString.append("</name>");
		xmlString.append("<value><string>");
		xmlString.append(bean.getOriginTrxId());
		xmlString.append("</string></value>");
		xmlString.append("</member>");
		
		xmlString.append("<member>");
		xmlString.append("<name>");
		xmlString.append("originTimeStamp");
		xmlString.append("</name>");
		xmlString.append("<value>");
		xmlString.append("<dateTime.iso8601>");
		xmlString.append(bean.getOriginTimeStamp());
		xmlString.append("</dateTime.iso8601>");
		xmlString.append("</value>");
		xmlString.append("</member>");
		
		xmlString.append("<member>");
		xmlString.append("<name>");
		xmlString.append("subscriberNumber");
		xmlString.append("</name>");
		xmlString.append("<value><string>");
		xmlString.append(bean.getSubscriberNumber());
		xmlString.append("</string></value>");
		xmlString.append("</member>");
		
		
		xmlString.append("</struct>");
		xmlString.append("</value>");
		xmlString.append("</param>");
		xmlString.append("</params>");
		xmlString.append("</methodCall>\n\n");
		
		//logger.info("XMLRequest String: \n" + xmlString.toString() + "\n============================================================\n\n\n");
		logger.log(Level.INFO, "XMLRequest String: \n" + xmlString.toString() + "\n============================================================\n\n\n");
		return xmlString.toString();
	}
	
	public String generateGetAccountBalanceRequest(String method_name, UCIPAccountBalanceBean bean, Logger logger)
	{
		//logger.info("[UCIPConnector] generateGetAccountBalanceRequest()");
		logger.log(Level.INFO, "[XMLBuilder] generateGetAccountBalanceRequest()");
		StringBuffer xmlString = new StringBuffer();
		
		xmlString.append("<?xml version=\"1.0\"?>");
		xmlString.append("<methodCall>");
		xmlString.append("<methodName>");
		xmlString.append(method_name);
		xmlString.append("</methodName>");
		xmlString.append("<params>");
		xmlString.append("<param>");
		xmlString.append("<value>");
		xmlString.append("<struct>");
		//xmlString.append("<member>");
		
		xmlString.append("<member>");
		xmlString.append("<name>");
		xmlString.append("originNodeType");
		xmlString.append("</name>");
		xmlString.append("<value><string>");
		xmlString.append(bean.getOriginNodeType());
		xmlString.append("</string></value>");
		xmlString.append("</member>");
		
		xmlString.append("<member>");
		xmlString.append("<name>");
		xmlString.append("originHostName");
		xmlString.append("</name>");
		xmlString.append("<value><string>");
		xmlString.append(bean.getOriginHostName());
		xmlString.append("</string></value>");
		xmlString.append("</member>");
		
		xmlString.append("<member>");
		xmlString.append("<name>");
		xmlString.append("originTransactionID");
		xmlString.append("</name>");
		xmlString.append("<value><string>");
		xmlString.append(bean.getOriginTrxId());
		xmlString.append("</string></value>");
		xmlString.append("</member>");
		
		xmlString.append("<member>");
		xmlString.append("<name>");
		xmlString.append("originTimeStamp");
		xmlString.append("</name>");
		xmlString.append("<value>");
		xmlString.append("<dateTime.iso8601>");
		xmlString.append(bean.getOriginTimeStamp());
		xmlString.append("</dateTime.iso8601>");
		xmlString.append("</value>");
		xmlString.append("</member>");
		
		// optional field
		if (bean.getSubsNumberNAI()!=null && !bean.getSubsNumberNAI().equals(""))
		{
			xmlString.append("<member>");
			xmlString.append("<name>");
			xmlString.append("susbcriberNumberNAI");
			xmlString.append("</name>");
			xmlString.append("<value>");
			xmlString.append(bean.getSubsNumberNAI());
			xmlString.append("</value>");
			xmlString.append("</member>");
		}
		
		xmlString.append("<member>");
		xmlString.append("<name>");
		xmlString.append("subscriberNumber");
		xmlString.append("</name>");
		xmlString.append("<value><string>");
		xmlString.append(bean.getSubscriberNumber());
		xmlString.append("</string></value>");
		xmlString.append("</member>");
		
		// optional field
		if (bean.getMessageFlag()!=null && !bean.getMessageFlag().equals(""))
		{
			xmlString.append("<member>");
			xmlString.append("<name>");
			xmlString.append("messageCapabilityFlag");
			xmlString.append("</name>");
			xmlString.append("<value>");
			xmlString.append(bean.getMessageFlag());
			xmlString.append("</value>");
			xmlString.append("</member>");
		}
		
		// optional field
		if (bean.getDedicatedAccountSelection()!=null && !bean.getDedicatedAccountSelection().equals(""))
		{
			xmlString.append("<member>");
			xmlString.append("<name>");
			xmlString.append("dedicatedAccountSelection");
			xmlString.append("</name>");
			xmlString.append("<value>");
			xmlString.append(bean.getDedicatedAccountSelection());
			xmlString.append("</value>");
			xmlString.append("</member>");
		}
		
		// optional field
		if (bean.getChargingRequestInfo()!= null && !bean.getChargingRequestInfo().equals(""))
		{
			xmlString.append("<member>");
			xmlString.append("<name>");
			xmlString.append("chargingRequestInformation");
			xmlString.append("</name>");
			xmlString.append("<value>");
			xmlString.append(bean.getChargingRequestInfo());
			xmlString.append("</value>");
			xmlString.append("</member>");
		}
		
		// optional field
		if (bean.getRequestSubordinateAccountDetails()!=null && !bean.getRequestSubordinateAccountDetails().equals(""))
		{
			xmlString.append("<member>");
			xmlString.append("<name>");
			xmlString.append("requestSubordinateDedicatedAccountDetailsFlag");
			xmlString.append("</name>");
			xmlString.append("<value>");
			xmlString.append(bean.getRequestSubordinateAccountDetails());
			xmlString.append("</value>");
			xmlString.append("</member>");
		}
		
		xmlString.append("</struct>");
		xmlString.append("</value>");
		xmlString.append("</param>");
		xmlString.append("</params>");
		xmlString.append("</methodCall>");
		
		//logger.info("XMLRequest String: \n" + xmlString.toString() + "\n============================================================\n\n\n");
		logger.log(Level.INFO, "XMLRequest String: \n" + xmlString.toString() + "\n============================================================\n\n\n");
		
		return xmlString.toString();
	}

}
