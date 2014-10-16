/**
 * ¾øÃÜ Created on 2009-8-11 by edmund
 */
package server.distribute;

import java.io.Serializable;

public class ResultInfo implements Serializable{
	private boolean isSuccess = true;
	private Serializable resultObj = null;
	
	public ResultInfo(){
		
	}
	
	public boolean isSuccess(){
		return this.isSuccess;
	}
	
	public void setIsSuccess(boolean isSuccess){
		this.isSuccess = isSuccess;
	}
	
	public void setResultInfoObj(Serializable info){
		this.resultObj = info;
	}
	public Serializable getResultInfoObj(){
		return this.resultObj;
	}
}
