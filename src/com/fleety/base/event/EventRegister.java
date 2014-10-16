/**
 * ¾øÃÜ Created on 2008-12-5 by edmund
 */
package com.fleety.base.event;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import server.threadgroup.PoolInfo;
import server.threadgroup.ThreadPoolGroupServer;

import com.fleety.base.CostStatLog;
import com.fleety.util.pool.thread.ITask;
import com.fleety.util.pool.thread.ThreadPool;
import com.fleety.util.pool.thread.ThreadPoolEventListener;

public class EventRegister{
	private String poolName = null;
	private ThreadPool pool = null;
	public EventRegister(){
		this(1000);
	}
	public EventRegister(int maxMsgNum){
		try{
			this.poolName = "EventRegister["+this.hashCode()+"]";
			PoolInfo pInfo = new PoolInfo();
			pInfo.poolType = ThreadPool.SINGLE_TASK_LIST_POOL;
			pInfo.workersNumber = 1;
			pInfo.taskCapacity = maxMsgNum;
			pInfo.isDaemo = true;
			pInfo.priority = Thread.MAX_PRIORITY;
			this.pool = ThreadPoolGroupServer.getSingleInstance().createThreadPool(this.poolName, pInfo);
			
			this.pool.addEventListener(new ThreadPoolEventListener() {
				private long lastPrintTime = 0;
				public void eventHappen(EventInfo event) {
					if(event.getEventType()==EventInfo.QUEUE_TASK_OVERFLOW_EVENT){
						if(System.currentTimeMillis() - this.lastPrintTime > 300000){
							System.out.println("task overflow!!!task size:"+event.getSource());
						}
					}
				}
			
			});
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	private HashMap mapping = new HashMap();
	public void addEventListener(int type,IEventListener listener){
		if(listener == null){
			return ;
		}
		System.out.println("Event Listener Add:"+type+" "+listener.getClass().getName());
		
		List list = (List)mapping.get(new Integer(type));
		
		if(list == null){
			synchronized(mapping){
				list = (List)mapping.get(new Integer(type));
				if(list == null){
					list = new LinkedList();
					mapping.put(new Integer(type), list);
				}
			}
		}
		
		synchronized(list){
			list.add(listener);
		}
	}
	
	public void removeEventListener(int type,IEventListener listener){
		if(listener == null){
			return ;
		}
		List list = (List)mapping.get(new Integer(type));
		
		if(list != null){
			synchronized(list){
				if(list.remove(listener)){
					listener.destroy();
				}
			}
		}
	}
	
	public void updateAllEventListener(HashMap newMapping){
		System.out.println("Event Listener Full Update:size="+newMapping.size());
		
		synchronized(this.mapping){
			Iterator itr = this.mapping.keySet().iterator(),itr1;
			List list;
			IEventListener listener;
			while(itr.hasNext()){
				list = (List)this.mapping.get(itr.next());
				if(list != null){
					itr1 = list.iterator();
					while(itr1.hasNext()){
						listener = (IEventListener)itr1.next();
						listener.destroy();
					}
				}
			}
		}
		
		this.mapping = newMapping;
	}
	
	public int getEventNum(int listNo){
		return this.pool.getTaskNum(listNo);
	}
	
	public void dispatchEvent(Event e){
		this.pool.addTask(new RunCaller(e));
	}
	
	private class RunCaller implements ITask{
		private Event e = null;
		public RunCaller(Event e){
			this.e = e;
		}
		
		public Object getFlag(){return null;}
		public String getDesc(){return null;}
		
		public boolean execute() throws Exception{
			int eventType = e.getEventType();
			List list = (List)mapping.get(new Integer(eventType));
			
			if(list != null){
				synchronized(list){
					for(Iterator itr = list.iterator();itr.hasNext();){
						long beginTime=System.currentTimeMillis();
						IEventListener listener=(IEventListener)itr.next();
						listener.eventHappen(e);

						CostStatLog.getSingleInstance().addCostInfo("EventRegister["+EventRegister.this.poolName+"]",eventType+"-"+listener.getClass().getName(),System.currentTimeMillis()-beginTime);
					}
				}
			}
			
			return true;
		}
	}
	
	public static void main(String[] argv){
		EventRegister register = new EventRegister();
		
		register.addEventListener(1, new EventListenerAdapter(){
			public void eventHappen(Event e){
				System.out.println(e);
			}
			public void setPara(Object key,Object value){}
		});
		for(int i=0;i<10;i++){
			register.dispatchEvent(new Event(1,null,null));
		}
		System.out.println("2222");
	}
}
