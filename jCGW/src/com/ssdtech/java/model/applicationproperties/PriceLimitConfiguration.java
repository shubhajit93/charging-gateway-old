package com.ssdtech.java.model.applicationproperties;

import com.ssdtech.java.utils.Utility;

import java.util.Properties;
import java.util.StringJoiner;

public class PriceLimitConfiguration {
    public final boolean enable;
    public final long monthlyLimit;
    public final long yearlyLimit;
    public final boolean carryForward;

    public PriceLimitConfiguration(Properties properties) {
        String enable = Utility.ptrim(properties, "PRICE_LIMIT_ENABLE", "0");
        String monthlyLimit = Utility.ptrim(properties, "PRICE_LIMIT_MONTHLY");
        String yearlyLimit = Utility.ptrim(properties, "PRICE_LIMIT_YEARLY");
        String carryForward = Utility.ptrim(properties, "PRICE_LIMIT_CARRY_FORWARD", "0");

        this.enable = enable.equals("1");
        this.monthlyLimit = Long.valueOf(monthlyLimit);
        this.yearlyLimit = Long.valueOf(yearlyLimit);
        this.carryForward = carryForward.equals("1");
    }

    @Override
    public String toString() {
        return new StringJoiner("\r\n")
                .add("\r\n\r\n[Price Limit configuration]")
                .add("PRICE_LIMIT_ENABLE = " + Utility.getEncodedValueForBooleanProperties(this.enable))
                .add("PRICE_LIMIT_MONTHLY = " + this.monthlyLimit)
                .add("PRICE_LIMIT_YEARLY = " + this.yearlyLimit)
                .add("PRICE_LIMIT_CARRY_FORWARD = " + Utility.getEncodedValueForBooleanProperties(this.carryForward))
                .toString();
    }
}
