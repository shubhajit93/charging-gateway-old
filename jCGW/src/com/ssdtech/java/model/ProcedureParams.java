package com.ssdtech.java.model;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import com.ssdtech.java.model.BillingResponse;
import com.ssdtech.java.model.RatingInfo;
import com.ssdtech.java.model.Request;
import com.ssdtech.java.model.SessionInfo;
import com.ssdtech.java.model.TPS;
import com.ssdtech.java.model.applicationproperties.ApplicationProperties;

public class ProcedureParams {	
	private Request request;
	private boolean msisdnBasedLogEnable;
	
	private String msisdn;
	private String realBNO;
	private String mappedChargingType;
	
	private PackageInfo packageInfo;
	
	private TPS tps;
	private String region;
	private String callTypeId;
	
	private String rateId;
	private long billingNodeId;
	private RatingInfo ratingInfo;
	
	private SubscriberService subscriberService;
	private Set<String> registeredSubscriptionGroupIds;
	
	private BillingResponse billingResponse;
	private String prevSerializedResponse;
	
	private Optional<SessionInfo> sessionInfo;
	
	private ServiceInfo serviceInfo;
	
	private boolean hasErrorOccured;
	private ResultCode error;
	
	private boolean storeCDRData;
	private boolean updateSessionData;
	
	private StringBuilder logBuilder;
	private Set<String> billingIDset = new LinkedHashSet<>();
	
	public ProcedureParams() {
		this.sessionInfo = Optional.empty();
		this.callTypeId = ApplicationProperties.getRatePlanConf().defaultCallTypeId;
		this.billingNodeId = -1;
	}
	
	public Request getRequest() {
		return this.request;
	}
	
	public boolean isMSISDNBasedLogEnable() {
		return this.msisdnBasedLogEnable;
	}
	
	public String getMSISDN() {
		return this.msisdn;
	}
	
	public String getRealBNO() {
		return this.realBNO;
	}
	
	public String getMappedChargedType() {
		return this.mappedChargingType;
	}
	
	public PackageInfo getPackageInformation() {
		return this.packageInfo;
	}
	
	public TPS getTPSInformation() {
		return this.tps;
	}
	
	public String getRegion() {
		return this.region;
	}
	
	public String getCallTypeId() {
		return this.callTypeId;
	}
	
	public SubscriberService getSubscriberService() {
		return this.subscriberService;
	}
	
	public Set<String> getRegisteredServices() {
		return this.registeredSubscriptionGroupIds;
	}
	
	public String getRateId() {
		return this.rateId;
	}
	
	public long getBillingNodeId() {
		return this.billingNodeId;
	}
	
	public RatingInfo getRatingInfo() {
		return this.ratingInfo;
	}
	
	public BillingResponse getBillingResponse() {
		return this.billingResponse;
	}
	
	public boolean errorOccured() {
		return this.hasErrorOccured;
	}
	
	public ResultCode getError() {
		return this.error;
	}
	
	public String getPrevSerializedResponse() {
		return this.prevSerializedResponse;
	}
	
	public Optional<SessionInfo> getSessionInfo() {
		return this.sessionInfo;
	}
	
	public boolean duplicationCDRIdCheckingEnable() {
		return ApplicationProperties.getCDRConfiguration().cdrDuplicationCheck 
				&& ApplicationProperties.duplicationCheckEnabledRequestType.contains(this.mappedChargingType.toLowerCase());
	}
	
	public boolean duplicationCDRIdInsertingEnable() {
		return  ApplicationProperties.getCDRConfiguration().cdrDuplicationCheck &&
				!this.hasErrorOccured && 
				this.billingResponse.isSuccessCode() &&
				ApplicationProperties.duplicationInsertEnabledRequestType.contains(this.mappedChargingType.toLowerCase());
	}
	
	public boolean requestToOperatorForPackageCheck() {
		return (ApplicationProperties.getBillingInterfaceConf().packageCheckForSession
				        && this.mappedChargingType.regionMatches(true, 0, "session", 0, 7)) ||
				(ApplicationProperties.getBillingInterfaceConf().packageCheckForSpecific
						&& this.mappedChargingType.equalsIgnoreCase("specific_charge"));
	}
	
	public boolean shouldStoreCdrData() {
		return this.storeCDRData;
	}
	
	public boolean shouldUpdateSessionData() {
		return this.updateSessionData;
	}
	
	public ServiceInfo getServiceInfo() {
		return this.serviceInfo;
	}
	
	public String getLoggableMessage() {
		return this.logBuilder.toString();
	}
	
	public void setRequest(Request request) {
		this.request = request;
	}
	
	public void setMSISDNBasedLog() {
		this.msisdnBasedLogEnable = true;
		this.logBuilder = new StringBuilder();
	}
	
	public void setMSISDN(String msisdn) {
		this.msisdn = msisdn;
	}
	
	public void setRealBNO(String bno) {
		this.realBNO = bno;
	}
	
	public void setMappedChargingType(String mappedChargingType) {
		this.mappedChargingType = mappedChargingType;
	}
	
	public void setPackageInformation(PackageInfo packageInfo) {
		this.packageInfo = packageInfo;
	}
	
	public void setTPSInformation(TPS tps) {
		this.tps = tps; 
	}
	
	public void setRegion(String region) {
		this.region = region;
	}
	
	public void setCallTypeId(String callTypeId) {
		this.callTypeId = callTypeId;
	}
	
	public void setSubscriberService(SubscriberService subscriberService) {
		this.subscriberService = subscriberService;
	}
	
	public void setRegisteredSubscriptionGroupIds(Set<String> registeredSubscriptionGroupIds) {
		this.registeredSubscriptionGroupIds = registeredSubscriptionGroupIds;
	}
	
	public void setRateId(String rateId) {
		this.rateId = rateId;
	}
	
	public void setBillingNodeId(long billingNodeId) {
		this.billingNodeId = billingNodeId;
	}

	public void setBillingNodeIdList(long billingNodeId) {
		this.billingIDset.add(Long.toString(billingNodeId));
	}


	//public void setBillingIDset(Set<String> billingIDset) {
	//	this.billingIDset = billingIDset;
	//}

	public void setRatingInfo(RatingInfo ratingInfo) {
		this.ratingInfo = ratingInfo;
	}

	public Set<String> getBillingIDset() {
		return billingIDset;
	}

	public void setBillingResponse(BillingResponse billingResponse) {
		this.billingResponse = billingResponse;
	}


	public void setFlagIfErrorOccured(ResultCode error) {
		this.hasErrorOccured = true;
		this.error = error;
	}
	
	public void setFlagIfErrorOccured(ResultCode error, boolean shouldStoreCdrData) {
		this.hasErrorOccured = true;
		this.error = error;
		this.storeCDRData = shouldStoreCdrData;
	}
	
	public void setPrevSerializedResponse(String prevSerializedResponse) {
		this.prevSerializedResponse = prevSerializedResponse;
	}
	
	public void setSessionInfo(Optional<SessionInfo> sessionInfo) {
		this.sessionInfo = sessionInfo;
	}
	
	public void setFlagIfCdrDataToBeStored() {
		this.storeCDRData = true;
	}
	
	public void setFlagIfSessionDataToBeStored() {
		this.updateSessionData = true;
	}
	
	public void setServiceInfo(ServiceInfo serviceInfo) {
		this.serviceInfo = serviceInfo;
	}
	
	public void addLoggableMessage(DebugPoint debugPoint, Object message) {
		if (this.msisdnBasedLogEnable) {
			DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
			this.logBuilder.append(ApplicationProperties.getLogConfiguration().instance).append("|").append(formatter.format(new Date())).append("|").append(debugPoint).append("|").append(message).append(ApplicationProperties.getLogConfiguration().lineSeparator);
		}
	}
	
	public void addLoggableMessage(DebugPoint debugPoint, String message) {
		if (this.msisdnBasedLogEnable) {
			DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
			this.logBuilder.append(ApplicationProperties.getLogConfiguration().instance).append("|").append(formatter.format(new Date())).append("|").append(debugPoint).append("|").append(message).append(ApplicationProperties.getLogConfiguration().lineSeparator);
		}
	}


	@Override
	public String toString() {
		return "ProcedureParams{" +
				"request=" + request +
				", msisdnBasedLogEnable=" + msisdnBasedLogEnable +
				", msisdn='" + msisdn + '\'' +
				", realBNO='" + realBNO + '\'' +
				", mappedChargingType='" + mappedChargingType + '\'' +
				", packageInfo=" + packageInfo +
				", tps=" + tps +
				", region='" + region + '\'' +
				", callTypeId='" + callTypeId + '\'' +
				", rateId='" + rateId + '\'' +
				", billingNodeId=" + billingNodeId +
				", ratingInfo=" + ratingInfo +
				", subscriberService=" + subscriberService +
				", registeredSubscriptionGroupIds=" + registeredSubscriptionGroupIds +
				", billingResponse=" + billingResponse +
				", prevSerializedResponse='" + prevSerializedResponse + '\'' +
				", sessionInfo=" + sessionInfo +
				", serviceInfo=" + serviceInfo +
				", hasErrorOccured=" + hasErrorOccured +
				", error=" + error +
				", storeCDRData=" + storeCDRData +
				", updateSessionData=" + updateSessionData +
				", logBuilder=" + logBuilder +
				", billingIDset=" + billingIDset +
				'}';
	}
}
