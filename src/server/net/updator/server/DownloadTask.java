package server.net.updator.server;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;

import com.fleety.util.pool.timer.FleetyTimerTask;

import server.socket.inter.ConnectSocketInfo;

public class DownloadTask extends FleetyTimerTask {
	private ConnectSocketInfo connInfo = null;
	private BufferedInputStream in = null;
	private int blockSize = 64*1024;
	public DownloadTask(ConnectSocketInfo connInfo,File f,int blockSize) throws Exception{
		this.connInfo = connInfo;
		this.in = new BufferedInputStream(new FileInputStream(f));
		this.blockSize = blockSize;
	}
	public void run() {
		try{
			if(connInfo.getMaxCachSize() - connInfo.getCurSize() < this.blockSize){
				return ;
			}
			byte[] arr = new byte[this.blockSize];
			int count = this.in.read(arr);
			if(count < 0){
				this.closeTask();
				return ;
			}
			
			connInfo.writeData(arr, 0, count);
		}catch(Exception e){
			e.printStackTrace();
			this.connInfo.closeSocket();
			this.closeTask();
		}
	}

	private void closeTask(){
		this.cancel();
		try{
			if(this.in != null){
				this.in.close();
			}
		}catch(Exception e){}
	}
}
