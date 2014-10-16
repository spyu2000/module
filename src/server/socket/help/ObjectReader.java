package server.socket.help;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.util.LinkedList;
import java.util.List;

import server.socket.inter.CmdInfo;
import server.socket.inter.ConnectSocketInfo;
import server.socket.inter.ICmdReader;

public class ObjectReader implements ICmdReader {
	private static final Object HEAD_BUFF_FLAG = new Object();
	private static final Object DATA_BUFF_FLAG = new Object();

	public void init(Object caller){
		
	}
	
	public CmdInfo[] readCmd(ConnectSocketInfo connInfo) throws Exception{
//		ͷʼ�մ�������Ϣ������.������δ��������ȡʱ���뵽������Ϣ������,���ڷ�����Ϣ����ʱȥ��.
		ByteBuffer head = (ByteBuffer)connInfo.getInfo(HEAD_BUFF_FLAG);
		ByteBuffer data = (ByteBuffer)connInfo.getInfo(DATA_BUFF_FLAG);
		if(head == null){
			head = ByteBuffer.allocate(4);
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
				cmdInfo.setInfo(CmdInfo.SOCKET_FLAG, connInfo);
				ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(data.array()));
				cmdInfo.setInfo(CmdInfo.DATA_FLAG, in.readObject());
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
				headByteArr = head.array();
				dataLen = head.getInt(0);
				
				//������Ϣ����Ϊ0,ֱ�ӷ�������
				if(dataLen == 0){
					throw new Exception("Zero Length Data!");
				}else{ //�������ݿռ�,�Ա������ж�ȡ����.
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

	public static byte[] object2ByteArr(Object obj,int maxSize) throws Exception{
		ByteArrayOutputStream bOut = new ByteArrayOutputStream(maxSize);
		ObjectOutputStream out = new ObjectOutputStream(bOut);
		out.writeObject(obj);
		ByteBuffer buff = ByteBuffer.allocate(bOut.size()+4);
		buff.putInt(buff.capacity()-4);
		buff.put(bOut.toByteArray());
		out.close();
		return buff.array();
	}
}
