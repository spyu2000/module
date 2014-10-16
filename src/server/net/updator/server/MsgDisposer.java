package server.net.updator.server;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import com.fleety.base.StrFilter;

import server.net.updator.IUpdator;
import server.socket.inter.CmdInfo;
import server.socket.inter.ConnectSocketInfo;
import server.socket.inter.ICmdReleaser;

public class MsgDisposer implements ICmdReleaser {
	private AutoUpdatorServer server = null;
	public void init(Object caller){
		this.server = (AutoUpdatorServer)caller;
	}
	
	public void releaseCmd(CmdInfo info){
		ConnectSocketInfo connInfo = (ConnectSocketInfo)info.getInfo(CmdInfo.SOCKET_FLAG);
		Object msg = info.getInfo(CmdInfo.CMD_FLAG);
		if(msg == CmdInfo.SOCKET_CONNECT_CMD){
			System.out.println("Socket Connected!"+connInfo.getRemoteSocketAddress());
			try{
				connInfo.switchSendMode2Thread(1024*1024);
			}catch(Exception e){
				e.printStackTrace();
			}
		}else if(msg == CmdInfo.SOCKET_DISCONNECT_CMD){
			System.out.println("Socket DisConnected!"+connInfo.getRemoteSocketAddress());
		}else{
			int msgInt = ((Integer)msg).intValue();
			ByteBuffer data = (ByteBuffer)info.getInfo(CmdInfo.DATA_FLAG);
			if(data.getLong() != -1){
				connInfo.closeSocket();
				return ;
			}
			
			if(msgInt == IUpdator.U_VERSION_COMPARE_MSG){
				this.compareVersion(connInfo, data);
			}else if(msgInt == IUpdator.U_FILE_DOWNLOAD_MSG){
				this.sendDownloadFile(connInfo,data);
			}
		}
	}

	private void compareVersion(ConnectSocketInfo connInfo,ByteBuffer data){
		int infoLen = data.getShort()&0xFFFF;
		String infoStr = new String(data.array(),data.position(),infoLen);
		String[] arr = StrFilter.split(infoStr,"\t");
		
		String version = arr[0];
		System.out.println("ClientVersion:version="+version);

		StringBuffer responseStr = new StringBuffer(256);
		if(this.server.getCurVersion() != null && !this.server.getCurVersion().equals(version)){
			responseStr.append(this.server.getCurVersion());
			
			File curVersionDir = new File(this.server.getUpdatePath(),this.server.getCurVersion());
			this.addUpdateFile(curVersionDir.getAbsolutePath(),curVersionDir,responseStr);
		}else{
			responseStr.append(arr[0]);
		}
		
		byte[] tData = responseStr.toString().getBytes();
		ByteBuffer sendData = ByteBuffer.allocate(9+2+tData.length);
		sendData.order(ByteOrder.LITTLE_ENDIAN);
		sendData.put((byte)IUpdator.D_VERSION_COMPARE_RES);
		sendData.putLong(sendData.capacity()-9);
		sendData.putShort((short)tData.length);
		sendData.put(tData);
		sendData.flip();
		try{
			connInfo.writeData(sendData);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	private void sendDownloadFile(ConnectSocketInfo connInfo,ByteBuffer data){
		int infoLen = data.getShort()&0xFFFF;
		String infoStr = new String(data.array(),data.position(),infoLen);
		String[] arr = StrFilter.split(infoStr,"\t");
		
		String version = arr[0];
		String filePath = arr[1];
		long lastModifiedTime = Long.parseLong(arr[2]);
		long size = Long.parseLong(arr[3]);

		byte[] pathArr = filePath.getBytes();
		File downloadFile = new File(new File(this.server.getUpdatePath(),version),filePath);

		if(downloadFile.exists() && (downloadFile.lastModified() != lastModifiedTime || downloadFile.length() != size)){
			ByteBuffer sendData = ByteBuffer.allocate(9+8+1+pathArr.length+1);
			sendData.order(ByteOrder.LITTLE_ENDIAN);
			sendData.put((byte)IUpdator.D_FILE_DOWNLOAD_RES);
			sendData.putLong(sendData.capacity()-9 + downloadFile.length());
			sendData.putLong(downloadFile.lastModified());
			sendData.put((byte)pathArr.length);
			sendData.put(pathArr);
			sendData.put((byte)0);
			sendData.flip();
			
			try{
				connInfo.writeData(sendData);
				this.server.getTimerPool().schedule(new DownloadTask(connInfo,downloadFile,512*1024), 0, 100);
			}catch(Exception e){
				e.printStackTrace();
				connInfo.closeSocket();
			}
		}else{
			ByteBuffer sendData = ByteBuffer.allocate(9+8+1+pathArr.length+1);
			sendData.order(ByteOrder.LITTLE_ENDIAN);
			sendData.put((byte)IUpdator.D_FILE_DOWNLOAD_RES);
			sendData.putLong(sendData.capacity()-9);
			sendData.putLong(downloadFile.lastModified());
			sendData.put((byte)pathArr.length);
			sendData.put(pathArr);
			sendData.put((byte)1);
			sendData.flip();

			try{
				connInfo.writeData(sendData);
			}catch(Exception e){
				e.printStackTrace();
				connInfo.closeSocket();
			}
		}
	}
	
	private void addUpdateFile(String sPath,File dir,StringBuffer strBuff){
		File[] childFileArr = dir.listFiles();
		for(int i=0;childFileArr != null && i<childFileArr.length;i++){
			if(childFileArr[i].isFile()){
				strBuff.append("\t");
				strBuff.append(childFileArr[i].getAbsolutePath().substring(sPath.length()));
			}else if(childFileArr[i].isDirectory()){
				this.addUpdateFile(sPath, childFileArr[i], strBuff);
			}
		}
	}
}
