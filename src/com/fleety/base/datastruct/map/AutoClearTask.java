package com.fleety.base.datastruct.map;

import java.lang.ref.WeakReference;
import java.util.Iterator;

import com.fleety.util.pool.timer.FleetyTimerTask;

public class AutoClearTask extends FleetyTimerTask {
	private WeakReference ref = null;
	public AutoClearTask(WeakReference ref){
		this.ref = ref;
	}

	public void run() {
		AutoClearHashMap obj = (AutoClearHashMap)ref.get();
		if(obj == null){
			System.out.println("Obj AutoClearHashMap is GC!");
			this.cancel();
			return ;
		}
		
		long timeout = obj.getTimeout();
		Object key;
		Long value;
		Object rValue;
		synchronized(obj){
			for(Iterator itr = obj.keySet().iterator();itr.hasNext();){
				key = itr.next();
				value = obj.getInputTime(key);
				if(value == null){
					System.out.println("should not print for auto_clear_task!"+key);
					continue;
				}
				
				if(System.currentTimeMillis() - value.longValue() > timeout){
					rValue = obj.get(key);
					if(obj.triggerEventWillHappen(AutoClearHashMap.DATA_REMOVE_EVENT, new Object[]{key,rValue})){
						itr.remove();
						obj.triggerEventHappened(AutoClearHashMap.DATA_REMOVE_EVENT, new Object[]{key,rValue});
					}
				}
			}
			obj.clearTimeKey();
		}
	}

}
