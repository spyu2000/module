/**
 * ¾øÃÜ Created on 2009-12-18 by edmund
 */
package com.fleety.base;

public class GPSEncode{
	static{
		init(null);
	}
	public static boolean init(String dllName){
		if(dllName == null){
			if(Util.isWindows()){
				dllName = Util.systemBits()==32?"gps_encode32":"gps_encode64";
			}else{
				dllName = Util.systemBits()==32?"_gps_encode32":"_gps_encode64";
			}
		}
		
		try{
			System.loadLibrary(dllName);
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	public native static boolean encode(double oriLon,double oriLat,double[] result);
	public native static boolean decode(double lo,double la,double[] result);
	
	public static void main(String[] argv){
		long t = System.currentTimeMillis();
		
		double[] a = new double[2];
		GPSEncode.encode(119.8109,31.3748, a);
		
		System.out.println(a[0]+"  "+a[1]);
		
		System.out.println(System.currentTimeMillis() - t);
	}
}