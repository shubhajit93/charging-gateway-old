package com.ssdtech.java.log.monitoring;

import java.util.HashMap;
import java.util.Map;

public class StateGenerator {
	private static final int SESSION_START_INDEX = 0;
	private static final int SESSION_INTERIM_INDEX = 1;
	private static final int SESSION_STOP_INDEX = 2;
	private static final int SPECIFIC_CHARGE_INDEX = 3;
	
	private static final int TRANSACTION_START = 0;
	private static final int TRANSACTION_END = 1;
	
	private static final int TRANSACTION_FAILURE = 0;
	private static final int TRANSACTION_SUCCESS = 1;
	
    private static final TransactionState[][][] stateContextLookupTable = new TransactionState[4][2][2];
    private static final Map<String, Integer> chargingTypeMapping = new HashMap<>();
    private static final int DEFAULT_CHARGING_TYPE_INDEX = 3;
    
    static {
    	stateContextLookupTable[SESSION_START_INDEX][TRANSACTION_START][TRANSACTION_FAILURE] = TransactionState.INITIALIZING;
    	stateContextLookupTable[SESSION_START_INDEX][TRANSACTION_START][TRANSACTION_SUCCESS] = TransactionState.INITIALIZING;
    	stateContextLookupTable[SESSION_START_INDEX][TRANSACTION_END][TRANSACTION_FAILURE] = TransactionState.RELEASED;
    	stateContextLookupTable[SESSION_START_INDEX][TRANSACTION_END][TRANSACTION_SUCCESS] = TransactionState.NA;
    	
    	stateContextLookupTable[SESSION_INTERIM_INDEX][TRANSACTION_START][TRANSACTION_FAILURE] = TransactionState.NA;
    	stateContextLookupTable[SESSION_INTERIM_INDEX][TRANSACTION_START][TRANSACTION_SUCCESS] = TransactionState.NA;
    	stateContextLookupTable[SESSION_INTERIM_INDEX][TRANSACTION_END][TRANSACTION_FAILURE] = TransactionState.RELEASED;
    	stateContextLookupTable[SESSION_INTERIM_INDEX][TRANSACTION_END][TRANSACTION_SUCCESS] = TransactionState.CONNECTED;
    	
    	stateContextLookupTable[SESSION_STOP_INDEX][TRANSACTION_START][TRANSACTION_FAILURE] = TransactionState.NA;
    	stateContextLookupTable[SESSION_STOP_INDEX][TRANSACTION_START][TRANSACTION_SUCCESS] = TransactionState.NA;
    	stateContextLookupTable[SESSION_STOP_INDEX][TRANSACTION_END][TRANSACTION_FAILURE] = TransactionState.RELEASED;
    	stateContextLookupTable[SESSION_STOP_INDEX][TRANSACTION_END][TRANSACTION_SUCCESS] = TransactionState.RELEASED;
    	
    	stateContextLookupTable[SPECIFIC_CHARGE_INDEX][TRANSACTION_START][TRANSACTION_FAILURE] = TransactionState.INITIALIZING;
    	stateContextLookupTable[SPECIFIC_CHARGE_INDEX][TRANSACTION_START][TRANSACTION_SUCCESS] = TransactionState.INITIALIZING;
    	stateContextLookupTable[SPECIFIC_CHARGE_INDEX][TRANSACTION_END][TRANSACTION_FAILURE] = TransactionState.RELEASED;
    	stateContextLookupTable[SPECIFIC_CHARGE_INDEX][TRANSACTION_END][TRANSACTION_SUCCESS] = TransactionState.RELEASED;
    }
    
    static {
    	chargingTypeMapping.put("SESSION_START", 0);
    	chargingTypeMapping.put("SESSION_INTERIM", 1);
    	chargingTypeMapping.put("SESSION_STOP", 2);
    	chargingTypeMapping.put("SPECIFIC_CHARGE", 3);
    }
    
    public static TransactionState getTransactionState(String chargingType, boolean beginTransaction, boolean successTransaction) {
    	int chargingIndex = getChargingTypeIndex(chargingType);
    	int transactionPhaseIndex = beginTransaction ? 0 : 1;
    	int transactionOutcomeIndex = successTransaction ? 1 : 0;
    	
    	return stateContextLookupTable[chargingIndex][transactionPhaseIndex][transactionOutcomeIndex];
    }
    
    private static int getChargingTypeIndex(String chargingType) {
    	return chargingTypeMapping.getOrDefault(chargingType.toUpperCase(), DEFAULT_CHARGING_TYPE_INDEX);
    }
}
