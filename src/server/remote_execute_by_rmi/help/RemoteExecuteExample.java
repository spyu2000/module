package server.remote_execute_by_rmi.help;

import com.fleety.base.InfoContainer;
import com.fleety.server.BasicServer;
import com.fleety.server.ServerContainer;

import server.remote_execute_by_rmi.IRemoteExecute;
import server.remote_execute_by_rmi.client.RemoteExecuteByRmiClient;
import server.remote_execute_by_rmi.server.RemoteExecuteByRmiServer;

public class RemoteExecuteExample extends BasicServer implements IRemoteExecute{
    public Object execute(InfoContainer para){
        System.out.println(para.getInfo("test"));
        RemoteExecuteByRmiServer server = (RemoteExecuteByRmiServer)ServerContainer.getSingleInstance().getServer("remote_execute_by_rmi_server");
        
        return "变更class:"+server.getStringPara(RemoteExecuteByRmiServer.IP_FLAG)+":"+server.getStringPara(RemoteExecuteByRmiServer.PORT_FLAG);
    }

    public boolean startServer(){
        RemoteExecuteByRmiClient client = (RemoteExecuteByRmiClient)ServerContainer.getSingleInstance().getServer("remote_execute_by_rmi_client");
        InfoContainer para = new InfoContainer();
        para.setInfo("test", "徐新你好,这只是测试!");
        System.out.println(client.remoteRmiExecute("server.remote_execute_by_rmi.help.RemoteExecuteExample", para));
        return true;
    }
    
    public void stopServer(){
        
    }
}
