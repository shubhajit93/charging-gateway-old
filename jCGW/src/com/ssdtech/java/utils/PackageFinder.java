package com.ssdtech.java.utils;

import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ssdtech.cgwcommon.INRequester;
import com.ssdtech.java.db.client.DBClient;
import com.ssdtech.java.in.SocketINRequester;
import com.ssdtech.java.model.DebugPoint;
import com.ssdtech.java.model.PackageInfo;
import com.ssdtech.java.model.ProcedureParams;
import com.ssdtech.java.model.ResultCode;
import com.ssdtech.java.model.applicationproperties.ApplicationProperties;

public class PackageFinder {
	private static final Logger logger = LogManager.getLogger(PackageFinder.class.getName());
	
	public void loadPackageInformationFromOperator(ProcedureParams params) {
		PackageInfo pkgInfo;
		
		if (params.requestToOperatorForPackageCheck()) {
		    Tuple<PackageInfo,Boolean> ret = this.gatherPackageInfoFromOperator(params);
		    pkgInfo = ret.first;
		    if (!ret.second) {
		    	params.setFlagIfErrorOccured(ret.first.getResultCode());
		    }
		} else {
			pkgInfo = new PackageInfo();
		}
		
		params.setPackageInformation(pkgInfo);
	}
	
    private Tuple<PackageInfo,Boolean> gatherPackageInfoFromOperator(ProcedureParams params) {
    	Optional<INRequester> requester = (ApplicationProperties.getBillingInterfaceConf().isINIntegrated) ? 
    			                               ApplicationProperties.getINRequester() :
    			                               SocketINRequester.getInstance(
    			                            		   ApplicationProperties.getBillingInterfaceConf().packageHostIp, 
    			                            		   ApplicationProperties.getBillingInterfaceConf().packageHostPort, 
    			                            		   (int) ApplicationProperties.getBillingInterfaceConf().receiveTimeout, 
    			                            		   ApplicationProperties.getBillingInterfaceConf().noOfRetryForPackageCheckWithBalance
    			                            	);
    	if (!requester.isPresent()) {
    		return new Tuple<>(new PackageInfo("INVALID", -1, ResultCode.Error_ARI_Server_Disconnected), false);
    	}
    	
    	String response = requester.get().queryBalance("package|" + params.getMSISDN());
    	
    	params.addLoggableMessage(DebugPoint.AIR_PACKAGE_INFO, "Request: package|" + params.getMSISDN() + " -> Response: " + response);
    	
		if (response == null) {
			return new Tuple<>(new PackageInfo("INVALID", -1, ResultCode.Error_ARI_Server_Response_Invalid), false);
		}
		
		response = response.trim();
		String[] pkgParts = response.split("\\|");
		if (pkgParts.length < 3) {
			return new Tuple<>(new PackageInfo("INVALID", -1, ResultCode.Error_ARI_Server_Response_Invalid), false);
		}
			
		long balance = -1L;
		ResultCode resultCode;
		String packageIdFromOperator = "";
			
		if (!pkgParts[0].equalsIgnoreCase("NULL")) {
			packageIdFromOperator = pkgParts[0];
		}
		
		if (packageIdFromOperator.equalsIgnoreCase("INVALID")) {
			return new Tuple<>(new PackageInfo(packageIdFromOperator, balance, ResultCode.ERROR_MSISDN_INVALID_PACKAGE), false);
		}
		
		try {
			balance = Long.valueOf(pkgParts[1]);
			resultCode = ResultCode.PkgRetrievalResultCode;
			resultCode.setAlphaNumericCode(pkgParts[2]);
			return new Tuple<>(new PackageInfo(packageIdFromOperator, balance, resultCode), true);
		} catch (NumberFormatException exception) {
			logger.error("Exception in received charging response [{}]", response, exception);
		}
		
		resultCode = ResultCode.Error_ARI_Server_Response_Invalid;
		return new Tuple<>(new PackageInfo(packageIdFromOperator, balance, resultCode), false);
	}
    
    public void updatePackageInformationFromLocal(ProcedureParams params) {
    	Tuple<String, String> packageInfo = this.getPackageIdFromLocal(
			    			                    params.getMSISDN(), 
			    			                    params.getPackageInformation().getPackageIdFromOperator(), 
			    			                    ApplicationProperties.getRatePlanConf().defaultPackageId
			    			                );
    	
    	params.getPackageInformation().setNodeId(packageInfo.first);
    	params.getPackageInformation().setPackageId(packageInfo.second);
    	
    	params.addLoggableMessage(DebugPoint.LOCAL_PACKAGE_INFO, "Node id: " + packageInfo.first + " -> Package id: " + packageInfo.second);
    }
    
    private Tuple<String, String> getPackageIdFromLocal(String msisdn, String packageIdFromIN, String defaultPackageId) {
		String searchingSlot = (packageIdFromIN == null || packageIdFromIN.trim().isEmpty()) ? "0" : "1";

		Optional<String> packageInfo = DBClient.searchPackageInfoFor(msisdn, searchingSlot);
		
		if (!packageInfo.isPresent()) {
			if (packageIdFromIN == null || packageIdFromIN.trim().isEmpty()) {
				return new Tuple<>(defaultPackageId, defaultPackageId);
			} else {
				return new Tuple<>(packageIdFromIN, packageIdFromIN);
			}
		} else {
			String[] packageInfoParts = packageInfo.get().split(":");
			if (packageInfoParts.length == 2) { // Package without range check.
				return new Tuple<>(packageInfoParts[0], packageInfoParts[1]);
			}
			
			/* Package with range check */
			long startMSISDN = Long.valueOf(packageInfoParts[0]);
			long endMSISDN = startMSISDN + (Long.valueOf(packageInfoParts[1]) - 1);
			long searchedMSISDN = Long.valueOf(searchingSlot + msisdn);
			if (searchedMSISDN >= startMSISDN && searchedMSISDN <= endMSISDN) {
				return new Tuple<>(packageInfoParts[2], packageInfoParts[3]);
			} else {
				if (packageIdFromIN == null || packageIdFromIN.trim().isEmpty()) {
					return new Tuple<>(defaultPackageId, defaultPackageId);
				} else {
					return new Tuple<>(packageIdFromIN, packageIdFromIN);
				}
			}
		}
	}
}
