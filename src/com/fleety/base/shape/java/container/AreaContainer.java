package com.fleety.base.shape.java.container;

import java.util.HashMap;
import java.util.Iterator;

import com.fleety.base.shape.java.AreaInfo;
import com.fleety.base.shape.java.PointInfo;

public class AreaContainer {
	private HashMap areaMapping = new HashMap();
	
	public AreaContainer(){
		
	}
	
	public void addArea(AreaInfo area){
		this.areaMapping.put(new Integer(area.getAreaId()),area);
	}
	public void removeAreaById(int areaId){
		this.areaMapping.remove(new Integer(areaId));
	}
	public int getNearestArea(int areaId,PointInfo pInfo){
		AreaInfo area = null;
		if(areaId > 0){
			area = (AreaInfo)this.areaMapping.get(new Integer(areaId));
			if(area!=null){
			if(area.isInArea(pInfo.getLo(), pInfo.getLa())){
				return areaId;
			}
			}
		}else{
			for(Iterator itr = this.areaMapping.values().iterator();itr.hasNext();){
				area = (AreaInfo)itr.next();
				if(area.isInArea(pInfo.getLo(), pInfo.getLa())){
					return area.getAreaId();
				}
			}
		}
		return -1;
	}
	public int size(){
		return this.areaMapping.size();
	}
}
