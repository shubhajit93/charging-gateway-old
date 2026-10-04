package com.ssdtech.java.controller;

import java.util.Optional;

import com.ssdtech.cgwcommon.INRequester;
import com.ssdtech.java.controller.BillingInterface;
import com.ssdtech.java.in.SocketINRequester;
import com.ssdtech.java.model.BillingResponse;
import com.ssdtech.java.model.DebugPoint;
import com.ssdtech.java.model.ProcedureParams;
import com.ssdtech.java.model.applicationproperties.ApplicationProperties;

public class ApplicationBillingInterface implements BillingInterface {
	private String host;
    private int port;
	
	public ApplicationBillingInterface(String host, int port) {
		this.host = host;
		this.port = port;
	}	
	
	@Override
	public BillingResponse chargeSubscriber(ProcedureParams params) {
		String chargingRequest = params.getRequest().getCDRId() 
				                    + "[" + params.getRequest().getServiceId() + "]|"
				                    + params.getRequest().getSequenceNo()
				                    + "|" + params.getMappedChargedType() 
				                    + "|" + ApplicationProperties.getReqTransConf().countryCode + params.getMSISDN() 
				                    + "|" + params.getRealBNO() 
				                    + "|" + params.getRequest().getDirection() 
				                    + "|" + params.getRateId() 
				                    + "|" + params.getRatingInfo().getAmount()
				                    + "|" + params.getRequest().getConsumedUnitSinceLastRequest()
				                    + "|" + params.getRatingInfo().getPulseSize();
									//add productcode
		
		if (ApplicationProperties.getBillingInterfaceConf().useRemarks) {
			chargingRequest += "|" + params.getRequest().getRemarks() + " " + params.getRequest().getApplicationId();
		}
		
		if (ApplicationProperties.getRatePlanConf().refundSession) {
			chargingRequest += "|" + params.getRatingInfo().getRefundAmount();
		}
		
		params.addLoggableMessage(DebugPoint.BILLING_REQUEST, chargingRequest);

		//System.out.println("inRequester charging Request: "+ chargingRequest);
		Optional<INRequester> requester = (ApplicationProperties.getBillingInterfaceConf().isINIntegrated) ? 
							                ApplicationProperties.getINRequester() :
							                SocketINRequester.getInstance(
							             		   this.host, 
							             		   this.port, 
							             		   (int) ApplicationProperties.getBillingInterfaceConf().receiveTimeout, 
							             		   1
							             	);
		String chargingResponse = requester.isPresent()? 
				                      requester.get().chargeSubscriber(chargingRequest) : null;
		
		params.addLoggableMessage(DebugPoint.RAW_BILLING_RESPONSE, chargingResponse);
		
		return BillingResponse.deserialize(chargingResponse);
	}
}
