package server.net.updator.client;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.net.Socket;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import server.help.SingleServer;
import server.net.updator.IUpdator;

import com.fleety.base.StrFilter;
import com.fleety.base.Util;
import com.fleety.server.BasicServer;

public class AutoUpdatorClient extends BasicServer implements IUpdator{
	public static final String CFG_SERVER_IP = "sip";
	public static final String CFG_SERVER_PORT = "sport";
	public static final String CFG_MAIN_PROCESS_START_SHELL = "main_process_start";
	public static final String CFG_VERSION_FILE = "version_file";
	public static final String CFG_VERSION_CLS_NAME = "version_cls_name";
	public static final String CFG_VERSION_FIELD_NAME = "version_field_name";
	
	
	private final String UPDATE_STATUS_FLAG = "_update_flag_";
	private final String UPDATE_DIR_FLAG = "client_update";
	private final String UPDATE_NOTIFY_FILE_NAME = "_notify_info_";
	private String curVersion = null;
	private StringBuffer updateInfo = new StringBuffer();
	
	public void updateAndStartClient() throws Exception{
		if(this.getUpdateFlagFile().exists()){
			this.curVersion = "0.0.0";
		}
		this.updateClient();
		this.startMainProcess();
	}
	
	public boolean updateClient() throws Exception{
		Socket socket = null;
		try{			
			socket = new Socket(this.getStringPara(CFG_SERVER_IP),this.getIntegerPara(CFG_SERVER_PORT).intValue());
			InputStream in = socket.getInputStream();
			OutputStream out = socket.getOutputStream();
			String[] infoArr = this.sendVersionCompare(in,out);
			if(infoArr != null){
				String version = this.update(in,out,infoArr);
				if(version != null){
					this.updateInfo.append("软件已更新到["+version+"]!");
					this.clearUpdateDir();
				}else{
					this.updateInfo.append("新版本更新失败");
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			if(socket != null){
				socket.close();
			}
		}
		return true;
	}
	
	private String[] sendVersionCompare(InputStream in,OutputStream out) throws Exception{
		byte[] versionArr = curVersion.getBytes();
		ByteBuffer buff = ByteBuffer.allocate(9+8+2+versionArr.length);
		buff.order(ByteOrder.LITTLE_ENDIAN);
		buff.put((byte)IUpdator.U_VERSION_COMPARE_MSG);
		buff.putLong(buff.capacity()-9);
		buff.putLong(-1);
		buff.putShort((short)versionArr.length);
		buff.put(versionArr);
		
		out.write(buff.array());
		
		ByteBuffer head = ByteBuffer.allocate(9);
		head.order(ByteOrder.LITTLE_ENDIAN);
		Util.readFull(in, head.array(), 0, head.capacity());
		
		if((head.get(0)&0xFF) != IUpdator.D_VERSION_COMPARE_RES){
			return null;
		}
		
		int size = (int)head.getLong(1);
		ByteBuffer data = ByteBuffer.allocate(size);
		data.order(ByteOrder.LITTLE_ENDIAN);
		Util.readFull(in, data.array(),0,data.capacity());
		int infoLen = data.getShort(0)&0xFFFF;
		String str = new String(data.array(),2,infoLen);
		

		System.out.println("Version Compare:"+str);
		
		String[] infoArr = StrFilter.split(str,"\t");
		if(infoArr.length < 2 || infoArr[0].equals(this.curVersion)){
			return null;
		}
		return infoArr;
	}
	
	private String update(InputStream in,OutputStream out,String[] infoArr) throws Exception{
		String version = infoArr[0];
		
		for(int i=1;i<infoArr.length;i++){
			if(!this.downloadOneFile(in,out,version,infoArr[i])){
				return null;
			}
		}
		
		this.getUpdateFlagFile().createNewFile();
		
		File srcFile,destFile;
		for(int i=1;i<infoArr.length;i++){
			srcFile = new File(this.getUpdateDir(),Util.getFilePathReg(infoArr[i]));
			destFile = new File(".",Util.getFilePathReg(infoArr[i]));
			destFile.getParentFile().mkdirs();
			
			System.out.println("Rename:"+ srcFile.getAbsolutePath()+" "+destFile.getAbsolutePath());
			if(destFile.exists()){
				if(!destFile.delete()){
					return null;
				}
			}
			if(!srcFile.renameTo(destFile)){
				return null;
			}
		}
		
		return version;
	}
	private boolean downloadOneFile(InputStream in,OutputStream out,String version,String filePath) throws Exception{
		File updateDir = this.getUpdateDir();
		File df = new File(updateDir,Util.getFilePathReg(filePath));
		df.getParentFile().mkdirs();

		StringBuffer strBuff = new StringBuffer(256);
		strBuff.append(version);
		strBuff.append("\t");
		strBuff.append(filePath);
		strBuff.append("\t");
		if(df.exists()){
			strBuff.append(""+df.lastModified());
			strBuff.append("\t");
			strBuff.append(""+df.length());
		}else{
			strBuff.append("-1");
			strBuff.append("\t");
			strBuff.append("-1");
		}
		
		byte[] infoArr = strBuff.toString().getBytes();
		ByteBuffer buff = ByteBuffer.allocate(9+8+2+infoArr.length);
		buff.order(ByteOrder.LITTLE_ENDIAN);
		buff.put((byte)IUpdator.U_FILE_DOWNLOAD_MSG);
		buff.putLong(buff.capacity()-9);
		buff.putLong(-1);
		buff.putShort((short)infoArr.length);
		buff.put(infoArr);
		
		out.write(buff.array());
		
		ByteBuffer head = ByteBuffer.allocate(9);
		head.order(ByteOrder.LITTLE_ENDIAN);
		Util.readFull(in, head.array(), 0, head.capacity());
		
		if((head.get(0)&0xFF) != IUpdator.D_FILE_DOWNLOAD_RES){
			System.out.println("Error Download Update Msg "+(head.get(0)&0xFF));
			return false;
		}
		
		int size = (int)head.getLong(1);
		ByteBuffer data = ByteBuffer.allocate(size);
		data.order(ByteOrder.LITTLE_ENDIAN);
		Util.readFull(in, data.array(), 0, data.capacity());
		
		long lastModified = data.getLong();
		int len = data.get()&0xFF;
		String path = new String(data.array(),data.position(),len);
		data.position(data.position()+len);
		
		int dflag = data.get()&0xFF;
		if(dflag != 0 && dflag != 1){
			System.out.println("Download Update Failure");
			return false;
		}
		
		if(dflag == 0){
			File f = new File(this.getUpdateDir(),Util.getFilePathReg(path));
			OutputStream fout = new BufferedOutputStream(new FileOutputStream(f));
			
			fout.write(data.array(),data.position(),data.remaining());
			
			fout.close();
			
			f.setLastModified(lastModified);
		}else if(dflag == 1){
			System.out.println("File has Updated!");
		}
		
		System.out.println("Version:"+version+" Update File "+filePath+" Success!");
		
		return true;
	}
	
	public void startMainProcess(){
		try{
			if(this.updateInfo.length() > 0){
				File f = new File(UPDATE_NOTIFY_FILE_NAME);
				FileOutputStream out = new FileOutputStream(f);
				out.write(this.updateInfo.toString().getBytes());
				out.close();
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		
		System.out.println("Start Main Process");
		try{
			singleServer.stopServer();
			
			String startShell = this.getStringPara(CFG_MAIN_PROCESS_START_SHELL);
			Runtime.getRuntime().exec("cmd /c start "+startShell, null, new File("."));
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	private File getUpdateFlagFile(){
		return new File(this.getUpdateDir(),UPDATE_STATUS_FLAG);
	}
	private File getUpdateDir(){
		return new File(UPDATE_DIR_FLAG);
	}
	
	private void clearUpdateDir(){
		this.deleteDir(this.getUpdateDir());
	}
	private void deleteDir(File dir){
		File[] cArr = dir.listFiles();
		
		for(int i=0;i<cArr.length;i++){
			if(cArr[i].isFile()){
				cArr[i].delete();
			}else if(cArr[i].isDirectory()){
				this.deleteDir(cArr[i]);
			}
		}
		
		dir.delete();
	}
	
	private static SingleServer singleServer = null;	
	public boolean startServer() {
		try{
			System.out.println("Start ......");
			//自身不能重复启动
			singleServer = new SingleServer();
			singleServer.addPara("single_port", "start_port");
			singleServer.startServer();

			//如果主程序启动，不能启动
			singleServer = new SingleServer();
			singleServer.startServer();

			System.out.println("Start Update");
			//检测更新后启动程序
			this.getVersion();
			if(this.curVersion != null){
				this.updateAndStartClient();
			}
			System.out.println("End Update");
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return true;
	}

	public String getVersion(){
		String versionJarFilePath = this.getStringPara(CFG_VERSION_FILE);
		String versionClsName = this.getStringPara(CFG_VERSION_CLS_NAME);
		String versionFieldName = this.getStringPara(CFG_VERSION_FIELD_NAME);
		
		try{
			File srcFile = new File(versionJarFilePath);
			if(!srcFile.exists()){
				this.curVersion = null;
				return this.curVersion;
			}
			File destFile = new File(srcFile.getParentFile(),srcFile.getName()+".vbak");
			if(!destFile.exists() || destFile.lastModified() != srcFile.lastModified() || destFile.length() != srcFile.length()){
				byte[] arr = new byte[(int)srcFile.length()];
				
				FileInputStream in = null;
				FileOutputStream out = null;
				try{
					in = new FileInputStream(srcFile);
					Util.readFull(in, arr, 0, arr.length);
					out = new FileOutputStream(destFile);
					out.write(arr);
					out.close();
				}catch(Exception e){
					e.printStackTrace();
				}finally{
					if(in != null){
						try{
							in.close();
						}catch(Exception e){}
					}
					if(out != null){
						try{
							out.close();
						}catch(Exception e){}
					}
				}
			}
			
			URL url = destFile.toURI().toURL();
			URLClassLoader loader = URLClassLoader.newInstance(new URL[]{url});
			Class cls = Class.forName(versionClsName,false,loader);
			Field field = cls.getField(versionFieldName);
			this.curVersion = (String)field.get(cls);
		}catch(Exception e){
			e.printStackTrace();
			this.curVersion = "0.0.0";
		}
		System.out.println("Cur Version:"+this.curVersion);
		
		return this.curVersion;
	}
}
