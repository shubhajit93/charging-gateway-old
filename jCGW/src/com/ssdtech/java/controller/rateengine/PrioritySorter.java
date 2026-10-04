package com.ssdtech.java.controller.rateengine;

import com.ssdtech.java.model.RateMaster;

import java.util.Comparator;

public class PrioritySorter implements Comparator<RateMaster> {
    @Override
    public int compare(RateMaster o1, RateMaster o2) {
       return Integer.toString(o1.getPriority()).compareTo(Integer.toString(o2.getPriority()));
    }
}
