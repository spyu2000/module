/**
 * 绝密 Created on 2008-5-15 by edmund
 */
package test;

import server.socket.inter.CmdInfo;
import server.socket.inter.ConnectSocketInfo;
import server.socket.inter.ICmdReleaser;

public class TestCmdReleaser implements ICmdReleaser{

	public void init(Object caller){
		
	}

	static byte[] bytes = new byte[1024*1024];
	static {
		for (int i = 0; i < bytes.length; i++) {
			bytes[i]=(byte)i;
		}
	}
	public void releaseCmd(CmdInfo info){
		Object cmd = info.getInfo(CmdInfo.CMD_FLAG);
		Object data = info.getInfo(CmdInfo.DATA_FLAG);
		System.out.println("cmd="+cmd+"; data="+data);
		
		ConnectSocketInfo conn = (ConnectSocketInfo)info.getInfo(CmdInfo.SOCKET_FLAG);
		if(cmd != null && cmd.equals(CmdInfo.SOCKET_CONNECT_CMD)){
			try{
				conn.switchSendMode2Thread(10*1024*1024);
				for(int i=0;i<9;i++){
					conn.writeData(bytes, 0, 1024*1024);
				}
				String str = "你们好,我是测试数据!";
				conn.writeData(str.getBytes(), 0, str.getBytes().length,conn.PRIORITY_LEVEL_1);
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}
}
