package com.ssdtech.java.model.applicationproperties;

import java.util.Properties;
import java.util.StringJoiner;

import com.ssdtech.java.utils.Utility;

public class RequestTransformationConfiguration {
	public final boolean discardPrefixOfOBD;
	public final int prefixOffsetOfOBD;
	public final int lengthOfOBD;
	public final String countryCode;
	
	public RequestTransformationConfiguration(Properties properties) {
		String discardPrefixOfOBD = Utility.ptrim(properties, "DISCARD_PREFIX_OBD");
		String prefixOffsetOBD = Utility.ptrim(properties, "CHECK_DISCARD_PREFIX_OBD");
		String lengthOfOBD = Utility.ptrim(properties, "MSISDN_LENGTH");
		String countryCode = Utility.ptrim(properties, "COUNTRY_CODE");
		
		this.discardPrefixOfOBD = discardPrefixOfOBD.equals("1");
		this.prefixOffsetOfOBD = Integer.valueOf(prefixOffsetOBD);
		this.lengthOfOBD = Integer.valueOf(lengthOfOBD);
		this.countryCode = countryCode;
	}
	
	@Override
	public String toString() {
		return new StringJoiner("\r\n")
				       .add("\r\n\r\n[CGW Request Transformation Configuration]")
				       .add("DISCARD_PREFIX_OBD = " + Utility.getEncodedValueForBooleanProperties(this.discardPrefixOfOBD))
				       .add("CHECK_DISCARD_PREFIX_OBD = " + this.prefixOffsetOfOBD)
				       .add("MSISDN_LENGTH = " + this.lengthOfOBD)
				       .add("COUNTRY_CODE = " + this.countryCode)
				       .toString();
	}
}