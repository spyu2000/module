package server.help.poly;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;

import com.fleety.server.BasicServer;

public class ColorationSelectionServer extends BasicServer {
	public static final String MID_PATH_FLAG = "mid_path";
	public static final String MIF_PATH_FLAG = "mif_path";
	
	private ArrayList regionList = new ArrayList(16);
	
	public boolean startServer() {
		try{
			long t = System.currentTimeMillis();
			
			this.initRegion();
			
			System.out.println("Init Cost Time:"+(System.currentTimeMillis()-t)+" Region Num:"+this.regionList.size());
			t = System.currentTimeMillis();
			
			this.countNeighbour();
			System.out.println("Neignbour Cost Time:"+(System.currentTimeMillis()-t));
			t = System.currentTimeMillis();
			
			this.countColor();
			System.out.println("Color Count Cost Time:"+(System.currentTimeMillis()-t));
			
		}catch(Exception e){
			e.printStackTrace();
		}
		return this.isRunning();
	}
	
	private void initRegion() throws Exception{
		String tempStr = this.getStringPara(MIF_PATH_FLAG);
		BufferedReader reader = new BufferedReader(new FileReader(tempStr));
		
		boolean isDataStart = false;
		int pointNum = -2;
		RegionInfo region = null;
		String[] arr;
		while((tempStr = reader.readLine()) != null){
			if(tempStr.equals("DATA")){
				isDataStart = true;
				continue;
			}
			if(!isDataStart){
				continue;
			}
			if(pointNum == -2){
				pointNum = -1;
				continue;
			}
			if(pointNum == -1){
				pointNum = Integer.parseInt(tempStr.trim());
				region = new RegionInfo(regionList.size(),pointNum);
				regionList.add(region);
				continue;
			}
			
			arr = tempStr.split(" ");
			region.addPoint(new Point2D.Double(Double.parseDouble(arr[0]),Double.parseDouble(arr[1])));
			
			if(region.pointList.size() == pointNum){
				tempStr = reader.readLine();
				pointNum = -2;
			}
		}
		
		reader.close();
	}
	
	private void countNeighbour(){
		RegionInfo o1,o2;
		for(int i=0;i<this.regionList.size();i++){
			o1 = (RegionInfo)this.regionList.get(i);
			for(int j=i+1;j<this.regionList.size();j++){
				o2 = (RegionInfo)this.regionList.get(j);
				
				if(o1.isNeighbour(o2)){
					o1.neighbourRegion.add(o2);
					o2.neighbourRegion.add(o1);
				}
			}
		}
	}
	
	private int fillCount = 0;
	private int maxColorNum = 4;
	private int maxArriveIndex = 0;
	private void countColor(){
		this.maxArriveIndex = -1;
		ArrayList colorIndexList = new ArrayList(this.regionList.size());
		this.fillColor(colorIndexList,0);
		while(true){
			if(colorIndexList.size() > maxArriveIndex){
				this.maxArriveIndex = colorIndexList.size();
				System.out.println("Max Arrive Index:"+this.maxArriveIndex);
			}
			if(colorIndexList.size() == this.regionList.size()){
				break;
			}else{
				if(colorIndexList.size() == 0){
					System.out.println("Can't Finish Color Fill");
					return ;
				}
				Integer colorIndex = (Integer)colorIndexList.remove(colorIndexList.size()-1);
				this.fillColor(colorIndexList, colorIndex+1);
			}
		}
		System.out.println("Fill Count:"+this.fillCount);
	}
	
	private void fillColor(ArrayList colorIndexList,int startIndex){
		if(colorIndexList.size() == this.regionList.size()){
			System.out.println("Finish Color Fill");
			
			this.printIndex(colorIndexList);
			return ;
		}
		fillCount ++;
		boolean isOk = false;
		for(int i=startIndex;i<this.maxColorNum;i++){
			if(this.isOk(i, colorIndexList)){
				isOk = true;
				colorIndexList.add(new Integer(i));
				break;
			}
		}
		if(isOk){
			this.fillColor(colorIndexList, 0);
		}else{
//			if(colorIndexList.size() == 0){
//				System.out.println("Can't Finish Color Fill");
//				return ;
//			}
//			Integer colorIndex = (Integer)colorIndexList.remove(colorIndexList.size()-1);
//			this.fillColor(colorIndexList, colorIndex+1);
		}
	}
	
	private void printIndex(ArrayList colorIndexList){
		for(int i=0;i<colorIndexList.size();i++){
			System.out.println("Region "+i+" ColorIndex="+colorIndexList.get(i));
		}
	}
	
	private boolean isOk(int colorIndex,ArrayList colorIndexList){
		int nextRegionIndex = colorIndexList.size();
		RegionInfo region = (RegionInfo)this.regionList.get(nextRegionIndex),nRegion;
		for(int i=0;i<region.neighbourRegion.size();i++){
			nRegion = (RegionInfo)region.neighbourRegion.get(i);
			if(nRegion.index < region.index){
				if(((Integer)colorIndexList.get(nRegion.index)).intValue() == colorIndex){
					return false;
				}
			}
		}
		
		return true;
	}

	public void stopServer(){
		super.stopServer();
	}
	
	private class RegionInfo{
		public ArrayList pointList = null;
		public ArrayList neighbourRegion = new ArrayList(8);
		public Rectangle2D.Double outerRect = null;
		public int index = -1;
		
		public RegionInfo(int index,int pointNum){
			this.index = index;
			this.pointList = new ArrayList(pointNum);
		}
		
		public void addPoint(Point2D.Double p){
			this.pointList.add(p);
			
			if(this.pointList.size() == 2){
				Point2D.Double o1 = (Point2D.Double)this.pointList.get(0);
				Point2D.Double o2 = (Point2D.Double)this.pointList.get(1);
				
				double minlo,minla,maxlo,maxla;
				minlo = Math.min(o1.x, o2.x);
				minla = Math.min(o1.y, o2.y);
				maxlo = Math.max(o1.x, o2.x);
				maxla = Math.max(o1.y, o2.y);
				outerRect = new Rectangle2D.Double(minlo,minla,maxlo-minlo,maxla-minla);
			}else if(this.pointList.size() > 2){
				double minlo,minla,maxlo,maxla;
				minlo = Math.min(outerRect.x, p.x);
				minla = Math.min(outerRect.y, p.y);
				maxlo = Math.max(outerRect.x + outerRect.width, p.x);
				maxla = Math.max(outerRect.y + outerRect.height, p.y);
				outerRect = new Rectangle2D.Double(minlo,minla,maxlo-minlo,maxla-minla);
			}
		}
		
		public boolean isNeighbour(RegionInfo region){
			if(!region.outerRect.intersects(this.outerRect)){
				return false;
			}
			
			Point2D.Double o1,o2;
			for(int i=0;i<this.pointList.size();i++){
				o1 = (Point2D.Double)this.pointList.get(i);
				for(int j=0;j<region.pointList.size();j++){
					o2 = (Point2D.Double)region.pointList.get(j);
					
					if(Math.abs(o1.x-o2.x) < 0.001 && Math.abs(o1.y-o2.y) < 0.001){
						return true;
					}
				}
			}
			return false;
		}
	}
	
	
	public static void main(String[] argv){
		ColorationSelectionServer server = new ColorationSelectionServer();
		
		server.addPara(MIF_PATH_FLAG, "c:/bou.mif");
		
		server.startServer();
	}
}
