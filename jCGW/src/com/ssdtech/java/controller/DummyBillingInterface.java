package com.ssdtech.java.controller;

import com.ssdtech.java.model.BillingResponse;
import com.ssdtech.java.model.ProcedureParams;

public class DummyBillingInterface implements BillingInterface {

	@Override
	public BillingResponse chargeSubscriber(ProcedureParams params) {
		//System.out.println("********INSIDE dummy billing interface************");
		return BillingResponse.getSuccessDummyResponseFrom(params);
	}
}
