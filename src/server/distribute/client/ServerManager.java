/**
 * ¾øÃÜ Created on 2009-8-13 by edmund
 */
package server.distribute.client;

import java.io.File;
import java.io.FileOutputStream;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.Hashtable;
import java.util.Iterator;
import com.fleety.base.loader.ClassLoaderFactory;
import com.fleety.util.pool.thread.ITask;
import server.distribute.*;

public class ServerManager{
	public static final String DISTRIBUTE_DIR = "distribute";
	
	private Hashtable serverMapping = new Hashtable();
	
	private ClientServer clientServer = null;
	public ServerManager(ClientServer clientServer){
		this.clientServer = clientServer;
	}
	
	public void destroyAllServer(){
		this.serverMapping.clear();
	}
	
	public void createServer(ServerInfo serverInfo) throws RemoteException{
		AllServerInfo info = new AllServerInfo();
		info.serverInfo = serverInfo;
		info.initServer();
		this.serverMapping.put(serverInfo.getServerName(), info);
	}
	
	public void executeTask(String serverName,TaskInfo taskInfo){
		AllServerInfo serverInfo = (AllServerInfo)this.serverMapping.get(serverName);
		clientServer.addTask(new TaskContainer(serverInfo,taskInfo));
	}
	
	private class AllServerInfo{
		private boolean isOk = false;
		public ServerInfo serverInfo = null;
		public ClassLoader loader = null;
		public Hashtable instanceMapping = new Hashtable();
		
		public void initServer(){
			this.isOk = false;
			
			String serverName = serverInfo.getServerName();
			
			File dir = new File(DISTRIBUTE_DIR+File.separator+serverInfo.getServerName());
			dir.mkdirs();

			try{
				JarInfo jarInfo = null;
				for(Iterator itr = serverInfo.getJarInfoList().iterator();itr.hasNext();){
					jarInfo = (JarInfo)itr.next();
					
					this.downloadJarFile(serverName,jarInfo);
				}
			
				this.loader = ClassLoaderFactory.createClassLoader(null, new File[]{dir}, this.getClass().getClassLoader());
				
				this.isOk = true;
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		
		public TaskServer getThreadTaskServerInstance() throws Exception{
			TaskServer server = (TaskServer)this.instanceMapping.get(Thread.currentThread());
			if(server == null){
				server = this.getServerMainClass();
				this.instanceMapping.put(Thread.currentThread(), server);
			}
			return server;
		}
		
		private void downloadJarFile(String serverName,JarInfo jarInfo) throws Exception{
			File jarFile;
			jarFile = new File(DISTRIBUTE_DIR+File.separator+serverName+File.separator+jarInfo.name);
			
			boolean needDownload = true;
			if(jarFile.exists()){
				if(jarFile.lastModified() == jarInfo.modifyTime && jarFile.length() == jarInfo.size){
					needDownload = false;
				}
			}
			if(needDownload){
				IDistribute serverRmi = clientServer.getServerRmi();
				
				byte[] data = serverRmi.s_requestJar(jarInfo);
				FileOutputStream out = new FileOutputStream(jarFile);
				out.write(data);
				out.close();
			}
		}
		
		public TaskServer getServerMainClass() throws Exception{
			try{
				TaskServer server = (TaskServer)Class.forName(serverInfo.getMainClass(),true,this.loader).newInstance();
				serverInfo.appendPara(server);
				return server;
			}catch(Exception e){
				this.initServer();
				throw e;
			}
		}
		
		public boolean isOk(){
			return this.isOk;
		}
	}
	
	public class TaskContainer implements ITask{
		private AllServerInfo serverInfo;
		private TaskInfo taskInfo;
		public TaskContainer(AllServerInfo serverInfo,TaskInfo taskInfo){
			this.serverInfo = serverInfo;
			this.taskInfo = taskInfo;
		}
		
		public boolean execute() throws Exception{
			boolean isSuccess = true;
			if(this.serverInfo == null){
				isSuccess = false;
			}
			
			ResultInfo resultInfo = null;
			try{
				if(isSuccess){
					TaskServer server = this.serverInfo.getThreadTaskServerInstance();
					server.setTaskInfo(this.taskInfo);
					isSuccess = server.startTask();
					resultInfo = server.getTaskResult();
				}
			}catch(Exception e){
				e.printStackTrace();
				isSuccess = false;
			}
			
			IDistribute serverRmi = clientServer.getServerRmi();
			if(resultInfo == null){
				resultInfo = new ResultInfo();
			}
			resultInfo.setIsSuccess(isSuccess);
			resultInfo.setResultInfoObj((Serializable)this.taskInfo.getInfo(TaskInfo.RESULT_EXTRA_RETURN_INFO_FLAG));
			taskInfo = serverRmi.s_taskFinish(clientServer.getGuid(), this.serverInfo.serverInfo.getServerName(), taskInfo.getId(), resultInfo);
			if(taskInfo != null){
				ServerManager.this.executeTask(taskInfo.getServerName(),taskInfo);
			}
			return isSuccess;
		}
		public String getDesc(){
			return null;
		}
		public Object getFlag(){
			return null;
		}
	}
}
