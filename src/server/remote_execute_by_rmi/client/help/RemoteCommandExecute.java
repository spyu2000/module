package server.remote_execute_by_rmi.client.help;

import java.util.Timer;
import java.util.TimerTask;

import com.fleety.base.InfoContainer;

import server.remote_execute_by_rmi.IRemoteExecute;

public class RemoteCommandExecute extends TimerTask implements IRemoteExecute {
	public Object execute(InfoContainer para) {
		InfoContainer result = new InfoContainer();
		result.setInfo("RESULT", Boolean.FALSE);
		String op = para.getString("op");
		if(op == null){
			return result;
		}
		if(op.equals("exit")){
			new Timer().schedule(this, 1000);
			
			result.setInfo("result", Boolean.TRUE);
		}else{
			
		}
		
		return result;
	}
	
	public void run(){
		System.exit(99);
	}
}
