package com.fleety.base.shape.java.container;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.fleety.base.shape.ResultInfo;
import com.fleety.base.shape.java.CorrdUtil;
import com.fleety.base.shape.java.LineInfo;
import com.fleety.base.shape.java.Point;
import com.fleety.base.shape.java.PointInfo;
import com.fleety.base.shape.java.SegmentInfo;

public class LineContainer {
	private HashMap areaPointMapping = new HashMap();
	private HashMap segmentMapping = new HashMap();
	
	public LineContainer(){
		
	}
	
	public int size(){
		return this.segmentMapping.size();
	}
	public void addRoadSegment(LineInfo segment){
		List pList = segment.getPointList();
		if(pList == null || pList.size() < 2){
			return ;
		}
		this.segmentMapping.put(new Integer(segment.getId()) ,segment);
		long startLoAreaId,startLaAreaId,endLoAreaId,endLaAreaId;
		long areaId;
		double minLo,maxLo,minLa,maxLa;

		List tempList;
		SegmentInfo areaSegmentInfo;

		int index = 0;
		Point p,preP;

		Iterator itr = pList.iterator();
		preP = (Point)itr.next();
		for(;itr.hasNext();index++){
			p = (Point)itr.next();

			minLo = preP.getLo()>p.getLo()?p.getLo():preP.getLo();
			maxLo = preP.getLo()>p.getLo()?preP.getLo():p.getLo();
			minLa = preP.getLa()>p.getLa()?p.getLa():preP.getLa();
			maxLa = preP.getLa()>p.getLa()?preP.getLa():p.getLa();

			areaId = CorrdUtil.getAreaId(minLo,minLa);
			startLoAreaId = (areaId >> 32);
			startLaAreaId = (areaId & 0xFFFFFFFFl);
			areaId = CorrdUtil.getAreaId(maxLo,maxLa);
			endLoAreaId = (areaId >> 32);
			endLaAreaId = (areaId & 0xFFFFFFFFl);
			
			areaSegmentInfo = null;
			for(long i=startLoAreaId;i<=endLoAreaId;i++){
				for(long j=startLaAreaId;j<=endLaAreaId;j++){
					areaId = (i<<32)+j;

					if(CorrdUtil.isInter(areaId,preP,p)){
						if(areaSegmentInfo == null){
							areaSegmentInfo = new SegmentInfo(segment.getId(),index);
						}
						tempList = (List)this.areaPointMapping.get(new Long(areaId));
						if(tempList == null){
							tempList = new LinkedList();
							this.areaPointMapping.put(new Long(areaId) ,tempList);
						}
						tempList.add(areaSegmentInfo);
					}
				}
			}
			preP = p;
		}
	}
	
	public void removeRoadSegmentById(int segmentId){
		LineInfo segment = (LineInfo)this.segmentMapping.remove(new Integer(segmentId));
		if(segment == null){
			return;
		}
		List pList = segment.getPointList();

		long startLoAreaId,startLaAreaId,endLoAreaId,endLaAreaId;
		long areaId;
		double minLo,maxLo,minLa,maxLa;

		List tempList;
		SegmentInfo areaSegmentInfo = null;

		Point p,preP;
		
		Iterator itr = pList.iterator();
		preP = (Point)itr.next();
		for(;itr.hasNext();){
			p = (Point)itr.next();

			minLo = preP.getLo()>p.getLo()?p.getLo():preP.getLo();
			maxLo = preP.getLo()>p.getLo()?preP.getLo():p.getLo();
			minLa = preP.getLa()>p.getLa()?p.getLa():preP.getLa();
			maxLa = preP.getLa()>p.getLa()?preP.getLa():p.getLa();

			areaId = CorrdUtil.getAreaId(minLo,minLa);
			startLoAreaId = (areaId >> 32);
			startLaAreaId = (areaId & 0xFFFFFFFFl);
			areaId = CorrdUtil.getAreaId(maxLo,maxLa);
			endLoAreaId = (areaId >> 32);
			endLaAreaId = (areaId & 0xFFFFFFFFl);

			for(long i=startLoAreaId;i<=endLoAreaId;i++){
				for(long j=startLaAreaId;j<=endLaAreaId;j++){
					areaId = (i<<32)+j;
					if(CorrdUtil.isInter(areaId,preP,p)){
						tempList = (List)this.areaPointMapping.get(new Long(areaId));
						if(tempList != null){
							for(Iterator itr1 = tempList.iterator();itr1.hasNext();){
								areaSegmentInfo = (SegmentInfo)itr1.next();
								if(areaSegmentInfo.getSegmentId() == segmentId){
									itr1.remove();
								}
							}

							if(tempList.size() == 0){
								this.areaPointMapping.remove(new Long(areaId));
							}
						}
					}
				}
			}
			preP = p;
		}
	}
	
	public LineInfo getRoadSegment(int segmentId){
		return (LineInfo)this.segmentMapping.get(new Integer(segmentId));
	}

	public int[] getAllSegmentId(){
		Object[] arr = this.segmentMapping.keySet().toArray();
		int[] rArr = new int[arr.length];
		for(int i=0;i<arr.length;i++){
			rArr[i] = ((Integer)arr[i]).intValue();
		}
		return rArr;
	}

	//绑定当前经纬度度点到最近的道路上，返回道路ID.如果返回值小于0，意味着无绑定。相关信息会设定到PointInfo对象中
	public int bindSegment(PointInfo pointInfo,int reqSegmentId){
		return this.bindSegment(pointInfo, reqSegmentId, 30);
	}
	public int bindSegment(PointInfo pointInfo,int reqSegmentId,int _maxOffset){
		return this.bindSegment(pointInfo, reqSegmentId, _maxOffset, null);
	}
	public int bindSegment(PointInfo pointInfo,int reqSegmentId,int _maxOffset,ResultInfo resultInfo){
		double lo = pointInfo.getLo();
		double la = pointInfo.getLa();
		List tempList;

		SegmentInfo areaSegmentinfo;
		LineInfo segmentInfo;
		Point p1,p2,p3;
		long interPoint;
		long distance;
		long maxOffset = _maxOffset;
		maxOffset *= _maxOffset;
		int lo1,la1,lo2,la2,lo3,la3,lo4,la4,minLo,maxLo,minLa,maxLa;
		
		int bindedSegmentId = -1,bindedPointIndex = -1;
		long bindedLo = 0,bindedLa = 0,minDistance = 0;
		int segmentId,pointIndex,tempSegmentOrder,segmentOrder = 0;

		int angle = pointInfo.getDir();

		p3 = new Point(0,lo,la);
		lo3 = p3.getLom();
		la3 = p3.getLam();
		
		int range = ((_maxOffset-1)/CorrdUtil.UNIT+1)*2+1;
		if(range < 3){
			range = 3;
		}

		//对相邻的9个区域进行寻找
		long areaId = CorrdUtil.getAreaId(lo,la);
		long startCol,tempStartCol = (areaId>>32) - range/2;
		long startRow = (areaId&0xFFFFFFFFl)-range/2;
		HashMap mapping = new HashMap();
		for(int i=0;i<range;i++,startRow++){
			startCol = tempStartCol;
			for(int j=0;j<range;j++,startCol++){
				areaId = (startCol << 32) + startRow;
				tempList = (List)this.areaPointMapping.get(new Long(areaId));
				if(tempList != null){
					for(Iterator itr = tempList.iterator();itr.hasNext();){
						areaSegmentinfo = (SegmentInfo)itr.next();
						
						segmentId = areaSegmentinfo.getSegmentId();
						pointIndex = areaSegmentinfo.getPointIndex();
						
						if(reqSegmentId>0){//若是对特定线路查询，则只判断该线路
							if(segmentId != reqSegmentId){
								continue;
							}
						}
						segmentInfo = (LineInfo)this.segmentMapping.get(new Integer(segmentId));
						if(segmentInfo == null){
							continue;
						}
						
						String key = segmentId+"-"+pointIndex;
						if(mapping.containsKey(key)){
							continue;
						}
						mapping.put(key, null);
						p1 = segmentInfo.getPoint(pointIndex);
						p2 = segmentInfo.getPoint(pointIndex+1);

						minLo = p1.getLom() > p2.getLom() ? p2.getLom() : p1.getLom();
						maxLo = p1.getLom() > p2.getLom() ? p1.getLom() : p2.getLom();
						minLa = p1.getLam() > p2.getLam() ? p2.getLam() : p1.getLam();
						maxLa = p1.getLam() > p2.getLam() ? p1.getLam() : p2.getLam();

						interPoint = CorrdUtil.countInterPoint(p1,p2,p3);
						lo4 = (int)(interPoint>>32);
						la4 = (int)(interPoint&0xFFFFFFFFl);

						if(lo4 >= minLo-_maxOffset && lo4 <= maxLo+_maxOffset && la4 >= minLa - _maxOffset && la4 <= maxLa + _maxOffset){
							distance = CorrdUtil.distancePower(lo3,la3,lo4,la4);
							if(distance <= maxOffset){
								tempSegmentOrder = p1.isSameDir(angle);
								
								if(tempSegmentOrder == Point.NATURAL_ORDER_SAME_DIR 
									|| 
									(tempSegmentOrder == Point.REVERSE_ORDER_SAME_DIR 
									&& segmentInfo.getDir() == LineInfo.DOUBLE_ORDER)
									){
									if(bindedSegmentId < 0 || distance < minDistance){
										bindedSegmentId = segmentId;
										bindedPointIndex = pointIndex;
										bindedLo = lo4;
										bindedLa = la4;
										minDistance = distance;
										segmentOrder = tempSegmentOrder;
									}
								}
							}
						}
					}
				}
			}
		}

		if(resultInfo != null){
			resultInfo.setBindedSegmentId(bindedSegmentId);
			resultInfo.setBindedLo(CorrdUtil.m2lola(bindedLo,true));
			resultInfo.setBindedLa(CorrdUtil.m2lola(bindedLa,false));
			resultInfo.setBindedPointIndex(bindedPointIndex);
			resultInfo.setMinDistance(minDistance);
			resultInfo.setSegmentOrder(segmentOrder);
		}
		
		return bindedSegmentId;
	}
}
