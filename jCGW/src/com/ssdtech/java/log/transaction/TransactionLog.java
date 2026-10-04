package com.ssdtech.java.log.transaction;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.ssdtech.java.log.transaction.TransactionLogException;

public class TransactionLog implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private static final String FIELD_SEPARATOR = "|";
	private static final int TOTAL_FIELDS = 27;
	
	private String cdrId;
	private String serviceId;
	private String chargingType;
	private String cgwId;
	private String ano;
	private String bno;
	private int direction;
	private String rateId;
	private String resultCode;
	private long amount;
	private String startTime;
	private String endTime;
	private String purpose;
	private String packageId;
	private int billingPort;
	private String cdrWriteTime;
	private String cdrFilePath;
	private String cdrWriteStatus;
	private String cdrIdFile;
	private String serialNo;
	private String userId;
	private String lastUpdate;
	private String subscriptionStatus;
	private String refundAmount;
	private long totalConsumedUnit;
	private String device;
	private long balanceBefore;
	private String productcode;

	private TransactionLog(String cdrId, String serviceId, String chargingType, String cgwId,
			                   String ano, String bno, int direction, String resultCode, String purpose) {
		this.cdrId = cdrId;
		this.serviceId = serviceId;
		this.chargingType = chargingType;
		this.cgwId = cgwId;
		this.ano = ano;
		this.bno = bno;
		this.direction = direction;
		this.resultCode = resultCode;
		this.purpose = purpose;
	}

	public String getProductcode() {
		return productcode;
	}


	public String getCdrId() {
		return this.cdrId;
	}

	public String getServiceId() {
		return this.serviceId;
	}

	public String getChargingType() {
		return this.chargingType;
	}

	public String getCgwId() {
		return this.cgwId;
	}

	public String getAno() {
		return this.ano;
	}

	public String getBno() {
		return this.bno;
	}

	public int getDirection() {
		return this.direction;
	}

	public String getRateId() {
		return this.rateId;
	}

	public String getResultCode() {
		return this.resultCode;
	}

	public long getAmount() {
		return this.amount;
	}

	public String getStartTime() {
		return this.startTime;
	}

	public String getEndTime() {
		return this.endTime;
	}

	public String getPurpose() {
		return this.purpose;
	}

	public String getPackageId() {
		return this.packageId;
	}

	public int getBillingPort() {
		return this.billingPort;
	}

	public String getCdrWriteTime() {
		return this.cdrWriteTime;
	}

	public String getCdrFilePath() {
		return this.cdrFilePath;
	}

	public String getCdrWriteStatus() {
		return this.cdrWriteStatus;
	}

	public String getCdrIdFile() {
		return this.cdrIdFile;
	}

	public String getSerialNo() {
		return this.serialNo;
	}

	public String getUserId() {
		return this.userId;
	}

	public String getLastUpdate() {
		return this.lastUpdate;
	}

	public String getSubscriptionStatus() {
		return this.subscriptionStatus;
	}

	public String getRefundAmount() {
		return this.refundAmount;
	}

	public long getTotalConsumedUnit() {
		return this.totalConsumedUnit;
	}

	public String getDevice() {
		return this.device;
	}

	public long getBalanceBefore() {
		return this.balanceBefore;
	}

	public String serialize() {
		return String.join(FIELD_SEPARATOR, this.cdrId, 
				                            this.serviceId, 
				                            this.chargingType, 
				                            this.cgwId, 
				                            this.ano, 
				                            this.bno, 
				                            String.valueOf(this.direction),
				                            this.rateId,
				                            this.resultCode,
				                            String.valueOf(this.amount),
				                            this.startTime,
				                            this.endTime,
				                            this.purpose,
				                            this.packageId,
				                            String.valueOf(this.billingPort),
				                            this.cdrWriteTime,
				                            this.cdrFilePath,
				                            this.cdrWriteStatus,
				                            this.cdrIdFile,
				                            this.serialNo,
				                            this.userId,
				                            this.lastUpdate,
				                            this.subscriptionStatus,
				                            String.valueOf(this.refundAmount),
				                            String.valueOf(this.totalConsumedUnit),
				                            this.device,
				                            String.valueOf(this.balanceBefore),
											this.productcode);
	}
	
	public static TransactionLog deserialize(String serializedTransaction) throws TransactionLogException {
		if (serializedTransaction == null) throw new TransactionLogException("Not a serialized Transaction log.", null);
		
		String[] transactionParts = serializedTransaction.split("\\" + FIELD_SEPARATOR);
		if (transactionParts.length < TOTAL_FIELDS) throw new TransactionLogException("Invalid serialized Transaction log.", null);
		
		Builder build = new TransactionLog.Builder(transactionParts[TransactionFieldIndex.CDR_ID], 
				                                       transactionParts[TransactionFieldIndex.SERVICE_ID],
				                                       transactionParts[TransactionFieldIndex.CHARGING_TYPE], 
				                                       transactionParts[TransactionFieldIndex.CGW_ID], 
				                                       transactionParts[TransactionFieldIndex.ANO], 
				                                       transactionParts[TransactionFieldIndex.BNO], 
				                                       Integer.valueOf(transactionParts[TransactionFieldIndex.DIRECTION]), 
				                                       transactionParts[TransactionFieldIndex.RESULT_CODE], 
				                                       transactionParts[TransactionFieldIndex.PURPOSE]);
		
		build.setAmount(Long.valueOf(transactionParts[TransactionFieldIndex.AMOUNT]));
		build.setBalanceBefore(Long.valueOf(transactionParts[TransactionFieldIndex.BALANCE_BEFORE]));
		build.setBillingNode(Integer.valueOf(transactionParts[TransactionFieldIndex.BILLING_PORT]));
		build.setCDRFilePath(transactionParts[TransactionFieldIndex.CDR_FILE_PATH]);
		build.setCDRIdFile(transactionParts[TransactionFieldIndex.CDR_ID_FILE]);
		build.setCDRWriteStatus(transactionParts[TransactionFieldIndex.CDR_WRITE_STATUS]);
		build.setCDRWriteTime(transactionParts[TransactionFieldIndex.CDR_WRITE_TIME]);
		build.setDevice(transactionParts[TransactionFieldIndex.DEVICE]);
		build.setEndTime(transactionParts[TransactionFieldIndex.END_TIME]);
		build.setLastUpdate(transactionParts[TransactionFieldIndex.LAST_UPDATE]);
		build.setPackageId(transactionParts[TransactionFieldIndex.PACKAGE_ID]);
		build.setRateId(transactionParts[TransactionFieldIndex.RATE_ID]);
		build.setRefundAmount(transactionParts[TransactionFieldIndex.REFUND_AMOUNT]);
		build.setSerialNo(transactionParts[TransactionFieldIndex.SERIAL_NO]);
		build.setStartTime(transactionParts[TransactionFieldIndex.START_TIME]);
		build.setSubscriptionStatus(transactionParts[TransactionFieldIndex.SUBSCRIPTION_STATUS]);
		build.setTotalConsumedUnit(Long.valueOf(transactionParts[TransactionFieldIndex.TOTAL_CONSUMED_UNIT]));
		build.setUserId(transactionParts[TransactionFieldIndex.USER_ID]);
		
		return build.build();
	}
	
	public static class Builder {
		private DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		private TransactionLog transactionLog;
		
		public Builder(String cdrId, String serviceId, String chargingType, String cgwId, 
				           String ano, String bno, int direction, String resultCode, String purpose) {
			this.transactionLog = new TransactionLog(cdrId, serviceId, chargingType, cgwId, ano, bno, direction, resultCode, purpose);
		}
		
		public TransactionLog build() {
			return this.transactionLog;
		}
		
		public Builder setRateId(String rateId) {
			this.transactionLog.rateId = rateId;
			return this;
		}
		
		public Builder setAmount(long amount) {
			this.transactionLog.amount = amount;
			return this;
		}
		
		public Builder setStartTime(Date startTime) {
			this.transactionLog.startTime = this.dateFormatter.format(startTime);
			return this;
		}
		
		public Builder setStartTime(String startTime) {
			this.transactionLog.startTime = startTime;
			return this;
		}
		
		public Builder setEndTime(Date endTime) {
			this.transactionLog.endTime = this.dateFormatter.format(endTime);
			return this;
		}
		
		public Builder setEndTime(String endTime) {
			this.transactionLog.endTime = endTime;
			return this;
		}
		
		public Builder setPackageId(String packageId) {
			this.transactionLog.packageId = packageId;
			return this;
		}
		
		public Builder setBillingNode(int billingPort) {
			this.transactionLog.billingPort = billingPort;
			return this;
		}
		
		public Builder setCDRWriteTime(Date cdrWriteTime) {
			this.transactionLog.cdrWriteTime = this.dateFormatter.format(cdrWriteTime);
			return this;
		}
		
		public Builder setCDRWriteTime(String cdrWriteTime) {
			this.transactionLog.cdrWriteTime = cdrWriteTime;
			return this;
		}
		
		public Builder setCDRFilePath(String cdrFilePath) {
			this.transactionLog.cdrFilePath = cdrFilePath;
			return this;
		}
		
		public Builder setCDRWriteStatus(String cdrWriteStatus) {
			this.transactionLog.cdrWriteStatus = cdrWriteStatus;
			return this;
		}
		
		public Builder setCDRIdFile(String cdrIdFile) {
			this.transactionLog.cdrIdFile = cdrIdFile;
			return this;
		}
		
		public Builder setSerialNo(String serialNo) {
			this.transactionLog.serialNo = serialNo;
			return this;
		}
		
		public Builder setUserId(String userId) {
			this.transactionLog.userId = userId;
			return this;
		}
		
		public Builder setLastUpdate(Date lastUpdate) {
			this.transactionLog.lastUpdate = this.dateFormatter.format(lastUpdate);
			return this;
		}
		
		public Builder setLastUpdate(String lastUpdate) {
			this.transactionLog.lastUpdate = lastUpdate;
			return this;
		}
		
		public Builder setSubscriptionStatus(String subscriptionStatus) {
			this.transactionLog.subscriptionStatus = subscriptionStatus;
			return this;
		}
		
		public Builder setRefundAmount(String refundAmount) {
			this.transactionLog.refundAmount = refundAmount;
			return this;
		}
		
		public Builder setTotalConsumedUnit(long totalConsumedUnit) {
			this.transactionLog.totalConsumedUnit = totalConsumedUnit;
			return this;
		}
		
		public Builder setDevice(String device) {
			this.transactionLog.device = device;
			return this;
		}
		
		public Builder setBalanceBefore(long balanceBefore) {
			this.transactionLog.balanceBefore = balanceBefore;
			return this;
		}
		public Builder setProductcode(String productcode) {
			this.transactionLog.productcode = productcode;
			return this;
		}

	}
	
	private static class TransactionFieldIndex {
		public static final int CDR_ID = 0;
		public static final int SERVICE_ID = 1;
		public static final int CHARGING_TYPE = 2;
		public static final int CGW_ID = 3;
		public static final int ANO = 4;
		public static final int BNO = 5;
		public static final int DIRECTION = 6;
		public static final int RATE_ID = 7;
		public static final int RESULT_CODE = 8;
		public static final int AMOUNT = 9;
		public static final int START_TIME = 10;
		public static final int END_TIME = 11;
		public static final int PURPOSE = 12;
		public static final int PACKAGE_ID = 13;
		public static final int BILLING_PORT = 14;
		public static final int CDR_WRITE_TIME = 15;
		public static final int CDR_FILE_PATH = 16;
		public static final int CDR_WRITE_STATUS = 17;
		public static final int CDR_ID_FILE = 18;
		public static final int SERIAL_NO = 19;
		public static final int USER_ID = 20;
		public static final int LAST_UPDATE = 21;
		public static final int SUBSCRIPTION_STATUS = 22;
		public static final int REFUND_AMOUNT = 23;
		public static final int TOTAL_CONSUMED_UNIT = 24;
		public static final int DEVICE = 25;
		public static final int BALANCE_BEFORE = 26;
		public static final int PRODUCTCODE = 27;
	}
}
