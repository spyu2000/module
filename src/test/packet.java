package test;

import java.io.FileInputStream;
import java.net.DatagramPacket;

import server.socket.udpsocket.PacketListener;

public class packet implements PacketListener {

	public void init() {
		
	}
	public void eventHappen(DatagramPacket packet) {

		
		int offset = packet.getOffset();
		byte[] data = packet.getData();
		int dataLen = packet.getLength();
		System.out.println("dataLen::"+dataLen);

		byte[] realData = new byte[dataLen];
		System.arraycopy(data, 0, realData, 0, dataLen);

		System.out.println("���յ���������Դ��"+packet.getAddress()+":::"+packet.getPort());
		System.out.println(new String(realData));

	}

	public void sendData() {
		try
		{
			String temp="C:\\Documents and Settings\\Administrator\\����\\track1.txt";
			FileInputStream input = new FileInputStream(temp);
			byte[] b = new byte[5120];
			int len;
			while ((len = input.read(b)) != -1) {
				System.out.println("�������ݣ���"+len);
	
				byte[] realData = new byte[len];
				System.arraycopy(b, 0, realData, 0, len);
//				udpTest1.getSingleInstance().sendData(realData);
			}
			input.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	public void init(Object caller) {
		// TODO Auto-generated method stub
		
	}

}
