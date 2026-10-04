package com.ssdtech;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.UnknownServiceException;
import java.util.Date;

import com.ericsson.pps.diameter.hlapi.ChargingFactory;
import com.ericsson.pps.diameter.hlapi.TariffChangeQuota;
import com.ericsson.pps.diameter.hlapi.quota.MoneyUnit;
import com.ericsson.pps.diameter.hlapi.quota.QuotaException;
import com.ericsson.pps.diameter.hlapi.quota.RequestedQuota;
import com.ericsson.pps.diameter.hlapi.quota.UsedQuota;
import com.ericsson.pps.diameter.rfcapi.base.DiameterConfig;
import com.ericsson.pps.diameter.rfcapi.base.DiameterConfigException;
import com.ericsson.pps.diameter.rfcapi.base.avp.Avp;
import com.ericsson.pps.diameter.rfcapi.base.avp.SessionIdAvp;
import com.ericsson.pps.diameter.scapv2.SCAPRatingParameters;
import com.ericsson.pps.diameter.scapv2.SCAPSessionProperties;
import com.ericsson.pps.diameter.scapv2.SCAPStack;
import com.ericsson.pps.diameter.scapv2.avp.TimeZoneAvp;
public abstract class AbstractCharging  
{
	 protected static  SCAPStack stack = null;
	 static String tempArray[] = Global.myURI.split("\\|");
 	 static int NumberUrl=Integer.parseInt(tempArray[0]);
	 protected static  SCAPStack[]  stackseries=new SCAPStack[NumberUrl];
	 protected String MSISDN =null;
	 protected  int Amount = 0;
	 protected String SPID=null;
	 protected String CDRID=null;
	 
	 //protected StringBuffer sb = new StringBuffer("");
	 
	    
	    protected void init()
	    {
	        stack = new SCAPStack();
	      

	      //  stack.setOwnDiameterUri("aaa://sivr01.banglalink.gsm.com:3868;transport=tcp");
	        /*
	        stack.setOwnDiameterUri("aaa://localhost:3880;transport=tcp");
	        stack.setOriginRealm("realm3.org");
	        */
	        //stack.setOwnDiameterUri("aaa://sivr01.banglalink.gsm.com;transport=tcp");
	        stack.setOwnDiameterUri(Global.myURI);
	       // stack.setOriginRealm("ccn1.banglalink.gsm.com");
	        stack.setOriginRealm(Global.myRealm);
	        stack.setOwnProductId(Global.OwnProductName); 
	        
	       
	      
	        
	       

	        try
	        {
	            //stack.addStaticRoute("testccn01.banglalink.gsm.com", -1, -1, "aaa://10.50.11.81:3868;transport=tcp");
	        	stack.addStaticRoute(Global.myDestinationRealm, -1, -1, Global.myDestinationHost);
	        } catch (UnknownServiceException e)
	        {
	            e.printStackTrace();
	        } catch (URISyntaxException e)
	        {
	            e.printStackTrace();
	        }
	        catch (Exception e)
	        {
	            e.printStackTrace();
	        }

	        try
	        {    
		        Global.write(Global.dateFormatHourMinSec.format(new Date())+" Trying to init the diameter client........." ,"AbstractCharging init method");
	            boolean res=stack.start();
	            if(res)
	            {
	            	System.out.println("stack stated");
	            	Global.write(Global.dateFormatHourMinSec.format(new Date())+" diameter client started........." ,"AbstractCharging init method");
	            }
	            else
	            {
	            	System.out.println("stack started failed");
	            	Global.write(Global.dateFormatHourMinSec.format(new Date())+" diameter client failed to start........." ,"AbstractCharging init method");
	            }
	            
	        } 
	        catch(IOException e)
	        {
	            e.printStackTrace();
	        } catch (DiameterConfigException e)
	        {
	            e.printStackTrace();
	        }
	        catch (Exception e)
	        {
	            e.printStackTrace();
	        }
	    } 
	    protected boolean init(int number)
	    {   
	    	boolean res=false;
	    	try
	    	{
	    		stackseries[number] = new SCAPStack();
		    	String urlsplit[] =  Global.myURI.split("\\|");
		    	stackseries[number].setOwnDiameterUri(urlsplit[number+1]);
		    	stackseries[number].setOriginRealm(Global.myRealm);
		    	stackseries[number].setOwnProductId(Global.OwnProductName);
		    	String deshostsplit[] =null;
		    	String desrelm[] = null; 
		    	    try
			        {   
		    	    	deshostsplit =  Global.myDestinationHost.split("\\|");
		    	    	desrelm=Global.myDestinationRealm.split("\\|");
			        	stackseries[number].addStaticRoute(desrelm[number], -1, -1,deshostsplit[number]);
			        } catch (UnknownServiceException e)
			        {
			            e.printStackTrace();
			        } catch (URISyntaxException e)
			        {
			            e.printStackTrace();
			        }
			        catch (Exception e)
			        {
			            e.printStackTrace();
			        }

			        try
			        {    
				        Global.write(Global.dateFormatHourMinSec.format(new Date())+" Trying to init the diameter client-"+(number+1) ,"AbstractCharging init method");
				        Global.write(Global.dateFormatHourMinSec.format(new Date())+"Diam info OriginHost "+urlsplit[number+1]+" DestinationHost "+deshostsplit[number] ,"AbstractCharging init method");
				        Global.logForNumber(this.MSISDN,Global.dateFormatHourMinSec.format(new Date())+" Diam info OriginHost "+urlsplit[number+1]+" DestinationHost "+deshostsplit[number] ,"AbstractCharging init method");
			            res=stackseries[number].start();
			            
			            if(res)
			            {
			            	System.out.println("stack started");
			            	Global.write(Global.dateFormatHourMinSec.format(new Date())+" diameter client-"+(number+1)+" started" ,"AbstractCharging init method");
			            	Global.logForNumber(this.MSISDN,Global.dateFormatHourMinSec.format(new Date())+" diameter client-"+(number+1)+" started" ,"AbstractCharging init method");
			            }
			            else
			            {
			            	System.out.println("stack started failed");
			            	Global.write(Global.dateFormatHourMinSec.format(new Date())+" diameter client failed to start........." ,"AbstractCharging init method");
			            	Global.logForNumber(this.MSISDN,Global.dateFormatHourMinSec.format(new Date())+" diameter client failed to start........." ,"AbstractCharging init method");
			            }
			            
			        } 
			        catch(IOException e)
			        {
			            e.printStackTrace();
			            try 
			            {
							Global.write(Global.dateFormatHourMinSec.format(new Date())+Global.getStackTrace(e), "Exception in init method");
							Global.logForNumber(this.MSISDN,Global.dateFormatHourMinSec.format(new Date())+Global.getStackTrace(e), "Exception in init method");
						} catch (Exception e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
			        } catch (DiameterConfigException e)
			        {
			            e.printStackTrace();
			            try 
			            {
							Global.write(Global.dateFormatHourMinSec.format(new Date())+Global.getStackTrace(e), "Exception in init method");
							Global.logForNumber(this.MSISDN,Global.dateFormatHourMinSec.format(new Date())+Global.getStackTrace(e), "Exception in init method");
						} catch (Exception e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
			        }
			        catch (Exception e)
			        {
			            e.printStackTrace();
			            try 
			            {
							Global.write(Global.dateFormatHourMinSec.format(new Date())+Global.getStackTrace(e), "Exception in init method");
							Global.logForNumber(this.MSISDN,Global.dateFormatHourMinSec.format(new Date())+Global.getStackTrace(e), "Exception in init method");
						} catch (Exception e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
			        }
			        
			        return res;
	    	}
	    	catch(Exception exlast)
	    	{
	    		exlast.printStackTrace();
	    		return res;
	    	}
	    }
	    protected SCAPSessionProperties createSessionProperties(int number)
	    {
	    	
	    	String dserelm[]=Global.myDestinationRealm.split("\\|");
	    	String destinationRealm;
	    	if(dserelm.length>1)
	    	  destinationRealm =dserelm[number];
	    	else
	    	   destinationRealm=Global.myDestinationRealm;
	        String subcriptionId = MSISDN;
	        SCAPSessionProperties sessionProperties = new SCAPSessionProperties(destinationRealm, subcriptionId);
	       // Long arg0 = new Long(1);
	        Long arg0 = new Long(Global.ServiceIdentifier);
			sessionProperties.setServiceIdentifier(arg0);
			// Time unit: Milli-Seconds
			if (!Global.defaultTxTimeoutEnable) {
				sessionProperties.setTxTimerValue(Global.txTimeout);
			}
			//sessionProperties.setTxTimerValue(500);
	        return sessionProperties;
	    }
	    
	    protected RequestedQuota createRequestedQuota()
	    {
	        RequestedQuota requestedQuota = new RequestedQuota();
	        MoneyUnit requestedMoneyUnit= new MoneyUnit(Amount, -6, 50);
	        requestedQuota.setRequestedMoneyUnit(requestedMoneyUnit);
	        
	        return requestedQuota;
	    }
	    
	    protected UsedQuota createUsedQuota()
	    {
	        UsedQuota usedQuota = new UsedQuota();
	        MoneyUnit requestedMoneyUnit= new MoneyUnit(10, -6, 50);
	        usedQuota.setUsedMoneyUnit(requestedMoneyUnit);
	        return usedQuota;
	    }
	    
	    protected TariffChangeQuota createTariffChangedQuota()
	    {
	        TariffChangeQuota usedQuota = ChargingFactory.createTariffChangeUsedQuota();
	        MoneyUnit usedMoneyUnit= new MoneyUnit(105, -1, 978);
	        usedQuota.setQuotaBeforeTariffChange(usedMoneyUnit);
	        usedMoneyUnit= new MoneyUnit(105, -1, 978);
	        usedQuota.setQuotaAfterTariffChange(usedMoneyUnit);
	        usedMoneyUnit= new MoneyUnit(105, -1, 978);
	        try
	        {
	            usedQuota.setQuotaIndeterminateTariffChange(usedMoneyUnit);
	        } catch (QuotaException ignore)
	        {
	        }
	       return usedQuota;
	    }
	    
	    protected SCAPRatingParameters createSCAPRatingParameters()
	    {   
	    
	        SCAPRatingParameters ratingParameters = new SCAPRatingParameters();
	      //  TimeZoneAvp time=new TimeZoneAvp((byte)7, (byte)0, (byte)0);
	        byte[] timeData = new byte[2];
	        
	        timeData[0] = (byte)66;
	        timeData[1] = (byte)0;

	        Avp timeAvp = new Avp (23,10415);
	        timeAvp.setData(timeData); 
	        TimeZoneAvp tza = new TimeZoneAvp(timeAvp);
	        ratingParameters.setTimeZoneAvp(tza);
	        /*
	        Avp arg0=new Avp(263);
	        arg0.setData("6-705062405-05062405");
			ratingParameters.addAvp(arg0);
	        */
	        
	        
	      //  ratingParameters.setTimeZoneAvp(new TimeZoneAvp((byte)6, (byte)0, (byte)0));
	        return ratingParameters;
	    }
	    
	    protected void waitForAnswer()
	    {
	        synchronized (this)
	        {
	            try
	            {   
	            	 
	                wait();
	            } catch (InterruptedException e)
	            {
	                e.printStackTrace();
	            }
	        }
	    }

}
