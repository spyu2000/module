/**
 * 绝密 Created on 2009-8-14 by edmund
 */
package server.distribute.server;

import java.util.*;
import com.fleety.util.pool.thread.ITask;
import com.fleety.util.pool.thread.ThreadPool;
import com.fleety.util.pool.timer.FleetyTimerTask;
import com.fleety.util.pool.timer.TimerPool;

import server.distribute.TaskInfo;
import server.threadgroup.PoolInfo;
import server.threadgroup.ThreadPoolGroupServer;

public class TaskContainer{
	private LinkedList taskList = new SyncList();
	private Hashtable pendingMapping = new Hashtable();
	
	private long lastDispatchTime = System.currentTimeMillis();
	private long scanInterval = 10000l;
	private TimerPool timer = null;
	private ClientManager clientManager = null;
	private String poolName = null;
	private ThreadPool threadPool = null;
	public TaskContainer(ClientManager clientManager){
		this.clientManager = clientManager;
		
		this.timer = ThreadPoolGroupServer.getSingleInstance().createTimerPool(TaskContainer.class.getName()+"["+this.hashCode()+"]");
		this.timer.schedule(new FleetyTimerTask(){
			public void run(){
				scanErrorTask();
			}
		}, scanInterval, scanInterval);
		this.timer.schedule(new FleetyTimerTask(){
			public void run(){
				if(System.currentTimeMillis() - lastDispatchTime > 2*scanInterval){
					dispatchTask();
				}
			}
		}, scanInterval, scanInterval);
		
		this.poolName = TaskContainer.class.getName()+"["+this.hashCode()+"]";
        try{
        	PoolInfo pInfo = new PoolInfo();
        	pInfo.poolType = ThreadPool.SINGLE_TASK_LIST_POOL;
        	pInfo.taskCapacity = 1;
        	pInfo.workersNumber = 1;
        	pInfo.priority = Thread.MAX_PRIORITY;
        	this.threadPool = ThreadPoolGroupServer.getSingleInstance().createThreadPool(this.poolName, pInfo);
        }catch(Exception e){
        	e.printStackTrace();
        }
	}

	public void stopServer(){
		this.timer.cancel();
	}
	
	private void scanErrorTask(){
		System.out.println("当前：待调配"+this.taskList.size()+";执行中:"+this.pendingMapping.size());

		boolean hasError = false;
		synchronized(this.pendingMapping){
			TaskInfo taskInfo;
			for(Iterator itr = this.pendingMapping.values().iterator();itr.hasNext();){
				taskInfo = (TaskInfo)itr.next();
				if(System.currentTimeMillis() - taskInfo.getStatusTime() > taskInfo.getTimeout()){
					itr.remove();
					
					taskInfo.updateStatus(TaskInfo.CREATE_STATUS);
					this.taskList.add(taskInfo);
					
					hasError = true;
					
					System.out.println("服务["+taskInfo.getServerName()+"]的任务["+taskInfo.getId()+"]超时未完成，重新发布。");
				}
			}
		}
		
		if(hasError){
			this.dispatchTask();
		}
	}
	
	public void addTask(TaskInfo taskInfo){
		this.taskList.add(taskInfo);
	}
	
	public TaskInfo releaseTask(){
		TaskInfo taskInfo = null;
		synchronized(this.taskList){
			if(this.taskList.size() > 0){
				taskInfo = (TaskInfo)this.taskList.remove(0);
			}
		}
		if(taskInfo != null){
			this.taskRealeased(taskInfo);
		}
		
		return taskInfo;
	}
	
	public void dispatchTask(){
		this.lastDispatchTime = System.currentTimeMillis();
		
		this.threadPool.addTask(new DispatchTask());
	}
	
	public void taskRealeased(TaskInfo taskInfo){
		taskInfo.updateStatus(TaskInfo.RELEASE_STATUS);
		this.pendingMapping.put(new Long(taskInfo.getId()), taskInfo);
	}
	
	public TaskInfo changeTaskStatus(long taskId,int newStatus){
		Object key = new Long(taskId);
		TaskInfo taskInfo = null;
		switch(newStatus){
			case TaskInfo.CREATE_STATUS:
				taskInfo = (TaskInfo)this.pendingMapping.remove(key);
				if(taskInfo != null){
					this.taskList.add(taskInfo);
				}
				break;
			case TaskInfo.FINISHED_STATUS:
				taskInfo = (TaskInfo)this.pendingMapping.remove(key);
				break;
		}
		if(taskInfo != null){
			taskInfo.updateStatus(newStatus);
		}
		
		return taskInfo;
	}
	
	private class DispatchTask implements ITask{
		public DispatchTask(){
			
		}
		
		public String getDesc(){
			return null;
		}
		public Object getFlag(){
			return null;
		}
		
		public boolean execute(){
			if(TaskContainer.this.taskList.isEmpty()){
				return true;
			}
				
			TaskContainer.this.clientManager.dispatchTask(TaskContainer.this.taskList);
			
			return true;
		}
	}
	
	private class SyncList extends LinkedList{
		public synchronized boolean add(Object obj){
			return super.add(obj);
		}
		
		public synchronized Object remove(int index){
			return super.remove(index);
		}
		public synchronized int size(){
			return super.size();
		}
		public synchronized boolean isEmpty(){
			return super.isEmpty();
		}
	}
}
