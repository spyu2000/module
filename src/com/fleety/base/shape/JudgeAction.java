package com.fleety.base.shape;
/**
 * 点与形状位置关系判断的jni接口
 * create by leo 2009-12-07
 *
 */
public class JudgeAction implements IAction{
	
	static
	{
		try{
			System.loadLibrary("judgeModule");
		}catch(Throwable e){
			e.printStackTrace();
		}
	}

	public native void addShape(int instanceId,int pid,double[] lo,double[] la,int type,int areaType);
	public native void removeShape(int instanceId,int pid,int type);
	public native int getArea(int instanceId,int areaId,double lo,double la);
	public native int getNearestPoint(int instanceId,double lo,double la,int limitDis);
	public native int getNearestLine(int instanceId,int lineId,double lo,double la,int limitDis,int dir);
	
	public int getNearestLine(int instanceId,int lineId,double lo,double la,int limitDis,int dir,ResultInfo resultInfo){
		return getNearestLine(instanceId,lineId,lo,la,limitDis,dir);
	}
}
