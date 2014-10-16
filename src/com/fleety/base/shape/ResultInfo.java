package com.fleety.base.shape;

public class ResultInfo {
	private int bindedSegmentId = 0;
	private int bindedPointIndex = 0;
	private double bindedLo = 0;
	private double bindedLa = 0;
	private long minDistance = 0;
	private int segmentOrder = 0;
	private int offsetDistance = 0;
	
	public double getBindedLa() {
		return bindedLa;
	}
	public void setBindedLa(double bindedLa) {
		this.bindedLa = bindedLa;
	}
	public double getBindedLo() {
		return bindedLo;
	}
	public void setBindedLo(double bindedLo) {
		this.bindedLo = bindedLo;
	}
	public int getBindedPointIndex() {
		return bindedPointIndex;
	}
	public void setBindedPointIndex(int bindedPointIndex) {
		this.bindedPointIndex = bindedPointIndex;
	}
	public int getBindedSegmentId() {
		return bindedSegmentId;
	}
	public void setBindedSegmentId(int bindedSegmentId) {
		this.bindedSegmentId = bindedSegmentId;
	}
	public long getMinDistance() {
		return minDistance;
	}
	public void setMinDistance(long minDistance) {
		this.minDistance = minDistance;
	}
	public int getSegmentOrder() {
		return segmentOrder;
	}
	public void setSegmentOrder(int segmentOrder) {
		this.segmentOrder = segmentOrder;
	}
	public void setOffsetDistance(int offsetDistance){
		this.offsetDistance = offsetDistance;
	}
	public int getOffsetDistance(){
		return this.offsetDistance;
	}
}
