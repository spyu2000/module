/**
 * 绝密 Created on 2010-9-16 by edmund
 */
package server.threadgroup;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Timer;

import com.fleety.server.BasicServer;
import com.fleety.util.pool.thread.BasicWorker;
import com.fleety.util.pool.thread.ITask;
import com.fleety.util.pool.thread.ThreadPool;
import com.fleety.util.pool.timer.FleetyTimerTask;
import com.fleety.util.pool.timer.TimerPool;

public class ThreadPoolGroupServer extends BasicServer{
	public static final Object _QUICK_EXECUTE_OBJ_NAME_ = new Object();
	
	private HashMap threadGroupMapping = null;
	private HashMap timerPoolMapping = null;
	private HashMap timerMapping = null;
	
	private static ThreadPoolGroupServer singleInstance = null;
	public static ThreadPoolGroupServer getSingleInstance(){
		if(singleInstance == null){
			synchronized(ThreadPoolGroupServer.class){
				if(singleInstance == null){
					singleInstance = new ThreadPoolGroupServer();
					singleInstance.startServer();
				}
			}
		}
		return singleInstance;
	}
	
	private ThreadPoolGroupServer(){
	}
	
	public boolean startServer(){
		if(this.isRunning()){
			return true;
		}

		try{
			Integer temp = null;
			temp = this.getIntegerPara("period");
			if(temp != null){
				period = temp.intValue() * 60 * 1000L;
			}
		} catch(Exception e){
			e.printStackTrace();
			return false;
		}

		this.threadGroupMapping = new HashMap(128);
		this.timerMapping = new HashMap(64);
		this.timerPoolMapping = new HashMap(64);
		
		this.isRunning = true;

		initTaskPoolContainerTask();
		return this.isRunning();
	}

	/**
	 * 建议使用createTimerPool
	 * @param timerName 定时器名称
	 * @return
	 */
	@Deprecated
	public Timer createTimer(Object timerName){
		return this.createTimer(timerName,true);
	}

	/**
	 * 建议使用createTimerPool
	 * @param timerName 定时器名称
	 * @param isDaemo
	 * @return
	 */
	@Deprecated
	public Timer createTimer(Object timerName,boolean isDaemo){
		if(!this.isRunning()){
			return null;
		}
		Timer timer = null;
		synchronized(this.timerMapping){
			timer = this.getTimer(timerName);
			if(timer == null){
				if(timerName == null){
					timer = new Timer(isDaemo);
				}else{
					timer = new Timer(timerName.toString(),isDaemo);
				}
				this.timerMapping.put(timerName, timer);
			}
		}
		return timer;
	}

	public TimerPool createTimerPool(Object timerPoolName){
		return this.createTimerPool(timerPoolName, 1, true);
	}
	public TimerPool createTimerPool(Object timerPoolName,int num){
		return this.createTimerPool(timerPoolName, num, true);
	}
	public TimerPool createTimerPool(Object timerPoolName,boolean isDaemo){
		return this.createTimerPool(timerPoolName, 1, isDaemo);
	}
	public TimerPool createTimerPool(Object timerPoolName,int num,boolean isDaemon){
		if(!this.isRunning()){
			return null;
		}
		if(timerPoolName == null){
			return null;
		}
		if(num <= 0){
			return null;
		}
		
		TimerPool timerPool = null;
		synchronized(this.timerPoolMapping){
			timerPool = this.getTimerPool(timerPoolName);
			if(timerPool == null){
				try{
					timerPool = new TimerPool(timerPoolName.toString(),num,isDaemon);
					this.timerPoolMapping.put(timerPoolName, timerPool);
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		}
		return timerPool;
	}
	
	public void destroyTimer(Object timerName){
		if(!this.isRunning()){
			return;
		}

		System.out.println("Remove Timer:"+timerName);
		Timer timer = null;
		synchronized(this.timerMapping){
			timer = (Timer)this.timerMapping.remove(timerName);
		}
		if(timer != null){
			timer.cancel();
		}
	}
	public Timer getTimer(Object timerName){
		if(!this.isRunning()){
			return null;
		}
		
		synchronized(this.timerMapping){
			return (Timer)this.timerMapping.get(timerName);
		}
	}
	public TimerPool getTimerPool(Object timerPoolName){
		if(!this.isRunning()){
			return null;
		}
		
		synchronized(this.timerPoolMapping){
			return (TimerPool)this.timerPoolMapping.get(timerPoolName);
		}
	}

	public void destroyTimerPool(Object timerPoolName){
		if(!this.isRunning()){
			return;
		}

		System.out.println("Remove TimerPool:"+timerPoolName);
		TimerPool timerPool = null;
		synchronized(this.timerPoolMapping){
			timerPool = (TimerPool)this.timerPoolMapping.remove(timerPoolName);
		}
		if(timerPool != null){
			timerPool.cancel();
		}
	}

	public ThreadPool createThreadPool(Object poolName,PoolInfo pInfo) throws Exception{
		if(!this.isRunning()){
			return null;
		}
		
		ThreadPool pool = this.getThreadPool(poolName);
		if(pool == null){
			System.out.println("Create ThreadPool:"+poolName);
			if(poolName == null){
				pool = new ThreadPool(pInfo.poolType,pInfo.workersNumber,pInfo.taskCapacity,BasicWorker.class,pInfo.isDaemo,pInfo.priority);
			}else{
				pool = new ThreadPool(poolName.toString(),pInfo.poolType,pInfo.workersNumber,pInfo.taskCapacity,BasicWorker.class,pInfo.isDaemo,pInfo.priority);
			}
			synchronized(this){
				this.threadGroupMapping.put(poolName, pool);
			}
		}else{
			System.out.println("对应池名["+poolName+"]的池已存在!");
		}
		
		return pool;
	}
	
	public ThreadPool removeThreadPool(Object poolName){
		return this.removeThreadPool(poolName, true);
	}
	public ThreadPool removeThreadPool(Object poolName,boolean isDetroyImmediately){
		if(!this.isRunning()){
			return null;
		}
        System.out.println("Remove ThreadPool:"+poolName+" "+isDetroyImmediately);
		
		ThreadPool pool = (ThreadPool)this.threadGroupMapping.remove(poolName);
		if(pool != null){
			pool.stopWork(isDetroyImmediately);
		}
		
		return pool;
	}
	
	public synchronized ThreadPool getThreadPool(Object poolName){
		if(!this.isRunning()){
			return null;
		}
		
		return (ThreadPool)this.threadGroupMapping.get(poolName);
	}
	
	public boolean addTask(String poolName,ITask task){
		if(!this.isRunning()){
			return false;
		}
		ThreadPool pool = this.getThreadPool(poolName);
		if(pool == null){
			return false;
		}
		pool.addTask(task);
		
		return true;
	}
	
	public synchronized void getThreadPoolInfo(StringBuffer buff){
		if(!this.isRunning() || buff == null){
			return ;
		}
		
		buff.append("线程池组服务信息:\n");
		
		String poolName;
		ThreadPool pool = null;
		int count = 0;
		for(Iterator itr = this.threadGroupMapping.keySet().iterator();itr.hasNext();){
			poolName = (String)itr.next();
			pool = this.getThreadPool(poolName);
			
			count ++;
			buff.append(count+":");
			buff.append("poolName="+poolName);
			buff.append("\n");
			pool.getThreadStackInfo(buff);
			buff.append("\n");
		}
	}
	
	public synchronized void stopServer(){
		this.isRunning = false;
		
		ThreadPool pool = null;
		for(Iterator itr = this.threadGroupMapping.values().iterator();itr.hasNext();){
			pool = (ThreadPool)itr.next();
			pool.stopWork();
		}
		
		this.threadGroupMapping.clear();
		
		Timer timer;
		for(Iterator itr = this.timerMapping.values().iterator();itr.hasNext();){
			timer = (Timer)itr.next();
			timer.cancel();
		}
		
		this.timerMapping.clear();
	}

	private TimerPool timer = null;
	private TaskPoolContainerTask task = null;

	static long period = 1 * 60 * 1000L;

	public synchronized void initTaskPoolContainerTask(){
		if(this.timer != null){
			return ;
		}

		this.timer = ThreadPoolGroupServer.getSingleInstance().createTimerPool("TaskPoolContainerTask");
		this.task = new TaskPoolContainerTask();
		this.timer.schedule(task, period, period);
	}

	class TaskPoolContainerTask extends FleetyTimerTask{
		public void run(){
			try{
				HashMap tempHm;
				synchronized(threadGroupMapping){
					tempHm = (HashMap) threadGroupMapping.clone();
				}
				Iterator it = tempHm.keySet().iterator();
				StringBuffer buf = new StringBuffer("");
				while(it.hasNext()){
					Object poolName = it.next();
					ThreadPool pool = (ThreadPool) tempHm.get(poolName);
					buf.append("ThreadPool:" + poolName
								+ ":"
								+ pool.getTaskNumInfo()+" finishNum:"+pool.getAndClearFinishTaskNum()+ "\n");
				}
				System.err.println(buf.toString());
			} catch(Exception e){
				e.printStackTrace();
			}
		}
	}
	
}
