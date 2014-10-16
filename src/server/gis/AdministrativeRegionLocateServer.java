package server.gis;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;

import com.fleety.base.shape.JudgeServer;
import com.fleety.server.BasicServer;

public class AdministrativeRegionLocateServer extends BasicServer {
	private static final AdministrativeRegionLocateServer singleInstance = new AdministrativeRegionLocateServer();
	private AdministrativeRegionLocateServer(){
		
	}
	
	public static AdministrativeRegionLocateServer getSingleInstance(){
		return singleInstance;
	}
	
	private HashMap id2Name = null;
	private JudgeServer server = null;
	public boolean startServer() {
		String tempStr = this.getStringPara("data_path");
		if(tempStr == null || tempStr.trim().length() == 0){
			return false;
		}
		File dataFile = new File(tempStr.trim());
		if(!dataFile.exists()){
			return false;
		}
		if(dataFile.isDirectory()){
			return false;
		}
		
		this.id2Name = new HashMap();
		this.server = new JudgeServer();
		BufferedInputStream in = null;
		try{
			in = new BufferedInputStream(new FileInputStream(dataFile));
			int len = (int)dataFile.length();
			ByteBuffer data = ByteBuffer.allocate(len);
			data.order(ByteOrder.LITTLE_ENDIAN);
			
			while(data.hasRemaining()){
				if(in.read(data.array(), data.position(), data.remaining()) < 0){
					throw new Exception("读取错误");
				}
			}
			in.close();
			
			int id = 1;
			while(this.loadOneRegion(data,id++)){
				
			}
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}finally{
			if(in != null){
				try{
					in.close();
				}catch(Exception e){}
			}
		}
		
		this.isRunning = true;
		return this.isRunning();
	}
	
	private boolean loadOneRegion(ByteBuffer data,int id){
		if(!data.hasRemaining()){
			return false;
		}
		int len = data.get()&0xFF;
		if(data.remaining() < len){
			System.out.println("数据不完整!");
			return false;
		}
		String name = new String(data.array(),data.position(),len);
		data.position(data.position()+len);
		
		int pNum = data.getShort();
		if(data.remaining() < 8){
			System.out.println("数据不完整!");
			return false;
		}
		double[] loArr = new double[pNum];
		double[] laArr = new double[pNum];
		int count = 0;
		while(count < pNum){
			loArr[count] = data.getInt()/1000000.0;
			laArr[count] = data.getInt()/1000000.0;
			
			count++;
		}
		this.server.addShape(id, loArr, laArr, JudgeServer.AREA_FLAG);
		this.id2Name.put(new Integer(id), name);
		
		return true;
	}
	
	public void stopServer(){
		super.stopServer();
		this.id2Name = null;
		this.server.clearShape();
	}

	public String getRegionCode(double lo,double la){
		if(!this.isRunning()){
			return null;
		}
		int id = this.server.getArea(lo, la);
		return (String)this.id2Name.get(new Integer(id));
	}
}
