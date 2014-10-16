package server.administrative;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.fleety.base.Util;
import com.fleety.base.shape.JudgeServer;
import com.fleety.base.shape.java.JudgeActionForJava;
import com.fleety.base.xml.XmlParser;
import com.fleety.server.BasicServer;

public class AdministrativeRegionServer extends BasicServer {
	public static final String ADMINISTRATIVE_INFO_PATH_KEY = "administrative_info_path";
	public static final String ADMINISTRATIVE_DATA_PATH_KEY = "administrative_data_path";
	public static final String LOAD_ADMINISTRATIVE_KEY = "load_administrative";
	
	private static AdministrativeRegionServer singleInstance = new AdministrativeRegionServer();
	public static AdministrativeRegionServer getSingleInstance(){
		return singleInstance;
	}

	private JudgeServer judgeServer = null;
	private HashMap adminMapping = new HashMap();
	private HashMap id2CodeMapping = new HashMap();
	private AdminInfo rootInfo = new AdminInfo("000000","δ֪");
	public boolean startServer() {
		String tempStr = null;
		
		try{
			tempStr = this.getStringPara(ADMINISTRATIVE_INFO_PATH_KEY);
			if(tempStr != null && tempStr.trim().length() > 0){
				this.loadAdministrativeInfo(tempStr.trim());
			}
			tempStr = this.getStringPara(ADMINISTRATIVE_DATA_PATH_KEY);
			if(tempStr != null && tempStr.trim().length() > 0){
				this.loadAdministrativeData(tempStr.trim(),this.getStringPara(LOAD_ADMINISTRATIVE_KEY));
			}
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}
		
		this.isRunning = true;
		return this.isRunning();
	}
	
	public String getAdministrativeCode(double lo,double la){
		if(!this.isRunning()){
			return null;
		}
		if(this.judgeServer == null){
			return null;
		}
		int id = this.judgeServer.getArea(lo, la);
		if(id < 0){
			return null;
		}
		return (String)this.id2CodeMapping.get(new Integer(id));
	}
	public AdminInfo getAdminitrativeInfo(String code){
		if(!this.isRunning()){
			return null;
		}
		return (AdminInfo)adminMapping.get(code);
	}
	
	private void loadAdministrativeInfo(String dataPath) throws Exception{
		File f = new File(dataPath);
		if(!f.exists() || !f.isFile()){
			throw new Exception("Error Administrative Info!");
		}
		
		Document doc = XmlParser.parse(f);
		this.loadCom(doc.getDocumentElement(),this.rootInfo);
		
	}
	private void loadCom(Node parentNode,AdminInfo parentInfo){
		Node[] subNodeArr = Util.getSonElementsByTagName(parentNode, "com");
		if(subNodeArr == null || subNodeArr.length == 0){
			return ;
		}
		
		AdminInfo subInfo ;
		Node node;
		for(int i=0;i<subNodeArr.length;i++){
			node = subNodeArr[i];
			
			subInfo = new AdminInfo(Util.getNodeAttr(node, "code"),Util.getNodeAttr(node, "name"));
			this.adminMapping.put(subInfo.getCode(), subInfo);
			if(parentInfo != null){
				parentInfo.addChild(subInfo);
			}
			
			this.loadCom(node,subInfo);
		}
	}
	
	private int adminId = 1;
	private void loadAdministrativeData(String dataPath,String loadAdministrative) throws Exception{
		File f = new File(dataPath);
		if(!f.exists() || !f.isFile()){
			throw new Exception("Error Administrative Data!");
		}
		
		boolean hasSpecialLoad = true;
		if(loadAdministrative == null || loadAdministrative.trim().length() == 0){
			hasSpecialLoad = false;
		}
		loadAdministrative = ","+loadAdministrative+",";
		
		this.judgeServer = new JudgeServer();
		this.judgeServer.setAction(new JudgeActionForJava());
		
		int pos = 0;
		BufferedInputStream in = new BufferedInputStream(new FileInputStream(f));
		int catagoryNum = in.read();
		catagoryNum |= in.read()<<8;
		catagoryNum |= in.read()<<16;
		catagoryNum |= in.read()<<24;
		pos += 4;
		
		ByteBuffer headBuff = ByteBuffer.allocate(16*catagoryNum);
		headBuff.order(ByteOrder.LITTLE_ENDIAN);
		int count;
		while(headBuff.hasRemaining()){
			count = in.read(headBuff.array(),headBuff.position(),headBuff.remaining());
			if(count < 0){
				throw new Exception("File Format Error1!");
			}
			headBuff.position(headBuff.position()+count);
		}
		pos += headBuff.capacity();
		
		ByteBuffer dataBuff = null;
		String code;
		int offset,len,skip;
		for(int i=0;i<catagoryNum;i++){
			headBuff.position(16*i);
			code = new String(headBuff.array(),headBuff.position(),8).trim();
			headBuff.position(headBuff.position()+8);
			offset = headBuff.getInt();
			len = headBuff.getInt();
			if(hasSpecialLoad && loadAdministrative.indexOf(","+code+",") < 0){
				while(len > 0){
					skip = (int)in.skip(len);
					if(skip < 0){
						throw new Exception("File Format Error2!");
					}
					len -= skip;
				}
				continue;
			}
			
			if(dataBuff == null || dataBuff.capacity() < len){
				dataBuff = ByteBuffer.allocate(len);
				dataBuff.order(ByteOrder.LITTLE_ENDIAN);
			}
			dataBuff.clear();
			while(dataBuff.position() < len){
				count = in.read(dataBuff.array(),dataBuff.position(),len - dataBuff.position());
				if(count < 0){
					throw new Exception("File Format Error3!");
				}
				dataBuff.position(dataBuff.position()+count);
			}
			dataBuff.flip();
			
			this.loadOneCatagory(dataBuff);
		}
	}
	
	private void loadOneCatagory(ByteBuffer buff){
		int codeLen;
		String code;
		int pNum;
		int infoNum = buff.getInt();
		double[] lo;
		double[] la;
		for(int i=0;i<infoNum;i++){
			codeLen = buff.get()&0xFF;
			code = new String(buff.array(),buff.position(),codeLen);
			buff.position(buff.position()+codeLen);
			pNum = buff.getInt();
			lo = new double[pNum];
			la = new double[pNum];
			for(int j=0;j<pNum;j++){
				lo[j] = buff.getInt()/1000000.0;
				la[j] = buff.getInt()/1000000.0;
			}
			this.judgeServer.addShape(this.adminId, lo, la, JudgeServer.AREA_FLAG);
			this.id2CodeMapping.put(new Integer(this.adminId), code);
			this.adminId ++;
		}
	}

	public void stopServer(){
		super.stopServer();
	}
	
	public class AdminInfo{
		private String code = null;
		private String name = null;
		private AdminInfo pInfo = null;
		public AdminInfo(String code,String name){
			this.code = code;
			this.name = name;
		}
		
		public String getCode(){
			return this.code;
		}
		public String getName(){
			return this.name;
		}
		public AdminInfo getParent(){
			return this.pInfo;
		}
		public void updateParent(AdminInfo pInfo){
			this.pInfo = pInfo;
		}
		public String getFullName(){
			if(this.pInfo == null){
				return "";
			}
			return this.pInfo.getFullName()+this.name;
		}
		
		private ArrayList subList = null;
		public void addChild(AdminInfo subInfo){
			if(subList == null){
				this.subList = new ArrayList(16);
			}
			this.subList.add(subInfo);
			subInfo.updateParent(this);
		}
	}
	
	public static void main(String[] argv) throws Exception{
		AdministrativeRegionServer server = new AdministrativeRegionServer();
		server.addPara("administrative_info_path","resource/administrative_region.xml");
		server.addPara("administrative_data_path","resource/administrative_geo.data");
		server.addPara("load_administrative", "");
		long t = System.currentTimeMillis();
		server.startServer();
		System.out.println(System.currentTimeMillis() - t);
		
		t = System.currentTimeMillis();
		
		String code = server.getAdministrativeCode(118.5044, 31.1448);
		AdminInfo info = server.getAdminitrativeInfo(code);
		System.out.println(code+"   "+(info!=null?info.getFullName():"null"));
		
		System.out.println("time="+(System.currentTimeMillis()-t));
		
	}
}
