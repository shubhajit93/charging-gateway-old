package com.ssdtech.java.model;

import java.util.StringTokenizer;

import com.ssdtech.java.utils.Utility;

public class BillingResponse {
	private String cdrId;
	private String serviceId;
	private String resultCode;
	private String accountingRecordType;
	private int accountingRecordNumber;
	private long reservedUnit;
	
	private BillingResponse(String cdrId, String serviceId, String resultCode, 
			                    int accountingRecordNumber,String accountingRecordType,  long reservedUnit) {
		this.cdrId = cdrId;
		this.serviceId = serviceId;
		
		this.resultCode = resultCode;
		
		this.accountingRecordNumber = accountingRecordNumber;
		this.accountingRecordType = accountingRecordType;
		this.reservedUnit = reservedUnit;
	}
	
	private BillingResponse(String resultCode) {
		this.resultCode = resultCode;
	}
	
	public String getCdrId() {
		return this.cdrId;
	}
	
	public String serviceId() {
		return this.serviceId;
	}
	
	public String getResultCode() {
		return this.resultCode;
	}
	
	public String getAccountingRecordType() {
		return this.accountingRecordType;
	}
	
	public int getAccountingRecordNumber() {
		return this.accountingRecordNumber;
	}
	
	public long getReservedUnit() {
		return this.reservedUnit;
	}
	
	public boolean isSuccessCode() {
		return this.resultCode.equalsIgnoreCase(ResultCode.SUCCESS.getAlphaNumericCode());
	}
	
	public static BillingResponse getSuccessDummyResponseFrom(ProcedureParams params) {
		return new BillingResponse(
				       params.getRequest().getCDRId(), 
				       params.getRequest().getServiceId(),
				       ResultCode.SUCCESS.getAlphaNumericCode(),
				       params.getRequest().getSequenceNo(),
				       params.getMappedChargedType(),
				       params.getRatingInfo().getPulseSize());
	}
	
	public static BillingResponse deserialize(String serializedResponse) {	
		if (serializedResponse != null) {
			serializedResponse = serializedResponse.trim();
			StringTokenizer st = new StringTokenizer(serializedResponse, " []");
			if (st.countTokens() == 6) { /* [TODO] Should not use hard coded value. */
				String cdrId = st.nextToken();
				String serviceId = st.nextToken();
				String rawResultCode = st.nextToken();
				String accoutingRecordNumber = st.nextToken();
				String accountingRecordType = st.nextToken();
				String reservedUnit = st.nextToken();
				
			    return new BillingResponse(cdrId, serviceId, rawResultCode,
			    		    Utility.isNumeric(accoutingRecordNumber) ? Integer.valueOf(accoutingRecordNumber) : 0,
			    		    accountingRecordType,
			    		    Utility.isNumeric(reservedUnit) ? Long.valueOf(reservedUnit) : 0
			    		);
			}
		}
		
		/* Erroneous serializedResponse.
		 * Thought we could set values [Apart from ResultCode of course] from ProcedureParams,
		 * They are ignored currently. Only ResultCode and isBillingSuccess() are currently needed.
		 * And isBillingSuccess() solely depends on ResultCode.
		 * */
		return new BillingResponse(ResultCode.ERROR_WRONG_RESPONSE_FORMAT.getAlphaNumericCode());
	}
	
	@Override
	public String toString() {
		return "CDR ID: " + this.cdrId + ", " +
	           "Service ID: " + this.serviceId + ", " +
			   "Result Code: " + this.resultCode + ", " + 
	           "Accounting record type: " + this.accountingRecordType + ", " +
			   "Accounting record number: " + this.accountingRecordNumber + ", " +
	           "Reserved units: " + this.reservedUnit;
	}
}
