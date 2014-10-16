/**
 * 绝密 Created on 2008-11-10 by edmund
 */
package server.distribute.client;

import java.rmi.Naming;
import server.distribute.IDistribute;
import server.distribute.client.ServerManager.TaskContainer;
import server.threadgroup.PoolInfo;
import server.threadgroup.ThreadPoolGroupServer;

import com.fleety.base.GUIDCreator;
import com.fleety.server.BasicServer;
import com.fleety.util.pool.thread.ThreadPool;
import com.fleety.util.pool.timer.FleetyTimerTask;

public class ClientServer extends BasicServer{
	private String serverRmiIp = null;
	private int serverRmiPort = 0;
	
	private IDistribute serverRmi = null;
	
	private int maxTaskNum = 1;
	
	private String clientGuid = null;
	
	private String poolName = null;
	private ThreadPool pool = null;
	private ClientRmiInterface iserver = null;
	
	public boolean startServer(){
		this.clientGuid = new GUIDCreator().createNewGuid(GUIDCreator.FORMAT_STRING);

		this.serverRmiIp = this.getStringPara("server_ip");
		this.serverRmiPort = this.getIntegerPara("server_port").intValue();
		
		Integer tempInt = this.getIntegerPara("concurrent_task_num");
		if(tempInt != null){
			this.maxTaskNum = tempInt.intValue();
		}
        
        try{
        	this.iserver = new ClientRmiInterface(this);
        }catch(Exception e){
        	e.printStackTrace();
        	return false;
        }
        
        this.poolName = this.getServerName()+"["+this.hashCode()+"]";
        try{
        	PoolInfo pInfo = new PoolInfo();
        	pInfo.poolType = ThreadPool.SINGLE_TASK_LIST_POOL;
        	pInfo.taskCapacity = this.maxTaskNum;
        	pInfo.workersNumber = this.maxTaskNum;
        	this.pool = ThreadPoolGroupServer.getSingleInstance().createThreadPool(this.poolName, pInfo);
        }catch(Exception e){
        	e.printStackTrace();
        	return false;
        }
        
        ThreadPoolGroupServer.getSingleInstance().createTimerPool(this.getServerName()+"["+this.hashCode()+"]").schedule(new FleetyTimerTask(){
        	public void run(){
        		ClientServer.this.scanServerRmi();

        		System.gc();
        	}
        }, 0, 60*1000);
        
        
        this.isRunning = true;
		return true;
	}
	
	public void addTask(TaskContainer task){
		if(this.isRunning()){
			this.pool.addTask(task);
		}
	}
	
	public IDistribute getServerRmi(){
		return this.serverRmi;
	}
	
	private void scanServerRmi(){
		if(this.serverRmi == null){
			this.connectRmi();
		}else{
			try{
				if(!this.serverRmi.s_heartConnect(this.clientGuid,this.pool.getWorkThreadNum())){
					throw new Exception("服务器不存在对应客户端信息!");
				}
			}catch(Exception e){
				e.printStackTrace();
				this.connectRmi();
			}
		}
	}
	private IDistribute connectRmi(){
		String rmiUrlStr = "//"+this.serverRmiIp+":"+this.serverRmiPort+"/"+IDistribute.DISTRIBUTE_SERVER_NAME;
		try{
			IDistribute rmi = (IDistribute)Naming.lookup(rmiUrlStr);
			this.serverRmi = rmi;
			
			rmi.s_registClient(this.clientGuid, this.maxTaskNum, this.iserver);
			
			return rmi;
		}catch(Exception e){
			e.printStackTrace();
			this.serverRmi = null;
			return null;
		}
	}
	
	public String getGuid(){
		return this.clientGuid;
	}

	public void stopServer(){		
		ThreadPoolGroupServer.getSingleInstance().removeThreadPool(this.poolName);
		if(this.pool != null){
			this.pool.stopWork();
		}
	}
}
