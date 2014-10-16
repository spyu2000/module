/**
 * ¾øÃÜ Created on 2008-12-5 by edmund
 */
package com.fleety.base.event;

public class Event
{
	private int eventType = 0;
	
	private Object eventPara = null;
	private Object source = null;
	
	public Event(int eventType,Object eventPara,Object source){
		this.eventType = eventType;
		this.eventPara = eventPara;
		
		this.source = source;
	}

	public Object getEventPara()
	{
		return eventPara;
	}

	public int getEventType()
	{
		return eventType;
	}
	
	public Object getSource(){
		return this.source;
	}
	
	public String toString(){
		return "eventTYpe="+this.eventType+";para="+this.eventPara+";source="+this.source;
	}
}
