/**
 * ���� Created on 2008-4-8 by edmund
 */
package com.fleety.server;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Iterator;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import server.notify.INotifyServer;

import com.fleety.base.FleetyThread;
import com.fleety.base.Util;

public class Server_Startup extends BasicServer{
	public static final String SERVER_CFG_PATH_KEY = "cfg_path";
	private String serverCfgPath = null;

	public boolean startServer(String serverPath){
		this.addPara(SERVER_CFG_PATH_KEY, serverPath);
		return this.startServer();
	}
	
	public boolean startServer(InputStream in){
		if(in == null){
			return false;
		}
		try{
			boolean isSuccess = this.loadAndRunServer(in);
			if(isSuccess){
				System.out.println("XML�����ݼ��سɹ�!");
			}else{
				System.out.println("XML�����ݼ���ʧ��!");
			}
			return isSuccess;
		}catch(Exception e){
			e.printStackTrace();
			System.out.println("XML�����ݼ���ʧ��!");
			return false;
		}
	}

	public boolean startServer(){
		String tempStr = this.getStringPara(SERVER_CFG_PATH_KEY);
		if(tempStr != null && tempStr.trim().length() > 0){
			this.serverCfgPath = tempStr.trim();
		}

		if(this.serverCfgPath == null){
			return false;
		}

		this.isRunning = true;
		String[] cfgArr = this.serverCfgPath.split(";");
		for(int i = 0; i < cfgArr.length; i++){
			this.isRunning |= this.loadAndRunServer(cfgArr[i]);
		}

		return this.isRunning;
	}

	public void stopServer(){
		this.isRunning = false;
	}

	private boolean loadAndRunServer(String serverPath){
		String realPath = null;
		try{
			URL url = null;
			if(serverPath.startsWith("!")){
				url = Server_Startup.class.getClassLoader().getResource(serverPath.substring(1));
			}else{
				File f = new File(serverPath);
				if(!f.exists()){
					url = Server_Startup.class.getClassLoader().getResource(f.getName());
				} else{
					url = f.toURI().toURL();
				}
			}
			realPath = url.getPath();
			
			InputStream in = url.openStream();
			this.loadAndRunServer(in);
			in.close();
		} catch(Exception e){
			e.printStackTrace();
			System.err.println("�����ļ����ش���!" + serverPath+"\t"+realPath);
			return false;
		}
		System.out.println("�����ļ����سɹ�!  " + serverPath+"\t"+realPath);
		return true;
	}
	
	private boolean loadAndRunServer(InputStream in) throws Exception{
		//���������ļ�
		DocumentBuilderFactory domfac = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = domfac.newDocumentBuilder();

		Document document = builder.parse(in);
		in.close();

		Element root = document.getDocumentElement();

		NodeList allServerNodeList = root.getElementsByTagName("server");
		int serverNum = allServerNodeList.getLength();
		for(int i = 0; i < serverNum; i++){
			this.runServerByNode(allServerNodeList.item(i));
		}
		
		return true;
	}

	private void runServerByNode(Node serverNode){
		String depends = Util.getNodeAttr(serverNode, "depends");
		String serverName = Util.getNodeText(Util.getSingleElementByTagName(serverNode, "server_name"));
		String className = Util.getNodeText(Util.getSingleElementByTagName(serverNode, "class_name"));
		String createMethod = Util.getNodeText(Util.getSingleElementByTagName(serverNode, "create_method"));
		String enableServer = Util.getNodeText(Util.getSingleElementByTagName(serverNode, "enable_server"));
		String failureExit = Util.getNodeText(Util.getSingleElementByTagName(serverNode, "failure_system_exit"));
		boolean isFailureExit = (failureExit != null && failureExit.equalsIgnoreCase("true")) ? true : false;

		if(enableServer != null && enableServer.equalsIgnoreCase("false")){
			System.out.println("����" + serverName + "��������!");
			return;
		}
		if(depends != null){
			String[] allDepends = depends.trim().split(",");
			int num = allDepends.length;
			IServer dependServer;
			String dependServerName;
			for(int i = 0; i < num; i++){
				dependServerName = allDepends[i].trim();
				if(dependServerName.length() == 0){
					continue;
				}
				dependServer = ServerContainer.getSingleInstance().getServer(dependServerName);
				if(dependServer == null || !dependServer.isRunning()){
					System.out.println("����" + serverName + "������ʧ��"
							+ (isFailureExit ? ",ϵͳ���˳�" : "")
							+ "!�������ķ���" + dependServerName
							+ "�������ڻ�δ����!");
					if(isFailureExit){
						INotifyServer.getSingleInstance().notifyInfo("ϵͳ����ʧ��֪ͨ.", "ϵͳ����ʧ��֪ͨ,�����["+serverName+"]����ʧ��,��������["+dependServerName+"].",INotifyServer.ERROR_LEVEL);
						System.exit(-1);
					} else{
						return;
					}
				}
			}
		}

		try{
			Class cls = Class.forName(className);
			IServer server = null;
			if(createMethod == null || createMethod.trim().length() == 0){
				server = (IServer) cls.newInstance();
			} else{
				if("getSingleInstance".equals(createMethod)){
					Method method = cls.getMethod(createMethod, new Class[0]);
					server = (IServer) method.invoke(null, new Object[0]);
				}else if("getSingleObj".equals(createMethod)){
					Method method = cls.getMethod(createMethod, cls.getClass());
					server = (IServer) method.invoke(null, cls);
				} 
			}

			//Ϊ�������趨������Ϣ,����Ĳ���ʹ�ã����ɷ������ڲ�ʹ��
			Node[] paraNodeArr = Util.getElementsByTagName(serverNode, "para");
			int paraNum = paraNodeArr.length;
			String key, value;
			for(int i = 0; i < paraNum; i++){
				key = Util.getNodeAttr(paraNodeArr[i], "key");
				value = Util.getNodeAttr(paraNodeArr[i], "value");
				if(key == null || value == null){
					continue;
				}
				server.addPara(key.trim(), value.trim());
			}
			server.setServerName(serverName);
			//��������
			if(!server.startServer()){
				server.stopServer();
				throw new Exception("false");
			}
			if(ServerContainer.getSingleInstance().getServer(serverName) != null){
				System.out.println("��������:��ͬ�ķ���������.serverName="
						+ serverName);
			}
			ServerContainer.getSingleInstance().addServer(server);
			System.out.println("����" + serverName + "�������ɹ�!");
			
			if(serverName.equals(FleetyThread.MODULE_MONITOR_MAIL_NAME)){
				INotifyServer.getSingleInstance().notifyInfo("ϵͳ��ʼ����֪ͨ.", "ϵͳ��ʼ����֪ͨ.",INotifyServer.ERROR_LEVEL);
			}
		} catch(Throwable e){
			e.printStackTrace();
			if(isFailureExit){
				INotifyServer.getSingleInstance().notifyInfo("ϵͳ����ʧ��֪ͨ.", "ϵͳ����ʧ��֪ͨ,�����["+serverName+"]����ʧ��.",INotifyServer.ERROR_LEVEL);
				System.err.println("����" + serverName + "������ʧ�ܣ�ϵͳ���˳�!");
				System.exit(-1);
			} else{
				System.err.println("����" + serverName + "������ʧ��!");
			}
		}
	}

	/**
	 * ����һ���߳����У������ⲻ�����������˳���
	 * �ýӿڲ������ã���õ���ͬ����⣬�������첽��⡣
	 */
	private boolean detectDog(){
		return true;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args){
		Server_Startup instance = new Server_Startup();
		instance.addPara(SERVER_CFG_PATH_KEY, "./conf/start_server.xml");

//		if(!instance.detectDog()){
//			System.out.println("Mic Dog Error!");
//			return;
//		}

		Runtime.getRuntime().addShutdownHook(new Thread(){
			public void run(){
				INotifyServer.getSingleInstance().notifyInfo("ϵͳ�ر�֪ͨ.", "ϵͳ�ر�֪ͨ.",INotifyServer.ERROR_LEVEL);
				Iterator serverIterator = ServerContainer.getSingleInstance().iteratorServerName();
				String serverName;
				IServer server = null;
				while(serverIterator.hasNext()){
					serverName = serverIterator.next().toString();
					server = (IServer) ServerContainer.getSingleInstance().getServer(serverName);

					System.out.println("����[" + serverName + "]����ֹͣ!");
					server.stopServer();
				}
				
				//ͣ2�룬ȷ���ʼ�������ɡ�
				try{
					FleetyThread.sleep(2000);
				}catch(Exception e){}
			}
		});

		instance.startServer();
		
		INotifyServer.getSingleInstance().notifyInfo("ϵͳ�������֪ͨ.", "ϵͳ����������.",INotifyServer.ERROR_LEVEL);
	}
}
