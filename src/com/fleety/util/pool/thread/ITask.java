/**
 * ¾øÃÜ Created on 2008-2-1 by edmund
 */
package com.fleety.util.pool.thread;

public interface ITask{
	public Object getFlag();
	public String getDesc();
	
	public boolean execute() throws Exception;
}
