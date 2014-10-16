package com.fleety.base.shape.java;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class LineInfo {
	public final static int NORMAL_ORDER = 1;
	public final static int REVERSE_ORDER = 2;
	public final static int DOUBLE_ORDER = 0;
	public final static int NO_ORDER = 3;
	
	private int id;
	private String name;
	private int width;
	private int dir;
	private int totalDistance = 0;
	private double[] bounds = new double[4];
	private List pList = new ArrayList(16);
	
	public LineInfo(int id,String name,int width,int dir){
		this.id = id;
		this.name = name;
		this.width = width;
		this.dir = dir;
	}
	
	public void addPoint(Point p){
		this.pList.add(p);
	}
	public List getPointList(){
		return this.pList;
	}
	public Point getPoint(int index){
		return (Point)this.pList.get(index);
	}
	public int pointSize(){
		return this.pList.size();
	}
	public int getId(){
		return this.id;
	}
	public String getName(){
		return this.name;
	}
	public int getDir(){
		return this.dir;
	}

	public void setBounds(double minLo,double maxLo,double minLa,double maxLa){
		this.bounds[0] = minLo;
		this.bounds[1] = maxLo;
		this.bounds[2] = minLa;
		this.bounds[3] = maxLa;
	}
	public double[] getBounds(){
		return this.bounds;
	}

	public void normalizeDir(){
		if(this.dir == LineInfo.REVERSE_ORDER){
			ArrayList tList = new ArrayList(this.pList.size());
			for(int i=this.pList.size()-1;i>=0;i--){
				tList.add(this.pList.get(i));
			}
			this.pList = tList;
			this.dir = LineInfo.NORMAL_ORDER;
		}
	}
	public void updatePointAssoInfo(){
		Point preP,p;
		Iterator itr = this.pList.iterator();
		preP = (Point)itr.next();
		this.totalDistance = 0;
		while(itr.hasNext()){
			p = (Point)itr.next();
			preP.updateNextPointAssoInfo(p);
			this.totalDistance += preP.getFollowDistance();
			p.setDistanceFromHead(totalDistance);

			preP = p;
		}
	}

	public long getDistanceToDestBreak(PointInfo pInfo){
		Point p = (Point)this.pList.get(pInfo.getPointIndex());

		long pointDistance = CorrdUtil.distance(p.getLom(),p.getLam(),pInfo.getAssLom(),pInfo.getAssLam());
		if(pInfo.getSegmentOrder() == Point.NATURAL_ORDER_SAME_DIR){
			return this.totalDistance - p.getDistanceFromHead() - pointDistance;
		}else{
			return p.getDistanceFromHead() + pointDistance;
		}
	}
}
