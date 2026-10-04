package com.ssdtech;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;

import com.ericsson.pps.diameter.dccapi.avp.ServiceParameterInfoAvp;
import com.ericsson.pps.diameter.dccapi.avp.ServiceParameterTypeAvp;
import com.ericsson.pps.diameter.dccapi.avp.ServiceParameterValueAvp;
import com.ericsson.pps.diameter.hlapi.ChargingException;
import com.ericsson.pps.diameter.hlapi.quota.RequestedQuota;
import com.ericsson.pps.diameter.rfcapi.base.DiameterConfigException;
import com.ericsson.pps.diameter.rfcapi.base.avp.Avp;
import com.ericsson.pps.diameter.scapv2.SCAPChargingFactory;
import com.ericsson.pps.diameter.scapv2.SCAPEvent;
import com.ericsson.pps.diameter.scapv2.SCAPRatingParameters;
import com.ericsson.pps.diameter.scapv2.SCAPSession;
import com.ericsson.pps.diameter.scapv2.SCAPSessionProperties;
import com.ericsson.pps.diameter.scapv2.avp.ServiceProviderIdAvp;
import com.ericsson.pps.diameter.scapv2.avp.TimeZoneAvp;

public class EventCharging extends AbstractCharging {

	// public static long ResultCode=6001;
	private static boolean call = false;
	private long RsCode;
	private AnswerListener answerListener;

	public EventCharging() {
		try {
			this.stackcall();
		} catch (Exception ex) {
			ex.printStackTrace();
			try {
				Global.write(Global.dateFormatHourMinSec.format(new Date()) + Global.getStackTrace(ex),
						"Exception in statck started");
			} catch (Exception e1) {
				e1.printStackTrace();
			}

		}

	}

	public EventCharging(String Msisdn, int amount) {
		this.MSISDN = Msisdn;
		// this.Amount=amount*10000;
		this.Amount = amount;
		this.run();
		// System.exit(0);
	}

	public EventCharging(String Msisdn, int amount, String spid, String cdrid) {
		this.MSISDN = Msisdn;
		// this.Amount=amount*10000;
		this.Amount = amount;
		this.SPID = spid;
		this.CDRID = cdrid;
		this.run();

		// System.exit(0);
	}

	public EventCharging(String Msisdn, int amount, String spid, String cdrid, Integer number) {
		this.MSISDN = Msisdn;
		// this.Amount=amount*10000;
		this.Amount = amount;
		this.SPID = spid;
		this.CDRID = cdrid;
		this.run(number);
	}

	private void stackcall() {
		try {
			String temp[] = Global.myURI.split("\\|");
			int NumberOfUrl = Integer.parseInt(temp[0]);
			System.out.println("trying to start all stacks");
			for (int i = 0; i < NumberOfUrl; i++) {
				boolean started = init(i);
				if (started == false) {
					System.out.println("Stack " + (i + 1) + " started failed");
					Thread.sleep((1000));
					i = i - 1;
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			try {
				Global.write(Global.dateFormatHourMinSec.format(new Date()) + Global.getStackTrace(ex),
						"Exception in statck started");
			} catch (Exception ex1) {
				ex1.printStackTrace();
			}

		}
	}

	private void doDirectDebit(int number) {
		String sessionid = "";
		String temp[] = Global.myURI.split("\\|");
		int NumberOfUrl = Integer.parseInt(temp[0]);
		String ipparse[] = new String[NumberOfUrl];
		SCAPSessionProperties sessionProperties = createSessionProperties(number);

		// sessionProperties.setDestinationHost("aaa://localhost:3890;transport=tcp");
		// System.out.println(Global.OwnIPAddress);
		// System.out.println(Global.ServiceContextId);

		if (Global.OwnIPAddress.equals("NA") == false && number == -1)
			sessionProperties.setDestinationHost(Global.OwnIPAddress);
		else if (Global.OwnIPAddress.equals("NA") == false && number != -1) {
			ipparse = Global.OwnIPAddress.split("\\|");
			sessionProperties.setDestinationHost(ipparse[number]);

		}
		sessionProperties.setServiceContextId(Global.ServiceContextId);

		/*
		 * SCAPSession event = SCAPChargingFactory.createSession( stack,
		 * sessionProperties, new AnswerListener(this));
		 */
		SCAPEvent event = null;
		if (number == -1) {
			event = SCAPChargingFactory.createEvent(stack, sessionProperties, new AnswerListener(this));
		} else if (number >= 0 && number < NumberOfUrl) {
			answerListener = new AnswerListener(this);
			event = SCAPChargingFactory.createEvent(stackseries[number], sessionProperties, answerListener);
		} else {
			try {
				Global.write(Global.dateFormatHourMinSec.format(new Date()) + "Error in number",
						"DoDerictdebit function event charging class");
				Global.logForNumber(this.MSISDN, Global.dateFormatHourMinSec.format(new Date()) + "Error in number",
						"DoDerictdebit function event charging class");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		RequestedQuota requestedQuota = createRequestedQuota();

		StringTokenizer st = new StringTokenizer(SPID, ">>");
		String spcode = st.nextToken();
		String param1 = st.nextToken();
		String param2 = st.nextToken();
		String param3 = st.nextToken();
		String param4 = st.nextToken();
		String param5 = st.nextToken();

		// BigInteger bigInt = BigInteger.valueOf(Integer.getInteger(param1));
		int bigInt = 1000;
		try {
			bigInt = Integer.parseInt(param1);
		} catch (Exception ex) {
			bigInt = 1000;
		}

		/*
		 * System.out.println(spcode); System.out.println(param1);
		 * System.out.println(param2); System.out.println(param3);
		 * System.out.println(param4);
		 */

		SCAPRatingParameters ratingParameters = createSCAPRatingParameters();

		ServiceProviderIdAvp sp_avp = new ServiceProviderIdAvp(spcode);
		ratingParameters.setServiceProviderIdAvp(sp_avp);

		// service parameter info
		ServiceParameterInfoAvp ParameterInfoAvp1 = new ServiceParameterInfoAvp();
		ServiceParameterInfoAvp ParameterInfoAvp2 = new ServiceParameterInfoAvp();
		ServiceParameterInfoAvp ParameterInfoAvp3 = new ServiceParameterInfoAvp();
		ServiceParameterInfoAvp ParameterInfoAvp4 = new ServiceParameterInfoAvp();

		// type
		ServiceParameterTypeAvp Parametertype1 = new ServiceParameterTypeAvp(0);
		ServiceParameterTypeAvp Parametertype2 = new ServiceParameterTypeAvp(200);
		ServiceParameterTypeAvp Parametertype3 = new ServiceParameterTypeAvp(201);
		ServiceParameterTypeAvp Parametertype4 = new ServiceParameterTypeAvp(202);

		// value
		ServiceParameterValueAvp ParameterValue1 = new ServiceParameterValueAvp(
				ByteBuffer.allocate(4).putInt(bigInt).array());
		ServiceParameterValueAvp ParameterValue2 = new ServiceParameterValueAvp(param2.getBytes());
		ServiceParameterValueAvp ParameterValue3 = new ServiceParameterValueAvp(param3.getBytes());
		ServiceParameterValueAvp ParameterValue4 = new ServiceParameterValueAvp(param4.getBytes());

		ParameterInfoAvp1.setValues(Parametertype1, ParameterValue1);
		ParameterInfoAvp2.setValues(Parametertype2, ParameterValue2);
		ParameterInfoAvp3.setValues(Parametertype3, ParameterValue3);
		ParameterInfoAvp4.setValues(Parametertype4, ParameterValue4);

		// lsit of service parameter info
		List<ServiceParameterInfoAvp> listServiceParameterInfoAvp = new ArrayList<ServiceParameterInfoAvp>();
		listServiceParameterInfoAvp.add(ParameterInfoAvp1);
		listServiceParameterInfoAvp.add(ParameterInfoAvp2);
		listServiceParameterInfoAvp.add(ParameterInfoAvp3);
		listServiceParameterInfoAvp.add(ParameterInfoAvp4);

		/*
		 * //one single service parameter info four value set ServiceParameterInfoAvp
		 * ParameterInfoAvp = new ServiceParameterInfoAvp() ;
		 * ParameterInfoAvp.setValues(Parametertype1,ParameterValue1,Parametertype2,
		 * ParameterValue2,Parametertype3,ParameterValue3,Parametertype4,ParameterValue4
		 * ); ratingParameters.addServiceParameterInfoAvp(ParameterInfoAvp);
		 */

		ratingParameters.addServiceParameterInfoAvps(listServiceParameterInfoAvp);
		/*
		 * if(param4.indexOf("_")==-1) {
		 * sessionid=param3.substring(2,4)+param4+this.CDRID; } else { String
		 * tem_parse[]=param4.split("_");
		 * sessionid=param3.substring(2,4)+tem_parse[tem_parse.length-1]+this.CDRID; }
		 */
		if (this.CDRID.length() <= Global.CdrLength) {
			sessionid = param5 + this.CDRID;
		} else {
			sessionid = param5 + this.CDRID.substring(this.CDRID.length() - Global.CdrLength);
		}

		com.ericsson.pps.diameter.hlapi.impl.AbstractOperation.generateSessionId(sessionid);

		try {
			String logmsg = "[" + this.MSISDN + "," + ipparse[number] + "]";
			String logmsg2 = "[" + sessionid + "," + spcode + "," + param1 + "," + param2 + "," + param3 + "," + param4
					+ "]";
			Global.write(Global.dateFormatHourMinSec.format(new Date()) + " [MSISDN,CCN] " + logmsg,
					"DoDerictdebit function event charging class");
			// Global.logForNumber(this.MSISDN,Global.dateFormatHourMinSec.format(new
			// Date())+"[MSISDN,CCN] "+logmsg,"DoDerictdebit function event charging
			// class");
			Global.write(Global.dateFormatHourMinSec.format(new Date()) + " [sessionid,serviceprovideid,0,200,201,202] "
					+ logmsg2, "DoDerictdebit function event charging class");
			Global.logForNumber(this.MSISDN, Global.dateFormatHourMinSec.format(new Date()) + "[MSISDN,CCN] " + logmsg,
					"DoDerictdebit function event charging class");
			Global.logForNumber(
					this.MSISDN, Global.dateFormatHourMinSec.format(new Date())
							+ " [sessionid,serviceprovideid,0,200,201,202] " + logmsg2,
					"DoDerictdebit function event charging class");
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		try {
			long startTime = System.currentTimeMillis();
			event.doDirectDebit(requestedQuota, ratingParameters);

			Global.write(Global.dateFormatHourMinSec.format(new Date()) + "Waiting For Answer",
					"DoDerictdebit function event charging class");
			Global.logForNumber(this.MSISDN, Global.dateFormatHourMinSec.format(new Date()) + "Waiting For Answer",
					"DoDerictdebit function event charging class");
			if (answerListener.getcheckcode() == false)
				waitForAnswer();
			// String CodeTemp=this.sb.toString();
			// this.RsCode=Long.parseLong(CodeTemp);
			this.RsCode = answerListener.getCode();
			long endTime = System.currentTimeMillis();
			if (Global.enableResultCodeWiseLog && Global.resultCodesWriterMapping.containsKey(this.RsCode)) {
				Global.resultCodesWriterMapping.get(this.RsCode)
						.storeContent(ipparse[number] + "|" + this.MSISDN + "|" + (endTime - startTime) + "ms");
			}

			// Global.write(Global.dateFormatHourMinSec.format(new Date())+"Answer Gotten
			// :"+EventCharging.ResultCode,"DoDerictdebit function event charging class");
			// Global.logForNumber(this.MSISDN,Global.dateFormatHourMinSec.format(new
			// Date())+"Answer Gotten :"+EventCharging.ResultCode,"DoDerictdebit function
			// event charging class");

			Global.write(Global.dateFormatHourMinSec.format(new Date()) + "Answer Gotten :" + this.RsCode,
					"DoDerictdebit function event charging class");
			Global.logForNumber(this.MSISDN,
					Global.dateFormatHourMinSec.format(new Date()) + "Answer Gotten :" + this.RsCode,
					"DoDerictdebit function event charging class");

		} catch (ChargingException e) {
			// EventCharging.ResultCode=6010;
			this.RsCode = 6010;
			e.printStackTrace();
			try {
				Global.write(Global.dateFormatHourMinSec.format(new Date()) + Global.getStackTrace(e),
						"Exception in Charging message");
				Global.logForNumber(this.MSISDN,
						Global.dateFormatHourMinSec.format(new Date()) + Global.getStackTrace(e),
						"ChargingException in Charging message");
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			try {
				stackseries[number].start();
			} catch (Exception ex) {
				ex.printStackTrace();
				try {
					Global.write(Global.dateFormatHourMinSec.format(new Date()) + Global.getStackTrace(ex),
							"Exception in stack started");
					Global.logForNumber(this.MSISDN,
							Global.dateFormatHourMinSec.format(new Date()) + Global.getStackTrace(ex),
							"Exception  in stack started");
				} catch (Exception e4) {
					e4.printStackTrace();
				}

			}
		} catch (Exception ex) {
			// EventCharging.ResultCode=6010;
			this.RsCode = 6012;
			ex.printStackTrace();
			try {
				Global.write(Global.dateFormatHourMinSec.format(new Date()) + Global.getStackTrace(ex),
						"Exception in Charging message");
				Global.logForNumber(this.MSISDN,
						Global.dateFormatHourMinSec.format(new Date()) + Global.getStackTrace(ex),
						"GeneralException in Charging message");
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}

	}

	private void run() {
		if (stack == null)
			init();
		doDirectDebit(-1);
		// doRefund();
	}

	public long GetResultCode() {
		return this.RsCode;
	}

	private void run(int number) {
		if (stackseries[number] == null)
			init(number);
		doDirectDebit(number);

	}

}
