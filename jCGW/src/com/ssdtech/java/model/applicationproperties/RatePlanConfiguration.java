package com.ssdtech.java.model.applicationproperties;

import java.util.Properties;
import java.util.StringJoiner;

import com.ssdtech.java.utils.Utility;

public class RatePlanConfiguration {
	public final boolean refundSession;
	public final boolean regionCheck;
	public final boolean callTypeIdCheck;
	public final boolean directDialingCheck;
	public final boolean packageRangeCheck;
	public final long packageLoadAfter;
	public final String defaultPackageId;
	public final int minPulseSize;
	public final String defaultTimeSlotId;
	public final String defaultCallTypeId;
	public final int defaultTPS;
	
	public RatePlanConfiguration(Properties properties) {
		String refundSession = Utility.ptrim(properties, "REFUND_SESSION", "1");
		String regionCheck = Utility.ptrim(properties, "REGION_CHECK_ENABLE", "0");
		String callTypeIdCheck = Utility.ptrim(properties, "CALL_TYPE_ID_CHECK", "0");
		String directDialingCheck = Utility.ptrim(properties, "DIRECT_DIALING_CHECK", "0");
		String packageRangeCheck = Utility.ptrim(properties, "PACKAGE_RANGE_CHECK_ENABLE", "0");
		String packageLoadAfter = Utility.ptrim(properties, "PACKAGE_LOADING_AFTER");
		String defaultPackageId = Utility.ptrim(properties, "DefaultPackageID");
		String minPulseSize = Utility.ptrim(properties, "MinPulseSize", "1");
		String defaultTimeSlotId = Utility.ptrim(properties, "DefaultTimeSlotId", "AnyTime");
		String defaultCallTypeId = Utility.ptrim(properties, "DefaultCallTypeId", "AnyCall");
		String defaultTPS = Utility.ptrim(properties, "DefaultTPS", "1000");
		
		this.refundSession = refundSession.equals("1");
		this.regionCheck = regionCheck.equals("1");
		this.callTypeIdCheck = callTypeIdCheck.equals("1");
		this.directDialingCheck = directDialingCheck.equals("1");
		this.packageRangeCheck = packageRangeCheck.equals("1");
		this.packageLoadAfter = Long.valueOf(packageLoadAfter);
		this.defaultPackageId = defaultPackageId;
		this.minPulseSize = Integer.valueOf(minPulseSize);
		this.defaultTimeSlotId = defaultTimeSlotId;
		this.defaultCallTypeId = defaultCallTypeId;
		this.defaultTPS = Integer.valueOf(defaultTPS);
	}
	
	@Override
	public String toString() {
		return new StringJoiner("\r\n")
				        .add("\r\n\r\n[Rate Plan Configuration]")
				        .add("REFUND_SESSION = " + Utility.getEncodedValueForBooleanProperties(this.refundSession))
				        .add("REGION_CHECK_ENABLE = " + Utility.getEncodedValueForBooleanProperties(this.regionCheck))
				        .add("CALL_TYPE_ID_CHECK = " + Utility.getEncodedValueForBooleanProperties(this.callTypeIdCheck))
				        .add("DIRECT_DIALING_CHECK = " + Utility.getEncodedValueForBooleanProperties(this.directDialingCheck))
				        .add("PACKAGE_RANGE_CHECK_ENABLE = " + Utility.getEncodedValueForBooleanProperties(this.packageRangeCheck))
				        .add("PACKAGE_LOADING_AFTER = " + this.packageLoadAfter)
				        .add("DefaultPackageID = " + this.defaultPackageId)
				        .add("MinPulseSize = " + this.minPulseSize)
				        .add("DefaultTimeSlotId = " + this.defaultTimeSlotId)
				        .add("DefaultCallTypeId = " + this.defaultCallTypeId)
				        .add("DefaultTPS = " + this.defaultTPS)
				        .toString();
	}
}
