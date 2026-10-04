package com.ssdtech.java.utils;

import com.ssdtech.java.db.client.DBClient;
import com.ssdtech.java.model.PriceLimit;
import com.ssdtech.java.model.ResultCode;
import com.ssdtech.java.model.applicationproperties.ApplicationProperties;

import java.time.LocalDateTime;

public class PriceLimitChecker {

    public static ResultCode capture(String uid, long amount) {
        try {
            PriceLimit priceLimit = getOrCreateUserPriceLimitInfo(uid);

            if (priceLimit.isYearCrossed()) {
                LocalDateTime now = LocalDateTime.now();
                priceLimit.setMonthIndex(now.getMonthValue());
                priceLimit.setLastYearlyResetDate(now);
                priceLimit.setMonthlyLimit(ApplicationProperties.getPriceLimitConfiguration().monthlyLimit);
                priceLimit.setYearlyLimit(ApplicationProperties.getPriceLimitConfiguration().yearlyLimit - ApplicationProperties.getPriceLimitConfiguration().monthlyLimit);
                DBClient.resetUserPriceLimitInfo(priceLimit);
            } else if (priceLimit.isCurrentMonthCrossed()) {
                long remYearlyLimit = priceLimit.getYearlyLimit() +
                        (ApplicationProperties.getPriceLimitConfiguration().carryForward ? priceLimit.getMonthlyLimit() : 0) -
                        ApplicationProperties.getPriceLimitConfiguration().monthlyLimit;
                if (remYearlyLimit < 0) return ResultCode.Error_Price_Limit_Crossed;

                LocalDateTime now = LocalDateTime.now();
                priceLimit.setMonthIndex(now.getMonthValue());
                priceLimit.setMonthlyLimit(ApplicationProperties.getPriceLimitConfiguration().monthlyLimit);
                priceLimit.setYearlyLimit(remYearlyLimit);
                DBClient.resetUserPriceLimitInfo(priceLimit);
            }

            if (priceLimit.getMonthlyLimit() < amount) return ResultCode.Error_Price_Limit_Crossed;

            if (!DBClient.updateUserMonthlyPriceLimit(uid, amount)) return ResultCode.Error_Price_Limit_Crossed;

            return ResultCode.SUCCESS;
        } catch (Exception exception) {
            return ResultCode.ERROR_DB_Query_Failed;
        }
    }

    public static void revert(String uid, long amount) {
        DBClient.updateUserMonthlyPriceLimit(uid, -1 * amount);
    }

    private static PriceLimit getOrCreateUserPriceLimitInfo(String uid) throws Exception {
        PriceLimit priceLimit = DBClient.getUserPriceLimitInfo(uid);
        if (priceLimit != null) return priceLimit;

        LocalDateTime now = LocalDateTime.now();
        PriceLimit newPriceLimit = new PriceLimit(
                0,
                uid,
                now.getMonthValue(),
                LocalDateTime.now(),
                ApplicationProperties.getPriceLimitConfiguration().monthlyLimit,
                ApplicationProperties.getPriceLimitConfiguration().yearlyLimit - ApplicationProperties.getPriceLimitConfiguration().monthlyLimit
        );
        long id = DBClient.createUserPriceLimitInfo(newPriceLimit);
        newPriceLimit.setId(id);
        return newPriceLimit;
    }
}
