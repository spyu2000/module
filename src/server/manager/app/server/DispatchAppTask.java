package server.manager.app.server;

import java.util.Iterator;
import java.util.List;

import server.manager.app.server.ServerManagerRmiInterface.AppInfo;
import server.manager.app.server.ServerManagerRmiInterface.ClientInfo;
import server.manager.app.server.ServerManagerRmiInterface.AppInfo.FileInfo;

import com.fleety.util.pool.thread.ITask;

public class DispatchAppTask implements ITask {
	private ClientInfo[] clientInfoArr;
	private AppInfo appInfo;
	private List fileInfoList;
	public DispatchAppTask(ClientInfo[] clientInfoArr,AppInfo appInfo,List fileInfoList){
		this.clientInfoArr = clientInfoArr;
		this.appInfo = appInfo;
		this.fileInfoList = fileInfoList;
	}

	public boolean execute() throws Exception {
		ClientInfo clientInfo;
		FileInfo fileInfo;
		for(int i=0;i<this.clientInfoArr.length;i++){
			clientInfo = this.clientInfoArr[i];
			if(!appInfo.containOwner(clientInfo.guid)){
				continue;
			}
			try{
				clientInfo.clientRmi.startDispatchApplication(appInfo.appName, appInfo.execFile, appInfo.startCmd, appInfo.stopCmd);
				
				for(Iterator itr = fileInfoList.iterator();itr.hasNext();){
					fileInfo = (FileInfo)itr.next();
					
					clientInfo.clientRmi.fileChanged(appInfo.appName, fileInfo.filePath, fileInfo.fileSize, fileInfo.lastModified);
				}
				
				clientInfo.clientRmi.endDispatchApplication(appInfo.appName);
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		
		return true;
	}

	public String getDesc() {
		return null;
	}

	public Object getFlag() {
		return null;
	}

}
