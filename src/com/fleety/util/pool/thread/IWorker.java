/**
 * ¾øÃÜ Created on 2008-2-1 by edmund
 */
package com.fleety.util.pool.thread;

import java.util.List;

import com.fleety.base.FleetyThread;

public abstract class IWorker extends FleetyThread{
	protected ThreadPool pool = null;
	public void setPool(ThreadPool pool){
		this.pool = pool;
	}
	
	protected List taskList = null;
	public void setTaskList(List taskList){
		this.taskList = taskList;
	}
	public List getTaskList(){
		return this.taskList;
	}
	
	protected ITask task = null;
	public ITask getCurTask(){
		return this.task;
	}
	
	public abstract void stopWork(boolean isImmediately);
}
