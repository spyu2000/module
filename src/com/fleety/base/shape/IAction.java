package com.fleety.base.shape;

public interface IAction {
	public static final int CIRCLE_FLAG = 1;
	public static final int RECTANGLE_FLAG = 2;
	public static final int POLYGON_FLAG = 3;
	
	/**
	 * 往判断模块中添加点、线、面
	 * @param instanceId 实例hashid
	 * @param pid	点、线、面的标识id
	 * @param lo	经度集合
	 * @param la	纬度集合
	 * @param type	shape类型	0:点；1:双向线；2:单向线；3:面
	 * @param areaType 面的形状类型,目前支持 多边形 圆形 矩形，若是圆形，则经纬度传内切矩形
	 */
	public void addShape(int instanceId,int pid,double[] lo,double[] la,int type,int areaType);
	/**
	 * 移除判断模块中的点、线、面
	 * @param instanceId	实例id
	 * @param pid	点、线、面的标识id
	 * @param type	shape类型	0:点；1:双向线；2:单向线；3:面
	 */
	public void removeShape(int instanceId,int pid,int type);
	/**
	 * 判断点有无落在某个区域内
	 * @param instanceId	实例id
	 * @param areaId	若areaId为0，则判断点是否落在任意某个区域内
	 * @param lo
	 * @param la
	 * @return -1表示不在区域内，>0表示落在某个区域内且落在的区域标识id为返回值
	 */
	public int getArea(int instanceId,int areaId,double lo,double la);
	
	/**
	 * 获取最近的点信息
	 * @param instanceId	实例id
	 * @param lo	
	 * @param la
	 * @param limitDis	限制判断距离，若获取的最近点需在方圆limitDis范围内，否则认为无最近点，单位米
	 * @return	-1表示无最近点，>0表示最近点的标识id
	 */
	public int getNearestPoint(int instanceId,double lo,double la,int limitDis);
	
	/**
	 * 判断是否在某个线路上，垂直距离该线路在limitDis距离内即认为在该线路上
	 * @param instanceId
	 * @param lineId
	 * @param lo
	 * @param la
	 * @param limitDis	判断点是否落在线路上的距离判断参数值，单位米
	 * @param dir 以正北方向顺时针偏移0到359的方向
	 * @return	-1表示未落在线路上，>0表示落在某个线路上且返回值为线路标识id
	 */
	public int getNearestLine(int instanceId,int lineId,double lo,double la,int limitDis,int dir);
	
	/**
	 * 判断是否在某个线路上，垂直距离该线路在limitDis距离内即认为在该线路上
	 * @param instanceId
	 * @param lineId
	 * @param lo
	 * @param la
	 * @param limitDis	判断点是否落在线路上的距离判断参数值，单位米
	 * @param dir 以正北方向顺时针偏移0到359的方向
	 * @param resultInfo 增加详细交互的信息内容
	 * @return	-1表示未落在线路上，>0表示落在某个线路上且返回值为线路标识id
	 */
	public int getNearestLine(int instanceId,int lineId,double lo,double la,int limitDis,int dir,ResultInfo resultInfo);
	
}
