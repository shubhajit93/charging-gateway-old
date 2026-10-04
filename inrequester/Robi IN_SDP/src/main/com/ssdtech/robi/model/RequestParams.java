package com.ssdtech.robi.model;

import com.ssdtech.robi.exceptions.RequestParamsException;
import com.ssdtech.robi.Global;
import com.ssdtech.robi.ResultCode;

public class RequestParams {
    private final String MSISDN;
	private final String TID;
	private final String serviceID;
	private final String rateID;
	private final long amount;
	private final ServiceInfo serviceInfo;
	private final String operationType;
	
	private final String commandID;
	private final int sequenceNo;
	private final String tidWithServiceID;
	private final String reservedUnits;
	
	private RequestParams(final String MSISDN, final String TID, final long amount, 
			              final String commandID, final int sequenceNo, final String tidWithServiceID,
			              final String serviceID, final String rateID, final ServiceInfo serviceInfo,
			              final String reservedUnits, final String operationType) {
		this.MSISDN = MSISDN;
		this.TID = TID;
		this.serviceID = serviceID;
		this.rateID = rateID;
		this.amount = amount;
		this.serviceInfo = serviceInfo;
		this.operationType = operationType;
		
		this.commandID = commandID;
		this.sequenceNo = sequenceNo;
		this.tidWithServiceID = tidWithServiceID;
		this.reservedUnits = reservedUnits;
	}
    
	public String getMSISDN() {
		return this.MSISDN;
	}
	
	public String getTID() {
		return this.TID;
	}
	
	public long getAmount() {
		return this.amount;
	}
	
	public String getCommandID() {
		return this.commandID;
	}
	
	public int getSequenceNo() {
		return this.sequenceNo;
	}
	
	public String getServiceID() {
		return this.serviceID;
	}
	
	public ServiceInfo getServiceInfo() {
		return this.serviceInfo;
	}
	
	public String getRateID() {
		return this.rateID;
	}
	
	public String getTIDWithServiceID() {
		return this.tidWithServiceID;
	}
	
	public String getReservedUnits() {
		return this.reservedUnits;
	}
	
	public String getOperationType() {
		return this.operationType;
	}
	
	public static class RequestParamsBuilder {
		private String TID;
		private String serviceID;
		private String rateID;
		private long amount;
		
		private String operationType;
		private ServiceInfo serviceInfo;
		
		private String MSISDN;
		
		private String commandID;
		private int sequenceNo;   // Default value '0' which is already set by JVM.
		private String tidWithServiceID;
		
		private String reservedUnits;
		
		public RequestParamsBuilder(final String TID, final String serviceID, String rateID, final long amount) throws RequestParamsException {
			this.TID = this.filteredTID(TID);
			this.serviceID = this.filteredServiceID(serviceID);
			this.rateID = this.filteredRateID(rateID);
			this.serviceInfo = this.getServiceInfo(this.rateID);
			this.amount = this.filteredAmount(amount);
			
			// Set option params' value.
			this.commandID = "SPECIFIC_CHARGE";
			this.tidWithServiceID = TID + "[SERVICE_ID_NOT_SET]";
		}
		
		public RequestParamsBuilder setCommandID(String commandID) {
			this.commandID = commandID;
			return this;
		}
		
		public RequestParamsBuilder setSequenceNo(int sequenceNo) {
			this.sequenceNo = sequenceNo;
			return this;
		}
		
		public RequestParamsBuilder setTIDWithServiceID(String tidWithServiceID) {
			this.tidWithServiceID = tidWithServiceID;
			return this;
		}
		
		public RequestParamsBuilder setChargingMSISDN(String ano, String bno, int direction) throws RequestParamsException {
			String msisdn = direction == 0 ? ano : bno;
			
			if (msisdn == null) {
				throw new RequestParamsException("MSISDN is null", ResultCode.INVALID_CGW_REQUEST);
			}
			
			String trimmedMSISDN = msisdn.trim();
			if (trimmedMSISDN.length() == 0) {
				throw new RequestParamsException("MSISDN is empty", ResultCode.INVALID_CGW_REQUEST);
			}
			
			String suffixMSISDN = null;
			try {
		        suffixMSISDN = trimmedMSISDN.substring(trimmedMSISDN.length() - Global.MSISDN_LENTH, trimmedMSISDN.length());
			} catch(IndexOutOfBoundsException exception) {
				throw new RequestParamsException("MSISDN length is less than " + Global.MSISDN_LENTH, ResultCode.INVALID_CGW_REQUEST);
			}
			
			this.MSISDN = Global.PREFIX + suffixMSISDN;
			return this;
		}
		
		public RequestParamsBuilder setReservedUnits(String reservedUnits) {
			this.reservedUnits = reservedUnits;
			return this;
		}
		
		private String filteredServiceID(String serviceID) throws RequestParamsException {
			if (serviceID == null || serviceID.trim().isEmpty()) {
			    throw new RequestParamsException("ServiceID is NULL", ResultCode.INVALID_CGW_REQUEST);
			}
			
			return serviceID.trim();
		}
		
		private ServiceInfo getServiceInfo(String rateID) throws RequestParamsException {
			ServiceInfo si = Global.rateidServiceInfoMapping.get(rateID);
			if (si == null) {
				throw new RequestParamsException("No ServiceInfo mapping found corresponding RateID :: " + rateID, ResultCode.INVALID_CGW_REQUEST);
			}
			
			return si;
		}
		
		private String filteredRateID(String rateID) throws RequestParamsException {
			if (rateID == null || rateID.trim().isEmpty()) {
				throw new RequestParamsException("RateID is NULL", ResultCode.INVALID_CGW_REQUEST);
			}
			
			return rateID.trim();
		}
		
		private String filteredTID(String TID) throws RequestParamsException {
			if (TID == null) {
				throw new RequestParamsException("TID is null", ResultCode.INVALID_CGW_REQUEST);
			}
			
			String trimmedTID = TID.trim();
			if (trimmedTID.length() == 0) {
				throw new RequestParamsException("TID length is Zero", ResultCode.INVALID_CGW_REQUEST);
			}
			
		    return trimmedTID;
		}
		
		private long filteredAmount(long amount) throws RequestParamsException {
			if (amount < 0) {
				throw new RequestParamsException("Negative amount is not allowed", ResultCode.INVALID_CGW_REQUEST);
			}
			
			return amount;
		}
		
		private void preprocessing() {
			this.TID = this.TID + this.sequenceNo;
			
			if (this.serviceID.endsWith("Renewal")) {
				this.operationType = "Renewal";
			} else {
				this.operationType = "Payment";
			}
		}
		
		public RequestParams createRequestParamsInstance() throws RequestParamsException {
			this.preprocessing();
			return new RequestParams(this.MSISDN, this.TID, this.amount, this.commandID, 
					                 this.sequenceNo, this.tidWithServiceID, this.serviceID,
					                 this.rateID, this.serviceInfo, this.reservedUnits,
					                 this.operationType);
		}
	}
}
