/**
 * ¾øÃÜ Created on 2008-5-18 by edmund
 */
package test;

import java.io.DataOutputStream;
import java.io.OutputStream;
import java.net.Socket;

public class SocketConnectTest{
	private static int ecount = 0;
	private static int count = 1;
	/**
	 * @param args
	 */
	public static void main(String[] args){
		for(int i=0;i<1;i++,count++){
			new Thread(){
				int count = SocketConnectTest.count;
				public void run(){
					try{
						Socket socket = new Socket("192.168.0.116",1980);
						
						Thread.sleep(100000);
						
						DataOutputStream out = new DataOutputStream(socket.getOutputStream());
						byte[] buff;
						if(count%2 == 1){
							buff = new byte[5];
							buff[0] = (byte)count;
							buff[1] = buff[2] = buff[3] = buff[4] = 0;
						}else{
							buff = new byte[9];
							buff[0] = (byte)count;
							buff[1] = buff[2] = buff[3] = 0;
							buff[4] = 4;
							buff[5] = buff[6] = buff[7] = buff[8] = 0;
						}
						int i = 0;
						while(i<1){
							out.writeUTF("2Äã1ÃÇaºÃ");
							out.flush();
							Thread.sleep(1000);
							i++;
						}
//						socket.close();
					}catch(Exception e){
						e.printStackTrace();
						ecount++;
					}
				}
			}.start();
			
			try{
				Thread.sleep(1000);
			}catch(Exception e){}
		}
		
		try{
			Thread.sleep(10000);
		}catch(Exception e){}
		System.out.println(ecount);
	}
}
