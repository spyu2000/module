package server.socket.socket;

import server.threadgroup.PoolInfo;
import server.threadgroup.ThreadPoolGroupServer;

import com.fleety.server.BasicServer;
import com.fleety.util.pool.thread.ThreadPool;

public class PoolSelectorCreator extends BasicServer implements ISelectorCreator {
	private String poolName = null;
	private int selectorNum = 0;
	private ReadSelectorThread[] selectorArr = null;
	public boolean startServer(){
		try{
			selectorNum = this.getIntegerPara("selector_num").intValue();
			this.poolName = this.getServerName()+"["+this.hashCode()+"] PoolSelectorCreator";
			PoolInfo pInfo = new PoolInfo(ThreadPool.SINGLE_TASK_LIST_POOL,selectorNum,selectorNum,false);
			ThreadPool pool = ThreadPoolGroupServer.getSingleInstance().createThreadPool(this.poolName, pInfo);
			
			this.selectorArr = new ReadSelectorThread[this.selectorNum];
			for(int i=0;i<this.selectorNum;i++){
				this.selectorArr[i] = new ReadSelectorThread();
				pool.addTask(this.selectorArr[i]);
			}
		}catch(Exception e){
			e.printStackTrace();
			System.out.println("服务运作线程实例化失败!");
			this.stopServer();
			return false;
		}
		this.isRunning = true;
		return this.isRunning();
	}
	
	public void stopServer(){
		if(this.selectorArr != null){
			for(int i=0;i<this.selectorArr.length;i++){
				if(this.selectorArr[i] != null){
					this.selectorArr[i].stopWork();
				}
			}
		}
		ThreadPoolGroupServer.getSingleInstance().removeThreadPool(this.poolName);
		super.stopServer();
	}
	
	private int index = 0;
	public synchronized ReadSelectorThread getSelector() {
		if(!this.isRunning()){
			return null;
		}
		ReadSelectorThread selector = this.selectorArr[index];
		index ++;
		if(index >= this.selectorNum){
			index = 0;
		}
		return selector;
	}

}
