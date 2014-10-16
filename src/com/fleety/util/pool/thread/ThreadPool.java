/**
 * Created on 2006-9-26
 * @author: Jami
 * @copyright: fleety.com
 * @notice:����
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
 * @description ���̳߳ؽ��������ݷ��͸���ͼ
 */
public class ThreadPool implements IPool{
	/**
	 * ����̹߳���һ�����У��Ƚ��ȳ���
	 */
	public static final Object SINGLE_TASK_LIST_POOL = new Object();
	/**
	 * ÿ���̶߳�Ӧһ�����У��Ƚ��ȳ�����Ҫ��;��ĳЩ��ͬ��������Ҫ����˳���Ⱥ�ִ�С�
	 */
	public static final Object MULTIPLE_TASK_LIST_POOL = new Object();
	
	//��ӽ�������������
	private long totalTaskNum = 0;
	private int workerNum = 0;
	private Object workingCountLock = new Object();
	private int workingThreadNum = 0;
	private IWorker[] workers;
	private Object poolType = null;
	private int taskCapacity = 0;
	private boolean isDaemoThread = false;

	//ע�ᵽ�ĸ�����list��ѭ����������
	private int registerLoopCount = 0;
	//�̳߳��Ƿ�������
	private boolean isStart = false;
	//�Ƿ������list�̳߳�
	private boolean isMultipleList = false;
	//�����б����顣����ǵ�����list��������鳤��Ϊ1�����Ϊ������list�������鳤�ȵ����߳�����
	private List[] taskListArr = null;
	//���������list��ӳ��ṹ���������flagΪkey.
	private HashMap taskListMapping = null;
	
	private String poolName = null;
    
    //���Ͷ��������ѭ������
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
			throw new Exception("������̳߳�����!");
		}
		if(taskCapacity <= 0 || workersNumber <= 0){
			throw new Exception("�������!�����������="+taskCapacity+";�߳���="+workersNumber);
		}
		
		this.isMultipleList = (this.poolType == MULTIPLE_TASK_LIST_POOL);
		//����һ�������Ķ���
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
			
			if(this.isMultipleList){ //����Ƕ����ģʽ����ÿ�����ж�Ӧһ���̡߳�
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
	 * ���һ�������̳߳ص������б���,����̳߳��ǵ����е�,����ֱ����ӵ�Ψһ������.
	 * ��������Ƕ���е�,������task�е�flag��ǰ��ע��Ķ���,���ûע��,��ѭ��ȡ��һ������,�״�ȡ��һ������.
	 * ���flagֵΪnull,��ѭ�����뵽��һ��������.
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
			//ֻ��Ҫһ����������Ҹ����󣬼��ٶ���Ĳ�����
			synchronized(taskListArr){
				if(System.currentTimeMillis()-this.overFlowNotifyTime>sendNotifyCycleTimeMinute){
					isSendMail = true;
	                this.overFlowNotifyTime=System.currentTimeMillis();
	            }
			}
			if(isSendMail){
				INotifyServer.getSingleInstance().notifyInfo("�̳߳ض������.", "�̳߳�����:["+this.getPoolName()+"],���������:"+this.taskCapacity,INotifyServer.WARN_LEVEL);
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
	 * �õ�������ע��Ķ��У��������ǵ����л��Ƕ���е�
	 * ���㷨��ѭ��ע�ᷨ
	 * @param task ��ִ�е�����
	 * @return Ӧ��ע�����
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
	 * �õ�������ע��Ķ��У��������ǵ����л��Ƕ���е�
	 * ���㷨��ѭ��ע�ᷨ
	 * @param task ��ִ�е�����
	 * @return Ӧ��ע�����
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
