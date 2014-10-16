package server.data.sync.client;

import server.data.sync.SyncObj;
import server.socket.inter.CmdInfo;
import server.socket.inter.ConnectSocketInfo;
import server.socket.inter.ICmdReleaser;

public class DataReleaser implements ICmdReleaser {	
	public static int MAX_DATA_SIZE = 1024*1024*5;
	private DataSyncServer dataSync = null;
	public void init(Object caller) {
		this.dataSync = (DataSyncServer)caller;
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
			
			this.dataSync.startLogin();
		}else if(cmd == CmdInfo.SOCKET_DISCONNECT_CMD){
			System.out.println("与控制服务中心断开,关闭服务!");
			System.exit(-1);
		}else{
			if(obj instanceof SyncObj){
				this.dataSync.dataArrived((SyncObj)obj,conn);
			}
		}
	}
}
