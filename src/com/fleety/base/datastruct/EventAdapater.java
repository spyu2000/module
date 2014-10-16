package com.fleety.base.datastruct;

public abstract class EventAdapater implements IEventListener {
	public boolean eventWillHappen(int eventType, Object[] paraInfoArr,Object source) {
		return true;
	}

}
