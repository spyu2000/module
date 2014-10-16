// AreaInfo.h: interface for the AreaInfo class.
//
//////////////////////////////////////////////////////////////////////

#if !defined(AFX_AREAINFO_H__14C92184_F7E0_4CBE_A35B_D2F93DFA8091__INCLUDED_)
#define AFX_AREAINFO_H__14C92184_F7E0_4CBE_A35B_D2F93DFA8091__INCLUDED_

#if _MSC_VER > 1000
#pragma once
#endif // _MSC_VER > 1000
#include <iostream>
#include <vector>
#include "Point.h"

class AreaInfo  
{
public:
	AreaInfo(int pid);
	virtual ~AreaInfo();

	int getAreaId();
	void addPoint(Point* p);
	void setBounds(double minLo,double maxLo,double minLa,double maxLa);
	void setAreaType(int areaType);
	int getAreaType();
	double* getBounds();
	std::vector<Point*> *getPointList();
	bool isInBound(double x,double y);
	bool isInArea(double x,double y);
	void setCirclePara(long lom,long lam,long radio);
	bool isInCircle(double lo,double la);
private:
	int id;
	double bounds[4];
	std::vector<Point*> pList;
	int areaType;
	long radio;
	long lomlam[2];
};

#endif // !defined(AFX_AREAINFO_H__14C92184_F7E0_4CBE_A35B_D2F93DFA8091__INCLUDED_)
