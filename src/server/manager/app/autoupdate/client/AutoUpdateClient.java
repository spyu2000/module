package server.manager.app.autoupdate.client;


import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Iterator;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import server.manager.app.autoupdate.info.RequestUpdateObject;
import server.remote_execute_by_rmi.client.RemoteExecuteByRmiClient;
import server.socket.help.ObjectReader;
import server.socket.socket.FleetySocket;
import server.threadgroup.ThreadPoolGroupServer;

import com.fleety.base.Util;
import com.fleety.base.xml.XmlNode;
import com.fleety.base.xml.XmlParser;
import com.fleety.util.pool.timer.FleetyTimerTask;

public class AutoUpdateClient extends FleetySocket {
	private File updateFile = null;
	private int updateCycle = 60000;
	
	public boolean startServer() {
		this.updateFile = new File(this.getStringPara("update_file_path"));
		if(!this.updateFile.exists()){
			return false;
		}
		this.loadUpdateInfo();
		
		String tempStr = this.getStringPara("update_cycle");
		if(tempStr != null && tempStr.trim().length() > 0){
			try{
				this.updateCycle = Integer.parseInt(tempStr.trim())*1000;
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		
		ThreadPoolGroupServer.getSingleInstance().createTimerPool("update_timer", 1, false).schedule(new FleetyTimerTask(){
			public void run(){
				AutoUpdateClient.this.sendUpdateRequest();
			}
		}, 5000, updateCycle);
		
		this.addPara("reader", "server.socket.help.ObjectReader");
		this.addPara("releaser", "server.manager.app.autoupdate.client.CmdReleaser");
		
		return super.startServer();
	}
	
	public void stopServer(){
		super.stopServer();
	}

	private void sendUpdateRequest(){
		this.loadUpdateInfo();
		
		if(this.appList.size() == 0){
			return ;
		}
		if(!this.isConnected()){
			return ;
		}
		
		for(Iterator itr = this.appList.iterator();itr.hasNext();){
			this.requestUpdate((AppInfo)itr.next());
		}
	}
	
	private void requestUpdate(AppInfo appInfo){
		RequestUpdateObject request = new RequestUpdateObject(appInfo.getName(),appInfo.getVersion(),appInfo.getFlag());
		
		try{
			byte[] data = ObjectReader.object2ByteArr(request, 1024*1024);
			this.sendData(data, 0, data.length);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public AppInfo getAppInfo(String appName){
		AppInfo appInfo;
		for(Iterator itr = this.appList.iterator();itr.hasNext();){
			appInfo = (AppInfo)itr.next();
			if(appInfo.getName().equals(appName)){
				return appInfo;
			}
		}
		return null;
	}
	
	private ArrayList appList = new ArrayList(4);
	private long lastModifiedTime = 0;
	public synchronized void loadUpdateInfo(){
		if(this.lastModifiedTime == this.updateFile.lastModified()){
			return ;
		}
		this.lastModifiedTime = this.updateFile.lastModified();
		
		ArrayList tempAppList = new ArrayList(4);
		try{
			Document document = XmlParser.parse(this.updateFile);
			if(document == null){
				return ;
			}
			
			String appName,appFlag,version,path,startBat;
			int rmiStopPort;
			String tempStr;
			AppInfo appInfo;
			Element root = document.getDocumentElement();
			Node[] appNodeArr = Util.getElementsByTagName(root, "app");
			for(int i=0;i<appNodeArr.length;i++){
				appName = Util.getNodeText(Util.getSingleElementByTagName(appNodeArr[i], "app_name"));
				if(appName == null || appName.trim().length() == 0){
					continue;
				}
				appName = appName.trim();
				
				appFlag = Util.getNodeText(Util.getSingleElementByTagName(appNodeArr[i], "app_flag"));
				if(appFlag == null || appFlag.trim().length() == 0){
					continue;
				}
				appFlag = appFlag.trim();
				
				version = Util.getNodeText(Util.getSingleElementByTagName(appNodeArr[i], "version"));
				
				path = Util.getNodeText(Util.getSingleElementByTagName(appNodeArr[i], "path"));
				if(path == null){
					continue;
				}
				path = path.trim();
				
				startBat = Util.getNodeText(Util.getSingleElementByTagName(appNodeArr[i], "start_bat"));
				if(startBat == null){
					continue;
				}
				startBat = startBat.trim();
				
				tempStr = Util.getNodeText(Util.getSingleElementByTagName(appNodeArr[i], "stop_port"));
				if(tempStr == null){
					continue;
				}

				try{
					rmiStopPort = Integer.parseInt(tempStr.trim());
					
					tempAppList.add(appInfo = new AppInfo(appName,appFlag,version,path,startBat,rmiStopPort));
				}catch(Exception e){
					e.printStackTrace();
					continue;
				}

				System.out.println("Manage App:name="+appInfo.getName()+" version="+appInfo.getVersion()+" path="+appInfo.getPath()+" start="+appInfo.getStartBat()+" stopPort="+appInfo.getRmiStopPort());
			}
			this.appList = tempAppList;
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	public synchronized void saveAppUpdateInfo(){
		XmlNode root = new XmlNode("cfg"),appNode;
		AppInfo appInfo ;
		for(Iterator itr = this.appList.iterator();itr.hasNext();){
			appInfo = (AppInfo)itr.next();
			
			appNode = new XmlNode("app");

			appNode.addNode(new XmlNode("app_name",appInfo.getName()));
			appNode.addNode(new XmlNode("app_flag",appInfo.getFlag()));
			appNode.addNode(new XmlNode("version",appInfo.getVersion()));
			appNode.addNode(new XmlNode("path",appInfo.getPath()));
			appNode.addNode(new XmlNode("start_bat",appInfo.getStartBat()));
			appNode.addNode(new XmlNode("stop_port",appInfo.getRmiStopPort()+""));
			
			root.addNode(appNode);
		}
		
		StringBuffer buff = new StringBuffer(1024*10);
		buff.append("<?xml version=\"1.0\" encoding=\"GB2312\"?>\n");
		root.toXmlString(buff, "", true);
		
		try{
			byte[] data = buff.toString().getBytes("GB2312");
			FileOutputStream out = new FileOutputStream(this.updateFile);
			out.write(data, 0, data.length);
			out.close();
			
			this.lastModifiedTime = this.updateFile.lastModified();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public class AppInfo{
		private String name = null;
		private String version = null;
		private String path = null;
		private String startBat = null;
		private String flag = null;
		private String rmiStopIp = "localhost";
		private int rmiStopPort = 0;
		
		public AppInfo(String name,String flag,String version,String path,String startBat,int rmiStopPort) throws Exception{
			this.name = name;
			this.flag = flag;
			this.version = version;
			this.path = path;
			this.startBat = startBat;
			this.rmiStopPort = rmiStopPort;
		}
		
		private RemoteExecuteByRmiClient initRemoteCall() throws Exception{
			RemoteExecuteByRmiClient remoteCall = null;
			remoteCall = new RemoteExecuteByRmiClient();
			remoteCall.addPara("ip", this.rmiStopIp);
			remoteCall.addPara("port", ""+this.rmiStopPort);
			remoteCall.addPara("group_name", "app_manage");
			remoteCall.addPara("cls_name", "!"+RmiSystemExitCmd.class.getName());
			remoteCall.addPara("cls_name", "!"+DetectCmd.class.getName());
			if(!remoteCall.startServer()){
				throw new Exception("App Manage Rmi Failure!");
			}
			return remoteCall;
		}
		
		public String getName() {
			return name;
		}
		
		public String getFlag(){
			return this.flag;
		}

		public String getVersion() {
			return version;
		}
		public void updateVersion(String newVersion){
			this.version = newVersion;
		}

		public String getPath() {
			return path;
		}

		public String getStartBat() {
			return startBat;
		}

		public String getRmiStopIp() {
			return rmiStopIp;
		}

		public int getRmiStopPort() {
			return rmiStopPort;
		}

		public boolean startApp(){
			try{
				Runtime.getRuntime().exec(new File(this.path,this.startBat).getAbsolutePath(),null,new File(this.path));
				return true;
			}catch(Exception e){
				e.printStackTrace();
				return false;
			}
		}
		
		public boolean detectExist(){
			try{
				RemoteExecuteByRmiClient remoteCall = this.initRemoteCall();
				if(remoteCall != null){
					if(remoteCall.remoteRmiExecute(DetectCmd.class.getName(), null)!= null){
						return true;
					}
				}
			}catch(Exception e){
				e.printStackTrace();
			}
			return false;
		}
		
		public void stopApp(){
			try{
				RemoteExecuteByRmiClient remoteCall = this.initRemoteCall();
				if(remoteCall != null){
					remoteCall.remoteRmiExecute(RmiSystemExitCmd.class.getName(), null);
				}
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		
		public void restart(){
			try{
				this.stopApp();
				Thread.sleep(3000);
				this.startApp();
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}
	
}
