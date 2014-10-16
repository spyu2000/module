package com.fleety.base.shape.java.container;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.fleety.base.shape.java.CorrdUtil;
import com.fleety.base.shape.java.Point;
import com.fleety.base.shape.java.PointInfo;

public class PointContainer {
	private HashMap areaIdPointMapping = new HashMap();
	private HashMap pointIdAreaIdMapping = new HashMap();
	
	public PointContainer(){
		
	}
	
	public void addPoint(Point point){
		long areaId = CorrdUtil.getAreaId(point.getLo(),point.getLa());

		List pList ;

		pList = (List)this.areaIdPointMapping.get(new Long(areaId));
		if(pList == null){
			pList = new LinkedList();
			this.areaIdPointMapping.put(new Long(areaId) ,pList);
		}

		pList.add(point);
		this.pointIdAreaIdMapping.put(new Integer(point.getPid()) , new Long(areaId));
	}
	public void removePoint(int pid){
		Long areaId = (Long)this.pointIdAreaIdMapping.remove(new Integer(pid));
		if(areaId == null){
			return;
		}

		List pList;
		pList = (List)this.areaIdPointMapping.get(areaId);
		if(pList == null){
			return;
		}

		Point p;
		Iterator itr = pList.iterator();
		for (;itr.hasNext();) {
			p = (Point)itr.next();
			if(p.getPid()==pid){
				itr.remove();
				break;
			}
		}
		
		if(pList.size() == 0){
			this.areaIdPointMapping.remove(areaId);
		}
	}

	public int getNearestPoint(PointInfo pInfo,int limitDis){
		return this.getNearestPoint(pInfo, limitDis, CorrdUtil.UNIT);
	}
	public int getNearestPoint(PointInfo pInfo,int _limitDis,int unit){
		double lo = pInfo.getLo();
		double la = pInfo.getLa();
		Point p,p2;
		long distance,minDistance=Long.MAX_VALUE;
		int resultPointId=-1;
		p = new Point(0,lo,la);
		List tempList;
		int range = ((_limitDis-1)/unit+1)*2+1;
		
		long areaId = CorrdUtil.getAreaId(lo,la,unit);
		long startCol,tempStartCol = (areaId>>32) - range/2;
		long startRow = (areaId&0xFFFFFFFFl)-range/2;

		long limitDis = _limitDis*_limitDis;
		for(int i=0;i<range;i++,startRow++){
			startCol = tempStartCol;
			for(int j=0;j<range;j++,startCol++){
				areaId = (startCol << 32) + startRow;
				tempList = (List)this.areaIdPointMapping.get(new Long(areaId));
				if(tempList == null){
					continue;
				}

				for (Iterator itr = tempList.iterator();itr.hasNext();) {
					p2 = (Point)itr.next();
					distance = CorrdUtil.distancePower(p.getLom(),p.getLam(),p2.getLom(),p2.getLam());
					if(distance <= limitDis){
						if(resultPointId < 0 || distance < minDistance){
							minDistance = distance;
							resultPointId = p2.getPid();
						}
					}
				}
			}
		}

		return resultPointId;
	}
	
	public int size(){
		return this.pointIdAreaIdMapping.size();
	}
}
