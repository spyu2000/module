/**
 * ¾øÃÜ Created on 2008-7-13 by edmund
 */
package com.fleety.server;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Calendar;
import java.util.Date;

import com.fleety.base.FleetyThread;
import com.fleety.base.GeneralConst;

public class GSDOG implements IDog{
	public int DogCascade = 0;
	public long DogResult = 0;
	public byte[] DogData;
	public int DogAddr = 0;
	public int DogPassword = 0;
	public int DogBytes = 6;
	
	public GSDOG(){
		this.DogData = new byte[1024];
	}
	
	public boolean detect(){
		new FleetyThread("Dog Detect"){
			public void run(){
				try{
					FleetyThread.sleep(60*60*1000l);
				}catch(Exception e){}
				
				GSDOG.this.scan();
			}
		}.start();
		
		return this.scan();
	}
	
	private boolean scan(){
		try{
			if(!this.subScan()){
				throw new Exception("Mic Dog Unvalid Error!");
			}
		}catch(Throwable e){
			e.printStackTrace();
			System.exit(1);
			return false;
		}
		return true;
	}
	
	private boolean subScan(){
		if(this.DogCheck() != 0){
			System.out.println("Mic Dog Not Exist!");
			return false;
		}else{
			this.DogAddr = 0;
			this.DogBytes = 15;
			
			String flag = "staff.fleety.cn";
			
			if(this.GetCurrentNo() != 0){
				System.out.println("Mic Dog No Error!");
				return false;
			}
			int curNo = (int)this.DogResult;
			
			if(this.ReadDog()!=0){
				System.out.println("Mic Dog Read Error!");
				return false;
			}
			
			if(!new String(this.DogData,0,this.DogBytes).trim().equals(flag)){
				System.out.println("Mic Dog Flag Error!");
				return false;
			}
			
			this.DogAddr = 100;
			this.DogBytes = 8;
			if(this.ReadDog()!=0){
				System.out.println("Mic Dog Read Error!");
				return false;
			}
			ByteBuffer dataBuff = ByteBuffer.wrap(this.DogData,0,this.DogBytes);
			dataBuff.order(ByteOrder.LITTLE_ENDIAN);
			int year = dataBuff.getShort();
			Calendar cal = Calendar.getInstance();
			cal.set(year, (dataBuff.get(2)&0xFF)-1, dataBuff.get(3)&0xFF);

			if(cal.getTime().before(new Date())){
				System.out.println("Mic Dog Expire Error!");
				return false;
			}
			
			if(curNo != dataBuff.getInt(4)){
				System.out.println("Mic Dog CurrentNo Error!");
				return false;
			}
		}
		
		return true;
	}
	
	public static void main(String[] argv){
		new GSDOG();
	}
	
	
	 /**
	* Perform Dog Check Service 
	* @param none.
	*/

	public native int DogCheck();  

	/**
	* Perform Dog Convert Service 
	* @param none.
	*/

	public native int DogConvert();  

	/**
        * Perform Dog Write Service 
	* @param none.
	*/

	public native int WriteDog();
	
	/**
        * Perform Dog Read Service 
	* @param none.
	*/

	public native int ReadDog();
	

	/**
        * Perform Dog GetCurrentNo Service 
	* @param none.
	*/

	public native int  GetCurrentNo();
	
	/**
	 *	Static initializer 
	 */

    static{
        try{
        	System.loadLibrary("DOGJava");
        }
        catch( UnsatisfiedLinkError e ){
            e.printStackTrace();
        }
     }
}
