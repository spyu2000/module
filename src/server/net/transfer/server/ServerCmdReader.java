package server.net.transfer.server;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.ClosedChannelException;
import java.util.LinkedList;
import java.util.List;

import com.fleety.base.Util;

import server.net.transfer.TransferProtocol;
import server.socket.inter.CmdInfo;
import server.socket.inter.ConnectSocketInfo;
import server.socket.inter.ICmdReader;

public class ServerCmdReader implements ICmdReader {
	private static final Object HTTP_HEAD_FLAG = new Object();
	private static final Object HEAD_BUFF_FLAG = new Object();
	private static final Object DATA_BUFF_FLAG = new Object();

	private static final Object StART_HTTP = new Object();
	private static final Object FIRST_ENTER_HTTP = new Object();
	private static final Object FULL_HTTP = new Object();

	public void init(Object caller) {
		
	}

	public CmdInfo[] readCmd(ConnectSocketInfo connInfo) throws Exception {
//		ͷʼ�մ�������Ϣ������.������δ��������ȡʱ���뵽������Ϣ������,���ڷ�����Ϣ����ʱȥ��.
		ByteBuffer head = (ByteBuffer)connInfo.getInfo(HEAD_BUFF_FLAG);
		ByteBuffer data = (ByteBuffer)connInfo.getInfo(DATA_BUFF_FLAG);
		if(head == null){
			head = ByteBuffer.allocate(17);
			head.order(ByteOrder.LITTLE_ENDIAN);
			connInfo.setInfo(HEAD_BUFF_FLAG, head);
		}
		
		List cmdList = null;
		long dataLen,readNum;
		CmdInfo cmdInfo;
		ByteBuffer httpHead = ByteBuffer.allocate(1);
		while(true){
			if(connInfo.getInfo(HTTP_HEAD_FLAG) != FULL_HTTP){
				readNum = connInfo.read(httpHead);
				if(readNum < 0 && cmdList == null){
					throw new ClosedChannelException();
				}
				if(readNum == 0){
					break;
				}
				
				if(httpHead.get(0) == '\n'){
					if(connInfo.getInfo(HTTP_HEAD_FLAG) == FIRST_ENTER_HTTP){
						connInfo.setInfo(HTTP_HEAD_FLAG, FULL_HTTP);
					}else{
						connInfo.setInfo(HTTP_HEAD_FLAG, FIRST_ENTER_HTTP);
					}
				}else{
					connInfo.setInfo(HTTP_HEAD_FLAG, StART_HTTP);
				}
				
				continue;
			}
			
			cmdInfo = null;
			
			//�����Ϣͷ�Ѿ���ȡ����,�������
			if(head.remaining() == 0){
				readNum = connInfo.read(data);
				if(readNum < 0 && cmdList == null){
					throw new ClosedChannelException();
				}
				
				//�������δ������,������,�ȴ��´ζ�.
				if(data.remaining() != 0){
					connInfo.setInfo(DATA_BUFF_FLAG, data);
					break;
				}
				//���ݶ�ȡ����,�����������.
				cmdInfo = new CmdInfo();
				cmdInfo.setInfo(CmdInfo.CMD_FLAG, (head.get(8)&0xFF)+"");
				cmdInfo.setInfo(CmdInfo.SOCKET_FLAG, connInfo);
				data.position(0);
				cmdInfo.setInfo(CmdInfo.DATA_FLAG, data);
			}else{ //�����Ϣͷδ������,�����Ϣͷ
				readNum = connInfo.read(head);
				if(readNum < 0 && cmdList == null){
					throw new ClosedChannelException();
				}
				
				//������ܶ�������Ϣͷ,��ζ������ȱ��,����.�ȴ��´ζ�.
				if(head.remaining() != 0){
					break;
				}
				//����ͷ��Ϣ��֪������Ϣ��.���������Ϣ����Ϊ0,ֱ�ӷ�������,��֮����������Ϣ����,�ȴ�������.��ʱͷ��Ϣ����.
				if(head.getLong(0) != TransferProtocol.PROTOCOL_HEAD_FLAG){
					throw new Exception("Error Protocol Head");
				}
				dataLen = head.getLong(9);
				//������Ϣ����Ϊ0,ֱ�ӷ�������
				if(dataLen == 0){
					cmdInfo = new CmdInfo();
					cmdInfo.setInfo(CmdInfo.CMD_FLAG, (head.get(8)&0xFF)+"");
					cmdInfo.setInfo(CmdInfo.SOCKET_FLAG, connInfo);
				}else{ //�������ݿռ�,�Ա������ж�ȡ����.
					data = ByteBuffer.allocate((int)dataLen);
					data.order(ByteOrder.LITTLE_ENDIAN);
				}
			}
			
			//����һ����Ϣ����.�������������Ϣ,�����ͷ��Ϣ,ͬʱ�����Ӷ������Ƴ�������Ϣ.
			if(cmdInfo != null){
				head.position(0);
				connInfo.removeInfo(DATA_BUFF_FLAG);
				connInfo.removeInfo(HTTP_HEAD_FLAG);
				
				if(cmdList == null){
					cmdList = new LinkedList();
				}
				cmdList.add(cmdInfo);
			}
		}
		
		//�ѵ�ǰ��װ�����������з���.
		CmdInfo[] allCmd = null;
		if(cmdList != null){
			allCmd = new CmdInfo[cmdList.size()];
			cmdList.toArray(allCmd);
		}
		
		return allCmd;
	}

}
