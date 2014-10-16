package com.fleety.base.datastruct.map;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.TimerTask;

import com.fleety.base.datastruct.EventAdapater;
import com.fleety.base.datastruct.IEventListener;
import com.fleety.util.pool.timer.FleetyTimerTask;

import server.threadgroup.ThreadPoolGroupServer;

public class AutoClearHashMap<K,V> extends Hashtable<K,V> {
	public static final int DATA_REMOVE_EVENT = 1;
	private HashMap keyTimeMapping = null;
	private long timeout = -1;
	private FleetyTimerTask task = null;
	
	public AutoClearHashMap(){
		super();
		this.initData();
	}
	public AutoClearHashMap(int initialCapacity) {
		super(initialCapacity);
		this.initData();
	}
	public AutoClearHashMap(int initialCapacity, float loadFactor) {
		super(initialCapacity,loadFactor);
		this.initData();
	}
	
	public synchronized V put(K key, V value) {
		this.keyTimeMapping.put(key, new Long(System.currentTimeMillis()));
		return super.put(key, value);
	}
	public synchronized V remove(Object key) {
		this.keyTimeMapping.remove(key);
		return super.remove(key);
	}
	public synchronized void clear() {
		this.keyTimeMapping.clear();
		super.clear();
	}
	public synchronized void updateKeyTime(Object key){
		this.keyTimeMapping.put(key, new Long(System.currentTimeMillis()));
	}
	
	private void initData(){
		this.keyTimeMapping = new HashMap();
	}

	public void setTimeout(long timeout){
		this.timeout=timeout;
		this.setTimeout(timeout, Math.round(this.timeout/2));
	}
	public void setTimeout(long timeout,long cycle){
		this.timeout = timeout;
		if(this.task != null){
			this.task.cancel();
		}
		if(this.timeout >= 1000){
			this.task = new AutoClearTask(new WeakReference(this));
			ThreadPoolGroupServer.getSingleInstance().createTimerPool("hashmap_auto_timeout_detect").schedule(this.task, cycle, cycle);
		}
	}
	public long getTimeout(){
		return this.timeout;
	}
	public synchronized Long getInputTime(Object key){
		return (Long)this.keyTimeMapping.get(key);
	}
	
	private ArrayList listenerList = null;
	public synchronized void addEventListener(IEventListener listener){
		if(listener == null){
			return ;
		}
		
		if(this.listenerList == null){
			this.listenerList = new ArrayList(2);
		}
		this.listenerList.add(listener);
	}
	public synchronized void removeEventListener(IEventListener listener){
		if(listener == null){
			return ;
		}
		this.listenerList.remove(listener);
	}
	protected synchronized boolean triggerEventWillHappen(int eventType,Object[] paraInfoArr){
		if(this.listenerList == null){
			return true;
		}
		
		boolean isGoOn = true;
		IEventListener listener ;
		for(Iterator itr = this.listenerList.iterator();itr.hasNext();){
			listener = (IEventListener)itr.next();
			isGoOn &= listener.eventWillHappen(eventType, paraInfoArr, this);
		}
		return isGoOn;
	}
	protected synchronized void triggerEventHappened(int eventType,Object[] paraInfoArr){
		if(this.listenerList == null){
			return ;
		}
		IEventListener listener ;
		for(Iterator itr = this.listenerList.iterator();itr.hasNext();){
			listener = (IEventListener)itr.next();
			listener.eventHappened(eventType, paraInfoArr, this);
		}
	}
	protected synchronized void clearTimeKey(){
		for(Iterator itr = this.keyTimeMapping.keySet().iterator();itr.hasNext();){
			if(!this.containsKey(itr.next())){
				itr.remove();
			}
		}
	}
	
	public static void main(String[] argv) throws Exception{
		AutoClearHashMap a = new AutoClearHashMap();
		a.addEventListener(new EventAdapater(){
			public boolean eventWillHappen(int eventType,Object[] paraInfoArr,Object source){
				System.out.println("Will:"+paraInfoArr[0]);
				if(paraInfoArr[0].toString().equals("1")){
					return false;
				}
				return true;
			}
			public void eventHappened(int eventType,Object[] paraInfoArr,Object source){
				System.out.println("Happen:"+paraInfoArr[0]);
			}
		});
		a.put("1", "2");
		a.put("3", "2");
		a.setTimeout(4000);
		a.put("2", "2");
		a.put("4", "2");
		a = null;

		Thread.sleep(6000);
		System.gc();

		System.gc();
		System.gc();
		Thread.sleep(10000);
		a = null;
	}
}
