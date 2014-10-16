package server.socket.help;

import java.nio.ByteBuffer;

import server.socket.inter.CmdInfo;
import server.socket.inter.ConnectSocketInfo;
import server.socket.inter.ICmdReader;

public class UnvalidCmdReader implements ICmdReader {
	private ByteBuffer buff = ByteBuffer.allocate(1024*8);
	public void init(Object caller) {

	}

	public CmdInfo[] readCmd(ConnectSocketInfo connInfo) throws Exception {
		buff.clear();
		connInfo.read(buff);
		return null;
	}

}
