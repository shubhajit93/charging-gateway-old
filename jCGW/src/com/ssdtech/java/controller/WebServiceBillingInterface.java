package com.ssdtech.java.controller;

import java.io.IOException;

import okhttp3.ResponseBody;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ssdtech.java.model.BillingResponse;
import com.ssdtech.java.model.DebugPoint;
import com.ssdtech.java.model.ProcedureParams;
import com.ssdtech.java.model.RatingInfo;
import com.ssdtech.java.model.Request;
import com.ssdtech.java.model.applicationproperties.ApplicationProperties;

public class WebServiceBillingInterface implements BillingInterface {
	private static final Logger logger = LogManager.getLogger(WebServiceBillingInterface.class.getName());
	
    private String host;
    private String pathSegment;
    private int port;
	
	WebServiceBillingInterface(String host, String pathSegment, int port) {
		this.host = host;
		this.pathSegment = pathSegment;
		this.port = port;
	}
	
	@Override
	public BillingResponse chargeSubscriber(ProcedureParams params) {
		Request request = params.getRequest();
		RatingInfo ri = params.getRatingInfo();
		
		okhttp3.HttpUrl.Builder urlBuilder = new okhttp3.HttpUrl.Builder()
				                    .scheme("http")
					                .host(this.host)
					                .addPathSegments(this.pathSegment)
					                .port(this.port)
					                .addQueryParameter("chargingid", request.getCDRId() + "[" + request.getServiceId() + "]")
					                .addQueryParameter("sequenceno", String.valueOf(request.getSequenceNo()))
					                .addQueryParameter("requesttype", params.getMappedChargedType())
					                .addQueryParameter("ano", ApplicationProperties.getReqTransConf().countryCode + params.getMSISDN())
					                .addQueryParameter("bno", params.getRealBNO())
					                .addQueryParameter("direction", String.valueOf(request.getDirection()))
					                .addQueryParameter("rateid", params.getRateId())
					                .addQueryParameter("amount", String.valueOf(ri.getAmount()))
					                .addQueryParameter("consumedunitsincelastrequest", String.valueOf(request.getConsumedUnitSinceLastRequest()))
					                .addQueryParameter("pulsesize", String.valueOf(ri.getPulseSize()))
									.addQueryParameter("productcode", request.getproductcode());
		if (ApplicationProperties.getBillingInterfaceConf().useRemarks) {
			urlBuilder.addQueryParameter("purpose", request.getRemarks() + "+" + request.getApplicationId());
		} 
		
		if (ApplicationProperties.getRatePlanConf().refundSession) {
			urlBuilder.addQueryParameter("refundamount", String.valueOf(ri.getRefundAmount()));
		}
        
		okhttp3.HttpUrl url = urlBuilder.build();
		//System.out.println("webservice billing request: "+url);
		params.addLoggableMessage(DebugPoint.BILLING_REQUEST, url);
		
		okhttp3.Request httpRequest = new okhttp3.Request.Builder().url(url).build();
		
		String chargingResponse = null;
		try {
			okhttp3.Response response = ApplicationProperties.getHttpClient().newCall(httpRequest).execute();
			ResponseBody responseBody = response.body();
			if(responseBody != null)
				chargingResponse = responseBody.string();
		} catch (IOException e) {
			logger.error("Exception while charging using web service billing interface.", e);
		}

		//System.out.println("webservice Billing Response: "+chargingResponse);
		
		params.addLoggableMessage(DebugPoint.RAW_BILLING_RESPONSE, chargingResponse);
		
		return BillingResponse.deserialize(chargingResponse);
	}
}
