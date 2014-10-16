/**
 * Created on 2006-9-26
 * @author: Jami
 * @copyright: fleety.com
 * @notice:绝密
 */
package com.fleety.util.pool.thread;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import server.notify.INotifyServer;
import com.fleety.base.FleetyThread;
import com.fleety.util.pool.IPool;
import com.fleety.util.pool.thread.ThreadPoolEventListener.EventInfo;

/**
 * @description 用线程池将车辆数据发送给地图
 */
public class ThreadPool implements IPool{
	/**
	 * 多个线程共享一个队列，先进先出。
	 */
	public static final Object SINGLE_TASK_LIST_POOL = new Object();
	/**
	 * 每个线程对应一个队列，先进先出。主要用途：某些相同的任务需要按照顺序先后执行。
	 */
	public static final Object MULTIPLE_TASK_LIST_POOL = new Object();
	
	//添加进来的总任务数
	private long totalTaskNum = 0;
	private int workerNum = 0;
	private Object workingCountLock = new Object();
	private int workingThreadNum = 0;
	private IWorker[] workers;
	private Object poolType = null;
	private int taskCapacity = 0;
	private boolean isDaemoThread = false;

	//注册到哪个任务list的循环计数对象
	private int registerLoopCount = 0;
	//线程池是否已启动
	private boolean isStart = false;
	//是否多任务list线程池
	private boolean isMultipleList = false;
	//任务列表数组。如果是单任务list，则该数组长度为1；如果为多任务list，该数组长度等于线程数。
	private List[] taskListArr = null;
	//任务和任务list的映射结构。以任务的flag为key.
	private HashMap taskListMapping = null;
	
	private String poolName = null;
    
    //发送队列溢出的循环周期
    private static long sendNotifyCycleTimeMinute=60*60*1000l;

	public ThreadPool() throws Exception {
		this(SINGLE_TASK_LIST_POOL, 1, 1000, BasicWorker.class);
	}
	
	public ThreadPool(Object poolType) throws Exception {
		this(poolType, 1, 1000, BasicWorker.class, false);
	}
	
	public ThreadPool(Object poolType, boolean isDameo) throws Exception {
		this(poolType, 1, 1000, BasicWorker.class, isDameo);
	}
	
	public ThreadPool(Object poolType, boolean isDameo,int priority) throws Exception {
		this(poolType, 1, 1000, BasicWorker.class, isDameo,priority);
	}

	public ThreadPool(Object poolType,int workersNumber) throws Exception {
		this(poolType, workersNumber, 1000, BasicWorker.class, false);
	}

	public ThreadPool(Object poolType,int workersNumber,boolean isDaemo) throws Exception {
		this(poolType, workersNumber, 1000, BasicWorker.class, isDaemo);
	}

	public ThreadPool(Object poolType,int workersNumber,boolean isDaemo,int priority) throws Exception {
		this(poolType, workersNumber, 1000, BasicWorker.class, isDaemo, priority);
	}

	public ThreadPool(Object poolType,int workersNumber, int taskCapacity) throws Exception {
		this(poolType, workersNumber, taskCapacity, BasicWorker.class, false);
	}

	public ThreadPool(Object poolType,int workersNumber, int taskCapacity, boolean isDaemo) throws Exception {
		this(poolType, workersNumber, taskCapacity, BasicWorker.class, isDaemo);
	}

	public ThreadPool(Object poolType,int workersNumber, int taskCapacity, boolean isDaemo,int priority) throws Exception {
		this(poolType, workersNumber, taskCapacity, BasicWorker.class, isDaemo, priority);
	}
	
	public ThreadPool(Object poolType,int workersNumber,int taskCapacity, Class workerType)throws Exception {
		this(poolType, workersNumber, taskCapacity, workerType, false);
	}
	public ThreadPool(Object poolType,int workersNumber,int taskCapacity, Class workerType, boolean isDaemo)throws Exception {
		this(poolType, workersNumber, taskCapacity, workerType, isDaemo, Thread.NORM_PRIORITY);
	}
	public ThreadPool(Object poolType,int workersNumber,int taskCapacity, Class workerType, boolean isDaemo, int priority)throws Exception {
		this(null,poolType, workersNumber, taskCapacity, workerType, isDaemo, Thread.NORM_PRIORITY);
	}
	public ThreadPool(String poolName,Object poolType,int workersNumber,int taskCapacity, Class workerType, boolean isDaemo, int priority)throws Exception {
		this.poolName = poolName;
		
		this.isStart = false;
		this.registerLoopCount = 0;
		this.isDaemoThread = isDaemo;
		
		this.taskCapacity = taskCapacity;
		this.workerNum = workersNumber;
		this.poolType = poolType;
		
		if(this.poolType != SINGLE_TASK_LIST_POOL && this.poolType != MULTIPLE_TASK_LIST_POOL){
			throw new Exception("错误的线程池类型!");
		}
		if(taskCapacity <= 0 || workersNumber <= 0){
			throw new Exception("错误参数!任务队列容量="+taskCapacity+";线程数="+workersNumber);
		}
		
		this.isMultipleList = (this.poolType == MULTIPLE_TASK_LIST_POOL);
		//构建一定数量的队列
		if(this.isMultipleList){
			this.taskListArr = new List[this.workerNum];
			this.taskListMapping = new HashMap();
		}else{
			this.taskListArr = new List[1];
		}
		int taskListNum = this.taskListArr.length;
		for(int i=0;i<taskListNum;i++){
			this.taskListArr[i] = new LinkedList();
		}
		this.workers = new IWorker[this.workerNum];
		for (int i = 0; i < this.workerNum; i++) {
			workers[i] = (IWorker) workerType.newInstance();
			workers[i].setPriority(priority);
			workers[i].setDaemon(this.isDaemoThread);
			workers[i].setPool(this);
			if(this.poolName != null){
				workers[i].setName(this.poolName+"-"+(i+1));
			}
			
			if(this.isMultipleList){ //如果是多队列模式，则每个队列对应一个线程。
				workers[i].setTaskList(this.taskListArr[i]);
			}else{ 
				workers[i].setTaskList(this.taskListArr[0]);
			}
			
			workers[i].start();
		}
		
		this.isStart = true;
	}
	
	public Object getPoolType(){
		return this.poolType;
	}
	
	public int getTaskNum(int listNo){
		if(listNo < 0 || listNo >= this.taskListArr.length){
			return -1;
		}
		return this.taskListArr[listNo].size();
	}
	public long getTotalTaskNum(){
		return this.totalTaskNum;
	}
	
	public String getPoolName(){
		return this.poolName;
	}
	
	public int getThreadNum(){
		return this.workerNum;
	}
	public int getWorkThreadNum(){
		return this.workingThreadNum;
	}
	
	protected void threadAlive(IWorker worker){
		
	}
	protected void threadDead(IWorker worker){
		this.assignTask2OtherThread(worker);
	}
	
	protected void threadStartWork(IWorker worker){
		synchronized(workingCountLock){
			this.workingThreadNum ++;
		}
	}
	protected void threadEndWork(IWorker worker){
		synchronized(workingCountLock){
			this.workingThreadNum --;
		}
		if(this.workingThreadNum == 0){
			this.triggerEventListener(new EventInfo(EventInfo.ALL_THREAD_IDLE_EVENT,this,null));
		}
	}
	
	private List listenerList = null;
	public void addEventListener(ThreadPoolEventListener listener){
		if(listener == null){
			return ;
		}
		if(this.listenerList == null){
			this.listenerList = new LinkedList();
		}
		synchronized(this.listenerList){
			this.listenerList.add(listener);
		}
	}
	public void removeEventListener(ThreadPoolEventListener listener){
		if(this.listenerList == null){
			return ;
		}
		synchronized(this.listenerList){
			this.listenerList.remove(listener);
		}
	}
	public void triggerEventListener(EventInfo event){
		if(this.listenerList == null){
			return ;
		}
		ThreadPoolEventListener listener;
		synchronized(this.listenerList){
			for(Iterator itr = this.listenerList.iterator();itr.hasNext();){
				listener = (ThreadPoolEventListener)itr.next();
				listener.eventHappen(event);
			}
		}
	}
	
	private long printTime = 0;
    private long overFlowNotifyTime=0;
	/**
	 * 添加一个任务到线程池的任务列表中,如果线程池是单队列的,任务直接添加到唯一队列中.
	 * 如果任务是多队列的,依赖于task中的flag当前已注册的队列,如果没注册,则循环取下一个队列,首次取第一个队列.
	 * 如果flag值为null,则循环放入到下一个队列中.
	 * @param task
	 */
    public void addTask(ITask task){
    	this.addTaskWithReturn(task,true);
    }
	public boolean addTaskWithReturn(ITask task,boolean removeOld) {
		if(!this.isStart){
			if(System.currentTimeMillis()-printTime >= 60000){
				this.printTime = System.currentTimeMillis();
				System.out.println("ThreadPool Stop!");
			}
			return false;
		}
		if(task == null){
			return true;
		}
		List taskList = this.getTaskList(task);
		if(taskList == null){
			this.stopWork(true);
			return false;
		}
		
		boolean isOverflow = false;
		synchronized(taskList){
			this.totalTaskNum ++;
			if(this.totalTaskNum == Long.MAX_VALUE){
				this.totalTaskNum = 0;
			}
			isOverflow = taskList.size() >= this.taskCapacity;
			
			if(isOverflow){
				if(!removeOld){
					return false;
				}
				taskList.remove(0);
			}
		}
		if(isOverflow){
			boolean isSendMail = false;
			//只是要一把锁，随便找个对象，减少对象的产生。
			synchronized(taskListArr){
				if(System.currentTimeMillis()-this.overFlowNotifyTime>sendNotifyCycleTimeMinute){
					isSendMail = true;
	                this.overFlowNotifyTime=System.currentTimeMillis();
	            }
			}
			if(isSendMail){
				INotifyServer.getSingleInstance().notifyInfo("线程池队列溢出.", "线程池名称:["+this.getPoolName()+"],队列最大数:"+this.taskCapacity,INotifyServer.WARN_LEVEL);
			}
			EventInfo event = new EventInfo(EventInfo.QUEUE_TASK_OVERFLOW_EVENT,this,new Integer(this.taskCapacity));
			this.triggerEventListener(event);
		}
		
		synchronized(taskList){
			taskList.add(task);
			taskList.notify();
		}
		return true;
	}
	
	public void removeAllTask(){
		List[] listArr = this.taskListArr;
		if(listArr == null){
			return ;
		}
		
		List tempList;
		for(int i=0;i<listArr.length;i++){
			tempList = listArr[i];
			synchronized(tempList){
				tempList.clear();
			}
		}
	}
	
	public boolean isStop(){
	    return !this.isStart;
	}
	
	
	private Object assignLock = new Object();
	private void assignTask2OtherThread(IWorker worker){
		if(worker == null){
			return ;
		}
		if(!this.isStart){
			return ;
		}
		List taskList = worker.getTaskList();
		if(taskList == null){
			return ;
		}
		ITask task = worker.getCurTask();
		if(task != null){
			System.out.println("Dead Because:"+task.getClass().getName()+"["+task.getDesc()+"] RemainTaskNum:"+taskList.size()+" CurrentThreadNum:"+this.workerNum);
		}
		
		if(!this.isMultipleList){
			return ;
		}
		
		int index = 0;
		IWorker[] tempWorkerArr = null;
		List[] tempListArr = null;

		
		synchronized(this.assignLock){
			synchronized(this){
				tempWorkerArr = new IWorker[this.workerNum-1];
				tempListArr = new List[this.workerNum-1];
				for(int i=0;i<this.workerNum;i++){
					if(this.workers[i] != worker){
						tempWorkerArr[index] = this.workers[i];
						tempListArr[index] = this.taskListArr[i];
						index ++;
					}
				}
				this.workerNum --;
				this.workers = tempWorkerArr;
				this.taskListArr = tempListArr;
			}
	
			synchronized(this.taskListMapping){
				Object value;
				for(Iterator itr = this.taskListMapping.values().iterator();itr.hasNext();){
					value = itr.next();
					if(value == taskList){
						itr.remove();
					}
				}
			}
	
			synchronized(taskList){
				for(Iterator itr = taskList.iterator();itr.hasNext();){
					this.addTask((ITask)itr.next());
				}
			}
		}
	}
	
	private synchronized List nextList(){
		if(this.workerNum <= 0){
			return null;
		}
		if(this.registerLoopCount >= this.workerNum){
			this.registerLoopCount = 0;
		}
		return this.taskListArr[this.registerLoopCount++];
	}
	
	/**
	 * 得到任务所注册的队列，依赖池是单队列还是多队列的
	 * 简单算法，循环注册法
	 * @param task 待执行的任务
	 * @return 应该注册队列
	 */
	private final List getTaskList(ITask task){
		List taskList = null;
		
		if(this.isMultipleList){
			Object flag = task.getFlag();
			synchronized(this.taskListMapping){
				taskList = (List)this.taskListMapping.get(flag);
				if(taskList == null){
					taskList = this.nextList();
					if(taskList != null){
						this.taskListMapping.put(flag, taskList);
					}
				}
			}
		}else{
			taskList = this.taskListArr[0];
		}
		return taskList;
	}


	
	/**
	 * 得到任务所注册的队列，依赖池是单队列还是多队列的
	 * 简单算法，循环注册法
	 * @param task 待执行的任务
	 * @return 应该注册队列
	 */
	public final String getTaskNumInfo(){
		try
		{
		List taskList = null;
		
		if(this.isMultipleList){
			StringBuffer taskContainer=new StringBuffer("");
			int taskNum=0;
			synchronized(this.taskListMapping){
				Iterator it=this.taskListMapping.keySet().iterator();
				while(it.hasNext())
				{
					Object flag=(Object)it.next();
					taskList = (List)this.taskListMapping.get(flag);
//					taskContainer.append("flag:"+flag+" taskNum:"+taskList.size()+" ");
					taskNum+=taskList.size();
				}
			}

			taskContainer.append("total: taskNum:"+taskNum+" ");
			return taskContainer.toString();
		}else{
			taskList = this.taskListArr[0];
			return "taskNum:"+taskList.size();
		}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return "get taskNum: Exception";
		}
	}
	

	int finishTaskNum=0;
	/**
	 */
	public synchronized int getAndClearFinishTaskNum(){
		int x=finishTaskNum;
		finishTaskNum=0;
		return x;
	}
	public synchronized void addFinishTaskNum(){
		 finishTaskNum++;
	}
	
	public void stopWork() {
		this.stopWork(true);
	}
	
	public synchronized void stopWork(boolean isImmediately){
		this.isStart = false;
		for (int i = 0; i < this.workerNum; i++) {
			if(workers[i]==null){
				continue;
			}
			workers[i].stopWork(isImmediately);
			workers[i] = null;
		}
		this.workerNum = 0;
		this.workers = null;
		
		if(this.taskListArr != null){
			List taskList = null;
			for(int i=0;i<this.taskListArr.length;i++){
				taskList = this.taskListArr[i];
				synchronized(taskList){
					if(isImmediately){
						taskList.clear();
					}
					taskList.notifyAll();
				}
			}
			this.taskListArr = null;
		}
	}
	
	public synchronized void getThreadStackInfo(StringBuffer buff){
		if(!this.isStart || buff == null){
			return ;
		}

		buff.append("PoolInfo:ThreadNum="+this.getThreadNum());
		buff.append(";TaskCapacity="+this.taskCapacity);
		buff.append(";TaskNum=");
		if(this.getPoolType() == ThreadPool.MULTIPLE_TASK_LIST_POOL){
			buff.append("[");
			for(int i=0;i<this.workingThreadNum;i++){
				if(i > 0){
					buff.append(",");
				}
				buff.append(this.getTaskNum(i));
			}
			buff.append("]");
		}else{
			buff.append(this.getTaskNum(0));
		}
		buff.append("\n");
		for(int i=0;i<workers.length;i++){
			FleetyThread.getRunStackTrace((i+1)+":", this.workers[i], buff);
			buff.append("\n");
		}
	}
	
	public String toString(){
		String str = super.toString();
		if(this.poolName != null){
			str = str+"["+this.poolName+"]";
		}
		return str;
	}
}
