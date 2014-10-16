package server.proxy.socket.read;

import java.nio.ByteBuffer;

import server.socket.inter.CmdInfo;
import server.socket.inter.ConnectSocketInfo;
import server.socket.inter.ICmdReader;

public class DestCmdReader implements ICmdReader {

	public void init(Object caller) {
		
	}
	
	private ByteBuffer buff = ByteBuffer.allocate(8*1024);
	public CmdInfo[] readCmd(ConnectSocketInfo connInfo) throws Exception {
		buff.clear();
		int num = connInfo.read(buff);
		
		if(num <= 0){
			return null;
		}
		
		ByteBuffer data = ByteBuffer.allocate(buff.position());
		System.arraycopy(buff.array(), 0, data.array(), 0, data.capacity());
		
		CmdInfo cmdInfo = new CmdInfo();
		cmdInfo.setInfo(CmdInfo.SOCKET_FLAG, connInfo);
		cmdInfo.setInfo(CmdInfo.DATA_FLAG, data);
		
		return new CmdInfo[]{cmdInfo};
	}

}
