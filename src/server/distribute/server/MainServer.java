/**
 * ¾øÃÜ Created on 2008-11-10 by edmund
 */
package server.distribute.server;

import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;
import server.distribute.IDistribute;
import com.fleety.server.BasicServer;

public class MainServer extends BasicServer{
	private String rmiIp = null;
	private int rmiPort = 0;
	
	public boolean startServer(){
		String distributePath = this.getStringPara("distribute_server_path");
		ClientManager manager = new ClientManager();
		if(!manager.initServer(distributePath)){
			return false;
		}
		
		
		this.rmiIp = this.getStringPara("ip");
		this.rmiPort = this.getIntegerPara("port").intValue();
		
		try{
            LocateRegistry.createRegistry(this.rmiPort);
        }catch (Exception e){
        	e.printStackTrace();
        }
        
        try{
        	ServerRmiInterface iserver = new ServerRmiInterface(manager);

            Naming.rebind("//" + this.rmiIp + ":" + this.rmiPort + "/" +IDistribute.DISTRIBUTE_SERVER_NAME, iserver);
        }catch(Exception e){
        	e.printStackTrace();
        	return false;
        }
        
		return true;
	}
	public void stopServer(){
		
	}
}
