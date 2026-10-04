package com.ssdtech.robi.requestgenerator;

import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

import com.ssdtech.robi.model.RequestParams;

public interface SOAPMessageGenerator {
	
	public SOAPMessage generate(RequestParams rp) throws SOAPException;
	
	public String getRequestURL();
}
