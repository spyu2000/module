package com.fleety.util.pool.timer;

import java.util.TimerTask;

public class NestTimerTask extends TimerTask {
	private TimerPool timerPool = null;
	private FleetyTimerTask task = null;
	public NestTimerTask(TimerPool timerPool,FleetyTimerTask task){
		this.timerPool = timerPool;
		this.task = task;
		this.task.setAssociateTimerTask(this);
	}
	
	public void run(){
		this.timerPool.addExecTask(this.task);
	}
}
