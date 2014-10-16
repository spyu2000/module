package server.data.sync.server;

import server.data.sync.SyncObj;
import server.socket.inter.CmdInfo;
import server.socket.inter.ConnectSocketInfo;
import server.socket.inter.ICmdReleaser;

public class DataReleaser implements ICmdReleaser {
	private static int MAX_DATA_SIZE = 1024*1024*5;
	private DataSyncServer dataSync = null;
	public void init(Object caller) {
		this.dataSync = (DataSyncServer)caller;
		Integer obj = this.dataSync.getIntegerPara("max_cache_size");
		if(obj != null){
			
		}
	}

	public void releaseCmd(CmdInfo info) {
		Object cmd = info.getInfo(CmdInfo.CMD_FLAG);
		Object obj = info.getInfo(CmdInfo.DATA_FLAG);
		ConnectSocketInfo conn = (ConnectSocketInfo)info.getInfo(CmdInfo.SOCKET_FLAG);
		
		
		if(cmd == CmdInfo.SOCKET_CONNECT_CMD){
			try{
				conn.switchSendMode2Thread(MAX_DATA_SIZE);
			}catch(Exception e){
				e.printStackTrace();
			}
		}else if(cmd == CmdInfo.SOCKET_DISCONNECT_CMD){
			
		}else{
			if(obj instanceof SyncObj){
				this.dataSync.dataArrived((SyncObj)obj,conn);
			}
		}
	}
}
