package server.cluster;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class GISMarkClusterInstance {
	//各个级别下每象素代表的米数
	private static double[] ratio = new double[]{40000,20000,10000,5000,2333.33333333,1333.33333333
		,666.66666667,333.33333333,133.33333333,66.66666667,33.33333333,16.66666667
		,6.66666667,3.33333333,1.8,0.9};
	
	//单位像素
	private int gridSize = 60;
	private boolean averageCenter = true;
	//单位米,设定缩放比时计算该参数大小
	private double maxGridSize = 0;
	
	private ArrayList<Cluster> clusterList = new ArrayList<Cluster>(8);

	public GISMarkClusterInstance(){
		this(60,true);
	}
		public GISMarkClusterInstance(int gridSize){
		this(gridSize,true);
	}
	public GISMarkClusterInstance(int gridSize,boolean averageCenter){
		this.gridSize = gridSize;
		this.averageCenter = averageCenter;
	}
	
	public void setScaleLevel(int scaleLevel){
		this.maxGridSize = gridSize*ratio[scaleLevel-1];
		this.clear();
	}
	
	public void setGridSizeWithMi(double mi){
		this.maxGridSize = mi;
	}
	public void addPoint(String id,double lo,double la){
		PointInfo pInfo = new PointInfo(id,lo,la);
		Cluster cluster = null,minCluster = null;
		double curDistance,minDistance = Double.MAX_VALUE;
		for(Iterator<Cluster> itr = this.clusterList.iterator();itr.hasNext();){
			cluster = itr.next();
			
			curDistance = countDistance(cluster.clo,cluster.cla,lo,la);
			if(curDistance < minDistance){
				minDistance  = curDistance;
				minCluster = cluster;
			}
		}
		
		if(minDistance < this.maxGridSize){
			minCluster.addPoint(pInfo);
		}else{
			cluster = new Cluster();
			cluster.addPoint(pInfo);
			this.clusterList.add(cluster);
		}
	}
	public List getClusterPoint(){
		return this.clusterList;
	}
	
	public void clear(){
		this.clusterList.clear();
	}
	
	public void printInfo(){
		System.out.println("ClusterNum:"+this.clusterList.size());
		Cluster cluster;
		for(Iterator<Cluster> itr=this.clusterList.iterator();itr.hasNext();){
			cluster = itr.next();
			System.out.println(cluster.toString());
		}
	}
	
	private static double EARTH_RADIUS = 6378.137;
	private static double rad(double d)
	 {
	  return d * Math.PI / 180.0;
	 }
	 /**
	  * 计算两个点(经纬度)的实际距离，单位米。
	  * added by edmund
	  * @param lat1
	  * @param lng1
	  * @param lat2
	  * @param lng2
	  * @return
	  */
	 public static double countDistance(double lo1, double la1, double lo2,
	   double la2)
	 {
	  double radLat1 = rad(la1);
	  double radLat2 = rad(la2);
	  double a = radLat1 - radLat2;
	  double b = rad(lo1) - rad(lo2);
	  double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2)
	    + Math.cos(radLat1) * Math.cos(radLat2)
	    * Math.pow(Math.sin(b / 2), 2)));
	  s = s * EARTH_RADIUS;
	  s = Math.round(s * 100000) / 100.0;
	  return s;
	 }
	
	public class Cluster{
		public double clo,cla;
		public List pList = new LinkedList();

		public void addPoint(PointInfo pInfo){
			if(this.pList.isEmpty()){
				this.clo = pInfo.lo;
				this.cla = pInfo.la;
			}else{
				if(averageCenter){
					int num = this.pList.size();
					this.clo = (this.clo*num+pInfo.lo)/(num+1);
					this.cla = (this.cla*num+pInfo.la)/(num+1);
				}
			}
			this.pList.add(pInfo);
		}
		
		public String toString(){
			NumberFormat format = NumberFormat.getNumberInstance();
			format.setMaximumFractionDigits(6);
			StringBuffer strBuff = new StringBuffer(256);
			strBuff.append("Cluster["+this.pList.size()+"]:");
			strBuff.append("center:"+format.format(clo)+","+format.format(cla));
			PointInfo pInfo;
			for(Iterator itr = this.pList.iterator();itr.hasNext();){
				pInfo = (PointInfo)itr.next();
				strBuff.append("\n"+pInfo.id+","+format.format(pInfo.lo)+","+format.format(pInfo.la));
			}
			
			return strBuff.toString();
		}
	}
	
	public class PointInfo{
		public String id = null;
		public double lo,la;
		
		public PointInfo(String id,double lo,double la){
			this.id = id;
			this.lo = lo;
			this.la = la;
		}
	}
	
	
	public static void main(String[] argv){
		GISMarkClusterInstance instance = new GISMarkClusterInstance();
		instance.setScaleLevel(12);
		
		instance.addPoint("1", 121.31, 31.4567);
		instance.addPoint("1", 121.32, 31.4567);
		instance.addPoint("1", 121.33, 31.4567);
		instance.addPoint("1", 121.34, 31.4567);
		instance.addPoint("1", 121.35, 31.4567);
		instance.addPoint("1", 121.36, 31.4567);
		
		instance.printInfo();
	}
}
