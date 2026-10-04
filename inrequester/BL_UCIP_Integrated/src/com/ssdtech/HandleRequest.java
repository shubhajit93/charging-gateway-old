package com.ssdtech;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

import com.ssdtech.cgwcommon.INRequester;

//import com.ericsson.pps.diameter.api.base.AvpDataException;

public class HandleRequest implements INRequester {
	String result;
	int resultcode;
	String MSISDN = "";
	int AccountingRecordType = 0;
	String SessionId = "";

	int direction = 0;
	String rateid;
	int amount = 0;
	int requestCount = 0;
	int AccountingRecordNumber = 0;
	int unitsReserved = 0;
	int UnitsUsed = 0;
	int serviceReservation = 0;
	int serviceUsed = 0;
	String ExtensionText = "";
	int EventNumber;
	String ServiceProviderId = "30";
	String TrafficCase = "21";
	String CallingPartyNumber = "2000";
	String CalledParty = "";
	String extno = "NA";
	String fileName = "";
	int ChargingRule = 0;
	int prametercount = 0;
	StringTokenizer st1;
	public String rs = null;
	Charging charging;
	RateConfigurations rateTemp;

	@Override
	public String executeCommand(String command) {
		Set<String> validCommands = new HashSet<String>();
		validCommands.add("reload");
		
		if (command == null || command.trim().isEmpty() || !validCommands.contains(command.trim().toLowerCase())) {
			return "Invalid command[" + command + "]"; 
		}
		
		if ("reload".equalsIgnoreCase(command.trim())) {
			int lineNumberOfRateConfig = Global.configure();
			int lineNumberOfNumberSeries = Global.numberseriesconfig();
			
			if (lineNumberOfRateConfig != 0) {
				return "Problem in rateconfig file. See log file. Line number " + lineNumberOfRateConfig;
			} else if (lineNumberOfNumberSeries != 0) {
				return "Problem in numberseries file. See log file. Line number " + lineNumberOfNumberSeries;
			}
		}
		
		return "Configuration Reloaded.";
	}
	
	@Override
	public String queryBalance(String balanceQuery) {
		return this.chargeSubscriber(balanceQuery);
	}

	@Override
	public String chargeSubscriber(String message) {
		try {

			if (message != null && !message.equals("") && !message.equals(" ")) {

				Global.write(Global.dateFormatHourMinSec.format(new Date()) + " Request Message " + message,
						"run method of handlerequest");

				String SP_ID = "";
				Integer number = new Integer(0);
				EventCharging instance;
				long resultcode = 0;
				requestCount++;
				MSISDN = "";
				st1 = new StringTokenizer(message, "|");
				prametercount = st1.countTokens();
				String temp_params[] = message.split("\\|");
				if (message.equals("hi")) {
					result = "hello";
				} else if (prametercount == 2 && temp_params[0].toUpperCase().equals("PACKAGE")) {

					String Responce = "";
					MSISDN = temp_params[1];

					Global.logForNumber(MSISDN,
							Global.dateFormatHourMinSec.format(new Date()) + " Request Message " + message,
							"run method of handlerequest");
					if (MSISDN.length() < 10) {
						Global.write(Global.dateFormatHourMinSec.format(new Date())
								+ "MSISDN lenth is less than 10. MSISDN:" + MSISDN,
								"From AIRGetBalance Class method:-RequestToGetBalance");
						Responce = "invalid|-1";
					} else {
						// Accept all 10-digit local MSISDNs (019 and 014 Banglalink series).
						// Route via numberseries digits 3-5; missing series falls back to node 0.
						MSISDN = MSISDN.substring(MSISDN.length() - 10);
						String prefix = MSISDN;
						MSISDN = "880" + MSISDN;
						prefix = prefix.substring(2, 5);
						Integer number2 = new Integer(0);
						boolean hasvalue = Global.Numberseries_MAP.containsKey(prefix);
						number2 = (hasvalue == true) ? Global.Numberseries_MAP.get(prefix) : 0;

						String temp[] = Global.myURI.split("\\|");
						int NumberOfUrl = Integer.parseInt(temp[0]);
						if (number2 >= NumberOfUrl)
							number2 = 0;

						AIRGetBalance airGetBalance = new AIRGetBalance(Global.UcipLog, number2, MSISDN);
						Responce = airGetBalance.RequestToGetBalance(MSISDN);
						if (Responce == null || Responce.isEmpty() || Responce.equals(""))
							Responce = "invalid|-1";
					}
					Responce = Responce + "|" + Global.InsuffientBalanceError;
					try {
						Global.write(Global.dateFormatHourMinSec.format(new Date()) + "Responce from UCIP: " + Responce,
								"From AIRGetBalance Class method:-RequestToGetBalance");
						Global.logForNumber(MSISDN,
								Global.dateFormatHourMinSec.format(new Date()) + " Responce from UCIP: " + Responce,
								"From AIRGetBalance Class method:-RequestToGetBalance");
					} catch (Exception ex1) {
						ex1.printStackTrace();
					}

					finally {
						result = Responce;
					}
				} else if (prametercount > 1) {

					String cdrid = "", prefix = "";

					try {

						SessionId = st1.nextToken(); // NOW cdrid
						AccountingRecordNumber = Integer.parseInt(st1.nextToken()); // sequenceno in aaaService Now
																					// SequenceNo
						// AccountingRecordType = Integer.parseInt(st1.nextToken()); // event based 1
						// session based 2,3,4
						int temp_index = SessionId.indexOf('[');
						if (temp_index != -1) {
							cdrid = SessionId.substring(0, temp_index) + String.valueOf(AccountingRecordNumber);
						} else {
							cdrid = SessionId + String.valueOf(AccountingRecordNumber);
						}

						String requestType = st1.nextToken();
						if (requestType.equals("SESSION_START"))
							AccountingRecordType = 2; // session based 2,3,4
						else if (requestType.equals("SESSION_INTERIM"))
							AccountingRecordType = 3;
						else if (requestType.equals("SESSION_STOP"))
							AccountingRecordType = 4;
						else if (requestType.equals("SPECIFIC_CHARGE"))
							AccountingRecordType = 1; // event based
						CallingPartyNumber = st1.nextToken(); // should be from CGWAPP according to direction
						CalledParty = st1.nextToken();// should be from CGWAPP according to direction
						direction = Integer.parseInt(st1.nextToken());
						rateid = st1.nextToken();
						boolean SpIdExists = Global.SPID_MAP.containsKey(rateid);
						SP_ID = (SpIdExists == true) ? Global.SPID_MAP.get(rateid) : "";
						amount = Integer.parseInt(st1.nextToken());
						UnitsUsed = Integer.parseInt(st1.nextToken());// Call GetAmout Value ? Now
																		// ConsumeUnitLastRequest
						unitsReserved = Integer.parseInt(st1.nextToken()); // Lpsize in aaaService Now PulseSize
						if (direction == 1) {
							MSISDN = CalledParty;
							TrafficCase = "21";
						} else {
							MSISDN = CallingPartyNumber;
							TrafficCase = "20";
						}

						if (MSISDN.length() >= 10) {
							prefix = MSISDN.substring(MSISDN.length() - 10);
							MSISDN = "880" + MSISDN.substring(MSISDN.length() - 10);
							prefix = prefix.substring(2, 5);

						}
						Global.logForNumber(MSISDN,
								Global.dateFormatHourMinSec.format(new Date()) + " Request Message " + message,
								"run method of handlerequest");

					} catch (Exception ex) {
						ex.printStackTrace();
						try {
							Global.write(
									Global.dateFormatHourMinSec.format(new Date()) + " " + Global.getStackTrace(ex),
									"run to ParameterCount and Parsing");
							Global.logForNumber(MSISDN,
									Global.dateFormatHourMinSec.format(new Date()) + " " + Global.getStackTrace(ex),
									"run to ParameterCount and Parsing");
						} catch (Exception ex1) {
							ex1.printStackTrace();
						}
					}

					try {
						if (prefix != "") {
							boolean hasvalue = Global.Numberseries_MAP.containsKey(prefix);
							number = (hasvalue == true) ? Global.Numberseries_MAP.get(prefix) : 0;

						}
						if (SP_ID != null && !SP_ID.equals("")) {
							if (Global.NumberSeriesEnable == true) {
								if (number == -1)
									resultcode = 2222;
								else {
									String temp[] = Global.myURI.split("\\|");
									int NumberOfUrl = Integer.parseInt(temp[0]);
									if (number >= NumberOfUrl)
										number = 0;
									instance = new EventCharging(MSISDN, amount, SP_ID, cdrid, number);
									resultcode = instance.GetResultCode();
								}

							} else {
								instance = new EventCharging(MSISDN, amount, SP_ID, cdrid);
								resultcode = instance.GetResultCode();
							}

						} else {
							resultcode = 1111;
						}

						rs = result = SessionId + " " + resultcode + " " + AccountingRecordType + " "
								+ AccountingRecordNumber + " 180";
					} catch (Exception e) {
						e.printStackTrace();
					}

					if (rs == null || rs.equals(null)) {
						result = SessionId + " " + "6000" + " " + AccountingRecordType + " " + AccountingRecordNumber
								+ " 180";
					} else {
						result = rs;
					}
					try {
						Global.write(Global.dateFormatHourMinSec.format(new Date()) + " Responce Message: " + result,
								"run method handlerequest.java");
						Global.logForNumber(MSISDN,
								Global.dateFormatHourMinSec.format(new Date()) + " Responce Message: " + result,
								"run method handlerequest.java");
					} catch (Exception ex1) {
						ex1.printStackTrace();
					}

				}

				return result;
			}
		} catch (Exception except) {
			except.printStackTrace();
		}

		return null;
	}

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}

	public Charging getCharging() {
		return charging;
	}

	public void setCharging(Charging charging) {
		this.charging = charging;
	}
}
