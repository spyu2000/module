/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class com_fleety_base_shape_JudgeAction */

#ifndef _Included_com_fleety_base_shape_JudgeAction
#define _Included_com_fleety_base_shape_JudgeAction
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     com_fleety_base_shape_JudgeAction
 * Method:    addShape
 * Signature: (II[D[DI)V
 */
JNIEXPORT void JNICALL Java_com_fleety_base_shape_JudgeAction_addShape
  (JNIEnv *, jobject, jint, jint, jdoubleArray, jdoubleArray, jint, jint);

/*
 * Class:     com_fleety_base_shape_JudgeAction
 * Method:    removeShape
 * Signature: (III)V
 */
JNIEXPORT void JNICALL Java_com_fleety_base_shape_JudgeAction_removeShape
  (JNIEnv *, jobject, jint, jint, jint);

/*
 * Class:     com_fleety_base_shape_JudgeAction
 * Method:    getArea
 * Signature: (IIDD)I
 */
JNIEXPORT jint JNICALL Java_com_fleety_base_shape_JudgeAction_getArea
  (JNIEnv *, jobject, jint, jint, jdouble, jdouble);

/*
 * Class:     com_fleety_base_shape_JudgeAction
 * Method:    getNearestPoint
 * Signature: (IDDI)I
 */
JNIEXPORT jint JNICALL Java_com_fleety_base_shape_JudgeAction_getNearestPoint
  (JNIEnv *, jobject, jint, jdouble, jdouble, jint);

/*
 * Class:     com_fleety_base_shape_JudgeAction
 * Method:    getNearestLine
 * Signature: (IIDDII)I
 */
JNIEXPORT jint JNICALL Java_com_fleety_base_shape_JudgeAction_getNearestLine
  (JNIEnv *, jobject, jint, jint, jdouble, jdouble, jint, jint);

#ifdef __cplusplus
}
#endif
#endif