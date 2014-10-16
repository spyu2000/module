package com.fleety.base.shape;

public interface IAction {
	public static final int CIRCLE_FLAG = 1;
	public static final int RECTANGLE_FLAG = 2;
	public static final int POLYGON_FLAG = 3;
	
	/**
	 * ���ж�ģ������ӵ㡢�ߡ���
	 * @param instanceId ʵ��hashid
	 * @param pid	�㡢�ߡ���ı�ʶid
	 * @param lo	���ȼ���
	 * @param la	γ�ȼ���
	 * @param type	shape����	0:�㣻1:˫���ߣ�2:�����ߣ�3:��
	 * @param areaType �����״����,Ŀǰ֧�� ����� Բ�� ���Σ�����Բ�Σ���γ�ȴ����о���
	 */
	public void addShape(int instanceId,int pid,double[] lo,double[] la,int type,int areaType);
	/**
	 * �Ƴ��ж�ģ���еĵ㡢�ߡ���
	 * @param instanceId	ʵ��id
	 * @param pid	�㡢�ߡ���ı�ʶid
	 * @param type	shape����	0:�㣻1:˫���ߣ�2:�����ߣ�3:��
	 */
	public void removeShape(int instanceId,int pid,int type);
	/**
	 * �жϵ���������ĳ��������
	 * @param instanceId	ʵ��id
	 * @param areaId	��areaIdΪ0�����жϵ��Ƿ���������ĳ��������
	 * @param lo
	 * @param la
	 * @return -1��ʾ���������ڣ�>0��ʾ����ĳ�������������ڵ������ʶidΪ����ֵ
	 */
	public int getArea(int instanceId,int areaId,double lo,double la);
	
	/**
	 * ��ȡ����ĵ���Ϣ
	 * @param instanceId	ʵ��id
	 * @param lo	
	 * @param la
	 * @param limitDis	�����жϾ��룬����ȡ����������ڷ�ԲlimitDis��Χ�ڣ�������Ϊ������㣬��λ��
	 * @return	-1��ʾ������㣬>0��ʾ�����ı�ʶid
	 */
	public int getNearestPoint(int instanceId,double lo,double la,int limitDis);
	
	/**
	 * �ж��Ƿ���ĳ����·�ϣ���ֱ�������·��limitDis�����ڼ���Ϊ�ڸ���·��
	 * @param instanceId
	 * @param lineId
	 * @param lo
	 * @param la
	 * @param limitDis	�жϵ��Ƿ�������·�ϵľ����жϲ���ֵ����λ��
	 * @param dir ����������˳ʱ��ƫ��0��359�ķ���
	 * @return	-1��ʾδ������·�ϣ�>0��ʾ����ĳ����·���ҷ���ֵΪ��·��ʶid
	 */
	public int getNearestLine(int instanceId,int lineId,double lo,double la,int limitDis,int dir);
	
	/**
	 * �ж��Ƿ���ĳ����·�ϣ���ֱ�������·��limitDis�����ڼ���Ϊ�ڸ���·��
	 * @param instanceId
	 * @param lineId
	 * @param lo
	 * @param la
	 * @param limitDis	�жϵ��Ƿ�������·�ϵľ����жϲ���ֵ����λ��
	 * @param dir ����������˳ʱ��ƫ��0��359�ķ���
	 * @param resultInfo ������ϸ��������Ϣ����
	 * @return	-1��ʾδ������·�ϣ�>0��ʾ����ĳ����·���ҷ���ֵΪ��·��ʶid
	 */
	public int getNearestLine(int instanceId,int lineId,double lo,double la,int limitDis,int dir,ResultInfo resultInfo);
	
}
