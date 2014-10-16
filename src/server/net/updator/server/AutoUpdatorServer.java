package server.net.updator.server;

import com.fleety.util.pool.timer.TimerPool;

import server.socket.help.LongCmdReader;
import server.socket.serversocket.FleetySocketServer;
import server.threadgroup.ThreadPoolGroupServer;

public class AutoUpdatorServer extends FleetySocketServer {
	private String curVersion = null;
	private String updatePath = null;
	private String poolName = "updator_download_helper";
	private TimerPool timerPool = null;
	
	public boolean startServer() {
		this.curVersion = this.getStringPara("lastest_version");
		if(this.curVersion != null && this.curVersion.trim().length() == 0){
			this.curVersion = null;
		}
		this.updatePath = this.getStringPara("update_path");
		if(this.updatePath == null){
			this.updatePath = "version_update";
		}
		int timerNum = 5;
		String tempStr = this.getStringPara("updator_thread_num");
		if(tempStr != null && tempStr.trim().length() > 0){
			try{
				timerNum = Integer.parseInt(tempStr.trim());
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		
		this.timerPool = ThreadPoolGroupServer.getSingleInstance().createTimerPool(poolName,timerNum);
		this.addPara("reader", LongCmdReader.class.getName());
		this.addPara("releaser", MsgDisposer.class.getName());
		
		return super.startServer();
	}
	public void stopServer(){
		super.stopServer();
		ThreadPoolGroupServer.getSingleInstance().destroyTimerPool(poolName);
	}
	
	public String getCurVersion(){
		return this.curVersion;
	}
	public TimerPool getTimerPool(){
		return this.timerPool;
	}
	public String getUpdatePath(){
		return this.updatePath;
	}
}
