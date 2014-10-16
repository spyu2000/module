package server.manager.app.client;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IAppClient extends Remote {
	public void startDispatchApplication(String appName,String execFile,String startCmd,String stopCmd) throws RemoteException;
	public void fileChanged(String appName,String filePath,long fileSize,long lastModified) throws RemoteException;
	public void endDispatchApplication(String appName) throws RemoteException;
	public void restart(String appName) throws RemoteException;
	public void stop(String appName) throws RemoteException;
}
