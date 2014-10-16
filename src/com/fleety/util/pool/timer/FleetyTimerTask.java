package com.fleety.util.pool.timer;

import java.util.TimerTask;

public abstract class FleetyTimerTask extends TimerTask {
	private TimerTask associateTask = null;
	public boolean cancel(){
		if(this.associateTask != null){
			this.associateTask.cancel();
		}
		return super.cancel();
	}
	
	public long scheduledExecutionTime(){
		if(this.associateTask != null){
			return this.associateTask.scheduledExecutionTime();
		}
		return super.scheduledExecutionTime();
	}

	final void setAssociateTimerTask(TimerTask task){
		this.associateTask = task;
	}
}
