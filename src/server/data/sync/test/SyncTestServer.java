package server.data.sync.test;

import java.io.Serializable;
import java.util.Timer;
import java.util.TimerTask;

import server.data.sync.client.DataSyncServer;
import server.data.sync.container.ISyncInfo;
import server.data.sync.container.SyncDataContainer;
import server.threadgroup.ThreadPoolGroupServer;

import com.fleety.server.BasicServer;
import com.fleety.util.pool.timer.FleetyTimerTask;
import com.fleety.util.pool.timer.TimerPool;

public class SyncTestServer extends BasicServer implements Serializable{
	private int seq = 1;
	public boolean startServer() {
		TimerPool timer = ThreadPoolGroupServer.getSingleInstance().createTimerPool(ThreadPoolGroupServer._QUICK_EXECUTE_OBJ_NAME_);
		timer.schedule(new FleetyTimerTask(){
			public void run(){
				int curSeq = seq++;
				SyncDataContainer.getSingleInstance().updateInfo(new CarProperty("cp-"+DataSyncServer.getSingleInstance().getNodeFlag()+"-"+curSeq));
				SyncDataContainer.getSingleInstance().updateInfo(new CarInfo("co-"+DataSyncServer.getSingleInstance().getNodeFlag()+"-"+curSeq));
				System.out.println(SyncDataContainer.getSingleInstance().getInfo("CarProperty"));
				System.out.println(SyncDataContainer.getSingleInstance().getInfo("CarInfo"));
			}
		}, 10000, 10000);
		
		return true;
	}

	public class CarProperty implements ISyncInfo{
		private String mdtId = null;
		public CarProperty(String mdtId){
			this.mdtId = mdtId;
		}
		
		public Serializable[] getFlagArr(){
			return new Serializable[]{"CarProperty",this.mdtId};
		}
	}
	public class CarInfo implements ISyncInfo{
		private String mdtId = null;
		public CarInfo(String mdtId){
			this.mdtId = mdtId;
		}
		
		public Serializable[] getFlagArr(){
			return new Serializable[]{"CarInfo",this.mdtId};
		}
	}
}
