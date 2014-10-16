// Util.h: interface for the Util class.
//
//////////////////////////////////////////////////////////////////////

#if !defined(AFX_UTIL_H__E8F7161F_F45E_436E_A5AF_4AE4C954D0F6__INCLUDED_)
#define AFX_UTIL_H__E8F7161F_F45E_436E_A5AF_4AE4C954D0F6__INCLUDED_

#if _MSC_VER > 1000
#pragma once
#endif // _MSC_VER > 1000

#include "math.h"
#include "VersionInfo.h"

class Point;
const int POINT_FLAG=0;
const int DOUBLE_LINE_FLAG=1;
const int SINGLE_LINE_FLAG=2;
const int AREA_FLAG=3;
const int CIRCLE_FLAG=1;
const int RECTANGLE_FLAG=2;
const int POLYGON_FLAG=3;
class Util  
{
public:
	Util();
	virtual ~Util();
	
	static short getShort(char* data,bool isLittle = true);
	static int getInt(char* data,bool isLittle = true);
	static long8 getLongLong(char* data,bool isLittle = true);
	static float getFloat(char* data);

	static int put(char *data,int value);
	static int putInt(char *data,int value,bool isLittle = true);
	static int putShort(char *data,int value,bool isLittle = true);

	static int putFlagInt(char *data,short flag,int value,bool isLittle=true);
	static int putFlagShort(char *data,short flag,int value,bool isLittle=true);
	static int putFlagByte(char *data,short flag,int value,bool isLittle=true);
	static int putFlagByteArr(char *data,short flag,char* value,int len,bool isLittle=true);

	static bool isInter(long8 areaId,Point *p1,Point *p2,int unit=200);
	static long8 getAreaId(double lo,double la,int unit=200);
	static long lola2m(double lola,bool isLo);
	static double m2lola(long m,bool isLo);
	static long distance(long lo1,long la1,long lo2,long la2);
	static long8 distancePower(long lo1,long la1,long lo2,long la2);
	static long8 countInterPoint(Point *p1,Point *p2,Point *p3);

	static int arc2Angle(double arc);
	static double angle2Arc(int angle);

	//经纬度和km的比例.度/km
	static double LO_SCALE;
	static double LA_SCALE;
	static double PI;
};

#endif // !defined(AFX_UTIL_H__E8F7161F_F45E_436E_A5AF_4AE4C954D0F6__INCLUDED_)
