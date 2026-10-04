package com.ssdtech.java.model;

public class RatingInfo {
	private final int totalSteps;
	private final long stepDuration;
	private final long stepPulse;
	private final long stepPulseRate;
	private final long amount;
	private long pulseSize;
	private final long refundAmount;

	public RatingInfo(final int totalSteps, final long stepDuration, final long stepPulse, final long stepPulseRate,
			final long amount, final long pulseSize, final long refundAmount) {
		this.totalSteps = totalSteps;
		this.stepDuration = stepDuration;
		this.stepPulse = stepPulse;
		this.stepPulseRate = stepPulseRate;

		this.amount = amount;
		this.pulseSize = pulseSize;
		this.refundAmount = refundAmount;
	}

	public int getTotalSteps() {
		return this.totalSteps;
	}

	public long getStepDuration() {
		return this.stepDuration;
	}

	public long getStepPulse() {
		return this.stepPulse;
	}

	public long getStepPulseRate() {
		return this.stepPulseRate;
	}

	public long getAmount() {
		return this.amount;
	}

	public long getPulseSize() {
		return this.pulseSize;
	}
	
	public long getRefundAmount() {
		return this.refundAmount;
	}
	
	public void setPulseSize(long pulseSize) {
		this.pulseSize = pulseSize;
	}
	
	@Override
	public String toString() {
		return "Total steps: " + this.totalSteps + ", " +
	           "Step duration: " + this.stepDuration + ", " +
			   "Step pulse: " + this.stepPulse + ", " +
	           "Step pulse rate: " + this.stepPulseRate + ", " +
			   "Amount: " + this.amount + ", " +
	           "Pulse size: " + this.pulseSize + ", " +
			   "Refund amount: " + this.refundAmount;
	}
}