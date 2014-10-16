package server.net.transfer.container;

import java.io.File;

import server.net.transfer.TransferProtocol;
import server.net.transfer.client.NetTransferClient;
import server.net.transfer.container.QueueContainer.QueueItemInfo;

import com.fleety.util.pool.thread.BasicTask;

public class ClientSendTask extends BasicTask {
	private NetTransferClient transfer = null;
	private QueueContainer sContainer = null;
	private boolean isStop = false;
	
	public ClientSendTask(NetTransferClient transfer,QueueContainer sContainer){
		this.transfer = transfer;
		this.sContainer = sContainer;
	}
	
	public void stop(){
		this.isStop = true;
		synchronized(this){
			this.notify();
		}
	}

	public boolean execute() throws Exception {
		while(!isStop){
			synchronized(this){
				this.wait(10000);
			}
			QueueItemInfo sInfo = this.sContainer.getFirstTask();
			if(sInfo != null){
				if(!sInfo.isValid()){
					this.sContainer.updateAndSaveQueue();
					this.transfer.triggerTaskChanged(sInfo);
				}else{
					this.createSendRequest(sInfo);
				}
			}
		}
		
		return true;
	}

	private void createSendRequest(QueueItemInfo sInfo){
		if(sInfo.isWorking()){
			sInfo.detectWorkStatus(600000);
			return ;
		}
		
		if(sInfo.canToWorking()){
			if(!sInfo.switchToWorking()){
				return ;
			}
			File f = sInfo.getQueueSendFile();
			if(!f.exists()){
				sInfo.updateWorkStatus(QueueContainer.WORK_STATUS_UNVALID);
				this.sContainer.updateAndSaveQueue();
				this.transfer.triggerTaskChanged(sInfo);
				return ;
			}
			if(f.length() != sInfo.size || f.lastModified() != sInfo.lastModifiedTime){
				sInfo.updateWorkStatus(QueueContainer.WORK_STATUS_UNVALID);
				this.sContainer.updateAndSaveQueue();
				this.transfer.triggerTaskChanged(sInfo);
				return ;
			}
			if(sInfo.isFinished()){
				this.sContainer.updateAndSaveQueue();
				this.transfer.triggerTaskChanged(sInfo);
				return ;
			}
			this.transfer.addDataSendTask(sInfo);
		}else{
			File f = sInfo.getQueueSendFile();
			sInfo.size = f.length();
			if(!f.exists() || sInfo.size == 0){
				sInfo.updateWorkStatus(QueueContainer.WORK_STATUS_UNVALID);
				this.sContainer.updateAndSaveQueue();
				this.transfer.triggerTaskChanged(sInfo);
				return ;
			}
			byte[] requestData = TransferProtocol.createUploadStatusRequest(sInfo);
			try{
				transfer.sendData(requestData, 0, requestData.length);
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}
}
