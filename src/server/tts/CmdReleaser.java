package server.tts;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import server.socket.inter.CmdInfo;
import server.socket.inter.ConnectSocketInfo;
import server.socket.inter.ICmdReleaser;

public class CmdReleaser implements ICmdReleaser {
	public static final int LOGIN_MSG = 1;
	public static final int HEART_MSG = 2;
	public static final int TTS_CONVERT_MSG = 3;

	private static final Object LOGIN_FLAG = new Object();
	
	private Text2AudioServer server = null;
	public void init(Object caller) {
		this.server = (Text2AudioServer)caller;
	}

	public void releaseCmd(CmdInfo info) {
		Object msg = info.getInfo(CmdInfo.CMD_FLAG);
		ConnectSocketInfo connInfo = (ConnectSocketInfo)info.getInfo(CmdInfo.SOCKET_FLAG);
		
		if(msg == CmdInfo.SOCKET_CONNECT_CMD){
			try{
				connInfo.switchSendMode2Thread(1024*1024);
			}catch(Exception e){
				e.printStackTrace();
			}
			System.out.println("Connect:"+connInfo.getRemoteSocketAddress());
		}else if(msg == CmdInfo.SOCKET_DISCONNECT_CMD){
			System.out.println("DisConnect:"+connInfo.getRemoteSocketAddress());
		}else{
			int cmd = Integer.parseInt(msg.toString());
			
			if(cmd == LOGIN_MSG){
				this.login(info);
			}else {
				if(connInfo.getInfo(LOGIN_FLAG) == null){
					System.out.println("No Login!");
					connInfo.closeSocket();
					return ;
				}
				if(cmd == HEART_MSG){
					this.heart(info);
				}else if(cmd == TTS_CONVERT_MSG){
					this.convert(info);
				}
			}
		}
	}

	private void login(CmdInfo info){
		ConnectSocketInfo connInfo = (ConnectSocketInfo)info.getInfo(CmdInfo.SOCKET_FLAG);
		ByteBuffer buff = (ByteBuffer)info.getInfo(CmdInfo.DATA_FLAG);
		buff.order(ByteOrder.LITTLE_ENDIAN);
		int len = buff.get()&0xFF;
		String account = new String(buff.array(),buff.arrayOffset()+buff.position(),len);
		buff.position(buff.position()+len);
		len = buff.get()&0xFF;
		String pwd = new String(buff.array(),buff.arrayOffset()+buff.position(),len);
		buff.position(buff.position()+len);
		
		boolean isSuccess = true;
		ByteBuffer data = ByteBuffer.allocate(6);
		data.order(ByteOrder.LITTLE_ENDIAN);
		data.put((byte)0x01);
		data.putInt(1);
		if(isSuccess = this.server.validUser(account, pwd)){
			data.put((byte)0);
		}else{
			data.put((byte)1);
		}
		data.flip();
		
		try{
			connInfo.writeData(data);
		}catch(Exception e){
			e.printStackTrace();
		}
		if(!isSuccess){
			connInfo.closeSocket();
		}else{
			connInfo.setInfo(LOGIN_FLAG, account);
		}
		System.out.println("Login:"+account+" "+(isSuccess?"Success":"Failure"));
	}
	private void heart(CmdInfo info){
		ConnectSocketInfo connInfo = (ConnectSocketInfo)info.getInfo(CmdInfo.SOCKET_FLAG);
		ByteBuffer data = ByteBuffer.allocate(65);
		data.order(ByteOrder.LITTLE_ENDIAN);
		data.put((byte)0x02);
		data.putInt(0);
		data.flip();

		try{
			connInfo.writeData(data);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	private void convert(CmdInfo info){		
		ConnectSocketInfo connInfo = (ConnectSocketInfo)info.getInfo(CmdInfo.SOCKET_FLAG);
		ByteBuffer buff = (ByteBuffer)info.getInfo(CmdInfo.DATA_FLAG);
		buff.order(ByteOrder.LITTLE_ENDIAN);
		int len = buff.getShort()&0xFFFF;
		String text = new String(buff.array(),buff.arrayOffset()+buff.position(),len);

		String account = (String)connInfo.getInfo(LOGIN_FLAG);
		System.out.println("Start Convert:"+account+" "+text);
		
		byte[] data = null;
		try{
			data = this.server.text2Audio(text, Text2AudioServer.MP3_AUDIO_FORMAT);
		}catch(Exception e){
			e.printStackTrace();
			return ;
		}

		int dataLen = 0;
		if(data != null){
			dataLen = data.length;
		}
		ByteBuffer sendData = ByteBuffer.allocate(5+1+(2+len)+(4+dataLen));
		sendData.order(ByteOrder.LITTLE_ENDIAN);
		sendData.put((byte)3);
		sendData.putInt(sendData.capacity()-5);
		
		if(data == null){
			sendData.put((byte)1);
		}else{
			sendData.put((byte)0);
		}
		sendData.putShort((short)len);
		sendData.put(text.getBytes());
		sendData.putInt(dataLen);
		if(data != null){
			sendData.put(data);
		}
		sendData.flip();
		

		try{
			connInfo.writeData(sendData);
		}catch(Exception e){
			e.printStackTrace();
		}

		System.out.println("End Convert:"+account+" "+text+" "+dataLen);
	}
}
