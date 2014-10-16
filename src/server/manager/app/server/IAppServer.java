package server.manager.app.server;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IAppServer extends Remote{
	public void heartConnect(String guid) throws RemoteException;
	public boolean registClient(String guid,String cip,int cport) throws RemoteException;
	public byte[] requestFile(String appName,String filePath,long fileSize,long lastModified) throws RemoteException;
	
	public boolean cmdExecute(int cmd,String[] argv) throws RemoteException;
}
