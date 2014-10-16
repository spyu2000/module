// RoadSegmentInfo.cpp: implementation of the RoadSegmentInfo class.
//
//////////////////////////////////////////////////////////////////////

#include "stdafx.h"
#include "RoadSegmentInfo.h"
#include   <algorithm> 

//////////////////////////////////////////////////////////////////////
// Construction/Destruction
//////////////////////////////////////////////////////////////////////

//物理方向 反物理方向 双向 不可通行
int RoadSegmentInfo::NORMAL_ORDER = 1;
int RoadSegmentInfo::REVERSE_ORDER = 2;
int RoadSegmentInfo::DOUBLE_ORDER = 0;
int RoadSegmentInfo::NO_ORDER = 3;

RoadSegmentInfo::RoadSegmentInfo(int id,char* name){
	this->id = id;
	this->name = name;
	this->totalDistance = 0;
}
RoadSegmentInfo::RoadSegmentInfo(int id,char* name,int adcode,int width,int dir){
	this->id = id;
	this->name = name;
	this->adcode = adcode;
	this->width = width;
	this->dir = dir;
	this->totalDistance = 0;
}

RoadSegmentInfo::~RoadSegmentInfo()
{
	if(this->name != NULL){
		delete this->name;
	}

	std::vector<Point*>::iterator itr = this->pList.begin();
	for(;itr != this->pList.end();itr++){
		delete(*itr);
	}
	this->pList.clear();
}

void RoadSegmentInfo::addPoint(Point *p){
	this->pList.push_back(p);
}
std::vector<Point*> *RoadSegmentInfo::getPointList(){
	return &this->pList;
}
Point* RoadSegmentInfo::getPoint(int index){
	return this->pList.at(index);
}
int RoadSegmentInfo::pointSize(){
	return this->pList.size();
}

void RoadSegmentInfo::setBounds(double minLo,double maxLo,double minLa,double maxLa){
	this->bounds[0] = minLo;
	this->bounds[1] = maxLo;
	this->bounds[2] = minLa;
	this->bounds[3] = maxLa;
}
double* RoadSegmentInfo::getBounds(){
	return this->bounds;
}

int RoadSegmentInfo::getId(){
	return this->id;
}
char* RoadSegmentInfo::getName(){
	return this->name;
}
int RoadSegmentInfo::getDir(){
	return this->dir;
}
int RoadSegmentInfo::getAdcode(){
	return this->adcode;
}
void RoadSegmentInfo::normalizeDir(){
	if(this->dir == RoadSegmentInfo::REVERSE_ORDER){
		this->dir = RoadSegmentInfo::NORMAL_ORDER;
		std::reverse(this->pList.begin(),this->pList.end());
	}
}
void RoadSegmentInfo::updatePointAssoInfo(){
	Point *preP,*p;
	std::vector<Point*>::iterator itr;
	itr = this->pList.begin();
	preP = *itr;
	itr ++;
	this->totalDistance = 0;
	while(itr != this->pList.end()){
		p = *itr;
		preP->updateNextPointAssoInfo(p);
		this->totalDistance += preP->getFollowDistance();
		p->setDistanceFromHead(totalDistance);

		preP = p;
		itr ++;
	}
}

long RoadSegmentInfo::getDistanceToDestBreak(PointInfo *pInfo){
	Point *p = this->pList.at(pInfo->getPointIndex());

	long pointDistance = Util::distance(p->getLom(),p->getLam(),pInfo->getAssLom(),pInfo->getAssLam());
	if(pInfo->getSegmentOrder() == Point::NATURAL_ORDER_SAME_DIR){
		return this->totalDistance - p->getDistanceFromHead() - pointDistance;
	}else{
		return p->getDistanceFromHead() + pointDistance;
	}
}
