package test;

import java.io.FileInputStream;
import java.util.Timer;
import java.util.TimerTask;

import server.socket.udpsocket.FleetyUdpServer;

public class udpTest1 extends FleetyUdpServer {


	private static udpTest1 singleInstance = null;
	
	public static udpTest1 getSingleInstance(){
		if(singleInstance == null){
			synchronized(udpTest1.class){
				if(singleInstance == null){
					singleInstance = new udpTest1();
				}
			}
		}
		return singleInstance;
	}

	public udpTest1(){
		
	}
	
	public boolean startServer(){
		super.startServer();
		
		try{
			System.out.println("send");
			byte[] buff = new byte[64];
			
			for(int i=0;i<10;i++){
				this.sendData(buff);
				System.out.println("aaaaaa");
				Thread.sleep(1000);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return true;
	}
}
