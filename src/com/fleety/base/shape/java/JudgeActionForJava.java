package com.fleety.base.shape.java;

import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.geom.Ellipse2D;

import com.fleety.base.shape.IAction;
import com.fleety.base.shape.JudgeServer;
import com.fleety.base.shape.ResultInfo;
import com.fleety.base.shape.java.container.AreaContainer;
import com.fleety.base.shape.java.container.LineContainer;
import com.fleety.base.shape.java.container.PointContainer;

public class JudgeActionForJava implements IAction{
	private PointContainer pContainer = new PointContainer();
	private LineContainer lContainer = new LineContainer();
	private AreaContainer aContainer = new AreaContainer();
	
	public void addShape(int instanceId,int pid,double[] lo,double[] la,int shapeType,int areaType){
		int length = lo.length;
		switch (shapeType){
		case JudgeServer.POINT_FLAG:
			{
				if(length < 1){
					return ;
				}
				Point p = new Point(pid,lo[0],la[0]);
				pContainer.removePoint(pid);
				pContainer.addPoint(p);
				break;	   
			}
		case JudgeServer.DOUBLE_LINE_FLAG:
		case JudgeServer.SINGLE_LINE_FLAG:
			{
				if(length < 2){
					return;
				}
				LineInfo segment;
				lContainer.removeRoadSegmentById(pid);

				Point p;
				if(shapeType==JudgeServer.SINGLE_LINE_FLAG){
					segment = new LineInfo(pid,null,1,LineInfo.NORMAL_ORDER);
				}else{
					segment = new LineInfo(pid,null,1,LineInfo.DOUBLE_ORDER);
				}
				
				for(int i=0;i<length;i++){
					p = new Point(0,lo[i],la[i]);
					segment.addPoint(p);
				}
				segment.updatePointAssoInfo();
	            lContainer.addRoadSegment(segment);
				break;
			}
		case JudgeServer.AREA_FLAG:
			{
				if(length<2){
					return;
				}
				AreaInfo area = new AreaInfo(pid);
				aContainer.removeAreaById(pid);

				if(areaType == IAction.POLYGON_FLAG){
					if(length<3){
						return;
					}
					Polygon poly = new Polygon();
					for(int i=0;i<length;i++){
						poly.addPoint(AreaInfo.fromDoubleToInt(lo[i]), AreaInfo.fromDoubleToInt(la[i]));
					}
					area.updateShape(poly);
				}else if(areaType == IAction.RECTANGLE_FLAG){
					double minLo=Double.MAX_VALUE,maxLo=Double.MIN_VALUE,minLa = Double.MAX_VALUE,maxLa = Double.MIN_VALUE;
					for(int i=0;i<4&&i<lo.length;i++){
						minLo = Math.min(lo[i], minLo);
						minLa = Math.min(la[i], minLa);
						maxLo = Math.max(lo[i], maxLo);
						maxLa = Math.max(la[i], maxLa);
					}
					Rectangle rect = new Rectangle(AreaInfo.fromDoubleToInt(minLo),AreaInfo.fromDoubleToInt(minLa),AreaInfo.fromDoubleToInt(maxLo-minLo),AreaInfo.fromDoubleToInt(maxLa-minLa));
					area.updateShape(rect);
				}else if(areaType==IAction.CIRCLE_FLAG){
					double minLo=Double.MAX_VALUE,maxLo=Double.MIN_VALUE,minLa = Double.MAX_VALUE,maxLa = Double.MIN_VALUE;
					for(int i=0;i<4&&i<lo.length;i++){
						minLo = Math.min(lo[i], minLo);
						minLa = Math.min(la[i], minLa);
						maxLo = Math.max(lo[i], maxLo);
						maxLa = Math.max(la[i], maxLa);
					}
					System.out.println(minLo+" "+minLa+" "+maxLo+" "+maxLa);
					Ellipse2D ellipse = new Ellipse2D.Double(AreaInfo.fromDoubleToInt(minLo),AreaInfo.fromDoubleToInt(minLa),AreaInfo.fromDoubleToInt(maxLo-minLo),AreaInfo.fromDoubleToInt(maxLa-minLa));
					area.updateShape(ellipse);
				}

				aContainer.addArea(area);
				break;
			}
		}
	}
	
	public void removeShape(int instanceId,int pid,int shapeType){
		switch (shapeType){
		case JudgeServer.POINT_FLAG:
			{
				pContainer.removePoint(pid);
				break;	   
			}
		case JudgeServer.DOUBLE_LINE_FLAG:
		case JudgeServer.SINGLE_LINE_FLAG:
			{
				lContainer.removeRoadSegmentById(pid);
				break;
			}
		case JudgeServer.AREA_FLAG:
			{
				aContainer.removeAreaById(pid);
				break;
			}
		}	
	}
	
	public int getArea(int instanceId,int areaId,double lo,double la){
		PointInfo pInfo = new PointInfo(0,lo,la,(short)0);
		return this.aContainer.getNearestArea(areaId, pInfo);
	}
	
	public int getNearestPoint(int instanceId,double lo,double la,int limitDis){
		PointInfo pInfo = new PointInfo(0,lo,la,(short)0);
		return pContainer.getNearestPoint(pInfo,limitDis);
	}
	
	public int getNearestLine(int instanceId,int lineId,double lo,double la,int limitDis,int dir){
		PointInfo pInfo = new PointInfo(0,lo,la,(short)dir);
		
		return lContainer.bindSegment(pInfo,lineId,limitDis);
	}
	public int getNearestLine(int instanceId,int lineId,double lo,double la,int limitDis,int dir,ResultInfo resultInfo){
		PointInfo pInfo = new PointInfo(0,lo,la,(short)dir);
		
		return lContainer.bindSegment(pInfo,lineId,limitDis,resultInfo);
	}
}