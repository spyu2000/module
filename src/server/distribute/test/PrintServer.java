/**
 * 绝密 Created on 2009-8-13 by edmund
 */
package server.distribute.test;

import com.fleety.util.pool.timer.FleetyTimerTask;
import com.fleety.util.pool.timer.TimerPool;

import server.distribute.TaskInfo;
import server.distribute.TaskServer;
import server.threadgroup.ThreadPoolGroupServer;

public class PrintServer extends TaskServer{
	private TimerPool timer = null;
	public boolean startServer(){
		this.timer = ThreadPoolGroupServer.getSingleInstance().createTimerPool(PrintServer.class.getName()+"["+this.hashCode()+"]");
		this.timer.schedule(new FleetyTimerTask(){
			public void run(){
				createTask();
			}
		}, 5000, 6000);
		
		return true;
	}

	public void createTask(){
		TaskInfo[] taskArr = new TaskInfo[20];
		for(int i=0;i<taskArr.length;i++){
			taskArr[i] = new TaskInfo(this.getServerName());
			taskArr[i].setInfo("info", "第"+taskArr[i].getId()+"个任务!");
		}
		this.clientManager.addTask(this.getServerName(), taskArr);
	}
	
	public boolean startTask(){
		System.out.println("start task:"+this.taskInfo.getString("info"));
		try{
			Thread.sleep(1000);
		}catch(Exception e){}
		System.out.println("end task:"+this.taskInfo.getString("info"));
		return true;
	}
	public void stopTask(){
		
	}
}
