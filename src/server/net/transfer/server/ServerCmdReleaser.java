package server.net.transfer.server;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;

import com.fleety.base.GeneralConst;
import com.fleety.base.StrFilter;
import com.fleety.base.Util;

import server.net.transfer.TransferProtocol;
import server.net.transfer.container.QueueContainer;
import server.net.transfer.container.QueueContainer.QueueItemInfo;
import server.socket.inter.CmdInfo;
import server.socket.inter.ConnectSocketInfo;
import server.socket.inter.ICmdReleaser;

public class ServerCmdReleaser implements ICmdReleaser {
	public static final Object DATA_DOWNLOAD_REQUEST_FLAG = new Object();
	public static final Object MAIN_CONNECT_ID_FLAG = new Object();
	
	private NetTransferServer server = null;
	public void init(Object caller) {
		this.server = (NetTransferServer)caller;
	}

	public void releaseCmd(CmdInfo info) {
		Object msgObj = info.getInfo(CmdInfo.CMD_FLAG);
		ConnectSocketInfo connInfo = (ConnectSocketInfo)info.getInfo(CmdInfo.SOCKET_FLAG);
		if(msgObj == CmdInfo.SOCKET_CONNECT_CMD){
			System.out.println("Connect:"+connInfo.getRemoteSocketAddress());
			try{
				connInfo.switchSendMode2Thread(1024*1024);
			}catch(Exception e){
				e.printStackTrace();
			}
		}else if(msgObj == CmdInfo.SOCKET_DISCONNECT_CMD){
			System.out.println("DisConnect:"+connInfo.getRemoteSocketAddress());
			removeMainConnect(connInfo);
			this.server.getDataDownloadHelper().removeConnInfo(connInfo);
		}else{
			int msg = Integer.parseInt(msgObj.toString());
			System.out.println("ArriveMsg:"+msg);
			if(msg == TransferProtocol.HEART_MSG){
				byte[] hData = TransferProtocol.createHeartData();
				try{
					connInfo.writeData(hData, 0, hData.length);
				}catch(Exception e){
					e.printStackTrace();
				}
			}else if(msg == TransferProtocol.DISCONNECT_MSG){
				byte[] hData = TransferProtocol.createDisconnectData();
				try{
					connInfo.writeData(hData, 0, hData.length);
				}catch(Exception e){
					e.printStackTrace();
				}
			}else if(msg == TransferProtocol.UPLOAD_STATUS_REQUEST_MSG){
				this.disposeUploadStatusRequest(info, connInfo);
			}else if(msg == TransferProtocol.DOWNLOAD_STATUS_REQUEST_MSG){
				this.disposeDownloadStatusRequest(info,connInfo);
			}else if(msg == TransferProtocol.NEST_TRANSFER_DATA_MSG){
				this.disposeNestTransferData(info, connInfo);
			}else if(msg == TransferProtocol.DOWNLOAD_DATA_REQUEST_MSG){
				this.disposeDownloadDataRequest(info, connInfo);
			}
		}
	}
	
	private void disposeUploadStatusRequest(CmdInfo info,ConnectSocketInfo connInfo){
		ByteBuffer buff = (ByteBuffer)info.getInfo(CmdInfo.DATA_FLAG);
		String infoStr = new String(buff.array());
		System.out.println(infoStr);
		String[] arr = StrFilter.split(infoStr, "\t");
		String id = arr[0];
		String name = arr[1];
		long size = Long.parseLong(arr[2]);
		long time = Long.parseLong(arr[3]);
		String appendInfo = "";
		if(arr[4].length() > 0){
			appendInfo = new String(Util.bcdStr2ByteArr(arr[4]));
		}

		QueueContainer receiveContainer = this.server.getReceiveQueueContainer();
		QueueItemInfo itemInfo = receiveContainer.getQueueItemInfoById(id);
		if(itemInfo == null){
			itemInfo = receiveContainer.new QueueItemInfo();
			itemInfo.name = name;
			this.createItemInfo(this.server.getUniqueFile().getAbsolutePath(), size, time, appendInfo, itemInfo);
			itemInfo.expiredTime = System.currentTimeMillis()+GeneralConst.ONE_DAY_TIME;
			if(receiveContainer.addReceiveTask(itemInfo)){
				this.server.triggerReceiveProgress(itemInfo);
			}
		}else{
			if(itemInfo.size != size || itemInfo.lastModifiedTime != time){
				itemInfo.name = name;
				this.createItemInfo(itemInfo.id, size, time, appendInfo, itemInfo);
				receiveContainer.updateAndSaveQueue();
				this.server.triggerReceiveProgress(itemInfo);
			}
		}
		byte[] rData = TransferProtocol.createUploadStatusResponse(itemInfo.id, name, itemInfo.getBlockSize(), (int)Math.min(itemInfo.getConcurrentNum(),itemInfo.getRemainBlockNum()), itemInfo.getFinishStatus());
		try{
			connInfo.writeData(rData, 0, rData.length);
		}catch(Exception e){
			e.printStackTrace();
		}
		connInfo.setInfo(MAIN_CONNECT_ID_FLAG, itemInfo.id);
		putMainConnect(connInfo);
	}
	private void disposeNestTransferData(CmdInfo info,ConnectSocketInfo connInfo){
		ByteBuffer buff = (ByteBuffer)info.getInfo(CmdInfo.DATA_FLAG);
		int uniqueNameLen = buff.getInt();
		String id = new String(buff.array(),4,uniqueNameLen);
		buff.position(buff.position()+uniqueNameLen);
		long blockNumber = buff.getLong();
		QueueItemInfo itemInfo = this.server.getReceiveQueueContainer().getQueueItemInfoById(id);
		if (itemInfo == null) {
			System.out.println("Not Exist:"+id);
			connInfo.closeSocket();
		} else {
			RandomAccessFile f = null;
			try {
				if(itemInfo.isFinishedForBlockNumber(blockNumber)){
					return ;
				}
				
				File rf = itemInfo.getQueueReceiveTempFile();
				rf.getParentFile().mkdirs();
				f = new RandomAccessFile(rf, "rw");
				f.seek(blockNumber * itemInfo.getBlockSize());
				f.write(buff.array(), buff.position(), buff.remaining());
				f.close();

				itemInfo.finishBlock(blockNumber);
				if(itemInfo.isFinished()){
					itemInfo.updateWorkStatus(QueueContainer.WORK_STATUS_FINISH);
					
					//需要保障外部的有效通知
					this.notifySave();
				}
				this.server.getReceiveQueueContainer().updateAndSaveQueue();
				
				try {
					ConnectSocketInfo mainConnInfo = getMainConnect(id);
					if (mainConnInfo != null) {
						byte[] rData = TransferProtocol.createUploadStatusResponse(id,
										itemInfo.name, itemInfo.getBlockSize(),
										itemInfo.getConcurrentNum(),
										itemInfo.getFinishStatus());
						mainConnInfo.writeData(rData, 0, rData.length);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				this.server.triggerReceiveProgress(itemInfo);
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				try {
					if (f != null) {
						f.close();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public void notifySave(){
		
	}
	
	private void disposeDownloadStatusRequest(CmdInfo info,ConnectSocketInfo connInfo){
		ByteBuffer buff = (ByteBuffer)info.getInfo(CmdInfo.DATA_FLAG);
		String infoStr = new String(buff.array());
		System.out.println(infoStr);
		String[] arr = StrFilter.split(infoStr, "\t");
		String id = arr[0];
		String name = arr[1];
		long size = Long.parseLong(arr[2]);
		long time = Long.parseLong(arr[3]);
		int blockSize = Integer.parseInt(arr[4]);
		int concurrentNum = Integer.parseInt(arr[5]);
		byte[] finishStatus = Util.bcdStr2ByteArr(arr[6]);
		String appendInfo = "";
		if(arr[7].length() > 0){
			appendInfo = new String(Util.bcdStr2ByteArr(arr[7]));
		}

		byte[] rData = null;
		File remoteFile = this.server.getRemoteFileCapture().getRemoteFile(appendInfo);
		if(remoteFile == null || !remoteFile.isFile() || !remoteFile.exists()){
			rData = TransferProtocol.createDownloadStatusResponse(id,name,false,0,0,0,0,new byte[0]);
		}else{
			QueueContainer sendContainer = this.server.getSendQueueContainer();
			QueueItemInfo itemInfo = sendContainer.getQueueItemInfoById(name);
			if(itemInfo == null){
				itemInfo = sendContainer.new QueueItemInfo();
				//创建唯一性id
				itemInfo.id = getUniqueID();
				itemInfo.name = remoteFile.getAbsolutePath();
				itemInfo.expiredTime = System.currentTimeMillis() + GeneralConst.ONE_HOUR_TIME;
				this.createItemInfo(itemInfo.id, remoteFile.length(), remoteFile.lastModified(), appendInfo, itemInfo);
				blockSize = itemInfo.blockSize;
				concurrentNum = itemInfo.concurrentNum;
				
				if(sendContainer.addDownloadTask(itemInfo)){
					this.server.triggerReceiveProgress(itemInfo);
				}
			}else{
				if(size != remoteFile.length() || remoteFile.lastModified() != time){
					itemInfo.id = getUniqueID();
					itemInfo.name = remoteFile.getAbsolutePath();
					this.createItemInfo(itemInfo.id, remoteFile.length(), remoteFile.lastModified(), appendInfo, itemInfo);
				}else{
					itemInfo.id = getUniqueID();
					itemInfo.name = remoteFile.getAbsolutePath();
					itemInfo.finishStatus = finishStatus;
					itemInfo.blockSize = blockSize;
					itemInfo.concurrentNum = concurrentNum;
				}
			}
			
			itemInfo.isWorking = false;
			itemInfo.switchToWorking();
			rData = TransferProtocol.createDownloadStatusResponse(id, itemInfo.id, true, remoteFile.length(), remoteFile.lastModified(), blockSize, concurrentNum, itemInfo.finishStatus);
		}

		try{
			connInfo.writeData(rData, 0, rData.length);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	private void disposeDownloadDataRequest(CmdInfo info,ConnectSocketInfo connInfo){
		ByteBuffer buff = (ByteBuffer)info.getInfo(CmdInfo.DATA_FLAG);
		String infoStr = new String(buff.array());
		System.out.println(infoStr);
		String[] arr = StrFilter.split(infoStr, "\t");
		String id = arr[0];
		QueueItemInfo itemInfo = this.server.getSendQueueContainer().getQueueItemInfoById(id);
		System.out.println(id+"   "+itemInfo);
		if(itemInfo == null){
			connInfo.closeSocket();
		}else{
			connInfo.setMaxCachSize(itemInfo.blockSize * 2 + 200);
			connInfo.setInfo(DATA_DOWNLOAD_REQUEST_FLAG, id);
			this.server.getDataDownloadHelper().addConnInfo(connInfo);
		}
	}
	
	private void createItemInfo(String uniqueName,long size,long time,String appendInfo,QueueItemInfo itemInfo){
		long concurrentNum = (size - 1)/102400 + 1;
		if(concurrentNum > 40){
			concurrentNum = 40;
		}
		
		long blockSize = size/concurrentNum;
		if(blockSize > 1024*1024){
			blockSize = 1024*1024;
		}
		blockSize = ((blockSize-1)/1024 + 1)*1024;
		concurrentNum = (concurrentNum-1)/4+1;
		
		byte[] finishStatus = new byte[(int)((((size-1)/blockSize+1)-1)/8+1)];
		
		itemInfo.updateItemEnvInfo(uniqueName, (int)blockSize, (int)concurrentNum, finishStatus);
		itemInfo.size = size;
		itemInfo.lastModifiedTime = time;
		itemInfo.appendInfo = appendInfo;
	}
	
	
	private static ArrayList mainConnectList = new ArrayList();
	private static void putMainConnect(ConnectSocketInfo connInfo){
		synchronized(mainConnectList){
			if(!mainConnectList.contains(connInfo)){
				mainConnectList.add(connInfo);
			}
		}
	}
	private static void removeMainConnect(ConnectSocketInfo connInfo){
		synchronized(mainConnectList){
			mainConnectList.remove(connInfo);
		}
	}
	private static ConnectSocketInfo getMainConnect(String id){
		if(id == null){
			return null;
		}
		synchronized(mainConnectList){
			ConnectSocketInfo connInfo;
			Object idFlag;
			for(Iterator itr = mainConnectList.iterator();itr.hasNext();){
				connInfo = (ConnectSocketInfo)itr.next();
				idFlag = connInfo.getInfo(MAIN_CONNECT_ID_FLAG);
				if(idFlag != null && idFlag.equals(id)){
					return connInfo;
				}
			}
		}
		return null;
	}
	
	private static int uid = 1;
	private synchronized static String getUniqueID(){
		int t = uid++;
		
		if(uid >= Integer.MAX_VALUE - 100){
			uid = 1;
		}
		
		return System.currentTimeMillis()+"-"+t;
	}
}
