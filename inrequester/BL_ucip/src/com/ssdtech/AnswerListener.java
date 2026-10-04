package com.ssdtech;

import com.ericsson.pps.diameter.rfcapi.base.message.DiameterMessage;
import com.ericsson.pps.diameter.scapv2.SCAPAnswerListener;
import com.ericsson.pps.diameter.scapv2.SCAPResult;

public class AnswerListener implements SCAPAnswerListener
{

    private AbstractCharging caller = null;
    private long Code;
    private boolean check=false;
    
    public long getCode()
    {
    	return this.Code;
    }
    public boolean getcheckcode()
    {
    	return this.check;
    }

    public AnswerListener(AbstractCharging caller)
    {   
    	//EventCharging.ResultCode=6001;
        this.caller = caller;
        this.Code=6001;
        this.check=false;
    }

    public void processAnswer(SCAPResult result)
    {   
       // result.get
        if(result==null)
        {
        //	EventCharging.ResultCode=6001;
        	this.Code=6004;
        }
        else
        {
        	//EventCharging.ResultCode=result.getResultCode();
        	
        //	if(EventCharging.ResultCode==3002)
        		//EventCharging.ResultCode=6002;
        	this.Code=result.getResultCode();
        	//System.out.println(this.Code);
        	if(this.Code==3002)
        		this.Code=6002;
        }
        this.check=true;
        synchronized (caller)
        {   
        	//caller.sb=new StringBuffer("");
        	//caller.sb.append(Code);
            caller.notify();
    
        }
    }

    public void staReceived(DiameterMessage sta)
    {
        System.out.println("staReceived");
        synchronized (caller)
        {
            caller.notify();
        }
    }

    public void txTimerExpired()
    {   
        //EventCharging.ResultCode=6003;
        this.Code=6003;
        this.check=true;
        synchronized (caller)
        {   
        //	caller.sb=new StringBuffer("");
        	//caller.sb.append(Code);
            caller.notify();
        }
    }

}