/**
 * ¾øÃÜ Created on 2008-12-5 by edmund
 */
package com.fleety.base.event;

public interface IEventListener{
	public static final Object _NAME_FLAG = new Object();
	public static final Object _ACTION_CONTAINER_FLAG = new Object();
	
	public boolean isInclude(String msg);
	public void eventHappen(Event e);
	
	public void setPara(Object key,Object value);
	
	public void init();
	public void destroy();
}
