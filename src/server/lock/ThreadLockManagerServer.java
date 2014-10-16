/**
 * ���� Created on 2008-5-5 by edmund
 */
package server.lock;

import java.util.*;

import com.fleety.base.FleetyThread;
import com.fleety.base.GeneralConst;
import com.fleety.server.BasicServer;

public class ThreadLockManagerServer extends BasicServer{
	private static ThreadLockManagerServer singleInstance = null;
	public static ThreadLockManagerServer getSingleInstance(){
		if(singleInstance == null){
			synchronized(ThreadLockManagerServer.class){
				if(singleInstance == null){
					singleInstance = new ThreadLockManagerServer();
				}
			}
		}
		return singleInstance;
	}

	private static final int WAIT_LOCK_TYPE = 1;
	private static final int HOLD_LOCK_TYPE = 2;
	
	//��ǰ���ڵ������߳�ӳ��
	//�߳�ӳ���� one:one   һ���߳�ͬһʱ��ֻ�������ڵȴ�һ����
	private HashMap threadWaitLockMapping = null;
	//��ӳ��ȴ����߳� one:many һ����ͬһʱ�̿��ܶ���̶߳��ڵȴ�
	private HashMap waitLockThreadMapping = null;
	
	//��ǰ��ӵ�������߳�ӳ��
	//�߳���ӳ�� one:many  һ���߳�ͬһʱ�̿���ӵ�ж����
	private HashMap threadLockMapping = null;
	//��ӳ��ӵ�е��߳� one:one  һ����ͬһʱ��ֻ�ܱ�һ���߳�ӵ��
	private HashMap lockThreadMapping = null;
	
	private boolean isStop = false;
	
	private ThreadLockManagerServer(){
		this.threadWaitLockMapping = new HashMap();
		this.waitLockThreadMapping = new HashMap();
		this.threadLockMapping = new HashMap();
		this.lockThreadMapping = new HashMap();
	}
	

	private long detectCycle = 9000l;
	private long timeoutCycle = 6000l;
	public boolean startServer(){
		this.isStop = false;
		try{
			this.detectCycle = Long.parseLong(this.getStringPara("detect_cycle"))*1000l;
		}catch(Exception e){}
		try{
			this.timeoutCycle = Long.parseLong(this.getStringPara("timeout_cycle"))*1000l;
		}catch(Exception e){}
		
		new FleetyThread(){
			public void run(){
				while(!isStop){
					try{
						FleetyThread.sleep(detectCycle);
					}catch(Exception e){}
					
					ThreadLockManagerServer.this.detectTimeout();
					ThreadLockManagerServer.this.detectDeadLock();
				}
			}
		}.start();

		this.isRunning = true;
		return true;
	}
	
	private synchronized void detectTimeout(){
		FleetyThread thread;
		LockInfo lockInfo;
		List lockList;
		
		Iterator iterator = threadLockMapping.keySet().iterator();
		while(iterator.hasNext()){
			thread = (FleetyThread)iterator.next();
			lockList = (List)threadLockMapping.get(thread);
			if(lockList != null){
				Iterator lockIterator = lockList.iterator();
				while(lockIterator.hasNext()){
					lockInfo = (LockInfo)lockIterator.next();
					if(System.currentTimeMillis()-lockInfo.startTime > timeoutCycle){
						ThreadLockManagerServer.this.printInfo();
						return ;
					}
				}
			}
		}
	}
	
	private synchronized void detectDeadLock(){
		Iterator waitThreadIterator = this.threadWaitLockMapping.keySet().iterator();
		
		Thread waitThread1,waitThread2;
		LockInfo waitLock1,waitLock2,holdLock;
		List lockList1,lockList2,threadList;
		Iterator holdLockIterator1,holdLockIterator2,anotherWaitThreadIterator;
		
		System.out.println("------������⿪ʼ---------------------------");
		while(waitThreadIterator.hasNext()){
			waitThread1 = (Thread)waitThreadIterator.next();
			
			lockList1 = (List)this.threadLockMapping.get(waitThread1);
			if(lockList1 != null && lockList1.size() > 0){
				waitLock1 = (LockInfo)this.threadWaitLockMapping.get(waitThread1);
				holdLockIterator1 = lockList1.iterator();
				while(holdLockIterator1.hasNext()){
					holdLock = (LockInfo)holdLockIterator1.next();
					threadList = (List)this.waitLockThreadMapping.get(holdLock.getLock());
					if(threadList != null && threadList.size() > 0){
						anotherWaitThreadIterator = threadList.iterator();
						while(anotherWaitThreadIterator.hasNext()){
							waitThread2 = (Thread)anotherWaitThreadIterator.next();
							lockList2 = (List)this.threadLockMapping.get(waitThread2);
							if(lockList2 != null && lockList2.size() > 0){
								holdLockIterator2 = lockList2.iterator();
								while(holdLockIterator2.hasNext()){
									waitLock2 = (LockInfo)holdLockIterator2.next();
									if(waitLock2.equals(waitLock1.getLock())){
										System.out.println("�߳�["+waitThread1.getName()+"]���߳�["+waitThread2.getName()+"]����!����["+holdLock.getLock()+"]����["+waitLock2.getLock()+"]֮��!");
									}
								}
							}
						}
					}
				}
			}
		}
		System.out.println("------����������---------------------------");
	}
	
	public void stopServer(){
		synchronized(this){
			this.isStop = true;
		}
		
		this.threadWaitLockMapping.clear();
		this.waitLockThreadMapping.clear();
		this.threadLockMapping.clear();
		this.lockThreadMapping.clear();
		
		this.isRunning = false;
	}
	
	public synchronized void waitLock(Object lock){
		if(this.isStop){
			return ;
		}
		
		Thread thread = Thread.currentThread();
		this.threadWaitLockMapping.put(thread, new LockInfo(System.currentTimeMillis(), lock, this.getDesc(lock,WAIT_LOCK_TYPE)));
		
		List threadList = (List)this.waitLockThreadMapping.get(lock);
		if(threadList == null){
			threadList = new LinkedList();
			this.waitLockThreadMapping.put(lock, threadList);
		}
		if(!threadList.contains(thread)){
			threadList.add(thread);
		}
	}
	
	public synchronized void holdLock(Object lock){
		if(this.isStop){
			return ;
		}
		
		this.releaseWaitLockInfo(lock);
		
		Thread thread = Thread.currentThread();
		this.lockThreadMapping.put(lock, thread);
		
		List lockList = (List)this.threadLockMapping.get(thread);
		if(lockList == null){
			lockList = new LinkedList();
			this.threadLockMapping.put(thread, lockList);
		}
		if(!lockList.contains(lock)){
			lockList.add(new LockInfo(System.currentTimeMillis(), lock, this.getDesc(lock, HOLD_LOCK_TYPE)));
		}
	}
	
	public synchronized void releaseLock(Object lock){
		if(this.isStop){
			return ;
		}
		
		this.releaseWaitLockInfo(lock);
		this.releaseHoldLockInfo(lock);
	}
	
	private void releaseWaitLockInfo(Object lock){
		Thread thread = Thread.currentThread();
		
		this.threadWaitLockMapping.remove(thread);
		
		List threadList = (List)this.waitLockThreadMapping.get(lock);
		if(threadList != null){
			threadList.remove(thread);
		}
	}
	
	private void releaseHoldLockInfo(Object lock){
		Thread thread = Thread.currentThread();
		
		this.lockThreadMapping.remove(lock);
		
		List lockList = (List)this.threadLockMapping.get(thread);
		if(lockList != null){
			Iterator iterator = lockList.iterator();
			while(iterator.hasNext()){
				if(iterator.next().equals(lock)){
					iterator.remove();
					break;
				}
			}
		}
	}
	
	private String getDesc(Object lock,int type){
		StringBuffer buff = new StringBuffer(1024);
		
		Thread thread = Thread.currentThread();
		buff.append("�߳�["+thread.getName()+"]");
		if(type == WAIT_LOCK_TYPE){
			buff.append("����");
		}else if(type == HOLD_LOCK_TYPE){
			buff.append("ӵ����");
		}
		buff.append("["+lock+"]");
		
		StackTraceElement[] eles = new Exception().getStackTrace();
		StackTraceElement ele;
		int num = eles.length;
		for(int i = 4;i < num;i++){
			ele = eles[i];
			
			buff.append("\n\t\t");
			buff.append(ele.getClassName());
			buff.append("[");
			buff.append(ele.getMethodName());
			buff.append("](");
			buff.append(ele.getLineNumber());
			buff.append(")");
		}
		buff.append("\n");
		
		return buff.toString();
	}
	
	public synchronized void printInfo(){
		System.out.println("------��ʼ��ӡ�߳�����Ϣ["+GeneralConst.YYYY_MM_DD_HH_MM_SS.format(new Date())+"]---------");
		LockInfo lockInfo;
		Iterator lockIterator = this.threadWaitLockMapping.values().iterator();
		while(lockIterator.hasNext()){
			lockInfo = (LockInfo)lockIterator.next();
			System.out.println("��ʼʱ��["+GeneralConst.YYYY_MM_DD_HH_MM_SS.format(new Date(lockInfo.getStartTime()))+"]\t"+lockInfo.getDesc());
		}
		
		List lockList;
		Iterator listIterator = this.threadLockMapping.values().iterator();
		while(listIterator.hasNext()){
			lockList = (List)listIterator.next();
			if(lockList != null){
				lockIterator = lockList.iterator();
				while(lockIterator.hasNext()){
					lockInfo = (LockInfo)lockIterator.next();
					System.out.println("��ʼʱ��["+GeneralConst.YYYY_MM_DD_HH_MM_SS.format(new Date(lockInfo.getStartTime()))+"]\t"+lockInfo.getDesc());
				}
			}
		}
		System.out.println("------������ӡ�߳�����Ϣ------------------------------\n");
	}
	
	
	public class LockInfo{
		private long startTime = 0;
		private Object lock = null;
		private String desc = null;
		
		public LockInfo(long startTime,Object lock,String desc){
			this.startTime = startTime;
			this.lock = lock;
			this.desc = desc;
		}
		
		public long getStartTime(){
			return this.startTime;
		}
		
		public Object getLock(){
			return this.lock;
		}
		
		public String getDesc(){
			return this.desc;
		}
		
		public boolean equals(Object o){
			if(o instanceof LockInfo){
				return this.lock == ((LockInfo)o).getLock();
			}
			return this.lock == o;
		}
	}
}
