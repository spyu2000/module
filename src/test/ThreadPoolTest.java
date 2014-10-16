/**
 * ¾øÃÜ Created on 2008-2-1 by edmund
 */
package test;

import com.fleety.util.pool.thread.ITask;
import com.fleety.util.pool.thread.ThreadPool;
import com.fleety.util.pool.thread.ThreadPoolEventListener;


public class ThreadPoolTest
{
	/**
	 * @param args
	 */
	public static void main(String[] args){
		try{
			ThreadPoolTest t = new ThreadPoolTest();
			ThreadPool threadPool = new ThreadPool(ThreadPool.MULTIPLE_TASK_LIST_POOL,3,10);
			threadPool.addEventListener(new ThreadPoolEventListener(){
				public void eventHappen(EventInfo event){
					System.out.println(event.getSource());
				}
			});
			for(int i=0;i<34;i++){
				threadPool.addTask(t.new WorkTask(null));
			}
			threadPool.addTask(t.new WorkTask("1"));
//			threadPool.addTask(t.new WorkTask("1"));
//			threadPool.addTask(t.new WorkTask("2"));
//			threadPool.addTask(t.new WorkTask("2"));
//			threadPool.addTask(t.new WorkTask("1"));
//			threadPool.addTask(t.new WorkTask("3"));
//			threadPool.addTask(t.new WorkTask("3"));
//			threadPool.addTask(t.new WorkTask("3"));
//			threadPool.addTask(t.new WorkTask("3"));
//			System.out.println("aaaaaaaa");
//			threadPool.addTask(t.new WorkTask("3"));
//			threadPool.addTask(t.new WorkTask("3"));
//			System.out.println("---");
////			Thread.sleep(1000);
//			threadPool.addTask(t.new WorkTask("3"));
//			threadPool.addTask(t.new WorkTask("1"));
//			threadPool.addTask(t.new WorkTask("3"));
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	private class WorkTask implements ITask{
		String flag = null;
		public WorkTask(String flag){
			this.flag = flag;
		}
		
		public Object getFlag(){
			return this.flag;
		}
		public String getDesc(){
			return null;
		}
		
		public boolean execute() throws Exception{
			System.out.println("execute");
			Thread.sleep(5000);
			System.out.println(this.flag + ";" + Thread.currentThread().getName());
			return true;
		}
	}
}
