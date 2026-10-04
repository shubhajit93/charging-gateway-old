package com.ssdtech.java.controller;

import com.ssdtech.java.model.BillingResponse;
import com.ssdtech.java.model.ProcedureParams;

public interface BillingInterface {
	BillingResponse chargeSubscriber(ProcedureParams params);
}
