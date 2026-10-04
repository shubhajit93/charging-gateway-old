package com.ssdtech.main.java.model;

public class AppConstant {
	public static final String ANSI_RESET = "\u001B[0m";
	public static final String ANSI_BLACK = "\u001B[30m";
	public static final String ANSI_RED = "\u001B[31m";
	public static final String ANSI_GREEN = "\u001B[32m";
	public static final String ANSI_YELLOW = "\u001B[33m";
	public static final String ANSI_BLUE = "\u001B[34m";
	public static final String ANSI_PURPLE = "\u001B[35m";
	public static final String ANSI_CYAN = "\u001B[36m";
	public static final String ANSI_WHITE = "\u001B[37m";	
	
    public static int port;
    public static int noOfThreads;
    public static int logEnable;
    public static long returnedAmountOnSuccess;
    
    public static int operatorINDelayLowerLimit;
    public static int operatorINDelayUpperLimit;
    public static int operatorINDelayLimitDiff;
    
    public static int airDelayLowerLimit;
    public static int airDelayUpperLimit;
    public static int airDelayLimitDiff;
    
    public static int unsuccessfulPercentage;
    public static int successfulPercentage;
    
    public static Boolean[] successDistribution;
}
