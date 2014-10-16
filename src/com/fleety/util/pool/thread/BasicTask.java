/**
 * ¾øÃÜ Created on 2010-9-16 by edmund
 */
package com.fleety.util.pool.thread;

public abstract class BasicTask implements ITask{
	public String getDesc(){
		return null;
	}

	public Object getFlag(){
		return null;
	}

	public int getTaskSize()
	{
		return 1;
	}
}
