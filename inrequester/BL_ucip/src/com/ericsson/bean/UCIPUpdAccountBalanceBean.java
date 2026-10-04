package com.ericsson.bean;

public class UCIPUpdAccountBalanceBean 
{
	
	private String originNodeType = "";
	private String originHostName = "";
	private String originTransactionID = "";
	private String originTimeStamp = "";
	private String subscriberNumberNAI = "";
	private String messageCapabilityFlag = "";
	private String subscriberNumber = "";
	private String originOperatorID = "";
	private String transactionCurrency = "";
	private String adjustmentAmountRelative = "";
	private String dedicatedAccountUpdateInformation = "";
	private String supevisionExpiryDateRelative = "";
	private String supevisionExpiryDate = "";
	private String serviceFeeExpiryDateRelative = "";
	private String serviceFeeExpiryDate = "";
	private String creditClearancePeriod = "";
	private String serviceRemovalPeriod = "";
	private String transactionType = "";
	private String transactionCode = "";
	private String externalData2 = "";
	
	private String originNodeTypeValue;
	private String originHostNameValue;
	private String originTransactionIDValue;
	private String originTimeStampValue;
	private String subscriberNumberNAIValue;
	private String messageCapabilityFlagValue;
	private String subscriberNumberValue;
	private String originOperatorIDValue;
	private String transactionCurrencyValue;
	private String adjustmentAmountRelativeValue;
	private String dedicatedAccountUpdateInformationValue;
	private String supevisionExpiryDateRelativeValue;
	private String supevisionExpiryDateValue;
	private String serviceFeeExpiryDateRelativeValue;
	private String serviceFeeExpiryDateValue;
	private String creditClearancePeriodValue;
	private String serviceRemovalPeriodValue;
	private String transactionTypeValue;
	private String transactionCodeValue;
	private String externalData2Value = "";
	
	
	public UCIPUpdAccountBalanceBean(){
		originNodeType = "originNodeType";
		originHostName = "originHostName";
		originTransactionID = "originTransactionID";
		originTimeStamp = "originTimeStamp";
		subscriberNumberNAI = "subscriberNumberNAI";
		messageCapabilityFlag = "messageCapabilityFlag";
		subscriberNumber = "subscriberNumber";
		originOperatorID = "originOperatorID";
		transactionCurrency = "transactionCurrency";
		adjustmentAmountRelative = "adjustmentAmountRelative";
		dedicatedAccountUpdateInformation = "dedicatedAccountUpdateInformation";
		supevisionExpiryDateRelative = "supevisionExpiryDateRelative";
		supevisionExpiryDate = "supevisionExpiryDate";
		serviceFeeExpiryDateRelative = "serviceFeeExpiryDateRelative";
		serviceFeeExpiryDate = "serviceFeeExpiryDate";
		creditClearancePeriod = "creditClearancePeriod";
		serviceRemovalPeriod = "serviceRemovalPeriod";
		transactionType = "transactionType";
		transactionCode = "transactionCode";
		externalData2 = "externalData2";
	}
	
	public String getOriginNodeTypeValue(){
		return originNodeTypeValue;
	}
	
	public String getOriginNodeTypeParamName(){
		return originNodeType;
	}
	
	public String getOriginHostNameValue(){
		return originHostNameValue;
	}
	
	
	public String getOriginHostNameParamName(){
		return originHostName;
	}
	
	public String getOriginTransactionIDValue(){
		return originTransactionIDValue;
	}
	
	public String getOriginTransactionIDParamName(){
		return originTransactionID;
	}
	
	public String getOriginTimeStampValue(){
		return originTimeStampValue;
	}
	
	public String getOriginTimeStampParamName(){
		return originTimeStamp;
	}
	
	public String getSubscriberNumberNAIValue(){
		return subscriberNumberNAIValue;
	}
	
	public String getSubscriberNumberNAIParamName(){
		return subscriberNumberNAI;
	}
	
	public String getMessageCapabilityFlagValue(){
		return messageCapabilityFlagValue;
	}
	
	public String getMessageCapabilityFlagParamName(){
		return messageCapabilityFlag;
	}
	
	public String getSubscriberNumberValue(){
		return subscriberNumberValue;
	}
	
	public String getSubscriberNumberParamName(){
		return subscriberNumber;
	}
	
	public String getOriginOperatorIDValue(){
		return originOperatorIDValue;
	}
	
	public String getOriginOperatorIDParamName(){
		return originOperatorID;
	}
	
	public String getTransactionCurrencyValue(){
		return transactionCurrencyValue;
	}
	
	public String getTransactionCurrencyParamName(){
		return transactionCurrency;
	}
	
	public String getAdjustmentAmountRelativeValue(){
		return adjustmentAmountRelativeValue;
	}
	
	public String getAdjustmentAmountRelativeParamName(){
		return adjustmentAmountRelative;
	}
	
	public String getDedicatedAccUpdInfoValue(){
		return dedicatedAccountUpdateInformationValue;
	}
	
	public String getDedicatedAccUpdInfoParamName(){
		return dedicatedAccountUpdateInformation;
	}
	
	public String getSupervisionExpiryDateRelativeValue(){
		return supevisionExpiryDateRelativeValue;
	}
	
	public String getSupervisionExpiryDateRelativeParamName(){
		return supevisionExpiryDateRelative;
	}
	
	public String getSupervisionExpiryDateValue(){
		return supevisionExpiryDateValue;
	}
	
	public String getSupervisionExpiryDateParamName(){
		return supevisionExpiryDate;
	}
	
	public String getSrvFeeExpiryDateRelativeValue(){
		return serviceFeeExpiryDateRelativeValue;
	}
	
	public String getSrvFeeExpiryDateRelativeParamName(){
		return serviceFeeExpiryDateRelative;
	}
	
	public String getServiceFeeExpiryDateValue(){
		return serviceFeeExpiryDateValue;
	}
	
	public String getServiceFeeExpiryDateParamName(){
		return serviceFeeExpiryDate;
	}
	
	public String getCreditClearancePeriodValue(){
		return creditClearancePeriodValue;
	}
	
	public String getCreditClearancePeriodParamName(){
		return creditClearancePeriod;
	}
	
	public String getServiceRemovalPeriodValue(){
		return serviceRemovalPeriodValue;
	}
	
	public String getServiceRemovalPeriodParamName(){
		return serviceRemovalPeriod;
	}
	
	public String getTransactionTypeValue(){
		return transactionTypeValue;
	}
	
	public String getTransactionTypeParamName(){
		return transactionType;
	}
	
	public String getTransactionCodeValue(){
		return transactionCodeValue;
	}
	
	public String getTransactionCodeParamName(){
		return transactionCode;
	}
	
	public String getExternalData2(){
		return externalData2;
	}
	
	public String getExternalData2Value(){
		return externalData2Value;
	}
	
	
	
	/**
	 * Setter
	 */
	
	public void setOriginNodeTypeValue(String value){
		this.originNodeTypeValue = value;
	}
	
	public void setOriginHostNameValue(String value){
		this.originHostNameValue = value;
	}
	
	public void setOriginTransactionIDValue(String value){
		this.originTransactionIDValue = value;
	}

	public void setOriginTimeStampValue(String value){
		this.originTimeStampValue = value;
	}

	public void setSubscriberNumberNAIValue(String value){
		this.subscriberNumberNAIValue = value;
	}

	public void setMessageCapabilityFlagValue(String value){
		this.messageCapabilityFlagValue = value;
	}

	public void setSubscriberNumberValue(String value){
		this.subscriberNumberValue = value;
	}

	public void setOriginOperatorIDValue(String value){
		this.originOperatorIDValue = value;
	}

	public void setTransactionCurrencyValue(String value){
		this.transactionCurrencyValue = value;
	}

	public void setAdjustmentAmountRelativeValue(String value){
		this.adjustmentAmountRelativeValue = value;
	}

	public void setDedicatedAccUpdInfoValue(String value){
		this.dedicatedAccountUpdateInformationValue = value;
	}
	
	public void setSupevisionExpiryDateRelativeValue(String value){
		this.supevisionExpiryDateRelativeValue = value;
	}

	public void setSupevisionExpiryDateValue(String value){
		this.supevisionExpiryDateValue = value;
	}
	
	public void setSrvFeeExpiryDateRelativeValue(String value){
		this.serviceFeeExpiryDateRelativeValue = value;
	}
	
	public void setServiceFeeExpiryDateValue(String value){
		this.serviceFeeExpiryDateValue = value;
	}
	
	public void setCreditClearancePeriodValue(String value){
		this.creditClearancePeriodValue = value;
	}
	
	public void setServiceRemovalPeriodValue(String value){
		this.serviceRemovalPeriodValue = value;
	}
	
	public void setTransactionTypeValue(String value){
		this.transactionTypeValue = value;
	}
	
	public void setTransactionCodeValue(String value){
		this.transactionCodeValue = value;
	}
	
	public void setExternalData2Value(String value){
		this.externalData2Value = value;
	}
	
	public void setExternalData2(String value){
		this.externalData2 = value;
	}

}
