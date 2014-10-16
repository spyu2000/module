package server.manager.app.server;

import java.io.File;
import java.io.FileInputStream;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Hashtable;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.fleety.base.Util;
import com.fleety.base.xml.XmlParser;
import com.fleety.util.pool.thread.ThreadPool;

import server.manager.app.ApplicationManageServer;
import server.manager.app.client.IAppClient;
import server.threadgroup.PoolInfo;
import server.threadgroup.ThreadPoolGroupServer;

/**
 * @author Administrator
 * 本服务类暂时不做主动扫描，也就是说如果服务方管理的应用程序发生了变动，需要重新启动服务方。服务方不主动监测变化。
 */
public class ServerManagerRmiInterface extends UnicastRemoteObject implements IAppServer{
	private ApplicationManageServer server = null;
	private String poolName = null;
	private ThreadPool pool = null;
	public ServerManagerRmiInterface(ApplicationManageServer server) throws RemoteException{
		this.server = server;
	}

	private String sip = null;
	private int sport = 0;
	public boolean start() throws Exception{
		this.sip = this.server.getStringPara(ApplicationManageServer.SERVER_IP_FLAG);
		this.sport = Integer.parseInt(this.server.getStringPara(ApplicationManageServer.SERVER_PORT_FLAG));
		
		this.appCfgPath = this.server.getStringPara(ApplicationManageServer.APP_CFG_FLAG);
		
		this.poolName = ServerManagerRmiInterface.class.getName()+"["+this.hashCode()+"]";
		PoolInfo pInfo = new PoolInfo();
		pInfo.poolType = ThreadPool.SINGLE_TASK_LIST_POOL;
		pInfo.workersNumber = 1;
		pInfo.taskCapacity = 1000;
		this.pool = ThreadPoolGroupServer.getSingleInstance().createThreadPool(this.poolName, pInfo);
        try{
        	this.initAppInfo();
        }catch(Exception e){
        	e.printStackTrace();
        	return false;
        }

		try{
            LocateRegistry.createRegistry(sport);
        }catch (Exception e){
        	e.printStackTrace();
        }
        
        try{
            Naming.rebind("//" + sip + ":" + sport + "/" +ApplicationManageServer.APP_MANAGE_SERVER_RMI_NAME, this);
        }catch(Exception e){
        	e.printStackTrace();
        	return false;
        }
        
		return true;
	}
	
	public void stop(){
		try{
            Naming.unbind("//" + sip + ":" + sport + "/" +ApplicationManageServer.APP_MANAGE_SERVER_RMI_NAME);
        }catch(Exception e){
        	e.printStackTrace();
        }
        
        ThreadPoolGroupServer.getSingleInstance().removeThreadPool(this.poolName);
        if(this.pool != null){
        	this.pool.stopWork();
        }
	}

	private Hashtable appMapping = new Hashtable();
	private String appCfgPath = null;
	private void initAppInfo() throws Exception{
		File f = new File(appCfgPath);
		if(!f.exists()){
			return ;
		}
		
		FileInputStream in = new FileInputStream(f);
		Element root = XmlParser.parse(in);
		in.close();
		
		AppInfo appInfo;
		Node appNode;
		String tempStr;
		Node[] allAppNode = Util.getElementsByTagName(root, "app");
		for(int i=0;i<allAppNode.length;i++){
			appNode = allAppNode[i];
			
			tempStr = Util.getNodeText(Util.getSingleElementByTagName(appNode, "enable"));
			if(tempStr != null && tempStr.trim().equalsIgnoreCase("false")){
				continue;
			}
			
			appInfo = new AppInfo();
			appInfo.appName = Util.getNodeText(Util.getSingleElementByTagName(appNode, "app_name"));
			appInfo.appDesc = Util.getNodeText(Util.getSingleElementByTagName(appNode, "desc"));
			tempStr = Util.getNodeText(Util.getSingleElementByTagName(appNode, "owner"));
			if(tempStr != null && tempStr.trim().length() > 0){
				appInfo.owner = ","+tempStr+",";
			}
			appInfo.appPath = Util.getNodeText(Util.getSingleElementByTagName(appNode, "app_path"));
			appInfo.execFile = Util.getNodeText(Util.getSingleElementByTagName(appNode, "exec_file"));
			appInfo.startCmd = Util.getNodeText(Util.getSingleElementByTagName(appNode, "start_cmd"));
			appInfo.stopCmd = Util.getNodeText(Util.getSingleElementByTagName(appNode, "stop_cmd"));
			
			appInfo.scanFileInfo();
			
			this.appMapping.put(appInfo.appName, appInfo);
		}
	}
	

	//以下是服务器端RMI接口
	private Hashtable clientMapping = new Hashtable(16);
	public void heartConnect(String guid) throws RemoteException{
		ClientInfo clientInfo = (ClientInfo)this.clientMapping.get(guid);
		if(clientInfo != null){
			clientInfo.lastHeartTime = System.currentTimeMillis();
		}
	}
	public boolean registClient(String guid,String cip,int cport) throws RemoteException{
		ClientInfo info = new ClientInfo();
		
		info.guid = guid;
		info.cip = cip;
		info.cport = cport;
		
		try{
			String rmiUrlStr = "//"+cip+":"+cport+"/"+ApplicationManageServer.APP_MANAGE_CLIENT_RMI_NAME;
			info.clientRmi = (IAppClient)Naming.lookup(rmiUrlStr);
				
			clientMapping.put(guid, info);
			System.out.println("新连接:"+info.guid);
			
			AppInfo[] arr = new AppInfo[this.appMapping.size()];
			this.appMapping.values().toArray(arr);
			for(int i=0;i<arr.length;i++){
				if(arr[i].containOwner(guid)){
					this.pool.addTask(new DispatchAppTask(new ClientInfo[]{info},arr[i],arr[i].fileList));
				}
			}
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	public byte[] requestFile(String appName,String filePath,long fileSize,long lastModified) throws RemoteException{
		AppInfo appInfo = (AppInfo)this.appMapping.get(appName);
		if(appInfo != null){
			File f = new File(appInfo.appPath+File.separator+filePath);
			if(f.exists()){
				try{
					byte[] data = new byte[(int)f.length()];
					FileInputStream in = new FileInputStream(f);
					in.read(data);
					in.close();
					
					return data;
				}catch(Exception e){
					e.printStackTrace();
				}
			}else{
				return null;
			}
		}
		
		return null;
	}
	
	public static final int APP_START_CMD = 1;
	public static final int APP_STOP_CMD = 2;
	public static final int APP_RESTART_CMD = 3;
	
	public boolean cmdExecute(int cmd,String[] argv) throws RemoteException{
		switch(cmd){
			case APP_START_CMD:
			case APP_STOP_CMD:
			case APP_RESTART_CMD:
				return this.startOrStopApp(cmd, argv);
		}
		return false;
	}
	
	private boolean startOrStopApp(int cmd,String[] argv){
		if(argv == null || argv.length < 1){
			return false;
		}
		ClientInfo[] allClient = null;
		synchronized(this.clientMapping){
			allClient = new ClientInfo[this.clientMapping.size()];
			clientMapping.values().toArray(allClient);
		}
		
		boolean isAll = false;
		String appName = argv[0];
		if(appName.equalsIgnoreCase("all")){
			isAll = true;
		}

		AppInfo[] arr = new AppInfo[this.appMapping.size()];
		this.appMapping.values().toArray(arr);
		
		boolean isOk = true;
		ClientInfo clientInfo;
		for(int i=0;i<allClient.length;i++){
			clientInfo = allClient[i];
			try{
				if(isAll){ //重新启动或停止所有的应用程序，多个管理客户端
					for(int j=0;j<arr.length;j++){
						if(arr[j].containOwner(clientInfo.guid)){
							if(cmd == APP_STOP_CMD){
								clientInfo.clientRmi.stop(arr[j].appName);
							}else{
								clientInfo.clientRmi.restart(arr[j].appName);
							}
						}
					}
				}else{//重新启动或停止指定的应用程序，多个管理客户端
					if(cmd == APP_STOP_CMD){
						clientInfo.clientRmi.stop(appName);
					}else{
						clientInfo.clientRmi.restart(appName);
					}
				}
			}catch(Exception e){
				e.printStackTrace();
				isOk = false;
			}
		}
		return isOk;
	}
	
	public class AppInfo{
		public String appPath = null;
		public String appDesc = null;
		public String owner = null;
		public String appName = null;
		public String execFile = null;
		public String startCmd = null;
		public String stopCmd = null;
		
		public ArrayList fileList = new ArrayList(64);
		
		public void scanFileInfo(){
			File rootFile = new File(this.appPath);
			this.scanDir(rootFile,"");
		}
		
		private void scanDir(File dirFile,String relativeDirStr){
			File[] subFileArr = dirFile.listFiles();
			File subFile;
			for(int i=0;i<subFileArr.length;i++){
				subFile = subFileArr[i];
				if(subFile.isFile()){
					this.addFileInfo(relativeDirStr+File.separator+subFile.getName());
				}else if(subFile.isDirectory()){
					this.scanDir(subFile, relativeDirStr+File.separator+subFile.getName());
				}
			}
		}
		
		public void addFileInfo(String filePath){
			FileInfo fileInfo = new FileInfo(filePath);
			
			fileInfo.updateFileInfo(new File(this.appPath,filePath));
			
			this.fileList.add(fileInfo);
		}
		
		public boolean containOwner(String oneOwner){
			if(this.owner == null){
				return true;
			}
			return this.owner.indexOf(","+oneOwner+",") >= 0;
		}
		
		public class FileInfo{
			public String filePath = null;
			public long fileSize = 0;
			public long lastModified = 0;
			
			public FileInfo(String filePath){
				this.filePath = filePath;
			}
			
			public void updateFileInfo(File f){
				this.fileSize = f.length();
				this.lastModified = f.lastModified();
			}
			
			public boolean isChanged(File f){
				return f.length() != this.fileSize || f.lastModified() != this.lastModified;
			}
		}
	}
	
	public class ClientInfo{
		public String guid = null;
		public long lastHeartTime = System.currentTimeMillis();
		public String cip;
		public int cport;
		
		public IAppClient clientRmi = null;
	}
}
