/**
 * ¾øÃÜ Created on 2008-4-28 by edmund
 */
package com.fleety.base;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Hashtable;

public class GeneralConst{
	private static Hashtable dateFormatMapping = new Hashtable(16);
	
	public static final SimpleDateFormat YYYYMMDDHHMMSS = GeneralConst.getSimpleDateFormat("yyyyMMddHHmmss");
	public static final SimpleDateFormat YYYYMMDDHHMMSSsss = GeneralConst.getSimpleDateFormat("yyyyMMddHHmmssSSS");
	public static final SimpleDateFormat YYYYMMDD = GeneralConst.getSimpleDateFormat("yyyyMMdd");
	public static final SimpleDateFormat YYYY_MM = GeneralConst.getSimpleDateFormat("yyyy-MM");
	public static final SimpleDateFormat YYYY_MM_DD = GeneralConst.getSimpleDateFormat("yyyy-MM-dd");
	public static final SimpleDateFormat YYYY_MM_DD_HH_MM_SS = GeneralConst.getSimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	public static final SimpleDateFormat YYYY_MM_DD_HH_MM_SS_SSS = GeneralConst.getSimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
	public static final SimpleDateFormat YYYY_MM_DD_HH_MM = GeneralConst.getSimpleDateFormat("yyyy-MM-dd HH:mm");
	public static final SimpleDateFormat YYYY_MM_DD_HH = GeneralConst.getSimpleDateFormat("yyyy-MM-dd HH");
	public static final SimpleDateFormat HH = GeneralConst.getSimpleDateFormat("HH");
	public static final SimpleDateFormat HHMM = GeneralConst.getSimpleDateFormat("HHmm");
	public static final SimpleDateFormat HHMMSS = GeneralConst.getSimpleDateFormat("HHmmss");
	public static final SimpleDateFormat YYMMDDHHMMSS = GeneralConst.getSimpleDateFormat("yyMMddHHmmss");

	public static final long ONE_HOUR_TIME = 60 * 60 * 1000l;
	public static final long ONE_DAY_TIME = 24 * 60 * 60 * 1000l;

	public static final SimpleDateFormat getSimpleDateFormat(String mode){
		SimpleDateFormat tmp = null;
		try{
			if(mode == null){
				return null;
			}
			tmp = (SimpleDateFormat) dateFormatMapping.get(mode);
			if(tmp == null){
				tmp = new FleetyDateFormat(mode);
				dateFormatMapping.put(mode, tmp);
			}
		} catch(Exception e){
			e.printStackTrace();
		}
		return tmp;
	}
    public static final long getNextInteval(int cycleSecond,int offSetSecond){
        return getNextInteval(System.currentTimeMillis(),cycleSecond, offSetSecond);
    }
    public static final long getNextInteval(long srcTime,int cycleSecond,int offSetSecond){
        Calendar calendar=Calendar.getInstance();
        calendar.setTimeInMillis(srcTime);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        while(calendar.getTimeInMillis()<srcTime){
            calendar.add(Calendar.SECOND, cycleSecond);
            System.out.println(calendar.getTime().toLocaleString());
        }
        
        calendar.add(Calendar.SECOND, offSetSecond);        
        return calendar.getTimeInMillis()-srcTime;
    }
    
    public static void main(String[] args){
        long time=GeneralConst.getNextInteval(3600, 60);
        System.out.println(time);
    }
}
