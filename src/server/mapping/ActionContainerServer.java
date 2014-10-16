/**
 * ���� Created on 2008-1-14 by edmund
 */
package server.mapping;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import server.threadgroup.PoolInfo;
import server.threadgroup.ThreadPoolGroupServer;

import com.fleety.base.InfoContainer;
import com.fleety.base.Util;
import com.fleety.server.BasicServer;
import com.fleety.util.pool.thread.ITask;
import com.fleety.util.pool.thread.ThreadPool;

public class ActionContainerServer extends BasicServer{	
	private String ALL_MSG_FLAG = "all_msg";
	private String MSG_NODE_NAME = "msg";
	private String DEFAULT_ACTION_FLAG = "default_action";
	
	public static final String ASYNC_THREAD_NUM_FLAG = "async_thread_num";
	public static final String CFG_PATH_KEY = "action_path";
	public static final String MULTIPLE_LIST = "multiple_list";
	public static final String TASK_CAPACITY = "task_capacity";
	public static final String PRINT_EXEC_TIME = "print_exec_time";
	
	
	private InfoFileObject[] cfgFile = null;
	private HashMap redirectMapping = null;
	private HashMap actionMapping = null;
	private HashMap defaultActionMapping = null;
	private HashMap appendInfoMapping = null;
	private String poolName = null;
	private ThreadPool threadPool = null;
	private boolean isPrintExecTime = true;
	
	/**
	 * ִ��ָ����Ϣ�ĵ�ȫ����Ϊ,��Ӧ��Ϊ
	 * ���ĳ����Ϣ���ص���,��ô���л����ص�����Ϣ��ִ�и���Ϣ.�ص�����Ϣֻ�ܵ��򵽾߱�������Ϊ����Ϣ.
	 * Ĭ����Ϊ,���ĳ����Ϣ�����ҵ��κο�ִ�е���Ϊ,��ô������ִ������Ϣ�����Ĭ����Ϊ����ȫ�ֵ�Ĭ����Ϣ��Ϊ,
	 */
	public ActionContainerServer(){
		
	}
	
	public void SetPrintExecTime(boolean isPrintExecTime){
		this.isPrintExecTime = isPrintExecTime;
	}
	
	public ActionContainerServer(String allMsgFlag,String msgNodeName){
		this.ALL_MSG_FLAG = allMsgFlag;
		this.MSG_NODE_NAME = msgNodeName;
	}
	public InfoContainer getAppendInfo(String msgName) {
		InfoContainer info=null;
        if(appendInfoMapping!=null)
          info=(InfoContainer)appendInfoMapping.get(msgName);
        return info;   
    }
	public List getAllAction(String msg){
		if(this.actionMapping == null){
			return null;
		}
		
		RedirectInfo rmsg = (RedirectInfo)this.redirectMapping.get(msg);
		if(rmsg != null){
			msg = rmsg.redirectName;
		}
		
		List allAction = new LinkedList();
		
		List tempList = (List)this.actionMapping.get(ALL_MSG_FLAG);
		if(tempList != null){
			allAction.addAll(tempList);
		}
		tempList = (List)this.actionMapping.get(msg);
		if(tempList != null){
			allAction.addAll(tempList);
		}
		tempList = (List)this.defaultActionMapping.get(msg);
		if(tempList != null){
			allAction.addAll(tempList);
		}
		tempList = (List)this.defaultActionMapping.get(ALL_MSG_FLAG);
		if(tempList != null){
			allAction.addAll(tempList);
		}

		return allAction;
	}
	
	/**
	 * ��ִ��������Ϣ�ϵķ�Ĭ����Ϊ,��ִ�ж�Ӧ��Ϣ�ϵķ�Ĭ����Ϊ
	 * �����������Ϊִ��,��ô���ִ��Ĭ����Ϊ.Ĭ����Ϊִֻ��һ��
	 * ���Ҷ�Ӧ��Ϣ�ϵ�Ĭ����Ϊ,����������Ϣ�ϵ�Ĭ����Ϊ.
	 * @param infos
	 */
	public void executeTask(InfoContainer infos){
		if(this.actionMapping == null){
			System.out.println("����["+this.getServerName()+"]δ����!");
			return ;
		}
		this.loadFromXml();
		
		long t = System.currentTimeMillis();

		String oriMsg = infos.getString(IAction.MSG_FLAG);
		String msg = oriMsg;
		RedirectInfo rmsg;
		StringBuffer strBuff = null;
		while((rmsg = (RedirectInfo)this.redirectMapping.get(msg)) != null){
			msg = rmsg.redirectName;
			if(rmsg.appendInfo != null){
				infos.addAll(rmsg.appendInfo);
			}
			if(strBuff == null){
				strBuff = new StringBuffer("��Ϣ�ص�:");
				strBuff.append(oriMsg);
			}
			strBuff.append(" -> "+msg);
		}
		if(this.isPrintExecTime && strBuff != null){
			System.out.println(strBuff);
		}
		
		int actionNum = 0;
		List actionList = null;
		
		//ִ��ȫ����Ϣע�����
		actionList = (List)this.actionMapping.get(ALL_MSG_FLAG);
		int returnFlag = this.executeAction(actionList,infos,msg,oriMsg); 
		if(returnFlag == BREAK_FLAG){
			if(this.isPrintExecTime){
				System.out.println("��Ϣ["+oriMsg+"]ִ��ʱ��:"+(System.currentTimeMillis() - t));
			}
			return ;
		}
		actionNum += returnFlag;

		//ִ�ж�Ӧ��Ϣע�����
		actionList = (List)this.actionMapping.get(msg);
		returnFlag = this.executeAction(actionList,infos,msg,oriMsg);
		if(returnFlag == BREAK_FLAG){
			if(this.isPrintExecTime){
				System.out.println("��Ϣ["+oriMsg+"]ִ��ʱ��:"+(System.currentTimeMillis() - t));
			}
			return ;
		}
		actionNum += returnFlag;
		
		//�����ִ����Ϊ,ִ��Ĭ����Ϊ
		if(actionNum == 0){
			actionList = (List)this.defaultActionMapping.get(msg);
			if(actionList == null || actionList.size() == 0){
				actionList = (List)this.defaultActionMapping.get(ALL_MSG_FLAG);
			}
			this.executeAction(actionList,infos,msg,oriMsg);
		}

		if(this.isPrintExecTime){
			System.out.println("��Ϣ["+oriMsg+"]ִ��ʱ��:"+(System.currentTimeMillis() - t));
		}
	}

	private static final int BREAK_FLAG = -1;
	/**
	 * ִ�ж��е���Ϊ�б�
	 * @param actionList
	 * @param info
	 * @param msg
	 * @return ���Ϊ-1,����Ӧ����������ִ��.���Ϊ����,����ǰ��ִ�е���Ϊ��.
	 */
	private int executeAction(List actionList,InfoContainer info,String msg,String oriMsg){
		if(actionList == null){
			return 0;
		}
		
		int actionNum = 0;
		IAction action = null;
		for(Iterator actionIterator = actionList.iterator();actionIterator.hasNext();){
			action = (IAction)actionIterator.next();
			try{
				if(!action.isInclude(msg)){
					continue;
				}
				if(action.isFilter()){
					if(!action.execute(info)){
						return BREAK_FLAG;
					}
				}else{
					actionNum++;
					if(action.isSynchronized()){
						action.execute(info);
					}else{
						//�첽ִ�У���Ҫһ���̳߳�����
						if(this.threadPool != null){
							this.threadPool.addTask(new WorkTask(action,info));
						}
					}
				}
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		return actionNum;
	}
	
	public boolean startServer(){
		int threadNum = 1;
		int taskCapacity = 1000;
		
		String tempStr;
		try{
			tempStr = this.getStringPara(ASYNC_THREAD_NUM_FLAG);
			if(tempStr != null && tempStr.trim().length() > 0){
				threadNum = Integer.parseInt(tempStr.trim());
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		try{
			tempStr = this.getStringPara(TASK_CAPACITY);
			if(tempStr != null && tempStr.trim().length() > 0){
				taskCapacity = Integer.parseInt(tempStr.trim());
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		
		try{
			PoolInfo pInfo = new PoolInfo();
			pInfo.taskCapacity = taskCapacity;
			pInfo.workersNumber = threadNum;
			tempStr = this.getStringPara(MULTIPLE_LIST);
			if(tempStr != null && tempStr.trim().equalsIgnoreCase("true")){
				pInfo.poolType = ThreadPool.MULTIPLE_TASK_LIST_POOL;
			}else{
				pInfo.poolType = ThreadPool.SINGLE_TASK_LIST_POOL;
			}
			
			this.poolName = this.getServerName()+"["+this.hashCode()+"]";
			this.threadPool = ThreadPoolGroupServer.getSingleInstance().createThreadPool(poolName, pInfo);
		}catch(Exception e){
			e.printStackTrace();
			System.out.println("����["+this.getServerName()+"]���̳߳س�ʼ��ʧ��!");
			return false;
		}
		tempStr = this.getStringPara(PRINT_EXEC_TIME);
		if(tempStr != null && tempStr.trim().equalsIgnoreCase("false")){
			this.isPrintExecTime = false;
		}else{
			this.isPrintExecTime = true;
		}
		
		Object cfgPath = this.getPara(CFG_PATH_KEY);
		if(cfgPath == null){
			System.out.println("����������ļ�·��!");
			return false;
		}
		if(cfgPath instanceof List){
			this.cfgFile = new InfoFileObject[((List)cfgPath).size()];
			int i=0;
			for(Iterator itr = ((List)cfgPath).iterator();itr.hasNext();){
				this.cfgFile[i++] = this.createObject(itr.next().toString());
			}
		}else{
			this.cfgFile = new InfoFileObject[]{this.createObject(cfgPath.toString())};
		}
		this.lastLoadTime = new long[this.cfgFile.length];
		
		boolean isSuccess =  this.loadFromXml();
		
		this.isRunning = isSuccess;
		
		return isSuccess;
	}
	
	private InfoFileObject createObject(String path){
		try{
			if(path.toUpperCase().startsWith("HTTP")){
				return new InfoFileObject(new URL(path));
			}else if(path.startsWith("!")){
				return new InfoFileObject(this.getClass().getClassLoader().getResource(path.substring(1)));
			}else{
				return new InfoFileObject(new File(path));
			}
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}
	
	private long[] lastLoadTime;
	private boolean loadFromXml(){
		if(this.cfgFile == null || this.cfgFile.length == 0){
			return false;
		}
		boolean isChanged = false;
		long lastModifyTime;
		for(int i=0;i<this.cfgFile.length;i++){
			lastModifyTime = this.cfgFile[i].lastModified();
			if(lastModifyTime > 0){
				isChanged |= (lastModifyTime != this.lastLoadTime[i]);
			}else{
				isChanged |= (this.lastLoadTime[i] == 0);
			}
		}
		if(!isChanged){
			return true;
		}
		
		HashMap newMapping = new HashMap();
		HashMap defaultNewMapping = new HashMap();
        HashMap newAppendInfoMapping = new HashMap();
        HashMap newRedirectMapping = new HashMap();
		
        boolean isSuccess = true;
        for(int i=0;i<this.cfgFile.length;i++){
        	this.lastLoadTime[i] = cfgFile[i].lastModified();
        	
        	if(isSuccess){
        		isSuccess &= this.loadFromXml(this.cfgFile[i], newMapping, defaultNewMapping, newAppendInfoMapping, newRedirectMapping);
        	}
        }

		if(isSuccess){
			this.actionMapping = newMapping;
			this.defaultActionMapping = defaultNewMapping;
			this.appendInfoMapping = newAppendInfoMapping;
			this.redirectMapping = newRedirectMapping;
		}
		
		return true;
	}
	
	private boolean loadFromXml(InfoFileObject xmlFile,HashMap newMapping,HashMap defaultNewMapping,HashMap newAppendInfoMapping,HashMap newRedirectMapping){
		if(!xmlFile.exists()){
			return true;
		}
		try{
			//���������ļ�
			DocumentBuilderFactory domfac=DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = domfac.newDocumentBuilder();
			Document document = builder.parse(xmlFile.getInputStream());
			
			Element root = document.getDocumentElement();
			NodeList allMsgNodeList = root.getElementsByTagName(MSG_NODE_NAME);
			int msgNum = allMsgNodeList.getLength();
			Element msgNode;
			String msgName,redirectName;
			ArrayList actionList = null,defaultActionList = null;
			for(int i=0;i<msgNum;i++){
				msgNode = (Element)allMsgNodeList.item(i);

				msgName = Util.getNodeAttr(msgNode, "name");
				if(msgName == null || msgName.trim().length()==0){
					continue;
				}
				msgName = msgName.trim();
				
				redirectName = Util.getNodeAttr(msgNode, "redirect");
				if(redirectName != null &&redirectName.trim().length() > 0){
					RedirectInfo rInfo = new RedirectInfo();
					rInfo.redirectName = redirectName.trim();
					Node[] paraArr = Util.getSonElementsByTagName(msgNode, "para");
					if(paraArr != null){
						for(int p=0;p<paraArr.length;p++){
							if(rInfo.appendInfo == null){
								rInfo.appendInfo = new InfoContainer();
							}
							rInfo.appendInfo.setInfo(Util.getNodeAttr(paraArr[p], "key"), Util.getNodeAttr(paraArr[p], "value"));
						}
					}
					newRedirectMapping.put(msgName, rInfo);
					continue;
				}
				
				actionList = new ArrayList();
				defaultActionList = new ArrayList();  
				this.loadActionFromXml(msgNode.getChildNodes(),actionList,defaultActionList);
				if(actionList.size() > 0){
					actionList.trimToSize();
					newMapping.put(msgName, actionList);
				}  
				if(defaultActionList.size() > 0){
					actionList.trimToSize();
					defaultNewMapping.put(msgName, defaultActionList);
				}  

                InfoContainer info=new InfoContainer();
                newAppendInfoMapping.put(msgName,info);
				Node[] nodes=Util.getAllAttrNode(msgNode);
				for(int k=0;k<nodes.length;k++){
					if(nodes[k].getNodeName()==null||nodes[k].getNodeName().equals(""))
						continue;
	                info.setInfo(nodes[k].getNodeName(),nodes[k].getNodeValue());
				}
                Node[] paraArr = Util.getSonElementsByTagName(msgNode, "para");
                Node paraNode;
                int paraNum = paraArr.length;
                for(int j=0;j<paraNum;j++){
                    paraNode = paraArr[j];
                    info.setInfo(Util.getNodeAttr(paraNode, "key"), Util.getNodeAttr(paraNode, "value"));
                }
			}
		}catch(Exception e){
			e.printStackTrace();
			System.out.println("����ӳ�����["+this.getServerName()+"]�������ļ�["+xmlFile+"]ʧ��!");
			return false;
		}
		System.out.println("����ӳ�����["+this.getServerName()+"]�������ļ�["+xmlFile+"]�ɹ�!");
		return true;
	}
	
	private void loadActionFromXml(NodeList actionNodeList,List actionList,List defaultActionList) throws Exception{
		int num = actionNodeList.getLength();
		Node actionNode ;
		String tagName,name,synch,className;
		IAction action;
		String enable,defaultAction;
		for(int i=0;i<num;i++){
			actionNode = actionNodeList.item(i);
			if(actionNode.getNodeType() != Node.ELEMENT_NODE){
				continue;
			}
			
			tagName = ((Element)actionNode).getTagName();
			if(tagName == null){
				continue;
			}
			tagName = tagName.trim();
			if(!tagName.equalsIgnoreCase("filter") && ! tagName.equalsIgnoreCase("action")){
				continue;
			}
			name= Util.getNodeAttr(actionNode, "name");
			synch= Util.getNodeAttr(actionNode, "sync");
			defaultAction = Util.getNodeAttr(actionNode, DEFAULT_ACTION_FLAG);
			
			className = Util.getNodeText(Util.getSingleElementByTagName(actionNode, "class_name")).trim();
			enable = Util.getNodeText(Util.getSingleElementByTagName(actionNode, "enable"));
			if(enable != null  && enable.equalsIgnoreCase("false")){
				System.out.println("��Ϊ��"+name+"��������!");
				continue;
			}
			String createMethod = Util.getNodeText(Util.getSingleElementByTagName(actionNode, "create_method"));
			try{
				Class cls = Class.forName(className);
				if(createMethod == null || createMethod.trim().length() == 0){
					action = (IAction)cls.newInstance();
				}else{
					Method method = cls.getMethod(createMethod, new Class[0]);
					action = (IAction)method.invoke(null, new Object[0]);
				}
			}catch(Exception e){
				System.out.println("��("+className+")ʵ�����쳣:"+e);
				continue;
			}
			
			action.addPara(IAction._NAME_FLAG, name);
			if(synch  == null || synch.equalsIgnoreCase("true")){
				action.addPara(IAction._SYNC_FLAG, Boolean.TRUE);
			}else{
				action.addPara(IAction._SYNC_FLAG, Boolean.FALSE);
			}
			if(tagName.equalsIgnoreCase("filter")){
				action.addPara(IAction._FILTER_FLAG, Boolean.TRUE);
			}else{
				action.addPara(IAction._FILTER_FLAG, Boolean.FALSE);
			}
			
			Node[] paraArr = Util.getElementsByTagName(actionNode, "para");
			Node paraNode;
			int paraNum = paraArr.length;
			for(int j=0;j<paraNum;j++){
				paraNode = paraArr[j];
				action.addPara(Util.getNodeAttr(paraNode, "key"), Util.getNodeAttr(paraNode, "value"));
			}
			action.addPara(IAction._ACTION_CONTAINER_FLAG, this);

			try{
				action.init();
			}catch(Exception e){
				e.printStackTrace();
				System.out.println("��Ϊ�ĳ�ʼ�����������쳣��������ӳ��!");
				continue;
			}

			//�˾���������Ĭ����Ϊ,ֻ����Ϊ���п�����Ĭ����Ϊ.
			if(action.isFilter()){
				actionList.add(action);
			}else{
				if(defaultAction != null && defaultAction.equals("true")){
					defaultActionList.add(action);
				}else{
					actionList.add(action);
				}
			}
		}
	}
	
	public void stopServer(){
		this.actionMapping = null;
		
		ThreadPoolGroupServer.getSingleInstance().removeThreadPool(this.poolName);
		if(this.threadPool != null){
			this.threadPool.stopWork();
		}
		this.threadPool = null;
		
		this.isRunning = false;
	}
	
	public String getInfoDesc(){
		if(!this.isRunning()){
			return "������ֹͣ!";
		}
		StringBuffer infoDesc = new StringBuffer(128);
		if(this.threadPool.getPoolType() == ThreadPool.SINGLE_TASK_LIST_POOL){
			infoDesc.append("threadNum=");
			infoDesc.append(this.threadPool.getThreadNum());
			infoDesc.append(";");
			infoDesc.append("capacity=");
			infoDesc.append(this.threadPool.getTaskNum(0));
		}else{
			infoDesc.append("threadNum=");
			infoDesc.append(this.threadPool.getThreadNum());
			infoDesc.append(";");
			infoDesc.append("capacity=");
			infoDesc.append("[");
			for(int i=0;i<this.threadPool.getThreadNum();i++){
				if(i>0){
					infoDesc.append(",");
				}
				infoDesc.append(this.threadPool.getTaskNum(i));
			}
			infoDesc.append("]");
			infoDesc.append(";totalTaskNum=");
			infoDesc.append(this.threadPool.getTotalTaskNum());
		}
		return infoDesc.toString();
	}
	
	private class RedirectInfo{
		public String redirectName = null;
		public InfoContainer appendInfo = null;
	}
	
	public class InfoFileObject{
		private Object cfgFile = null;
		private boolean isFile = false;
		
		public InfoFileObject(Object cfgFile){
			this.cfgFile = cfgFile;
			this.isFile = (this.cfgFile instanceof File);
		}
		
		public boolean isFile(){
			return this.isFile;
		}
		
		public long lastModified(){
			if(this.isFile){
				return ((File)this.cfgFile).lastModified();
			}
			return -1;
		}
		
		public boolean exists(){
			if(this.isFile){
				return ((File)this.cfgFile).exists();
			}
			return true;
		}
		
		public InputStream getInputStream() throws Exception{
			if(this.isFile){
				return new FileInputStream((File)this.cfgFile);
			}else{
				return ((URL)this.cfgFile).openStream();
			}
		}
		
		public String toString(){
			if(this.isFile){
				return ((File)this.cfgFile).getAbsolutePath();
			}else{
				return ((URL)this.cfgFile).getPath();
			}
		}
	}
	
	public static class WorkTask implements ITask{
		private IAction action;
		private InfoContainer info;
		public WorkTask(IAction action,InfoContainer info){
			this.action = action;
			this.info = info;
		}
		
		public Object getFlag(){
			return info.getInfo(IAction.TASK_FLAG);
		}
		public String getDesc(){
			return info.getString(IAction.MSG_FLAG);
		}
		
		public boolean execute() throws Exception{
			return this.action.execute(info);
		}
		public String getMsgFlag(){
			return info.getString(IAction.MSG_FLAG);
		}
		
	}

    public HashMap getAppendInfoMapping()
    {
        return (HashMap)appendInfoMapping.clone();
    }
}
