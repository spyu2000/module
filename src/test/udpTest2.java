package test;

import java.util.Timer;
import java.util.TimerTask;

import server.socket.udpsocket.FleetyUdpServer;

public class udpTest2 extends FleetyUdpServer {



	private static udpTest2 singleInstance = null;
	
	public static udpTest2 getSingleInstance(){
		if(singleInstance == null){
			synchronized(udpTest2.class){
				if(singleInstance == null){
					singleInstance = new udpTest2();
				}
			}
		}
		return singleInstance;
	}
	public boolean startServer()
	{
		boolean t1=super.startServer();

		Timer timer = new Timer();

		timer.schedule(new TimerTask(){
			public void run(){
				new packet().sendData();
			}
		}, 0, 10000*1000l);
		return t1;
	}
}
