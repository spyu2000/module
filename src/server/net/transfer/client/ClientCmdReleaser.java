package server.net.transfer.client;

import java.io.RandomAccessFile;
import java.nio.ByteBuffer;

import com.fleety.base.StrFilter;
import com.fleety.base.Util;

import server.net.transfer.TransferProtocol;
import server.net.transfer.container.QueueContainer;
import server.net.transfer.container.QueueContainer.QueueItemInfo;
import server.socket.inter.CmdInfo;
import server.socket.inter.ConnectSocketInfo;
import server.socket.inter.ICmdReleaser;

public class ClientCmdReleaser implements ICmdReleaser {
	private NetTransferClient server = null;
	public void init(Object caller) {
		this.server = (NetTransferClient)caller;
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
		}else{
			int msg = Integer.parseInt(msgObj.toString());
			if(msg == TransferProtocol.HEART_MSG){
				System.out.println("Transfer Heart MSG");
			}else if(msg == TransferProtocol.UPLOAD_STATUS_RESPONSE_MSG){
				this.disposeUploadStatusResponse(info,connInfo);
			}else if(msg == TransferProtocol.DOWNLOAD_STATUS_RESPONSE_MSG){
				this.disposeDownloadStatus(info, connInfo);
			}
		}
	}

	private void disposeUploadStatusResponse(CmdInfo info,ConnectSocketInfo connInfo){
		ByteBuffer buff = (ByteBuffer)info.getInfo(CmdInfo.DATA_FLAG);
		String infoStr = new String(buff.array());
		System.out.println(infoStr);
		String[] arr = StrFilter.split(infoStr, "\t");
		
		QueueItemInfo sInfo = this.server.getSendInfoByName(arr[1]);
		if(sInfo != null){
			boolean needSave = sInfo.id == null || !sInfo.id.equals(arr[0]);
			sInfo.updateItemEnvInfo(arr[0], Integer.parseInt(arr[3]), Integer.parseInt(arr[2]), Util.bcdStr2ByteArr(arr[4]));
			if(sInfo.isFinished()){
				sInfo.updateWorkStatus(QueueContainer.WORK_STATUS_FINISH);
				needSave = true;
			}
			if(needSave){
				this.server.getSendQueueContainer().updateAndSaveQueue();
				this.server.triggerTaskChanged(sInfo);
			}
			if(!sInfo.isWorking()){
				this.server.newSendTaskArrived();
			}
			
			this.server.triggerSendProgress(sInfo);
		}
		this.server.notifyUploadNow();
	}
	
	private void disposeDownloadStatus(CmdInfo info,ConnectSocketInfo connInfo){
		ByteBuffer buff = (ByteBuffer)info.getInfo(CmdInfo.DATA_FLAG);
		String infoStr = new String(buff.array());
		System.out.println("Client:"+infoStr);
		String[] arr = StrFilter.split(infoStr, "\t");
		
		String localId = arr[0];
		String remoteId = arr[1];
		int result = Integer.parseInt(arr[2]);
		long size = Long.parseLong(arr[3]);
		long time = Long.parseLong(arr[4]);
		int concurrentNum = Integer.parseInt(arr[5]);
		int blockSize = Integer.parseInt(arr[6]);
		byte[] finishStatus = Util.bcdStr2ByteArr(arr[7]);
		
		QueueItemInfo itemInfo = this.server.getReceiveQueueContainer().getQueueItemInfoById(localId);
		if(itemInfo == null){
			return ;
		}
		if(result == 0){
			itemInfo.updateWorkStatus(QueueContainer.WORK_STATUS_UNVALID);
			this.server.triggerTaskChanged(itemInfo);
			this.server.getReceiveQueueContainer().updateAndSaveQueue();
			return ;
		}
		this.server.triggerSendProgress(itemInfo);

		itemInfo.size = size;
		itemInfo.lastModifiedTime = time;
		itemInfo.name = remoteId;
		itemInfo.updateItemEnvInfo(localId, blockSize, concurrentNum, finishStatus);
		this.server.getReceiveQueueContainer().updateAndSaveQueue();

		RandomAccessFile rf = null;
		try{
			rf = new RandomAccessFile(itemInfo.getQueueReceiveTempFile(),"rw");
			rf.setLength(size);
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			if(rf != null){
				try{
					rf.close();
				}catch(Exception e){}
			}
		}
		this.server.notifyDownloadNow();
	}
}
