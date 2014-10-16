// AreaSegmentInfo.h: interface for the AreaSegmentInfo class.
//
//////////////////////////////////////////////////////////////////////

#if !defined(AFX_AREASEGMENTINFO_H__760FF71A_ACE5_48D3_ADA6_B4BB00FAE6D9__INCLUDED_)
#define AFX_AREASEGMENTINFO_H__760FF71A_ACE5_48D3_ADA6_B4BB00FAE6D9__INCLUDED_

#if _MSC_VER > 1000
#pragma once
#endif // _MSC_VER > 1000
#include <iostream>

class AreaSegmentInfo  
{
public:
	AreaSegmentInfo(int segmentId,int pointIndex);
	virtual ~AreaSegmentInfo();

	int getSegmentId();
	int getPointIndex();

	void increaseRefCount();
	void decreaseRefCount();
private:
	int segmentId;
	int pointIndex;
	short refCount;
};

#endif // !defined(AFX_AREASEGMENTINFO_H__760FF71A_ACE5_48D3_ADA6_B4BB00FAE6D9__INCLUDED_)
