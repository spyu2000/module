package server.proxy.socket.read;

import java.nio.ByteBuffer;

import server.proxy.socket.SocketProxyServer;
import server.proxy.socket.help.SocketProxyRecordServer;
import server.socket.inter.CmdInfo;
import server.socket.inter.ConnectSocketInfo;
import server.socket.inter.ICmdReleaser;

public class DestCmdReleaser implements ICmdReleaser {
	public static final Object SRC_CONN_KEY = new Object();
	public void init(Object caller) {

	}

	public void releaseCmd(CmdInfo info){
		ConnectSocketInfo connInfo = (ConnectSocketInfo)info.getInfo(CmdInfo.SOCKET_FLAG);
		Object msg = info.getInfo(CmdInfo.CMD_FLAG);
		
		if(msg == CmdInfo.SOCKET_CONNECT_CMD){
			try{
				connInfo.switchSendMode2Thread(1024*64);
			}catch(Exception e){
				e.printStackTrace();
			}
			System.out.println("Dest Connect:"+connInfo.getRemoteSocketAddress());
		}else if(msg == CmdInfo.SOCKET_DISCONNECT_CMD){
			ConnectSocketInfo srcConnInfo = (ConnectSocketInfo)connInfo.getInfo(SRC_CONN_KEY);
			if(srcConnInfo != null){
				srcConnInfo.closeSocket();
			}
			System.out.println("Dest Disconnect:"+connInfo.getRemoteSocketAddress());
		}else{
			this.sendData(connInfo,info);
		}
	}
	private void sendData(ConnectSocketInfo connInfo,CmdInfo cmdInfo){
		ByteBuffer data = (ByteBuffer)cmdInfo.getInfo(CmdInfo.DATA_FLAG);
		
		ConnectSocketInfo srcConnInfo = (ConnectSocketInfo)connInfo.getInfo(SRC_CONN_KEY);
		SocketProxyRecordServer.getSingleInstance().recordData(srcConnInfo.getString(SocketProxyServer.FLAG_KEY), false, data.array(), data.arrayOffset()+data.position(), data.remaining());
		try{
			srcConnInfo.writeData(data.duplicate());
		}catch(Exception e){
			e.printStackTrace();
			connInfo.closeSocket();
		}
	}
}
