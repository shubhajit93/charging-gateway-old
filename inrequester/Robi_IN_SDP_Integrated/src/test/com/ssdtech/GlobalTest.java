package com.ssdtech;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.junit.Ignore;
import org.junit.Test;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.runner.RunWith;

import com.ssdtech.robi.Configuration;
import com.ssdtech.robi.Global;
import com.ssdtech.robi.ResultCode;
import com.ssdtech.robi.model.ServiceInfo;

@RunWith(JUnitParamsRunner.class)
public class GlobalTest {
    private static final String CONFIG_FILE_PATH = "./ConfigurationFiles/in/config.cfg";
    private static final String RATECONFIG_FILE_PATH = "./ConfigurationFiles/in/rateConfig.txt";
	
	/***
	 * Application wide configuration properties load from <project-root>/ConfigurationFiles/config.cfg
	 */
	@Test
	public void checkIfConfigurationFilesCorrectlyLocated() {
		File file = new File("./ConfigurationFiles/in/config.cfg");
		assertTrue("config.cfg file is not located under <project-root>/ConfigurationFiles/in/ directory", file.exists());
	}
	
	@Test
	public void checkIfConfigFileLocationSetInMemberVariable() {
		assertEquals(
				"config.cfg file path is not set in Configuration class",
				CONFIG_FILE_PATH, Configuration.CONFIG_FILE_PATH);
	}
	
	@Test
	public void checkIfRateConfigurationFilesCorrectlyLocated() {
		File file = new File("./ConfigurationFiles/in/rateConfig.txt");
		assertTrue("rateConfig.txt file is not located under <project-root>/ConfigurationFiles/in/ directory", file.exists());
	}
	
	@Test
	public void checkIfRateConfigFileLocationSetInMemberVariable() {
		assertEquals(
				"config.cfg file path is not set in Configuration class",
				RATECONFIG_FILE_PATH, Global.RATECONFIG_FILE_PATH);
	}
	
	@Test
	public void checkIfPropertiesFromConfigFileCorrectlyLoaded() {

	}
	
	@Test
	public void checkIfPropertiesValueLoadCorrectly() throws SecurityException, NoSuchFieldException, Exception {
		String config_content = "LOGENABLE=0\r\n" +
                                 "LISTEN_PORT = 10888\r\n" +
                                 "SOAPENV_URL = http://schemas.xmlsoap.org/soap/envelope/\r\n" + 
                                 "V2_URL = http://www.huawei.com.cn/schema/common/v2_1\r\n" +
                                 "LOC_URL = http://www.csapi.org/schema/parlayx/payment/amount_charging/v2_1/local\r\n" +
                                 "REQUEST_URL=http://IP:Port/AmountChargingService/services/AmountCharging\r\n" +
                                 "PREFIX=111\r\n" + 
                                 "DESCRIPTION=charging information\r\n" +
                                 "CURRENCY=BDT\r\n" +
                                 "MSISDN_LENTH=4\r\n";
		Properties properties = buildProperties(config_content, "\r\n");
		
		Configuration conf = new Configuration();
		setFinalStatic(conf.getClass().getDeclaredField("prop"), properties, conf);
		
		assertEquals("LOGENABLE is not set properly", "0", conf.getProperty("LOGENABLE"));
		assertEquals("LISTEN_PORT is not set properly", "10888", conf.getProperty("LISTEN_PORT"));
		assertEquals("SOAPENV_URL is not set properly", "http://schemas.xmlsoap.org/soap/envelope/", conf.getProperty("SOAPENV_URL"));
		assertEquals("V2_URL is not set properly", "http://www.huawei.com.cn/schema/common/v2_1", conf.getProperty("V2_URL"));
		assertEquals("LOC_URL is not set properly", "http://www.csapi.org/schema/parlayx/payment/amount_charging/v2_1/local", conf.getProperty("LOC_URL"));
		assertEquals("REQUEST_URL is not set properly", "http://IP:Port/AmountChargingService/services/AmountCharging", conf.getProperty("REQUEST_URL"));
		assertEquals("PREFIX is not set properly", "111", conf.getProperty("PREFIX"));
		assertEquals("DESCRIPTION is not set properly", "charging information", conf.getProperty("DESCRIPTION"));
		assertEquals("MSISDN_LENGTH is not set properly", "4", conf.getProperty("MSISDN_LENTH"));
	}
	
	@Test
	public void checkThatDefaultValueGivenIfPropertyDoesNotDeclare() throws SecurityException, NoSuchFieldException, Exception {
		String config_content = "propertyName=propertyValue\r\nprop1= \r\n";
        Properties properties = buildProperties(config_content, "\r\n");

        Configuration conf = new Configuration();
        setFinalStatic(conf.getClass().getDeclaredField("prop"), properties, conf);

        assertEquals("Set value does given.", "propertyValue", conf.getProperty("propertyName", "defaultPropertyValue"));
        assertEquals("Default property value does not given if not found.", "defaultPropertyValue", conf.getProperty("diffPropertyName", "defaultPropertyValue"));
        assertEquals("Default property value does not set if only whitespaces are available", "defaultPropertyValue", conf.getProperty("prop1", "defaultPropertyValue"));
	}
	
	@Test
	public void checkIfSpaceIsTrimmedWhenLoadFromConfigFile() throws SecurityException, NoSuchFieldException, Exception {
		String config_content = "LOGENABLE = 0 \r\n";

		Properties properties = buildProperties(config_content, "\r\n");
		
		Configuration conf = new Configuration();
		setFinalStatic(conf.getClass().getDeclaredField("prop"), properties, conf);
		
		assertEquals("LOGENABLE is not set properly", "0", conf.getProperty("LOGENABLE"));
	}
	
	public static Object[] geInvalidConfigContent() {
		return new Object[] {
            "LOGENABLE0=0",
            "LOGENABLE1=1"
		};
	}
	
	@Test
	@Parameters(method = "geInvalidConfigContent")
	public void checkThatPropertyValueIsNullIfNotFound(String config_content) throws SecurityException, NoSuchFieldException, Exception {
        Properties properties = buildProperties(config_content, "\r\n");
		
		Configuration conf = new Configuration();
		setFinalStatic(conf.getClass().getDeclaredField("prop"), properties, conf);
		
		assertEquals("LOGENABLE is not set properly", null, conf.getProperty("LOGENABLE"));
	}
	
	public static Object[] getMD5parameters() {
		return new Object[] {
			new Object[] {"22202Robi123420170720114000", "665eeba3d8c42fd970e185e02873b275"},
			new Object[] {"22203Robi234520170720114000", "376a5fe42a89c559571d1939ba8c996a"}
		};
	}
	
	@Test
	@Parameters(method = "getMD5parameters")
	public void checkIfMD5GeneratesCorrectly(String password, String expectedPasswordHash) {
		assertEquals("Generated MD5 hash does not match", expectedPasswordHash, Global.getMD5(password));
	}
	
	@Test
	public void checkIfDefaultPaymentPropertiesSetProperly() throws SecurityException, NoSuchFieldException, Exception {
		String config_content = "PAYMENT_URL=\r\n"
			                  + "DESCRIPTION=\r\n"
			                  + "CURRENCY=\r\n";

		Properties properties = buildProperties(config_content, "\r\n");
		
		Configuration conf = new Configuration();
		setFinalStatic(conf.getClass().getDeclaredField("prop"), properties, conf);
		
		Global.loadPaymentInformation(conf);
		
		assertEquals("Payment URL is not set correctly", "", Global.PAYMENT_SPECIFIC_DATA.get("url"));
		assertEquals("Payment description is not set correctly", "charging information", Global.PAYMENT_SPECIFIC_DATA.get("description"));
		assertEquals("Payment currency is not set correctly", "BDT", Global.PAYMENT_SPECIFIC_DATA.get("currency"));
	}
	
	@Test
	public void checkIfGenerationInformationLoadedCorrectly() throws SecurityException, NoSuchFieldException, Exception {
		Global.LOGENABLE = -1;
		Global.LISTEN_PORT = 0;
		Global.SOAPENV_URL = null;
		Global.V2_URL = null;
		Global.LOC_URL = null;
		Global.PREFIX = null;
		Global.MSISDN_LENTH = 0;
		
		String config_content = "LOGENABLE=0\r\n" 
				              + "LISTEN_PORT = 10888\r\n"
				              + "SOAPENV_URL = http://schemas.xmlsoap.org/soap/envelope/\r\n"
				              + "V2_URL = http://www.huawei.com.cn/schema/common/v2_1\r\n"
				              + "LOC_URL = http://www.csapi.org/schema/parlayx/payment/amount_charging/v2_1/local\r\n"
				              + "LOC_URL1 = http://www.csapi.org/schema/parlayx/subscribe/manage/v1_0/local\r\n"
				              + "PREFIX = 18\r\n"
				              + "MSISDN_LENTH = 10";

		Properties properties = buildProperties(config_content, "\r\n");
		
		Configuration conf = new Configuration();
		setFinalStatic(conf.getClass().getDeclaredField("prop"), properties, conf);
		
		Global.loadGeneralInformation(conf);
		
		assertEquals("LOGENABLE is not set properly.", 0, Global.LOGENABLE);
		assertEquals("LISTEN_PORT is not set properly.", 10888, Global.LISTEN_PORT);
		assertEquals("SOAPENV URL is not set properly", "http://schemas.xmlsoap.org/soap/envelope/", Global.SOAPENV_URL);
		assertEquals("V2_URL is not set properly.", "http://www.huawei.com.cn/schema/common/v2_1", Global.V2_URL);
		assertEquals("LOC_URL is not set properly.", "http://www.csapi.org/schema/parlayx/payment/amount_charging/v2_1/local", Global.LOC_URL);
		assertEquals("LOC_URL1 is not set properly.", "http://www.csapi.org/schema/parlayx/subscribe/manage/v1_0/local", Global.LOC_URL1);
		assertEquals("PREFIX is not set properly", "18", Global.PREFIX);
		assertEquals("MSISDN_LENGTH is not set properly.", 10, Global.MSISDN_LENTH);
	}
	
	@Test
	public void checkIfServiceConfigLoadCorrectly() throws IOException {
		String fileContent = "ServiceName>>ServiceID>>SPID>>Password>>ProductID\r\n"
						   + "Rateid_Islamic_21279>>0303233019>>200033>>Robi1234>>0300398904\r\n";
		InputStream stream = new ByteArrayInputStream(fileContent.getBytes());
		BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
		
		Global.loadServiceConfiguration(reader);
		
		ServiceInfo si1 = Global.rateidServiceInfoMapping.get("Rateid_Islamic_21279");
		assertEquals("ServiceID is not set properly.", "0303233019", si1.getServiceCode());
		assertEquals("Sp ID is not set properly", "200033", si1.getSPID());
		assertEquals("Password is not set properly", "Robi1234", si1.getSPPassword());
		assertEquals("Product ID is not set properly.", "0300398904", si1.getProductID());
	}
	
	@Test
	public void checkIfServiceConfigLoadCorrectlyForOptionalParameters() throws IOException {
		String fileContent = "ServiceName>>ServiceID>>SPID>>Password>>ProductID\r\n"
						   + "Rateid_Islamic_21279>>0303233019>>200033>>>>0300398904\r\n";
		InputStream stream = new ByteArrayInputStream(fileContent.getBytes());
		BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
		
		Global.loadServiceConfiguration(reader);
		
		ServiceInfo si1 = Global.rateidServiceInfoMapping.get("Rateid_Islamic_21279");
		assertEquals("ServiceID is not set properly.", "0303233019", si1.getServiceCode());
		assertEquals("Sp ID is not set properly", "200033", si1.getSPID());
		assertEquals("Password is not set properly", "", si1.getSPPassword());
		assertEquals("Product ID is not set properly.", "0300398904", si1.getProductID());
	}
	
	@Test
	public void checkIfNoServiceConfigurationFoundForZeroContent() throws IOException {
		String fileContent = "ServiceName>>ServiceID>>SPID>>Password>>ProductID\r\n\r\n";
		InputStream stream = new ByteArrayInputStream(fileContent.getBytes());
		BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
		
		Global.loadServiceConfiguration(reader);
		
		ServiceInfo si1 = Global.rateidServiceInfoMapping.get("Rateid_Islamic_21279");
		assertNull("ServiceInfo found for rateid : Rateid_Islamic_21279", si1);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void checkIfLoadingOfServiceInfoBehavingCorrectlyForAllOptionParameters() throws IOException {
		String fileContent = "ServiceName>>ServiceID>>SPID>>Password>>ProductID\r\n"
				           + ">>>>>>>>";
		InputStream stream = new ByteArrayInputStream(fileContent.getBytes());
		BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
		
		Global.loadServiceConfiguration(reader);
	}
	
	@Test
	public void checkRenewalPropertiesCanBeSetToDefaultIfNotProvided() throws SecurityException, NoSuchFieldException, Exception {
        Properties properties = buildProperties("", "\r\n");
		
		Configuration conf = new Configuration();
		setFinalStatic(conf.getClass().getDeclaredField("prop"), properties, conf);
		
		Global.loadRenewalInformation(conf);
		
		assertTrue("Key: url is missing.", Global.RENEWAL_SPECIFIC_DATA.containsKey("url"));
		assertTrue("Key: actionType is missing.", Global.RENEWAL_SPECIFIC_DATA.containsKey("actionType"));
		
		assertNotNull("Success code mapping object is not initialized", Global.RENEWAL_SUCCESS_CODES_MAPPING);
		assertNotNull("Failure code mapping object is not initialized", Global.RENEWAL_FAILURE_CODES_MAPPING);
		
		assertNotNull("CGW success code is not initialized", Global.RENEWAL_CGW_SUCCESS_CODE);
		assertNotNull("CGW failure code is not initialized", Global.RENEWAL_CGW_FAILURE_CODE);
		
		assertEquals("Default Renewal result code is not set", ResultCode.RENEWAL_DEFAULT_RESULT_CODE, Global.RENEWAL_DEFAULT_RESULT_CODE);
		
		Set<String> defaultSuccessCodeMapping = new HashSet<String>();
		defaultSuccessCodeMapping.add("1");
		assertTrue("Default Success code mapping is not set", 
				(Global.RENEWAL_SUCCESS_CODES_MAPPING.size() == defaultSuccessCodeMapping.size()) 
				&& (Global.RENEWAL_SUCCESS_CODES_MAPPING.containsAll(defaultSuccessCodeMapping)));
		
		assertTrue("Default Failure code mapping should be empty", Global.RENEWAL_FAILURE_CODES_MAPPING.size() == 0);
		assertEquals(ResultCode.SUCCESS_CODE, Global.RENEWAL_CGW_SUCCESS_CODE);
		assertEquals(ResultCode.RENEWAL_FAILURE_CODE, Global.RENEWAL_CGW_FAILURE_CODE);
	}
	
	@Test
	public void checkIfRenewalPropertiesCanBeSetProperlyWithNoAllowedSuccessCode() throws SecurityException, NoSuchFieldException, Exception {
		String content = "RENEWAL_URL = http://10.16.100.194:8310/SubScriptionService/services/SubScription\r\n" +
                         "RENEWAL_ACTION_TYPE = 1\r\n" +
                         "RENEWAL_SUCCESS_CODE_MAPPING = -> 2001\r\n" +
                         "RENEWAL_FAILURE_CODE_MAPPING = 0 -> 9001\r\n" +
                         "RENEWAL_DEFAULT_RESULT_CODE = 9001";
        Properties properties = buildProperties(content, "\r\n");
		
		Configuration conf = new Configuration();
		setFinalStatic(conf.getClass().getDeclaredField("prop"), properties, conf);
		
		Global.loadRenewalInformation(conf);
		
		assertTrue("Key: url is missing.", Global.RENEWAL_SPECIFIC_DATA.containsKey("url"));
		assertTrue("Key: actionType is missing.", Global.RENEWAL_SPECIFIC_DATA.containsKey("actionType"));
		
		assertNotNull("Success code mapping object is not initialized", Global.RENEWAL_SUCCESS_CODES_MAPPING);
		assertNotNull("Failure code mapping object is not initialized", Global.RENEWAL_FAILURE_CODES_MAPPING);
		
		assertNotNull("CGW success code is not initialized", Global.RENEWAL_CGW_SUCCESS_CODE);
		assertNotNull("CGW failure code is not initialized", Global.RENEWAL_CGW_FAILURE_CODE);
		
		assertEquals("Renewal result code is not set", "9001", Global.RENEWAL_DEFAULT_RESULT_CODE);
		
		Set<String> defaultSuccessCodeMapping = new HashSet<String>();
		defaultSuccessCodeMapping.add("1");
		assertTrue("Default Success code mapping is not set", 
				(Global.RENEWAL_SUCCESS_CODES_MAPPING.size() == defaultSuccessCodeMapping.size()) 
				&& (Global.RENEWAL_SUCCESS_CODES_MAPPING.containsAll(defaultSuccessCodeMapping)));
		
		Set<String> failureCodeMapping = new HashSet<String>();
		failureCodeMapping.add("0");
		assertTrue("Failure code mapping should have size 1", Global.RENEWAL_FAILURE_CODES_MAPPING.size() == 1);
		assertTrue("Failure code mapping is not set correctly", Global.RENEWAL_FAILURE_CODES_MAPPING.containsAll(failureCodeMapping));
		
		assertEquals("2001", Global.RENEWAL_CGW_SUCCESS_CODE);
		assertEquals("9001", Global.RENEWAL_CGW_FAILURE_CODE);
	}
	
	@Test
	public void checkIfRenewalPropertiesCanBeSetProperlyWithAllowedSuccessCode() throws SecurityException, NoSuchFieldException, Exception {
		String content = "RENEWAL_URL = http://10.16.100.194:8310/SubScriptionService/services/SubScription\r\n" +
                         "RENEWAL_ACTION_TYPE = 1\r\n" +
                         "RENEWAL_SUCCESS_CODE_MAPPING = 1, 2 -> 2001\r\n" +
                         "RENEWAL_FAILURE_CODE_MAPPING = 0 -> 9001\r\n" +
                         "RENEWAL_DEFAULT_RESULT_CODE = 9001";
        Properties properties = buildProperties(content, "\r\n");
		
		Configuration conf = new Configuration();
		setFinalStatic(conf.getClass().getDeclaredField("prop"), properties, conf);
		
		Global.loadRenewalInformation(conf);
		
		assertTrue("Key: url is missing.", Global.RENEWAL_SPECIFIC_DATA.containsKey("url"));
		assertTrue("Key: actionType is missing.", Global.RENEWAL_SPECIFIC_DATA.containsKey("actionType"));
		
		assertNotNull("Success code mapping object is not initialized", Global.RENEWAL_SUCCESS_CODES_MAPPING);
		assertNotNull("Failure code mapping object is not initialized", Global.RENEWAL_FAILURE_CODES_MAPPING);
		
		assertNotNull("CGW success code is not initialized", Global.RENEWAL_CGW_SUCCESS_CODE);
		assertNotNull("CGW failure code is not initialized", Global.RENEWAL_CGW_FAILURE_CODE);
		
		assertEquals("Renewal result code is not set", "9001", Global.RENEWAL_DEFAULT_RESULT_CODE);
		
		Set<String> successCodeMapping = new HashSet<String>();
		successCodeMapping.add("1");
		successCodeMapping.add("2");
		assertTrue("Success code mapping is not set", 
				(Global.RENEWAL_SUCCESS_CODES_MAPPING.size() == successCodeMapping.size()) 
				&& (Global.RENEWAL_SUCCESS_CODES_MAPPING.containsAll(successCodeMapping)));
		
		Set<String> failureCodeMapping = new HashSet<String>();
		failureCodeMapping.add("0");
		assertTrue("Failure code mapping should have size 1", Global.RENEWAL_FAILURE_CODES_MAPPING.size() == 1);
		assertTrue("Failure code mapping is not set correctly", Global.RENEWAL_FAILURE_CODES_MAPPING.containsAll(failureCodeMapping));
		
		assertEquals("2001", Global.RENEWAL_CGW_SUCCESS_CODE);
		assertEquals("9001", Global.RENEWAL_CGW_FAILURE_CODE);
	}
	
	private static void setFinalStatic(Field field, Object newValue, Object instance) throws Exception {
        field.setAccessible(true);

        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);

        field.set(instance, newValue);
    }
	
	public static Properties buildProperties(String properties, String propertySeparator) throws IOException {
		if (properties == null) {
			throw new NullPointerException("Properties string can not be null");
		}
		
		propertySeparator = (propertySeparator == null) ? "\n" : propertySeparator;
		
		Properties prop = new Properties();
		prop.load(new StringReader(properties.replaceAll(propertySeparator, "\n")));
		return prop;
	}
}
