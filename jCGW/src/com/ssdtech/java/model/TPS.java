package com.ssdtech.java.model;

public class TPS {
	private final String applicationId;
	private final String timeSlotId;
	private final long tpsLimit;
	
	public TPS (final String applicationId, final String timeSlotId, final long tpsLimit) {
		this.applicationId = applicationId;
		this.timeSlotId = timeSlotId;
		this.tpsLimit = tpsLimit;
	}
	
	public String getApplicationId() {
		return this.applicationId;
	}
	
	public String getTimeSlotId() {
		return this.timeSlotId;
	}
	
	public long getTPSLimit() {
		return this.tpsLimit;
	}
}