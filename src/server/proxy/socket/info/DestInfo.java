/**
 * ¾øÃÜ Created on 2008-4-24 by edmund
 */
package server.proxy.socket.info;

import java.nio.ByteBuffer;

import server.proxy.socket.read.DestCmdReader;
import server.proxy.socket.read.DestCmdReleaser;
import server.socket.inter.ConnectSocketInfo;
import server.socket.socket.FleetySocket;

public class DestInfo{
	public static final int TCP_TYPE = 1;
	public static final int UDP_TYPE = 2;
	
	private String ip = null;
	private int port = 0;
	private int type = TCP_TYPE;

	private FleetySocket socket = null;
	
	public DestInfo(String ip,int port,int type){
		this.ip = ip;
		this.port = port;
		this.type = type;
	}
	
	public DestInfo(DestInfo destInfo){
		this.ip = destInfo.getIp();
		this.port = destInfo.getPort();
		this.type = destInfo.getType();
	}
	
	public String getIp(){
		return this.ip;
	}
	
	public int getPort(){
		return this.port;
	}
	
	public int getType(){
		return this.type;
	}
	
	public boolean connect(){
		if(this.type != TCP_TYPE){
			return true;
		}
		
		this.socket = new FleetySocket();
		socket.addPara(FleetySocket.SERVER_IP_FLAG, this.ip);
		socket.addPara(FleetySocket.SERVER_PORT_FLAG, this.port);
		socket.addPara(FleetySocket.AUTO_CONNECT_CYCLE_FLAG, 3600000+"");
		socket.addPara(FleetySocket.CMD_READER_FLAG, DestCmdReader.class.getName());
		socket.addPara(FleetySocket.CMD_RELEASER_FLAG, DestCmdReleaser.class.getName());		
		socket.startServer();
		
		return true;
	}
	public boolean isConnect(){
		return this.socket.getConnInfo().isConnected();
	}
	
	public void close(){
		this.socket.stopServer();
	}
	
	public void sendData(ByteBuffer data) throws Exception{
		synchronized(this.socket){
			this.socket.getConnInfo().writeData(data);
		}
	}
	
	public ConnectSocketInfo getConnInfo(){
		return this.socket.getConnInfo();
	}
	
	public String toString(){
		return this.ip+":"+this.port;
	}
}
