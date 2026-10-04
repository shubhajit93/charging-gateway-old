package com.ssdtech.java.model;

public class RatePulse {
	public final int stepDuration;
	public final int stepPulse;
	public final long stepPulseRate;
	public final int minAmount;
	public final int maxAmount;

	public RatePulse(final int stepDuration, final int stepPulse, final long stepPulseRate, final int minAmount, final int maxAmount) {
		this.stepDuration = stepDuration;
		this.stepPulse = stepPulse;
		this.stepPulseRate = stepPulseRate;
		this.minAmount = minAmount;
		this.maxAmount = maxAmount;
	}
}
