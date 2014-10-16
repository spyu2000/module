// PointInfo.cpp: implementation of the PointInfo class.
//
//////////////////////////////////////////////////////////////////////

#include "stdafx.h"
#include "PointInfo.h"

//////////////////////////////////////////////////////////////////////
// Construction/Destruction
//////////////////////////////////////////////////////////////////////

PointInfo::PointInfo(int id,double lo,double la,short speed,short dir,time_t time)
{
	this->id = id;
	this->lo = lo;
	this->la = la;
	this->speed = speed;
	this->dir = dir;
	this->time = time;

//	printf("mdtId=%d; time=%dl; lo=%f; la=%f; speed=%d; dir=%d; \n",id,time,lo,la,speed,dir);
}

PointInfo::~PointInfo()
{
	
}

int PointInfo::getId(){
	return this->id;
}
char* PointInfo::getDesc(){
	return this->desc;
}
double PointInfo::getLo(){
	return this->lo;
}
double PointInfo::getLa(){
	return this->la;
}
short PointInfo::getSpeed(){
	return this->speed;
}
short PointInfo::getDir(){
	return this->dir;
}
time_t PointInfo::getTime(){
	return this->time;
}

void PointInfo::setAssociateInfo(int segmentId,int pointIndex,long lom,long lam,long minDistance,int segmentOrder){
	this->segmentId = segmentId;
	this->pointIndex = pointIndex;
	this->assLom = lom;
	this->assLam = lam;
	this->distance = minDistance;
	this->segmentOrder = segmentOrder;
}
int PointInfo::getSegmentId(){
	return this->segmentId;
}
int PointInfo::getSegmentOrder(){
	return this->segmentOrder;
}
int PointInfo::getPointIndex(){
	return this->pointIndex;
}
long PointInfo::getAssLom(){
	return this->assLom;
}
long PointInfo::getAssLam(){
	return this->assLam;
}
long PointInfo::getDistance(){
	return this->distance;
}
