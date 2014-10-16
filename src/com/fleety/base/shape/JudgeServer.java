package com.fleety.base.shape;

import java.util.HashMap;
import java.util.Iterator;

import com.fleety.base.shape.java.JudgeActionForJava;

/**
 * 点与形状相关位置判断服务
 * 采用linux dll实现，创建jni接口，由VC代码实现接口
 * 注意：使用此模块功能的时候，需先将linux动态链接库文件libjudgeModule.so存放路径配置到LD_LIBRARY_PATH路径下
 * 若libjudgeModule.so文件是在程序启动目录下
 * 请在程序启动脚本中添加，如export LD_LIBRARY_PATH=/home/fleety/MainServer/:$LD_LIBRARY_PATH，将启动目录配置进路径，才会正确使用动态链接库。
 * 请先分辨程序部署服务器的linux版本
 * 在module工程下的linux_dll目录中有两个so文件，
 * libjudgeModule.so为32位linux服务器版本的动态链接库，
 * libjudgeModule_x64.so为64位linux服务器版本的动态链接库
 * create by leo 2009-12-07
 *
 */
public class JudgeServer {
	private IAction action=null;
	/**
	 * 形状类型之点标识
	 */
	public final static int POINT_FLAG=0;
	/**
	 *双向线标识
	 */
	public final static int DOUBLE_LINE_FLAG=1;
	/**
	 * 点物理顺序单向的线标识
	 */
	public final static int SINGLE_LINE_FLAG=2;
	/**
	 * 形状类型之面标识
	 */
	public final static int AREA_FLAG=3;
	
	private int actionInsId=0;//实例的哈希值
	
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
	 * 往判断模块中添加点、线、面
	 * @param pid	点、线、面的标识id
	 * @param lo	经度集合
	 * @param la	纬度集合
	 * @param type	shape类型	0:点；1:双向线；2:单向线；3:面
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
	 * 移除判断模块中的点、线、面
	 * @param pid	点、线、面的标识id
	 * @param type	shape类型	0:点；1:双向线；2:单向线；3:面
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
	 * 判断点有无落在某个区域内
	 * @param lo
	 * @param la
	 * @return -1表示不在区域内，>=0表示落在某个区域内且落在的区域标识id为返回值
	 */
	public synchronized int getArea(double lo,double la){
		return getArea(-1, lo, la);
	}
	/**
	 * 判断点有无落在某个区域内
	 * @param areaId	若areaId为-1，则判断点是否落在任意某个区域内
	 * @param lo
	 * @param la
	 * @return -1表示不在区域内，>=0表示落在某个区域内且落在的区域标识id为返回值
	 */
	public synchronized int getArea(int areaId,double lo,double la){
		return action.getArea(actionInsId, areaId, lo, la);
	}
	
	/**
	 * 获取最近的点信息
	 * @param lo	
	 * @param la
	 * @param limitDis	限制判断距离，>0表示获取的最近点需在方圆limitDis范围内，否则认为无最近点，0表示只需找到最近点，单位米
	 * @return	-1表示无最近点，>=0表示最近点的标识id
	 */
	public synchronized int getNearestPoint(double lo,double la,int limitDis){
		return action.getNearestPoint(actionInsId, lo, la, limitDis);
	}
	
	/**
	 * 判断是否在某个线路上，垂直距离该线路在limitDis距离内即认为在该线路上
	 * @param lo
	 * @param la
	 * @param limitDis	判断点是否落在线路上的距离判断参数值，单位米
	 * @param dir 以正北方向顺时针偏移0到359的方向
	 * @return	-1表示未落在线路上，>=0表示落在某个线路上且返回值为线路标识id
	 */
	public synchronized int getNearestLine(double lo,double la,int limitDis,int dir){
		return getNearestLine(-1, lo, la, limitDis,dir);
	}
	/**
	 * 判断是否在某个线路上，垂直距离该线路在limitDis距离内即认为在该线路上
	 * @param lineId
	 * @param lo
	 * @param la
	 * @param limitDis	判断点是否落在线路上的距离判断参数值，单位米
	 * @param dir 以正北方向顺时针偏移0到359的方向
	 * @return	-1表示未落在线路上，>=0表示落在某个线路上且返回值为线路标识id
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
