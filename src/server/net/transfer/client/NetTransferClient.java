package server.net.transfer.client;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.JOptionPane;

import com.fleety.util.pool.thread.ThreadPool;
import com.fleety.util.pool.timer.FleetyTimerTask;
import com.fleety.util.pool.timer.TimerPool;


import server.help.SingleServer;
import server.net.transfer.IDataListener;
import server.net.transfer.ITransfer;
import server.net.transfer.TransferProtocol;
import server.net.transfer.container.ClientDataReceiveTask;
import server.net.transfer.container.ClientDataSendTask;
import server.net.transfer.container.ClientReceiveTask;
import server.net.transfer.container.ClientSendTask;
import server.net.transfer.container.QueueContainer;
import server.net.transfer.container.QueueContainer.QueueItemInfo;
import server.socket.socket.FleetySocket;
import server.threadgroup.PoolInfo;
import server.threadgroup.ThreadPoolGroupServer;

public class NetTransferClient extends FleetySocket implements ITransfer {
	private String SEND_THREAD_POOL_NAME = null;
	private String RECEIVE_THREAD_POOL_NAME = null;
	private String HEART_TIMER_NAME = null;
	private ThreadPool sendPool = null;
	private ThreadPool receivePool = null;
	private ClientSendTask sendTask = null;
	private ClientReceiveTask receiveTask = null;
	private File dir = new File("_send_data_dir_");
	
	private QueueContainer sendContainer = null;
	private QueueContainer receiveContainer = null;
	public boolean startServer() {
		SingleServer s = new SingleServer();
		s.addPara("single_port", this.getStringPara("single_port"));
		s.startServer();
		
		dir.mkdirs();
		
		try{
			this.sendContainer = new QueueContainer(this,new File("_client_send_queue_file_"));
			this.receiveContainer = new QueueContainer(this,new File("_client_receive_queue_file_"));
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}
		
		this.addPara("reader", ClientCmdReader.class.getName());
		this.addPara("releaser", ClientCmdReleaser.class.getName());
		

		if(!this.createHeartTimer()){
			return false;
		}
		if(!this.createSendThreadPool()){
			return false;
		}
		if(!this.createReceiveThreadPool()){
			return false;
		}
		
		//test
		//this.addDownloadTask(new File("c:/xjs.rar"), GeneralConst.ONE_DAY_TIME, new File("e:/PGIS.rar").getAbsolutePath());
		
		return super.startServer();
	}
	
	private boolean createHeartTimer(){
		HEART_TIMER_NAME = "heart_timer_"+this.hashCode();
		try{
			TimerPool timer = ThreadPoolGroupServer.getSingleInstance().createTimerPool(HEART_TIMER_NAME, 1);
			timer.schedule(new FleetyTimerTask(){
				public void run(){
					if(NetTransferClient.this.isConnected()){
						byte[] data = TransferProtocol.createHeartData();
						try{
							NetTransferClient.this.sendData(data, 0, data.length);
						}catch(Exception e){
							e.printStackTrace();
						}
					}
				}
			}, 30000, 30000);
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	private boolean createSendThreadPool(){
		try{
			SEND_THREAD_POOL_NAME = "send_thread_pool_"+this.hashCode();
			PoolInfo pInfo = new PoolInfo();
			pInfo.workersNumber = 21;
			pInfo.taskCapacity = 100;
			sendPool = ThreadPoolGroupServer.getSingleInstance().createThreadPool(SEND_THREAD_POOL_NAME, pInfo);
			sendPool.addTask(sendTask = new ClientSendTask(this,this.sendContainer));
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}
		return true;
	}
	private boolean createReceiveThreadPool(){
		try{
			RECEIVE_THREAD_POOL_NAME = "receive_thread_pool_"+this.hashCode();
			PoolInfo pInfo = new PoolInfo();
			pInfo.workersNumber = 21;
			pInfo.taskCapacity = 100;
			receivePool = ThreadPoolGroupServer.getSingleInstance().createThreadPool(RECEIVE_THREAD_POOL_NAME, pInfo);
			receivePool.addTask(receiveTask = new ClientReceiveTask(this,this.receiveContainer));
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	public void stopServer(){
		ThreadPoolGroupServer.getSingleInstance().removeThreadPool(SEND_THREAD_POOL_NAME);
		ThreadPoolGroupServer.getSingleInstance().removeThreadPool(RECEIVE_THREAD_POOL_NAME);
		ThreadPoolGroupServer.getSingleInstance().destroyTimerPool(HEART_TIMER_NAME);
		if(this.sendTask != null){
			this.sendTask.stop();
		}
		if(this.receiveTask != null){
			this.receiveTask.stop();
		}
		super.stopServer();
	}
	public boolean addDownloadTask(File saveFile,long maxDuration,String appendInfo){
		QueueItemInfo itemInfo = this.receiveContainer.new QueueItemInfo();
		itemInfo.updateItemEnvInfo(saveFile.getAbsolutePath(),0,0,new byte[0]);
		if(maxDuration >= 0){
			itemInfo.expiredTime = System.currentTimeMillis() + maxDuration;
		}else{
			itemInfo.expiredTime = -1;
		}
		itemInfo.appendInfo = appendInfo;
		boolean isOk = this.receiveContainer.addReceiveTask(itemInfo);
		if(itemInfo != null){
			this.triggerTaskChanged(itemInfo);
		}
		this.notifyDownloadNow();
		return isOk;
	}
	
	public void notifyDownloadNow(){
		synchronized(this.receiveTask){
			this.receiveTask.notify();
		}
	}
	
	public boolean addUploadTask(File f,boolean isOnce,String appendInfo){
		return this.addUploadTask(f, isOnce, Long.MAX_VALUE, appendInfo);
	}
	public boolean addUploadTask(File f,long maxDuration,String appendInfo){
		return this.addUploadTask(f, false, maxDuration, appendInfo);
	}
	public boolean addUploadTask(File f,boolean isOnce,long maxDuration,String appendInfo){
		return this.addUploadTask(f, isOnce, maxDuration, false, appendInfo);
	}
	public boolean addUploadTask(File f,boolean isOnce,long maxDuration,boolean needDeleteFile,String appendInfo){
		QueueItemInfo sInfo = this.sendContainer.addUploadTask(f, isOnce, maxDuration, needDeleteFile, appendInfo);
		if(sInfo != null){
			this.triggerTaskChanged(sInfo);
		}
		this.notifyUploadNow();
		return sInfo != null;
	}
	
	public boolean addUploadTask(byte[] data,boolean isOnce,String appendInfo){
		return this.addUploadTask(data, isOnce, -1, appendInfo);
	}
	public boolean addUploadTask(byte[] data,long maxDuration,String appendInfo){
		return this.addUploadTask(data, false, maxDuration, appendInfo);
	}
	public boolean addUploadTask(byte[] data,boolean isOnce,long maxDuration,String appendInfo){
		if(!isOnce){
			File f = getUniqueFile();
			
			BufferedOutputStream out = null;
			try{
				out = new BufferedOutputStream(new FileOutputStream(f));
				out.write(data);
			}catch(Exception e){
				e.printStackTrace();
				return false;
			}finally{
				if(out != null){
					try{
						out.close();
					}catch(Exception e){
						e.printStackTrace();
						return false;
					}
				}
			}
			QueueItemInfo sInfo = this.sendContainer.addUploadTask(f, isOnce, maxDuration, true, appendInfo);
			if(sInfo != null){
				this.triggerTaskChanged(sInfo);
			}
			return sInfo != null;
		}else{
			//Ö±½Ó·¢ËÍ
			if(!this.isConnected()){
				return false;
			}
			byte[] sendData = TransferProtocol.createUserSendData(data, appendInfo);
			try{
				this.sendData(sendData, 0, sendData.length);
			}catch(Exception e){
				e.printStackTrace();
				return false;
			}
		}
		return true;
	}
	public void notifyUploadNow(){
		synchronized(this.sendTask){
			this.sendTask.notify();
		}
	}

	private synchronized File getUniqueFile(){
		long t = System.currentTimeMillis();;
		File f;
		int count = 1;
		while(true){			
			f = new File(this.dir,t+"-"+count+".data");
			if(!f.exists()){
				break;
			}
			count ++;
		}
		return f;
	}
	
	public QueueContainer getSendQueueContainer(){
		return this.sendContainer;
	}
	public QueueContainer getReceiveQueueContainer(){
		return this.receiveContainer;
	}
	
	public QueueItemInfo getSendInfoByName(String name){
		return this.sendContainer.getQueueItemInfoByName(name);
	}

	private Vector listeners = new Vector();
	public void addDataReceiveListener(IDataListener listener){
		if(listener == null){
			return ;
		}
		
		if(listeners.contains(listener)){
			return ;
		}
		listeners.add(listener);
	}
	
	public void triggerSendProgress(QueueItemInfo itemInfo){
		IDataListener listener;
		synchronized(listeners){
			for(Iterator itr = listeners.iterator();itr.hasNext();){
				listener = (IDataListener)itr.next();
				listener.dataSended(this, itemInfo, itemInfo.getProgress());
			}
		}
	}
	public void triggerTaskChanged(QueueItemInfo itemInfo){
		IDataListener listener;
		synchronized(listeners){
			for(Iterator itr = listeners.iterator();itr.hasNext();){
				listener = (IDataListener)itr.next();
				listener.taskChanged(this, itemInfo);
			}
		}
	}
	
	public void newSendTaskArrived(){
		synchronized(this.sendTask){
			this.sendTask.notify();
		}
	}
	
	public void addDataSendTask(QueueItemInfo sInfo){
		System.out.println("Send Param:ConCurrent="+sInfo.getConcurrentNum()+" BlockSize="+sInfo.getBlockSize()/1024+" TotalBlockNum="+sInfo.getTotalBlockNum());
		String ip = this.getStringPara(FleetySocket.SERVER_IP_FLAG);
		int port = this.getIntegerPara(FleetySocket.SERVER_PORT_FLAG).intValue();
		for(int i=0;i<sInfo.getConcurrentNum();i++){
			this.sendPool.addTask(new ClientDataSendTask(i+1,ip,port,sInfo));
		}
	}
	public void addDataReceiveTask(QueueItemInfo sInfo){
		System.out.println("Download Param:ConCurrent="+sInfo.getConcurrentNum()+" BlockSize="+sInfo.getBlockSize()/1024+" TotalBlockNum="+sInfo.getTotalBlockNum());
		String ip = this.getStringPara(FleetySocket.SERVER_IP_FLAG);
		int port = this.getIntegerPara(FleetySocket.SERVER_PORT_FLAG).intValue();
		for(int i=0;i<sInfo.getConcurrentNum();i++){
			this.receivePool.addTask(new ClientDataReceiveTask(this,i+1,ip,port,sInfo));
		}
	}

	public String getSavePath(QueueItemInfo itemInfo){
		return null;
	}
	
	public void cancelTask(QueueItemInfo itemInfo){
		itemInfo.updateWorkStatus(QueueContainer.WORK_STATUS_CANCEL);
		itemInfo.getQueueContainer().updateAndSaveQueue();
		this.triggerTaskChanged(itemInfo);
	}
}
