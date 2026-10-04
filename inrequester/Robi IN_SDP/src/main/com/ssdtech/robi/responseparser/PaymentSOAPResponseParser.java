package com.ssdtech.robi.responseparser;

import javax.xml.soap.SOAPBody;

import org.w3c.dom.NodeList;

import com.ssdtech.robi.ResultCode;
import com.ssdtech.robi.model.RequestParams;

public class PaymentSOAPResponseParser extends SOAPResponseParser {

	@Override
	protected String processSOAPSuccessResponse(SOAPBody body, RequestParams rp) {
		NodeList nodes = body.getChildNodes();
		for(int index = 0; index < nodes.getLength(); index++) {
			if (nodes.item(index).getLocalName().equals("chargeAmountResponse")) {
				return ResultCode.SUCCESS_CODE;
			}
		}
		return ResultCode.SOAP_PARSING_ERROR_CODE;
	}
}
