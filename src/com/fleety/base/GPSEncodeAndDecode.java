package com.fleety.base;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.zip.GZIPInputStream;

public class GPSEncodeAndDecode {
	public GPSEncodeAndDecode(String dataPath){
		this.initFromText(dataPath);
	}
	
	public double[] encode(double loIn,double laIn){
		int keyId = ((((int)(((loIn+180)*10)+0.5))<<16)|((int)(((laIn+90)*10)+0.5)));
	    Integer value = (Integer)mapping.get(new Integer(keyId));      
	
		if(value == null){
			return new double[]{loIn,laIn};
		}else{
			int offsetLo = (short)(value>>16);
			int offsetLa = (short)(value&0xFFFF);
			return new double[]{loIn + offsetLo/1000000.0,laIn + offsetLa/1000000.0};
		}
	}
	public double[] decode(double loIn,double laIn){
		int keyId = ((((int)(((loIn+180)*10)+0.5))<<16)|((int)(((laIn+90)*10)+0.5)));
	    Integer value = (Integer)mapping.get(new Integer(keyId));
	    
		if(value == null){
			return new double[]{loIn,laIn};
		}else{
			int offsetLo = (short)(value>>16);
			int offsetLa = (short)(value&0xFFFF);
			return new double[]{loIn - offsetLo/1000000.0,laIn - offsetLa/1000000.0};
		}
	}
	
	public double[] decodeWithLine(double loIn,double laIn){
		int x = (int)((loIn+180)*10);
		int y = (int)((laIn+90)*10);
		int keyId = (x<<16)|y;
	    Integer value = (Integer)mapping.get(new Integer(keyId));
		int keyIdU = (x<<16)|(y+1);
	    Integer valueU = (Integer)mapping.get(new Integer(keyIdU));  
		int keyIdR = ((x+1)<<16)|y;
	    Integer valueR = (Integer)mapping.get(new Integer(keyIdR));  
		int keyIdUR = ((x+1)<<16)|(y+1);
	    Integer valueUR = (Integer)mapping.get(new Integer(keyIdUR));      
	
		if(value == null || valueU == null || valueR == null || valueUR == null){
			return new double[]{loIn,laIn};
		}else{
			int offsetLo = (short)(value>>16);
			int offsetLa = (short)(value&0xFFFF);
			int offsetLoU = (short)(valueU>>16);
			int offsetLaU = (short)(valueU&0xFFFF);
			int offsetLoR = (short)(valueR>>16);
			int offsetLaR = (short)(valueR&0xFFFF);
			int offsetLoUR = (short)(valueUR>>16);
			int offsetLaUR = (short)(valueUR&0xFFFF);
			double ratioX = 1- (loIn - (x/10.0-180)) / 0.1;
			double ratioY = 1-   (laIn - (y/10.0-90)) / 0.1;
			
			int offsetX = (int)Math.round((offsetLo * ratioX + offsetLoR *(1-ratioX))*ratioY 
						+ (offsetLoU * ratioX + offsetLoUR *(1-ratioX))*(1-ratioY)); 
			int offsetY = (int)Math.round((offsetLa * ratioY + offsetLaU *(1-ratioY))*ratioX 
						+ (offsetLaR * ratioY + offsetLaUR *(1-ratioY))*(1-ratioX)); 

			return new double[]{loIn - offsetX/1000000.0,laIn - offsetY/1000000.0};
		}
	}
	
	private HashMap mapping = new HashMap();
	private void putData(int areaId,int offsetLo,int offsetLa){
		int value = (short)offsetLo;
		value = (value<<16) | (((short)offsetLa)&0xFFFF);
		mapping.put(new Integer(areaId),new Integer(value));
	}
	
	private void init(){
		try{			
			BufferedInputStream in = new BufferedInputStream(new FileInputStream("./data.gps"));
			ByteBuffer buff = ByteBuffer.allocate(8);
			buff.order(ByteOrder.LITTLE_ENDIAN);
			int count = in.read(buff.array());
			while(count == buff.capacity()){
				this.putData(buff.getInt(0), buff.getShort(4), buff.getShort(6));
				
				count = in.read(buff.array());
			}
			in.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	private void initFromText(String dataPath){
		try{
			BufferedReader in = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(dataPath))));
			String str = in.readLine();
			String[] arr = null;
			while(str != null){
				arr = str.split(",");
				this.putData(Integer.parseInt(arr[0]), Integer.parseInt(arr[1]), Integer.parseInt(arr[2]));
				
				str = in.readLine();
			}
			in.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public static void main(String[] argv){
		GPSEncodeAndDecode decode = new GPSEncodeAndDecode("baidu_gpsdata.zip");
		double lo = 119.966868;
		double la = 31.794684;
		
		double[] arr = decode.decode(lo, la);
		System.out.println(arr[0]+","+arr[1]);
		arr = decode.decodeWithLine(lo, la);
		System.out.println(arr[0]+","+arr[1]);
	}
}
