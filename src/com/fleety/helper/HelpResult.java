package com.fleety.helper;

import com.fleety.base.InfoContainer;

public class HelpResult extends InfoContainer {	
	public HelpResult(){
		
	}
	public HelpResult(boolean isSuccess,String reason){
		if(!isSuccess){
			this.setInfo("isSuccess", Boolean.FALSE);
			this.setInfo("reason", reason);
		}
	}
	public boolean isSuccess(){
		Boolean isSuccess = this.getBoolean("isSuccess");
		
		if(isSuccess != null && !isSuccess.booleanValue()){
			return false;
		}
		return true;
	}
	public String getReason(){
		return this.getString("reason");
	}
}
