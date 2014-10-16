package com.fleety.base;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Iterator;
import java.util.Vector;

import server.notify.INotifyServer;

public class FleetyThread {
	private static Vector threadList = new Vector(128);
	
	private String createStackInfo = null;
	private Thread thread = null;
	
	private Runnable runnable = null; 

	public FleetyThread(){
		this(null,null,null);
	}
	public FleetyThread(String name){
		this(null,null,name);
	}
	public FleetyThread(Runnable runnable){
		this(null,runnable,null);
	}
	public FleetyThread(Runnable runnable, String name){
		this(null,runnable,name);
	}
	public FleetyThread(ThreadGroup group, Runnable runnable, String name){
		this.runnable = runnable;

		this.thread = new Thread(group,(Runnable)null){
			public void run(){
				FleetyThread.this._run();
			}
		};
		if(name != null){
			this.thread.setName(name);
		}
		
		this.createStackInfo = "fleety thread create: "+FleetyThread.getStrByException(new Exception());
	}
	public void setName(String name){
		if(this.thread != null){
			this.thread.setName(name);
		}
	}
	
	public static String getStrByException(Throwable e){
		ByteArrayOutputStream arrOut;
		PrintStream print = new PrintStream(arrOut = new ByteArrayOutputStream(5120));
		try{
			e.printStackTrace(print);
			print.close();
		}catch(Exception ee){}
		return arrOut.toString().trim();
	}
	
	public String getCreateStackInfo(){
		return this.createStackInfo;
	}

	public void _run(){
    	FleetyThread.threadList.add(this);
    	
		try{
			if(this.runnable != null){
				this.runnable.run();
			}else{
				this.run();
			}
		}catch(Throwable e){
			this.printThrowable(e);
			
			String content = "Error:\n"+FleetyThread.getStrByException(e)+"\n\n"+"Thread Create:"+this.createStackInfo;
			INotifyServer.getSingleInstance().notifyInfo("Module Monitor Thread Crash.",content,INotifyServer.ERROR_LEVEL);
		}finally{
			FleetyThread.threadList.remove(this);
    	}
	}
	
	private void printThrowable(Throwable e){
		System.err.println(this.createStackInfo);
		e.printStackTrace();
	}
	
	public static final String MODULE_MONITOR_MAIL_NAME = "module monitor mail server";
	public static void sendMonitorMail(String head,String content){
		INotifyServer.getSingleInstance().notifyInfo(head, content, INotifyServer.ERROR_LEVEL);
	}

    public void run(){
    	
    }

    public void start(){
    	this.thread.start();
    }
    
    public void interrupt(){
    	this.thread.interrupt();
    }
    
    public boolean isAlive(){
    	return this.thread.isAlive();
    }
    
    public void setDaemon(boolean on){
    	this.thread.setDaemon(on);
    }
    public void setPriority(int newPriority){
    	this.thread.setPriority(newPriority);
    }
    public void join() throws InterruptedException{
    	this.thread.join();
    }
    public StackTraceElement[] getStackTrace(){
    	return this.thread.getStackTrace();
    }
    public String getName(){
    	return this.thread.getName();
    }

    public static boolean interrupted(){
    	return Thread.interrupted();
    }
    public static void sleep(long millis) throws InterruptedException{
    	Thread.sleep(millis);
    }
    public static void sleep(long millis,int nanos) throws InterruptedException{
    	Thread.sleep(millis, nanos);
    }
    public static int getThreadNum(){
    	return FleetyThread.threadList.size();
    }
    /**
     * 打印当前正运行的线程
     * @param print 可传递null，代表需要打印到控制台。
     */
    public static void printThreadInfo(PrintStream print){
    	StringBuffer buff = new StringBuffer(10240);
    	synchronized(FleetyThread.threadList){
    		Iterator itr = FleetyThread.threadList.iterator();
    		FleetyThread thread;
    		int count = 0;
    		while(itr.hasNext()){
    			thread = (FleetyThread)itr.next();
    			
    			count ++;
    			
    			FleetyThread.getRunStackTrace(count+":", thread, buff);
    		}
    	}
    	if(print == null){
    		System.out.println(buff);
    	}else{
    		print.println(buff);
    	}
    }
    
    public static void getRunStackTrace(String flag,FleetyThread thread,StringBuffer buff){
    	if(thread == null || buff == null){
    		return ;
    	}
    	if(flag != null){
    		buff.append(flag);
    	}
    	buff.append("name=");
    	buff.append(thread.getName());
    	buff.append("\nCreateStack:");
    	buff.append(thread.getCreateStackInfo());
    	buff.append("\nRunStatus:");
    	StackTraceElement[] trace = thread.getStackTrace();
    	for (int i=0; i < trace.length; i++){
    		buff.append("\tat " + trace[i]+"\n");
        }
    }
}
