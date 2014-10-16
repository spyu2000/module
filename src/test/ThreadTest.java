/**
 * æ¯√‹ Created on 2008-5-5 by edmund
 */
package test;

import com.fleety.base.FleetyBase64;
import com.fleety.base.FleetyThread;
import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;

import server.lock.ThreadLockManagerServer;

public class ThreadTest{
	private static A a = new A(), b = new A();
	public static void main(String[] argv){
		
		try{
			FleetyBase64 base = new FleetyBase64();
			base.setBase64Char("0123456789:;ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz");
			System.out.println(new FleetyBase64().encode("À’E-M9610".getBytes("GB2312"),false));
			System.out.println(base.encode("À’E-M190—ß".getBytes("GB2312"),false));
		}catch(Exception e){
			e.printStackTrace();
		}
		if(true)return;
		
		ThreadLockManagerServer.getSingleInstance().startServer();
		startThread(3);
		
		for(int i=0;i<3;i++){
			new Thread(){
				public void run(){
					try{
						Thread.sleep(1000);
						ThreadLockManagerServer.getSingleInstance().waitLock(a);
						synchronized(a){
							ThreadLockManagerServer.getSingleInstance().holdLock(a);
							
							Thread.sleep(1000);
							
							ThreadLockManagerServer.getSingleInstance().waitLock(b);
							synchronized(b){
								ThreadLockManagerServer.getSingleInstance().holdLock(b);
	
								Thread.sleep(1000);
							}
							ThreadLockManagerServer.getSingleInstance().releaseLock(b);
						}
						ThreadLockManagerServer.getSingleInstance().releaseLock(a);
					}catch(Exception e){
						e.printStackTrace();
					}
				}
			}.start();
		}
	}
	
	private void testFleetyThread(){
		new FleetyThread(){
			public void run(){
				try{
					Thread.sleep(10000);
					}catch(Exception e){}
				int a = 0;
				int b = 0;
				int c = a/b;
			}
		}.start();
		
		new FleetyThread(){
			public void run(){
				try{
					Thread.sleep(10000);
					}catch(Exception e){}
				byte[] a = new byte[1024*1024*128];
			}
		}.start();
		
		new FleetyThread(){
			public void run(){
				int[] b = new int[10];
				try{
				Thread.sleep(10000);
				}catch(Exception e){}
				System.out.println(b[10]);
			}
		}.start();
		
		try{
			Thread.sleep(1000);
			FleetyThread.printThreadInfo(null);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	private static void startThread(int num){
		for(int j=0;j<num;j++){
			new Thread(){
				public void run(){
					this.execute();
				}
				
				public void execute(){
					try{
						Thread.sleep(1000);
						ThreadLockManagerServer.getSingleInstance().waitLock(b);
						synchronized(b){
							ThreadLockManagerServer.getSingleInstance().holdLock(b);
							
							Thread.sleep(1000);
							
							ThreadLockManagerServer.getSingleInstance().waitLock(a);
							synchronized(a){
								ThreadLockManagerServer.getSingleInstance().holdLock(a);
	
								Thread.sleep(1000);
							}
							ThreadLockManagerServer.getSingleInstance().releaseLock(a);
						}
						ThreadLockManagerServer.getSingleInstance().releaseLock(b);
					}catch(Exception e){
						e.printStackTrace();
					}
				}
			}.start();
		}
	}
	
	
	private static class A extends Object{
		public A(){
			
		}
	}
}
