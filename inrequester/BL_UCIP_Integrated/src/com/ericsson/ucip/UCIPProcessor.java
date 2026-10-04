package com.ericsson.ucip;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Hashtable;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ericsson.bean.UCIPAccountBalanceBean;
import com.ericsson.bean.UCIPAccountDetailBean;
import com.ericsson.bean.UCIPUpdAccountBalanceBean;
import com.ericsson.ucip.UCIPConnector;
import com.ericsson.ucip.UCIPProcessor;
//import com.ericsson.ucip.UCIPSnippet;
import com.ericsson.util.ParsingException;
import com.ericsson.util.XMLParser;
import com.ssdtech.Global;

public class UCIPProcessor {

	// private static UCIPProcessor instance = new UCIPProcessor();
	// private String GET_ACCOUNT_DETAILS = "GetAccountDetails";
	private String GET_ACCOUNT_DETAILS = null;
	// private String GET_BALANCE_DATE = "GetBalanceAndDate";
	private String GET_BALANCE_DATE = null;
	private String UPDATE_BALANCE_DATE = null;
	private UCIPConnector ucipCon = null;
	private String glCode = null;
	// private Vector direct_debit_parameters = new Vector();
	// private Vector direct_refund_parameters = new Vector();
	// private Logger logger = Logger.getLogger(UCIPProcessor.class);
	XMLBuilder xmlB = new XMLBuilder();

	public UCIPProcessor(UCIPConnector ucip, Properties prop, Logger logger) throws IOException {
		ucipCon = ucip;
		GET_ACCOUNT_DETAILS = prop.getProperty("AccountDetailMethod");
		GET_BALANCE_DATE = prop.getProperty("BalanceDateMethod");
		UPDATE_BALANCE_DATE = prop.getProperty("UpdateBalanceMethod");
		glCode = prop.getProperty("GLCode");
		logger.log(Level.INFO, "[UCIPProcessor] UCIPProcessor() Object created.\n");
	}

	public String getAccountDetails(String origin_node, String origin_host, String txId, String time_stamp,
			String sub_msisdn_NAI, String sub_msisdn, String msg_flag, String info_flag, Logger logger) {
		// logger.info("[UCIPProcessor] getAccountDetails()");
		logger.log(Level.INFO, "[UCIPProcessor] getAccountDetails()\n");
		String responseStr = null;
		// set parameters
		UCIPAccountDetailBean param = new UCIPAccountDetailBean();
		param.setOriginNodeType(origin_node);
		param.setOriginHostName(origin_host);
		param.setOriginTrxId(txId);
		param.setOriginTimeStamp(time_stamp);
		param.setSubsNumberNAI(sub_msisdn_NAI);
		param.setSubscriberNumber(sub_msisdn);
		param.setMessageFlag(msg_flag);
		param.setInfoFlag(info_flag);

		String xmlRequest = xmlB.generateGetAccountDetRequest(GET_ACCOUNT_DETAILS, param, logger);

		try {
			// responseStr = ucipCon.sendRequest(xmlRequest,logger);
			/*
			 * File file = new File("responce1.txt"); FileInputStream fis = new
			 * FileInputStream(file); byte[] data = new byte[(int) file.length()];
			 * fis.read(data); fis.close(); String str = new String(data, "UTF-8");
			 * responseStr=str;
			 */
		} catch (Exception ex) {
			ex.printStackTrace();
			// logger.error("[UCIPProcessor] getAccountDetails() Exception thrown. ", ex);
			logger.log(Level.SEVERE, "[UCIPProcessor] getAccountDetails() Exception thrown. \n" + ex);
		}
		return responseStr;
	}

	public String doGetBalanceAndDate(String origin_node, String origin_host, String txId, String time_stamp,
			String sub_msisdn_NAI, String sub_msisdn, String msg_flag, String info_flag, Logger logger) {
		// logger.info("[UCIPProcessor] doGetBalanceAndDate()");
		logger.log(Level.INFO, "[UCIPProcessor] doGetBalanceAndDate()\n");
		String responseStr = null;
		// set parameters
		UCIPAccountBalanceBean param = new UCIPAccountBalanceBean();
		param.setOriginNodeType(origin_node);
		param.setOriginHostName(origin_host);
		param.setOriginTrxId(txId);
		param.setOriginTimeStamp(time_stamp);
		param.setSubsNumberNAI(sub_msisdn_NAI);
		param.setSubscriberNumber(sub_msisdn);
		param.setMessageFlag(msg_flag);
		param.setDedicatedAccountSelection("");
		param.setChargingRequestInfo("");
		param.setRequestSubordinateAccountDetails("");

		// String request =
		// UCIPConnector.getInstance().generateGetAccountBalanceRequest(GET_BALANCE_DATE,
		// param,logger);
		String xmlRequest = xmlB.generateGetAccountBalanceRequest(GET_BALANCE_DATE, param, logger);
		try {
			responseStr = ucipCon.sendRequest(xmlRequest, logger);
		} catch (Exception ex) {
			ex.printStackTrace();
			try {
				Global.write(Global.dateFormatHourMinSec.format(new Date()) + Global.getStackTrace(ex),
						"Exception in Sending ucip message");
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}
		return responseStr;
	}

	public String doUpdateBalanceAndDate(String origin_node, String origin_host, String trxId, String time_stamp,
			String msisdn, String amount, Logger logger) {
		// logger.info("[UCIPProcessor] doUpdateBalanceAndDate()");
		logger.log(Level.INFO, "[UCIPProcessor] doUpdateBalanceAndDate()\n");
		UCIPUpdAccountBalanceBean bean = new UCIPUpdAccountBalanceBean();
		// set the value
		bean.setOriginNodeTypeValue(origin_node);
		bean.setOriginHostNameValue(origin_host);
		bean.setOriginTransactionIDValue(trxId);
		bean.setOriginTimeStampValue(time_stamp);
		bean.setSubscriberNumberNAIValue("");
		bean.setMessageCapabilityFlagValue("");
		bean.setSubscriberNumberValue(msisdn);
		bean.setOriginOperatorIDValue("");
		bean.setTransactionCurrencyValue("MYR");
		bean.setAdjustmentAmountRelativeValue(amount);
		bean.setDedicatedAccUpdInfoValue("");
		bean.setSupevisionExpiryDateRelativeValue("1");
		bean.setSupevisionExpiryDateValue("");
		bean.setSrvFeeExpiryDateRelativeValue("");
		bean.setServiceFeeExpiryDateValue("");
		bean.setCreditClearancePeriodValue("");
		bean.setServiceRemovalPeriodValue("");
		bean.setTransactionTypeValue("");
		bean.setTransactionCodeValue("");
		bean.setExternalData2Value(glCode);

		// String xml_request =
		// UCIPConnector.getInstance().generateUpdateRequest(UCIPSnippet.UPDATE_BALANCE_AND_DATE,
		// bean,logger);
		String status = null;
		String xmlRequest = xmlB.generateUpdateRequest(UPDATE_BALANCE_DATE, bean, logger);
		try {
			status = ucipCon.sendRequest(xmlRequest, logger);
		} catch (Exception ex) {
			// logger.error("[UCIPProcessor] doUpdateBalanceAndDate() Exception thrown.",
			// ex);
			logger.log(Level.SEVERE, "[UCIPProcessor] doUpdateBalanceAndDate() Exception thrown. \n" + ex);
		}
		return status;
	}

	/**
	 * Process GetAccountDetail response
	 * 
	 * @param responseStr
	 * @return
	 */
	public Object processGetAccountDetailResponse(String responseStr, Logger logger) {
		// logger.info("[UCIPProcessor] processGetAccountDetailResponse()");
		logger.log(Level.INFO, "[UCIPProcessor] processGetAccountDetailResponse()\n");
		XMLParser parser = new XMLParser();
		try {
			@SuppressWarnings("unchecked")
			Hashtable<String, String> params = (Hashtable<String, String>) parser.parseAll(responseStr, "member");
			if (!params.isEmpty()) {
				UCIPAccountDetailBean bean = new UCIPAccountDetailBean();
				bean.setResponseCode(params.get("responseCode") + "");
				if (params.containsKey("originTransactionID"))
					bean.setOriginTrxId(params.get("originTransactionID") + "");
				if (params.containsKey("languageIDCurrent"))
					bean.setLanguageId(params.get("languageIDCurrent") + "");
				if (params.containsKey("serviceClassCurrent"))
					bean.setServiceClass(params.get("serviceClassCurrent") + "");
				if (params.containsKey("masterAccountNumber"))
					bean.setMasterAccountId(params.get("masterAccountNumber") + "");
				if (params.containsKey("temporaryBlockedFlag")) {
					if (params.get("temporaryBlockedFlag").toString().equals("1"))
						bean.setIsBlocked(true);
					else
						bean.setIsBlocked(false);
				}

				if (params.containsKey("serviceOfferings")) {
					if (params.containsKey("serviceOfferingID")
							&& params.get("serviceOfferingActiveFlag").toString().equals("1")) {
						bean.setServiceId(params.get("serviceOfferingID") + "");
						bean.setServiceActiveFlag(true);
					}
				}
				if (params.containsKey("accountActivatedFlag"))
					bean.setOriginTrxId(params.get("") + "");
				if (params.containsKey(""))
					bean.setOriginTrxId(params.get("") + "");
				if (params.containsKey(""))
					bean.setOriginTrxId(params.get("") + "");
				if (params.containsKey(""))
					bean.setOriginTrxId(params.get("") + "");
				if (params.containsKey(""))
					bean.setOriginTrxId(params.get("") + "");
				if (params.containsKey(""))
					bean.setOriginTrxId(params.get("") + "");
				if (params.containsKey(""))
					bean.setOriginTrxId(params.get("") + "");
				if (params.containsKey(""))
					bean.setOriginTrxId(params.get("") + "");
			}
		} catch (ParsingException pex) {
			// logger.error("[UCIPProcessor] processGetAccountDetailResponse() -
			// ParsingException thrown. ", pex);
			logger.log(Level.SEVERE,
					"[UCIPProcessor] processGetAccountDetailResponse() - ParsingException thrown. \n" + pex);
		} catch (Exception ex) {
			// logger.error("[UCIPProcessor] processGetAccountDetailResponse() - Exception
			// thrown. ", ex);
			logger.log(Level.SEVERE, "[UCIPProcessor] processGetAccountDetailResponse() - Exception thrown. \n" + ex);
		}
		return null;
	}

	/**
	 * Process GetBalanceAndDate response
	 * 
	 * @param responseStr
	 * @return
	 */
	public Object processGetBalanceAndDateResponse(String responseStr, Logger logger) {
		// logger.info("[UCIPProcessor] processGetAccountDetailResponse()");
		logger.log(Level.INFO, "[UCIPProcessor] processGetAccountDetailResponse()\n");
		XMLParser parser = new XMLParser();
		try {
			@SuppressWarnings("unchecked")
			Hashtable<String, String> params = (Hashtable<String, String>) parser.parseAll(responseStr, "member");
			if (!params.isEmpty()) {
				UCIPAccountBalanceBean bean = new UCIPAccountBalanceBean();
				bean.setResponseCode(params.get("responseCode") + "");
				if (params.containsKey(""))
					bean.setOriginTrxId(params.get("") + "");
				if (params.containsKey(""))
					bean.setOriginTrxId(params.get("") + "");
				if (params.containsKey(""))
					bean.setOriginTrxId(params.get("") + "");
				if (params.containsKey(""))
					bean.setOriginTrxId(params.get("") + "");
				if (params.containsKey(""))
					bean.setOriginTrxId(params.get("") + "");
				if (params.containsKey(""))
					bean.setOriginTrxId(params.get("") + "");
				if (params.containsKey(""))
					bean.setOriginTrxId(params.get("") + "");
			}
		} catch (ParsingException pex) {
			// logger.error("[UCIPProcessor] processGetBalanceAndDateResponse() -
			// ParsingException thrown. ", pex);
			logger.log(Level.SEVERE,
					"[UCIPProcessor] processGetBalanceAndDateResponse() - ParsingException thrown. \n" + pex);
		} catch (Exception ex) {
			// logger.error("[UCIPProcessor] processGetBalanceAndDateResponse() - Exception
			// thrown. ", ex);
			logger.log(Level.SEVERE, "[UCIPProcessor] processGetBalanceAndDateResponse() - Exception thrown. \n" + ex);
		}
		return null;
	}

	/**
	 * Process UpdateBalanceAndDate response
	 * 
	 * @param responseStr
	 * @return
	 */
	public Object processUpdateBalanceAndDateResponse(String responseStr, Logger logger) {
		// logger.info("[UCIPProcessor] processUpdateBalanceAndDateResponse()");
		logger.log(Level.INFO, "[UCIPProcessor] processUpdateBalanceAndDateResponse()\n");
		XMLParser parser = new XMLParser();
		try {

			@SuppressWarnings("unchecked")
			Hashtable<String, String> params = (Hashtable<String, String>) parser.parseAll(responseStr, "member");
			if (!params.isEmpty()) {
				UCIPUpdAccountBalanceBean bean = new UCIPUpdAccountBalanceBean();
				/**
				 * temporary disable
				 * 
				 */
			}
		} catch (ParsingException pex) {
			// logger.error("[UCIPProcessor] processUpdateBalanceAndDateResponse() -
			// ParsingException thrown. ", pex);
			logger.log(Level.SEVERE,
					"[UCIPProcessor] processUpdateBalanceAndDateResponse() - ParsingException thrown. \n" + pex);
		} catch (Exception ex) {
			// logger.error("[UCIPProcessor] processUpdateBalanceAndDateResponse() -
			// Exception thrown. ", ex);
			logger.log(Level.SEVERE,
					"[UCIPProcessor] processUpdateBalanceAndDateResponse() - Exception thrown. \n" + ex);
		}
		return null;
	}
}