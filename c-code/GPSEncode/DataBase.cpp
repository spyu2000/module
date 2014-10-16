// DataBase.cpp: implementation of the DataBase class.
//
//////////////////////////////////////////////////////////////////////

#include "stdafx.h"
#include "DataBase.h"
#include <iostream>
#include <utility>
using namespace std;

//////////////////////////////////////////////////////////////////////
// Construction/Destruction
//////////////////////////////////////////////////////////////////////
std::map<int,AREAOFFSET> DataBase::m_arrPoint;

DataBase::DataBase()
{

}

DataBase::~DataBase()
{

}

void DataBase::putData(int nAareaId, int nOffsetX, int nOffsetY)
{
	AREAOFFSET fp;
	fp.xOffset = nOffsetX;
	fp.yOffset = nOffsetY;
	m_arrPoint.insert(make_pair(nAareaId, fp));		
}