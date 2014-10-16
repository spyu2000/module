// DataBase.h: interface for the DataBase class.
//
//////////////////////////////////////////////////////////////////////

#if !defined(AFX_DATABASE_H__8BF26681_0E43_4C11_8341_EA6DA5F29D98__INCLUDED_)
#define AFX_DATABASE_H__8BF26681_0E43_4C11_8341_EA6DA5F29D98__INCLUDED_

#if _MSC_VER > 1000
#pragma once
#endif // _MSC_VER > 1000

#include <map>

typedef struct tagAREAOFFSET{
	int xOffset;
	int yOffset;
}AREAOFFSET,*PAREAOFFSET;

class DataBase  
{
public:
	DataBase();
	virtual ~DataBase();
	void Init(){};
	void putData(int nAareaId, int nOffsetX, int nOffsetY);
	static std::map<int,AREAOFFSET> m_arrPoint;
};

#endif // !defined(AFX_DATABASE_H__8BF26681_0E43_4C11_8341_EA6DA5F29D98__INCLUDED_)
