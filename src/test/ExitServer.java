package test;

import java.util.Timer;
import java.util.TimerTask;

import com.fleety.base.GeneralConst;
import com.fleety.base.InfoContainer;

import server.remote_execute_by_rmi.client.RemoteExecuteByRmiClient;
import server.remote_execute_by_rmi.client.help.RemoteCommandExecute;

public class ExitServer extends RemoteExecuteByRmiClient {
	private Timer timer = null;
	private long delay = 1000;
	private long cycle = GeneralConst.ONE_DAY_TIME;
	private long offset = GeneralConst.ONE_HOUR_TIME;
	
	public boolean startServer(){
		boolean isSuccess = super.startServer();
		
		String tempStr;
		
		tempStr = this.getStringPara("delay");
		if(tempStr != null && tempStr.trim().length() > 0){
			this.delay = Long.parseLong(tempStr.trim())*1000;
		}
		tempStr = this.getStringPara("cycle");
		if(tempStr != null && tempStr.trim().length() > 0){
			this.cycle = Long.parseLong(tempStr.trim())*GeneralConst.ONE_HOUR_TIME;
		}
		tempStr = this.getStringPara("offset");
		if(tempStr != null && tempStr.trim().length() > 0){
			this.offset = Long.parseLong(tempStr.trim())*GeneralConst.ONE_HOUR_TIME;
		}
		
		this.timer = new Timer();
		this.timer.schedule(new ExitTask(),delay);
		
		return isSuccess;
	}
	
	private void sendExitCommand(){
		try{
			System.out.println("·¢ËÍ¹Ø±ÕÃüÁî!");
			InfoContainer para = new InfoContainer();
			para.setInfo("op", "exit");
			InfoContainer result = (InfoContainer)this.remoteRmiExecute(RemoteCommandExecute.class.getName(), para, true);
			if(result.getBoolean("result")!=null &&  result.getBoolean("result") .booleanValue()){
				System.out.println("¹Ø±Õ³É¹¦!");
			}else{
				System.out.println("¹Ø±ÕÊ§°Ü!");
			}
			
		}catch(Exception e){
			e.printStackTrace();
			System.out.println("·¢ËÍÃüÁîÊ§°Ü¡£");
		}finally{
			long nextDelay = this.cycle;
			if(Math.random() >= 0.5){
				nextDelay += Math.round(Math.random() * this.offset);
			}else{
				nextDelay -= Math.round(Math.random() * this.offset);
			}
			System.out.println("Delay: "+nextDelay);
			this.timer.schedule(new ExitTask(), nextDelay);
		}
	}
	
	private class ExitTask extends TimerTask{
		public void run(){
			sendExitCommand();
		}
	}
}
