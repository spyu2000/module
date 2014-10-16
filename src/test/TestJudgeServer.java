/**
 * ¾øÃÜ Created on 2010-8-20 by edmund
 */
package test;

import java.util.ArrayList;
import java.util.Iterator;
import com.fleety.base.shape.JudgeServer;

public class TestJudgeServer
{
	/**
	 * @param args
	 */
	public static void main(String[] args){
		try{
			for(int i=0;i<10000;i++){
				System.out.println("start-"+i);
				createJudgeServer(i);
				System.out.println("finish-"+i);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	private static ArrayList objList = new ArrayList(100);
	private static void createJudgeServer(int num) throws Exception{
		JudgeServer server = null;
		for(Iterator itr = objList.iterator();itr.hasNext();){
			server = (JudgeServer)itr.next();
			itr.remove();
			
			server.clearShape();
		}
		System.gc();
		Thread.sleep(200);

		for(int i=0;i<50;i++){
			double[] loArr = new double[100];
			double[] laArr = new double[100];
			for(int j=0;j<100;j++){
				loArr[j] = j/10.0;
				laArr[j] = j/20.0;
			}
			
			server = new JudgeServer();
			
			server.addShape(i, loArr, laArr, JudgeServer.DOUBLE_LINE_FLAG);
			
			objList.add(server);
			
			System.out.println("create one:"+i);
		}
	}
}
