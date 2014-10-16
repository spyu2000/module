// PointInfo.h: interface for the PointInfo class.
//
//////////////////////////////////////////////////////////////////////

#if !defined(AFX_POINTINFO_H__FCC30371_B442_4287_AAED_998261F11462__INCLUDED_)
#define AFX_POINTINFO_H__FCC30371_B442_4287_AAED_998261F11462__INCLUDED_

#if _MSC_VER > 1000
#pragma once
#endif // _MSC_VER > 1000

#include "time.h"

class PointInfo  
{
public:
	PointInfo(int id,double lo,double la,short speed,short dir,time_t time);
	virtual ~PointInfo();

	int getId();
	char* getDesc();
	double getLo();
	double getLa();
	short getSpeed();
	short getDir();
	time_t getTime();

	void setAssociateInfo(int segmentId,int pointIndex,long lom,long lam,long distance,int segmentOrder = 1);
	int getSegmentId();
	int getSegmentOrder();
	int getPointIndex();
	long getAssLom();
	long getAssLam();
	long getDistance();
private:
	int id;
	char* desc;
	double lo;
	double la;
	short speed;
	short dir;
	time_t time;

	int segmentId;
	int segmentOrder;
	int pointIndex;
	long assLom;
	long assLam;
	long distance;
};

#endif // !defined(AFX_POINTINFO_H__FCC30371_B442_4287_AAED_998261F11462__INCLUDED_)
