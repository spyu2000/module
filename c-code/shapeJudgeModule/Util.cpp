// Util.cpp: implementation of the Util class.
//
//////////////////////////////////////////////////////////////////////

#include "stdafx.h"
#include "Util.h"
#include "Point.h"
#include <iostream>

//////////////////////////////////////////////////////////////////////
// Construction/Destruction
//////////////////////////////////////////////////////////////////////

double Util::PI = 3.1415926;
double Util::LO_SCALE = 0.010506;
double Util::LA_SCALE = 0.009;

Util::Util()
{

}

int Util::put(char *data,int value){
	*data = value&0xFF;
	return 1;
}

int Util::putInt(char *data,int value,bool isLittle){
	if(isLittle){
		*data = value&0xFF;
		*(data+1) = (value>>8)&0xFF;
		*(data+2) = (value>>16)&0xFF;
		*(data+3) = (value>>24)&0xFF;
	}else{
		*data = (value>>24)&0xFF;
		*(data+1) = (value>>16)&0xFF;
		*(data+2) = (value>>8)&0xFF;
		*(data+3) = (value>>0)&0xFF;
	}
	return 4;
}
int Util::putShort(char *data,int value,bool isLittle){
	if(isLittle){
		*data = value&0xFF;
		*(data+1) = (value>>8)&0xFF;
	}else{
		*(data+0) = (value>>8)&0xFF;
		*(data+1) = (value>>0)&0xFF;
	}
	return 2;
}

int Util::putFlagInt(char *data,short flag,int value,bool isLittle){
	Util::putShort(data,flag,isLittle);
	Util::putShort(data+2,4,isLittle);
	Util::putInt(data+4,value,isLittle);
	return 8;
}
int Util::putFlagShort(char *data,short flag,int value,bool isLittle){
	Util::putShort(data,flag,isLittle);
	Util::putShort(data+2,2,isLittle);
	Util::putShort(data+4,value,isLittle);
	return 6;
}
int Util::putFlagByte(char *data,short flag,int value,bool isLittle){
	Util::putShort(data,flag,isLittle);
	Util::putShort(data+2,1,isLittle);
	*(data+4) = value&0xFF;
	return 5;
}
int Util::putFlagByteArr(char *data,short flag,char* value,int len,bool isLittle){
	Util::putShort(data,flag,isLittle);
	Util::putShort(data+2,len,isLittle);
	::memcpy(data+4,value,len);
	return 4+len;
}

short Util::getShort(char* data,bool isLittle){
	short result = 0;
	if(isLittle){
		result = (*data&0xFF)|((*(data+1)&0xFF)<<8);
	}else{
		result = ((*data&0xFF)<<8)|((*(data+1)&0xFF)<<0);
	}
	return result;
}
int Util::getInt(char* data,bool isLittle){
	int result = 0;
	if(isLittle){
		result = ((*data&0xFF)<<0)|((*(data+1)&0xFF)<<8)|((*(data+2)&0xFF)<<16)|((*(data+3)&0xFF)<<24);
	}else{
		result = ((*data&0xFF)<<24)|((*(data+1)&0xFF)<<16)|((*(data+2)&0xFF)<<8)|((*(data+3)&0xFF)<<0);
	}
	return result;
}
long8 Util::getLongLong(char* data,bool isLittle){
	long8 result = 0;
	if(isLittle){
		
	}else{
		
	}
	return result;
}

float Util::getFloat(char* data){
	float* p = (float*)data;
	return *p;
}

/**
 * 首先判断经纬度上是否有重合
 * 然后判断区域的四个角是否在线段的同侧。
 * 原始分割区域边界描述，[)
 */
bool Util::isInter(long8 areaId,Point *p1,Point *p2,int unit){
	double lo1 = p1->getLo();
	double la1 = p1->getLa();
	double lo2 = p2->getLo();
	double la2 = p2->getLa();
	
	double minLo = lo1>lo2?lo2:lo1;
	double maxLo = lo1>lo2?lo1:lo2;
	double minLa = la1>la2?la2:la1;
	double maxLa = la1>la2?la1:la2;

	long loAreaId = areaId >> 32;
	long laAreaId = areaId & 0xFFFFFFFFl;

	double oriMinLo = loAreaId*Util::LO_SCALE/1000*unit - 180;
	double oriMinLa = laAreaId*Util::LA_SCALE/1000*unit - 90;
	double oriMaxLo = oriMinLo + unit*Util::LO_SCALE/1000;
	double oriMaxLa = oriMinLa + unit*Util::LA_SCALE/1000;

	if(maxLo < oriMinLo || minLo>= oriMaxLo || maxLa < oriMinLa || minLa>=oriMaxLo){
		return false;
	}

	if(lo2 - lo1 == 0){
		return true;
	}

	double tag = (la2-la1)/(lo2-lo1);
	double offset = (la1*lo2-la2*lo1)/(lo2-lo1);

	bool isPositive;

	isPositive = (tag*oriMinLo - oriMinLa + offset >= 0);
	if(isPositive != (tag*oriMinLo - oriMaxLa + offset >= 0)){
		return true;
	}
	if(isPositive != (tag*oriMaxLo - oriMinLa + offset >= 0)){
		return true;
	}
	if(isPositive != (tag*oriMaxLo - oriMaxLa + offset >= 0)){
		return true;
	}

	return false;
}
long Util::lola2m(double lola,bool isLo){
	if(isLo){
		return (long)((lola + 180) * 1000 / Util::LO_SCALE + 0.5);
	}else{
		return (long)((lola + 90) * 1000 / Util::LA_SCALE + 0.5);
	}
}
double Util::m2lola(long m,bool isLo){
	if(isLo){
		return m * Util::LO_SCALE / 1000 - 180;
	}else{
		return m * Util::LA_SCALE / 1000 - 90;
	}
}

long8 Util::getAreaId(double lo,double la,int unit){
	long8 result = 0;

	long8 loAreaId = (long)((lo + 180) * 1000 / Util::LO_SCALE / unit);
	long8 laAreaId = (long)((la + 90) * 1000 / Util::LA_SCALE / unit);

	result = (loAreaId << 32) + laAreaId;

	return result;
}

long Util::distance(long lo1,long la1,long lo2,long la2){
	long8 lo = lo1 - lo2;
	long8 la = la1 - la2;

	return (long)sqrtl(lo*lo+la*la);
}
long8 Util::distancePower(long lo1,long la1,long lo2,long la2){
	long8 lo = lo1 - lo2;
	long8 la = la1 - la2;

	return lo*lo+la*la;
}
long8 Util::countInterPoint(Point *p1,Point *p2,Point *p3){
	long8 lo1 = p1->getLom();
	long8 la1 = p1->getLam();
	long8 lo2 = p2->getLom();
	long8 la2 = p2->getLam();
	long8 lo3 = p3->getLom();
	long8 la3 = p3->getLam();

	long8 lo4;
	long8 la4;
	double tempLo;

	if(lo2 - lo1 == 0){
		la4 = la3;
		lo4 = lo1;
	}else{
		double tag = (la2-la1)*1.0/(lo2-lo1);
		if(tag == 0){
			lo4 = lo3;
			la4 = la1;
		}else{
			double tagOver = 1/tag;
			long8 offset = (long8)((la1*lo2-la2*lo1)*1.0/(lo2-lo1));

			tempLo = (la3-offset + lo3*tagOver)/(tag + tagOver);
			lo4 = (long8)tempLo;
			tempLo = tag * tempLo + offset;
			la4 = (long8)tempLo;
		}
	}
	return (lo4<<32)+la4;
}
int Util::arc2Angle(double arc){
	return (int)(arc/Util::PI * 180 + 0.5);
}
double Util::angle2Arc(int angle){
	return angle * Util::PI / 180;
}


Util::~Util()
{

}
