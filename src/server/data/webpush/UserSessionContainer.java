package server.data.webpush;

import java.util.HashMap;

import com.fleety.util.pool.timer.FleetyTimerTask;

import server.threadgroup.ThreadPoolGroupServer;

public class UserSessionContainer {
	private static final UserSessionContainer singleInstance = new UserSessionContainer();
	public static UserSessionContainer getSingleInstance(){
		return singleInstance;
	}
	
	private static final String TIMER_NAME = "expire_conn_detect_timer";
	private UserSessionContainer(){
		ThreadPoolGroupServer.getSingleInstance().createTimerPool(TIMER_NAME).schedule(new FleetyTimerTask(){
			public void run(){
				scanExpireConn();
			}
		}, 60000, 30000);
	}
	
	private void scanExpireConn(){
		UserSession[] arr = this.getAllUserSession();

		for(int i=0;i<arr.length;i++){
			arr[i].detectHeart();
			
			if(!arr[i].isValid()){
				System.out.println("Expire Destroy User Session:"+arr[i].getUserFlag());
				this.removeSession(arr[i].getUserFlag());
			}
		}
	}
	
	private HashMap userMapping = new HashMap();
	public UserSession getUserSession(String userFlag){
		return this.getUserSession(userFlag, 600000l);
	}
	public UserSession getUserSession(String userFlag, long validDuration){
		UserSession session = null;
		synchronized(userMapping){
			session = (UserSession)userMapping.get(userFlag);
			if(session == null){
				session = new UserSession(userFlag,validDuration);
				System.out.println("Create User Session:"+userFlag);
				userMapping.put(userFlag, session);
			}
		}
		return session;
	}
	
	public boolean existUserSession(String userFlag){
		synchronized(userMapping){
			return this.userMapping.containsKey(userFlag);
		}
	}
	
	public UserSession removeSession(String userFlag){
		synchronized(userMapping){
			return (UserSession)this.userMapping.remove(userFlag);
		}
	}
	
	public UserSession[] getAllUserSession(){
		UserSession[] arr;
		synchronized(userMapping){
			arr = new UserSession[userMapping.size()];
			userMapping.values().toArray(arr);
		}
		return arr;
	}
	
}
