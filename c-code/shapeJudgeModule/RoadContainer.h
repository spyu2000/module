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
	//�������е�·�Ρ�����ֵΪ·�ε����������С�ڵ���0����ʧ�ܡ�idָ��ָ���׸�id��ָ��λ�ã������ռ䡣
	int getAllSegmentId(int **idPointer);

	//�󶨵�ǰ��γ�ȶȵ㵽����ĵ�·�ϣ����ص�·ID.�������ֵС��0����ζ���ް󶨡������Ϣ���趨��PointInfo������
	int bindSegment(PointInfo *pointInfo,int reqSegmentId,int maxOffset = 30);
private:

	//long8 �ṹ���߶�4���ֽڴ����ȵ�areaId���Ͷ�4���ֽڴ���γ�ȵ�areaId
	std::map<long8,std::vector<AreaSegmentInfo*>*> areaPointMapping;
	std::map<int,RoadSegmentInfo*> segmentMapping;
};

#endif // !defined(AFX_ROADCONTAINER_H__280D9E01_4939_4585_9FA9_C22A02103B00__INCLUDED_)
