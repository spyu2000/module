package test;
/**
 * 测试点、线、面的添加、删除以及点与各形状关系的查询
 * create by leo 2009-12-14
 */
import java.util.ArrayList;

import com.fleety.base.shape.JudgeServer;
import com.fleety.server.BasicServer;

public class TestServer extends BasicServer{
	JudgeServer server = new  JudgeServer();
	ArrayList lineList = new ArrayList(2000);
	ArrayList areaList = new ArrayList(5000);
	ArrayList pointList = new ArrayList(20000);
	public boolean startServer() {
		new LineAddTest().start();
		new AreaAddTest().start();
		new PointAddTest().start();
		new LineQueryTest().start();
		new LineRemoveTest().start();
		return true;
	}
	
	private double[][] getPointArray(int pointSize){
		double[][] par= new double[2][pointSize];
		try {
			int offset = (int)(Math.random()*90);
			if(((int)(Math.random()*100))%2==0){
				offset = -offset;
			}
			for (int i = 0; i < pointSize; i++) {
				par[0][i] = (Math.random()*1)+offset;
				par[1][i] = (Math.random()*1)+offset;
			}
		} catch (Exception e) {
		}
		return par;
	}
	
	class LineAddTest extends Thread{
		public LineAddTest(){
		}
		public void run(){
			try {
				int i=0;
				while(true){
					for (int j = 0; j < 5; j++) {
						i++;
						double[][] lola = getPointArray(1000);
						double[] loArray = lola[0];
						double[] laArray = lola[1];
						int flag = JudgeServer.DOUBLE_LINE_FLAG;
						if(i%2==0){
							flag = JudgeServer.SINGLE_LINE_FLAG;
						}
//						long l1 = System.currentTimeMillis();
//						System.out.println("pre add shape "+i);
						server.addShape(i, loArray, laArray, flag);
//						System.out.println("add shape "+i+" over! cose "+(System.currentTimeMillis()-l1)+"ms");
						synchronized (lineList) {
							lineList.add(new Integer(i));
						}
						if(i%100==0){
							System.out.println("line number "+i);
						}
					}
					sleep(1000);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	class PointAddTest extends Thread{
		public PointAddTest(){
		}
		public void run(){
			try {
				int i=0;
				while(true){
					for (int j = 0; j < 100; j++) {
						i++;
						double[][] lola = getPointArray(1);
						double[] loArray = lola[0];
						double[] laArray = lola[1];
						server.addShape(i, loArray, laArray,JudgeServer.POINT_FLAG);
						synchronized (pointList) {
							pointList.add(new Integer(i));
						}
						if(i%1000==0){
							System.out.println("point number "+pointList.size());
						}
					}
//					System.out.println("point number "+pointList.size());
					sleep(1000);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	class AreaAddTest extends Thread{
		public AreaAddTest(){
			
		}
		public void run(){
			try {
				int i=0;
				while(true){
					for (int j = 0; j < 50; j++) {
						i++;
						double[][] lola = getPointArray(500);
						double[] loArray = lola[0];
						double[] laArray = lola[1];
						server.addShape(i, loArray, laArray, JudgeServer.AREA_FLAG);
						synchronized (areaList) {
							areaList.add(new Integer(i));
						}
						if(i%1000==0){
							System.out.println("area number "+i);
						}
					}
					sleep(1000);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	class LineQueryTest extends Thread{
		public void run(){
			try {
				int i=0;
				while(true) {
					for (int j = 0; j < 4000; j++) {
						double[][] lola = getPointArray(1);
						double[] loArray = lola[0];
						double[] laArray = lola[1];
						server.getNearestLine(loArray[0],laArray[0], 30, 120);
						server.getArea(loArray[0],laArray[0]);
						server.getNearestPoint(loArray[0],laArray[0], 1000);
					}
					i++;
					sleep(1000);
//					if(i%100==0){
						System.out.println("query count ::"+i);
//					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	class LineRemoveTest extends Thread{
		public void run(){
			try {
				while(true){
					synchronized (lineList) {
						while(lineList.size()>1000){
							Integer pid = (Integer)lineList.remove(0);
							server.removeShape(pid.intValue(), JudgeServer.DOUBLE_LINE_FLAG);
						}
					}
					synchronized (pointList) {
						while(pointList.size()>10000){
							Integer pid = (Integer)pointList.remove(0);
							server.removeShape(pid.intValue(), JudgeServer.POINT_FLAG);
						}
					}
					synchronized (areaList) {
						while(areaList.size()>3000){
							Integer pid = (Integer)areaList.remove(0);
							server.removeShape(pid.intValue(), JudgeServer.AREA_FLAG);
						}
					}
					sleep(500);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public void stopServer() {
		
	}

}
