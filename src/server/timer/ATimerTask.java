/**
 * 绝密 Created on 2007-11-14 by edmund
 */
package server.timer;

import java.util.HashMap;
import java.util.TimerTask;

import com.fleety.util.pool.timer.FleetyTimerTask;

public abstract class ATimerTask extends FleetyTimerTask{
	private String name = null;

	
	private HashMap mapping = new HashMap();
	
	public void addPara(Object key,Object value){
		this.mapping.put(key, value);
	}
	
	public Object getPara(Object key){
		return this.mapping.get(key);
	}		
	public String getName(){
		return name;
	}

	public void setName(String name){
		this.name = name;
	}
	
	/**
	 * 定时器加载的时候将调用该方法
	 */
	public void init() throws Exception{
		
	}
	
}
