package com.fleety.base;

import java.util.HashMap;
import java.util.Iterator;

import server.threadgroup.ThreadPoolGroupServer;

import com.fleety.server.BasicServer;
import com.fleety.util.pool.timer.FleetyTimerTask;
import com.fleety.util.pool.timer.TimerPool;

public class CostStatLog extends BasicServer{
	private HashMap costStatMap = new HashMap();
	static long period = 5 * 60 * 1000L;

	private static CostStatLog instance = null;
	private TimerPool timer = null;
	private CostTimePrintTask task = null;

	private CostStatLog(){
	}

	public static CostStatLog getSingleInstance(){
		if(instance == null){
			instance = new CostStatLog();
		}
		return instance;
	}

	public boolean startServer(){
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

		this.initTask();
		
		return true;

	}

	public void stopServer(){
		if(task != null){
			task.cancel();
		}
		if(timer != null){
			timer.cancel();
		}
	}

	public void finalize() throws Throwable{
		this.stopServer();
		super.finalize();
	}

	public synchronized void initTask(){
		if(this.timer != null){
			return ;
		}

		this.timer = ThreadPoolGroupServer.getSingleInstance().createTimerPool("cost_stat");
		this.task = new CostTimePrintTask();
		this.timer.schedule(task, period, period);
	}

	class CostTimePrintTask extends FleetyTimerTask{
		public void run(){
			try{
				HashMap tempHm;
				synchronized(costStatMap){
					tempHm = (HashMap) costStatMap.clone();
					costStatMap = new HashMap();
				}
				Iterator it = tempHm.keySet().iterator();
				StringBuffer buf = new StringBuffer("");
				while(it.hasNext()){
					Object key = it.next();
					HashMap tempHashMap = (HashMap) tempHm.get(key);
					Iterator itTemp = tempHashMap.keySet().iterator();
					while(itTemp.hasNext()){
						Object keyTemp = itTemp.next();

						costStatObj statObj = (costStatObj) tempHashMap.get(keyTemp);
						statObj.avgCostTime = statObj.totalCostTime
								/ statObj.times;
						buf.append("\t CostTimeStat:" + statObj.infoKey
								+ "::avgCost:"
								+ statObj.avgCostTime
								+ ";times:" + statObj.times
								+ ";totalCost:"
								+ statObj.totalCostTime
								+ ";maxCost:"
								+ statObj.maxCostTime
								+ ";minCost:"
								+ statObj.minCostTime + "\n");
					}
				}
				System.out.println(buf.toString());
			} catch(Exception e){
				e.printStackTrace();
			}
		}
	}
	public void addCostInfo(Object source, String key, long costTime){
		addCostInfo(source, key, costTime,1);
	}

	public void addCostInfo(Object source, String key, long costTime,int taskSize){
		initTask();

		if(costTime < 0)
			return;
		key = source.toString() + "--" + key;
		costStatObj obj;
		synchronized(costStatMap){

			HashMap tempHashMap = (HashMap) costStatMap.get(source);
			if(tempHashMap == null){
				tempHashMap = new HashMap();
				costStatMap.put(source, tempHashMap);
			}
			obj = (costStatObj) tempHashMap.get(key);
			if(obj == null){
				obj = new costStatObj(key);
				tempHashMap.put(key, obj);
			}
		}
		obj.addInfo(costTime);
	}

	class costStatObj{
		final String infoKey;
		long times = 0;//次数
		long totalCostTime = 0;//总花费时间
		long avgCostTime = 0;//平均花费时间
		long maxCostTime = 0;//最大花费时间
		long minCostTime = -1;//最小花费时间

		private costStatObj(String infoKey){
			this.infoKey = infoKey;
		}

		synchronized void addInfo(long costTime){
			addInfo(costTime,1);
		}
		synchronized void addInfo(long costTime,int taskNum){
			times+=taskNum;
			totalCostTime += costTime;
			if(maxCostTime < costTime){
				maxCostTime = costTime;
			}
			if(minCostTime == -1 || minCostTime > costTime){
				minCostTime = costTime;
			}
		}
	}
}
