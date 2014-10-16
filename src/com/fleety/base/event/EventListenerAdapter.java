/**
 * 绝密 Created on 2008-12-5 by edmund
 */
package com.fleety.base.event;

import java.util.HashMap;

public abstract class EventListenerAdapter  implements IEventListener{
	/**
	 * 排除和包含某些信息，以排除所包含的信息优先
	 * 如果包含标识中不含任何信息，意味着包含所有
	 * 如果排除标识中不含任何信息，意味着不排除任何
	 */
	public static final String EXCLUDES_FLAG = "excludes";
	public static final String INCLUDES_FLAG = "includes";
	
	private HashMap mapping = null;
	public void setPara(Object key, Object value){
		if(key == null || value == null){
			return ;
		}
		if(mapping == null){
			mapping = new HashMap();
		}
		
		mapping.put(key, value);
	}
	
	public Object getPara(Object key){
		if(mapping == null){
			return null;
		}
		return mapping.get(key);
	}
	private String includeStr = null;
	private String excludeStr = null;
	public boolean isInclude(String msg){
		if(excludeStr != null && excludeStr.indexOf(","+msg+",") >= 0){
			return false;
		}
		if(includeStr != null && includeStr.indexOf(","+msg+",") < 0){
			return false;
		}
		
		return true;
	}
	
	public void init(){
		//初始化被排除的信息
		this.excludeStr = (String)this.getPara(EXCLUDES_FLAG);
		if(this.excludeStr != null){
			if(this.excludeStr.trim().length() == 0){
				this.excludeStr = null;
			}else{
				this.excludeStr = "," + this.excludeStr + ",";
			}
		}
		
		//初始化被包含的信息
		this.includeStr = (String)this.getPara(INCLUDES_FLAG);
		if(this.includeStr != null){
			if(this.includeStr.trim().length() == 0){
				this.includeStr = null;
			}else{
				this.includeStr = "," + this.includeStr + ",";
			}
		}
	
		
	}
	
	public void destroy(){
		
	}
}
