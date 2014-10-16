package com.fleety.base;

public class GPSEncode
{
    public native static boolean encode(double loIn, double laIn, double[] point);      
    public native static boolean decode(double loIn, double laIn, double[] point);
 
	
    static {
        System.loadLibrary("DataCal");
    }
 
    public void call(boolean bShow)
    {
       if(bShow)
       {
          System.out.println("Test start...");
       }
       double loIn = 121.2345;
       double laIn = 31.6789;
       double arr[] = new double[2];
       boolean  bRes = encode(loIn,laIn,arr);
       if(bShow)
       {
           System.out.println("encode:la:" + arr[0] +" la:"+arr[1] + " exce:"+bRes);
       }
       loIn = arr[0];
       laIn = arr[1];
       double loOut = 0;
       double laOut = 0;       
       bRes = decode(loIn,laIn,arr);
       if(bShow)
       {
           System.out.println("decode la:" + arr[0] +" la:"+arr[1] + " exec:"+bRes);
           System.out.println("call over..");
       }
    }
}
