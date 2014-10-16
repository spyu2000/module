package server.socket.udpsocket;

import java.net.DatagramPacket;

public interface PacketListener {

	public void eventHappen(DatagramPacket packet );

	public void init(Object caller);
}
