// RoadContainer.h: interface for the RoadContainer class.
//
//////////////////////////////////////////////////////////////////////

#if !defined(AFX_ROADCONTAINER_H__280D9E01_4939_4585_9FA9_C22A02103B00__INCLUDED_)
#define AFX_ROADCONTAINER_H__280D9E01_4939_4585_9FA9_C22A02103B00__INCLUDED_

#if _MSC_VER > 1000
#pragma once
#endif // _MSC_VER > 1000

#include <iostream>
#include <map>
#include <list>
#include <vector>

#include "RoadSegmentInfo.h"
#include "AreaSegmentInfo.h"
#include "Point.h"
#include "PointInfo.h"
#include "Util.h"

class RoadContainer  
{
public:
	RoadContainer();
	virtual ~RoadContainer();

	int size();
	void addRoadSegment(RoadSegmentInfo* segment);
	void removeRoadSegmentById(int segmentId);
	RoadSegmentInfo* getRoadSegment(int segmentId);
	//返回所有的路段。返回值为路段的数量，如果小于等于0代表失败。id指针指向首个id的指针位置，连续空间。
	int getAllSegmentId(int **idPointer);

	//绑定当前经纬度度点到最近的道路上，返回道路ID.如果返回值小于0，意味着无绑定。相关信息会设定到PointInfo对象中
	int bindSegment(PointInfo *pointInfo,int reqSegmentId,int maxOffset = 30);
private:

	//long8 结构：高端4个字节代表经度的areaId，低端4个字节代表纬度的areaId
	std::map<long8,std::vector<AreaSegmentInfo*>*> areaPointMapping;
	std::map<int,RoadSegmentInfo*> segmentMapping;
};

#endif // !defined(AFX_ROADCONTAINER_H__280D9E01_4939_4585_9FA9_C22A02103B00__INCLUDED_)
