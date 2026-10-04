package com.ericsson.bean;

public class UCIPAccountDetailBean {

	private String origin_node_type;
	private String origin_host_name;
	private String origin_trans_id;
	private String origin_time_stamp;
	private String subscriber_number_NAI;
	private String subscriber_number;
	private String message_flag;
	private String info_flag;

	// response variables
	private String response_code;
	private String language_id;
	private String service_class;
	private String master_acc_id;
	private boolean isblocked = false;
	private String service_id;
	private boolean service_active_flag = false;
	private String acc_status;
	private String acc_expiry_date;
	private String acc_active_period;
	private String acc_credit;
	private String currency;

	public UCIPAccountDetailBean() {
	}

	/** --- setter --- **/
	public void setOriginNodeType(String value) {
		origin_node_type = value;
	}

	public void setOriginHostName(String value) {
		origin_host_name = value;
	}

	public void setOriginTrxId(String value) {
		origin_trans_id = value;
	}

	public void setOriginTimeStamp(String value) {
		origin_time_stamp = value;
	}

	public void setSubsNumberNAI(String value) {
		subscriber_number_NAI = value;
	}

	public void setSubscriberNumber(String value) {
		subscriber_number = value;
	}

	public void setMessageFlag(String value) {
		message_flag = value;
	}

	public void setInfoFlag(String value) {
		info_flag = value;
	}

	/** --- getter --- **/
	public String getOriginNodeType() {
		return origin_node_type;
	}

	public String getOriginHostName() {
		return origin_host_name;
	}

	public String getOriginTrxId() {
		return origin_trans_id;
	}

	public String getOriginTimeStamp() {
		return origin_time_stamp;
	}

	public String getSubsNumberNAI() {
		return subscriber_number_NAI;
	}

	public String getSubscriberNumber() {
		return subscriber_number;
	}

	public String getMessageFlag() {
		return message_flag;
	}

	public String getInfoFlag() {
		return info_flag;
	}

	/* ----------------- response variables ------------------------------ */
	public String getResponseCode() {
		return response_code;
	}

	public String getLanguageId() {
		return language_id;
	}

	public String getServiceClass() {
		return service_class;
	}

	public String getMasterAccountId() {
		return master_acc_id;
	}

	public boolean getIsBlocked() {
		return isblocked;
	}

	public String getServiceId() {
		return service_id;
	}

	public boolean getServiceActiveFlag() {
		return service_active_flag;
	}

	public String getAccountStatus() {
		return acc_status;
	}

	public String getAccountExpiryDate() {
		return acc_expiry_date;
	}

	public String getAccountActivePeriod() {
		return acc_active_period;
	}

	public String getAccountCredit() {
		return acc_credit;
	}

	public String getCurrency() {
		return currency;
	}

	public void setResponseCode(String response_code) {
		this.response_code = response_code;
	}

	public void setLanguageId(String language_id) {
		this.language_id = language_id;
	}

	public void setServiceClass(String service_class) {
		this.service_class = service_class;
	}

	public void setMasterAccountId(String master_acc_id) {
		this.master_acc_id = master_acc_id;
	}

	public void setIsBlocked(boolean value) {
		this.isblocked = value;
	}

	public void setServiceId(String service_id) {
		this.service_id = service_id;
	}

	public void setServiceActiveFlag(boolean value) {
		this.service_active_flag = value;
	}

	public void setAccountStatus(String acc_status) {
		this.acc_status = acc_status;
	}

	public void setAccountExpiryDate(String acc_expiry_date) {
		this.acc_expiry_date = acc_expiry_date;
	}

	public void setAccountActivePeriod(String acc_active_period) {
		this.acc_active_period = acc_active_period;
	}

	public void setAccountCredit(String acc_credit) {
		this.acc_credit = acc_credit;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

}
