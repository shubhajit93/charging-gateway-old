package com.ssdtech.java.utils;

import com.ssdtech.java.model.ProcedureParams;

public class JSONParser implements Parser {
	private final String cgwRequest;
	
	public JSONParser(String cgwRequest) {
		this.cgwRequest = cgwRequest;
	}
	
	@Override
	public void parse(ProcedureParams params) {
	    // [TODO]
	}
}
