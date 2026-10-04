package com.ssdtech.java.controller;

import com.ssdtech.java.utils.JSONParser;
import com.ssdtech.java.utils.Parser;
import com.ssdtech.java.utils.PipeLineParser;

public class RequestParserFactory {
	
	public static Parser getParser(String request) {
		if (request == null) return new PipeLineParser(null); // It should be a dummy parser which will throw error.
		
		String trimmedRequest = request.trim();
		
		if (trimmedRequest.startsWith("{")) {
			return new JSONParser(trimmedRequest);
		} else {
			return new PipeLineParser(trimmedRequest);
		}
	}
}
