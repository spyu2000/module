/**
 * 绝密 Created on 2008-5-15 by edmund
 */
package server.socket.serversocket;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketOptions;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;
import java.util.List;

import server.socket.inter.CmdInfo;
import server.socket.inter.ConnectSocketInfo;
import server.socket.inter.ICmdReader;
import server.socket.inter.ICmdReleaser;
import server.socket.serversocket.ReadSelectorThread.RegistInfo;
import server.threadgroup.PoolInfo;
import server.threadgroup.ThreadPoolGroupServer;

import com.fleety.base.FleetyThread;
import com.fleety.server.BasicServer;
import com.fleety.util.pool.thread.BasicTask;
import com.fleety.util.pool.thread.ThreadPool;
import com.fleety.util.pool.timer.FleetyTimerTask;

public class FleetySocketServer extends BasicServer{
	public static final String CMD_READER_FLAG = "reader";
	public static final String CMD_RELEASER_FLAG = "releaser";
	
	public static final String SERVER_IP_FLAG = "ip";
	public static final String SERVER_PORT_FLAG = "port";
	public static final String SELECTOR_NUM_FLAG = "selector_num";
	
	public static final String DETECT_TIMEOUT_FLAG = "detect_timeout";
	public static final String DETECT_CYCLE_FLAG = "detect_cycle";
	public static final String TIME_OUT_FLAG = "timeout";
	
	//配置参数，单位秒
	public static final String SOCKET_CACH_PRINT_INTERVAL_FLAG = "print_socket_cach_interval";
	
	private String ip = null;
	private int port = 0;
	private ServerSocketChannel serverSocket = null;
	
	private int cachPrintInterval = -1;
	
	private boolean isStartTimeoutDetect = false;
	private long detectCycle = 2*60*1000l;
	private long timeout = 5*60*1000l;
	
	private int readSelectorNum = 5;
	private Selector accpetSelector = null;
	private Selector[] readSelector = null;
	
	private SocketTimeOutDetect detectThread = null;
	private AcceptSelectorThread acceptThread = null;
	private ReadSelectorThread[] readThreadArr = null;
	
	private ICmdReader cmdReader = null;
	private ICmdReleaser cmdReleaser = null;
	
	public FleetySocketServer(){
		this(null,0);
	}
	public FleetySocketServer(int port){
		this(null,port);
	}
	public FleetySocketServer(String ip,int port){
		this.ip = ip;
		this.port = port;
	}
	
	private List connInfoList = new LinkedList();
	private int selectorIndex = 0;
	protected void _registerSocketChannel(RegistInfo registInfo,Selector selector){
		try{
			registInfo.channel.configureBlocking(false);
			registInfo.channel.socket().setSoLinger(true, 10);
			registInfo.channel.socket().setTcpNoDelay(false);
			registInfo.channel.socket().setKeepAlive(true);
			registInfo.connInfo.setSelectionKey(registInfo.channel.register(selector,SelectionKey.OP_READ,registInfo.connInfo));
			
			synchronized(connInfoList){
				connInfoList.add(registInfo.connInfo);
			}
		}catch(Exception e){
			e.printStackTrace();
			try{
				registInfo.channel.close();
			}catch(Exception ee){}
			return ;
		}
		
		try{
			//发布的时候需要把所属的selector也发布出去，所以必须在注册后发布。
			CmdInfo cmdInfo = new CmdInfo();
			cmdInfo.setInfo(CmdInfo.CMD_FLAG, CmdInfo.SOCKET_CONNECT_CMD);
			cmdInfo.setInfo(CmdInfo.SOCKET_FLAG, registInfo.connInfo);
			this.releaseCmdInfo(registInfo.connInfo,new CmdInfo[]{cmdInfo});
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	protected synchronized ConnectSocketInfo registerSocketChannel(SocketChannel channel){
		ConnectSocketInfo connInfo = this.registerSocketChannel(channel, null, null, null);
		return connInfo;
	}
	protected ConnectSocketInfo registerSocketChannel(SocketChannel channel,ICmdReader reader,ICmdReleaser releaser,Object optionalInfo){
		if(channel == null){
			return null;
		}
		
		ConnectSocketInfo connInfo = null;
		try{
			ReadSelectorThread sthread = null;
			synchronized(this){
				sthread = readThreadArr[this.selectorIndex];
				this.selectorIndex ++;
				if(this.selectorIndex >= this.readSelectorNum){
					this.selectorIndex = 0;
				}
			}
			
			connInfo = new ConnectSocketInfo(channel,reader,releaser,optionalInfo,this);
			sthread.addSocketChannel(sthread.new RegistInfo(channel,connInfo));
		}catch(Exception e){
			e.printStackTrace();
			try{
				channel.close();
			}catch(Exception a){}
			return null;
		}
		
		return connInfo;
	}
	
	public String getInfoDesc(){
		if(!this.isRunning()){
			return "服务已停止!";
		}
		StringBuffer infoDesc = new StringBuffer(128);
		infoDesc.append("selectorNum=");
		infoDesc.append(this.readSelectorNum);
		infoDesc.append(";");
		infoDesc.append("registNum=");
		infoDesc.append("[");
		for(int i=0;i<this.readSelectorNum;i++){
			if(i>0){
				infoDesc.append(",");
			}
			infoDesc.append(this.readSelector[i].keys().size());
		}
		infoDesc.append("]");
			
		return infoDesc.toString();
	}
	
	public void destroySocketChannel(ConnectSocketInfo connInfo,Object closeCode){
		if(connInfo == null){
			return ;
		}
		boolean isRemove = true;
		synchronized(this.connInfoList){
			isRemove = this.connInfoList.remove(connInfo);
		}
		synchronized(connInfo){
			connInfo.destroy();
		}
		if(isRemove){
			CmdInfo cmdInfo = new CmdInfo();
			cmdInfo.setInfo(CmdInfo.CMD_FLAG, CmdInfo.SOCKET_DISCONNECT_CMD);
			cmdInfo.setInfo(CmdInfo.SOCKET_FLAG, connInfo);
			cmdInfo.setInfo(CmdInfo.SOCKET_CLOSE_CODE_FLAG, closeCode);
				
			this.releaseCmdInfo(connInfo,new CmdInfo[]{cmdInfo});
		}
	}
	
	public int getConnSize(){
		synchronized(this.connInfoList){
			return this.connInfoList.size();
		}
	}
	
	public ConnectSocketInfo[] getAllConnInfo(){
		Object[] objArr = null;
		synchronized(this.connInfoList){
			objArr = this.connInfoList.toArray();
		}
		ConnectSocketInfo[] arr = new ConnectSocketInfo[objArr.length];
		System.arraycopy(objArr, 0, arr, 0, objArr.length);
		return arr;
	}
	
	protected void readCmd(ConnectSocketInfo connInfo) throws Exception{
		ICmdReader reader = connInfo.getReader();
		if(reader == null){
			reader = this.cmdReader;
		}
		if(reader != null){
			CmdInfo[] infos = reader.readCmd(connInfo);
			connInfo.updateLastActiveTime();
			this.releaseCmdInfo(connInfo,infos);
		}
	}
	
	protected void releaseCmdInfo(ConnectSocketInfo connInfo,CmdInfo[] cmdInfos){
		if(cmdInfos == null){
			return ;
		}
		ICmdReleaser releaser = connInfo.getReleaser();
		if(releaser == null){
			releaser = this.cmdReleaser;
		}
		if(releaser != null){
			int num = cmdInfos.length;
			for(int i=0;i<num;i++){
				cmdInfos[i].setInfo(CmdInfo.SOCKET_FLAG, connInfo);
				releaser.releaseCmd(cmdInfos[i]);
			}
		}
	}

	private String acceptPoolName = null,readPoolName = null;
	public boolean startServer(){		
		try{
			if(this.ip == null){
				this.ip = this.getStringPara(SERVER_IP_FLAG);
			}
			if(this.ip == null || this.ip.trim().length() == 0){
				this.ip = "0.0.0.0";
			}
			
			String tempStr = this.getStringPara(SERVER_PORT_FLAG);
			if(tempStr != null){
				this.port = Integer.parseInt(tempStr.trim());
			}
			
			tempStr = this.getStringPara(SELECTOR_NUM_FLAG);
			if(tempStr != null){
				this.readSelectorNum = Integer.parseInt(tempStr.trim());
			}
			if(this.readSelectorNum == 0){
				this.readSelectorNum = 5;
			}
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
		
		Integer tempInteger = this.getIntegerPara(SOCKET_CACH_PRINT_INTERVAL_FLAG);
		if(tempInteger != null){
			this.cachPrintInterval = tempInteger.intValue()*1000;
		}
		if(this.cachPrintInterval >= 1000){
			ThreadPoolGroupServer.getSingleInstance().createTimerPool(ThreadPoolGroupServer._QUICK_EXECUTE_OBJ_NAME_).schedule(new FleetyTimerTask(){
				public void run(){
					ConnectSocketInfo[] arr = FleetySocketServer.this.getAllConnInfo();
					StringBuilder strBuff = new StringBuilder(1024);
					strBuff.append("Server Total Conn Info:"+FleetySocketServer.this.getServerName());
					strBuff.append(" num="+arr.length);
					for(int i=0;i<arr.length;i++){
						strBuff.append("\n");
						strBuff.append(arr[i].getSocketDesc());
						strBuff.append(":");
						strBuff.append(arr[i].remaining());
						strBuff.append("/");
						strBuff.append(arr[i].getAllSize());
					}
					System.out.println(strBuff.toString());
				}
			}, this.cachPrintInterval, this.cachPrintInterval);
		}
		
		try{
			String tempStr = this.getStringPara(CMD_READER_FLAG);
			Class cls = Class.forName(tempStr.trim());
			Object tempObj = cls.newInstance();
			this.cmdReader = (ICmdReader)tempObj;
			this.cmdReader.init(this);
		}catch(Exception e){
			e.printStackTrace();
			System.out.println("命令阅读器实例化失败!");
			return false;
		}
		
		try{
			String tempStr = this.getStringPara(CMD_RELEASER_FLAG);
			Class cls = Class.forName(tempStr.trim());
			Object tempObj = cls.newInstance();
			this.cmdReleaser = (ICmdReleaser)tempObj;
			this.cmdReleaser.init(this);
		}catch(Exception e){
			e.printStackTrace();
			System.out.println("命令分配器实例化失败!");
			return false;
		}
		
		try{
			this.readPoolName = this.getServerName()+"["+this.hashCode()+"] Socket Read";
			PoolInfo pInfo = new PoolInfo(ThreadPool.SINGLE_TASK_LIST_POOL,readSelectorNum,readSelectorNum,false);
			ThreadPool pool = ThreadPoolGroupServer.getSingleInstance().createThreadPool(readPoolName, pInfo);
			
			this.readSelector = new Selector[this.readSelectorNum];
			this.readThreadArr = new ReadSelectorThread[this.readSelectorNum];
			for(int i=0;i<this.readSelectorNum;i++){
				this.readSelector[i] = Selector.open();
				this.readThreadArr[i] = new ReadSelectorThread(this.readSelector[i],this);
				pool.addTask(this.readThreadArr[i]);
			}
			
			this.serverSocket = ServerSocketChannel.open();
			this.serverSocket.configureBlocking(false);
			this.serverSocket.socket().bind(new InetSocketAddress(this.ip,this.port));
			this.accpetSelector = Selector.open();
			this.serverSocket.register(this.accpetSelector, SelectionKey.OP_ACCEPT);
			System.out.println("\t服务地址:"+this.ip+":"+this.port+" 开启成功!");

			
			this.acceptPoolName = this.getServerName()+"["+this.hashCode()+"] Socket Accept";
			pInfo = new PoolInfo(ThreadPool.SINGLE_TASK_LIST_POOL,1,1,false);
			pInfo.priority = Thread.MAX_PRIORITY;
			pool = ThreadPoolGroupServer.getSingleInstance().createThreadPool(acceptPoolName, pInfo);

			this.acceptThread = new AcceptSelectorThread(this.accpetSelector,this);
			pool.addTask(this.acceptThread);
			
			
			if(this.isStartTimeoutDetect){
				this.detectThread = new SocketTimeOutDetect();
				this.detectThread.start();
			}
		}catch(Exception e){
			e.printStackTrace();
			System.out.println("服务运作线程实例化失败!");
			this.stopServer();
			return false;
		}

		this.isRunning = true;
		return true;
	}
	
	public void stopServer(){
		ThreadPoolGroupServer.getSingleInstance().removeThreadPool(this.acceptPoolName);
		ThreadPoolGroupServer.getSingleInstance().removeThreadPool(this.readPoolName);
		
		if(this.acceptThread != null){
			this.acceptThread.stopWork();
		}
		
		try{
			if(this.accpetSelector != null){
				this.accpetSelector.close();
			}
			if(this.serverSocket != null){
				this.serverSocket.close();
			}
			
			if(this.detectThread != null){
				this.detectThread.stopWork();
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		
		for(int i=0;i<this.readSelectorNum;i++){
			if(null != this.readThreadArr[i]){
				this.readThreadArr[i].stopWork();
			}
		}
		
		this.isRunning = false;
	}
	public boolean isOpen(){
		if(this.serverSocket==null){
			return false;
		}
		return this.serverSocket.isOpen();
	}
	private class SocketTimeOutDetect extends BasicTask{
		private boolean isStop = false;
		
		private String poolName = null;
		public void start(){
			try{
				this.poolName = FleetySocketServer.this.getServerName()+"["+FleetySocketServer.this.hashCode()+"] Socket Timeout Detect";
				PoolInfo pInfo = new PoolInfo(ThreadPool.SINGLE_TASK_LIST_POOL,1,1,false);
				ThreadPool pool = ThreadPoolGroupServer.getSingleInstance().createThreadPool(poolName, pInfo);
				pool.addTask(this);
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		public boolean execute() throws Exception{
			try{
				this.run();
			}catch(Throwable e){
				e.printStackTrace();
			}finally{
				ThreadPoolGroupServer.getSingleInstance().removeThreadPool(this.poolName);
			}
			return true;
		}
		
		public void run(){
			Object[] connInfoArr = null;
			ConnectSocketInfo connInfo = null;
			while(!isStop){
				try{
					FleetyThread.sleep(detectCycle);
				}catch(Exception e){}
				
				synchronized(connInfoList){
					connInfoArr = connInfoList.toArray();
				}
				int num = connInfoArr.length;
				
				for(int i=0;i<num;i++){
					connInfo = (ConnectSocketInfo)connInfoArr[i];
					if(System.currentTimeMillis() - connInfo.getLastActiveTime() >= timeout){
						FleetySocketServer.this.destroySocketChannel(connInfo,CmdInfo.CLOSE_TIMEOUT_CODE);
					}
				}
			}
		}
		
		public void stopWork(){
			ThreadPoolGroupServer.getSingleInstance().removeThreadPool(this.poolName);
			this.isStop = true;
		}
	}
}
