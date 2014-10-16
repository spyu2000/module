// RoadContainer.cpp: implementation of the RoadContainer class.
//
//////////////////////////////////////////////////////////////////////

#include "stdafx.h"
#include "RoadContainer.h"

//////////////////////////////////////////////////////////////////////
// Construction/Destruction
//////////////////////////////////////////////////////////////////////

RoadContainer::RoadContainer()
{

}

int RoadContainer::size(){
	return this->segmentMapping.size();
}

void RoadContainer::addRoadSegment(RoadSegmentInfo* segment){
	this->segmentMapping[segment->getId()] = segment;

	std::vector<Point*> *pList = segment->getPointList();
	std::vector<Point*>::iterator itr;
	itr = pList->begin();

	long startLoAreaId,startLaAreaId,endLoAreaId,endLaAreaId;
	long8 areaId;
	double minLo,maxLo,minLa,maxLa;

	std::map<long8,std::vector<AreaSegmentInfo*>*>::iterator listIter;
	std::vector<AreaSegmentInfo*> *tempList;
	AreaSegmentInfo *areaSegmentInfo;

	int index = 0;
	Point *p,*preP;
	preP = *itr;
	++itr;

	for(;itr != pList->end(); ++itr,index++){
		p = *itr;

		minLo = preP->getLo()>p->getLo()?p->getLo():preP->getLo();
		maxLo = preP->getLo()>p->getLo()?preP->getLo():p->getLo();
		minLa = preP->getLa()>p->getLa()?p->getLa():preP->getLa();
		maxLa = preP->getLa()>p->getLa()?preP->getLa():p->getLa();

		areaId = Util::getAreaId(minLo,minLa);
		startLoAreaId = (areaId >> 32);
		startLaAreaId = (areaId & 0xFFFFFFFF);
		areaId = Util::getAreaId(maxLo,maxLa);
		endLoAreaId = (areaId >> 32);
		endLaAreaId = (areaId & 0xFFFFFFFF);
		
		areaSegmentInfo = NULL;
		for(long i=startLoAreaId;i<=endLoAreaId;i++){
			for(long j=startLaAreaId;j<=endLaAreaId;j++){
				areaId = (((long8)i)<<32)+j;

				if(Util::isInter(areaId,preP,p)){
					if(areaSegmentInfo == NULL){
						areaSegmentInfo = new AreaSegmentInfo(segment->getId(),index);
					}
					listIter = this->areaPointMapping.find(areaId);
					if(listIter == this->areaPointMapping.end()){
						tempList = new std::vector<AreaSegmentInfo*>();
						this->areaPointMapping[areaId] = tempList;
					}else{
						tempList = (*listIter).second;
					}
					tempList->push_back(areaSegmentInfo);
					areaSegmentInfo->increaseRefCount();

					//printf("areaId=%lld id=%d index=%d lo1,la1,lo2,la2=%f,%f,%f,%f\n",areaId,segment->getId(),index,preP->getLo(),preP->getLa(),p->getLo(),p->getLa());
				}
			}
		}

		preP = p;
	}

/*
	double *minMax = segment->getBounds();
	double minLo,maxLo,minLa,maxLa;
	minLo = *minMax;
	maxLo = *(minMax+1);
	minLa = *(minMax+2);
	maxLa = *(minMax+3);

	printf("id=%d name=%s dir=%d bounds=%f,%f,%f,%f pSize=%d\n",segment->getId(),segment->getName(),segment->getDir(),minLo,maxLo,minLa,maxLa,segment->pointSize());
*/
}

void RoadContainer::removeRoadSegmentById(int segmentId){
	std::map<int,RoadSegmentInfo*>::iterator iter;
	iter = this->segmentMapping.find(segmentId);

	if(iter == this->segmentMapping.end()){
		return;
	}
	RoadSegmentInfo* segment = (*iter).second;
	this->segmentMapping.erase(iter);

	std::cout.flush();
	std::vector<Point*> *pList = segment->getPointList();
	std::vector<Point*>::iterator itr;
	std::vector<AreaSegmentInfo*>::iterator itert;
	std::map<long8,std::vector<AreaSegmentInfo*>*>::iterator listIter;

	long startLoAreaId,startLaAreaId,endLoAreaId,endLaAreaId;
	long8 areaId;
	double minLo,maxLo,minLa,maxLa;

	std::vector<AreaSegmentInfo*> *tempList;
	AreaSegmentInfo *areaSegmentInfo = NULL;

	Point *p,*preP;
	
	itr = pList->begin();
	preP = *itr;
	++itr;
	for(;itr != pList->end(); ++itr){
		p = *itr;

		minLo = preP->getLo()>p->getLo()?p->getLo():preP->getLo();
		maxLo = preP->getLo()>p->getLo()?preP->getLo():p->getLo();
		minLa = preP->getLa()>p->getLa()?p->getLa():preP->getLa();
		maxLa = preP->getLa()>p->getLa()?preP->getLa():p->getLa();

		areaId = Util::getAreaId(minLo,minLa);
		startLoAreaId = (areaId >> 32);
		startLaAreaId = (areaId & 0xFFFFFFFF);
		areaId = Util::getAreaId(maxLo,maxLa);
		endLoAreaId = (areaId >> 32);
		endLaAreaId = (areaId & 0xFFFFFFFF);

		for(long i=startLoAreaId;i<=endLoAreaId;i++){
			for(long j=startLaAreaId;j<=endLaAreaId;j++){
				areaId = (((long8)i)<<32)+j;
				if(Util::isInter(areaId,preP,p)){
					listIter = this->areaPointMapping.find(areaId);
					if(listIter != this->areaPointMapping.end()){
						tempList = (*listIter).second;
						itert = tempList->begin();
						for(;itert != tempList->end();){
							areaSegmentInfo = *itert;
							if(areaSegmentInfo->getSegmentId()==segmentId){
								areaSegmentInfo->decreaseRefCount();
								itert = tempList->erase(itert);
							}else{
								itert++;
							}
						}

						if(tempList->size() == 0){
							this->areaPointMapping.erase(listIter);
							delete(tempList);
						}
					}
				}
			}
		}
		preP = p;
	}
	delete(segment);

	//printf("segment size===%d\n",this->segmentMapping.size());
}

RoadSegmentInfo* RoadContainer::getRoadSegment(int segmentId){
	std::map<int,RoadSegmentInfo*>::iterator iter;
	iter = this->segmentMapping.find(segmentId);

	if(iter == this->segmentMapping.end()){
		return NULL;
	}else{
		return (*iter).second;
	}
}

int RoadContainer::getAllSegmentId(int **idPointer){
	int num = this->segmentMapping.size();

	if(num > 0){
		int *buff = (int*)malloc(4 * num);
		*idPointer = buff;

		std::map<int,RoadSegmentInfo*>::iterator itr = this->segmentMapping.begin();
		for(int index =0;itr != this->segmentMapping.end();itr++,index++){
			*(buff+index) = (*itr).first;
		}
	}

	return num;
}

int RoadContainer::bindSegment(PointInfo *pointInfo,int reqSegmentId,int maxOffset){
	double lo = pointInfo->getLo();
	double la = pointInfo->getLa();
	std::vector<AreaSegmentInfo*> *tempList;
	std::vector<AreaSegmentInfo*>::iterator itr;
	std::map<int,RoadSegmentInfo*>::iterator iter;

	AreaSegmentInfo *areaSegmentinfo;
	RoadSegmentInfo *segmentInfo;
	Point *p1,*p2,*p3;
	
	long8 interPoint;
	long distance;
	long lo1,la1,lo2,la2,lo3,la3,lo4,la4,minLo,maxLo;
	

	int bindedSegmentId = -1,bindedPointIndex = -1;
	long bindedLo,bindedLa,minDistance;
	int segmentId,pointIndex,tempSegmentOrder,segmentOrder;

	int angle = pointInfo->getDir();

	p3 = new Point(0,lo,la);
	lo3 = p3->getLom();
	la3 = p3->getLam();

	std::map<long8,std::vector<AreaSegmentInfo*>*>::iterator listIter;
	//对相邻的9个区域进行寻找
	long8 areaId = Util::getAreaId(lo,la);
	long8 startCol,tempStartCol = (areaId>>32) - 1;
	long8 startRow = (areaId&0xFFFFFFFFl)-1;
	for(int i=0;i<3;i++,startRow++){
		startCol = tempStartCol;
		for(int j=0;j<3;j++,startCol++){
			areaId = (startCol << 32) + startRow;
			listIter = this->areaPointMapping.find(areaId);
			if(listIter != this->areaPointMapping.end()){
				tempList = (*listIter).second;

				itr = tempList->begin();
				for(;itr != tempList->end();itr++){
					areaSegmentinfo = *itr;
					
					segmentId = areaSegmentinfo->getSegmentId();
					pointIndex = areaSegmentinfo->getPointIndex();
					
					if(reqSegmentId>0){//若是对特定线路查询，则只判断该线路
						if(segmentId!=reqSegmentId){
							continue;
						}
					}

					iter = this->segmentMapping.find(segmentId);
					if(iter == this->segmentMapping.end()){
						continue;
					}
					segmentInfo = (*iter).second;
					
					p1 = segmentInfo->getPoint(pointIndex);
					p2 = segmentInfo->getPoint(pointIndex+1);

					minLo = p1->getLom() > p2->getLom() ? p2->getLom() : p1->getLom();
					maxLo = p1->getLom() > p2->getLom() ? p1->getLom() : p2->getLom();

					interPoint = Util::countInterPoint(p1,p2,p3);
					lo4 = (long)(interPoint>>32);
					la4 = (long)(interPoint&0xFFFFFFFFl);

					if(lo4 >= minLo-maxOffset && lo4 <= maxLo+maxOffset){
						distance = Util::distance(lo3,la3,lo4,la4);
						if(distance <= maxOffset){
							tempSegmentOrder = p1->isSameDir(angle);
							
							if(tempSegmentOrder == Point::NATURAL_ORDER_SAME_DIR 
								|| 
								(tempSegmentOrder == Point::REVERSE_ORDER_SAME_DIR 
								&& segmentInfo->getDir() == RoadSegmentInfo::DOUBLE_ORDER)
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
	delete(p3);

	return bindedSegmentId;
}

RoadContainer::~RoadContainer()
{
	if(this->areaPointMapping.size() > 0 || this->segmentMapping.size() > 0){
		printf("RoadContainer:%d,%d,%d\n",&this->areaPointMapping,this->areaPointMapping.size(),this->segmentMapping.size());
	}
	this->areaPointMapping.clear();
	this->segmentMapping.clear();
}
