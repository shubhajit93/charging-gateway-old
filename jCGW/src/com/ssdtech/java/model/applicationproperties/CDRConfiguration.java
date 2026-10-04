package com.ssdtech.java.model.applicationproperties;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.StringJoiner;

import com.ssdtech.java.model.Constant;
import com.ssdtech.java.utils.Utility;

public class CDRConfiguration {
	public final int numberOfCdrProcessingThreads;
	
	public final boolean cdrWriteInFileEnable;
	public final boolean immediatelySaveSuccessCdr;
	
	public final String cdrDirectory;
	public final int cdrFileCreationInterval;
	public final boolean cdrDuplicationCheck;
	public final boolean cdrDuplicationLastResponse;
	public final boolean cdrForTPSError;
	public final boolean cdrForAuthentication;
	
	public final String fieldSeparator;
	public final String lineSeparator;
	
	public final int batchSize;
	
	private final int[][][] cdrStreamIndexLookupTable = new int[2][2][2];

	public CDRConfiguration(Properties properties) {
		String numberOfCdrProcessingThreads = Utility.ptrim(properties, "NUMBER_OF_CDR_PROCESSING_THREADS", "0");
	
		String cdrWriteInFileEnable = Utility.ptrim(properties, "CDR_WRITE_IN_FILE_ENABLE", "1");
		String immediatelySaveSuccessCdr = Utility.ptrim(properties, "IMMEDIATELY_SAVE_SUCCESS_CDR", "1");
		
		String cdrDirectory = Utility.ptrim(properties, "CDR_DIRECTORY");
		String cdrFileCreationInterval = Utility.ptrim(properties, "CDR_FILE_CREATION_INTERVAL");
		String cdrDuplicationCheck = Utility.ptrim(properties, "CDR_DUPLICATION_CHECK_ENABLE");
		String cdrDuplicationLastResponse = Utility.ptrim(properties, "CDR_DUPLICATION_LAST_RESPONSE");
		String cdrForTPSError = Utility.ptrim(properties, "INSERT_INTO_CRD_FOR_TPS");
		String cdrForAuthentication = Utility.ptrim(properties, "INSERT_INTO_CRD_FOR_ERROR_IN_APPID_OR_PASSWORD");
		String fieldSeparator = properties.getProperty("CDR_FIELD_SEPARATOR", "|"); // Should not be trimmed. Have to use raw value.
		String lineSeparator = properties.getProperty("CDR_LINE_SEPARATOR", "\r\n"); // Should not be trimmed. Have to use raw value.
		String batchSize = Utility.ptrim(properties, "CDR_BATCH_SIZE", "10");
		
		this.numberOfCdrProcessingThreads = Integer.parseInt(numberOfCdrProcessingThreads);
		
		this.cdrWriteInFileEnable = cdrWriteInFileEnable.equals("1");
		this.immediatelySaveSuccessCdr = immediatelySaveSuccessCdr.equals("1");
		
		this.cdrDirectory = cdrDirectory;
		this.cdrFileCreationInterval = Integer.valueOf(cdrFileCreationInterval);
		this.cdrDuplicationCheck = cdrDuplicationCheck.equals("1");
		this.cdrDuplicationLastResponse = cdrDuplicationLastResponse.equals("1");
		this.cdrForTPSError = cdrForTPSError.equals("1");
		this.cdrForAuthentication = cdrForAuthentication.equals("1");
		
		this.fieldSeparator = fieldSeparator;
		this.lineSeparator = lineSeparator;
		
		this.batchSize = Integer.valueOf(batchSize);
		
		cdrStreamIndexLookupTable[0][0][0] = cdrStreamIndexLookupTable[0][1][0] = Constant.CDR_STREAM_DB;
		cdrStreamIndexLookupTable[0][0][1] = cdrStreamIndexLookupTable[0][1][1] = Constant.CDR_STREAM_FILE;
		cdrStreamIndexLookupTable[1][0][0] = cdrStreamIndexLookupTable[1][0][1] = Constant.CDR_STREAM_IN_MEMORY_DB;
		cdrStreamIndexLookupTable[1][1][0] = Constant.CDR_STREAM_DB;
	    cdrStreamIndexLookupTable[1][1][1] = Constant.CDR_STREAM_FILE;
	}
	
	public int getCdrStreamIndex(boolean isCdrSuccess) {
		int indexForThreads = this.numberOfCdrProcessingThreads > 0 ? 1 : 0;
		int indexForSuccessCdr = (this.immediatelySaveSuccessCdr && isCdrSuccess) ? 1 : 0;
		int indexForCdrWriteTo = this.cdrWriteInFileEnable ? 1 : 0;
		
		return this.cdrStreamIndexLookupTable[indexForThreads][indexForSuccessCdr][indexForCdrWriteTo]; //111
	}
	
	@Override
	public String toString() {
		List<Integer> lp = new ArrayList<>();
		for (Byte b : this.lineSeparator.getBytes()) {
			lp.add((int) b);
		}
		
		return new StringJoiner("\r\n")
				       .add("\r\n\r\n[CDR Configuration]")
				       .add("NUMBER_OF_CDR_PROCESSING_THREADS = " + this.numberOfCdrProcessingThreads)
				       .add("CDR_WRITE_IN_FILE_ENABLE = " + Utility.getEncodedValueForBooleanProperties(this.cdrWriteInFileEnable))
				       .add("IMMEDIATELY_SAVE_SUCCESS_CDR = " + Utility.getEncodedValueForBooleanProperties(this.immediatelySaveSuccessCdr))
				       .add("CDR_DIRECTORY = " + this.cdrDirectory)
				       .add("CDR_FILE_CREATION_INTERVAL = " + this.cdrFileCreationInterval)
				       .add("CDR_DUPLICATION_CHECK_ENABLE = " + Utility.getEncodedValueForBooleanProperties(this.cdrDuplicationCheck))
				       .add("CDR_DUPLICATION_LAST_RESPONSE = " + Utility.getEncodedValueForBooleanProperties(this.cdrDuplicationLastResponse))
				       .add("INSERT_INTO_CRD_FOR_TPS = " + Utility.getEncodedValueForBooleanProperties(this.cdrForTPSError))
				       .add("INSERT_INTO_CRD_FOR_ERROR_IN_APPID_OR_PASSWORD = " + Utility.getEncodedValueForBooleanProperties(this.cdrForAuthentication))
				       .add("CDR_FIELD_SEPARATOR = " + this.fieldSeparator)
				       .add("CDR_LINE_SEPARATOR = " + lp)
				       .add("CDR_BATCH_SIZE = " + this.batchSize)
				       .toString();
				 
	}
} 
