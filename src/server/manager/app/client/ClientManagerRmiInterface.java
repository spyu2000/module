package server.manager.app.client;

import java.io.File;
import java.io.FileOutputStream;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Hashtable;

import com.fleety.base.FleetyThread;
import com.fleety.base.GUIDCreator;
import com.fleety.util.pool.timer.FleetyTimerTask;

import server.manager.app.ApplicationManageServer;
import server.manager.app.server.IAppServer;
import server.threadgroup.ThreadPoolGroupServer;

public class ClientManagerRmiInterface extends UnicastRemoteObject implements IAppClient{
	private ApplicationManageServer server = null;
	private String clientGuid = null;
	private String appPath = null;
	
	public ClientManagerRmiInterface(ApplicationManageServer server) throws RemoteException{
		this.server = server;
	}
	
	private String sip = null;
	private int sport = 0;
	private String cip = null;
	private int cport = 0;
	public boolean start() throws Exception{
		this.clientGuid = this.server.getStringPara(ApplicationManageServer.CLIENT_NAME_FLAG);
		if(this.clientGuid == null || this.clientGuid.trim().length() == 0){
			this.clientGuid = new GUIDCreator().createNewGuid(GUIDCreator.FORMAT_STRING);
		}
		
		this.appPath = this.server.getStringPara(ApplicationManageServer.CLIENT_APP_PATH_FLAG);
		if(this.appPath == null){
			this.appPath = "_app";
		}
		
		this.sip = this.server.getStringPara(ApplicationManageServer.SERVER_IP_FLAG);
		this.sport = Integer.parseInt(this.server.getStringPara(ApplicationManageServer.SERVER_PORT_FLAG));
		
		this.cip = this.server.getStringPara(ApplicationManageServer.IP_FLAG);
		this.cport = Integer.parseInt(this.server.getStringPara(ApplicationManageServer.PORT_FLAG));
		
		try{
            LocateRegistry.createRegistry(cport);
        }catch (Exception e){
        	e.printStackTrace();
        }
        
        try{
            Naming.rebind("//" + cip + ":" + cport + "/" +ApplicationManageServer.APP_MANAGE_CLIENT_RMI_NAME, this);
        }catch(Exception e){
        	e.printStackTrace();
        	return false;
        }
        
        ThreadPoolGroupServer.getSingleInstance().createTimerPool(ClientManagerRmiInterface.class.getName()+"["+this.hashCode()+"]").schedule(new FleetyTimerTask(){
        	public void run(){
        		connectServer();
        	}
        }, 0, 60000l);
        
		return true;
	}
	
	public void stop(){
		try{
            Naming.unbind("//" + cip + ":" + cport + "/" +ApplicationManageServer.APP_MANAGE_CLIENT_RMI_NAME);
        }catch(Exception e){
        	e.printStackTrace();
        }
	}
	
	private IAppServer serverManager = null;
	private void connectServer(){
		if(this.serverManager != null){
			try{
				this.serverManager.heartConnect(this.clientGuid);
				return ;
			}catch(Exception e){
				e.printStackTrace();
				this.serverManager = null;
			}
		}
		String rmiUrlStr = "//"+this.sip+":"+this.sport+"/"+ApplicationManageServer.APP_MANAGE_SERVER_RMI_NAME;
		try{
			this.serverManager = (IAppServer)Naming.lookup(rmiUrlStr);
			
			if(this.serverManager.registClient(this.clientGuid, this.cip, this.cport)){
				
			}else{
				this.serverManager = null;
			}
		}catch(Exception e){
			e.printStackTrace();
			this.serverManager = null;
		}
	}

	
	
	
	//以下是客户器端RMI接口
	private Hashtable appMapping = new Hashtable(8);
	public void startDispatchApplication(String appName,String execFile,String startCmd,String stopCmd) throws RemoteException{
		AppInfo appInfo = new AppInfo();
		appInfo.appName = appName;
		appInfo.execFile = execFile;
		appInfo.startCmd = startCmd;
		appInfo.stopCmd = stopCmd;
		
		this.appMapping.put(appInfo.appName, appInfo);
	}

	public void fileChanged(String appName,String filePath,long fileSize,long lastModified) throws RemoteException{
		AppInfo appInfo = (AppInfo)this.appMapping.get(appName);
		if(appInfo != null){
			long localFileSize = 0;
			long localLastModified = 0;
			File f = new File(this.appPath+File.separator+appName+File.separator+filePath);
			boolean update = false;
			if(f.exists()){
				localFileSize = f.length();
				localLastModified = f.lastModified();
				if(localFileSize != fileSize || localLastModified != lastModified){
					update = true;
				}
			}else{
				update = true;
			}
			
			if(update){
				try{
					byte[] data = this.serverManager.requestFile(appName, filePath, localFileSize, localLastModified);
					
					if(data != null){
						f.getParentFile().mkdirs();
						FileOutputStream out = new FileOutputStream(f);
						out.write(data);
						out.close();
						
						f.setLastModified(lastModified);
						appInfo.isChanged = true;
						
						System.out.println("应用程序["+appName+"]更新文件["+filePath+"]"+"size["+localFileSize+","+fileSize+"]"+"time["+localLastModified+","+lastModified+"]"+"成功!");
					}
				}catch(Exception e){
					e.printStackTrace();
					this.serverManager = null;
					throw new RemoteException("应用程序["+appName+"]的文件["+filePath+"]发布出错，将重新发布!");
				}
			}
		}
	}
	
	public void endDispatchApplication(String appName) throws RemoteException{
		AppInfo appInfo = (AppInfo)this.appMapping.get(appName);
		if(appInfo != null){
			if(appInfo.isChanged){
				appInfo.init();
				appInfo.restart();
			}
		}
	}
	
	public void restart(String appName) throws RemoteException{
		AppInfo appInfo = (AppInfo)this.appMapping.get(appName);
		if(appInfo != null){
			appInfo.restart();
		}
	}
	public void stop(String appName) throws RemoteException{
		AppInfo appInfo = (AppInfo)this.appMapping.get(appName);
		if(appInfo != null){
			appInfo.stop();
		}
	}
	
	private class AppInfo{
		public String appName = null;
		public String execFile = null;
		public String startCmd = null;
		public String stopCmd = null;
		
		public boolean isChanged = false;
		
		public void init(){
			String appDir = appPath+File.separator+appName;
			
			if(this.execFile != null){
				String[] execFileArr = this.execFile.split(":");
				for(int i=0;i<execFileArr.length;i++){
					try{
						Runtime.getRuntime().exec("chmod 775 "+appDir+File.separator+execFileArr[i]);
					}catch(Exception e){
						e.printStackTrace();
					}
				}
			}
		}
		
		private void start(){
			String appDir = appPath+File.separator+appName;
			String execCmd = appDir+File.separator+this.startCmd;
			try{
				Process process = Runtime.getRuntime().exec(execCmd, null, new File(appDir));
				
				System.out.println("应用程序["+this.appName+"]重启成功!");
			}catch(Exception e){
				e.printStackTrace();
				System.out.println("应用程序["+this.appName+"]重启失败!执行命令:"+execCmd);
			}
		}
		
		private void stop(){
			try{
				if(this.stopCmd != null && this.stopCmd.trim().length() > 0){
					String appDir = appPath+File.separator+appName;
					Process process = Runtime.getRuntime().exec(appDir+File.separator+this.stopCmd, null, new File(appDir));
					process.waitFor();
				}
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		
		private void restart(){
			this.isChanged = false;
			
			this.stop();
			
			try{
				FleetyThread.sleep(5000);
			}catch(Exception e){}
			
			this.start();
		}
	}
}
