/**
 * 绝密 Created on 2008-8-20 by edmund
 */
package com.fleety.util.pool.thread;

public interface ThreadPoolEventListener{	
	public void eventHappen(EventInfo event);
	
	public class EventInfo{
		//任务队列溢出事件
		public static final int QUEUE_TASK_OVERFLOW_EVENT = 1;
		//任务超时执行事件
		public static final int TASK_OVERTIME_EXECUTE_EVENT = 2;
		//所有线程空闲事件
		public static final int ALL_THREAD_IDLE_EVENT = 3;
		
		private int eventType;
		private ThreadPool pool;
		private Object source;
		
		public EventInfo(int eventType,ThreadPool pool,Object source){
			this.eventType = eventType;
			this.pool = pool;
			this.source = source;
		}

		public int getEventType(){
			return eventType;
		}
		public ThreadPool getPool(){
			return pool;
		}
		public Object getSource(){
			return source;
		}
	}
}
