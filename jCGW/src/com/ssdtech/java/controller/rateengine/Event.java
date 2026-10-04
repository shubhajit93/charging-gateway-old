package com.ssdtech.java.controller.rateengine;

import com.ssdtech.java.model.ProcedureParams;

public interface Event {
    
	void process(ProcedureParams params);
}
