/**
 * ���� Created on 2008-9-4 by edmund
 */
package test.spyu;

import java.nio.ByteBuffer;

import javax.naming.LimitExceededException;

import server.socket.inter.CmdInfo;
import server.socket.inter.ConnectSocketInfo;
import server.socket.inter.ICmdReleaser;
import com.fleety.server.BasicServer;

public class SpyuCmdReleaser implements ICmdReleaser {

	private String ��B58888;
	private String receivetask;

	/**
	 * �趨buffer��С
	 */
	public void init(Object caller) {
		BasicServer server = (BasicServer) caller;
	}

	/**
	 * �ӿ�ʵ�֣�������Ϣ
	 */
	public void releaseCmd(CmdInfo cmdInfo) {
		
//		 �յ�����Ϣ��
		Object cmd = cmdInfo.getInfo(CmdInfo.CMD_FLAG);
		System.out.println(cmd);

		// �յ���SocketInfo
		ConnectSocketInfo socketInfo = (ConnectSocketInfo) cmdInfo
				.getInfo(CmdInfo.SOCKET_FLAG);
		

		if (cmd == CmdInfo.SOCKET_CONNECT_CMD) {
			try {				
				while(true){
//					����			
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
		 * ���ݴ�ӡ,����ʹ��.
		 * 
		 */
		if (cmdInfo.getInfo(CmdInfo.DATA_FLAG) != null) {
			//�յ�������
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
