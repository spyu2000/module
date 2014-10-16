package test;

import java.io.*;
import java.net.*;
public class SocketClient{
	public static  String serverip="192.168.0.123";
	public static  int port =7600;
    static 	DataInputStream   din;
	static  DataOutputStream  dout;
	
    public static void main(String args[]) throws Exception{
    	System.in.read();
    	 Socket a=new Socket(serverip,port);
    	 long t = System.currentTimeMillis();
    	 InputStream in = a.getInputStream();
    	 
    	 int totalLen = 10*1024*1024;
    	 int count = 0;
    	 byte[] buff = new byte[1024*10];
    	 while(count < totalLen){
    		 count += in.read(buff);
    	 }
    	 System.out.println(System.currentTimeMillis()-t);
    	 
      }
}

