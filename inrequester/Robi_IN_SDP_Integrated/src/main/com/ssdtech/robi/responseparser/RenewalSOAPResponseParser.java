package com.ssdtech.robi.responseparser;

import javax.xml.soap.Node;
import javax.xml.soap.SOAPBody;

import org.w3c.dom.NodeList;

import com.ssdtech.robi.Global;
import com.ssdtech.robi.ResultCode;
import com.ssdtech.robi.model.RequestParams;

public class RenewalSOAPResponseParser extends SOAPResponseParser {

	@Override
	protected String processSOAPSuccessResponse(SOAPBody body, RequestParams rp) {
		Node recordSum = getNode("recordSum", 
                             getNodes(getNode("getSubScriptionListRsp", body.getChildNodes())));
	    
		if (recordSum != null) {
			int numberOfRecords = Integer.parseInt(recordSum.getTextContent());
			
			if (numberOfRecords <= 0) {
				return Global.RENEWAL_DEFAULT_RESULT_CODE;
			} else {
				NodeList subscriptionInfo = getNode("subScriptionInfos",
							                        getNodes(getNode("getSubScriptionListRsp", body.getChildNodes())))
						                                .getChildNodes();
				
				String productId = rp.getServiceInfo().getProductID();
				
				for (int index = 0; index < subscriptionInfo.getLength(); index++) {
					Node productIdElem = getNode("productID", subscriptionInfo.item(index).getChildNodes());
					
					if (productIdElem.getTextContent().equals(productId)) {
						Node subScriptionState = getNode("subScriptionState", subscriptionInfo.item(index).getChildNodes());
						
						if (Global.RENEWAL_SUCCESS_CODES_MAPPING.contains(subScriptionState.getTextContent())) {
							return Global.RENEWAL_CGW_SUCCESS_CODE;
						} else if (Global.RENEWAL_FAILURE_CODES_MAPPING.contains(subScriptionState.getTextContent())) {
							return Global.RENEWAL_CGW_FAILURE_CODE;
						} else {
							return subScriptionState.getTextContent();
						}
					}
				}
				
				return Global.RENEWAL_DEFAULT_RESULT_CODE;
			}
		}
		
		return ResultCode.SOAP_PARSING_ERROR_CODE;
	}
	
	private NodeList getNodes(Node node) {
		if (node != null) {
			return node.getChildNodes();
		}
		
		return null;
	}
	
	private Node getNode(String nodeLocalName, NodeList nodes) {
		if (nodes == null) {
			return null;
		}
		
		for (int index = 0; index < nodes.getLength(); index++) {
			if (nodes.item(index).getLocalName().equals(nodeLocalName)) {
				return (Node) nodes.item(index);
			}
		}
		
		return null;
	}
}
