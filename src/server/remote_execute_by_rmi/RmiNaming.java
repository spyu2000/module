package server.remote_execute_by_rmi;

import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.HashMap;

import com.fleety.base.GeneralConst;
import com.fleety.util.pool.timer.FleetyTimerTask;
import com.fleety.util.pool.timer.TimerPool;

import server.threadgroup.ThreadPoolGroupServer;

public class RmiNaming {
	public static final RmiNaming singleInstance = new RmiNaming();
	
	private TimerPool timerPool = null;
	private RmiNaming(){
		timerPool = ThreadPoolGroupServer.getSingleInstance().createTimerPool("_rmi_regist_detect_");
		timerPool.schedule(new FleetyTimerTask(){
			public void run(){
				RmiNaming.this.autoDetect();
			}
		}, GeneralConst.ONE_DAY_TIME, GeneralConst.ONE_DAY_TIME);
	}
	public static RmiNaming getSingleInstance(){
		return singleInstance;
	}
	
	private HashMap mapping = new HashMap();
	
	public void rebind(String name, Remote obj) throws RemoteException, java.net.MalformedURLException{
		Naming.rebind(name,obj);
		
		synchronized(mapping){
			mapping.put(name, obj);
		}
	}
	
	public void unbind(String name) throws RemoteException,NotBoundException,java.net.MalformedURLException{
		Naming.unbind(name);

		synchronized(mapping){
			mapping.remove(name);
		}
	}
	
	private void autoDetect(){
		String[] objArr = null;
		synchronized(mapping){
			objArr = new String[this.mapping.size()];
			this.mapping.keySet().toArray(objArr);
		}
		
		String name;
		Remote remote;
		for(int i=0;i<objArr.length;i++){
			try{
				name = objArr[i];

				synchronized(mapping){
					remote = (Remote)this.mapping.get(name);
				}

				if(remote != null){
					if(Naming.lookup(name) == null){
						Naming.rebind(name,remote);
					}
				}
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}
}
