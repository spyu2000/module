#include "stdafx.h"
#include <stdio.h>   
#include "com_fleety_base_GPSEncode.h"
#include "DataLib.h"
  
JNIEXPORT jboolean JNICALL Java_com_fleety_base_GPSEncode_encode
  (JNIEnv* env, jclass cls, jdouble lo, jdouble la, jdoubleArray jResultArr)
{
 //   printf("test encode!\n"); 
	double loOut = 0;
    double laOut = 0;

	bool bResult = encode(lo,la,loOut,laOut);

	if(bResult)
	{
		env->SetDoubleArrayRegion(jResultArr,0,1,&loOut);
		env->SetDoubleArrayRegion(jResultArr,1,1,&laOut);
	}

    return bResult;  
}

JNIEXPORT jboolean JNICALL Java_com_fleety_base_GPSEncode_decode
  (JNIEnv* env, jclass cls, jdouble lo, jdouble la, jdoubleArray jResultArr)
{
 //   printf("test decode!\n"); 
	double loOut = 0;
    double laOut = 0;

	bool bResult = decode(lo,la,loOut,laOut);

	if(bResult)
	{
		env->SetDoubleArrayRegion(jResultArr,0,1,&loOut);
		env->SetDoubleArrayRegion(jResultArr,1,1,&laOut);
	}

    return bResult;  	
}

