package com.fleety.base.shape.java;

public class SegmentInfo {
	private int segmentId;
	private int pointIndex;
	
	public SegmentInfo(int segmentId,int pointIndex){
		this.segmentId = segmentId;
		this.pointIndex = pointIndex;
	}
	
	public int getSegmentId(){
		return this.segmentId;
	}
	public int getPointIndex(){
		return this.pointIndex;
	}
}
