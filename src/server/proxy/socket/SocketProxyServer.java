/**
 * ¾øÃÜ Created on 2008-4-24 by edmund
 */
package server.proxy.socket;

import java.util.LinkedList;
import java.util.List;
import server.proxy.socket.info.DestInfo;
import server.proxy.socket.read.SrcCmdReader;
import server.proxy.socket.read.SrcCmdReleaser;
import server.proxy.socket.read.UdpCmdReleaser;
import server.socket.serversocket.FleetySocketServer;
import server.socket.udpsocket.FleetyUdpServer;

import com.fleety.server.BasicServer;

public class SocketProxyServer extends BasicServer{
	public static final Object FLAG_KEY = new Object();
	public static final Object SERVER_KEY = new Object();
	
	public static final int ONE_ONE_TYPE = 1;
	public static final int MANY_ONE_TYPE = 2;

	private static SocketProxyServer singleInstance = null;
	public static SocketProxyServer getSingleInstance(){
		if(singleInstance == null){
			synchronized(SocketProxyServer.class){
				if(singleInstance == null){
					singleInstance = new SocketProxyServer();
				}
			}
		}
		return singleInstance;
	}
	
	private int tcpPort = -1;
	private int udpPort = -1;
	
	private FleetySocketServer tcpServer = null;
	private FleetyUdpServer udpServer = null;

	private DestInfo[] tcpDestInfoArr = null;
	private DestInfo[] udpDestInfoArr = null;
	
	private int proxyMethod = ONE_ONE_TYPE;
	
	public boolean startServer(){
		try{
			String tcpPortStr = this.getStringPara("tcp_port");
			if(tcpPortStr != null && tcpPortStr.trim().length() > 0){
				this.tcpPort = Integer.parseInt(tcpPortStr.trim());
			}
			String udpPortStr = this.getStringPara("udp_port");
			if(udpPortStr != null && udpPortStr.trim().length() > 0){
				this.udpPort = Integer.parseInt(udpPortStr.trim());
			}
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}
		
		String methodStr = this.getStringPara("method");
		if(methodStr != null && methodStr.equalsIgnoreCase("one-one")){
			this.proxyMethod = ONE_ONE_TYPE;
		}else{
			this.proxyMethod = MANY_ONE_TYPE;
		}
		
		List tempList = new LinkedList();
		
		String[] destArr,addrArr;
		String tcpDestStr = this.getStringPara("tcp_dest");
		tempList.clear();
		if(tcpDestStr != null){
			destArr = tcpDestStr.split(",");
			for(int i=destArr.length - 1;i>=0;i--){
				addrArr = destArr[i].split(":");
				if(addrArr.length < 2){
					continue;
				}
				try{
					tempList.add(new DestInfo(addrArr[0],Integer.parseInt(addrArr[1]),DestInfo.TCP_TYPE));
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		}
		this.tcpDestInfoArr = new DestInfo[tempList.size()];
		tempList.toArray(this.tcpDestInfoArr);
		
		String udpDestStr = this.getStringPara("udp_dest");
		tempList.clear();
		if(udpDestStr != null){
			destArr = udpDestStr.split(",");
			for(int i=destArr.length - 1;i>=0;i--){
				addrArr = destArr[i].split(":");
				if(addrArr.length < 2){
					continue;
				}
				try{
					tempList.add(new DestInfo(addrArr[0],Integer.parseInt(addrArr[1]),DestInfo.UDP_TYPE));
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		}
		this.udpDestInfoArr = new DestInfo[tempList.size()];
		tempList.toArray(this.udpDestInfoArr);
		
		
		if(tcpPort > 0){
			try{
				this.tcpServer = new FleetySocketServer();
				String tempStr = this.getStringPara(FleetySocketServer.SERVER_IP_FLAG);
				if(tempStr == null || tempStr.trim().length() == 0){
					tempStr = "0.0.0.0";
				}
				this.tcpServer.addPara(FleetySocketServer.SERVER_IP_FLAG, tempStr);
				this.tcpServer.addPara(FleetySocketServer.SERVER_PORT_FLAG, tcpPort+"");
				this.tcpServer.addPara(FleetySocketServer.SELECTOR_NUM_FLAG, this.getPara(FleetySocketServer.SELECTOR_NUM_FLAG));
				this.tcpServer.addPara(FleetySocketServer.DETECT_TIMEOUT_FLAG, this.getPara(FleetySocketServer.DETECT_TIMEOUT_FLAG));
				this.tcpServer.addPara(FleetySocketServer.CMD_READER_FLAG, SrcCmdReader.class.getName());
				this.tcpServer.addPara(FleetySocketServer.CMD_RELEASER_FLAG, SrcCmdReleaser.class.getName());
				this.tcpServer.addPara(SERVER_KEY, this);
				
				if(!this.tcpServer.startServer()){
					return false;
				}
			}catch(Exception e){
				e.printStackTrace();
				return false;
			}
		}
		if(udpPort > 0){
			try{
				this.udpServer = new FleetyUdpServer();
				this.udpServer.addPara(FleetyUdpServer.LOCALIP_FLAG, "0.0.0.0");
				this.udpServer.addPara(FleetyUdpServer.LOCALPORT_FLAG, this.udpPort+"");
				this.udpServer.addPara(FleetyUdpServer.DEAL_DATA_Listener_FLAG, UdpCmdReleaser.class.getName());
				this.udpServer.addPara(FleetyUdpServer.BUFFER_SIZE_FLAG, this.getPara(FleetyUdpServer.BUFFER_SIZE_FLAG));				
				this.udpServer.addPara(SERVER_KEY, this);
				
				if(!this.udpServer.startServer()){
					return false;
				}
			}catch(Exception e){
				e.printStackTrace();
				return false;
			}
		}

		this.isRunning = true;
		return true;
	}
	
	public void stopServer(){
		if(this.tcpServer != null){
			try{
				this.tcpServer.stopServer();
			}catch(Exception e){}
		}
		
		if(this.udpServer != null){
			try{
				this.udpServer.stopServer();
			}catch(Exception e){}
		}
		
		this.isRunning = false;
	}
	
	public DestInfo[] getTcpDestInfoArr(){
		return this.tcpDestInfoArr;
	}
	public DestInfo[] getUdpDestInfoArr(){
		return this.udpDestInfoArr;
	}
	public int getProxyMethod(){
		return this.proxyMethod;
	}
}
