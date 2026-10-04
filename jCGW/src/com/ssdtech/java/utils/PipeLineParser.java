package com.ssdtech.java.utils;

import java.io.IOException;
import java.io.StringReader;
import java.util.Properties;

import com.ssdtech.java.utils.Parser;
import com.ssdtech.java.model.DebugPoint;
import com.ssdtech.java.model.ProcedureParams;
import com.ssdtech.java.model.Request;
import com.ssdtech.java.model.Request.Builder;
import com.ssdtech.java.model.applicationproperties.ApplicationProperties;
import com.ssdtech.java.model.ResultCode;

public class PipeLineParser implements Parser {
	private static final String propertySeparator = "&";
	private static final int MINIMUM_NO_OF_CMDPARAMS = 7;
	
	private final String cgwRequest;
	
	public PipeLineParser(String cgwRequest) {
		this.cgwRequest = cgwRequest;
	}
	
	@Override
	public void parse(ProcedureParams params) {
		if (this.cgwRequest == null) {
			params.setFlagIfErrorOccured(ResultCode.ERROR_WRONG_REQUEST_FORMAT);
			return;
		}
		
		Properties prop = new Properties();
		try {
			prop.load(new StringReader(cgwRequest.replaceAll(propertySeparator, "\n")));
		} catch (IOException e) {
			params.setFlagIfErrorOccured(ResultCode.ERROR_WRONG_REQUEST_FORMAT);
			return;
		}
		
		// [TODO] Need to introduce more validation rules.
		if (prop.containsKey("appid") &&
		        prop.containsKey("apppass") &&
		            prop.containsKey("cmdid") &&
		                prop.containsKey("cmdparam")) {
			String[] cmdParams = prop.getProperty("cmdparam").split("\\|");
			if (cmdParams.length >= MINIMUM_NO_OF_CMDPARAMS) {				
				Builder builder = new Request.Builder();
				try {
					builder.setApplicationId(prop.getProperty("appid"))
							.setApplicationPassword(prop.getProperty("apppass"))
							.setChargingType(prop.getProperty("cmdid"))
							.setANO(cmdParams[0])
							.setBNO(cmdParams[1])
							.setDirection(Integer.valueOf(cmdParams[2]))
							.setServiceId(cmdParams[3])
							.setCDRId(cmdParams[4])
							.setSequenceNo(Integer.valueOf(cmdParams[5]))
							.setConsumedUnitSinceLastRequest(Long.valueOf(cmdParams[6]));
				} catch (NumberFormatException e) {
					params.setFlagIfErrorOccured(ResultCode.ERROR_WRONG_REQUEST_FORMAT);
					return;
				}
				
				int totalOptionalParams = cmdParams.length - MINIMUM_NO_OF_CMDPARAMS;

				switch (totalOptionalParams) {
					case 6:
						builder.setSubscriptionCycle(Integer.valueOf(cmdParams[12]));
					case 5:
						builder.setfixedAmount(Integer.valueOf(cmdParams[11]));
					case 4:
						builder.setProductcode(cmdParams[10]);
					case 3:
						builder.setChannel(cmdParams[9]);
					case 2:
						builder.setSubscriptionGroupId(cmdParams[8]);
					case 1:
						builder.setRemarks(cmdParams[7]);
				}
				
				params.setRequest(builder.build());
				if (ApplicationProperties.getLogConfiguration().logForAnyMSISDN
						|| ApplicationProperties.getLogConfiguration().loggableMSISDN.contains(cmdParams[0])) {
					params.setMSISDNBasedLog();
				}
				
				params.addLoggableMessage(DebugPoint.RAW_TRANSACTION_REQUEST, this.cgwRequest);
				params.addLoggableMessage(DebugPoint.PARSED_REQUEST, params.getRequest());
				return;
			}
			
			params.setFlagIfErrorOccured(ResultCode.ERROR_WRONG_REQUEST_FORMAT);
		}
		
		params.setFlagIfErrorOccured(ResultCode.ERROR_WRONG_REQUEST_FORMAT);
	}
} 
