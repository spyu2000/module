package com.fleety.util.pool.timer;

import java.util.Timer;

class SharedTimer extends Timer{
	private static final SharedTimer singleInstance = new SharedTimer("timerpool_default_global_timer");
	public static SharedTimer getSingleInstance(){
		return singleInstance;
	}
	public SharedTimer(String name){
		super(name,true);
	}
}
