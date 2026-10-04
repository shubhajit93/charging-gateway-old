package com.ssdtech.java.model;

import com.ssdtech.java.model.applicationproperties.ApplicationProperties;

public class Response {
	private static final String BASE_RESPONSE_FORMAT = "%s %s %s %d %d";
	private static final String EXTENDED_RESPONSE_FORMAT = "%s %s %s %d %d %s";
	
	public static String serialize(ProcedureParams params) {
		// Required object: Request, BillingResponse, RatingInfo
		// If request parsing error occur, then there will be no Request object.
		//        Current CGW C application don't send any response upon receiving 
		//        wrong formatted request.
		// -> BillingResponse object should be set. 
		//    Except for error, BillingResponse object will always be created.
		
		// If CDR duplication occur, then we have to send previous response [If enabled]
		if (params.getRequest() == null) {
			return "-1 " + params.getError().getAlphaNumericCode() + " INVALID_REQUEST 0 0 " + 
		           (ApplicationProperties.getServerSpecificConfiguration().outputSubscriptionStatus ? "0" : "");
		}
		
		if (ApplicationProperties.getCDRConfiguration().cdrDuplicationLastResponse 
				&& params.errorOccured() && params.getError().equals(ResultCode.ERROR_DUPLICATE_CDR_DETECT)) {
			return params.getPrevSerializedResponse();
		}
		
		String resultCode = (params.errorOccured()) ? params.getError().getAlphaNumericCode() :  
				                                      params.getBillingResponse().getResultCode();
		long pulseSize = (params.getRatingInfo() != null) ? params.getRatingInfo().getPulseSize() : 0L;
		String mappedChargingType = (params.getMappedChargedType() != null) ? 
				                        params.getMappedChargedType() : params.getRequest().getChargingType(); 
		
		if (ApplicationProperties.getServerSpecificConfiguration().outputSubscriptionStatus) {
			String subscriptionStatus = ((params.getSubscriberService() != null) ? 
				                              params.getSubscriberService().getNumericSubscriptionStatus() : "0");
			return String.format(EXTENDED_RESPONSE_FORMAT, 
				                    params.getRequest().getCDRId(),
				                    resultCode,
				                    mappedChargingType,
				                    params.getRequest().getSequenceNo(),
				                    pulseSize,
				                    subscriptionStatus
				                );	
		} else {
		    return String.format(BASE_RESPONSE_FORMAT, 
	                                 params.getRequest().getCDRId(),
                                     resultCode,
                                     mappedChargingType,
                                     params.getRequest().getSequenceNo(),
                                     pulseSize
                                 );	
		}
	}
}
