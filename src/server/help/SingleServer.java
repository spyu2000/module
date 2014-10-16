package server.help;

import java.io.RandomAccessFile;

import server.threadgroup.ThreadPoolGroupServer;

import com.fleety.server.BasicServer;
import com.fleety.util.pool.timer.FleetyTimerTask;

public class SingleServer extends BasicServer {
	private RandomAccessFile out = null;
	public boolean startServer() {
		String tempStr = this.getStringPara("single_port");
		if(tempStr == null || tempStr.trim().length() == 0){
			tempStr = "single_server_port";
		}
		tempStr = tempStr.trim();
		try{
			FleetyTimerTask task = null;
			ThreadPoolGroupServer.getSingleInstance().createTimerPool("single_detect_timer").schedule(task = new FleetyTimerTask(){
				public void run(){
					System.out.println("Single Lock Failure!");
					System.exit(0);
				}
			}, 5000);
			out = new RandomAccessFile(tempStr,"rws");
			out.getChannel().lock();
			task.cancel();
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public void stopServer(){
		super.stopServer();
		try{
			if(out != null){
				out.close();
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		ThreadPoolGroupServer.getSingleInstance().destroyTimerPool("single_detect_timer");
	}
}
