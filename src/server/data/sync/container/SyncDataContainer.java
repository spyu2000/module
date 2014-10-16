package server.data.sync.container;

import java.io.Serializable;

import server.data.sync.SyncObj;
import server.data.sync.client.DataSyncServer;

public class SyncDataContainer extends BasicDataContainer {
	private static SyncDataContainer singleInstance = null;
	public static SyncDataContainer getSingleInstance(){
		return singleInstance;
	}
	
	private int seq = 1;
	public SyncDataContainer(){
		this.flagArr = new String[]{"Sync Root"};
	}
	
	private synchronized int getSeq(){
		if(this.seq == Integer.MAX_VALUE){
			this.seq = 1;
		}
		
		return this.seq ++;
	}
	
	private DataSyncServer client = null;
	public void register(DataSyncServer client){
		SyncDataContainer.singleInstance = this;
		this.client = client;
	}
	
	public ISyncInfo getInfo(Serializable[] flagArr){
		return this.getInfo(flagArr, -1);
	}
	
	public boolean removeInfo(ISyncInfo info){
		return this.removeInfo(info, true);
	}
	public synchronized boolean removeInfo(ISyncInfo info,boolean isSync){
		boolean isOk = this.removeInfo(info, -1);
		if(isSync){
			if(this.client != null){
				this.client.releaseDataObject(new SyncObj(this.client.getNodeFlag(),this.getSeq(),info));
			}
		}
		return isOk;
	}

	public boolean updateInfo(ISyncInfo info) {
		return this.updateInfo(info, true);
	}
	public synchronized boolean updateInfo(ISyncInfo info,boolean isSync) {
		boolean isOk = this.updateInfo(info, -1);
		if(isSync){
			if(this.client != null){
				this.client.releaseDataObject(new SyncObj(this.client.getNodeFlag(),this.getSeq(),info));
			}
		}
		return isOk;
	}
	
	public String getPrintInfo(ISyncInfo infoFlag){
		ISyncInfo info = this.getInfo(infoFlag.getFlagArr());
		return info.toString();
	}
}
