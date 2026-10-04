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

public class PaymentSOAPMessageGenerator implements SOAPMessageGenerator {
	private static final DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
	private String url;
	
	public PaymentSOAPMessageGenerator(String url) {
		this.url = url;
	}
	
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

		// envelope create
		envelope.addNamespaceDeclaration("v2", Global.V2_URL);
		envelope.addNamespaceDeclaration("loc", Global.LOC_URL);

		// header create
		SOAPElement requestSOAPHeader = header.addChildElement("RequestSOAPHeader", "v2");
		requestSOAPHeader.addChildElement("spId", "v2").addTextNode(rp.getServiceInfo().getSPID());
		
		String currentTime = dateFormat.format(new Date());
		requestSOAPHeader.addChildElement("spPassword", "v2").addTextNode(
	    		    Global.getMD5(rp.getServiceInfo().getSPID() + rp.getServiceInfo().getSPPassword() + currentTime)
	    		);
		
		requestSOAPHeader.addChildElement("serviceId", "v2").addTextNode(rp.getServiceInfo().getServiceCode());
		requestSOAPHeader.addChildElement("timeStamp", "v2").addTextNode(currentTime);
		requestSOAPHeader.addChildElement("OA", "v2").addTextNode(rp.getMSISDN());
		requestSOAPHeader.addChildElement("FA", "v2").addTextNode(rp.getMSISDN());
		requestSOAPHeader.addChildElement("token", "v2");

		// Body create
		SOAPElement chargeAmount = body.addChildElement("chargeAmount", "loc");
		chargeAmount.addChildElement("endUserIdentifier", "loc").addTextNode(rp.getMSISDN());
		SOAPElement charge = chargeAmount.addChildElement("charge", "loc");
		charge.addChildElement("description").addTextNode(Global.PAYMENT_SPECIFIC_DATA.get("description"));
		charge.addChildElement("currency").addTextNode(Global.PAYMENT_SPECIFIC_DATA.get("currency"));
		charge.addChildElement("amount").addTextNode(Long.toString(rp.getAmount()));
		charge.addChildElement("code").addTextNode(rp.getServiceInfo().getSPID());
		chargeAmount.addChildElement("referenceCode", "loc").addTextNode(rp.getTID());

		return request;
	}

	@Override
	public String getRequestURL() {
		return this.url;
	}
}
