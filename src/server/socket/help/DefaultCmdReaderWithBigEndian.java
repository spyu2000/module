/**
 * ���� Created on 2008-5-15 by edmund
 */
package server.socket.help;

import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.util.LinkedList;
import java.util.List;
import server.socket.inter.CmdInfo;
import server.socket.inter.ConnectSocketInfo;
import server.socket.inter.ICmdReader;

/**
 * 1�ֽڵ���Ϣ�ţ�4�ֽ�(С��)�����ݳ��ȣ�����Ϊ�������ݡ�
 * @author Administrator
 *
 */
public class DefaultCmdReaderWithBigEndian implements ICmdReader{
	private static final Object HEAD_BUFF_FLAG = new Object();
	private static final Object DATA_BUFF_FLAG = new Object();

	private int MAX_LENGTH = 64*1024*1024;
	public void init(Object caller){
		
	}
	
	public CmdInfo[] readCmd(ConnectSocketInfo connInfo) throws Exception{
		//ͷʼ�մ�������Ϣ������.������δ��������ȡʱ���뵽������Ϣ������,���ڷ�����Ϣ����ʱȥ��.
		ByteBuffer head = (ByteBuffer)connInfo.getInfo(HEAD_BUFF_FLAG);
		ByteBuffer data = (ByteBuffer)connInfo.getInfo(DATA_BUFF_FLAG);
		if(head == null){
			head = ByteBuffer.allocate(5);
			connInfo.setInfo(HEAD_BUFF_FLAG, head);
		}
		
		List cmdList = null;
		int dataLen,readNum;
		CmdInfo cmdInfo;
		byte[] headByteArr;
		while(true){
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
				headByteArr = head.array();
				cmdInfo = new CmdInfo();
				cmdInfo.setInfo(CmdInfo.CMD_FLAG, new Integer(headByteArr[0]&0xFF));
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
				dataLen = head.getInt(1);
				
				//������Ϣ����Ϊ0,ֱ�ӷ�������
				if(dataLen == 0){
					cmdInfo = new CmdInfo();
					cmdInfo.setInfo(CmdInfo.CMD_FLAG, new Integer(head.get(0)&0xFF));
					cmdInfo.setInfo(CmdInfo.SOCKET_FLAG, connInfo);
				}else{ //�������ݿռ�,�Ա������ж�ȡ����.
					if(dataLen >= MAX_LENGTH){
						throw new Exception("Error Msg Length,"+dataLen);
					}
					data = ByteBuffer.allocate(dataLen);
				}
			}
			
			//����һ����Ϣ����.�������������Ϣ,�����ͷ��Ϣ,ͬʱ�����Ӷ������Ƴ�������Ϣ.
			if(cmdInfo != null){
				head.position(0);
				connInfo.removeInfo(DATA_BUFF_FLAG);
				
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
