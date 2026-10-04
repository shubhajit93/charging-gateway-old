package com.ssdtech.java.model;

public class Request {
	private String applicationId;
	private String applicationPassword;
	
	private String chargingType;
	
	private String ano;
	private String bno;
	private int direction;
	private int fixedAmount;
	private String serviceId;
	private String cdrId;
	private int sequenceNo;
	private long consumedUnitSinceLastRequest;
	private String remarks;
	private String subscriptionGroupId;
	private String channel;
	private String productcode;
	private int subscriptionCycle;


	private Request() {}
	
	public String getApplicationId() {
		return this.applicationId;
	}
	
	public String getApplicationPassword() {
		return this.applicationPassword;
	}
	
	public String getChargingType() {
		return this.chargingType;
	}
	
	public String getANO() {
		return this.ano;
	}
	
	public String getBNO() {
		return this.bno;
	}
	
	public int getDirection() {
		return this.direction;
	}
	
	public String getServiceId() {
		return this.serviceId;
	}
	
	public String getCDRId() {
		return this.cdrId;
	}
	
	public int getSequenceNo() {
		return this.sequenceNo;
	}
	
	public long getConsumedUnitSinceLastRequest() {
		return this.consumedUnitSinceLastRequest;
	}
	
	public String getRemarks() {
		return this.remarks;
	}
	
	public String getSubscriptionGroupId() {
		return this.subscriptionGroupId;
	}
	
	public String getChannel() {
		return this.channel;
	}

	public String getproductcode() {
		return this.productcode;
	}
	public int getfixedAmount() {
		return this.fixedAmount;
	}
	
	public boolean isSubscriptionGroupIdApplicable() {
		return !this.subscriptionGroupId.equalsIgnoreCase("na");
	}


	public int getSubscriptionCycle() {
		return subscriptionCycle;
	}

	@Override
	public String toString() {
		return "Application id: " + this.applicationId + ", " +
	           "Application password: " + this.applicationPassword + ", " +
			   "Charging type: " + this.chargingType + ", " +
	           "MSISDN: " + this.ano + ", " +
			   "BNI: " + this.bno + ", " +
	           "Direction: " + this.direction + ", " +
			   "Service id: " + this.serviceId + ", " +
	           "Sequence no: " + this.sequenceNo + ", " +
			   "Consumed unit since last request: " + this.consumedUnitSinceLastRequest + ", " +
	           "Subscription group id: " + this.subscriptionGroupId + ", " +
			   "Channel: " + this.channel + ", " +
				"Productcode: " + this.productcode + ", " +
				"fixedAmount: " + this.fixedAmount + ", "+
				"subscriptionCycle: " + this.subscriptionCycle;
	}

	public static class Builder {
		private Request request;
		
		public Builder() {
			this.request = new Request();
			
			// Set optional parameters.
			this.request.remarks = "";
			this.request.subscriptionGroupId = "na";
			this.request.channel = "na";
			this.request.productcode = "";
			this.request.fixedAmount = -1;
		}
		
		public Request build() {
			Request builtRequest = this.request;
			this.request = new Request();
			return builtRequest;
		}
		
		public Builder setApplicationId(String applicationId) {
			this.request.applicationId = applicationId;
			return this;
		}
		
		public Builder setApplicationPassword(String applicationPassword) {
			this.request.applicationPassword = applicationPassword;
			return this;
		}
		
		public Builder setChargingType(String chargingType) {
			this.request.chargingType = chargingType;
			return this;
		}
		
		public Builder setANO(String ano) {
			this.request.ano = ano;
			return this;
		}
		
		public Builder setBNO(String bno) {
			this.request.bno = bno;
			return this;
		}
		
		public Builder setDirection(int direction) {
			this.request.direction = direction;
			return this;
		}
		
		public Builder setServiceId(String serviceId) {
			this.request.serviceId = serviceId;
			return this;
		}
		
		public Builder setCDRId(String cdrId) {
			this.request.cdrId = cdrId;
			return this;
		}
		
		public Builder setSequenceNo(int sequenceNo) {
			this.request.sequenceNo = sequenceNo;
			return this;
		}
		
		public Builder setConsumedUnitSinceLastRequest(long consumedUnitSinceLastRequest) {
			this.request.consumedUnitSinceLastRequest = consumedUnitSinceLastRequest;
			return this;
		}
		
		public Builder setRemarks(String remarks) {
			this.request.remarks = remarks;
			return this;
		}
		
		public Builder setSubscriptionGroupId(String subscriptionGroupId) {
			this.request.subscriptionGroupId = subscriptionGroupId;
			return this;
		}
		
		public Builder setChannel(String channel) {
			this.request.channel = channel;
			return this;
		}

		public Builder setProductcode(String productcode) {
			this.request.productcode = productcode;
			return this;
		}

		public Builder setfixedAmount(int fixedAmount) {
			this.request.fixedAmount = fixedAmount;
			return this;
		}


		public Builder setSubscriptionCycle(int cycle) {
			this.request.subscriptionCycle = cycle;
			return this;
		}

	}
}
