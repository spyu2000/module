/**
 * ¾øÃÜ Created on 2010-9-16 by edmund
 */
package server.threadgroup;

import com.fleety.util.pool.thread.ThreadPool;

public class PoolInfo{
	public Object poolType = ThreadPool.SINGLE_TASK_LIST_POOL;
	public int workersNumber = 1;
	public int taskCapacity = 1000;
	public boolean isDaemo = true;
	public int priority = Thread.NORM_PRIORITY;
	
	public PoolInfo(){
		
	}
	
	public PoolInfo(Object poolType,int workersNumber,int taskCapacity,boolean isDaemo){
		this(poolType,workersNumber,taskCapacity,isDaemo,Thread.NORM_PRIORITY);
	}
	
	public PoolInfo(Object poolType,int workersNumber,int taskCapacity,boolean isDaemo,int priority){
		this.poolType = poolType;
		this.workersNumber = workersNumber;
		this.taskCapacity = taskCapacity;
		this.isDaemo = isDaemo;
		this.priority = priority;
	}
}
