package server.proxy.socket.help;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.HashMap;

import com.fleety.server.BasicServer;

public class SocketProxyRecordServer extends BasicServer {
	private static final SocketProxyRecordServer singleInstance = new SocketProxyRecordServer();
	public static SocketProxyRecordServer getSingleInstance(){
		return singleInstance;
	}
	private SocketProxyRecordServer(){}
	
	private File logDir = new File("./recordLog");
	private boolean isRecordTime = true;
	public boolean startServer() {
		String tempStr = this.getStringPara("record_time");
		if(tempStr != null && tempStr.trim().equalsIgnoreCase("false")){
			this.isRecordTime = false;
		}
		logDir.mkdir();
		this.isRunning = true;
		return this.isRunning();
	}

	public void stopServer(){
		super.stopServer();
	}
	
	
	private HashMap mapping = new HashMap();
	public void recordData(String flag,boolean isSrc,byte[] data,int offset,int len){
		if(!this.isRunning() || flag == null){
			return ;
		}
		StreamInfo sInfo = null;
		synchronized(this.mapping){
			sInfo = (StreamInfo)this.mapping.get(flag);
			if(sInfo == null){
				try{
					sInfo = new StreamInfo(flag);
					this.mapping.put(flag, sInfo);
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		}
		
		if(sInfo != null){
			try{
				sInfo.appendData(isSrc, data, offset, len);
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}
	
	public void closeRecord(String flag){
		if(!this.isRunning() || flag == null){
			return ;
		}
		synchronized(this.mapping){
			StreamInfo sInfo = (StreamInfo)this.mapping.remove(flag);
			if(sInfo != null){
				try{
					sInfo.close();
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		}
	}
	
	private class StreamInfo{
		private OutputStream srcOut = null;
		private OutputStream dstOut = null;
		
		public StreamInfo(String flag) throws Exception{
			this.srcOut = new FileOutputStream(new File(logDir,"src-"+flag+".record"));
			this.dstOut = new FileOutputStream(new File(logDir,"dst-"+flag+".record"));
		}
		
		public void close() throws Exception{
			if(this.srcOut != null){
				this.srcOut.close();
			}
			if(this.dstOut != null){
				this.dstOut.close();
			}
		}
		
		public void appendData(boolean isSrc,byte[] data,int offset,int len) throws Exception{
			if(isSrc){
				synchronized(this){
					this.srcOut.write(data, offset, len);
				}
			}else{
				synchronized(this){
					this.dstOut.write(data, offset, len);
				}
			}
		}
	}
}
