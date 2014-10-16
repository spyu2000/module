/**
 * ¾øÃÜ Created on 2009-8-13 by edmund
 */
package server.distribute;

import server.distribute.server.ClientManager;
import com.fleety.server.BasicServer;

public abstract class TaskServer extends BasicServer{
	protected TaskInfo taskInfo = null;
	protected ClientManager clientManager = null;
	
	public void setTaskInfo(TaskInfo taskInfo){
		this.taskInfo = taskInfo;
	}
	public TaskInfo getTaskInfo(){
		return this.taskInfo;
	}
	
	public void setClientManager(ClientManager clientManager){
		this.clientManager = clientManager;
	}
	
	public void stopServer(){
		
	}
	
	public void taskStatusChanged(TaskInfo taskInfo, ResultInfo resultInfo){
		
	}
	
	public abstract boolean startTask();
	public abstract void stopTask();
	
	public ResultInfo getTaskResult(){
		return new ResultInfo();
	}
}
