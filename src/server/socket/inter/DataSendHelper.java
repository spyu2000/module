/**
 * ���� Created on 2008-9-20 by edmund
 */
package server.socket.inter;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import server.threadgroup.PoolInfo;
import server.threadgroup.ThreadPoolGroupServer;

import com.fleety.util.pool.thread.BasicTask;
import com.fleety.util.pool.thread.ThreadPool;

public class DataSendHelper{
	private static DataSendHelper singleInstance = null;
	public static DataSendHelper getSingleInstance(){
		if(singleInstance == null){
			synchronized(DataSendHelper.class){
				if(singleInstance == null){
					singleInstance = new DataSendHelper();
				}
			}
		}
		return singleInstance;
	}
	
	private boolean isStop = false;
	private SendThread sendThread = null;
	private DataSendHelper(){
		this.isStop = false;
		
		sendThread = new SendThread();
		sendThread.start();
	}
	
	private Object lock = new Object();
	private List connList = new LinkedList();;
	public void add2Help(ConnectSocketInfo conn){
		synchronized(lock){
			if(connList.contains(conn)){
				return ;
			}
			connList.add(conn);
			lock.notify();
		}
	}
	
	private class SendThread extends BasicTask{
		private long loopCycle = 30;
		private ConnectSocketInfo curConn;
		private List tempList = new LinkedList(),swapList;
		
		private String poolName = null;
		public void start(){
			try{
				this.poolName = "Data Send Helper["+this.hashCode()+"] Send Thread";
				PoolInfo pInfo = new PoolInfo(ThreadPool.SINGLE_TASK_LIST_POOL,1,1,false);
				ThreadPool pool = ThreadPoolGroupServer.getSingleInstance().createThreadPool(poolName, pInfo);
				pool.addTask(this);
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		public boolean execute() throws Exception{
			try{
				this.run();
			}catch(Throwable e){
				e.printStackTrace();
			}finally{
				ThreadPoolGroupServer.getSingleInstance().removeThreadPool(this.poolName);
			}
			return true;
		}
		
		public void run(){
			while(!isStop){
				synchronized(lock){
					if(connList.isEmpty()){  //���û�д�������,��ȴ�
						try{
							lock.wait();
						}catch(Exception e){
							e.printStackTrace();
						}
					}
					
					swapList = tempList;
					tempList = connList;
					connList = swapList;
				}
					
				
				for(Iterator itr = tempList.iterator();itr.hasNext();){
					curConn = (ConnectSocketInfo)itr.next();
					itr.remove();
					
					//���һ����Чд������ѳ�����60��.��ζ�����60�붼û�������ܷ��ͳ�ȥ,���ٸ���д�����ӵ�����
					if(curConn.getLastWriteTimeDiff() >= 60000){ 
						
					}else{
						//��Ȼ����ʣ������
						if(curConn.flush() && curConn.hasRemainData()){
							DataSendHelper.this.add2Help(curConn);
						}
					}
				}
				
				synchronized(lock){
					if(!connList.isEmpty()){  //������ڴ�д��,��ȴ�ѭ�������Ժ��ٽ�����д.
						try{
							lock.wait(loopCycle);
						}catch(Exception e){
							e.printStackTrace();
						}
					}
				}
			}
		}
	}
}
