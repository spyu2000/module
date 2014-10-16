#include "stdafx.h"
#include "com_fleety_base_shape_JudgeAction.h"
#include "RoadContainer.h"
#include "PointContainer.h"
#include "AreaContainer.h"
#include "Util.h"
#include <iostream.h>

static std::map<int,void*> lineObjMapping;
static std::map<int,void*> pointObjMapping;
static std::map<int,void*> areaObjMapping;

JNIEXPORT void JNICALL Java_com_fleety_base_shape_JudgeAction_addShape
  (JNIEnv *env, jobject obj, jint instanceId, jint pid, jdoubleArray lo, jdoubleArray la, jint shapeType,jint areaType)
{
	switch (shapeType){
	case POINT_FLAG:
		{
			jsize length=env->GetArrayLength(lo); 
			if(length<1){
				return;
			}
			Point *p;
			PointContainer* pcontainer =(PointContainer*)pointObjMapping[instanceId];
			if(pcontainer==NULL){
				pcontainer = new PointContainer();
				pointObjMapping[instanceId] = pcontainer;
			}
			
			pcontainer->removePoint(pid);

			jdouble  *body_lo  = env->GetDoubleArrayElements(lo,NULL);
			jdouble  *body_la  = env->GetDoubleArrayElements(la,NULL);
			
			jdouble lon = body_lo[0];
			jdouble lat = body_la[0];
			p = new Point(pid,lon,lat);
			pcontainer->addPoint(p);

			env->ReleaseDoubleArrayElements(lo,body_lo,JNI_ABORT);
			env->ReleaseDoubleArrayElements(la,body_la,JNI_ABORT);
			break;	   
		}
	case DOUBLE_LINE_FLAG:
	case SINGLE_LINE_FLAG:
		{

			jsize length=env->GetArrayLength(lo); 
			if(length<2){
				return;
			}
			
			jdouble  *body_lo  = env->GetDoubleArrayElements(lo,NULL);
			jdouble  *body_la  = env->GetDoubleArrayElements(la,NULL);

			RoadSegmentInfo* segment;
			RoadContainer* rdContainer = (RoadContainer*)lineObjMapping[instanceId];
			if(rdContainer==NULL){
				rdContainer = new RoadContainer();
				lineObjMapping[instanceId] = rdContainer;
			}
			rdContainer->removeRoadSegmentById(pid);

			Point *p;
			if(shapeType==SINGLE_LINE_FLAG){
				segment = new RoadSegmentInfo(pid,NULL,0,1,1);
			}else{
				segment = new RoadSegmentInfo(pid,NULL,0,1,0);
			}
			
			for(int i=0;i<length;i++){
				jdouble lon = body_lo[i];
				jdouble lat = body_la[i];
				p = new Point(0,lon,lat);
				segment->addPoint(p);
			}
			segment->updatePointAssoInfo();
            rdContainer->addRoadSegment(segment);
			
			env->ReleaseDoubleArrayElements(lo,body_lo,JNI_ABORT);
			env->ReleaseDoubleArrayElements(la,body_la,JNI_ABORT);
			break;
		}
	case AREA_FLAG:
		{
			jsize length=env->GetArrayLength(lo); 
			if(length<2){
				return;
			}
			Point* p;
			AreaInfo* area = new AreaInfo(pid);
			AreaContainer* acontainer = (AreaContainer*)areaObjMapping[instanceId];
			if(acontainer==NULL){
				acontainer = new AreaContainer();
				areaObjMapping[instanceId] = acontainer;
			}
			acontainer->removeAreaById(pid);

			jdouble  *body_lo  = env->GetDoubleArrayElements(lo,NULL);
			jdouble  *body_la  = env->GetDoubleArrayElements(la,NULL);
			double minla=-1;
			double minlo=-1;
			double maxlo=-1;
			double maxla=-1;
			if(areaType==POLYGON_FLAG){
				if(length<3){
					return;
				}
				for(int i=0;i<length;i++){
					jdouble lon = body_lo[i];
					jdouble lat = body_la[i];
					p = new Point(0,lon,lat);
					area->addPoint(p);
					if(minla==-1){
						minla = lat;
						maxla = lat;
						minlo = lon;
						maxlo = lon;
					}
					if(lat<minla){
						minla=lat;
					}else if(lat>maxla){
						maxla = lat;
					}
					if(lon<minlo){
						minlo = lon;
					}else if(lon>maxlo){
						maxlo = lon;
					}
				}
				area->setBounds(minlo,maxlo,minla,maxla);
				area->setAreaType(areaType);
				acontainer->addArea(area);
			}else if(areaType==RECTANGLE_FLAG){
				for(int i=0;i<length;i++){
					jdouble lon = body_lo[i];
					jdouble lat = body_la[i];
					if(minla==-1){
						minla = lat;
						maxla = lat;
						minlo = lon;
						maxlo = lon;
					}
					if(lat<minla){
						minla=lat;
					}else if(lat>maxla){
						maxla = lat;
					}
					if(lon<minlo){
						minlo = lon;
					}else if(lon>maxlo){
						maxlo = lon;
					}
				}
				
				p = new Point(0,minlo,maxla);
				area->addPoint(p);
				p = new Point(0,maxlo,maxla);
				area->addPoint(p);
				p = new Point(0,maxlo,minla);
				area->addPoint(p);
				p = new Point(0,minlo,minla);
				area->addPoint(p);
			}else if(areaType==CIRCLE_FLAG){
				for(int i=0;i<length;i++){
					jdouble lon = body_lo[i];
					jdouble lat = body_la[i];
					if(minla==-1){
						minla = lat;
						maxla = lat;
						minlo = lon;
						maxlo = lon;
					}
					if(lat<minla){
						minla=lat;
					}else if(lat>maxla){
						maxla = lat;
					}
					if(lon<minlo){
						minlo = lon;
					}else if(lon>maxlo){
						maxlo = lon;
					}
				}
				//圆形传进来的经纬度为内切矩形经纬度
				double lo = (minlo+maxlo)/2;//中心点经度
				double la = (minla+maxla)/2;//中心点纬度
				long lom = Util::lola2m(lo,true);
				long lam = Util::lola2m(la,false);
				long minlom = Util::lola2m(minlo,true);
				long minlam = Util::lola2m(minla,false);
				long radio = Util::distance(lom,lam,minlom,minlam);//半径
				area->setCirclePara(lom,lam,radio);
				area->setAreaType(areaType);
				acontainer->addArea(area);
			}
			
			env->ReleaseDoubleArrayElements(lo,body_lo,JNI_ABORT);
			env->ReleaseDoubleArrayElements(la,body_la,JNI_ABORT);
			break;
		}
	}
}

/*
 * Class:     com_fleety_base_shape_JudgeAction
 * Method:    removeShape
 * Signature: (III)V
 */
JNIEXPORT void JNICALL Java_com_fleety_base_shape_JudgeAction_removeShape
  (JNIEnv *env, jobject obj, jint instanceId, jint pid, jint shapeType)
{
	std::map<int,void*>::iterator itr;
	switch (shapeType){
	case POINT_FLAG:
		{
			itr = pointObjMapping.find(instanceId);
			if(itr == pointObjMapping.end()){
				return;
			}
			PointContainer* pcontainer =(PointContainer*)((*itr).second);
			pcontainer->removePoint(pid);

			if(pcontainer->size() == 0){
				pointObjMapping.erase(itr);

				delete(pcontainer);
			}
			break;	   
		}
	case DOUBLE_LINE_FLAG:
	case SINGLE_LINE_FLAG:
		{
			itr = lineObjMapping.find(instanceId);
			if(itr == lineObjMapping.end()){
				return;
			}
			RoadContainer* rdContainer = (RoadContainer*)((*itr).second);
			
			rdContainer->removeRoadSegmentById(pid);

			if(rdContainer->size() == 0){
				lineObjMapping.erase(itr);

				delete(rdContainer);
			}

			break;
		}
	case AREA_FLAG:
		{
			itr = areaObjMapping.find(instanceId);
			if(itr == areaObjMapping.end()){
				return;
			}
			AreaContainer* acontainer = (AreaContainer*)((*itr).second);
			acontainer->removeAreaById(pid);
			
			
			if(acontainer->size() == 0){
				areaObjMapping.erase(itr);

				delete(acontainer);
			}

			break;
		}
	}	
}

/*
 * Class:     com_fleety_base_shape_JudgeAction
 * Method:    getArea
 * Signature: (IIDD)I
 */
JNIEXPORT jint JNICALL Java_com_fleety_base_shape_JudgeAction_getArea
  (JNIEnv *env, jobject obj, jint instanceId, jint areaId, jdouble lo, jdouble la)
{
	std::map<int,void*>::iterator itr;
	itr = areaObjMapping.find(instanceId);
	if(itr!=areaObjMapping.end()){
		AreaContainer* acontainer =(AreaContainer*)((*itr).second);
		PointInfo pInfo(0,lo,la,0,0,0);
		int resAreaId = acontainer->getNearestArea(areaId,&pInfo);
		if(resAreaId>0){
			return resAreaId;
		}
	}
	return -1;
}

/*
 * Class:     com_fleety_base_shape_JudgeAction
 * Method:    getNearestPoint
 * Signature: (IDDI)I
 */
JNIEXPORT jint JNICALL Java_com_fleety_base_shape_JudgeAction_getNearestPoint
  (JNIEnv *env, jobject obj, jint instanceId, jdouble lo, jdouble la, jint limitDis)
{
	std::map<int,void*>::iterator itr;
	itr = pointObjMapping.find(instanceId);
	if(itr!=pointObjMapping.end()){
		PointContainer* pcontainer =(PointContainer*)((*itr).second);
		PointInfo pInfo(0,lo,la,0,0,0);
		int respointId = pcontainer->getNearestPoint(&pInfo,limitDis);
		if(respointId>0){
			return respointId;
		}
	}
	return -1;
}

/*
 * Class:     com_fleety_base_shape_JudgeAction
 * Method:    getNearestLine
 * Signature: (IIDDII)I
 */
JNIEXPORT jint JNICALL Java_com_fleety_base_shape_JudgeAction_getNearestLine
  (JNIEnv *env, jobject obj, jint instanceId, jint lineId, jdouble lo, jdouble la, jint limitDis, jint dir)
{
	std::map<int,void*>::iterator itr;
	itr = lineObjMapping.find(instanceId);
	if(itr!=lineObjMapping.end()){
		RoadContainer* rdContainer =(RoadContainer*)((*itr).second);
		PointInfo pInfo(0,lo,la,0,dir,0);
		int segmentId = rdContainer->bindSegment(&pInfo,lineId,limitDis);
		if(segmentId>0){
			return segmentId;
		}
	}
	return -1;
}
