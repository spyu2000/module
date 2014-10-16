package com.fleety.base.shape.java;

import java.awt.Shape;

public class AreaInfo {
	public static final int SCALE = 1000000;
	private int pid = 0;
	private Shape shape = null;
	
	public AreaInfo(int pid){
		this.pid = pid;
	}
	
	public int getAreaId(){
		return this.pid;
	}
	public void updateShape(Shape newShape){
		this.shape = newShape;
	}
	public boolean isInArea(double lo,double la){
		if(this.shape == null){
			return false;
		}
		return this.shape.contains(fromDoubleToInt(lo), fromDoubleToInt(la));
	}
	
	public static int fromDoubleToInt(double d){
		return (int)Math.round(d*SCALE);
	}
}
