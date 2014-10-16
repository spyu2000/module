// AreaSegmentInfo.cpp: implementation of the AreaSegmentInfo class.
//
//////////////////////////////////////////////////////////////////////

#include "stdafx.h"
#include "AreaSegmentInfo.h"

//////////////////////////////////////////////////////////////////////
// Construction/Destruction
//////////////////////////////////////////////////////////////////////

AreaSegmentInfo::AreaSegmentInfo(int segmentId,int pointIndex)
{
	this->segmentId = segmentId;
	this->pointIndex = pointIndex;
	this->refCount = 0;
}

AreaSegmentInfo::~AreaSegmentInfo()
{

}
void AreaSegmentInfo::increaseRefCount(){
	this->refCount ++;
}
void AreaSegmentInfo::decreaseRefCount(){
	this->refCount--;
	if(this->refCount <= 0){
		delete(this);
	}
}

int AreaSegmentInfo::getSegmentId(){
	return this->segmentId;
}
int AreaSegmentInfo::getPointIndex(){
	return this->pointIndex;
}
