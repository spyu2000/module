package server.proxy.socket.read;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Date;

import com.fleety.base.FleetyThread;
import com.fleety.base.GeneralConst;

import server.proxy.socket.SocketProxyServer;
import server.proxy.socket.help.SocketProxyRecordServer;
import server.proxy.socket.info.DestInfo;
import server.socket.inter.CmdInfo;
import server.socket.inter.ConnectSocketInfo;
import server.socket.inter.ICmdReleaser;
import server.socket.serversocket.FleetySocketServer;

public class SrcCmdReleaser implements ICmdReleaser {
	private static final Object DEST_CONN_KEY = new Object();
	private SocketProxyServer proxyServer = null;
	private FleetySocketServer server = null;
	private String serverFlag = "";
	
	public void init(Object caller) {
		this.server = (FleetySocketServer)caller;
		this.proxyServer = (SocketProxyServer)this.server.getPara(SocketProxyServer.SERVER_KEY);

		this.serverFlag = this.server.getStringPara("flag");
		if(this.serverFlag == null){
			this.serverFlag = "";
		}
		this.serverFlag = this.serverFlag.trim();
	}

	public void releaseCmd(CmdInfo info) {
		ConnectSocketInfo connInfo = (ConnectSocketInfo)info.getInfo(CmdInfo.SOCKET_FLAG);
		Object msg = info.getInfo(CmdInfo.CMD_FLAG);
		
		if(msg == CmdInfo.SOCKET_CONNECT_CMD){
			System.out.println("Src Connect:"+connInfo.getRemoteSocketAddress());
			try{
				connInfo.switchSendMode2Thread(1024*64);
			}catch(Exception e){
				e.printStackTrace();
			}
			
			String connFlag = this.serverFlag+"-"+connInfo.getRemoteSocketAddress()+"-"+GeneralConst.YYMMDDHHMMSS.format(new Date());
			System.out.println(connFlag);
			connFlag = connFlag.replace(':', '-').replace('/', ' ').trim();
			connInfo.setInfo(SocketProxyServer.FLAG_KEY, connFlag);
			
			if(!this.createDestConnect(connInfo)){
				connInfo.closeSocket();
			}
		}else if(msg == CmdInfo.SOCKET_DISCONNECT_CMD){
			this.destroyDestConnect(connInfo);
			SocketProxyRecordServer.getSingleInstance().closeRecord(connInfo.getString(SocketProxyServer.FLAG_KEY));
			connInfo.removeInfo(SocketProxyServer.FLAG_KEY);
			System.out.println("Src Disconnect:"+connInfo.getRemoteSocketAddress());
		}else{
			this.sendData(connInfo,info);
		}
	}

	private boolean createDestConnect(ConnectSocketInfo connInfo){
		DestInfo[] destInfoArr = this.proxyServer.getTcpDestInfoArr();
		DestInfo destInfo;
		ArrayList list = new ArrayList(destInfoArr.length);
		connInfo.setInfo(DEST_CONN_KEY, list);
		
		try{
			int count = 0;
			ConnectSocketInfo destConnInfo = null;
			for(int i=0;i<destInfoArr.length;i++){
				destInfo = new DestInfo(destInfoArr[i]);
				destInfo.connect();
				
				count = 0;
				while(count++ < 200){
					destConnInfo = destInfo.getConnInfo();
					if(destConnInfo != null && destConnInfo.isConnected()){
						break;
					}
					destConnInfo = null;
					FleetyThread.sleep(10);
				}
				if(destConnInfo == null){
					System.out.println("Dest Connect Timeout:"+destInfo);
					connInfo.closeSocket();
					return false;
				}
				destConnInfo.setInfo(DestCmdReleaser.SRC_CONN_KEY,connInfo);
				list.add(destInfo);
				return true;
			}
		}catch(Exception e){
			e.printStackTrace();
			connInfo.closeSocket();
		}
		return false;
	}
	
	private void destroyDestConnect(ConnectSocketInfo connInfo){
		DestInfo destInfo;
		ArrayList list = (ArrayList)connInfo.getInfo(DEST_CONN_KEY);
		for(int i=0;i<list.size();i++){
			destInfo = (DestInfo)list.get(i);
			try{
				destInfo.close();
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}
	
	private void sendData(ConnectSocketInfo connInfo,CmdInfo cmdInfo){
		ByteBuffer data = (ByteBuffer)cmdInfo.getInfo(CmdInfo.DATA_FLAG);
		
		SocketProxyRecordServer.getSingleInstance().recordData(connInfo.getString(SocketProxyServer.FLAG_KEY), true, data.array(), data.arrayOffset()+data.position(), data.remaining());
		
		DestInfo destInfo;
		ArrayList list = (ArrayList)connInfo.getInfo(DEST_CONN_KEY);
		for(int i=0;i<list.size();i++){
			destInfo = (DestInfo)list.get(i);
			try{
				destInfo.sendData(data.duplicate());
			}catch(Exception e){
				connInfo.destroy();
			}
		}
	}
}
