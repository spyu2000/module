// DataLib.h: interface for the DataLib class.
//
//////////////////////////////////////////////////////////////////////

#ifndef DATALIB_H
#define DATALIB_H

#if _MSC_VER > 1000
#pragma once
#endif // _MSC_VER > 1000
//__declspec(dllexport)

#include <map>
#include "DataBase.h"

//#define _LINUX  //±àLinux°æ±¾Ê±£¬¿ªÆô_LINUXºê

#ifdef  __cplusplus
	extern "C" {
#endif

#ifdef _LINUX
bool  encode(double loIn, double laIn, double& loOut, double& laOut);
bool  decode(double loIn, double laIn, double& loOut, double& laOut);
//double __stdcall encodeEx(const double loIn[], const double laIn[], double size, double loOut[],double laOut[]);
//double __stdcall decodeEx(const double loIn[], const double laIn[], double size, double loOut[],double laOut[]);
#else
void __declspec(dllexport)   __stdcall  initialize();
bool __declspec(dllexport)   __stdcall  encode(double loIn, double laIn, double& loOut, double& laOut);
bool __declspec(dllexport)   __stdcall  decode(double loIn, double laIn, double& loOut, double& laOut);
#endif


class DataLib  
{
public:
	DataLib();
	virtual ~DataLib();
	void InitData();
	void OutPut();
	bool GetData(int nKey, AREAOFFSET& offset);
};

#ifdef __cplusplus
	}
#endif

#endif // DATALIB_H
