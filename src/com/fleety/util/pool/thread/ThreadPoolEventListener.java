/**
 * ���� Created on 2008-8-20 by edmund
 */
package com.fleety.util.pool.thread;

public interface ThreadPoolEventListener{	
	public void eventHappen(EventInfo event);
	
	public class EventInfo{
		//�����������¼�
		public static final int QUEUE_TASK_OVERFLOW_EVENT = 1;
		//����ʱִ���¼�
		public static final int TASK_OVERTIME_EXECUTE_EVENT = 2;
		//�����߳̿����¼�
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
