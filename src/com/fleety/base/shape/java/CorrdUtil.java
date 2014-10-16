package com.fleety.base.shape.java;

public class CorrdUtil {
	public static final int UNIT = 200;
	
	public static final double PI = 3.1415926;
	public static final double LO_SCALE = 0.010506;
	public static final double LA_SCALE = 0.009;
	
	public static boolean isInter(long areaId,Point p1,Point p2){
		return isInter(areaId,p1,p2,UNIT);
	}
	/**
	 * 首先判断经纬度上是否有重合
	 * 然后判断区域的四个角是否在线段的同侧。
	 * 原始分割区域边界描述，[)
	 */
	public static boolean isInter(long areaId,Point p1,Point p2,int unit){
		double lo1 = p1.getLo();
		double la1 = p1.getLa();
		double lo2 = p2.getLo();
		double la2 = p2.getLa();
		
		double minLo = lo1>lo2?lo2:lo1;
		double maxLo = lo1>lo2?lo1:lo2;
		double minLa = la1>la2?la2:la1;
		double maxLa = la1>la2?la1:la2;

		long loAreaId = areaId >> 32;
		long laAreaId = areaId & 0xFFFFFFFFl;

		double oriMinLo = loAreaId*CorrdUtil.LO_SCALE/1000*unit - 180;
		double oriMinLa = laAreaId*CorrdUtil.LA_SCALE/1000*unit - 90;
		double oriMaxLo = oriMinLo + unit*CorrdUtil.LO_SCALE/1000;
		double oriMaxLa = oriMinLa + unit*CorrdUtil.LA_SCALE/1000;

		if(maxLo < oriMinLo || minLo>= oriMaxLo || maxLa < oriMinLa || minLa>=oriMaxLo){
			return false;
		}

		if(lo2 - lo1 == 0){
			return true;
		}

		double tag = (la2-la1)/(lo2-lo1);
		double offset = (la1*lo2-la2*lo1)/(lo2-lo1);

		boolean isPositive;

		isPositive = (tag*oriMinLo - oriMinLa + offset >= 0);
		if(isPositive != (tag*oriMinLo - oriMaxLa + offset >= 0)){
			return true;
		}
		if(isPositive != (tag*oriMaxLo - oriMinLa + offset >= 0)){
			return true;
		}
		if(isPositive != (tag*oriMaxLo - oriMaxLa + offset >= 0)){
			return true;
		}

		return false;
	}
	
	public static int lola2m(double lola,boolean isLo){
		if(isLo){
			return (int)((lola + 180) * 1000 / CorrdUtil.LO_SCALE + 0.5);
		}else{
			return (int)((lola + 90) * 1000 / CorrdUtil.LA_SCALE + 0.5);
		}
	}
	public static double m2lola(long m,boolean isLo){
		if(isLo){
			return m * CorrdUtil.LO_SCALE / 1000 - 180;
		}else{
			return m * CorrdUtil.LA_SCALE / 1000 - 90;
		}
	}

	public static long getAreaId(double lo,double la){
		return getAreaId(lo,la,UNIT);
	}
	public static long getAreaId(double lo,double la,int unit){
		long result = 0;

		long loAreaId = (long)((lo + 180) * 1000 / CorrdUtil.LO_SCALE / unit);
		long laAreaId = (long)((la + 90) * 1000 / CorrdUtil.LA_SCALE / unit);

		result = (loAreaId << 32) + laAreaId;

		return result;
	}

	public static int distance(int lo1,int la1,int lo2,int la2){
		long lo = lo1 - lo2;
		long la = la1 - la2;

		return (int)Math.sqrt(lo*lo+la*la);
	}
	
	public static long distancePower(int lo1,int la1,int lo2,int la2){
		long lo = lo1 - lo2;
		long la = la1 - la2;

		return lo*lo+la*la;
	}
	
	public static long countInterPoint(Point p1,Point p2,Point p3){
		long lo1 = p1.getLom();
		long la1 = p1.getLam();
		long lo2 = p2.getLom();
		long la2 = p2.getLam();
		long lo3 = p3.getLom();
		long la3 = p3.getLam();

		long lo4;
		long la4;
		double tempLo;

		if(lo2 - lo1 == 0){
			la4 = la3;
			lo4 = lo1;
		}else{
			double tag = (la2-la1)*1.0/(lo2-lo1);
			if(tag == 0){
				lo4 = lo3;
				la4 = la1;
			}else{
				double tagOver = 1/tag;
				long offset = (long)((la1*lo2-la2*lo1)*1.0/(lo2-lo1));

				tempLo = (la3-offset + lo3*tagOver)/(tag + tagOver);
				lo4 = (long)tempLo;
				tempLo = tag * tempLo + offset;
				la4 = (long)tempLo;
			}
		}
		return (lo4<<32)+la4;
	}
	
	public static int arc2Angle(double arc){
		return (int)(arc/CorrdUtil.PI * 180 + 0.5);
	}
	
	public static double angle2Arc(int angle){
		return angle * CorrdUtil.PI / 180;
	}
}
