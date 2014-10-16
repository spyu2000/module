/**
 * 绝密 Created on 2008-6-19 by edmund
 */
package com.fleety.base;

import java.util.*;

/**
 * @title		以超时方式等待信号响应的模块。可以返回响应时的一个信息对象。
 * @description
 * @version      1.0
 * @author       edmund
 *
 */
public class SignWait{
	public static SignWait singleInstance = null;
	public static SignWait getSingleInstance(){
		if(singleInstance == null){
			synchronized(SignWait.class){
				if(singleInstance == null){
					singleInstance = new SignWait();
				}
			}
		}
		return singleInstance;
	}
	
	private HashMap mapping = new HashMap(16);
	private Hashtable existSignMapping = new Hashtable(16);
	
	private boolean isCacheSignal = true;
	public void setSinalCache(boolean isCache){
		this.isCacheSignal = isCache;
	}
	public boolean isCacheSignal(){
		return this.isCacheSignal;
	}
	
	/**
	 * 等待某个信号的发生，并且返回信号发生时设定的信息。
	 * @param signFlag   信号标识
	 * @param timeout    最长等待时间,单位毫秒
	 * @return		     返回信号发生时设定的信息
	 * @throws Exception
	 */
	public Object waitInfo(Object signFlag,long timeout) throws Exception{		
		return this.waitInfo(signFlag, timeout, null);
	}
	
	public Object waitInfo(Object signFlag,long timeout,Object para) throws Exception{
		if(signFlag == null){
			return null;
		}
		
		if(timeout <= 0){
			timeout = 5000;
		}
		InfoCarriage temp = new InfoCarriage();
		temp.setPara(para);
		
		//添加等到对象到信号的注册列表中
		synchronized(mapping){
			List waiterList = (List) mapping.get(signFlag);
			if(waiterList == null){
				waiterList = new LinkedList();
				mapping.put(signFlag, waiterList);
			}
			waiterList.add(temp);
		}

		try{
			Object info = this.existSignMapping.remove(signFlag);
			if(info == null){
				synchronized(temp){
					temp.wait(timeout);
				}
			} else{
				temp.setInfo(info);
			}
		} catch(Exception e){
			throw e;
		} finally{
			//移除信号等待者，不管是否获取到信号的响应。
			synchronized(mapping){
				List waiterList = (List) mapping.get(signFlag);
				if(waiterList != null){
					waiterList.remove(temp);
					if(waiterList.size() == 0){
						mapping.remove(signFlag);
					}
				}
			}
		}
		
		return temp.getInfo();
	}
	
	public Object getWaitPara(Object signFlag){		
		//得到该信号上的所有等待者
		synchronized(mapping){
			List waiterList = (List)mapping.get(signFlag);
			if(waiterList != null && waiterList.size() > 0){
				return ((InfoCarriage)waiterList.get(0)).getPara();
			}
		}
		return null;
	}
	
	/**
	 * 发布信号
	 * @param signFlag	发布的信号对象
	 * @param info      信号发布时需要设定的信息
	 */
	public void releaseSign(Object signFlag,Object info){
		if(signFlag == null){
			return ;
		}
		
		boolean hasWaiter = false;
		//得到该信号上的所有等待者
		synchronized(mapping){
			InfoCarriage carriage;
			List waiterList = (List) mapping.get(signFlag);
			if(waiterList != null && waiterList.size() > 0){
				hasWaiter = true;
				for(Iterator itr = waiterList.iterator(); itr.hasNext();){
					carriage = (InfoCarriage) itr.next();
					carriage.setInfo(info);
					synchronized(carriage){
						carriage.notify();
					}
				}
			}
		}

		if(!hasWaiter){
			if(info != null && this.isCacheSignal){
				existSignMapping.put(signFlag, info);
			}
		}
	}
	
	public void clearSign(Object signFlag){
		if(signFlag == null){
			return ;
		}
		
		synchronized(signFlag){
			this.existSignMapping.remove(signFlag);
		}
	}
	
	private class InfoCarriage{
		private Object info = null;
		private Object paraInfo = null;
		
		public void setPara(Object paraInfo){
			this.paraInfo = paraInfo;
		}
		
		public Object getPara(){
			return this.paraInfo;
		}
		
		public Object getInfo(){
			return this.info;
		}
		
		public void setInfo(Object info){
			this.info = info;
		}
		
	}
	
	
	public static void main(String[] argv){		
		new FleetyThread(){
			public void run(){
				try{
					for(int i=0;i<4;i++){
						FleetyThread.sleep(3000);
						SignWait.getSingleInstance().releaseSign("1-10001", "徐新");
					}
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		}.start();
		
		for(int i=0;i<10;i++){
			try{
				FleetyThread.sleep(1000);
			}catch(Exception e){
				
			}
			new FleetyThread(){
				public void run(){
					try{
						System.out.println(SignWait.getSingleInstance().waitInfo("1-10001", 10000));
					}catch(Exception e){
						e.printStackTrace();
					}
				}
			}.start();
		}
	}
}
