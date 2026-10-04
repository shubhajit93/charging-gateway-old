package com.ssdtech;

public class RateConfigurations {

	private String serviceProviderId;
	private String extentionNo;
	private String extentionText;
	private String eventNumber;

	public String getServiceProviderId() {
		return serviceProviderId;
	}

	public void setServiceProviderId(String srvProviderId) {
		if (srvProviderId.equalsIgnoreCase("serviceproviderid"))
			this.serviceProviderId = null;
		else
			this.serviceProviderId = srvProviderId;
	}

	public String getExtentionNo() {
		return extentionNo;
	}

	public void setExtentionNo(String extNo) {
		if (extNo.equalsIgnoreCase("extensionno"))
			this.extentionNo = null;
		else
			this.extentionNo = extNo;
	}

	public String getExtentionText() {
		return extentionText;
	}

	public void setExtentionText(String extText) {
		if (extText.equalsIgnoreCase("extensiontext"))
			this.extentionText = null;
		else
			this.extentionText = extText;
	}

	public String getEventNumber() {
		return eventNumber;
	}

	public void setEventNumber(String eventNo) {
		if (eventNo.equalsIgnoreCase("eventnumber"))
			this.eventNumber = null;
		else
			this.eventNumber = eventNo;
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		System.out.println("serviceProviderId: " + this.serviceProviderId + " extentionNo:" + this.extentionNo
				+ " extentionText:" + this.extentionText + " eventNumber:" + this.eventNumber);
		// return "serviceProviderId: "+this.serviceProviderId+"
		// extentionNo:"+this.extentionNo+" extentionText:"+this.extentionText+"
		// eventNumber:"+this.eventNumber;
		return super.toString();
	}

}
