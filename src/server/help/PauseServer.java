/**
 * ¾øÃÜ Created on 2008-6-20 by edmund
 */
package server.help;

import com.fleety.base.FleetyThread;
import com.fleety.server.BasicServer;

public class PauseServer extends BasicServer{
	
	public boolean startServer(){
		try{
			int pauseTime = Integer.parseInt(this.getStringPara("pause_time"),10);
			FleetyThread.sleep(pauseTime);
		}catch(Exception e){
			e.printStackTrace();
		}
		this.isRunning = true;
		return true;
	}

	public void stopServer(){
		this.isRunning = false;
	}
}
