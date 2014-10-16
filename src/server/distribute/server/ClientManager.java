/**
 * 绝密 Created on 2009-8-11 by edmund
 */
package server.distribute.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import com.fleety.base.Util;
import com.fleety.base.loader.StandardClassLoader;
import com.fleety.base.xml.XmlParser;
import com.fleety.util.pool.thread.ITask;
import com.fleety.util.pool.thread.ThreadPool;
import com.fleety.util.pool.timer.FleetyTimerTask;

import server.distribute.*;
import server.threadgroup.PoolInfo;
import server.threadgroup.ThreadPoolGroupServer;

public class ClientManager extends FleetyTimerTask{
	private Hashtable clientMapping = new Hashtable(16);
	private long maxInterval = 10*60*1000l;
	
	private Hashtable serverMapping = new Hashtable(32);
	private TaskContainer taskContainer = null;
	
	private String poolName = null;
	private ThreadPool taskPool = null;
	public ClientManager(){
		ThreadPoolGroupServer.getSingleInstance().createTimerPool(ClientManager.class.getName()+"["+this.hashCode()+"]").schedule(this, 60000, 60000);
		this.taskContainer = new TaskContainer(this);
		
		this.poolName = ClientManager.class.getName()+"["+this.hashCode()+"]";
        try{
        	PoolInfo pInfo = new PoolInfo();
        	pInfo.poolType = ThreadPool.SINGLE_TASK_LIST_POOL;
        	pInfo.taskCapacity = 1000;
        	pInfo.workersNumber = 8;
        	pInfo.priority = Thread.MAX_PRIORITY;
        	this.taskPool = ThreadPoolGroupServer.getSingleInstance().createThreadPool(this.poolName, pInfo);
        }catch(Exception e){
        	e.printStackTrace();
        }
	}
	
	public void run(){
		Object[] keys = null;
		synchronized(this.clientMapping){
			keys = this.clientMapping.keySet().toArray();
		}
		ClientInfo info;
		for(int i=0;i<keys.length;i++){
			info = (ClientInfo)this.clientMapping.get(keys[i]);
			if(System.currentTimeMillis() - info.lastHeartTime > maxInterval){
				this.clientMapping.remove(keys[i]);
			}
		}
	}
	
	public void newClientConnect(String guid,int concurrentNum,IDistribute client){
		ClientInfo info = new ClientInfo();
		
		info.guid = guid;
		info.maxTaskNum = concurrentNum;
		
		try{
			info.clientRmi = client;
			
			AllServerInfo[] infos = new AllServerInfo[this.serverMapping.size()];
			this.serverMapping.values().toArray(infos);
			ServerInfo[] sinfos = new ServerInfo[infos.length];
			for(int i=0;i<infos.length;i++){
				sinfos[i] = infos[i].serverInfo;
			}
			info.clientRmi.c_dispatchServer(sinfos);
			
			this.clientMapping.put(guid, info);
				
			System.out.println("新连接:"+info.guid);
			
			this.taskContainer.dispatchTask();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public void addTask(String serverName,TaskInfo[] taskArr){
		for(int i=0;i<taskArr.length;i++){
			taskArr[i].setServerName(serverName);
			this.taskContainer.addTask(taskArr[i]);
		}
		this.taskContainer.dispatchTask();
	}
	
	public boolean heartConnect(String guid,int curTaskNum){
		ClientInfo info = null;
		synchronized(this.clientMapping){
			info = (ClientInfo)this.clientMapping.get(guid);
		}
		if(info == null){
			return false;
		}
		System.out.println(guid+"心跳:执行中任务数:"+curTaskNum+" 服务器计算(当前/最大):"+info.curTaskNum+"/"+info.maxTaskNum);
		info.lastHeartTime = System.currentTimeMillis();
//		info.curTaskNum = curTaskNum;
		
		return true;
	}
	
	public byte[] loadJarInfo(JarInfo jarInfo){
		File f = new File(jarInfo.jarPath);
		InputStream in = null;
		try{
			in = new FileInputStream(f);
			byte[] data = new byte[(int)f.length()];
			in.read(data);
			return data;
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			try{
				if(in != null){
					in.close();
				}
			}catch(Exception e){}
		}
		return null;
	}
	
	public TaskInfo taskFinished(String guid,String serverName,long taskId,ResultInfo resultInfo){
		TaskInfo taskInfo = this.taskContainer.changeTaskStatus(taskId, resultInfo.isSuccess()?TaskInfo.FINISHED_STATUS:TaskInfo.CREATE_STATUS);
		
		if(taskInfo != null){
			AllServerInfo allInfo = (AllServerInfo)this.serverMapping.get(serverName);
			allInfo.server.taskStatusChanged(taskInfo, resultInfo);
		}
		
		ClientInfo cinfo = (ClientInfo)this.clientMapping.get(guid);
		if(cinfo == null){
			return null;
		}
		
		taskInfo = this.taskContainer.releaseTask();
		if(taskInfo == null){
			cinfo.addCurTaskNum(-1);
		}
		
		return taskInfo;
	}
	
	protected void dispatchTask(LinkedList taskList){
		ClientInfo cinfo;
		Object[] clientArr = null;
		synchronized(this.clientMapping){
			clientArr = this.clientMapping.values().toArray();
		}
		for(int i = 0;i < clientArr.length;i++){
			cinfo = (ClientInfo)clientArr[i];
			try{
				cinfo.dispatchTask(taskList);
			}catch(Exception e){
				e.printStackTrace();
				this.removeClient(cinfo.guid);
			}
		}
	}
	private void removeClient(String guid){
		this.clientMapping.remove(guid);
	}

	
	protected boolean initServer(String path){
		InputStream in = null;
		try{
			Element root = XmlParser.parse(in = new FileInputStream(path));
			
			Node[] serverNode = Util.getElementsByTagName(root, "server");
			for(int i=0;i<serverNode.length;i++){
				this.loadServerByNode(serverNode[i]);
			}
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}finally{
			try{
				if(in != null){
					in.close();
				}
			}catch(Exception e){
				
			}
		}
		return true;
	}
	
	private void loadServerByNode(Node serverNode){
    	String depends = Util.getNodeAttr(serverNode, "depends");
    	String serverName = Util.getNodeText(Util.getSingleElementByTagName(serverNode, "server_name"));
    	String className = Util.getNodeText(Util.getSingleElementByTagName(serverNode, "class_name"));
    	String createMethod = Util.getNodeText(Util.getSingleElementByTagName(serverNode, "create_method"));
    	String enableServer = Util.getNodeText(Util.getSingleElementByTagName(serverNode, "enable_server"));
    	
    	if(enableServer != null && enableServer.equalsIgnoreCase("false")){
    		System.out.println("服务【"+serverName+"】被禁用!");
    		return;
    	}
    	if(depends != null){
    		String[] allDepends = depends.trim().split(",");
    		int num = allDepends.length;
    		TaskServer dependServer;
    		String dependServerName;
    		for(int i=0;i<num;i++){
    			dependServerName = allDepends[i].trim();
    			if(dependServerName.length() == 0){
    				continue;
    			}
    			dependServer = (TaskServer)this.serverMapping.get(dependServerName);
    			if(dependServer == null || !dependServer.isRunning()){
    				System.out.println("服务【"+serverName+"】启动失败!所依赖的服务【"+dependServerName+"】不存在或未启动!");
    			}
    		}
    	}
    	
    	try{
    		AllServerInfo allInfo = new AllServerInfo();
    		ServerInfo serverInfo = allInfo.serverInfo = new ServerInfo();
    		serverInfo.setServerName(serverName);
    		serverInfo.setMainClass(className);
    		
    		String jarStr = Util.getNodeText(Util.getSingleElementByTagName(serverNode, "jar"));
    		if(jarStr == null){
    			throw new Exception("缺少jar!");
    		}
    		String[] jarArr = jarStr.split(";");
    		File f = null;
    		JarInfo jarInfo;
    		List urlList = new ArrayList(16);
    		for(int i=0;i<jarArr.length;i++){
    			f = new File(jarArr[i]);
    			if(f.exists() && f.isFile()){
    				jarInfo = new JarInfo(serverName,f.getName(),f.getAbsolutePath(),f.lastModified(),f.length());
    				serverInfo.addJarInfo(jarInfo);
    				urlList.add(f.toURL());
    			}
    		}

    		URL[] urls = new URL[urlList.size()];
    		urlList.toArray(urls);
    		allInfo.loader = new StandardClassLoader(urls, this.getClass().getClassLoader());
    		
    		Class cls = Class.forName(className,true,allInfo.loader);
    		TaskServer server = null;
    		if(createMethod == null || createMethod.trim().length() == 0){
    			server = (TaskServer)cls.newInstance();
    		}else{
    			Method method = cls.getMethod(createMethod, new Class[0]);
    			server = (TaskServer)method.invoke(null, new Object[0]);
    		}
    		server.setClientManager(this);
    		allInfo.server = server;
    		
    		//为服务类设定参数信息,具体的参数使用，则由服务类内部使用
    		Node[] paraNodeArr = Util.getElementsByTagName(serverNode, "para");
    		int paraNum = paraNodeArr.length;
    		String key,value;
    		for(int i=0;i<paraNum;i++){
    			key = Util.getNodeAttr(paraNodeArr[i], "key");
    			value = Util.getNodeAttr(paraNodeArr[i], "value");
    			if(key == null || value == null){
    				continue;
    			}
    			serverInfo.addPara(key.trim(), value.trim());
    		}
    		serverInfo.appendPara(server);
    		server.setServerName(serverName);
    		
    		//启动服务
    		if(!server.startServer()){
    			server.stopServer();
    			throw new Exception("false");
    		}

    		this.serverMapping.put(serverName, allInfo);
    		System.out.println("服务【"+serverName+"】启动成功!");
    	}catch(Exception e){
    		e.printStackTrace();
    		System.err.println("服务【"+serverName+"】启动失败!");
    	}
    }

	private class AllServerInfo{
		public TaskServer server = null;
		public ServerInfo serverInfo = null;
		public ClassLoader loader = null;
		public long timeout = 10000l;
	}
	
	private class ClientInfo{
		public String guid = null;
		public int maxTaskNum = 0;
		private int curTaskNum = 0;
		public long lastHeartTime = System.currentTimeMillis();
		public String rmiIp;
		public int rmiPort;
		
		public IDistribute clientRmi = null;
		
		public int dispatchTask(LinkedList taskList) throws Exception{
			int num = 0;
			if(this.clientRmi != null){
				int tempNum = Math.min(taskList.size(),this.maxTaskNum-this.curTaskNum);

				if(tempNum > 0){
					AllServerInfo server;
					TaskInfo[] infos = new TaskInfo[tempNum];
					int index = 0;
					synchronized(taskList){
						for(Iterator itr = taskList.iterator();itr.hasNext() && index < tempNum; index++){
							infos[index] = (TaskInfo)itr.next();
							itr.remove();
							
							server = (AllServerInfo)serverMapping.get(infos[index].getServerName());
							taskContainer.taskRealeased(infos[index]);
							server.server.taskStatusChanged(infos[index], null);
						}
					}
					
					num = tempNum;
					this.addCurTaskNum(num);
					ClientManager.this.taskPool.addTask(new ExecTaskInfo(this,infos));
				}
			}
			
			return num;
		}
		
		public synchronized void addCurTaskNum(int offset){
			this.curTaskNum += offset;
			if(this.curTaskNum < 0){
				this.curTaskNum = 0;
			}
		}
	}
	
	public class ExecTaskInfo implements ITask{
		private ClientInfo cinfo = null;
		private TaskInfo[] infos = null;
		public ExecTaskInfo(ClientInfo cinfo,TaskInfo[] infos){
			this.cinfo = cinfo;
			this.infos = infos;
		}
		public String getDesc(){
			return null;
		}
		public Object getFlag(){
			return null;
		}
		
		public boolean execute(){
			try{
				this.cinfo.clientRmi.c_dispatchTask(infos);
			}catch(Exception e){
				e.printStackTrace();
				ClientManager.this.removeClient(cinfo.guid);
			}
			return true;
		}
	}
}
