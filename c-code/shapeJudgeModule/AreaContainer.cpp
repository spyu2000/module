// AreaContainer.cpp: implementation of the AreaContainer class.
//
//////////////////////////////////////////////////////////////////////

#include "stdafx.h"
#include "AreaContainer.h"

//////////////////////////////////////////////////////////////////////
// Construction/Destruction
//////////////////////////////////////////////////////////////////////
AreaContainer::AreaContainer()
{

}

AreaContainer::~AreaContainer()
{

}

int AreaContainer::size(){
	return this->areaMapping.size();
}

void AreaContainer::addArea(AreaInfo* area){
	this->areaMapping[area->getAreaId()] = area;
	this->areaList.push_back(area);
}
void AreaContainer::removeAreaById(int areaId){
	std::map<int,AreaInfo*>::iterator itr;
	itr = this->areaMapping.find(areaId);
	
	if(itr==this->areaMapping.end()){
		return;
	}
	AreaInfo* area = (*itr).second;
	this->areaMapping.erase(itr);

	AreaInfo* area2 ;
	std::list<AreaInfo*>::iterator iter;
	iter = this->areaList.begin();
	for (;iter != this->areaList.end();) {
		area2 = *iter;
		if(area2->getAreaId()==areaId){
			iter = this->areaList.erase(iter);
			break;
		}else{
			iter++;
		}
	}
	delete(area);
}
int AreaContainer::getNearestArea(int areaId,PointInfo* pInfo){
	double lo = pInfo->getLo();
	double la = pInfo->getLa();
	std::list<Point*> *pList;

	if(areaId>0){
		std::map<int,AreaInfo*>::iterator itr;
		itr = this->areaMapping.find(areaId);
		if(itr!=this->areaMapping.end()){
			AreaInfo* area = (*itr).second;
			if(area->getAreaType()==RECTANGLE_FLAG){
				if(area->isInBound(lo,la)){
					return areaId;
				}
			}else if(area->getAreaType()==POLYGON_FLAG){
				if(area->isInBound(lo,la)){
					if(area->isInArea(lo,la)){
						return areaId;
					}
				}
			}else if(area->getAreaType()==CIRCLE_FLAG){
				if(area->isInCircle(lo,la)){
					return areaId;
				}
			}
		}
	}else{
		AreaInfo* area ;
		std::list<AreaInfo*>::iterator iter;
		iter = this->areaList.begin();
		for (;iter != this->areaList.end();iter++) {
			area=*iter;
			areaId = area->getAreaId();
			if(area->getAreaType()==RECTANGLE_FLAG){
				if(area->isInBound(lo,la)){
					return areaId;
				}
			}else if(area->getAreaType()==POLYGON_FLAG){
				if(area->isInBound(lo,la)){
					if(area->isInArea(lo,la)){
						return areaId;
					}
				}
			}else if(area->getAreaType()==CIRCLE_FLAG){
				if(area->isInCircle(lo,la)){
					return areaId;
				}
			}
		}
	}
	return -1;
}
