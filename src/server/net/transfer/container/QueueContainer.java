package server.net.transfer.container;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;

import server.net.transfer.IDataTask;
import server.net.transfer.ITransfer;
import server.threadgroup.PoolInfo;
import server.threadgroup.ThreadPoolGroupServer;

import com.fleety.base.StrFilter;
import com.fleety.base.Util;
import com.fleety.util.pool.thread.BasicTask;
import com.fleety.util.pool.thread.ThreadPool;

public class QueueContainer {
	public static final int WORK_STATUS_WAIT = 0;
	public static final int WORK_STATUS_PENDING = 1;
	public static final int WORK_STATUS_FINISH = 2;
	public static final int WORK_STATUS_CANCEL = 3;
	public static final int WORK_STATUS_UNVALID = 4;
	
	private ITransfer transfer = null;
	private File queueFile = null;
	private LinkedList queue = new LinkedList();
	
	public QueueContainer(ITransfer transfer,File f) throws Exception{
		this.transfer = transfer;
		this.queueFile = f;

		this.loadQueue();
	}
	
	public QueueItemInfo getFirstTask(){
		synchronized(this.queue){
			if(this.queue.size() == 0){
				return null;
			}
			return (QueueItemInfo)this.queue.get(0);
		}
	}
	public QueueItemInfo getQueueItemInfoByName(String name){
		synchronized(this.queue){
			QueueItemInfo sInfo;
			for(Iterator itr = this.queue.iterator();itr.hasNext();){
				sInfo = (QueueItemInfo)itr.next();
				if(sInfo.name.equals(name)){
					return sInfo;
				}
			}
		}
		return null;
	}
	public QueueItemInfo getQueueItemInfoById(String id){
		synchronized(this.queue){
			QueueItemInfo sInfo;
			for(Iterator itr = this.queue.iterator();itr.hasNext();){
				sInfo = (QueueItemInfo)itr.next();
				if(sInfo.id != null && sInfo.id.equals(id)){
					return sInfo;
				}
			}
		}
		return null;
	}
	
	public boolean addDownloadTask(QueueItemInfo itemInfo){
		synchronized(this.queue){
			this.queue.add(itemInfo);
		}
		
		if(this.updateAndSaveQueue()){
			//发布事件
			return true;
		}else{
			synchronized(this.queue){
				this.queue.remove(itemInfo);
			}
		}
		return false;
	}

	public QueueItemInfo addUploadTask(File f,boolean isOnce,long maxDuration,boolean needDeleteFile,String appendInfo){
		QueueItemInfo sInfo = new QueueItemInfo();
		if(f.exists() && f.isFile()){
			sInfo.size = f.length();
			sInfo.lastModifiedTime = f.lastModified();
		}else{
			return null;
		}
		sInfo.name = f.getAbsolutePath();
		sInfo.appendInfo = appendInfo;
		sInfo.needDeleteFile = needDeleteFile?1:0;
		if(maxDuration >= 0){
			sInfo.expiredTime = System.currentTimeMillis()+maxDuration;
		}else{
			sInfo.expiredTime = -1;
		}

		synchronized(this.queue){
			this.queue.add(sInfo);
		}
		if(this.updateAndSaveQueue()){
			this.transfer.newSendTaskArrived();
			return sInfo;
		}else{
			synchronized(this.queue){
				this.queue.remove(sInfo);
			}
		}
		return null;
	}
	public boolean addReceiveTask(QueueItemInfo itemInfo){
		try{
			File rf = itemInfo.getQueueReceiveTempFile();
			if(rf != null){
				rf.getParentFile().mkdirs();
				RandomAccessFile f = new RandomAccessFile(rf,"rw");
				f.setLength(itemInfo.size);
				f.close();
			}
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}
		synchronized(this.queue){
			this.queue.add(itemInfo);
		}
		if(this.updateAndSaveQueue()){
			//发布事件
			return true;
		}else{
			synchronized(this.queue){
				this.queue.remove(itemInfo);
			}
		}
		System.out.println("Save Error");
		return false;
	}
	
	private int securityKey = 0x00;
	private void loadQueue() throws Exception{
		if(!queueFile.exists()){
			return ;
		}
		
		BufferedReader reader = null;
		
		try{
			byte[] data = Util.loadFileWithSecurity(this.queueFile.getName(), securityKey);
			ByteArrayInputStream in = new ByteArrayInputStream(data);
			reader = new BufferedReader(new InputStreamReader(in));
			String str;
			QueueItemInfo sInfo;
			while((str = reader.readLine()) != null){
				sInfo = new QueueItemInfo();
				sInfo.parser(str);
				synchronized(this.queue){
					this.queue.add(sInfo);
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			if(reader != null){
				try{
					reader.close();
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		}
	}
	public boolean updateAndSaveQueue(){
		QueueItemInfo sInfo;
		StringBuffer strBuff = new StringBuffer(1024);
		synchronized(this.queue){
			for(Iterator itr=this.queue.iterator();itr.hasNext();){
				sInfo = (QueueItemInfo)itr.next();
				if(!sInfo.isValid()){
					if(sInfo.isNeedDeleteFile()){
						if(deleteFilePool != null){
							deleteFilePool.addTask(new DeleteFileTask(sInfo));
						}
					}
					sInfo.moveToReceiveFile();
					itr.remove();
					continue;
				}
				
				if(strBuff.length() > 0){
					strBuff.append("\n");
				}
				strBuff.append(sInfo.toString());
			}
			
			return Util.saveFileWithSecurity(strBuff.toString().getBytes(), this.queueFile.getAbsolutePath(),securityKey);
		}
	}
	
	public QueueItemInfo[] getAllQueueItemInfo(){
		synchronized(this.queue){
			QueueItemInfo[] arr = new QueueItemInfo[this.queue.size()];
			this.queue.toArray(arr);
			return arr;
		}
	}
	
	
	public class QueueItemInfo{
		public String id = "";
		public String name = "";
		public long size = 0;
		public long lastModifiedTime = 0;
		public int needDeleteFile = 0;
		public long expiredTime = 0;
		public String appendInfo = "";
		private int status = WORK_STATUS_WAIT;
		
		private boolean hasWorkStatus = false;
		public int blockSize = 0;
		public int concurrentNum = 0;
		private long finishBlockNum = 0;
		public byte[] finishStatus = null;
		private byte[] workFinishStatus = null;
		public boolean isWorking = false;
		
		private ArrayList socketList = new ArrayList();
		
		public QueueContainer getQueueContainer(){
			return QueueContainer.this;
		}
		public void updateItemEnvInfo(String id,int blockSize,int concurrentNum,byte[] finishStatus){
			this.hasWorkStatus = blockSize > 0;
			this.id = id;
			if(blockSize > 0){
				this.blockSize = blockSize;
			}
			if(concurrentNum > 0){
				this.concurrentNum = concurrentNum;
			}
			this.finishStatus = finishStatus;
		}
		public boolean isNeedDeleteFile(){
			return this.needDeleteFile == 1;
		}
		public int getBlockSize(){
			return this.blockSize;
		}
		public int getConcurrentNum(){
			return this.concurrentNum;
		}
		public long getRemainBlockNum(){
			return this.getTotalBlockNum()-this.getFinishBlockNum();
		}
		public File getQueueSendFile(){
			return new File(this.name);
		}
		public File getQueueReceiveFile(){
			String path = QueueContainer.this.transfer.getSavePath(this);
			if(path != null){
				return new File(path);
			}
			if(this.id == null || this.id.trim().length() == 0){
				return null;
			}
			return new File(this.id);
		}
		public File getQueueReceiveTempFile(){
			File f = this.getQueueReceiveFile();
			if(f == null){
				return null;
			}
			return new File(f.getParentFile(),f.getName()+".temp");
		}
		public boolean moveToReceiveFile(){
			boolean deleteOk = true;
			if(this.status != QueueContainer.WORK_STATUS_FINISH){
				if(this.getQueueReceiveTempFile() != null){
					deleteOk = this.getQueueReceiveTempFile().delete();
					System.out.println("Del:"+this.getQueueReceiveTempFile().getAbsolutePath()+" del:"+deleteOk);
				}
				return deleteOk;
			}else if(this.getQueueReceiveTempFile() != null && this.getQueueReceiveTempFile().exists()){
				deleteOk = this.getQueueReceiveFile().delete();
				boolean isOk =  this.getQueueReceiveTempFile().renameTo(this.getQueueReceiveFile());
				System.out.println("Rename: "+this.getQueueReceiveFile().getAbsolutePath()+" del:"+deleteOk+" mv:"+isOk);
				return isOk;
			}
			return true;
		}
		public synchronized void finishBlock(long blockNumber){
			finishStatus[(int)(blockNumber/8)] |= (0x80>>(blockNumber%8));
		}
		public byte[] getFinishStatus(){
			return this.finishStatus;
		}
		public long getFinishBlockNum(){
			this.getProgress();
			return this.finishBlockNum;
		}
		public long getTotalBlockNum(){
			if(this.blockSize <= 0){
				return 0;
			}
			return (this.size-1)/this.blockSize+1;
		}
		public boolean isFinishedForBlockNumber(long blockNumber){
			if(this.blockSize <= 0){
				return false;
			}
			return (finishStatus[(int)(blockNumber/8)] & (0x80>>(blockNumber%8))) > 0;
		}
		public int getProgress(){
			if(this.finishStatus == null){
				return 0;
			}
			if(this.blockSize <= 0){
				return 0;
			}
			
			long totalBlockNum = (this.size-1)/this.blockSize+1;
			long byteNum = totalBlockNum/8;
			int v,bit,limit;
			long finish = 0;
			for(int i=0;i<this.finishStatus.length;i++){
				v = this.finishStatus[i];
				limit = 8;
				if(i>=byteNum){
					limit = (int)(totalBlockNum%8);
				}
				bit = 0x80;
				for(int k=0;k<limit;k++){
					if((v&bit) > 0){
						finish ++;
					}
					bit >>= 1;
				}
			}
			this.finishBlockNum = finish;
			return (int)(finish*100/totalBlockNum);
		}
		public synchronized boolean isFinished(){
			this.getProgress();
			if(this.blockSize <= 0){
				return false;
			}
			if(this.finishBlockNum == (this.size-1)/this.blockSize+1){
				this.status = WORK_STATUS_FINISH;
				return true;
			}
			return false;
		}
		
		public void printProgress(){
			System.out.println("["+this.id+"] Progress:"+this.getProgress());
		}
		
		public synchronized void socketAdd(BasicTask task){
			this.socketList.add(task);
			this.updateWorkStatus(QueueContainer.WORK_STATUS_PENDING);
		}
		public synchronized void socketMinus(BasicTask task){
			if(this.socketList.remove(task)){
				if(this.socketList.size() == 0){
					this.isWorking = false;
				}
			}
		}
		public synchronized void updateWorkStatus(int status){
			this.status = status;
		}
		public synchronized int getWorkStatus(){
			return this.status;
		}
		
		public boolean canToWorking(){
			return this.hasWorkStatus;
		}
		public boolean isWorking(){
			return this.isWorking;
		}
		public synchronized void detectWorkStatus(long limitTime){
			IDataTask task;
			for(Iterator itr = this.socketList.iterator();itr.hasNext();){
				task = (IDataTask)itr.next();
				if(!task.isAlive(limitTime)){
					task.stop();
				}
			}
		}
		public boolean switchToWorking(){
			if(this.isWorking()){
				return false;
			}
			this.isWorking = true;
			this.workFinishStatus = finishStatus;
			this.hasWorkStatus = false;
			return true;
		}
		public synchronized int getNextSendBlockNumber(){
			int blockNumber = -1;
			if(this.workFinishStatus == null){
				return blockNumber;
			}
			
			int v;
			int bit = 0x80;
			boolean isGoOn = true;
			for(int i=0;i<this.workFinishStatus.length && isGoOn;i++){
				v = this.workFinishStatus[i]&0xFF;
				if(v == 0xFF){
					continue;
				}
				blockNumber = i*8;
				bit = 0x80;
				for(int k=0;k<8;k++){
					if((v&bit) == 0){
						isGoOn = false;
					}else{
						bit >>= 1;
						blockNumber ++;
					}
				}
				this.workFinishStatus[i] |= bit;
			}

			if(isGoOn || blockNumber >= (this.size-1)/this.blockSize + 1){
				blockNumber = -1;
			}
			
			return blockNumber;
		}
		
		public void parser(String lineInfo) throws Exception{
			String[] arr = StrFilter.split(lineInfo,"\t");
			this.id = arr[0];
			this.name = arr[1];
			this.needDeleteFile = Integer.parseInt(arr[2]);
			this.expiredTime = Long.parseLong(arr[3]);
			this.size = Long.parseLong(arr[4]);
			this.lastModifiedTime = Long.parseLong(arr[5]);
			if(arr[6].length() > 0){
				this.appendInfo = new String(Util.bcdStr2ByteArr(arr[6]));
			}
			if(arr[7].length() > 0){
				this.finishStatus = Util.bcdStr2ByteArr(arr[7]);
			}
			if(arr[8].length() > 0){
				this.blockSize = Integer.parseInt(arr[8]);
			}
			if(arr[9].length() > 0){
				this.concurrentNum = Integer.parseInt(arr[9]);
			}
		}
		
		public synchronized boolean isValid(){
			if(this.expiredTime >=0 && System.currentTimeMillis() > this.expiredTime){
				this.status = WORK_STATUS_UNVALID;
				return false;
			}
			return this.status==WORK_STATUS_WAIT || this.status==WORK_STATUS_PENDING;
		}
		
		public String toString(){
			StringBuffer strBuff = new StringBuffer(256);
			strBuff.append(id);
			strBuff.append("\t");
			strBuff.append(name);
			strBuff.append("\t");
			strBuff.append(needDeleteFile);
			strBuff.append("\t");
			strBuff.append(expiredTime);
			strBuff.append("\t");
			strBuff.append(size);
			strBuff.append("\t");
			strBuff.append(lastModifiedTime);
			strBuff.append("\t");
			if(appendInfo != null){
				byte[] data = appendInfo.getBytes();
				strBuff.append(Util.byteArr2BcdStr(data, 0, data.length));
			}
			strBuff.append("\t");
			if(this.finishStatus != null){
				strBuff.append(Util.byteArr2BcdStr(this.finishStatus, 0, this.finishStatus.length));
			}
			strBuff.append("\t");
			strBuff.append(this.blockSize);
			strBuff.append("\t");
			strBuff.append(this.concurrentNum);
			return strBuff.toString();
		}
	}
	
	private class DeleteFileTask extends BasicTask{
		private QueueItemInfo sInfo = null;
		public DeleteFileTask(QueueItemInfo sInfo){
			this.sInfo = sInfo;
		}
		
		public boolean execute(){
			if(this.sInfo == null){
				return false;
			}
			
			File f = this.sInfo.getQueueSendFile();
			boolean isSuccess = true;
			if(f != null && f.exists()){
				isSuccess = f.delete();
			}
			System.out.println("Delete UploadFile: "+f.getAbsolutePath()+"   "+isSuccess);
			return true;
		}
	}
	
	private static ThreadPool deleteFilePool = null;
	static{
		PoolInfo pInfo = new PoolInfo();
		pInfo.workersNumber = 1;
		pInfo.taskCapacity = 1000;
		try{
			deleteFilePool = ThreadPoolGroupServer.getSingleInstance().createThreadPool("_source_file_delete_", pInfo);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
