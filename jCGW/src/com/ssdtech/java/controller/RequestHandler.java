package com.ssdtech.java.controller;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Optional;

import com.ssdtech.java.model.*;
import com.ssdtech.java.utils.*;
import okhttp3.ResponseBody;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ssdtech.java.model.applicationproperties.ApplicationProperties;
import com.ssdtech.java.controller.rateengine.Event;
import com.ssdtech.java.controller.rateengine.RateEngineFactory;
import com.ssdtech.java.db.client.DBClient;
import com.ssdtech.java.log.monitoring.StateContext;
import com.ssdtech.java.log.monitoring.StateGenerator;
import com.ssdtech.java.log.monitoring.StateMonitoring;
import com.ssdtech.java.model.ServiceInfo.RegionConf;

public class RequestHandler extends Thread {
	private static final Logger logger = LogManager.getLogger(RequestHandler.class.getName());
	private static boolean stop = false;
	
	private Socket clientSocket;
	private InputStream in;
	private OutputStream out;
	BillingResponse billingRsp;
	
	RequestHandler(Socket clientSocket) throws IOException {
		this.clientSocket = clientSocket;
		this.in = clientSocket.getInputStream();
		this.out = clientSocket.getOutputStream();
	}
	
	public static void stopProcessingFurtherRequests() {
		stop = true;
	}
	
	@Override
	public void run() {
        while (!stop) {
        	byte[] buffer = new byte[1000]; // Currently we need ~600[max] bytes to accommodate each request.
        	try {
				int readableBytes = this.in.read(buffer);
				if (readableBytes < 0) break;

				//System.out.println("readbyte:"+readableBytes+"buffer:"+buffer);
				
				ProcedureParams params = this.processRequest(new String(Arrays.copyOfRange(buffer, 0, readableBytes)));
				String generatedResponse = Response.serialize(params);
				this.out.write(generatedResponse.getBytes());
				
				params.addLoggableMessage(DebugPoint.GENERATED_THIRD_PARTY_RESPONSE, generatedResponse);
				
				if (params.shouldUpdateSessionData()) {
				    SessionManagement.updateAndSetSessionInfo(params);
				}
		
				if (params.shouldStoreCdrData()) {
					DBClient.saveCDRData(params);
				}
				
				if (params.duplicationCDRIdInsertingEnable()) {
					DBClient.storeCDRId(params);
				}
				
				if (params.isMSISDNBasedLogEnable()) {
					Utility.writeLogToFile(
							params.getMSISDN() != null ? params.getMSISDN() : params.getRequest().getANO(), 
							params.getLoggableMessage());
				}
				
				// Send to Log Server.
				if (ApplicationProperties.getLogServerConfiguration().enable && params.getRequest() != null
						&& params.getMappedChargedType() != null) {
					boolean isSuccess = !params.errorOccured() && params.getBillingResponse().isSuccessCode();
					Request r = params.getRequest();
					StateMonitoring.getInstance().addStateContext(new StateContext(r.getCDRId(), StateGenerator.getTransactionState(params.getMappedChargedType(), false, isSuccess)));
				}
			} catch (IOException e) {
				break;
			}
        }
        
        try {
			this.out.close();
			this.in.close();
	        this.clientSocket.close();
		} catch (IOException e) {}
	}
	
	private String getRegion(ProcedureParams params) {
		ServiceInfo si = params.getServiceInfo();
		if (ApplicationProperties.getRatePlanConf().regionCheck && si.getRegionConf().isPresent()) {
            RegionConf regionConf = si.getRegionConf().get();
			
			okhttp3.HttpUrl url = new okhttp3.HttpUrl.Builder()
					                 .scheme("http")
					                 .host(regionConf.host)
					                 .port(regionConf.port)
					                 .addPathSegments(regionConf.pathSegment)
					                 .addQueryParameter("msisdn", params.getMSISDN())
					                 .addQueryParameter("serviceid", params.getRequest().getServiceId())
					                 .build();
			okhttp3.Request request = new okhttp3.Request.Builder().url(url).build();
			try {
				okhttp3.Response response = ApplicationProperties.getHttpClient().newCall(request).execute();
				ResponseBody responseBody = response.body();
				if(responseBody != null)
				{
					String region = responseBody.string();
					return (region == null) ? "na" : region.toLowerCase();
				}
			} catch (IOException e) {
				logger.error("Exception in region finding.", e);
			}
		}
		
		return "na";
	}
	
	private ProcedureParams processRequest(String trxRequest) {
		System.out.println("Request:"+trxRequest);
		ProcedureParams params = new ProcedureParams(); // It will hold all the execution data.

		// Parsing.
		RequestParserFactory.getParser(trxRequest).parse(params);
		if (params.errorOccured()) return params;

		// Authentication.
		Authentication.authenticate(params);
		if (params.errorOccured()) return params;
		
		// Authorization.
		//     |----> Check TPS.
		//     |----> Check duplication CDR ID.		
		Authorization.checkTPS(params);
		if (params.errorOccured()) return params;
		
		// If not found, a dummy ServiceInfo will be set.
		params.setServiceInfo(DBClient.getServiceInfo(params.getRequest().getServiceId()));
		
		// Transformation.
		Transformer.transform(params);
		
		// Send to Log Server.
		if (ApplicationProperties.getLogServerConfiguration().enable) {
			Request r = params.getRequest();
			StateMonitoring.getInstance().addStateContext(new StateContext(r.getCDRId(), StateGenerator.getTransactionState(params.getMappedChargedType(), true, false)));
		}
		
		// Activate session info storage for SESSION_INTERIM and SESSION_STOP.
		if (params.getMappedChargedType().equalsIgnoreCase("SESSION_INTERIM") 
				|| params.getMappedChargedType().equalsIgnoreCase("SESSION_STOP")) {
			params.setFlagIfSessionDataToBeStored();
		}
		
		if (params.duplicationCDRIdCheckingEnable()) {
			DBClient.checkDuplicateCDRId(params);
			if (params.errorOccured()) return params;
		}
		
		// Information gathering.
		//     |----> AIR request.
		//     |----> Package ID finding.
		//     |----> Region finding.
		//     |----> Subscription Group info finding.
		//     |----> Subscriber's info finding.
		//     |----> Wallet information finding (If have any).
		//     |----> Rate Id finding.
		//     |----> Amount finding.
		//     |----> Wallet update (If have any).
		PackageFinder pi = new PackageFinder(); // Can be used as static class as it does not hold any states.
		pi.loadPackageInformationFromOperator(params);
		if (params.errorOccured()) return params;
		
		pi.updatePackageInformationFromLocal(params);
		
		params.setRegion(this.getRegion(params));
		
		if (ApplicationProperties.getRatePlanConf().callTypeIdCheck) {
		    params.setCallTypeId(CallTypeChecker.getCallTypeId(params.getMSISDN(), params.getRealBNO()));
		}
		
		Event event = RateEngineFactory.getRateEngine(
				           params.getMappedChargedType(), 
				           DBClient.walletOption(params.getRequest().getServiceId()));
		event.process(params); //process event
		if (params.errorOccured()) return params;
		
		if (params.isMSISDNBasedLogEnable()) {
			params.addLoggableMessage(DebugPoint.RATING_INFO, "Rate id: " + params.getRateId() + " -> " + params.getRatingInfo());
		}

		if (ApplicationProperties.getPriceLimitConfiguration().enable
				&& params.getMappedChargedType().equalsIgnoreCase("SPECIFIC_CHARGE")) {
			ResultCode limitCheckCode = PriceLimitChecker.capture(params.getMSISDN(), params.getRatingInfo().getAmount());
			if (!limitCheckCode.isSuccessCode()) {
				params.setFlagIfErrorOccured(limitCheckCode);
				return params;
			}
		}
		
		if (params.getMappedChargedType().regionMatches(true, 0, "session", 0, 7)) {
			params.setFlagIfSessionDataToBeStored();
		} else {
			params.setFlagIfCdrDataToBeStored();
		}

		/* Start*/

		Iterator<String> nodeID = params.getBillingIDset().iterator();

		System.out.println("billing id set: "+params.getBillingIDset());

		while(nodeID.hasNext())
		{
			Long bNodeid = Long.parseLong(nodeID.next());
			BillingInterface billingInterface = BillingInterfaceFactory.getInstance(bNodeid, params.getPackageInformation());


			if (billingInterface instanceof DummyBillingInterface ||
					params.getRateId().regionMatches(true, 0, "zero", 0, 4)) {
				if (params.getRatingInfo().getPulseSize() == 0) {
					params.setFlagIfErrorOccured(ResultCode.ERROR_SESSION_TIMEOUT);
				} else {
					params.setBillingResponse(BillingResponse.getSuccessDummyResponseFrom(params));
				}
				return params;
			}

			if (ApplicationProperties.getBillingInterfaceConf().packageCheckWithBalance &&
					params.getRatingInfo().getAmount() > params.getPackageInformation().getSubscriberBalance()) {
				params.setFlagIfErrorOccured(params.getPackageInformation().getResultCode());
				if (ApplicationProperties.getPriceLimitConfiguration().enable
						&& params.getMappedChargedType().equalsIgnoreCase("SPECIFIC_CHARGE")) {
					PriceLimitChecker.revert(params.getMSISDN(), params.getRatingInfo().getAmount());
				}
				return params;
			}

			billingRsp = billingInterface.chargeSubscriber(params);
			//System.out.println("Object:"+billingRsp);
			if(billingRsp.getResultCode().equalsIgnoreCase("2001"))
			{
				break;
			}
		}
		/*End*/

		//System.out.println("billing resp:"+billingRsp);
		if(billingRsp == null)
		{
			params.setBillingResponse(BillingResponse.getSuccessDummyResponseFrom(params));
			return params;
		}
		else
		    params.setBillingResponse(billingRsp);
        params.addLoggableMessage(DebugPoint.PARSED_BILLING_RESPONSE, params.getBillingResponse());
		params.getBillingIDset().clear();

		if (ApplicationProperties.getPriceLimitConfiguration().enable
				&& params.getMappedChargedType().equalsIgnoreCase("SPECIFIC_CHARGE")
				&& !params.getBillingResponse().isSuccessCode()) {
			PriceLimitChecker.revert(params.getMSISDN(), params.getRatingInfo().getAmount());
		}
		
		return params;
	}
}