package server.remote_execute_by_rmi.server;

import java.rmi.registry.LocateRegistry;

import server.remote_execute_by_rmi.RmiNaming;

import com.fleety.server.BasicServer;

public class RemoteExecuteByRmiServer extends BasicServer{
    public static final String IP_FLAG = "ip";
    public static final String PORT_FLAG = "port";
    
    public static final String REMOTE_EXECUTE_RMI_NAME = "rmiRemoteExecute";
    
    private String registName = null;
    public boolean startServer(){
        String ip = this.getStringPara(IP_FLAG);
        int port = Integer.parseInt(this.getStringPara(PORT_FLAG));
        
        try{
        	LocateRegistry.createRegistry(port);
        }catch (Exception e){}
        
        try{
        	RmiNaming.getSingleInstance().rebind(this.registName = "//" + ip + ":" + port + "/" + REMOTE_EXECUTE_RMI_NAME, new ClsRemoteRmiInterface());
        }catch(Exception e){
            e.printStackTrace();
            return false;
        }
        
        this.isRunning = true;
        return this.isRunning;
    }

    public void stopServer(){
        try{
            if(this.registName != null){
                RmiNaming.getSingleInstance().unbind(this.registName);
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
