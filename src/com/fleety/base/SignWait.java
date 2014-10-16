/**
 * ���� Created on 2008-6-19 by edmund
 */
package com.fleety.base;

import java.util.*;

/**
 * @title		�Գ�ʱ��ʽ�ȴ��ź���Ӧ��ģ�顣���Է�����Ӧʱ��һ����Ϣ����
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
	 * �ȴ�ĳ���źŵķ��������ҷ����źŷ���ʱ�趨����Ϣ��
	 * @param signFlag   �źű�ʶ
	 * @param timeout    ��ȴ�ʱ��,��λ����
	 * @return		     �����źŷ���ʱ�趨����Ϣ
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
		
		//��ӵȵ������źŵ�ע���б���
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
			//�Ƴ��źŵȴ��ߣ������Ƿ��ȡ���źŵ���Ӧ��
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
		//�õ����ź��ϵ����еȴ���
		synchronized(mapping){
			List waiterList = (List)mapping.get(signFlag);
			if(waiterList != null && waiterList.size() > 0){
				return ((InfoCarriage)waiterList.get(0)).getPara();
			}
		}
		return null;
	}
	
	/**
	 * �����ź�
	 * @param signFlag	�������źŶ���
	 * @param info      �źŷ���ʱ��Ҫ�趨����Ϣ
	 */
	public void releaseSign(Object signFlag,Object info){
		if(signFlag == null){
			return ;
		}
		
		boolean hasWaiter = false;
		//�õ����ź��ϵ����еȴ���
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
						SignWait.getSingleInstance().releaseSign("1-10001", "����");
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
