package server.net.transfer.server;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.ArrayList;

import com.fleety.util.pool.thread.BasicTask;
import com.fleety.util.pool.thread.ThreadPool;

import server.net.transfer.TransferProtocol;
import server.net.transfer.container.QueueContainer.QueueItemInfo;
import server.socket.inter.ConnectSocketInfo;
import server.threadgroup.PoolInfo;
import server.threadgroup.ThreadPoolGroupServer;

public class DataDownloadHelper extends BasicTask {
	private NetTransferServer server = null;
	private ThreadPool pool = null;
	private String poolName = null;
	private boolean isWorking = true;
	
	public DataDownloadHelper(NetTransferServer server) throws Exception{
		this.server = server;
		
		PoolInfo pInfo = new PoolInfo();
		this.poolName = "Net_Transfer_Server_Helper"+server.hashCode();
		this.pool = ThreadPoolGroupServer.getSingleInstance().createThreadPool(this.poolName, pInfo);
		this.pool.addTask(this);
	}
	
	private ArrayList connList = new ArrayList(1024);
	public void addConnInfo(ConnectSocketInfo connInfo){
		synchronized(connList){
			connList.add(connInfo);
		}
		this.notifySend();
	}
	
	public void removeConnInfo(ConnectSocketInfo connInfo){
		synchronized(connList){
			connList.remove(connInfo);
		}
	}
	
	public void notifySend(){
		synchronized(this){
			this.notify();
		}
	}
	
	public boolean execute(){
		while(this.isWorking){
			try{
				this.sendData();
				
				synchronized(this){
					this.wait(500);
				}
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		return true;
	}
	
	private void sendData(){
		Object[] connArr = null;
		synchronized(connList){
			connArr = connList.toArray();
		}
		
		ConnectSocketInfo connInfo;
		for(int i=0;i<connArr.length;i++){
			connInfo = (ConnectSocketInfo)connArr[i];
			
			this.detectDataSend(connInfo);
		}
	}
	
	private void detectDataSend(ConnectSocketInfo connInfo){
		try{
			String remoteId = (String)connInfo.getInfo(ServerCmdReleaser.DATA_DOWNLOAD_REQUEST_FLAG);
			QueueItemInfo itemInfo = this.server.getSendQueueContainer().getQueueItemInfoById(remoteId);
			if(itemInfo == null){
				System.out.println("Not Exist,RID="+remoteId);
				connInfo.closeSocket();
				return ;
			}
			File f = itemInfo.getQueueSendFile();
			if(!f.exists() || !f.isFile()){
				System.out.println("File Not Exist!"+f.getAbsolutePath());
				connInfo.closeSocket();
				return ;
			}
			
			int blockSize = itemInfo.getBlockSize();
			if(blockSize+1000 > connInfo.getMaxCachSize() - connInfo.getCurSize()){
				return ;
			}
			int blockNumber = itemInfo.getNextSendBlockNumber();
			if(blockNumber < 0){
				byte[] disConnData = TransferProtocol.createDisconnectData();
				connInfo.writeData(disConnData,0,disConnData.length);
				this.removeConnInfo(connInfo);
				return ;
			}
			System.out.println("blockNumber="+blockNumber);
			RandomAccessFile rf = null;
			try{
				rf = new RandomAccessFile(f,"r");
				long offset = blockNumber * blockSize;
				rf.seek(offset);
			
				int size = (int)Math.min(f.length()-offset,blockSize);
				byte[] data = new byte[size];
				
				int count = 0,tempCount;
				while(count < size){
					tempCount = rf.read(data, count, size-count);
					if(tempCount < 0){
						throw new Exception("Read File Error");
					}
					count += tempCount;
				}
				byte[] sdata = TransferProtocol.createNestSendData(data, 0, size, itemInfo.id, blockNumber);
				
				int writeNum = connInfo.writeData(sdata,0,sdata.length);
				if(writeNum != sdata.length){
					System.out.println("BUG:"+sdata.length+" "+writeNum);
				}
			}finally{
				if(rf != null){
					rf.close();
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public void destroy(){
		this.isWorking = false;
		this.notifySend();
		ThreadPoolGroupServer.getSingleInstance().removeThreadPool(this.poolName);
	}
}
