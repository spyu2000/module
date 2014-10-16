package com.fleety.base.shape.java;

public class Point {
	public final static int NOT_SAME_DIR = 0;
	public final static int NATURAL_ORDER_SAME_DIR = 1;
	public final static int REVERSE_ORDER_SAME_DIR = 2;
	
	private int pid;
	private double lo;
	private double la;
	private boolean isLast = true;
	
	private int lom;
	private int lam;

	//和后一个点所形成直线的角度，[0,360)
	private int angle = 0;
	//和后一个点的距离，单位米
	private int followDistance = 0;

	//从头到该点的距离，单位米
	private int distanceFromHead = 0;
	
	public Point(int pid,double lo,double la){
		this(pid,lo,la,true);
	}
	public Point(int pid,double lo,double la,boolean isLast){
		this.pid = pid;
		this.lo = lo;
		this.la = la;
		this.isLast = isLast;
		
		this.lom = CorrdUtil.lola2m(this.lo,true);
		this.lam = CorrdUtil.lola2m(this.la,false);
	}
	
	public int getPid(){
		return this.pid;
	}

	public double getLo(){
		return this.lo;
	}
	public double getLa(){
		return this.la;
	}

	public int getLom(){
		return this.lom;
	}
	public int getLam(){
		return this.lam;
	}

	public boolean isLastPoint(){
		return this.isLast;
	}
	public void setAngle(int angle){
		this.angle = angle;
	}
	public int getAngle(){
		return this.angle;
	}

	public void setFollowDistance(int followDistance){
		this.followDistance = followDistance;
	}
	public int getFollowDistance(){
		return this.followDistance;
	}

	public void setDistanceFromHead(int distanceFromHead){
		this.distanceFromHead = distanceFromHead;
	}
	public int getDistanceFromHead(){
		return this.distanceFromHead;
	}

	public void updateNextPointAssoInfo(Point nextPoint){
		this.isLast = false;
		if(this.lom == nextPoint.getLom()){
			this.angle = nextPoint.getLam() > this.lam ? 0 : 180;
		}else{
			double arc = Math.atan((nextPoint.getLam()-this.lam)*1.0/(nextPoint.getLom()-this.lom));
			arc = CorrdUtil.PI/2 - arc;
			//PI的精度不够，可能会出现负数。
			if(arc < 0){
				arc = 0;
			}
			if(nextPoint.getLom() < this.lom){
				arc += CorrdUtil.PI;
			}
			this.angle = CorrdUtil.arc2Angle(arc);
		}
		this.followDistance = CorrdUtil.distance(nextPoint.getLom(),nextPoint.getLam(),this.lom,this.lam);
	}

	public int isSameDir(int angle){
		return this.isSameDir(angle, 90);
	}
	public int isSameDir(int angle,int offset){
		int realOffset = Math.abs(this.angle - angle);
		if(realOffset > 180){
			realOffset = 360 - realOffset;
		}

		if(realOffset <= offset){
			return NATURAL_ORDER_SAME_DIR;
		}else if(180 - realOffset <= offset){
			return REVERSE_ORDER_SAME_DIR;
		}

		return NOT_SAME_DIR;
	}
}
