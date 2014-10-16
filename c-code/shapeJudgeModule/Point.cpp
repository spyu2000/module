// Point.cpp: implementation of the Point class.
//
//////////////////////////////////////////////////////////////////////

#include "stdafx.h"
#include "Point.h"
#include "Util.h"

//////////////////////////////////////////////////////////////////////
// Construction/Destruction
//////////////////////////////////////////////////////////////////////

int Point::NOT_SAME_DIR = 0;
int Point::NATURAL_ORDER_SAME_DIR = 1;
int Point::REVERSE_ORDER_SAME_DIR = 2;

Point::Point(int pid,double lo,double la,bool isLast)
{
	this->pid = pid;
	this->lo = lo;
	this->la = la;
	this->isLast = isLast;

	this->lom = Util::lola2m(this->lo,true);
	this->lam = Util::lola2m(this->la,false);

	this->angle = 0;
	this->followDistance = 0;

	this->distanceFromHead = 0;
}

int Point::getPid(){
	return this->pid;
}

double Point::getLo(){
	return this->lo;
}

double Point::getLa(){
	return this->la;
}

long Point::getLom(){
	return this->lom;
}
long Point::getLam(){
	return this->lam;
}

void Point::setAngle(int angle){
	this->angle = angle;
}
int Point::getAngle(){
	return this->angle;
}
void Point::setFollowDistance(long followDistance){
	this->followDistance = followDistance;
}
long Point::getFollowDistance(){
	return this->followDistance;
}
void Point::setDistanceFromHead(long distanceFromHead){
	this->distanceFromHead = distanceFromHead;
}
long Point::getDistanceFromHead(){
	return this->distanceFromHead;
}
bool Point::isLastPoint(){
	return this->isLast;
}

void Point::updateNextPointAssoInfo(Point *nextPoint){
	this->isLast = false;
	if(this->lom == nextPoint->getLom()){
		this->angle = nextPoint->getLam() > this->lam ? 0 : 180;
	}else{
		double arc = atan((nextPoint->getLam()-this->lam)*1.0/(nextPoint->getLom()-this->lom));
		arc = Util::PI/2 - arc;
		//PI的精度不够，可能会出现负数。
		if(arc < 0){
			arc = 0;
		}
		if(nextPoint->getLom() < this->lom){
			arc += Util::PI;
		}
		this->angle = Util::arc2Angle(arc);
	}
	this->followDistance = Util::distance(nextPoint->getLom(),nextPoint->getLam(),this->lom,this->lam);
}

int Point::isSameDir(int angle,int offset){
	int realOffset = abs(this->angle - angle);
	if(realOffset > 180){
		realOffset = 360 - realOffset;
	}

	if(realOffset <= offset){
		return NATURAL_ORDER_SAME_DIR;
	}else if(180 - realOffset <= offset){
		return REVERSE_ORDER_SAME_DIR;
	}

	return NOT_SAME_DIR;
}

Point::~Point()
{

}
