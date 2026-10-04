package com.ericsson.bean;

public class UCIPAccountBalanceBean 
{
	
	// --------- request variables ------------- //
		private String origin_node_type;
		private String origin_host_name;
		private String origin_trans_id;
		private String origin_time_stamp;
		private String subscriber_number_NAI;
		private String subscriber_number;
		private String message_flag;
		private String dedicated_acc_selection;
		//private String charging_req_info;
		private String req_sub_acc_det_flag;
		
		
		// ---------- response variables ----------- //
		private String response_code;
		private String service_class;
		private String account_value;
		private String aggregated_balance;
		private String currency;
		private String dedicated_account_id;
		private String service_expiry_date;
		private String service_removal_date;
		private String language_id;
		private boolean isBlocked = false;
		

		public UCIPAccountBalanceBean(){}
		
		public void setOriginNodeType(String value){
			origin_node_type = value;
		}
		
		public void setOriginHostName(String value){
			origin_host_name = value;
		}
		
		public void setOriginTrxId(String value){
			origin_trans_id = value;
		}
		
		public void setOriginTimeStamp(String value){
			origin_time_stamp = value;
		}
		
		public void setSubsNumberNAI(String value){
			subscriber_number_NAI = value;
		}
		
		public void setSubscriberNumber(String value){
			subscriber_number = value;
		}
		
		public void setMessageFlag(String value){
			message_flag = value;
		}
		
		public void setDedicatedAccountSelection(String value){
			dedicated_acc_selection = value;
		}
		
		public void setChargingRequestInfo(String value){
			dedicated_acc_selection = value;
		}
		
		public void setRequestSubordinateAccountDetails(String value){
			req_sub_acc_det_flag = value;
		}
		
		
		public String getOriginNodeType(){
			return origin_node_type;
		}
		
		public String getOriginHostName(){
			return origin_host_name;
		}
		
		public String getOriginTrxId(){
			return origin_trans_id;
		}
		
		public String getOriginTimeStamp(){
			return origin_time_stamp;
		}
		
		public String getSubsNumberNAI(){
			return subscriber_number_NAI;
		}
		
		public String getSubscriberNumber(){
			return subscriber_number;
		}
		
		public String getMessageFlag(){
			return message_flag;
		}
		
		public String getDedicatedAccountSelection(){
			return dedicated_acc_selection;
		}
		
		public String getChargingRequestInfo(){
			return dedicated_acc_selection;
		}
		
		public String getRequestSubordinateAccountDetails(){
			return req_sub_acc_det_flag;
		}
		
		
		
		/** --------------------- response usage -------------------------------- **/
		public String getResponseCode(){
			return response_code;
		}
		
		public String getServiceClass(){
			return service_class;
		}
		
		public String getAccountValue(){
			return account_value;
		}
		
		public String getAggregatedBalance(){
			return aggregated_balance;
		}
		
		public String getCurrency(){
			return currency;
		}
		
		public String getDedicateAccountId(){
			return dedicated_account_id;
		}
		
		public String getServiceExpiryDate(){
			return service_expiry_date;
		}
		
		public String getServiceRemovalDate(){
			return service_removal_date;
		}
		
		public String getLanguageId(){
			return language_id;
		}
		
		public boolean isAccountBlocked(){
			return isBlocked;
		}
		


		
		public void setResponseCode(String value){
			response_code = value;
		}
		
		public void setServiceClass(String value){
			service_class  = value;
		}
		
		public void setAccountValue(String value){
			account_value = value;
		}
		
		public void setAggregatedBalance(String value){
			aggregated_balance = value;
		}
		
		public void setCurrency(String value){
			currency = value;
		}
		
		public void setDedicateAccountId(String value){
			dedicated_account_id = value;
		}
		
		public void setServiceExpiryDate(String value){
			service_expiry_date = value;
		}
		
		public void setServiceRemovalDate(String value){
			service_removal_date = value;
		}
		
		public void setLanguageId(String value){
			language_id = value;
		}
		
		public void setAccountBlockedStatus(boolean value){
			isBlocked = value;
		}

}