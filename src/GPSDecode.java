import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.*;
import java.util.zip.GZIPInputStream;
public class GPSDecode {
	/**
	 * ½øÐÐGPSÆ«²î
	 * @param argv
	 * @throws Exception
	 */
	public static void main(String[] argv) throws Exception{
//		GPSDecode.decode(118.3414, 30.671);
//		GPSDecode.decode(122.0135, 32.671);
		
		GPSDecode.initFromText();
		
		double loStart = 109.6504 ,loEnd = 117.2147,laStart = 20.208,laEnd = 25.53;
		loStart = 0;
		loEnd = 180;
		laStart = 0;
		laEnd = 90;

		int loStartInt = (int)Math.round((loStart+180)*10);
		int loEndInt = (int)Math.round((loEnd+180)*10);
		int laStartInt = (int)Math.round((laStart+90)*10);
		int laEndInt = (int)Math.round((laEnd+90)*10);
		
		Integer keyId;
		short loInt,laInt;
		Integer value;
		BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream("baidu_data.gps"));
		ByteBuffer buff = ByteBuffer.allocate(8);
		buff.order(ByteOrder.LITTLE_ENDIAN);
		for(Iterator itr = mapping.keySet().iterator();itr.hasNext();){
			keyId = (Integer)itr.next();
			loInt = (short)(keyId.intValue()>>16);
			laInt = (short)(keyId.intValue()&0xFFFF);
			
			value = (Integer)mapping.get(new Integer(keyId)); 
//			System.out.println((short)(value&0xFFFF));
			buff.clear();
			buff.putInt(keyId);
			buff.putShort((short)(value>>16));
			buff.putShort((short)(value&0xFFFF));
			if(loInt >= loStartInt && loInt<= loEndInt && laInt>=laStartInt && laInt <= laEndInt){
				out.write(buff.array());
			}
		}
		out.close();
	}
	public static double[] encode(double loIn,double laIn){
		int keyId = ((((int)(((loIn+180)*10)+0.5))<<16)|((int)(((laIn+90)*10)+0.5)));
	    Integer value = (Integer)mapping.get(new Integer(keyId));      
	
		if(value == null){
			return new double[]{loIn,laIn};
		}else{
			int offsetLo = (short)(value>>16);
			int offsetLa = (short)(value&0xFFFF);
			return new double[]{loIn + offsetLo/1000000.0,laIn + offsetLa/1000000.0};
		}
	}
	public static double[] decode(double loIn,double laIn){
		int keyId = ((((int)(((loIn+180)*10)+0.5))<<16)|((int)(((laIn+90)*10)+0.5)));
	    Integer value = (Integer)mapping.get(new Integer(keyId));    
	
		if(value == null){
			return new double[]{loIn,laIn};
		}else{
			int offsetLo = (short)(value>>16);
			int offsetLa = (short)(value&0xFFFF);
			return new double[]{loIn - offsetLo/1000000.0,laIn - offsetLa/1000000.0};
		}
	}
	
	private static HashMap mapping = new HashMap();
	private static void putData(int areaId,int offsetLo,int offsetLa){
		int value = (short)offsetLo;
		value = (value<<16) | (((short)offsetLa)&0xFFFF);
		mapping.put(new Integer(areaId),new Integer(value));
	}
	
	public static void init(){
		try{			
			BufferedInputStream in = new BufferedInputStream(new FileInputStream("./data.gps"));
			ByteBuffer buff = ByteBuffer.allocate(8);
			buff.order(ByteOrder.LITTLE_ENDIAN);
			int count = in.read(buff.array());
			while(count == buff.capacity()){
				GPSDecode.putData(buff.getInt(0), buff.getShort(4), buff.getShort(6));
				
				count = in.read(buff.array());
			}
			in.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public static void initFromText(){
		try{
			BufferedReader in = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream("./baidu_gpsdata.zip"))));
			String str = in.readLine();
			String[] arr = null;
			while(str != null){
				arr = str.split(",");
				GPSDecode.putData(Integer.parseInt(arr[0]), Integer.parseInt(arr[1]), Integer.parseInt(arr[2]));
				
				str = in.readLine();
			}
			in.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	static{
//		GPSDecode.init();
	}
}
