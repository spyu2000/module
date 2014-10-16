/**
 * ���� Created on 2008-6-17 by edmund
 */
package server.socket.socket;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

import server.socket.inter.CmdInfo;
import server.socket.inter.ConnectSocketInfo;
import server.socket.inter.ICmdReader;
import server.socket.inter.ICmdReleaser;
import server.socket.socket.ReadSelectorThread.RegistInfo;
import server.threadgroup.PoolInfo;
import server.threadgroup.ThreadPoolGroupServer;

import com.fleety.base.FleetyThread;
import com.fleety.server.BasicServer;
import com.fleety.util.pool.thread.BasicTask;
import com.fleety.util.pool.thread.ThreadPool;
import com.fleety.util.pool.timer.FleetyTimerTask;

/**
 * ��װSocket���ӵĿͻ��ˣ�ͨ��ָ���������Ķ����Լ���������������ݵĶ�ȡ��
 * ���ݵķ��Ϳ���ͨ���ӿ�sendData�������Ӻ����ⲿ֪ͨ��ConnetSocketInfo�������
 * @title
 * @description
 * @version      1.0
 * @author       edmund
 *
 */
public class FleetySocket extends BasicServer{
	//������ConnectSocketInfo�е�FleetySocket�����key
	public static final Object FLEETY_SOCKET_FLAG = new Object();
	//�ⲿ��������Selector�����key
	public static final Object SELECTOR_FLAG = new Object();
	public static final String TIMER_POOL_NAME = "timer_name";
	
	//��Ҫ֧�ֵĲ�����ʶ���ֱ�Ϊ�����Ķ������ͷ����Լ���������ip�Ͷ˿�
	public static final String CMD_READER_FLAG = "reader";
	public static final String CMD_RELEASER_FLAG = "releaser";
	public static final String SERVER_IP_FLAG = "ip";
	public static final String SERVER_PORT_FLAG = "port";
	public static final String SERVER_IP_BAK_FLAG = "ip_bak";
	public static final String SERVER_PORT_BAK_FLAG = "port_bak";
	public static final String AUTO_CONNECT_CYCLE_FLAG = "connect_cycle";
	
	public static final String DETECT_TIMEOUT_FLAG = "detect_timeout";
	public static final String DETECT_CYCLE_FLAG = "detect_cycle";
	public static final String TIME_OUT_FLAG = "timeout";

	private boolean isStartTimeoutDetect = false;
	private long detectCycle = 2*60*1000l;
	private long timeout = 5*60*1000l;
	
	private ConnectSocketInfo connInfo = null;
	
	private String ip_cur = null;
	private int port_cur = 0;

	private String ip = null;
	private int port = 0;

	private String ip_bak = null;
	private int port_bak = 0;
	
	private boolean isMain_current=true;
	
	private int connectCycle = 60000;

	private String poolName = null,timerName = null;
	private boolean isSelf = false;
	private ReadSelectorThread selector = null;
	
	private ICmdReader cmdReader = null;
	private ICmdReleaser cmdReleaser = null;
	
	private FleetyTimerTask timeoutTask = null,switchTask = null,connectTask = null;

	/**
	 * ��������
	 */
	public boolean startServer(){		
		try{
			this.ip = this.getStringPara(SERVER_IP_FLAG);
			this.ip_cur=this.ip;
			
			String tempStr = this.getStringPara(SERVER_PORT_FLAG);
			if(tempStr != null && tempStr.trim().length() > 0){
				this.port = Integer.parseInt(tempStr.trim());
			}else{
				throw new Exception("����˿�����!");
			}
			this.port_cur=this.port;
			
			tempStr = this.getStringPara(SERVER_IP_BAK_FLAG);
			if(tempStr != null && tempStr.trim().length() > 0){
				this.ip_bak = tempStr;

				tempStr = this.getStringPara(SERVER_PORT_BAK_FLAG);
				if(tempStr != null && tempStr.trim().length() > 0){
					this.port_bak = Integer.parseInt(tempStr.trim());
				}else{
					throw new Exception("����ı��ö˿�����!");
				}
			}
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}
		
		try{
			String str = this.getStringPara(AUTO_CONNECT_CYCLE_FLAG);
			if(str != null){
				this.connectCycle = Integer.parseInt(str);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		
		try{
			Object tempStr = this.getPara(CMD_READER_FLAG);
			if(tempStr instanceof String){
				Class cls = Class.forName(((String)tempStr).trim());
				Object tempObj = cls.newInstance();
				this.cmdReader = (ICmdReader)tempObj;
				this.cmdReader.init(this);
			}else if(tempStr instanceof ICmdReader){
				this.cmdReader = (ICmdReader)tempStr;
			}else{
				throw new Exception("������Ķ���!"+tempStr);
			}
		}catch(Exception e){
			e.printStackTrace();
			System.out.println("�����Ķ���ʵ����ʧ��!");
			return false;
		}
		
		try{
			Object tempStr = this.getPara(CMD_RELEASER_FLAG);
			if(tempStr instanceof String){
				Class cls = Class.forName(((String)tempStr).trim());
				Object tempObj = cls.newInstance();
				this.cmdReleaser = (ICmdReleaser)tempObj;
				this.cmdReleaser.init(this);
			}else if(tempStr instanceof ICmdReleaser){
				this.cmdReleaser = (ICmdReleaser)tempStr;
			}else{
				throw new Exception("����ķ�����!"+tempStr);
			}
		}catch(Exception e){
			e.printStackTrace();
			System.out.println("���������ʵ����ʧ��!");
			return false;
		}
		
		try{
			ReadSelectorThread tempSelector = (ReadSelectorThread)this.getPara(SELECTOR_FLAG);
			if(tempSelector == null){
				this.createSelector();
			}else{
				this.selector = tempSelector;
				this.isSelf = false;
			}
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}
		
		String tempStr;
		try{
			tempStr = this.getStringPara(DETECT_CYCLE_FLAG);
			if(tempStr != null){
				this.detectCycle = Integer.parseInt(tempStr.trim());
			}
			tempStr = this.getStringPara(TIME_OUT_FLAG);
			if(tempStr != null){
				this.timeout = Integer.parseInt(tempStr.trim());
			}
			tempStr = this.getStringPara(DETECT_TIMEOUT_FLAG);
			if(tempStr != null){
				this.isStartTimeoutDetect = tempStr.trim().equalsIgnoreCase("true");
			}
		}catch(Exception e){
			e.printStackTrace();
		}


		this.timerName = this.getStringPara(TIMER_POOL_NAME);
		if(timerName == null){
			timerName = FleetySocket.this.getServerName() + "[" + FleetySocket.this.hashCode() + "]";
		}
		if(this.ip_bak != null){
			ThreadPoolGroupServer.getSingleInstance().createTimerPool(timerName).schedule(switchTask = new FleetyTimerTask(){
				public void run(){
					switchSocket();
				}
			}, this.connectCycle, this.connectCycle);
		}

		if(this.isStartTimeoutDetect){
			ThreadPoolGroupServer.getSingleInstance().createTimerPool(timerName).schedule(timeoutTask = new FleetyTimerTask(){
				public void run(){
					ConnectSocketInfo tempConn = FleetySocket.this.connInfo;
					if(tempConn != null){
						if(System.currentTimeMillis()
								- tempConn.getLastActiveTime() >= timeout){
							FleetySocket.this._closeSocket();
						}
					}
				}
			}, this.detectCycle, this.detectCycle);
		}
		
		this.connectSocket();
		ThreadPoolGroupServer.getSingleInstance().createTimerPool(timerName).schedule(connectTask = new FleetyTimerTask(){
			public void run(){
				connectSocket();
			}
		}, this.connectCycle, this.connectCycle);
		
		this.isRunning = true;
		return true;
	}

	/**
	 * �رշ���
	 */
	public synchronized void stopServer(){
		ThreadPoolGroupServer.getSingleInstance().removeThreadPool(this.poolName);
		ThreadPoolGroupServer.getSingleInstance().destroyTimerPool(this.timerName);

		if(this.timeoutTask != null){
			this.timeoutTask.cancel();
			this.timeoutTask = null;
		}
		if(this.switchTask != null){
			this.switchTask.cancel();
			this.switchTask = null;
		}
		if(this.connectTask != null){
			this.connectTask.cancel();
			this.connectTask = null;
		}

		this.ip = null;
		this.ip_bak = null;
		this.ip_cur = null;
		if(this.connInfo != null){
			this.connInfo.closeSocket();
		}
		if(this.isSelf){
			if(this.selector != null){
				try{
					this.selector.stopWork();
				}catch(Exception e){}
				this.selector = null;
			}
		}
		
		super.stopServer();
	}
	
	private void createSelector() throws Exception{
		this.isSelf = true;
		this.selector = new ReadSelectorThread();

		this.poolName = this.getServerName()+"["+this.hashCode()+"]FleetySocket";
		PoolInfo pInfo = new PoolInfo(ThreadPool.SINGLE_TASK_LIST_POOL,1,1,false);
		ThreadPool pool = ThreadPoolGroupServer.getSingleInstance().createThreadPool(poolName, pInfo);
		pool.addTask(this.selector);
	}
	
	protected void _registerSocketChannel(RegistInfo registInfo,Selector selector){
		try{
			registInfo.channel.configureBlocking(false);
			registInfo.channel.socket().setSoLinger(true, 10);
			registInfo.channel.socket().setTcpNoDelay(false);
			registInfo.channel.socket().setKeepAlive(true);
			registInfo.connInfo.setSelectionKey(registInfo.channel.register(selector,SelectionKey.OP_READ,registInfo.connInfo));
		}catch(Exception e){
			e.printStackTrace();
			try{
				registInfo.channel.close();
			}catch(Exception ee){}
			return ;
		}
		
		try{
			//������ʱ����Ҫ��������selectorҲ������ȥ�����Ա�����ע��󷢲���
			CmdInfo cmdInfo = new CmdInfo();
			cmdInfo.setInfo(CmdInfo.CMD_FLAG, CmdInfo.SOCKET_CONNECT_CMD);
			cmdInfo.setInfo(CmdInfo.SOCKET_FLAG, registInfo.connInfo);
			this.releaseCmdInfo(registInfo.connInfo,new CmdInfo[]{cmdInfo});
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	protected void readCmd(ConnectSocketInfo connInfo) throws Exception{
		CmdInfo[] cmdArr = this.cmdReader.readCmd(connInfo);
		connInfo.updateLastActiveTime();
		if(cmdArr != null && cmdArr.length > 0){
			this.releaseCmdInfo(connInfo, cmdArr);
		}
	}
	private void releaseCmdInfo(ConnectSocketInfo connInfo,CmdInfo[] cmdArr) throws Exception{
		if(cmdArr != null && cmdArr.length > 0){
			for(int i=0;i<cmdArr.length;i++){
				cmdArr[i].setInfo(CmdInfo.SOCKET_FLAG, connInfo);
				this.cmdReleaser.releaseCmd(cmdArr[i]);
			}
		}
	}
	protected void destroySocketChannel(ConnectSocketInfo connInfo,Object closeCode){
		this._closeSocket();
	}
	
	/**
	 * ����Socket����
	 * @return true����ɹ�����֮����ʧ��
	 */
	public synchronized boolean connectSocket(){
		if(this.isConnected()){
			return true;
		}
		this._closeSocket();

		if(this.ip_cur == null){
			return false;
		}
		try{
			SocketChannel channel = SocketChannel.open();
			channel.configureBlocking(false);
			channel.socket().setSoTimeout(5000);
			System.out.println("���ӵ�ַ��"+this.ip_cur+":"+this.port_cur);
			boolean isSuccess = channel.connect(new InetSocketAddress(InetAddress.getByName(this.ip_cur),this.port_cur));
			
			//�첽�ͻ������ӵ����⡣���ӿ���δ��ɼ�ִ���������룬������Ҫ�ڴ˴��жϣ�����ע����selector�н���Ч��
			int count = 0;
			if(channel.isConnectionPending()){
				while(!channel.finishConnect()){
					count ++;
					if(count >= 500){
						channel.close();
					}
					FleetyThread.sleep(10);
				}
			}

			this.connInfo = new ConnectSocketInfo(channel,this);
			this.connInfo.setInfo(FLEETY_SOCKET_FLAG, this);
			this.selector.addSocketChannel(this.selector.new RegistInfo(this,channel,connInfo));
		}catch(Exception e){
			System.out.println("Connect Error:ip="+this.ip_cur+" port="+this.port_cur);
			e.printStackTrace();
			this._closeSocket();
			return connectSocketBak();
		}
		
		return true;
	}
	

	/**
	 * ����Socket����
	 * @return true����ɹ�����֮����ʧ��
	 */
	public synchronized boolean connectSocketBak(){
		try{
			if(!isMain_current)
			{
				return false;
			}
			
			if(this.ip_bak == null){
				return false;
			}
			
			this.ip_cur=this.ip_bak;
			this.port_cur=this.port_bak;
			this.isMain_current=false;
			System.out.println("���ӱ��õ�ַ��"+this.ip_cur+":"+this.port_cur);
			return this.connectSocket();
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}
	}
	/**
	 * �رյ�ǰ�����ӡ�
	 */
	private synchronized void _closeSocket(){
		ConnectSocketInfo conn = this.connInfo;
		this.connInfo = null;
		if(conn != null){
			conn.destroy();
			
			CmdInfo cmdInfo = new CmdInfo();
			cmdInfo.setInfo(CmdInfo.CMD_FLAG, CmdInfo.SOCKET_DISCONNECT_CMD);
			cmdInfo.setInfo(CmdInfo.SOCKET_FLAG, conn);
			this.cmdReleaser.releaseCmd(cmdInfo);
			
			this.connectSocket();
		}
	}
	public void closeSocket(){
		this._closeSocket();
	}
	
	public ConnectSocketInfo getConnInfo(){
		return this.connInfo;
	}
	
	public synchronized boolean isConnected(){
		return this.connInfo != null;
	}
	
	/**
	 * ͨ����ǰ�����ӷ������ݵ�������
	 * @param data			����������
	 * @param offset		��ʼ���͵�λ��ƫ��
	 * @param len			��Ҫ���͵����ݳ���
	 * @throws Exception
	 */
	public void sendData(byte[] data,int offset,int len) throws Exception{
		if(this.connInfo != null){
			this.connInfo.writeData(data, offset, len);
		}else{
			throw new Exception("δ�����쳣!");
		}
	}
	
	private void switchSocket()
	{
		try
		{
			//�Ѿ������ӵ�����ַ
			if(isMain_current){
				return ;
			}
			Socket socket=new Socket(InetAddress.getByName(this.ip),this.port);
			socket.close();

			System.out.println("��IP����,���л�����IP!");
			this.ip_cur=this.ip;
			this.port_cur=this.port;
			this.isMain_current=true;
			this._closeSocket();
			connectSocket();
		}
		catch(Exception e)
		{
			System.out.println("�����л�����IPʧ��!");
			e.printStackTrace();
		}
	}
	public boolean isMain_current()
	{
		return isMain_current;
	}
	
}
