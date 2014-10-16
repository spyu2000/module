package server.net.transfer.container;

import server.net.transfer.TransferProtocol;
import server.net.transfer.client.NetTransferClient;
import server.net.transfer.container.QueueContainer.QueueItemInfo;

import com.fleety.util.pool.thread.BasicTask;

public class ClientReceiveTask extends BasicTask {
	private NetTransferClient transfer = null;
	private QueueContainer sContainer = null;
	private boolean isStop = false;
	
	public ClientReceiveTask(NetTransferClient transfer,QueueContainer sContainer){
		this.transfer = transfer;
		this.sContainer = sContainer;
	}
	
	public boolean execute() throws Exception {
		while(!isStop){
			synchronized(this){
				this.wait(5000);
			}
			QueueItemInfo sInfo = this.sContainer.getFirstTask();
			if(sInfo != null){
				if(!sInfo.isValid()){
					this.sContainer.updateAndSaveQueue();
					this.transfer.triggerTaskChanged(sInfo);
				}else{
					this.createReceiveRequest(sInfo);
				}
			}
		}
		
		return true;
	}

	private void createReceiveRequest(QueueItemInfo sInfo){
		if(sInfo.isWorking()){
			sInfo.detectWorkStatus(600000);
			return ;
		}
		if(sInfo.canToWorking()){
			if(!sInfo.switchToWorking()){
				return ;
			}
			if(sInfo.isFinished()){
				this.sContainer.updateAndSaveQueue();
				this.transfer.triggerTaskChanged(sInfo);
				return ;
			}
			this.transfer.addDataReceiveTask(sInfo);
		}else{
			byte[] requestData = TransferProtocol.createDownloadStatusRequest(sInfo);
			try{
				transfer.sendData(requestData, 0, requestData.length);
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}
	
	public void stop(){
		this.isStop = true;
		synchronized(this){
			this.notify();
		}
	}
}
