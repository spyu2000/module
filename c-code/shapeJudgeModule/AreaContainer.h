// AreaContainer.h: interface for the AreaContainer class.
//
//////////////////////////////////////////////////////////////////////

#if !defined(AFX_AREACONTAINER_H__47743B3D_6BED_485A_9CBA_E1B8459006DB__INCLUDED_)
#define AFX_AREACONTAINER_H__47743B3D_6BED_485A_9CBA_E1B8459006DB__INCLUDED_

#if _MSC_VER > 1000
#pragma once
#endif // _MSC_VER > 1000

#include <iostream>
#include <map>
#include <list>
#include "AreaInfo.h"
#include "PointInfo.h"
#include "Point.h"
#include "Util.h"

class AreaContainer  
{
public:
	AreaContainer();
	virtual ~AreaContainer();

	void addArea(AreaInfo* area);
	void removeAreaById(int areaId);
	int getNearestArea(int areaId,PointInfo* pInfo);
	int size();
private:
	std::map<int,AreaInfo*> areaMapping;
	std::list<AreaInfo*> areaList;
};

#endif // !defined(AFX_AREACONTAINER_H__47743B3D_6BED_485A_9CBA_E1B8459006DB__INCLUDED_)
