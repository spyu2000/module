// DataLib.cpp: implementation of the DataLib class.
//
//////////////////////////////////////////////////////////////////////

#include "stdafx.h"
#include "DataLib.h"
#include <iostream>
#include <utility>
#include "DataChild1.h"
#include "DataChild2.h"
#include "DataChild3.h"
#include "DataChild4.h"
#include "DataChild5.h"
#include "DataChild6.h"
#include "DataChild7.h"
#include "DataChild8.h"
#include "DataChild9.h"
#include "DataChild10.h"

using namespace std;

DataLib aa;

#ifdef _LINUX
bool  encode(double loIn, double laIn, double& loOut, double& laOut)
#else
bool __stdcall encode(double loIn, double laIn, double& loOut, double& laOut)
#endif
{	
	bool bResult = false;
    int key = ((((int)(((loIn+180)*10)+0.5))<<16)|((int)(((laIn+90)*10)+0.5)));
    AREAOFFSET  areaOff;
	bResult = aa.GetData(key, areaOff);

	if(bResult)
	{
		loOut = loIn + areaOff.xOffset/1000000.0;
		laOut = laIn + areaOff.yOffset/1000000.0;
	}

	return bResult;
}


#ifdef _LINUX
bool decode(double loIn, double laIn, double& loOut, double& laOut)
#else
bool __stdcall decode(double loIn, double laIn, double& loOut, double& laOut)
#endif
{
	bool bResult = false;

    int key = ((((int)(((loIn+180)*10)+0.5))<<16)|((int)(((laIn+90)*10)+0.5)));
    AREAOFFSET  areaOff;
	bResult = aa.GetData(key, areaOff);    

	if(bResult)
	{
		loOut = loIn - areaOff.xOffset/1000000.0;
		laOut = laIn - areaOff.yOffset/1000000.0;
	}
	return bResult;
}

//////////////////////////////////////////////////////////////////////
// Construction/Destruction
//////////////////////////////////////////////////////////////////////
DataLib::DataLib()
{
	InitData();
}

DataLib::~DataLib()
{

}


void DataLib::OutPut()
{
	std::map<int, AREAOFFSET>::iterator pIter;
	cout<<"size:==="<<DataBase::m_arrPoint.size()<<endl;
/*	for(pIter=DataBase::m_arrPoint.begin(); pIter!= DataBase::m_arrPoint.end(); pIter++)
	{
		cout<<"===xOffset::"<<((AREAOFFSET)pIter->second).xOffset <<"  yOffset::"<<((AREAOFFSET)pIter->second).yOffset<<endl;
	}*/
}

bool DataLib::GetData(int nKey, AREAOFFSET& offset)
{
	std::map<int, AREAOFFSET>::iterator pIter;
	if(DataBase::m_arrPoint.size()<1)
	{
		return false;
	}

	pIter = DataBase::m_arrPoint.find(nKey);
	if(pIter==DataBase::m_arrPoint.end())
	{
		return false;
	}
	offset = (AREAOFFSET)pIter->second;
	return true;
}

void DataLib::InitData()
{
	DataChild1 d1;
    DataChild2 d2;
	DataChild3 d3;
    DataChild4 d4;
	DataChild5 d5;
    DataChild6 d6;
	DataChild7 d7;
    DataChild8 d8;
	DataChild9 d9;
    DataChild10 d10;
	DataBase::m_arrPoint.clear();
	d1.Init();
	d2.Init();
	d3.Init();
	d4.Init();
	d5.Init();
	d6.Init();
	d7.Init();
	d8.Init();
	d9.Init();
	d10.Init();
}

