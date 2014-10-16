package server.manager.app.autoupdate.server;

import java.io.File;
import java.util.HashMap;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.fleety.base.Util;
import com.fleety.base.xml.XmlParser;

import server.socket.serversocket.FleetySocketServer;

public class AutoUpdateServer extends FleetySocketServer {
	private File updateFile = null;
	public boolean startServer() {
		this.updateFile = new File(this.getStringPara("update_file_path"));
		if(!this.updateFile.exists()){
			return false;
		}
		
		this.loadUpdateInfo();
		
		this.addPara("reader", "server.socket.help.ObjectReader");
		this.addPara("releaser", "server.manager.app.autoupdate.server.CmdReleaser");
		return super.startServer();
	}

	private HashMap appMapping = new HashMap();
	private long lastModifiedTime = 0;
	public void loadUpdateInfo(){
		if(this.lastModifiedTime == this.updateFile.lastModified()){
			return ;
		}
		this.lastModifiedTime = this.updateFile.lastModified();
		
		HashMap tempMapping = new HashMap();
		try{
			Document document = XmlParser.parse(this.updateFile);
			if(document == null){
				return ;
			}
			
			String appName,version;
			String tempStr;
			byte[] appData;
			AppInfo appInfo;
			Element root = document.getDocumentElement();
			Node[] appNodeArr = Util.getElementsByTagName(root, "app");
			for(int i=0;i<appNodeArr.length;i++){
				tempStr = Util.getNodeText(Util.getSingleElementByTagName(appNodeArr[i], "enable"));
				if(tempStr != null && tempStr.trim().equalsIgnoreCase("false")){
					continue;
				}
				appName = Util.getNodeText(Util.getSingleElementByTagName(appNodeArr[i], "app_name"));
				if(appName == null || appName.trim().length() == 0){
					continue;
				}
				appName = appName.trim();
				
				version = Util.getNodeText(Util.getSingleElementByTagName(appNodeArr[i], "version"));
				if(version == null){
					continue;
				}
				version = version.trim();
				
				tempStr = Util.getNodeText(Util.getSingleElementByTagName(appNodeArr[i], "path"));
				appData = Util.readFile(new File(tempStr));
				if(appData == null){
					continue;
				}
				
				tempMapping.put(appName, appInfo = new AppInfo(appName,version,appData));

				System.out.println("Manage App:name="+appInfo.getName()+" version="+appInfo.getVersion()+" size="+appInfo.getData().length);
			}
			this.appMapping = tempMapping;
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public AppInfo getAppInfo(String appName){
		return (AppInfo)this.appMapping.get(appName);
	}
	
	public class AppInfo{
		private String name = null;
		private String version = null; 
		private byte[] data = null;
		
		public AppInfo(String name,String version,byte[] data){
			this.name = name;
			this.version = version;
			this.data = data;
		}

		public String getName() {
			return name;
		}

		public String getVersion() {
			return version;
		}

		public byte[] getData() {
			return data;
		}
		
		public boolean needUpdate(String version){
			if(version == null){
				return true;
			}
			
			return !this.version.equals(version);
		}
	}
}
