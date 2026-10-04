package com.ssdtech.cgwcommon;

public interface INRequester {
    
	public String queryBalance(String balanceQuery);
	public String chargeSubscriber(String chargeRequest);
	public String executeCommand(String command);
}
