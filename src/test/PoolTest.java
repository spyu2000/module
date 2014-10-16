package test;

import com.fleety.base.CostStatLog;
import com.fleety.util.pool.timer.FleetyTimerTask;
import com.fleety.util.pool.timer.TimerPool;

import server.threadgroup.ThreadPoolGroupServer;

public class PoolTest {
	private static int count = 1;
	
	public static void main(String[] args) throws Exception{
		CostStatLog.getSingleInstance().addPara("period", "1");
		CostStatLog.getSingleInstance().startServer();
		
		TimerPool pool = ThreadPoolGroupServer.getSingleInstance().createTimerPool("xjs", 10);
		
		for(int i=0;i<10;i++){
			pool.schedule(new FleetyTimerTask(){
				private int seq = count++;
				public void run(){
					for(int i=0;i<10;i++){
						System.out.println(i+" "+seq+" "+(i/seq));
						try{
							Thread.sleep(100);
						}catch(Exception e){}
					}
				}
			}, 1000);
		}

		TimerPool pool2 = ThreadPoolGroupServer.getSingleInstance().createTimerPool("xjs11", 10);
		
		for(int i=0;i<30;i++){
			pool2.schedule(new FleetyTimerTask(){
				private int seq = count++;
				public void run(){
					for(int i=0;i<10;i++){
						System.out.println(i+" "+seq+" "+(i/seq));
						try{
							Thread.sleep(100);
						}catch(Exception e){}
					}
				}
			}, 1000);
		}
		
		Thread.sleep(100000);
	}

}
