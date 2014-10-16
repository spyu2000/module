/**
 * ¾øÃÜ Created on 2009-8-11 by edmund
 */
package server.distribute;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IDistribute extends Remote{
	public static final String DISTRIBUTE_SERVER_NAME = "DISTRIBUTE_SERVER";
	public static final String DISTRIBUTE_CLIENT_NAME = "DISTRIBUTE_CLIENT";
	
	public boolean s_heartConnect(String guid,int curTaskNum) throws RemoteException;
	
	public void s_registClient(String guid,int concurrentNum,IDistribute client) throws RemoteException;
	
	public TaskInfo s_taskFinish(String guid,String serverName,long taskId,ResultInfo resultInfo) throws RemoteException;
	
	public byte[] s_requestJar(JarInfo jarInfo) throws RemoteException;

	public void c_heartConnect(String info) throws RemoteException;
	public void c_dispatchServer(ServerInfo[] serverInfo) throws RemoteException;
	public void c_dispatchTask(TaskInfo[] task) throws RemoteException;
}
