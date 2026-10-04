package com.ssdtech.robi;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.StringWriter;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Logger;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;

import com.ssdtech.cgwcommon.INRequester;
import com.ssdtech.robi.exceptions.RequestParamsException;
import com.ssdtech.robi.model.RequestParams;
import com.ssdtech.robi.model.ResponseParams;
import com.ssdtech.robi.responseparser.SOAPResponseParser;

public class HandleRequest implements INRequester {
	/* my needed variable */

	String message;
	String result;
	int resultcode;
	String cdrid_serviceid;
	String serviceName = "";
	String spId = "";
	String password = "";
	String serviceId = "";
	String MSISDN = "";
	String requesttype = "";
	String sequencenumber;
	String cdrid;
	String unitsReserved = "";
	String result_code = "";
	int direction = 0;
	int amount = 0;
	String callingPartyNumber;
	String calledParty;
	String bno;
	String requestparams[];
	boolean cgw_params_validity = true;
	SimpleDateFormat dateFormat;
	/* my needed variable */

	private final Logger logger = LogManager.getLogger(HandleRequest.class.getName());
	
	public HandleRequest() {
		dateFormat = new SimpleDateFormat("yyyyMMddhhmmss");
		
		if (Global.LOGENABLE == 1) {
			this.logger.setLevel(Level.INFO);
		} else { 
			this.logger.setLevel(Level.OFF);
		}
	}
	
	@Override
	public String queryBalance(String arg0) {
		return "|0|" + ResultCode.UNIMPLEMENTED_METHOD_ERROR_CODE;
	}

	@Override
	public String executeCommand(String command) {
		Set<String> validCommands = new HashSet<String>();
		validCommands.add("reload");
		
		if (command == null || command.trim().isEmpty() || !validCommands.contains(command.trim().toLowerCase())) {
			return "Invalid command[" + command + "]"; 
		}
		
		if (command.toLowerCase().equals("reload")) {
			try (BufferedReader serviceConfigReader = new BufferedReader(
		                                 new InputStreamReader(
		                                     new FileInputStream(Global.RATECONFIG_FILE_PATH)))) {
				Global.loadServiceConfiguration(serviceConfigReader);
			} catch(Exception exception) {
				return "Error while reloading - " + exception.getMessage();
			}
		}
		return "Configuration Loaded.";
	}

	@Override
	public String chargeSubscriber(String cgwRequest) {			
		logger.info("Request from CGW : " + cgwRequest);
					
		RequestParams rp = this.getRequestParams(cgwRequest);
		String resultCode = null;
		
		if (rp != null) {
		    SOAPMessage request = this.getSOAPMessageForSDP(rp);
		    if (request != null) {
		    	resultCode = this.getSDPResponse(request, rp);
		    } else {
		    	resultCode = ResultCode.SOAP_GENERATION_ERROR_CODE;
		    }
		} else {
			resultCode = ResultCode.INVALID_CGW_REQUEST;
		}

        ResponseParams resp = (rp != null) ?  new ResponseParams(rp.getTIDWithServiceID(), 
        		                                                 resultCode, 
        		                                                 rp.getSequenceNo(),
                                                                 rp.getCommandID(),
                                                                 rp.getReservedUnits())
        		                                            :
        		                              new ResponseParams(resultCode);
		
		String cgwResponse = resp.generateResponseString();
		logger.info("Response to CGW : " + cgwResponse);
		
		return cgwResponse;
	}
	
	private RequestParams getRequestParams(String cgwRequest) {
		try {
			String[] requestParts = cgwRequest.split("\\|");
			String cdrIdWithServiceName = requestParts[0];
			String[] cdrParts = cdrIdWithServiceName.split("\\[|\\]");
			String cdrId = cdrParts[0];
			String serviceName = cdrParts[1];
			int sequenceNumber = Integer.parseInt(requestParts[1]);
			String requestType = requestParts[2];
			String ano = requestParts[3];
			String bno = requestParts[4];
			int direction = Integer.parseInt(requestParts[5]);
			String rateId = requestParts[6];
			long amount = Long.parseLong(requestParts[7]);
			String unitsReserved = requestParts[9];
			
			return new RequestParams.RequestParamsBuilder(cdrId, serviceName, rateId, amount)
					                                 .setChargingMSISDN(ano, bno, direction)
					                                 .setCommandID(requestType)
					                                 .setSequenceNo(sequenceNumber)
					                                 .setTIDWithServiceID(cdrIdWithServiceName)
					                                 .setReservedUnits(unitsReserved)
					                             .createRequestParamsInstance();
		} catch (RequestParamsException exception1) {
			logger.error(exception1);
		} catch (Exception exception2) {
			logger.error(exception2);
		}
		
		return null;
	}
	
	private SOAPMessage getSOAPMessageForSDP(RequestParams rp) {
		try {
			return Global.REQUEST_GENERATOR_TYPE.get(rp.getOperationType()).generate(rp);
		} catch (SOAPException exception) {
			logger.error(exception);
		} catch (Exception exception1) {
			logger.error(exception1);
		}
		
		return null;
	}
	
	private String getSDPResponse(SOAPMessage request, RequestParams rp) {
		String resultCode = ResultCode.SOAP_PARSING_ERROR_CODE;
		
		try {
			SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance();
			SOAPConnection soapConnection = soapConnectionFactory.createConnection();
			
			try {
				logger.info("SOAP request to ROBI SDP : " + this.getTextualRepresentation(request));
			} catch (Exception exception) {}
			
			SOAPMessage soapResponse = soapConnection.call(request, Global.REQUEST_GENERATOR_TYPE.get(rp.getOperationType()).getRequestURL());
			
			try {
				logger.info("SOAP response from ROBI SDP : " + this.getTextualRepresentation(soapResponse));
			} catch (Exception exception) {}
			
			SOAPResponseParser responseParser = Global.RESPONSE_PARSER_TYPE.get(rp.getOperationType());
			resultCode = responseParser.process(soapResponse, rp);
			soapConnection.close();
		} catch (SOAPException exception) {
			logger.error(exception);
		} catch (Exception exception) {
			logger.error(exception);
		}
		
		return resultCode;
	}
	
	private String getTextualRepresentation(SOAPMessage soapMessage) throws SOAPException, TransformerException {
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		Source sourceContent = soapMessage.getSOAPPart().getContent();

		StringWriter writer = new StringWriter();
		transformer.transform(sourceContent, new StreamResult(writer));
		return writer.toString();
	}

	private SOAPMessage createSOAPRequest() throws Exception {
		try {
			String Date = "", Sp_Password = "";
			MessageFactory factory = MessageFactory.newInstance();
			SOAPMessage soapMsg = factory.createMessage();
			SOAPPart part = soapMsg.getSOAPPart();
			SOAPEnvelope envelope = part.getEnvelope();
			javax.xml.soap.SOAPHeader header = envelope.getHeader();
			SOAPBody body = envelope.getBody();

			envelope.removeNamespaceDeclaration(envelope.getPrefix());
			header.removeNamespaceDeclaration(header.getPrefix());
			body.removeNamespaceDeclaration(body.getPrefix());
			envelope.setPrefix("soapenv");
			header.setPrefix("soapenv");
			body.setPrefix("soapenv");

			// envelope create
			envelope.addNamespaceDeclaration("v2", Global.V2_URL);
			envelope.addNamespaceDeclaration("loc", Global.LOC_URL);

			// header create
			SOAPElement header_elem_1 = header.addChildElement("RequestSOAPHeader", "v2");
			SOAPElement header_elem_2 = header_elem_1.addChildElement("spId", "v2");
			header_elem_2.addTextNode(spId);
			SOAPElement header_elem_3 = header_elem_1.addChildElement("spPassword", "v2");
			Date = dateFormat.format(new Date());
			Sp_Password = spId + password + Date;
			header_elem_3.addTextNode(Global.getMD5(Sp_Password));
			SOAPElement header_elem_4 = header_elem_1.addChildElement("serviceId", "v2");
			header_elem_4.addTextNode(serviceId);
			SOAPElement header_elem_5 = header_elem_1.addChildElement("timeStamp", "v2");
			header_elem_5.addTextNode(Date);
			SOAPElement header_elem_6 = header_elem_1.addChildElement("OA", "v2");
			header_elem_6.addTextNode(MSISDN);
			SOAPElement header_elem_7 = header_elem_1.addChildElement("FA", "v2");
			header_elem_7.addTextNode(MSISDN);
			header_elem_1.addChildElement("token", "v2");

			// Body create
			SOAPElement body_elem_1 = body.addChildElement("chargeAmount", "loc");
			SOAPElement body_elem_2 = body_elem_1.addChildElement("endUserIdentifier", "loc");
			body_elem_2.addTextNode(MSISDN);
			SOAPElement body_elem_3 = body_elem_1.addChildElement("charge", "loc");
			SOAPElement body_elem_4 = body_elem_3.addChildElement("description");
			body_elem_4.addTextNode(Global.PAYMENT_SPECIFIC_DATA.get("description"));
			SOAPElement body_elem_5 = body_elem_3.addChildElement("currency");
			body_elem_5.addTextNode(Global.PAYMENT_SPECIFIC_DATA.get("currency"));
			SOAPElement body_elem_6 = body_elem_3.addChildElement("amount");
			body_elem_6.addTextNode(Integer.toString(amount));
			SOAPElement body_elem_7 = body_elem_3.addChildElement("code");
			body_elem_7.addTextNode(spId);
			SOAPElement body_elem_8 = body_elem_1.addChildElement("referenceCode", "loc");
			body_elem_8.addTextNode(cdrid);

			soapMsg.writeTo(System.out);
			FileOutputStream fOut = new FileOutputStream("SoapMessage.xml");

			soapMsg.writeTo(fOut);
			System.out.println();

			System.out.println("SOAP msgg created");

			return soapMsg;

		} catch (Exception e) {
			e.printStackTrace();
			return null;

		}
	}

	private String printSOAPResponse(SOAPMessage soapResponse) throws Exception {
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		Source sourceContent = soapResponse.getSOAPPart().getContent();
		System.out.print("\nResponse SOAP Message = ");

		StreamResult result = new StreamResult(System.out);
		transformer.transform(sourceContent, result);
		StringWriter writer = new StringWriter();
		transformer.transform(sourceContent, new StreamResult(writer));
		String xmlData = writer.toString();
		String resCode = xmlParse("chargeAmountResponse", xmlData, "Success");
		if (resCode == "2001")
			return resCode;
		return resCode = xmlParse("faultcode|faultstring", xmlData, "Error");
	}

	public String xmlParse(String tag, String xmlData, String condition) {
		try {
			int pos1, pos2;
			String faultcode = "", faultstring = "";
			if (condition == "Success") {
				if (xmlData.contains(tag))
					return "2001";
				return "9000";
			} else {
				String parsetag[] = tag.split("\\|");
				pos1 = xmlData.indexOf("<" + parsetag[0] + ">") + parsetag[0].length() + 2;
				pos2 = xmlData.indexOf("</" + parsetag[0] + ">");
				faultcode = xmlData.substring(pos1, pos2);
				pos1 = xmlData.indexOf("<" + parsetag[1] + ">") + parsetag[1].length() + 2;
				pos2 = xmlData.indexOf("</" + parsetag[1] + ">");
				faultstring = xmlData.substring(pos1, pos2);
				Global.write(Global.dateFormatHourMinSec.format(new Date()) + " Error in Responce->Faultcode: "
						+ faultcode + " Faultstring: " + faultstring, "xmlParse");
				return faultcode;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			return "Exception";
		}
	}
}
