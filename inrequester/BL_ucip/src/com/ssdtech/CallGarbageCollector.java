package com.ssdtech;

public class CallGarbageCollector  extends Thread{
  
	@Override
	public void run() 
	{
		System.out.println("Running Garbage Collector");
		System.gc();
	}
	
}
