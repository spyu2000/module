// PointContainer.cpp: implementation of the PointContainer class.
//
//////////////////////////////////////////////////////////////////////

#include "stdafx.h"
#include "PointContainer.h"

//////////////////////////////////////////////////////////////////////
// Construction/Destruction
//////////////////////////////////////////////////////////////////////

PointContainer::PointContainer()
{

}

PointContainer::~PointContainer()
{

}

int PointContainer::size(){
	return this->pointIdAreaIdMapping.size();
}

void PointContainer::addPoint(Point* point){
	long8 areaId = Util::getAreaId(point->getLo(),point->getLa());

	std::list<Point*> *pList ;

	std::map<long8,std::list<Point*>*>::iterator iter;
	iter = this->areaIdPointMapping.find(areaId);
	if(iter==this->areaIdPointMapping.end()){
		pList = new std::list<Point*>();
		this->areaIdPointMapping[areaId] = pList;
	}else{
		pList = (*iter).second;
	}

	pList->push_back(point);
	this->pointIdAreaIdMapping[point->getPid()] = areaId;
}
void PointContainer::removePoint(int pid){
	std::map<int,long8>::iterator iter;
	iter = this->pointIdAreaIdMapping.find(pid);
	if(iter == this->pointIdAreaIdMapping.end()){
		return;
	}
	long8 areaId = (*iter).second;
	this->pointIdAreaIdMapping.erase(iter);


	std::list<Point*> *pList;
	std::map<long8,std::list<Point*>*>::iterator litr;
	litr = this->areaIdPointMapping.find(areaId);
	if(litr==this->areaIdPointMapping.end()){
		return;
	}
	pList = (*litr).second;

	Point* p;
	std::list<Point*>::iterator itr;
	itr = pList->begin();
	for (;itr != pList->end();itr++) {
		p = *itr;
		if(p->getPid()==pid){
			itr = pList->erase(itr);
			delete p;
			break;
		}
	}
	if(pList->size() == 0){
		this->areaIdPointMapping.erase(litr);
		delete pList;
	}
}
int PointContainer::getNearestPoint(PointInfo* pInfo,int _limitDis){
	double lo = pInfo->getLo();
	double la = pInfo->getLa();
	Point *p,*p2;
	long8 distance,minDistance;
	int resultPointId=-1;
	p = new Point(0,lo,la);
	std::list<Point*> *tempList;
	//每两百米一个区域
	int unit = 200;
	int range = ((_limitDis-1)/unit+1)*2+1;
	
	long8 areaId = Util::getAreaId(lo,la);
	long8 startCol,tempStartCol = (areaId>>32) - range/2;
	long8 startRow = (areaId&0xFFFFFFFFl)-range/2;

	long8 limitDis = _limitDis*_limitDis;
	std::map<long8,std::list<Point*>*>::iterator litr;
	for(int i=0;i<range;i++,startRow++){
		startCol = tempStartCol;
		for(int j=0;j<range;j++,startCol++){
			areaId = (startCol << 32) + startRow;
			litr = this->areaIdPointMapping.find(areaId);
			if(litr==this->areaIdPointMapping.end()){
				continue;
			}
			tempList = (*litr).second;

			std::list<Point*>::iterator itert;
			itert = tempList->begin();
			for (;itert!=tempList->end();itert++) {
				p2 = *itert;
				//printf("lo2=%f,la2=%f,startCol=%lld,startRow=%lld,areaId=%lld\n",p2->getLo(),p2->getLa(),startCol,startRow,areaId);
				//std::cout.flush();
				distance = Util::distancePower(p->getLom(),p->getLam(),p2->getLom(),p2->getLam());
				//printf("distance=%ld\n",distance);
				//std::cout.flush();
				if(distance<=limitDis){
					if(resultPointId < 0 || distance < minDistance){
						minDistance = distance;
						resultPointId = p2->getPid();
					}
				}
			}
		}
	}
	delete(p);
	return resultPointId;
}
