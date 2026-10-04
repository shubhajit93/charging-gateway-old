package com.ssdtech;

import java.io.StringWriter;
import java.util.HashMap;

import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;

import org.junit.Before;
import org.junit.Test;

import com.ssdtech.robi.Global;
import com.ssdtech.robi.exceptions.RequestParamsException;
import com.ssdtech.robi.model.RequestParams;
import com.ssdtech.robi.model.ServiceInfo;
import com.ssdtech.robi.requestgenerator.RenewalSOAPMessageGenerator;
import com.ssdtech.robi.requestgenerator.SOAPMessageGenerator;

public class RenewalSOAPMessageGeneratorTest {
	private static final String USERID = "8801899999999";
	private static final String SERVICE_NAME = "service_id";
	private static final String RATE_ID = "rate_id";
	private static final String SP_CODE = "200033";
	private static final String SP_ID = "200033";
	private static final String STAND_ALONE_PASS = "Robi1234";
	private static final String PRODUCT_ID = "0300405271";
	
    ServiceInfo newSi = new ServiceInfo(SP_CODE, SP_ID, STAND_ALONE_PASS, PRODUCT_ID);
    
	@Before
	public void setUp() {
		Global.rateidServiceInfoMapping = new HashMap<String, ServiceInfo>();
		Global.rateidServiceInfoMapping.put(RATE_ID, newSi);
		
		Global.RENEWAL_SPECIFIC_DATA = new HashMap<String,String>();
		Global.RENEWAL_SPECIFIC_DATA.put("actionType", "1");
		
		Global.V2_URL = "http://www.huawei.com.cn/schema/common/v2_1";
		Global.LOC_URL2 = "http://www.sdp.com/schema/subscription/v1_0/local";
		
		Global.MSISDN_LENTH = 10;
		Global.PREFIX = "18";
	}
	
	@Test
	public void checkIfCorrectSOAPResponseGeneratedForRenewal() throws Exception {
		RequestParams rp = new RequestParams.RequestParamsBuilder("123456789123456", SERVICE_NAME, "rate_id", 100L)
								                .setChargingMSISDN(USERID, "1111", 0)
								                .createRequestParamsInstance();
		
		SOAPMessageGenerator generator = new RenewalSOAPMessageGenerator(null);
		SOAPMessage message = generator.generate(rp);
	}
	
	private static String getSOAPResponseInString(SOAPMessage soapResponse) throws Exception {
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		Source sourceContent = soapResponse.getSOAPPart().getContent();

		StringWriter writer = new StringWriter();
		transformer.transform(sourceContent, new StreamResult(writer));
		return writer.toString();
	}
}
