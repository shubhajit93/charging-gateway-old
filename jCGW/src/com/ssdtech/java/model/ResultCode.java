package com.ssdtech.java.model;

public enum ResultCode {
    SUCCESS("2001"),
    PkgRetrievalResultCode,
	Error_BillingInterface_Disconnected("9001"),
	Error_BillingInterface_Response_Invalid("9002"),
	Error_GetRateAndBillingNode_Failed("9003"),
	ERROR_WRONG_REQUEST_FORMAT("9004"),
	ERROR_WRONG_RESPONSE_FORMAT("9005"),
	ERROR_INSERTINTOCDR("9006"),
	ERROR_Session_Que_Missing_Data("9007"),
	ERROR_Session_Que_Different_Event_Data("9008"),
	ERROR_DB_Connection_Failed("9009"),
	ERROR_DB_Query_Failed("9010"),
	ERROR_DB_StoreResult_Failed("9011"),
	Error_DB_GetRateAndBillingNode_BlankAmount("9012"),
	Error_DB_GetRateAndBillingNode_BlankPulse("9013"),
	Error_Session_Que_RaiseEventFailed("9014"),
	Error_ChargingRequest_Que_RaiseEventFailed("9015"),
	ERROR_MSISDN_INVALID_PACKAGE("9016"),
	ERROR_SESSION_TIMEOUT("9017"),
	ERROR_BILLING_WEBSERVICE_FAILED("9018"),
	ERROR_PREPAID_PACKAGE_CHECK_API_TIMEOUT("9019"),
	ERROR_POSTPAID_PACKAGE_CHECK_API_TIMEOUT("9020"),
	ERROR_EXCEEED_TPS_LIMIT("9021"),
	ERROR_IN_APPID_OR_PASSWORD("9022"),
	Error_ARI_Server_Disconnected("9023"),
	Error_ARI_Server_Response_Invalid("9024"),
	ERROR_DUPLICATE_CDR_DETECT("9025"),
	Error_DB_GetPackageIDIntegrated_BlankPackage("9026"),
	CGW_DENIED_REQUEST("9027"),
	Error_DB_BlankRateId("9028"),
	Error_Wallet_Parsing("9029"),
	Error_No_Session_Info_Found("9030"),
	Error_No_Wallet_Conf_Found("9031"),
	Error_Invalid_Specific_Charging_Amount_Range("9032"),
	Error_Price_Limit_Crossed("9033"),
	Authentication_Failed("1012"),
	Authentication_Not_Needed("1013"),
	Error_Command;

	private String alphaNumericCode;
	private String message;
	
	ResultCode() {}
	
	ResultCode(String alphaNumericCode) {
		this.alphaNumericCode = alphaNumericCode;
	}
	
	ResultCode(String alphaNumericCode, String message) {
	    this.alphaNumericCode = alphaNumericCode;
	    this.message = message;
	}
	
	public String getAlphaNumericCode() {
		return this.alphaNumericCode;
	}
	
	public String getMessage() {
		return this.message;
	}
	
	public boolean isSuccessCode() {
		return this.equals(ResultCode.SUCCESS);
	}
	
	public void setAlphaNumericCode(String alphaNumericCode) {
	    this.alphaNumericCode = alphaNumericCode;
	}
	
	public boolean equals(ResultCode resultCode) {
		return this.alphaNumericCode.equals(resultCode.getAlphaNumericCode());
	}
}
