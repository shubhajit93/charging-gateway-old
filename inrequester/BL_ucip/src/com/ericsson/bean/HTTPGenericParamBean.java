package com.ericsson.bean;

public class HTTPGenericParamBean 
{
	
	private String param_name = "";
	private String param_value = "";
	
	public void setParamName(String param_name){
		this.param_name = param_name;
	}
	
	public void setParamValue(String param_value){
		this.param_value = param_value;
	}
	
	public String getParamName(){
		return param_name;
	}
	
	public String getParamValue(){
		return param_value;
	}

}