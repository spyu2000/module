/**
 * ¾øÃÜ Created on 2009-8-11 by edmund
 */
package server.distribute;

import com.fleety.base.InfoContainer;

public final class TaskInfo extends InfoContainer{
	public static final int CREATE_STATUS = 1;
	public static final int RELEASE_STATUS = 2;
	public static final int FINISHED_STATUS = 3;
	
	public static final Object RESULT_EXTRA_RETURN_INFO_FLAG = new Object();
	
	private String serverName = null;
	private long id = 0;
	private long timeout = 10000l;
	
	private int status = CREATE_STATUS;
	private long statusTime = System.currentTimeMillis();
	
	public TaskInfo(String serverName){
		this.serverName = serverName;
		this.id = TaskInfo.getAvaId();
	}
	
	public long getId(){
		return this.id;
	}
	
	public void setServerName(String serverName){
		this.serverName = serverName;
	}
	
	public String getServerName(){
		return this.serverName;
	}
	
	public void setTimeout(long timeout){
		this.timeout = timeout;
	}
	public long getTimeout(){
		return this.timeout;
	}
	
	public void updateStatus(int status){
		this.status = status;
		this.statusTime = System.currentTimeMillis();
	}
	public int getStatus(){
		return this.status;
	}
	public long getStatusTime(){
		return this.statusTime;
	}
	
	private static long ava_id = 0;
	private synchronized static long getAvaId(){
		long id = ++TaskInfo.ava_id;
		if(id == Long.MAX_VALUE){
			TaskInfo.ava_id = 0;
		}
		return id;
	}
}
