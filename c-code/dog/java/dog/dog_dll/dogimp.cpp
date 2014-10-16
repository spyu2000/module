 /*************************************************************
 *
 *      @project:        DOGJava.dll
 *
 *
 *      @filename:       dogimp.cpp
 *        
 *      @description:    Implementation of low-level functions.
 *
 *      @Author:         AnHuimin
 *      CopyRight:       SafeNet China Ltd.
 *
 *************************************************************
 *
 */


#include <jni.h>
#include "gsdog.h"
#include "gsmh.h"



short int DogBytes,DogAddr;
unsigned long DogPassword;
unsigned long NewPassword;
unsigned long DogResult;
unsigned char DogCascade;
void * DogData;


//Disable sharing dog
JNIEXPORT jint JNICALL Java_p1_GSDOG_DisableShare
  (JNIEnv * env, jobject obj)
{
	jint retCode;
	jclass cls = env->GetObjectClass(obj);//Get class
	jfieldID fid;						  //Field ID
	fid = env->GetFieldID(cls, "DogCascade", "I");//Get field ID of DogCascade
	if (fid == 0) {
	return 1;
	}
	DogCascade = (unsigned char) env->GetIntField(obj,fid);//Get DogCascade 
	retCode = DisableShare();//Call interface function
	
	return retCode ;

}

//Check dog
JNIEXPORT jint JNICALL Java_com_fleety_server_GSDOG_DogCheck
  (JNIEnv * env, jobject obj)
{
	jint retCode;
	
	jclass cls = env->GetObjectClass( obj);//Get class
	jfieldID fid;						   //Field ID	

	fid = env->GetFieldID(cls, "DogCascade", "I");//Get Field ID of DogCascade
	if (fid == 0) {
	return 1;
	}
	DogCascade =(unsigned char)env->GetIntField(obj,fid);//Get DogCascade
	retCode = DogCheck();//Call interface function
	return retCode ;
}


//Transform
JNIEXPORT jint JNICALL Java_p1_GSDOG_DogConvert
  (JNIEnv * env, jobject obj)
{
	jint retCode;
	jclass cls = env->GetObjectClass( obj);	//Get class
	jfieldID fid;

	fid = env->GetFieldID(cls, "DogCascade", "I");//Get field ID of DogCascade
	if (fid == 0) {
	return 1;
	}
	
	DogCascade =(unsigned char)env->GetIntField(obj,fid);//Get DogCascade

	fid = env->GetFieldID(cls,"DogBytes","I");//Get field ID of DogBytes
	if (0 == fid)
	{
		return 1;
	}
	DogBytes = short int (env->GetIntField(obj,fid));//Get Dogbytes
	fid = env->GetFieldID(cls,"DogAddr","I");//Get field ID of dogAddr
	if (0 == fid)
	{
		return 1;
	}
	DogAddr = short int (env->GetIntField(obj,fid));//Get DogAddr
	fid = env->GetFieldID(cls,"DogPassword","I");////Get field ID of DogPassword
	if (0 == fid)
	{
		return 1;
	}
	DogPassword = unsigned long (env->GetIntField(obj,fid));//Get DogPassword
    DogResult = 0; // first, clear it.

	fid = env->GetFieldID(cls, "DogData", "[B");//Get field ID of DogData
	if (0==fid)
		return 1;
	jbyteArray a1;
	a1=(jbyteArray)(env->GetObjectField(obj,fid));
	jsize n=0;
	n= env->GetArrayLength((jbyteArray)a1);
	jboolean copy=true;
	jbyte* tmpdata0=env->GetByteArrayElements((jbyteArray)a1,&copy);
	DogData=tmpdata0;

	 retCode = DogConvert();//Call interface function
	 if(0!=retCode)
		 return retCode;
	 fid = env->GetFieldID(cls,"DogResult","J");//Get field ID of DogResult
	 if (0 == fid)
		 return 1;
	 env->SetLongField(obj,fid,DogResult);//Set field ID of DogResult


	return retCode ;

}


//Get current number
JNIEXPORT jint JNICALL Java_com_fleety_server_GSDOG_GetCurrentNo
  (JNIEnv * env, jobject obj)
{
	jint retCode;
	
	jclass cls = env->GetObjectClass( obj);//Get class
	jfieldID fid;						   //Field ID 	

	fid = env->GetFieldID(cls, "DogCascade", "I");// Get DogCascadeField ID
	if (fid == 0) {
	return 1;
	}
	
	DogCascade =(unsigned char)env->GetIntField(obj,fid);//Get DogCascade

	DogData = & DogResult;
	
	retCode=GetCurrentNo();

	fid = env->GetFieldID(cls,"DogResult","J");//Ger field ID of DogResult
	if(0 == fid)
	{
		return 1;
	}
	env->SetLongField(obj,fid,jlong(DogResult));//Set field ID of DogResult
	return retCode ;
}

//Read dog
JNIEXPORT jint JNICALL Java_com_fleety_server_GSDOG_ReadDog
  (JNIEnv *env, jobject obj)
{
	jint retCode;
	
	jclass cls = env->GetObjectClass( obj);		//Get class
	jfieldID fid;

	fid = env->GetFieldID(cls, "DogCascade", "I");//Get field ID of DogCascade
	if (fid == 0) {
	return 1;
	}
	DogCascade =(unsigned char)env->GetIntField(obj,fid);			//Get DogCascade

	fid = env->GetFieldID(cls,"DogBytes","I");//Get field ID of DogBytes
	if (0 == fid)
	{
		return 1;
	}
	DogBytes = short int (env->GetIntField(obj,fid));//Get Dogbytes

	fid = env->GetFieldID(cls,"DogAddr","I");//Get field ID of DogAddr
	if (0 == fid)
	{
		return 1;
	}
	DogAddr = short int (env->GetIntField(obj,fid));//Get DogAddr

	fid = env->GetFieldID(cls,"DogPassword","I");//Get field ID of DogPassword
	if (0 == fid)
	{
		return 1;
	}
	DogPassword = unsigned long (env->GetIntField(obj,fid));//Get DogPassword

	fid = env->GetFieldID(cls, "DogData", "[B");//Get field ID of DogData
	if (0==fid)
		return 1;
	jbyteArray a1;
	a1=(jbyteArray)(env->GetObjectField(obj,fid));
	jsize n=0;
	n= env->GetArrayLength((jbyteArray)a1);
	jboolean copy=true;
	jbyte* tmpdata0=env->GetByteArrayElements((jbyteArray)a1,&copy);
	DogData=tmpdata0;

	retCode = ReadDog();//Call interface function
	if (0!=retCode)
		return retCode;

	env->SetByteArrayRegion(a1,0,DogBytes,(jbyte*)DogData);

	env->SetObjectField(obj, fid, a1);//Set field ID
	
	return retCode ;
}

//Write dog
JNIEXPORT jint JNICALL Java_com_fleety_server_GSDOG_WriteDog
  (JNIEnv *env, jobject obj)
{
	jint retCode;
	
	jclass cls = env->GetObjectClass( obj);//Get class
	jfieldID fid;

	fid = env->GetFieldID(cls, "DogCascade", "I");//Get field ID of DogCascade
	if (fid == 0) {
	return 1;
	}
	DogCascade =(unsigned char)env->GetIntField(obj,fid);//Get DogCascade
	
	fid = env->GetFieldID(cls,"DogBytes","I");//Get field ID of Dogbytes
	if (0 == fid)
	{
		return 1;
	}
	DogBytes = short int (env->GetIntField(obj,fid));//Get Dogbytes
	fid = env->GetFieldID(cls,"DogAddr","I");//Get DogAddrField ID
	if (0 == fid)
	{
		return 1;
	}
	DogAddr = short int (env->GetIntField(obj,fid));//Get DogAddr
	fid = env->GetFieldID(cls,"DogPassword","I");//Get field ID of DogPassword
	if (0 == fid)
	{
		return 1;
	}
	DogPassword = unsigned long (env->GetIntField(obj,fid));//Get DogPassword
	fid = env->GetFieldID(cls, "DogData", "[B");//Get field ID of DogData
	if (0==fid)
		return 1;
	jbyteArray a1;
	a1=(jbyteArray)(env->GetObjectField(obj,fid));
	jsize n=0;
	n= env->GetArrayLength((jbyteArray)a1);
	jbyte* tmpdata0=env->GetByteArrayElements((jbyteArray)a1,NULL);
	DogData=tmpdata0;


	retCode = WriteDog();//Call interface function
	return retCode ;
}
