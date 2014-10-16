/**
 * ¾øÃÜ Created on 2009-8-11 by edmund
 */
package server.distribute.server;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import server.distribute.*;

public class ServerRmiInterface extends UnicastRemoteObject implements IDistribute{
	private ClientManager manager = null;
	public ServerRmiInterface(ClientManager manager) throws Exception{
		this.manager = manager;
	}

	public boolean s_heartConnect(String guid,int curTaskNum) throws RemoteException{
		return this.manager.heartConnect(guid,curTaskNum);
	}

	public void s_registClient(String guid,int concurrentNum,IDistribute client) throws RemoteException{
		this.manager.newClientConnect(guid,concurrentNum,client);
	}
	
	public TaskInfo s_taskFinish(String guid,String serverName,long taskId,ResultInfo resultInfo) throws RemoteException{
		return this.manager.taskFinished(guid,serverName,taskId,resultInfo);
	}
	public byte[] s_requestJar(JarInfo jarInfo) throws RemoteException{
		return this.manager.loadJarInfo(jarInfo);
	}
	
	public void c_heartConnect(String info) throws RemoteException{};
	public void c_dispatchTask(TaskInfo[] task) throws RemoteException{}
	public void c_dispatchServer(ServerInfo[] serverInfo) throws RemoteException{}
}
