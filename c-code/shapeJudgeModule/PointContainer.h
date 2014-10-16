// PointContainer.h: interface for the PointContainer class.
//
//////////////////////////////////////////////////////////////////////

#if !defined(AFX_POINTCONTAINER_H__F49659A1_BF27_4F7B_9C06_13B709C3B470__INCLUDED_)
#define AFX_POINTCONTAINER_H__F49659A1_BF27_4F7B_9C06_13B709C3B470__INCLUDED_

#if _MSC_VER > 1000
#pragma once
#endif // _MSC_VER > 1000

#include <iostream>
#include <map>
#include <list>

#include "Point.h"
#include "PointInfo.h"
#include "Util.h"

class PointContainer  
{
public:
	PointContainer();
	virtual ~PointContainer();

	void addPoint(Point* point);
	void removePoint(int pointId);
	int getNearestPoint(PointInfo* pinfo,int limitDis);
	int size();
private:
	std::map<long8,std::list<Point*>*> areaIdPointMapping;
	std::map<int,long8> pointIdAreaIdMapping;
};

#endif // !defined(AFX_POINTCONTAINER_H__F49659A1_BF27_4F7B_9C06_13B709C3B470__INCLUDED_)
