package server.manager.app;

import server.manager.app.client.ClientManagerRmiInterface;
import server.manager.app.server.ServerManagerRmiInterface;

import com.fleety.server.BasicServer;

public class ApplicationManageServer extends BasicServer {
	public static final String SERVER_FLAG = "is_server";
	
	public static final String CLIENT_NAME_FLAG = "client_name";
	public static final String CLIENT_APP_PATH_FLAG = "app_path";
	
	public static final String IP_FLAG = "ip";
	public static final String PORT_FLAG = "port";
	
	public static final String SERVER_IP_FLAG = "server_ip";
	public static final String SERVER_PORT_FLAG = "server_port";
	
	public static final String APP_OWNER_FLAG = "owner";
	public static final String APP_CFG_FLAG = "app_cfg";

	public static final String APP_MANAGE_SERVER_RMI_NAME = "app_manage_server";
	public static final String APP_MANAGE_CLIENT_RMI_NAME = "app_manage_client";
	
	private boolean isServer = false;
	public boolean startServer() {
		String tempStr;
		
		tempStr = this.getStringPara(SERVER_FLAG);
		if(tempStr != null && tempStr.equalsIgnoreCase("true")){
			this.isServer = true;
		}
		
		boolean isSuccess = false;
		try{
			if(this.isServer){
				isSuccess = this.serverStart();
			}else{
				isSuccess = this.clientStart();
			}
		}catch(Exception e){
			e.printStackTrace();
			isSuccess = false;
		}
		
		if(isSuccess){
			this.isRunning = true;
		}
		
		return isSuccess;
	}
	
	private ServerManagerRmiInterface serverManager = null;
	public boolean serverStart() throws Exception{
		this.serverManager = new ServerManagerRmiInterface(this);
		return this.serverManager.start();
	}
	
	private ClientManagerRmiInterface clientManager = null;
	public boolean clientStart() throws Exception{
		this.clientManager = new ClientManagerRmiInterface(this);
		return this.clientManager.start();
	}

	public void stopServer() {
		if(this.serverManager != null){
			this.serverManager.stop();
		}
		if(this.clientManager != null){
			this.clientManager.stop();
		}
	}

}
