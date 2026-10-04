package com.ericsson.pps.diameter.hlapi.impl;

import com.ericsson.pps.diameter.dccapi.DCCStack;
import com.ericsson.pps.diameter.dccapi.avp.CCInputOctetsAvp;
import com.ericsson.pps.diameter.dccapi.avp.CCMoneyAvp;
import com.ericsson.pps.diameter.dccapi.avp.CCOutputOctetsAvp;
import com.ericsson.pps.diameter.dccapi.avp.CCServiceSpecificUnitsAvp;
import com.ericsson.pps.diameter.dccapi.avp.CCTimeAvp;
import com.ericsson.pps.diameter.dccapi.avp.CCTotalOctetsAvp;
import com.ericsson.pps.diameter.dccapi.avp.CurrencyCodeAvp;
import com.ericsson.pps.diameter.dccapi.avp.ExponentAvp;
import com.ericsson.pps.diameter.dccapi.avp.ServiceParameterInfoAvp;
import com.ericsson.pps.diameter.dccapi.avp.SubscriptionIdAvp;
import com.ericsson.pps.diameter.dccapi.avp.UnitValueAvp;
import com.ericsson.pps.diameter.dccapi.avp.ValueDigitsAvp;
import com.ericsson.pps.diameter.dccapi.command.Ccr;
import com.ericsson.pps.diameter.hlapi.ChargingException;
import com.ericsson.pps.diameter.hlapi.DccAnswerQueue;
import com.ericsson.pps.diameter.hlapi.RatingParameters;
import com.ericsson.pps.diameter.hlapi.ServiceContextAnswerListener;
import com.ericsson.pps.diameter.hlapi.SessionProperties;
import com.ericsson.pps.diameter.hlapi.quota.MoneyUnit;
import com.ericsson.pps.diameter.hlapi.quota.RequestedQuota;
import com.ericsson.pps.diameter.hlapi.quota.ServiceSpecificUnit;
import com.ericsson.pps.diameter.hlapi.quota.TimeUnit;
import com.ericsson.pps.diameter.hlapi.quota.VolumeUnit;
import com.ericsson.pps.diameter.rfcapi.base.avp.Avp;
import com.ericsson.pps.diameter.rfcapi.base.avp.AvpDataException;
import com.ericsson.pps.diameter.rfcapi.base.avp.EventTimestampAvp;
import com.ericsson.pps.diameter.rfcapi.base.avp.avpdatatypes.Time;
import com.ericsson.pps.diameter.rfcapi.base.message.DiameterMessage;
import com.ericsson.pps.diameter.rfcapi.base.message.DiameterMessageListener;
import com.ericsson.pps.diameter.rfcapi.util.DiameterLogger;
import com.ericsson.pps.diameter.util.AvpFactory;
import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public abstract class AbstractOperation implements DiameterMessageListener, TxTimerListener {
	protected static DiameterLogger logger = DiameterLogger.getLogger("com.ericsson.pps.diameter.hlapi");
	private static String SESSION_ID_PREFIX = "6-";
	protected DCCStack dccStack = null;
	private boolean isAnswerProcessed;
	protected DiameterMessage myAnswer;
	protected ServiceContextAnswerListener myCallBack;
	protected Ccr myCcr = null;
	protected String sessionId;
	public static String session;
	protected SessionProperties sessionProperties;
	private TxTimer timer;
	private static final long baseLine = System.currentTimeMillis() << 10;
	private static long uniqueCounter = 0L;

	protected List<Avp> createAvpListFromUnits(MoneyUnit paramMoneyUnit, ServiceSpecificUnit paramServiceSpecificUnit,
			TimeUnit paramTimeUnit, VolumeUnit paramVolumeUnit) {
		LinkedList localLinkedList1 = new LinkedList();
		long l3;
		if (paramMoneyUnit != null) {
			CCMoneyAvp localCCMoneyAvp = new CCMoneyAvp();
			UnitValueAvp localUnitValueAvp = new UnitValueAvp();
			LinkedList localLinkedList2 = new LinkedList();
			localLinkedList2.add(new ValueDigitsAvp(paramMoneyUnit.getValue()));
			int i = paramMoneyUnit.getExponent();
			if (i != 0) {
				localLinkedList2.add(new ExponentAvp(i));
			}
			localUnitValueAvp.setValues(localLinkedList2);
			localCCMoneyAvp.addSubAvp(localUnitValueAvp);
			l3 = paramMoneyUnit.getCurrencyCode();
			if (l3 != 0L) {
				localCCMoneyAvp.addSubAvp(new CurrencyCodeAvp(l3));
			}
			localLinkedList1.add(localCCMoneyAvp);
		}
		if (paramServiceSpecificUnit != null) {
			localLinkedList1.add(new CCServiceSpecificUnitsAvp(paramServiceSpecificUnit.getServiceSpecificUnit()));
		}
		if (paramTimeUnit != null) {
			localLinkedList1.add(new CCTimeAvp(paramTimeUnit.getTimeUnit()));
		}
		if (paramVolumeUnit != null) {
			long l1 = paramVolumeUnit.getVolumeInputOctets();
			if (l1 != 0L) {
				localLinkedList1.add(new CCInputOctetsAvp(l1));
			}
			long l2 = paramVolumeUnit.getVolumeOutputOctets();
			if (l2 != 0L) {
				localLinkedList1.add(new CCOutputOctetsAvp(l2));
			}
			l3 = paramVolumeUnit.getVolumeTotalOctets();
			if (l3 != 0L) {
				localLinkedList1.add(new CCTotalOctetsAvp(l3));
			}
		}
		return localLinkedList1;
	}

	protected void createCcr(long paramLong, RatingParameters paramRatingParameters) {
		this.myCcr = new Ccr(getSessionId(), this.dccStack.getDiameterStack(),
				this.sessionProperties.getServiceContextId());
		this.myCcr.setCCRequestNumber(paramLong);
		updateCcrWithSessionProperties();
		updateCcrWithRatingParameters(paramRatingParameters);
	}

	private List<Avp> createHumanReadableAvps(List<Avp> paramList) {
		LinkedList localLinkedList = new LinkedList();
		Iterator localIterator = paramList.iterator();
		Avp localAvp1 = null;
		Avp localAvp2 = null;
		try {
			while (localIterator.hasNext()) {
				localAvp2 = (Avp) localIterator.next();
				try {
					localAvp1 = (Avp) AvpFactory.getInstance().getAvpClass(localAvp2)
							.getConstructor(new Class[] { Avp.class }).newInstance(new Object[] { localAvp2 });
				} catch (Exception localException1) {
					localAvp1 = AvpFactory.getInstance().createAvp(localAvp2);
					logger.debug("No copy constructor or AVP class not loaded; AVP CODE: " + localAvp2.getAvpCode());
				}
				localLinkedList.add(localAvp1);
			}
		} catch (Exception localException2) {
			localException2.printStackTrace();
		}
		return localLinkedList;
	}

	protected DiameterMessage createHumanReadableMessage() {
		DiameterMessage localDiameterMessage = new DiameterMessage(this.myAnswer.getCommandCode());
		localDiameterMessage.setApplicationId(this.myAnswer.getApplicationId());
		localDiameterMessage.addAll(createHumanReadableAvps(this.myAnswer.getAvps()));
		return localDiameterMessage;
	}

	private static synchronized long generateUniqueId() {
		uniqueCounter += 1L;
		return baseLine + uniqueCounter;
	}

	protected void generateSessionId() {
		this.sessionId = AbstractOperation.session;
	}

	public static void generateSessionId(String sessionid) {
		AbstractOperation.session = sessionid;
	}

	protected String getSessionId() {
		return this.sessionId;
	}

	public void processAnswer(DiameterMessage paramDiameterMessage1, DiameterMessage paramDiameterMessage2) {
		if (this.isAnswerProcessed) {
			logger.fine("exitting process answer,      answer is already precessed!!");
			return;
		}
		this.timer.stop();
		this.myAnswer = paramDiameterMessage1;
		this.isAnswerProcessed = true;
		DccAnswerQueue.getInstance().storeInQueue(this);
	}

	public void processTxExpired() {
		logger.partial("TxTimer Expired!");
		updateSessionState(StateMachineEvent.TxExpired);
		this.myCallBack.txTimerExpired();
	}

	public abstract void sendAnswerToListener();

	protected void sendCcr() throws ChargingException {
		if (logger.isDebugLevel()) {
			logger.debug("CCR to send:\n" + this.myCcr.toString());
		}
		this.timer = new TxTimer(this, this.sessionProperties.getTxTimerValue());
		this.isAnswerProcessed = false;
		try {
			this.myCcr.send(this);
		} catch (Exception localException) {
			this.timer.stop();
			throw new ChargingException("Exception during sending CCR: " + localException.toString());
		}
		this.myCcr = null;
	}

	protected void setRequestedQuotaInCcr(RequestedQuota paramRequestedQuota) {
		if (paramRequestedQuota == null) {
			return;
		}
		MoneyUnit localMoneyUnit = paramRequestedQuota.getRequestedMoneyUnit();
		ServiceSpecificUnit localServiceSpecificUnit = paramRequestedQuota.getRequestedServiceSpecificUnit();
		TimeUnit localTimeUnit = paramRequestedQuota.getRequestedTimeUnit();
		VolumeUnit localVolumeUnit = paramRequestedQuota.getRequestedVolumeUnit();
		List localList = createAvpListFromUnits(localMoneyUnit, localServiceSpecificUnit, localTimeUnit,
				localVolumeUnit);
		this.myCcr.setRequestedServiceUnit(localList);
	}

	protected void updateCcrWithRatingParameters(RatingParameters paramRatingParameters) {
		if (paramRatingParameters == null) {
			return;
		}
		Object localObject = paramRatingParameters.getServiceParameterInfoAvps().iterator();
		while (((Iterator) localObject).hasNext()) {
			ServiceParameterInfoAvp localServiceParameterInfoAvp = (ServiceParameterInfoAvp) ((Iterator) localObject)
					.next();
			try {
				this.myCcr.addServiceParameterInfo(localServiceParameterInfoAvp.getValues());
			} catch (AvpDataException localAvpDataException) {
				logger.error("Cannot read the data from SPI AVP");
			}
		}
		localObject = paramRatingParameters.getAvps();
		if (localObject != null) {
			this.myCcr.addAvps((Collection) localObject);
		}
	}

	private void updateCcrWithSessionProperties() {
		Long localLong = this.sessionProperties.getServiceIdentifier();
		if (localLong != null) {
			this.myCcr.setServiceIdentifier(localLong.longValue());
		}
		String str = this.sessionProperties.getDestinationHost();
		if (str != null) {
			this.myCcr.setDestinationHost(str);
		}
		this.myCcr.setDestinationRealm(this.sessionProperties.getDestinationRealm());
		if (this.sessionProperties.isEnabledAddEventTimeStamp()) {
			this.myCcr.addAvp(new EventTimestampAvp(new Time(new Date(System.currentTimeMillis()))));
		}
		Iterator localIterator = this.sessionProperties.getSubscriptionIdList().iterator();
		while (localIterator.hasNext()) {
			SubscriptionIdAvp localSubscriptionIdAvp = (SubscriptionIdAvp) localIterator.next();
			this.myCcr.addAvp(localSubscriptionIdAvp);
		}
	}

	protected abstract void updateSessionState(StateMachineEvent paramStateMachineEvent);

	protected static enum StateMachineEvent {
		OtherFailedResultCode, SuccessfulCCAnswerReceived, TxExpired;

		private StateMachineEvent() {
		}
	}
}

/*
 * Location:
 * D:\Projects\Banglalink\Docs\Shahriar\SCAPv2Emulator_190\SCAPv2Emulator_190\
 * HighLevelApi\ Qualified Name:
 * com.ericsson.pps.diameter.hlapi.impl.AbstractOperation JD-Core Version:
 * 0.7.0.1
 */