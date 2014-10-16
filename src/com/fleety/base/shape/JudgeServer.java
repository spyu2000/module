package com.fleety.base.shape;

import java.util.HashMap;
import java.util.Iterator;

import com.fleety.base.shape.java.JudgeActionForJava;

/**
 * ������״���λ���жϷ���
 * ����linux dllʵ�֣�����jni�ӿڣ���VC����ʵ�ֽӿ�
 * ע�⣺ʹ�ô�ģ�鹦�ܵ�ʱ�����Ƚ�linux��̬���ӿ��ļ�libjudgeModule.so���·�����õ�LD_LIBRARY_PATH·����
 * ��libjudgeModule.so�ļ����ڳ�������Ŀ¼��
 * ���ڳ��������ű�����ӣ���export LD_LIBRARY_PATH=/home/fleety/MainServer/:$LD_LIBRARY_PATH��������Ŀ¼���ý�·�����Ż���ȷʹ�ö�̬���ӿ⡣
 * ���ȷֱ�������������linux�汾
 * ��module�����µ�linux_dllĿ¼��������so�ļ���
 * libjudgeModule.soΪ32λlinux�������汾�Ķ�̬���ӿ⣬
 * libjudgeModule_x64.soΪ64λlinux�������汾�Ķ�̬���ӿ�
 * create by leo 2009-12-07
 *
 */
public class JudgeServer {
	private IAction action=null;
	/**
	 * ��״����֮���ʶ
	 */
	public final static int POINT_FLAG=0;
	/**
	 *˫���߱�ʶ
	 */
	public final static int DOUBLE_LINE_FLAG=1;
	/**
	 * ������˳������߱�ʶ
	 */
	public final static int SINGLE_LINE_FLAG=2;
	/**
	 * ��״����֮���ʶ
	 */
	public final static int AREA_FLAG=3;
	
	private int actionInsId=0;//ʵ���Ĺ�ϣֵ
	
	private static JudgeServer singleInstance = null;
	private HashMap shapeMapping = new HashMap();
	public static JudgeServer getSingleInstace(){
		if(singleInstance==null){
			synchronized (JudgeServer.class) {
				if(singleInstance==null){
					singleInstance = new JudgeServer();
				}
			}
		}
		return singleInstance;
	}
	public HashMap getShpLst(int shapeType){
		return (HashMap)shapeMapping.get(new Integer(shapeType));
	}
	public JudgeServer(){
		action = new JudgeActionForJava();
		actionInsId = action.hashCode();
	}
	
	public void setAction(IAction action){
		this.action = action;
	}
	
	/**
	 * ���ж�ģ������ӵ㡢�ߡ���
	 * @param pid	�㡢�ߡ���ı�ʶid
	 * @param lo	���ȼ���
	 * @param la	γ�ȼ���
	 * @param type	shape����	0:�㣻1:˫���ߣ�2:�����ߣ�3:��
	 */
	public synchronized void addShape(int pid,double[] lo,double[] la,int type){
		this.addShape(pid, lo, la, type, JudgeAction.POLYGON_FLAG);
	}
	public synchronized void addShape(int pid,double[] lo,double[] la,int type,int areaType){
		action.addShape(actionInsId, pid, lo, la, type, areaType);
		HashMap shpLst = (HashMap)shapeMapping.get(new Integer(type));
		if(shpLst==null){
			shpLst = new HashMap();
			shapeMapping.put(new Integer(type), shpLst);
		}
		shpLst.put(new Integer(pid),null);
	}
	
	/**
	 * �Ƴ��ж�ģ���еĵ㡢�ߡ���
	 * @param pid	�㡢�ߡ���ı�ʶid
	 * @param type	shape����	0:�㣻1:˫���ߣ�2:�����ߣ�3:��
	 */
	public synchronized void removeShape(int pid,int type){
		action.removeShape(actionInsId, pid, type);
		HashMap mapping = (HashMap)this.shapeMapping.get(new Integer(type));
		if(mapping != null){
			mapping.remove(new Integer(pid));
			if(mapping.size() == 0){
				this.shapeMapping.remove(new Integer(type));
			}
		}
	}
	public synchronized void clearShape(){
		HashMap mapping ;
		Integer type,pid;
		for(Iterator itr1 = this.shapeMapping.keySet().iterator();itr1.hasNext();){
			type = (Integer)itr1.next();
			mapping = (HashMap)this.shapeMapping.get(type);
			if(mapping != null){
				for(Iterator itr2 = mapping.keySet().iterator();itr2.hasNext();){
					pid = (Integer)itr2.next();
					if(pid != null){
						action.removeShape(actionInsId, pid.intValue(), type.intValue());
					}
				}
			}
		}
		this.shapeMapping.clear();
	}
	
	/**
	 * �жϵ���������ĳ��������
	 * @param lo
	 * @param la
	 * @return -1��ʾ���������ڣ�>=0��ʾ����ĳ�������������ڵ������ʶidΪ����ֵ
	 */
	public synchronized int getArea(double lo,double la){
		return getArea(-1, lo, la);
	}
	/**
	 * �жϵ���������ĳ��������
	 * @param areaId	��areaIdΪ-1�����жϵ��Ƿ���������ĳ��������
	 * @param lo
	 * @param la
	 * @return -1��ʾ���������ڣ�>=0��ʾ����ĳ�������������ڵ������ʶidΪ����ֵ
	 */
	public synchronized int getArea(int areaId,double lo,double la){
		return action.getArea(actionInsId, areaId, lo, la);
	}
	
	/**
	 * ��ȡ����ĵ���Ϣ
	 * @param lo	
	 * @param la
	 * @param limitDis	�����жϾ��룬>0��ʾ��ȡ����������ڷ�ԲlimitDis��Χ�ڣ�������Ϊ������㣬0��ʾֻ���ҵ�����㣬��λ��
	 * @return	-1��ʾ������㣬>=0��ʾ�����ı�ʶid
	 */
	public synchronized int getNearestPoint(double lo,double la,int limitDis){
		return action.getNearestPoint(actionInsId, lo, la, limitDis);
	}
	
	/**
	 * �ж��Ƿ���ĳ����·�ϣ���ֱ�������·��limitDis�����ڼ���Ϊ�ڸ���·��
	 * @param lo
	 * @param la
	 * @param limitDis	�жϵ��Ƿ�������·�ϵľ����жϲ���ֵ����λ��
	 * @param dir ����������˳ʱ��ƫ��0��359�ķ���
	 * @return	-1��ʾδ������·�ϣ�>=0��ʾ����ĳ����·���ҷ���ֵΪ��·��ʶid
	 */
	public synchronized int getNearestLine(double lo,double la,int limitDis,int dir){
		return getNearestLine(-1, lo, la, limitDis,dir);
	}
	/**
	 * �ж��Ƿ���ĳ����·�ϣ���ֱ�������·��limitDis�����ڼ���Ϊ�ڸ���·��
	 * @param lineId
	 * @param lo
	 * @param la
	 * @param limitDis	�жϵ��Ƿ�������·�ϵľ����жϲ���ֵ����λ��
	 * @param dir ����������˳ʱ��ƫ��0��359�ķ���
	 * @return	-1��ʾδ������·�ϣ�>=0��ʾ����ĳ����·���ҷ���ֵΪ��·��ʶid
	 */
	public synchronized int getNearestLine(int lineId,double lo,double la,int limitDis,int dir){
		return getNearestLine(-1, lo, la, limitDis,dir,null);
	}
	
	public synchronized int getNearestLine(int lineId,double lo,double la,int limitDis,int dir,ResultInfo resultInfo){
		return action.getNearestLine(actionInsId, lineId, lo, la, limitDis,dir,resultInfo);
	}
	
	public void finalize() throws Throwable {
		this.clearShape();
		super.finalize();
	}
}
