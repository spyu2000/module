/**
 * ¾øÃÜ Created on 2010-7-1 by edmund
 */
package test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;

import com.fleety.base.shape.IAction;
import com.fleety.base.shape.JudgeServer;

public class TestSahpe{
	/**
	 * @param args
	 */
	public static void main(String[] args){
		try{
			testPoint();
			
//			testPolygon();
			
//			testLine();
			
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	private static void testPoint() throws Exception{
		double[] lo = new double[1],la = new double[1];
		JudgeServer server = new JudgeServer();

		lo[0] = 0.5;
		la[0] = 0.5;
		server.addShape(1, lo, la, JudgeServer.POINT_FLAG);
		
		lo[0] = 0.9;
		la[0] = 0.9;
		server.addShape(2, lo, la, JudgeServer.POINT_FLAG);
		
		System.out.println(server.getNearestPoint(0.6, 0.6, 10000));
	}
	
	private static void testPolygon() throws Exception{
		JudgeServer server = new JudgeServer();
		double[] lo = new double[3];
		double[] la = new double[3];

		lo[0] = 0;
		lo[1] = 0;
		lo[2] = 1;
		la[0] = 0;
		la[1] = 1;
		la[2] = 1;
		server.addShape(1, lo, la, JudgeServer.AREA_FLAG,IAction.POLYGON_FLAG);
		
		System.out.println(server.getArea(0.5, 0.6));
	}
	
	private static void testLine() throws Exception{
		System.out.println("----------");
		JudgeServer server = new JudgeServer();
		double[] lo = new double[3];
		double[] la = new double[3];
		lo[0] = 1;
		la[0] = 1;
		lo[1] = 1;
		la[1] = 10;
		lo[2] = 10;
		la[2] = 11;
		
		long t = System.currentTimeMillis();
		server.addShape(1, lo, la, JudgeServer.DOUBLE_LINE_FLAG);
		System.out.println(System.currentTimeMillis()-t);
		
		t= System.currentTimeMillis();
		System.out.println(server.getNearestLine(1, 0.998, 250, 0));
		System.out.println(System.currentTimeMillis()-t);
	}
}
