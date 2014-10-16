package com.fleety.base.shape.java;

public class PointInfo {
	private int id = 0;
	private String desc;
	private double lo;
	private double la;
	private short dir;

	private int segmentId;
	private int segmentOrder = 0;
	private int pointIndex = 0;
	private int assLom = 0;
	private int assLam = 0;
	private int distance = 0;
	
	public PointInfo(int id,double lo,double la,short dir){
		this.id = id;
		this.lo = lo;
		this.la = la;
		this.dir = dir;
	}
	
	public int getId(){
		return this.id;
	}
	public String getDesc(){
		return this.desc;
	}
	public double getLo(){
		return this.lo;
	}
	public double getLa(){
		return this.la;
	}
	public short getDir(){
		return this.dir;
	}

	public void setAssociateInfo(int segmentId,int pointIndex,int lom,int lam,int distance){
		this.setAssociateInfo(segmentId, pointIndex, lom, lam, distance, 1);
	}
	public void setAssociateInfo(int segmentId,int pointIndex,int lom,int lam,int distance,int segmentOrder){
		this.segmentId = segmentId;
		this.pointIndex = pointIndex;
		this.assLom = lom;
		this.assLam = lam;
		this.distance = distance;
		this.segmentOrder = segmentOrder;
	}
	public int getSegmentId(){
		return this.segmentId;
	}
	public int getSegmentOrder(){
		return this.segmentOrder;
	}
	public int getPointIndex(){
		return this.pointIndex;
	}
	public int getAssLom(){
		return this.assLom;
	}
	public int getAssLam(){
		return this.assLam;
	}
	public int getDistance(){
		return this.distance;
	}
}
