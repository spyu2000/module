package server.remote_execute_by_rmi.server;

import java.rmi.Remote;
import java.rmi.RemoteException;

import com.fleety.base.InfoContainer;

public interface IClsRemote extends Remote{
    public boolean registerClass(String groupName, String clsName, byte[] clsData)throws RemoteException;
    public Object execute(String groupName, String clsName,InfoContainer para) throws RemoteException;
    public void clearGroupClass(String groupName) throws RemoteException;
}
