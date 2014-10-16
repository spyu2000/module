package com.fleety.util.pool.timer;

import java.util.Date;

import server.notify.INotifyServer;
import server.threadgroup.PoolInfo;
import server.threadgroup.ThreadPoolGroupServer;

import com.fleety.base.CostStatLog;
import com.fleety.base.FleetyThread;
import com.fleety.util.pool.IPool;
import com.fleety.util.pool.thread.BasicTask;
import com.fleety.util.pool.thread.ThreadPool;

public class TimerPool implements IPool {
	private String poolName = null;
	private String threadPoolName = null;
	private ThreadPool pool = null;
	
	public TimerPool(String name,int num,boolean isDaemon) throws Exception{
		if(num <= 0){
			throw new Exception("Error Parameter:"+num);
		}
		this.poolName = name;
		this.threadPoolName = name+"-timerpool";

		PoolInfo pInfo = new PoolInfo();
		pInfo.isDaemo = isDaemon;
		pInfo.taskCapacity = num * 10;
		pInfo.poolType = ThreadPool.SINGLE_TASK_LIST_POOL;
		pInfo.workersNumber = num;
		this.pool = ThreadPoolGroupServer.getSingleInstance().createThreadPool(this.threadPoolName, pInfo);
	}
	
	public String toString(){
		return this.poolName==null?"EmptyName":this.poolName;
	}
	
	public void schedule(FleetyTimerTask task, long delay){
		SharedTimer.getSingleInstance().schedule(new NestTimerTask(this,task), delay);
	}
	public void schedule(FleetyTimerTask task, Date time){
		SharedTimer.getSingleInstance().schedule(new NestTimerTask(this,task), time);
	}
	public void schedule(FleetyTimerTask task, long delay, long period){
		SharedTimer.getSingleInstance().schedule(new NestTimerTask(this,task), delay, period);
	}
	public void schedule(FleetyTimerTask task, Date firstTime, long period){
		SharedTimer.getSingleInstance().schedule(new NestTimerTask(this,task), firstTime, period);
	}
	public void scheduleAtFixedRate(FleetyTimerTask task, long delay, long period){
		SharedTimer.getSingleInstance().scheduleAtFixedRate(new NestTimerTask(this,task), delay, period);
	}
	public void scheduleAtFixedRate(FleetyTimerTask task, Date firstTime, long period){
		SharedTimer.getSingleInstance().scheduleAtFixedRate(new NestTimerTask(this,task), firstTime, period);
	}
	
	public void cancel(){
		ThreadPoolGroupServer.getSingleInstance().removeThreadPool(this.threadPoolName);
		this.pool = null;
	}
	
	protected void addExecTask(FleetyTimerTask task){
		if(this.pool == null){
			task.cancel();
			return ;
		}
		this.pool.addTask(new TimerTaskExecute(task));
	}
	
	private class TimerTaskExecute extends BasicTask{
		private FleetyTimerTask task = null;
		public TimerTaskExecute(FleetyTimerTask task){
			this.task = task;
		}
		
		public boolean execute(){
			long t = System.currentTimeMillis();
			try{
				this.task.run();
			}catch(Exception e){
				e.printStackTrace();
				
				INotifyServer.getSingleInstance().notifyInfo("Timer Task Exception", FleetyThread.getStrByException(e), INotifyServer.WARN_LEVEL);
			}
			t = System.currentTimeMillis()-t;
			CostStatLog.getSingleInstance().addCostInfo(TimerPool.this, this.task.getClass().getName(), t);
			
			return true;
		}
	}
}
