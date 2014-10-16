/**
 * 绝密 Created on 2008-2-1 by edmund
 */
package com.fleety.util.pool.thread;

import server.mapping.ActionContainerServer;

import com.fleety.base.CostStatLog;

public class BasicWorker extends IWorker{
	private boolean isStop = false;
	private boolean isStopWithEmptyTask = false;
	
	public void stopWork(boolean isImmediately){
		if(isImmediately){
			this.isStop = true;
		}else{
			this.isStopWithEmptyTask = true;
		}
		if(this.taskList != null){
			synchronized(this.taskList){
				this.taskList.notifyAll();
			}
		}
	}
	
	public void run(){
		if(this.taskList == null){
			return ;
		}

		this.pool.threadStartWork(this);
		
		try{
			while(!isStop){
				if(this.taskList.isEmpty()){
					synchronized(this.taskList){
						if(isStop){
							break;
						}
						
						if(this.taskList.isEmpty()){
							//当任务执行完毕关闭标识为true时，退出线程。
							if(this.isStopWithEmptyTask){
								this.isStop = true;
								break;
							}
							this.pool.threadEndWork(this);
							try{
								this.taskList.wait();
							}catch(Exception e){
								e.printStackTrace();
							}finally{
								this.pool.threadStartWork(this);
							}
						}
					}
				}else{
					task = null;
					
					synchronized(this.taskList){
						if(!this.taskList.isEmpty()){
							task = (ITask)this.taskList.remove(0);
						}
					}


					if(task != null){
						long beginTime=System.currentTimeMillis();
						try{
							task.execute();
							pool.addFinishTaskNum();
						}catch(Exception e){
							e.printStackTrace();
						}
						if(task instanceof ActionContainerServer.WorkTask)
						{
							ActionContainerServer.WorkTask task2=(ActionContainerServer.WorkTask)task;
							CostStatLog.getSingleInstance().addCostInfo(this.pool,task.getClass().getName()+"_"+(task2.getMsgFlag()==null?"":task2.getMsgFlag()),System.currentTimeMillis()-beginTime);
						}
						else{
							CostStatLog.getSingleInstance().addCostInfo(this.pool,task.getClass().getName(),System.currentTimeMillis()-beginTime);
						}
							
					}
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		catch(Error e)
		{
			e.printStackTrace();
		}
		finally{
			this.pool.threadEndWork(this);
			this.pool.threadDead(this);
		}
	}
}
