/**
 * ¾øÃÜ Created on 2008-1-3 by edmund
 */
package com.fleety.server;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import server.var.VarManageServer;

public abstract class BasicServer implements IServer{
	private HashMap paraMapping = null;
	protected boolean isRunning = false;
	
	public void addPara(Object key,Object value){
		if(key == null || value == null){
			return ;
		}
		
		if(VarManageServer.getSingleInstance().isRunning()){
			value = VarManageServer.getSingleInstance().updateValueByVar(value);
		}
		
		if(this.paraMapping == null){
			this.paraMapping = new HashMap();
		}
		Object existValue = this.paraMapping.get(key);
		if(existValue == null){
			this.paraMapping.put(key, value);
		}else if(existValue instanceof List){
			((List)existValue).add(value);
		}else{
			List tempList = new LinkedList();
			tempList.add(existValue);
			tempList.add(value);
			this.paraMapping.put(key, tempList);
		}
	}
	
	public void removePara(Object key){
		this.paraMapping.remove(key);		
	}
	
	public boolean isRunning(){
		return this.isRunning;
	}
	
	public Object getPara(Object key){
		if(this.paraMapping == null){
			return null;
		}
		return this.paraMapping.get(key);
	}
	
	public Boolean getBooleanPara(Object key){
		if(this.paraMapping == null){
			return null;
		}
		Object obj = this.paraMapping.get(key);
		
		if(obj == null){
			return null;
		}else if(obj instanceof Boolean){
			return (Boolean)obj;
		}
		
		return new Boolean(obj.toString().equalsIgnoreCase("true"));
	}
	
	public String getStringPara(Object key){
		if(this.paraMapping == null){
			return null;
		}
		Object obj = this.paraMapping.get(key);
		
		return obj==null?null:obj.toString();
	}
	
	public Integer getIntegerPara(Object key){
		if(this.paraMapping == null){
			return null;
		}
		Object obj = this.paraMapping.get(key);
		
		if (obj == null){
			return null;
		} else if (obj instanceof Integer){
			return (Integer) obj;
		} else{
			try{
				String str = obj.toString().trim();
				if(str.length() == 0){
					return null;
				}
				if(str.startsWith("0x")){
					return new Integer(Integer.parseInt(str.substring(2),16));
				}else{
					return new Integer(str);
				}
			} catch (Exception e){
				e.printStackTrace();
				return null;
			}
		}
	}
	
	private String serverName = null;
	public void setServerName(String serverName){
		this.serverName = serverName;
	}
	
	public String getServerName(){
		return this.serverName;
	}
	
	public void stopServer(){
		this.paraMapping = null;
		this.isRunning = false;
	}
	
	public void copyPara(BasicServer server){
		this.paraMapping.putAll(server.paraMapping);
	}
}
