// Point.h: interface for the Point class.
//
//////////////////////////////////////////////////////////////////////

#if !defined(AFX_POINT_H__92FE886C_DFE1_4E6E_B3E5_DB74B92FB715__INCLUDED_)
#define AFX_POINT_H__92FE886C_DFE1_4E6E_B3E5_DB74B92FB715__INCLUDED_

#if _MSC_VER > 1000
#pragma once
#endif // _MSC_VER > 1000

#include "Util.h"
#include "stdlib.h"

class Point  
{
public:
	Point(int pid,double lo,double la,bool isLast = true);
	virtual ~Point();

	int getPid();

	double getLo();
	double getLa();

	long getLom();
	long getLam();

	bool isLastPoint();
	void setAngle(int angle);
	int getAngle();

	void setFollowDistance(long followDistance);
	long getFollowDistance();

	void setDistanceFromHead(long distanceFromHead);
	long getDistanceFromHead();

	void updateNextPointAssoInfo(Point *nextPoint);

	int isSameDir(int angle,int offset=90);

private:
	int pid;
	double lo;
	double la;
	bool isLast;
	
	//��γ��ת��Ϊ�׺������
	long lom;
	long lam;

	//�ͺ�һ�������γ�ֱ�ߵĽǶȣ�[0,360)
	int angle;
	//�ͺ�һ����ľ��룬��λ��
	long followDistance;

	//��ͷ���õ�ľ��룬��λ��
	long distanceFromHead;

public:
	static int NOT_SAME_DIR;
	static int NATURAL_ORDER_SAME_DIR;
	static int REVERSE_ORDER_SAME_DIR;
};

#endif // !defined(AFX_POINT_H__92FE886C_DFE1_4E6E_B3E5_DB74B92FB715__INCLUDED_)
