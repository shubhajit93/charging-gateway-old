package com.ssdtech.java.model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;

public class PriceLimit {
    private final static int YEAR_IN_DAYS = 365;
    private long id;
    private String uid;
    private int monthIndex;
    private LocalDateTime lastYearlyResetDate;
    private long monthlyLimit;
    private long yearlyLimit;

    public PriceLimit(long id, String uid, int monthIndex, LocalDateTime lastYearlyResetDate, long monthlyLimit, long yearlyLimit) {
        this.id = id;
        this.uid = uid;
        this.monthIndex = monthIndex;
        this.lastYearlyResetDate = lastYearlyResetDate;
        this.monthlyLimit = monthlyLimit;
        this.yearlyLimit = yearlyLimit;
    }

    public long getId() {
        return this.id;
    }

    public String getUid() {
        return this.uid;
    }

    public int getMonthIndex() {
        return this.monthIndex;
    }

    public LocalDateTime getLastYearlyResetDate() {
        return this.lastYearlyResetDate;
    }

    public LocalDateTime getLastYearlyResetDateInString() {
        return this.lastYearlyResetDate;
    }

    public long getMonthlyLimit() {
        return this.monthlyLimit;
    }

    public long getYearlyLimit() {
        return this.yearlyLimit;
    }

    public boolean isCurrentMonthCrossed() {
        return LocalDateTime.now().getMonthValue() != this.monthIndex;
    }

    public boolean isYearCrossed() {
        return Duration.between(this.lastYearlyResetDate, LocalDateTime.now()).toDays() >= YEAR_IN_DAYS;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public void setMonthIndex(int monthIndex) {
        this.monthIndex = monthIndex;
    }

    public void setLastYearlyResetDate(LocalDateTime lastYearlyResetDate) {
        this.lastYearlyResetDate = lastYearlyResetDate;
    }

    public void setMonthlyLimit(long monthlyLimit) {
        this.monthlyLimit = monthlyLimit;
    }

    public void setYearlyLimit(long yearlyLimit) {
        this.yearlyLimit = yearlyLimit;
    }
}
