package com.ssdtech;

import java.io.StringWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.HashSet;

import javax.xml.soap.Detail;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFault;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.runner.RunWith;

import com.ssdtech.robi.Global;
import com.ssdtech.robi.HandleRequest;
import com.ssdtech.robi.ResultCode;
import com.ssdtech.robi.exceptions.RequestParamsException;
import com.ssdtech.robi.model.RequestParams;
import com.ssdtech.robi.model.ServiceInfo;
import com.ssdtech.robi.requestgenerator.PaymentSOAPMessageGenerator;
import com.ssdtech.robi.requestgenerator.SOAPMessageGenerator;
import com.ssdtech.robi.responseparser.PaymentSOAPResponseParser;
import com.ssdtech.robi.responseparser.SOAPResponseParser;

@RunWith(JUnitParamsRunner.class)
public class HandleRequestTest {
	private static final String SOAPENV_URL = "http://schemas.xmlsoap.org/soap/envelope/";
	private static final String V2_URL = "http://www.huawei.com.cn/schema/common/v2_1";
	private static final String LOC_URL = "http://www.csapi.org/schema/parlayx/payment/amount_charging/v2_1/local";
	private static final String NS1_URL_SUBSCRIBE = "http://www.csapi.org/schema/parlayx/subscribe/manage/v1_0/local";
	private static final String REQUEST_URL = "http://IP:Port/AmountChargingService/services/AmountCharging";
	private static final String NS1_URL = "http://www.csapi.org/schema/parlayx/payment/amount_charging/v2_1";
	
    private static final String CHARGING_REQUEST_FORMAT = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>" +
    		                                              "<soapenv:Envelope xmlns:soapenv=\"" + SOAPENV_URL + "\" xmlns:loc=\"" +LOC_URL + "\" xmlns:v2=\"" + V2_URL + "\">" + 
														  "<soapenv:Header>" +
														      "<v2:RequestSOAPHeader>" +
														         "<v2:spId>%s</v2:spId>" + 
														         "<v2:spPassword>%s</v2:spPassword>" + 
														         "<v2:serviceId>%s</v2:serviceId>" +
														         "<v2:timeStamp>%s</v2:timeStamp>" +
														         "<v2:OA>%s</v2:OA>" + 
														         "<v2:FA>%s</v2:FA>" + 
														         "<v2:token/>" + 
														      "</v2:RequestSOAPHeader>" + 
														   "</soapenv:Header>" + 
														   "<soapenv:Body>" + 
														      "<loc:chargeAmount>" + 
														         "<loc:endUserIdentifier>%s</loc:endUserIdentifier>" + 
														         "<loc:charge>" + 
														            "<description>%s</description>" + 
														            "<currency>%s</currency>" + 
														            "<amount>%d</amount>" + 
														            "<code>%s</code>" + 
														         "</loc:charge>" + 
														         "<loc:referenceCode>%s</loc:referenceCode>" + 
														      "</loc:chargeAmount>" + 
														   "</soapenv:Body>" + 
														   "</soapenv:Envelope>";
    
    private static final String CHARGING_RESPONSE_FORMAT = "<soapenv:Envelope xmlns:soapenv=\"" + SOAPENV_URL + "\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">" + 
															   "<soapenv:Body>" +  
															      "<ns1:chargeAmountResponse xmlns:ns1=\"" + LOC_URL + "\"/>" +  
															   "</soapenv:Body>" +  
														   "</soapenv:Envelope>";
    private static final String SOAP_ERROR_FORMAT = "<soapenv:Envelope xmlns:soapenv=\"" + SOAPENV_URL + "\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">" + 
													  "<soapenv:Body>" + 
													    "<soapenv:Fault>" + 
													      "<faultcode>%s</faultcode>" + 
													      "<faultstring>%s</faultstring>" + 
													      "<detail>" + 
													        "<ns1:ServiceException xmlns:ns1=\"http://www.csapi.org/schema/parlayx/common/v2_1\">" + 
													          "<messageId>%s</messageId>" + 
													          "<text>%s</text>" + 
													          "<variables>Variables</variables>" + 
													        "</ns1:ServiceException>" + 
													      "</detail>" + 
													    "</soapenv:Fault>" + 
													  "</soapenv:Body>" +
													"</soapenv:Envelope>";
    
    ServiceInfo newSi = new ServiceInfo("1234", "22202", "Robi1234", "");
    
	@Before
	public void setUp() {
		Global.rateidServiceInfoMapping = new HashMap<String, ServiceInfo>();
		Global.rateidServiceInfoMapping.put("rate_id", newSi);
		
		Global.PAYMENT_SPECIFIC_DATA = new HashMap<String, String>();
		Global.PAYMENT_SPECIFIC_DATA.put("url", REQUEST_URL);
		Global.PAYMENT_SPECIFIC_DATA.put("description", "charging information");
		Global.PAYMENT_SPECIFIC_DATA.put("currency", "BDT");
		
		Global.MSISDN_LENTH = 10;
		Global.PREFIX = "880";
	}
    
    /* @Test
    public void checkIfCorrectSOAPRequestGenerated() throws SecurityException, NoSuchFieldException, Exception {
    	Global.V2_URL = V2_URL;
    	Global.LOC_URL = LOC_URL;

    	HandleRequest hr = new HandleRequest();
    	setFinalStatic(hr.getClass().getDeclaredField("dateFormat"), new SimpleDateFormat("20170720114000"), hr);
    	setFinalStatic(hr.getClass().getDeclaredField("cdrid"), "1234567891234567", hr);
    	setFinalStatic(hr.getClass().getDeclaredField("MSISDN"), "8801823208599", hr);
    	setFinalStatic(hr.getClass().getDeclaredField("amount"), 100, hr);
    	setFinalStatic(hr.getClass().getDeclaredField("spId"), "22202", hr);
    	setFinalStatic(hr.getClass().getDeclaredField("password"), "Robi1234", hr);
    	setFinalStatic(hr.getClass().getDeclaredField("serviceId"), "1234", hr);

    	String expectedSOAPRequest = String.format(CHARGING_REQUEST_FORMAT,
    			                                      "22202",
    			                                      "665eeba3d8c42fd970e185e02873b275",
    			                                      "1234",
    			                                      "20170720114000",
    			                                      "8801823208599", "8801823208599", "8801823208599",
    			                                      Global.PAYMENT_SPECIFIC_DATA.get("description"),
    			                                      Global.PAYMENT_SPECIFIC_DATA.get("currency"),
    			                                      100, "22202", "1234567891234567");
    	Method createSOAPRequest = hr.getClass().getDeclaredMethod("createSOAPRequest");
    	createSOAPRequest.setAccessible(true);
    	Method printSOAPResponse = hr.getClass().getDeclaredMethod("printSOAPResponse", SOAPMessage.class);
    	printSOAPResponse.setAccessible(true);

    	String actualSOAPRequest = getSOAPResponseInString((SOAPMessage) createSOAPRequest.invoke(hr));

        assertEquals("Charing SOAP request message is not correctly generated.", expectedSOAPRequest, actualSOAPRequest);
    } */
    
    @Test
    public void checkIfCorrectSOAPMessageGeneratedUsingPaymentSOAPMessageGeneratorAPI() throws Exception {
    	RequestParams rp = new RequestParams.RequestParamsBuilder("1234567891234567", "Kids_Zone_IVR", "rate_id", 100L)
    			                                 .setChargingMSISDN("8801823208599", "1111", 0)
    			                                 .createRequestParamsInstance();
    	SOAPMessageGenerator sg = new PaymentSOAPMessageGenerator(Global.PAYMENT_SPECIFIC_DATA.get("url"));
    	setFinalStatic(sg.getClass().getDeclaredField("dateFormat"), new SimpleDateFormat("20170720114000"), sg);
    	
    	SOAPMessage request = sg.generate(rp);
    	
    	String expectedSOAPRequest = String.format(CHARGING_REQUEST_FORMAT, 
									                "22202", 
									                "665eeba3d8c42fd970e185e02873b275",
									                "1234",
									                "20170720114000",
									                "8801823208599", "8801823208599", "8801823208599",
									                Global.PAYMENT_SPECIFIC_DATA.get("description"), 
  			                                        Global.PAYMENT_SPECIFIC_DATA.get("currency"),
									                100, "22202", "12345678912345670");
    	String actualSOAPRequest = getSOAPResponseInString(request);
    	assertEquals("Expected SOAP request does not generated.", expectedSOAPRequest, actualSOAPRequest);
    }
    
    /* @Test
    public void checkIfSOAPResponseForPaymentReturnSuccessCodeOnSuccess() {
    	HandleRequest hr = new HandleRequest();
    	String resultCode = hr.xmlParse("chargeAmountResponse", CHARGING_RESPONSE_FORMAT, "Success");
    	assertEquals("Success is not returned.", ResultCode.SUCCESS_CODE, resultCode);
    }
    
    @Test
    public void checkIfSOAPResponseForPaymentReturn9000WithConditionSuccess() {
    	String errorCode = "dummy";
    	String errorDesp = "dummy";
    	HandleRequest hr = new HandleRequest();
    	String soapResponse = String.format(SOAP_ERROR_FORMAT, errorCode, errorDesp, errorCode, errorDesp);
    	String resultCode = hr.xmlParse("chargeAmountResponse", soapResponse, "Success");
    	assertEquals("9000 is not returned", "9000", resultCode);
    } */
    
    public static Object[] getSOAPErrorCodes() {
    	return new Object[] {
    			new Object[] {"SVC0001", "Description 0001"},
    			new Object[] {"SVC0002", "Description 0002"},
    			new Object[] {"SVC0270", "Description 0270"},
    			new Object[] {"SVC0901", "Description 0901"}
    	};
    }
    
    public static Object[] getSOAPErrorCodesUsingNewPaymentAPI() {
    	return new Object[] {
    			new Object[] {"SVC0001", "Description 0001", "SVC0001"},
    			new Object[] {"SVC0002", "Description 0002", "SVC0002"},
    			new Object[] {"SVC0270", "Description 0270", "SVC0270"},
    			new Object[] {"SVC0901", "Description 0901", "SVC0901"}
    	};
    }
    
    /* @Test
    @Parameters(method = "getSOAPErrorCodes")
    public void checkIfSOAPResponseForPaymentReturnCorrectErrorCodeOnFailure(String errorCode, String errorDesp) {
    	Global.LOGENABLE = -1;
    	HandleRequest hr = new HandleRequest();
    	String soapResponse = String.format(SOAP_ERROR_FORMAT, errorCode, errorDesp, errorCode, errorDesp);
    	String actualErrorCode = hr.xmlParse("faultcode|faultstring", soapResponse, "Error");
    	assertEquals("Error code does not match.", errorCode, actualErrorCode);
    }*/
    
    @Test
    public void checkIfSOAPResponseForPaymentReturnSuccessCodeWithSOAPResponseParserPaymentAPI() throws SOAPException, Exception {
    	SOAPResponseParser respParser = new PaymentSOAPResponseParser();
    	String resultCode = respParser.process(this.generateSuccessSOAPMessageForPayment("chargeAmountResponse"), null);
    	assertEquals("Success is not returned.", ResultCode.SUCCESS_CODE, resultCode);
    }
    
    @Test
    @Parameters(method = "getSOAPErrorCodesUsingNewPaymentAPI")
    public void checkIfSOAPResponseForPaymentReturnCorrectErrorCodeOnFailureWithSOAPResponseParserAPI(String errorCode, String errorDesp, String expectedErrorCode) throws Exception {
    	SOAPMessage response = this.generateSOAPMessageWithFault(errorCode, errorDesp);
    	String actualErrorCode = new PaymentSOAPResponseParser().process(response, null);
    	assertEquals("Error code does not match.", expectedErrorCode, actualErrorCode);
    }
    
    @Test
    public void checkIfSOAPParsingErrorCodeReturnsInCaseOfWrongNodeValueUsingPaymentParserAPI() throws SOAPException {
    	String wrongPaymentNodeName = "wrongChargeAmountResponse";
    	SOAPResponseParser respParser = new PaymentSOAPResponseParser();
    	String resultCode = respParser.process(this.generateSuccessSOAPMessageForPayment(wrongPaymentNodeName), null);
    	assertEquals("SOAP parsing error code does not return.", ResultCode.SOAP_PARSING_ERROR_CODE, resultCode);
    }
    
    /* @Ignore
    public void checkIfDefaultBalanceInformationReturnForDefaultBalanceQuery() {
    	HandleRequest handleRequest = new HandleRequest();
    	assertEquals("Default balance query does not return.", "|0|" + ResultCode.UNIMPLEMENTED_METHOD_ERROR_CODE, handleRequest.queryBalance(null));
    } */
    
    public static Object[] getSuccessCodesForSubscribe() {
    	return new Object[] {
    			new Object[] {"0", "successful"},
    			new Object[] {"00000000", "successful"},
    			new Object[] {"20002017", "successful"}
    	};
    }
    
    public static Object[] getErrorCodesForSubscribe() {
    	return new Object[] {
    		new Object[] {"10001211", "The field in the request is incorrect.", "10001211"},
    		new Object[] {"22007304", "The user's subscription capability is suspended and the user cannot subscribe to the product.", "22007304"},
    		new Object[] {"20002018", "Custom error code", "9001"}
    	};
    }
    
    public static Object[] getInvalidNodeLocalNames() {
    	return new Object[] {
			new String[] {"subscribeProductResponse1", "subscribeProductRsp", "result", "resultDescription"},
			new String[] {"subscribeProductResponse", "subscribeProductRsp1", "result", "resultDescription"},
			new String[] {"subscribeProductResponse", "subscribeProductRsp", "result1", "resultDescription"}
    	};
    }
    
    private SOAPMessage generateSuccessSOAPMessageForPayment(String nodeName) throws SOAPException {
    	MessageFactory factory = MessageFactory.newInstance();
		SOAPMessage soapMsg = factory.createMessage();
		SOAPPart part = soapMsg.getSOAPPart();
		SOAPEnvelope envelope = part.getEnvelope();
		envelope.getHeader().detachNode();
		SOAPBody body = envelope.getBody();

		envelope.removeNamespaceDeclaration(envelope.getPrefix());
		body.removeNamespaceDeclaration(body.getPrefix());
		
		envelope.setPrefix("soapenv");
		body.setPrefix("soapenv");
		envelope.addNamespaceDeclaration("xsi", "http://www.w3.org/2001/XMLSchema-instance");
		
		body.addChildElement(nodeName, "ns1", LOC_URL);
		
		return soapMsg;
    }
    
    private SOAPMessage generateSOAPMessageForSubscription(String[] nodeLocalNames, String resultCode, String resultDescription) throws SOAPException {
    	MessageFactory factory = MessageFactory.newInstance();
		SOAPMessage soapMsg = factory.createMessage();
		SOAPPart part = soapMsg.getSOAPPart();
		SOAPEnvelope envelope = part.getEnvelope();
		envelope.getHeader().detachNode();
		SOAPBody body = envelope.getBody();

		envelope.removeNamespaceDeclaration(envelope.getPrefix());
		body.removeNamespaceDeclaration(body.getPrefix());
		
		envelope.setPrefix("soapenv");
		body.setPrefix("soapenv");
		envelope.addNamespaceDeclaration("xsi", "http://www.w3.org/2001/XMLSchema-instance");
		
		SOAPElement elem1 = body.addChildElement(nodeLocalNames[0], "ns1", NS1_URL_SUBSCRIBE);
		SOAPElement elem2 = elem1.addChildElement(nodeLocalNames[1], "ns1");
		
		SOAPElement result = elem2.addChildElement(nodeLocalNames[2]);
		result.addTextNode(resultCode);
		SOAPElement resultDesp = elem2.addChildElement(nodeLocalNames[3]);
		resultDesp.addTextNode(resultDescription);
		
		return soapMsg;
    }
    
    private SOAPMessage generateSOAPMessageWithFault(String errorCode, String errorDesp) throws SOAPException {
    	MessageFactory factory = MessageFactory.newInstance();
		SOAPMessage soapMsg = factory.createMessage();
		SOAPPart part = soapMsg.getSOAPPart();
		SOAPEnvelope envelope = part.getEnvelope();
		envelope.getHeader().detachNode();
		SOAPBody body = envelope.getBody();

		envelope.removeNamespaceDeclaration(envelope.getPrefix());
		body.removeNamespaceDeclaration(body.getPrefix());
		
		envelope.setPrefix("soapenv");
		body.setPrefix("soapenv");
		envelope.addNamespaceDeclaration("xsi", "http://www.w3.org/2001/XMLSchema-instance");
		
		SOAPFault fault = body.addFault();
		fault.setPrefix("soapenv");
		fault.setFaultCode(errorCode);
		fault.setFaultString(errorDesp);
		
		Detail detail = fault.addDetail();
		SOAPElement ns1 = detail.addChildElement("Exception", "ns1", NS1_URL);
		
		SOAPElement messageId = ns1.addChildElement("messageId");
		messageId.addTextNode(errorCode);
		SOAPElement text = ns1.addChildElement("text");
		text.addTextNode(errorDesp);
		SOAPElement variables = ns1.addChildElement("variables");
		variables.addTextNode("Variables");
		
		return soapMsg;
    }
    
	private static void setFinalStatic(Field field, Object newValue, Object instance) throws Exception {
        field.setAccessible(true);

        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);

        field.set(instance, newValue);
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
