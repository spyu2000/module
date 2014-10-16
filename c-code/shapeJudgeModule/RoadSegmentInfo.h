// RoadSegmentInfo.h: interface for the RoadSegmentInfo class.
//
//////////////////////////////////////////////////////////////////////

#if !defined(AFX_ROADSEGMENTINFO_H__8A237B9A_E665_45DE_B985_D790E199B115__INCLUDED_)
#define AFX_ROADSEGMENTINFO_H__8A237B9A_E665_45DE_B985_D790E199B115__INCLUDED_

#if _MSC_VER > 1000
#pragma once
#endif // _MSC_VER > 1000

#include <iostream>
#include <vector>
#include "Point.h"
#include "PointInfo.h"

class RoadSegmentInfo  
{
public:
	RoadSegmentInfo(int id,char* name);
	RoadSegmentInfo(int id,char* name,int adcode,int width,int dir);
	virtual ~RoadSegmentInfo();

	void addPoint(Point *p);
	std::vector<Point*> *getPointList();
	Point* getPoint(int index);
	int pointSize();
	int getId();
	char* getName();
	int getDir();
	int getAdcode();

	void setBounds(double minLo,double maxLo,double minLa,double maxLa);
	double* getBounds();

	void normalizeDir();
	void updatePointAssoInfo();

	long getDistanceToDestBreak(PointInfo* pInfo);

	static int NORMAL_ORDER;
	static int REVERSE_ORDER;
	static int DOUBLE_ORDER;
	static int NO_ORDER;
private:
	int id;
	int adcode;
	char* name;
	int width;
	int dir;
	long totalDistance;
	double bounds[4];

	std::vector<Point*> pList;
};

#endif // !defined(AFX_ROADSEGMENTINFO_H__8A237B9A_E665_45DE_B985_D790E199B115__INCLUDED_)
