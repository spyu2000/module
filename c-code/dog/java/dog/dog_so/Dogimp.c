 /*************************************************************
 *
 *      @project:        libMhlinuxj.so
 *
 *
 *      @filename:       dogimp.cc
 *        
 *      @description:    Implementation of low-level functions.
 *
 *
 *************************************************************
 *
 */


#include <jni.h>
#include "gsdog.h"
#include "gsmh.h"
#include "version.c"


short int DogBytes,DogAddr;
unsigned long DogPassword;
unsigned long NewPassword;
unsigned long DogResult;
unsigned char DogCascade;
void * DogData;


//Check dog
JNIEXPORT jint JNICALL Java_com_fleety_server_GSDOG_DogCheck
  (JNIEnv * env, jobject obj)
{
	jint retCode;
	jfieldID fid;						   //Field ID
		
	jclass cls = (*env)->GetObjectClass(env, obj);//Get class

	fid = (*env)->GetFieldID(env, cls, "DogCascade", "I");//Get field ID of DogCascade
	if (fid == 0) {
	return 1;
	}
	DogCascade =(unsigned char)(*env)->GetIntField(env,obj,fid);//Get DogCascade
	retCode = DogCheck();//Call interface function
	return retCode ;
}


//Transform
JNIEXPORT jint JNICALL Java_com_fleety_server_GSDOG_DogConvert
  (JNIEnv * env, jobject obj)
{
	jint retCode;
	jfieldID fid;
	jbyteArray a1;
	jsize n;
	jbyte* tmpdata0;
	jboolean copy=1;
			
	jclass cls = (*env)->GetObjectClass(env, obj);	//Get class


	fid = (*env)->GetFieldID(env, cls, "DogCascade", "I");//Get field ID of DogCascade
	if (fid == 0) {
	return 1;
	}
	
	DogCascade =(unsigned char)(*env)->GetIntField(env,obj,fid);//Get DogCascade

	fid = (*env)->GetFieldID(env,cls,"DogBytes","I");//Get field ID of DogBytes
	if (0 == fid)
	{
		return 1;
	}
	DogBytes = (short int) ((*env)->GetIntField(env,obj,fid));//Get DogBytes
	fid = (*env)->GetFieldID(env,cls,"DogAddr","I");//Get field ID of DogAddr
	if (0 == fid)
	{
		return 1;
	}
	DogAddr = (short int) ((*env)->GetIntField(env,obj,fid));//Get DogAddr
	fid = (*env)->GetFieldID(env,cls,"DogPassword","I");////Get field ID of DogPassword
	if (0 == fid)
	{
		return 1;
	}
	DogPassword = (unsigned long) ((*env)->GetIntField(env,obj,fid));//Get DogPassword
        DogResult = 0; // first, clear it.


	fid = (*env)->GetFieldID(env,cls, "DogData", "[B");//Get field ID
	if (0==fid)
		return 1;

	a1=(jbyteArray)((*env)->GetObjectField(env,obj,fid));

	n= (*env)->GetArrayLength(env,(jbyteArray)a1);

	tmpdata0=(*env)->GetByteArrayElements(env,(jbyteArray)a1,&copy);
	DogData=tmpdata0;

	 retCode = DogConvert();//Call interface function
	 if(0!=retCode)
		 return retCode;
	 fid = (*env)->GetFieldID(env,cls,"DogResult","J");//Get field ID of DogResult
	 if (0 == fid)
		 return 1;
	 (*env)->SetLongField(env,obj,fid,DogResult);//Set field of DogResult



	return retCode ;

}


//Get Current number
JNIEXPORT jint JNICALL Java_com_fleety_server_GSDOG_GetCurrentNo
  (JNIEnv * env, jobject obj)
{
	jint retCode;
	jfieldID fid;						   //Field ID
		
	jclass cls = (*env)->GetObjectClass(env, obj);//Get class


	fid = (*env)->GetFieldID(env,cls, "DogCascade", "I");// Get field ID of DogCascade
	if (fid == 0) {
	return 1;
	}
	
	DogCascade =(unsigned char)(*env)->GetIntField(env,obj,fid);//Get DogCascade

	DogData = & DogResult;
	
	retCode=GetCurrentNo();

	fid = (*env)->GetFieldID(env,cls,"DogResult","J");//Get field ID of DogResult
	if(0 == fid)
	{
		return 1;
	}
	(*env)->SetLongField(env,obj,fid,(jlong)DogResult);//Set field value of DogResult
	return retCode ;
}

//Read dog
JNIEXPORT jint JNICALL Java_com_fleety_server_GSDOG_ReadDog
  (JNIEnv *env, jobject obj)
{
	jint retCode;
	jfieldID fid;
	jbyteArray a1;
	jbyte* tmpdata0;
	jsize n=0;
	
	jboolean copy=1;				
	jclass cls = (*env)->GetObjectClass(env, obj);		//Get class

	fid = (*env)->GetFieldID(env, cls, "DogCascade", "I");//Get field ID of DogCascade
	if (fid == 0) {
	return 1;
	}
	DogCascade =(unsigned char)(*env)->GetIntField(env,obj,fid);			//Get DogCascade

	fid = (*env)->GetFieldID(env,cls,"DogBytes","I");//Get field ID of DogBytes
	if (0 == fid)
	{
		return 1;
	}
	DogBytes = (short int) ((*env)->GetIntField(env,obj,fid));//Get DogBytes

	fid = (*env)->GetFieldID(env,cls,"DogAddr","I");//Get field ID of DogAddr
	if (0 == fid)
	{
		return 1;
	}
	DogAddr = (short int) ((*env)->GetIntField(env,obj,fid));//Get DogAddr

	fid = (*env)->GetFieldID(env,cls,"DogPassword","I");//Get field ID of DogPassword
	if (0 == fid)
	{
		return 1;
	}
	DogPassword = (unsigned long) ((*env)->GetIntField(env,obj,fid));//Get DogPassword


	fid = (*env)->GetFieldID(env,cls, "DogData", "[B");//Get field ID
	if (0==fid)
		return 1;

	a1=(jbyteArray)((*env)->GetObjectField(env,obj,fid));

	n= (*env)->GetArrayLength(env,(jbyteArray)a1);

	tmpdata0=(*env)->GetByteArrayElements(env,(jbyteArray)a1,&copy);
	DogData=tmpdata0;

	retCode = ReadDog();//Call interface function
	if (0!=retCode)
		return retCode;



	(*env)->SetByteArrayRegion(env,a1,0,DogBytes,(jbyte*)DogData);

	(*env)->SetObjectField(env,obj, fid, a1);//Set field value
	
	return retCode ;
}

//Write dog
JNIEXPORT jint JNICALL Java_com_fleety_server_GSDOG_WriteDog
  (JNIEnv *env, jobject obj)
{
	jint retCode;
	jfieldID fid;
	jbyteArray a1;
	jbyte* tmpdata0;
	jsize n=0;
	jboolean copy=1;
	
	jclass cls = (*env)->GetObjectClass(env, obj);//Get class

	fid = (*env)->GetFieldID(env,cls, "DogCascade", "I");//Get field of DogCascade
	if (fid == 0) {
	return 1;
	}
	DogCascade =(unsigned char)(*env)->GetIntField(env,obj,fid);//Get DogCascade
	
	fid = (*env)->GetFieldID(env,cls,"DogBytes","I");//Get field of DogBytes
	if (0 == fid)
	{
		return 1;
	}
	DogBytes = (short int) ((*env)->GetIntField(env,obj,fid));//Get DogBytes
	fid = (*env)->GetFieldID(env,cls,"DogAddr","I");//Get field ID of DogAddr
	if (0 == fid)
	{
		return 1;
	}
	DogAddr = (short int) ((*env)->GetIntField(env,obj,fid));//Get DogAddr
	fid = (*env)->GetFieldID(env,cls,"DogPassword","I");//Get field of DogPassword
	if (0 == fid)
	{
		return 1;
	}
	DogPassword = (unsigned long) ((*env)->GetIntField(env,obj,fid));//Get DogPassword

	fid = (*env)->GetFieldID(env,cls, "DogData", "[B");//Get field ID
	if (0==fid)
		return 1;

	a1=(jbyteArray)((*env)->GetObjectField(env,obj,fid));
	n= (*env)->GetArrayLength(env,(jbyteArray)a1);
	tmpdata0=(*env)->GetByteArrayElements(env,(jbyteArray)a1,NULL);
	DogData=tmpdata0;
	retCode = WriteDog();//Call interface function
	if (0!=retCode)
		return retCode;

	return retCode ;
}
