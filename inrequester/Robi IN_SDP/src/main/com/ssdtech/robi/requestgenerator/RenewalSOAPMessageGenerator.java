package com.ssdtech.robi.requestgenerator;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPMessage;

import com.ssdtech.robi.Global;
import com.ssdtech.robi.model.RequestParams;

public class RenewalSOAPMessageGenerator implements SOAPMessageGenerator {
	private static final DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
	private static final String fromDate = "20140114000001";
	private String url;
	
	public RenewalSOAPMessageGenerator(String url) {
		this.url = url;
	}
	
	@Override
	public SOAPMessage generate(RequestParams rp) throws SOAPException {
        MessageFactory factory = MessageFactory.newInstance();
		
		SOAPMessage request = factory.createMessage();
		SOAPEnvelope envelope = request.getSOAPPart().getEnvelope();
		SOAPHeader header = envelope.getHeader();
		SOAPBody body = envelope.getBody();

		envelope.removeNamespaceDeclaration(envelope.getPrefix());
		header.removeNamespaceDeclaration(header.getPrefix());
		body.removeNamespaceDeclaration(body.getPrefix());
		envelope.setPrefix("soapenv");
		header.setPrefix("soapenv");
		body.setPrefix("soapenv");

		envelope.addNamespaceDeclaration("loc", Global.LOC_URL2);

		String currentTime = dateFormat.format(new Date());
		
		// header create
		SOAPElement requestSOAPHeader = header.addChildElement("RequestSOAPHeader", "tns", Global.V2_URL);
	    requestSOAPHeader.addChildElement("spId", "tns").addTextNode(rp.getServiceInfo().getSPID());
	    requestSOAPHeader.addChildElement("spPassword", "tns").addTextNode(
	    		    Global.getMD5(rp.getServiceInfo().getSPID() + rp.getServiceInfo().getSPPassword() + currentTime)
	    		);
	    requestSOAPHeader.addChildElement("timeStamp", "tns").addTextNode(currentTime);

		// Body create
		SOAPElement getSubScriptionListReq = body.addChildElement("getSubScriptionListReq", "loc");
		
		SOAPElement userID = getSubScriptionListReq.addChildElement("userID", "loc");
		userID.addChildElement("ID").addTextNode(rp.getMSISDN());
		userID.addChildElement("type").addTextNode("0");
		
		getSubScriptionListReq.addChildElement("actionType", "loc")
		                          .addTextNode(Global.RENEWAL_SPECIFIC_DATA.get("actionType"));
		
		SOAPElement langInfo = getSubScriptionListReq.addChildElement("langInfo", "loc");
		langInfo.addChildElement("language").addTextNode("en");
		langInfo.addChildElement("country").addTextNode("US");
		
		getSubScriptionListReq.addChildElement("recordIndex", "loc").addTextNode("1");
		getSubScriptionListReq.addChildElement("batchSize", "loc").addTextNode("10");
		
		SOAPElement extensionInfos = getSubScriptionListReq.addChildElement("extensionInfos", "loc");
		
		SOAPElement fromDateItem = extensionInfos.addChildElement("item", "loc");
		fromDateItem.addChildElement("key").addTextNode("fromDate");
		fromDateItem.addChildElement("value").addTextNode(fromDate);
		
		SOAPElement toDateItem = extensionInfos.addChildElement("item", "loc");
		toDateItem.addChildElement("key").addTextNode("toDate");
		toDateItem.addChildElement("value").addTextNode(currentTime);
		
		SOAPElement seqTypeItem = extensionInfos.addChildElement("item", "loc");
		seqTypeItem.addChildElement("key").addTextNode("sequenceType");
		seqTypeItem.addChildElement("value").addTextNode("0");
		
		SOAPElement seqCondItem = extensionInfos.addChildElement("item", "loc");
		seqCondItem.addChildElement("key").addTextNode("sequenceCondition");
		seqCondItem.addChildElement("value").addTextNode("3");
		
		SOAPElement langItem = extensionInfos.addChildElement("item", "loc");
		langItem.addChildElement("key").addTextNode("language");
		langItem.addChildElement("value").addTextNode("en_US");
		
		return request;
	}

	@Override
	public String getRequestURL() {
		return this.url;
	}
}
