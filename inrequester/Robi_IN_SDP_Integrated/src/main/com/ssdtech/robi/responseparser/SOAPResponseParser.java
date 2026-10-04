package com.ssdtech.robi.responseparser;

import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

import com.ssdtech.robi.model.RequestParams;

public abstract class SOAPResponseParser {
    
	public String process(SOAPMessage response, RequestParams rp) throws SOAPException {
        SOAPBody body = response.getSOAPBody();
		
		if (body.hasFault()) {
			return body.getFault().getFaultCode();
		}
		
		return this.processSOAPSuccessResponse(body, rp);
	}
	
	protected abstract String processSOAPSuccessResponse(SOAPBody body, RequestParams rp);
}
