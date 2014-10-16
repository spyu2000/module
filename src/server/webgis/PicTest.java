package server.webgis;

import java.awt.Color;
import java.awt.Point;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;

public class PicTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		BufferedReader reader = new BufferedReader(new FileReader("c:/data.txt"));
		String str ;
		String[] arr;
		ArrayList list = new ArrayList(128);
		double maxLo = 0,minLo=0,maxLa=0,minLa=0;
		double[] dArr;
		while((str = reader.readLine()) != null){
			arr = str.split("\t");
			dArr = new double[]{Double.parseDouble(arr[0]),-Double.parseDouble(arr[1])};
			if(list.size()==0){
				minLo = maxLo = dArr[0];
				minLa = maxLa = dArr[1];
			}else{
				maxLo = Math.max(maxLo, dArr[0]);
				minLo = Math.min(minLo, dArr[0]);
				maxLa = Math.max(maxLa, dArr[1]);
				minLa = Math.min(minLa, dArr[1]);
			}
			list.add(dArr);
		}
		reader.close();
		
		
		int width = 1800,height = 2000;
		int scale = WebgisPhotoServer.getFixedScaleLevel(maxLo-minLo, maxLa-minLa, width, height);
		System.out.println((maxLo-minLo)+" "+(maxLa-minLa)+" "+scale);
		WebgisPhotoServer.getSingleInstance().addPara("webgis_pic_addr",
				"http://61.152.124.150:5226/webgis/jsp/interface/gis_pic.jsp");
		WebgisPhotoServer.getSingleInstance().addPara("user", "xjs");
		WebgisPhotoServer.getSingleInstance().addPara("pwd", "_xjs");
		WebgisPhotoServer.getSingleInstance().startServer();
		
		int gridStepX = 50,gridStepY = 50;
		int gridX = width / gridStepX;
		int gridY = height / gridStepY;
		Point p;
		WebgisPhotoInfo photoInfo = WebgisPhotoServer.getSingleInstance().getWegGisPhotoInfo((maxLo+minLo)/2,(maxLa+minLa)/2,width,height,scale);
		for(int i=1;i<gridX;i++){
			photoInfo.drawLine(i*gridStepX, 0, i*gridStepX, height, Color.BLACK, 1);
		}
		for(int i=1;i<gridY;i++){
			photoInfo.drawLine(0, i*gridStepY, width, i*gridStepY, Color.BLACK, 1);
		}
		int[][] gridArr = new int[gridX][gridY];
		for(int i=0;i<list.size();i++){
			dArr = (double[])list.get(i);
			p = photoInfo.drawPoint(dArr[0], dArr[1], Color.RED, 3);
			gridArr[p.x/gridStepX][p.y/gridStepY] ++;
		}
		
		for(int i=0;i<gridArr.length;i++){
			for(int j=0;j<gridArr[0].length;j++){
				if(gridArr[i][j] == 0){
					continue;
				}
				photoInfo.drawString(gridArr[i][j]+"", i*gridStepX+gridStepX/2, j*gridStepY+gridStepY/2, Color.red, 16, 0, 0);
				
			}
		}
		photoInfo.save("c:/a.png");
	}

}
