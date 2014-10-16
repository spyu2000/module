/**
 * 绝密 Created on 2008-9-4 by edmund
 */
package test.spyu;

import java.nio.ByteBuffer;

import javax.naming.LimitExceededException;

import server.socket.inter.CmdInfo;
import server.socket.inter.ConnectSocketInfo;
import server.socket.inter.ICmdReleaser;
import com.fleety.server.BasicServer;

public class SpyuCmdReleaser implements ICmdReleaser {

	private String 粤B58888;
	private String receivetask;

	/**
	 * 设定buffer大小
	 */
	public void init(Object caller) {
		BasicServer server = (BasicServer) caller;
	}

	/**
	 * 接口实现，解析消息
	 */
	public void releaseCmd(CmdInfo cmdInfo) {
		
//		 收到的消息号
		Object cmd = cmdInfo.getInfo(CmdInfo.CMD_FLAG);
		System.out.println(cmd);

		// 收到的SocketInfo
		ConnectSocketInfo socketInfo = (ConnectSocketInfo) cmdInfo
				.getInfo(CmdInfo.SOCKET_FLAG);
		

		if (cmd == CmdInfo.SOCKET_CONNECT_CMD) {
			try {				
				while(true){
//					测试			
					 byte[] data = SpyuCmdBuilder.getTaskInfo(1, "admin", "test", 1);
					 Thread.sleep(6000);
					 socketInfo.writeData(data,0,data.length);
				}
				
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return;
		} else if (cmd == CmdInfo.SOCKET_DISCONNECT_CMD) {
			return;
		}

		

		/*
		 * 数据打印,测试使用.
		 * 
		 */
		if (cmdInfo.getInfo(CmdInfo.DATA_FLAG) != null) {
			//收到的数据
			byte[] bytes = ((ByteBuffer) cmdInfo.getInfo(CmdInfo.DATA_FLAG))
					.array();
			StringBuffer buff = new StringBuffer(256);
			for (int i = 0; i < bytes.length; i++) {
				buff.append((bytes[i] & 0xFF) + ",");
			}
			System.out.println(buff.toString());
		}

	}
}
