package com.ssdtech;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

import java.util.HashMap;

import com.ssdtech.robi.Global;
import com.ssdtech.robi.exceptions.RequestParamsException;
import com.ssdtech.robi.model.RequestParams;
import com.ssdtech.robi.model.ServiceInfo;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.runner.RunWith;

@RunWith(JUnitParamsRunner.class)
public class RequestParamsTester {
	public static final String MSISDN = "8801899999999";
	public static final String TID = "12345678912345678912";
	public static final String SERVICE_ID = "service_id";
	public static final String RATE_ID = "rate_id";
	public static final String BNO = "1111";
	public static final long AMOUNT = 100L;
    
	@Before
	public void setUp() {
		String serviceCode = "1000";
		String spId = "1100";
		String spPassword = "1110";
		String productID = "1111";
		
		Global.MSISDN_LENTH = 10;
		Global.PREFIX = "880";
		
		ServiceInfo newSi = new ServiceInfo(serviceCode, spId, spPassword, productID);
		Global.rateidServiceInfoMapping = new HashMap<String, ServiceInfo>();
		Global.rateidServiceInfoMapping.put("rate_id", newSi);
		Global.rateidServiceInfoMapping.put("rate_id", newSi);
	}
	
	public static Object[] getInvalidMSISDN() {
		return new Object[] {
				null,
				"",
				" ",
				"   ",
				"89999999"
		};
	}
	
	public static Object[] getValidMSISDN() {
		return new Object[] {
				new Object[] {"8801899999999", BNO, 0, "8801899999999"},
				new Object[] {"01899999999", BNO, 0, "8801899999999"},
				new Object[] {"1899999999", BNO, 0, "8801899999999"},
				new Object[] {BNO, "8801899999999", 1, "8801899999999"},
				new Object[] {BNO, "01899999999", 1, "8801899999999"},
				new Object[] {BNO, "1899999999", 1, "8801899999999"},
		};
	}
	
	@Test
	@Parameters(method = "getValidMSISDN")
	public void checkIfMSISDNCanBeSet(String MSISDN, String bno, int direction, String expectedMSISDN) {
		RequestParams rp = null;
		try {
			rp = new RequestParams.RequestParamsBuilder(TID, SERVICE_ID, RATE_ID, AMOUNT)
					                  .setChargingMSISDN(MSISDN, bno, direction)
					                  .createRequestParamsInstance();
		} catch (RequestParamsException e) {
			e.printStackTrace();
		}
		assertEquals("MSISDN can not be set properly.", expectedMSISDN, rp.getMSISDN());
	}
	
	@Test(expected = RequestParamsException.class)
	@Parameters(method = "getInvalidMSISDN")
	public void checkIfExceptionThrowsForInvalidMSISDN(String MSISDN) throws RequestParamsException {
		RequestParams rp = new RequestParams.RequestParamsBuilder(TID, SERVICE_ID, RATE_ID, AMOUNT)
				                               .setChargingMSISDN(MSISDN, BNO, 0) 
				                               .createRequestParamsInstance();
	}
	
	@Test
	public void checkIfTIDCanBeSet() {
		String cdrID = "12345678912345678912";
		String expectedTID = "123456789123456789120";
		RequestParams rp = null;
		try {
			rp = new RequestParams.RequestParamsBuilder(cdrID, SERVICE_ID, RATE_ID, AMOUNT).createRequestParamsInstance();
		} catch (RequestParamsException e) {
			e.printStackTrace();
		}
		assertEquals("TID can not be set properly.", expectedTID, rp.getTID());
	}
	
	public static Object[] getInvalidTID() {
		return new Object[] {
				null,
				"",
				" ",
				"   "
		};
	}
	
	/**
	 * No restriction on TID length.
	 * @param tid
	 * @throws RequestParamsException
	 */
	@Test(expected = RequestParamsException.class)
	@Parameters(method = "getInvalidTID")
	public void checkIfExceptionThrowsForInvalidTID(String tid) throws RequestParamsException {
		RequestParams rp = new RequestParams.RequestParamsBuilder(tid, SERVICE_ID, RATE_ID, AMOUNT).createRequestParamsInstance();
	}
	
	@Test
	public void checkIfAmountCanBeSet() {
		long amount = 1000L;
		RequestParams rp = null;
		try {
			rp = new RequestParams.RequestParamsBuilder(TID, SERVICE_ID, RATE_ID, amount).createRequestParamsInstance();
		} catch (RequestParamsException e) {
			e.printStackTrace();
		}
		assertEquals("Amount can not be set properly.", amount, rp.getAmount());
	}
	
	@Test
	public void checkIfFractionAmountIsTrimmedDownBeforeSet() {
		long amount = 1000L;
		RequestParams rp = null;
		try {
			rp = new RequestParams.RequestParamsBuilder(TID, SERVICE_ID, RATE_ID, (long)1000.00).createRequestParamsInstance();
		} catch (RequestParamsException e) {
			e.printStackTrace();
		}
		assertEquals("Amount can not be set properly.", amount, rp.getAmount());
	}
	
	@Test(expected = RequestParamsException.class)
	public void checkIfExceptionThrowsForNegativeAmount() throws RequestParamsException {
		long amount = -1L;
		new RequestParams.RequestParamsBuilder(TID, SERVICE_ID, RATE_ID, amount).createRequestParamsInstance();
	}
	
	@Test
	public void checkIfCommandIDCanBeSetCorrectly() throws RequestParamsException {
		String commandID = "SPECIFIC_CHARGE";
		RequestParams rp = new RequestParams.RequestParamsBuilder(TID, SERVICE_ID, RATE_ID, AMOUNT)
		                                        .setCommandID(commandID)
		                                        .createRequestParamsInstance();
		assertEquals("COMMAND ID can not be set properly.", commandID, rp.getCommandID());
	}
	
	@Test
	public void checkDefaultCommandIDSetIfNotPassed() throws RequestParamsException {
		String defaultCommandId = "SPECIFIC_CHARGE";
		RequestParams rp = new RequestParams.RequestParamsBuilder(TID, SERVICE_ID, RATE_ID, AMOUNT)
				                                 .createRequestParamsInstance();
		assertEquals("Default command id 'SPECIFIC_CHARGE' can not be set", defaultCommandId, rp.getCommandID());
	}
	
	@Test
	public void checkIfSequenceNoCanBeSetProperly() throws RequestParamsException {
		int sequenceNo = 0;
		RequestParams rp = new RequestParams.RequestParamsBuilder(TID, SERVICE_ID, RATE_ID, AMOUNT)
				                                .setSequenceNo(sequenceNo)
				                                .createRequestParamsInstance();
		assertEquals("Sequence no can not be set properly", sequenceNo, rp.getSequenceNo());
	}
	
	@Test
	public void checkDefaultSequenceNoSetIfNotPassed() throws RequestParamsException {
		int defaultSequenceNo = 0;
		RequestParams rp = new RequestParams.RequestParamsBuilder(TID, SERVICE_ID, RATE_ID, AMOUNT)
				                                 .createRequestParamsInstance();
		assertEquals("Default command id 'SEQUENCE_NO' can not be set", defaultSequenceNo, rp.getSequenceNo());
	}
	
	@Test
	public void checkIfTIDWithServiceTypeCanBeSetProperly() throws RequestParamsException {
		String tidWithServiceId = "12345678912345678912[test]";
		RequestParams rp = new RequestParams.RequestParamsBuilder(TID, SERVICE_ID, RATE_ID, AMOUNT)
				                                .setTIDWithServiceID(tidWithServiceId)
				                                .createRequestParamsInstance();
		assertEquals("TID with ServiceID can not be set properly", tidWithServiceId, rp.getTIDWithServiceID());
	}
	
	@Test
	public void checkDefaultTIDWithServiceIDSetIfNotPassed() throws RequestParamsException {
		String defaultTidWithServiceID = TID + "[SERVICE_ID_NOT_SET]";
		RequestParams rp = new RequestParams.RequestParamsBuilder(TID, SERVICE_ID, RATE_ID, AMOUNT)
				                                 .createRequestParamsInstance();
		assertEquals("Default TID with Service ID can not be set properly", defaultTidWithServiceID, rp.getTIDWithServiceID());
	}
	
	@Test
	public void checkIfServiceIDCanBeSetProperly() throws RequestParamsException {
		String serviceID = "service_id";
		RequestParams rp = new RequestParams.RequestParamsBuilder(TID, serviceID, RATE_ID, AMOUNT)
                                                .createRequestParamsInstance();
		assertEquals("Service ID can not be set properly", serviceID, rp.getServiceID());
	}
	
	public static Object[] getInvalidServiceID() {
		return new Object[] {
			null,
			"",
			" ",
			"   "
		};
	}
	
	@Test(expected = RequestParamsException.class)
	@Parameters(method = "getInvalidServiceID")
	public void checkIfExceptionReturnsWhenServiceIDIsInvalid(String serviceID) throws RequestParamsException {
		new RequestParams.RequestParamsBuilder(TID, serviceID, RATE_ID, AMOUNT)
                         .createRequestParamsInstance();
	}
	
	@Test
	public void checkIfRateIDCanBeSetProperly() throws RequestParamsException {
		String rateID = "rate_id";
		RequestParams rp = new RequestParams.RequestParamsBuilder(TID, SERVICE_ID, rateID, AMOUNT)
                                                .createRequestParamsInstance();
		assertEquals("Rate ID can not be set properly", rateID, rp.getRateID());
	}
	
	public static Object[] getInvalidRateID() {
		return new Object[] {
			null,
			"",
			" ",
			"   "
		};
	}
	
	@Test(expected = RequestParamsException.class)
	@Parameters(method = "getInvalidRateID")
	public void checkIfExceptionReturnsWhenRateIDIsInvalid(String rateID) throws RequestParamsException {
		new RequestParams.RequestParamsBuilder(TID, SERVICE_ID, rateID, AMOUNT)
                         .createRequestParamsInstance();
	}
	
	public static Object[] getSequenceNo() {
		return new Object[] {
			0,
			1
		};
	}
	
	@Test
	@Parameters(method = "getSequenceNo")
	public void checkIfSequenceNumberAddedAfterCDRID(int sequenceNo) throws RequestParamsException {
		RequestParams rp = new RequestParams.RequestParamsBuilder(TID, SERVICE_ID, RATE_ID, AMOUNT)
				                            .setSequenceNo(sequenceNo)
                                            .createRequestParamsInstance();
		assertEquals("TID <> CDR ID + Sequence No", TID + sequenceNo, rp.getTID());
	}
	
	@Test
	public void checkIfRequestParamsObjectHoldCorrectServiceInfoObject() throws RequestParamsException {
		String serviceCode = "1000";
		String spId = "1100";
		String spPassword = "1110";
		
		RequestParams rp = new RequestParams.RequestParamsBuilder(TID, SERVICE_ID, RATE_ID, AMOUNT)
								                .createRequestParamsInstance();
		
		ServiceInfo si = rp.getServiceInfo();
		assertEquals("Service code does not set properly", serviceCode, si.getServiceCode());
		assertEquals("SP ID does not set properly", spId, si.getSPID());
		assertEquals("SP Password does not set properly", spPassword, si.getSPPassword());
	}
	
	@Test
	public void checkIfReservedUnitsSetCorrectly() throws RequestParamsException {
		String expectedReservedUnits = "180";
		RequestParams rp = new RequestParams.RequestParamsBuilder(TID, SERVICE_ID, RATE_ID, AMOUNT)
				                                 .setReservedUnits(expectedReservedUnits)
                                            .createRequestParamsInstance();
		assertEquals("Reserved units can not be set.", expectedReservedUnits, rp.getReservedUnits());
	}
	
	public static Object[] getOperationType() {
		return new Object[] {
	        new Object[] {"service_idRenewal", "Renewal"},
	        new Object[] {"service_id", "Payment"}
		};
	}
	
	@Test
	@Parameters(method = "getOperationType")
	public void checkIfOperationTypeSetProperly(String serviceId, String operationType) throws RequestParamsException {
		RequestParams rp = new RequestParams.RequestParamsBuilder(TID, serviceId, RATE_ID, AMOUNT)
								            .createRequestParamsInstance();
		
		assertEquals("Correct operation type not set", operationType, rp.getOperationType());
	}
}
