package server.remote_execute_by_rmi.server;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;

import server.remote_execute_by_rmi.IRemoteExecute;

import com.fleety.base.InfoContainer;

public class ClsRemoteRmiInterface extends UnicastRemoteObject implements IClsRemote{
    private HashMap loaderMapping = null;
    public ClsRemoteRmiInterface() throws Exception{
        this.loaderMapping = new HashMap(4);
    }
    
    public Object execute(String groupName, String clsName, InfoContainer para)
            throws RemoteException{
    	ClassLoader loader = null;
        synchronized(this.loaderMapping){
            loader = (ByteClassLoader)this.loaderMapping.get(groupName);
        }
        if(loader == null){
            loader = ClsRemoteRmiInterface.class.getClassLoader();
        }
        try{
            System.out.println("RemoteExecute:groupName="+groupName+";clsName="+clsName);
            Class cls = Class.forName(clsName, false, loader);
            IRemoteExecute obj = (IRemoteExecute)cls.newInstance();
            return obj.execute(para);
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public boolean registerClass(String groupName, String clsName, byte[] clsData)
            throws RemoteException{
        ByteClassLoader loader = null;
        synchronized(this.loaderMapping){
            loader = (ByteClassLoader)this.loaderMapping.get(groupName);
            if(loader == null){
                loader = new ByteClassLoader(ClsRemoteRmiInterface.class.getClassLoader());
                this.loaderMapping.put(groupName, loader);
            }
        }
        
        return loader.registerClass(clsName, clsData) != null;
    }
    
    public void clearGroupClass(String groupName) throws RemoteException{
        synchronized(this.loaderMapping){
            this.loaderMapping.remove(groupName);
        }
    }
}
