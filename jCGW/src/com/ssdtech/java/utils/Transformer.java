package com.ssdtech.java.utils;

import java.util.HashMap;
import java.util.Map;

import com.ssdtech.java.db.client.DBClient;
import com.ssdtech.java.db.management.DBConnectionFactory;
import com.ssdtech.java.model.ProcedureParams;
import com.ssdtech.java.model.applicationproperties.ApplicationProperties;

public class Transformer {
	private static final Map<String, String> mappedChargingType;
	
	static {
		mappedChargingType = new HashMap<>();
		mappedChargingType.put("auth_call", "SESSION_START");
		mappedChargingType.put("reauth_call", "SESSION_INTERIM");
		mappedChargingType.put("end_call", "SESSION_STOP");
		mappedChargingType.put("charge_specific_amount", "SPECIFIC_CHARGE");
	}
	
	public static void transform(ProcedureParams params) {
		// First map request type.
		params.setMappedChargingType(getMappedChargingType(params.getRequest().getChargingType()));
		
		String ano = params.getRequest().getANO();
		String bno = params.getRequest().getBNO();
		
		// OBD related transformation.
		if (params.getRequest().getDirection() == 1 
				&& ApplicationProperties.getReqTransConf().discardPrefixOfOBD) {
			int beginIndex = ApplicationProperties.getReqTransConf().prefixOffsetOfOBD;
			int endIndex = ApplicationProperties.getReqTransConf().prefixOffsetOfOBD  
					           + ApplicationProperties.getReqTransConf().lengthOfOBD;
			endIndex = (endIndex > bno.length()) ? bno.length() : endIndex;
			// [TODO] Ensure beginIndex <= endIndex at properties' loading time.
			if (beginIndex < bno.length()) {
			    bno = bno.substring(beginIndex, endIndex);
			} // [TODO] We currently don't handle the ELSE case.
		} else if (ApplicationProperties.getRatePlanConf().directDialingCheck
				      && params.getRequest().getDirection() == 0) {
			bno = DBConnectionFactory.getEmbededDataSource().getActualBNO(bno);
		}
		
		String userMsisdn = params.getRequest().getDirection() == 0 ? ano : bno; 
		String realBNO = params.getRequest().getDirection() == 0 ? bno : ano;
		
		// Content gift will work on ano/bno; depends on direction.
		String payerMsisdn = userMsisdn;
		if (params.getServiceInfo().contentGiftingEnable()) {
			payerMsisdn = DBClient.getPayerMSISDN(userMsisdn, params.getRequest().getServiceId());
		}
		
		params.setMSISDN(payerMsisdn);
		params.setRealBNO(realBNO);
	}
	
	private static String getMappedChargingType(String actualChargingType) {
		return mappedChargingType.getOrDefault(actualChargingType.toLowerCase(), actualChargingType);
 	}
}
