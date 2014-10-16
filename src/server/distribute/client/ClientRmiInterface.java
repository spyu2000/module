/**
 * ¾øÃÜ Created on 2009-8-11 by edmund
 */
package server.distribute.client;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import server.distribute.*;

public class ClientRmiInterface extends UnicastRemoteObject implements IDistribute{
	private ServerManager serverManager = null;
	public ClientRmiInterface(ClientServer clientServer) throws Exception{
		this.serverManager = new ServerManager(clientServer);
	}
	
	
	public void c_dispatchTask(TaskInfo[] task) throws RemoteException{
		System.out.println("TaskNum="+task.length);
		for(int i=0;i<task.length;i++){
			this.serverManager.executeTask(task[i].getServerName(),task[i]);
		}
	}
	public void c_dispatchServer(ServerInfo[] serverInfo) throws RemoteException{
		this.serverManager.destroyAllServer();
		for(int i=0;i<serverInfo.length;i++){
			this.serverManager.createServer(serverInfo[i]);
		}
	}
	public void c_heartConnect(String info) throws RemoteException{
		System.out.println(info);
	}

	public boolean s_heartConnect(String guid,int curTaskNum) throws RemoteException{return true;}
	public void s_registClient(String guid,int concurrentNum,IDistribute client) throws RemoteException{}
	public TaskInfo s_taskFinish(String guid,String serverName,long taskId,ResultInfo resultInfo) throws RemoteException{return null;}
	public byte[] s_requestJar(JarInfo jarInfo) throws RemoteException{return null;};
}
