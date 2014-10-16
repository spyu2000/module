package server.data.sync;

import server.socket.inter.ConnectSocketInfo;

public interface IDataSync {
	public boolean releaseDataObject(SyncObj obj);
	
	public void dataArrived(SyncObj obj,ConnectSocketInfo conn);
}
