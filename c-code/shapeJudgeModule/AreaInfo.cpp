// AreaInfo.cpp: implementation of the AreaInfo class.
//
//////////////////////////////////////////////////////////////////////

#include "stdafx.h"
#include "AreaInfo.h"
#include "Util.h"

//////////////////////////////////////////////////////////////////////
// Construction/Destruction
//////////////////////////////////////////////////////////////////////

AreaInfo::AreaInfo(int areaId)
{
	this->id = areaId;
}

AreaInfo::~AreaInfo()
{
	std::vector<Point*>::iterator itr = this->pList.begin();
	for(;itr != this->pList.end();itr++){
		delete(*itr);
	}
	this->pList.clear();
}

int AreaInfo::getAreaId(){
	return this->id;
}
void AreaInfo::addPoint(Point* p){
	this->pList.push_back(p);
}
void AreaInfo::setBounds(double minLo,double maxLo,double minLa,double maxLa){
	this->bounds[0] = minLo;
	this->bounds[1] = maxLo;
	this->bounds[2] = minLa;
	this->bounds[3] = maxLa;
}
void AreaInfo::setAreaType(int areaType){
	this->areaType=areaType;
}
int AreaInfo::getAreaType(){
	return this->areaType;
}
double* AreaInfo::getBounds(){
	return this->bounds;
}
std::vector<Point*> *AreaInfo::getPointList(){
	return &this->pList;
}
bool AreaInfo::isInBound(double lo,double la){
	if(lo>this->bounds[1] || lo<this->bounds[0] || la<this->bounds[2] || la>this->bounds[3] ){//不在大的边框范围内，直接返回
		return false;
	}
	return true;
}
bool AreaInfo::isInArea(double x,double y){
	int hits = 0;
	std::vector<Point*>::reverse_iterator itr;
	itr = this->pList.rbegin();
	Point* p = *itr;
	double lastx = (p->getLo());
	double lasty = (p->getLa());
	double curx, cury;

	std::vector<Point*>::iterator iter;
	iter = this->pList.begin();
	for (;iter != this->pList.end(); lastx = curx, lasty = cury, iter++) {
		Point* point = *iter;
	    curx = point->getLo();
	    cury = point->getLa();

	    if (cury == lasty) {
			continue;
	    }

	    double leftx;
	    if (curx < lastx) {
			if (x >= lastx) {
				continue;
			}
			leftx = curx;
	    } else {
			if (x >= curx) {
				continue;
			}
			leftx = lastx;
	    }

	    double test1, test2;
	    if (cury < lasty) {
			if (y < cury || y >= lasty) {
				continue;
			}
			if (x < leftx) {
				hits++;
				continue;
			}
			test1 = x - curx;
			test2 = y - cury;
	    } else {
			if (y < lasty || y >= cury) {
				continue;
			}
			if (x < leftx) {
				hits++;
				continue;
			}
			test1 = x - lastx;
			test2 = y - lasty;
	    }

	    if (test1 < (test2 / (lasty - cury) * (lastx - curx))) {
			hits++;
	    }
	}
	return ((hits & 1) != 0);
}
void AreaInfo::setCirclePara(long lom,long lam,long radio){
	this->radio = radio;
	this->lomlam[0] = lom;
	this->lomlam[1] = lam;
}
bool AreaInfo::isInCircle(double lo,double la){
	long lom = Util::lola2m(lo,true);
	long lam = Util::lola2m(la,false);
	long radio = Util::distance(lom,lam,this->lomlam[0],this->lomlam[1]);
	return radio<=this->radio;
}
