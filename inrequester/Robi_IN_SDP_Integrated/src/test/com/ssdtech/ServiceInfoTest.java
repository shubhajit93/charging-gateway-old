package com.ssdtech;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import com.ssdtech.robi.model.ServiceInfo;

public class ServiceInfoTest {
    private static final String SERVICE_CODE = "1234";
    private static final String SP_ID = "22202";
    private static final String SP_PASSWORD = "Robi1234";
    private static final String PRODUCT_ID = "1234";
	
	@Test
	public void checkIfServiceInformationSetCorrectly() {	
		ServiceInfo si = new ServiceInfo(SERVICE_CODE, SP_ID, SP_PASSWORD, PRODUCT_ID);
		assertEquals("Service Code does not match.", SERVICE_CODE, si.getServiceCode());
		assertEquals("SP ID does not match", SP_ID, si.getSPID());
		assertEquals("SP Password does not match", SP_PASSWORD, si.getSPPassword());
		assertEquals("Product ID does not match", PRODUCT_ID, si.getProductID());
	}
	
	@Test
	public void checkIfServiceInformationMappedCorrectlyWithServiceName() {
		Map<String, ServiceInfo> subInfoMapping = new HashMap<String, ServiceInfo>();
		ServiceInfo si = new ServiceInfo(SERVICE_CODE, SP_ID, SP_PASSWORD, PRODUCT_ID);
		subInfoMapping.put("Kids_Zone_IVR", si);
		
		ServiceInfo rsi = subInfoMapping.get("Kids_Zone_IVR");
		assertEquals("Service Code does not match.", SERVICE_CODE, rsi.getServiceCode());
		assertEquals("SP ID does not match", SP_ID, rsi.getSPID());
		assertEquals("SP Password does not match", SP_PASSWORD, rsi.getSPPassword());
		assertEquals("Product ID does not match", PRODUCT_ID, rsi.getProductID());
	}
	
	@Test
	public void checkIfSpacesAreTrimmedWhenSavingServiceInformation() {
		ServiceInfo si = new ServiceInfo(SERVICE_CODE, SP_ID + " ", SP_PASSWORD + "  ", PRODUCT_ID + "   ");
		assertEquals("Service Code does not match.", SERVICE_CODE, si.getServiceCode());
		assertEquals("SP ID does not match", SP_ID, si.getSPID());
		assertEquals("SP Password does not match", SP_PASSWORD, si.getSPPassword());
		assertEquals("Product ID does not match", PRODUCT_ID, si.getProductID());
	}
	
	@Test(expected = NullPointerException.class)
	public void checkNullPointerExceptionReturnsInCaseOfNullEntry() {
		new ServiceInfo(null, SP_ID, SP_PASSWORD, PRODUCT_ID);
	}
}
