package server.proxy.socket.read;

import java.net.DatagramPacket;

import server.proxy.socket.SocketProxyServer;
import server.proxy.socket.info.DestInfo;
import server.socket.udpsocket.FleetyUdpServer;
import server.socket.udpsocket.PacketListener;

public class UdpCmdReleaser implements PacketListener {
	private FleetyUdpServer udpServer = null;
	private SocketProxyServer proxyServer = null;
	public void init(Object caller) {
		this.udpServer = (FleetyUdpServer)caller;
		this.proxyServer = (SocketProxyServer)this.udpServer.getPara(SocketProxyServer.SERVER_KEY);
	}

	public void eventHappen(DatagramPacket packet) {
		DestInfo[] arr = this.proxyServer.getUdpDestInfoArr();
		if(arr == null || arr.length == 0){
			return ;
		}
		
		byte[] data = new byte[packet.getLength()];
		System.arraycopy(packet.getData(), packet.getOffset(), data, 0, data.length);
		DestInfo destInfo;
		for(int i=0;i<arr.length;i++){
			destInfo = arr[i];
			this.udpServer.sendData(data, destInfo.getIp(), destInfo.getPort());
		}
	}

}
