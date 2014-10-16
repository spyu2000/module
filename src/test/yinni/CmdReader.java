/**
 * ���� Created on 2009-10-13 by edmund
 */
package test.yinni;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.ClosedChannelException;
import java.util.ArrayList;
import java.util.List;
import server.socket.inter.CmdInfo;
import server.socket.inter.ConnectSocketInfo;
import server.socket.inter.ICmdReader;

public class CmdReader implements ICmdReader{
	private static final Object HEAD_BUFF_FLAG = new Object();
	private static final Object DATA_BUFF_FLAG = new Object();
	private static final Object CMD_LIST_FLAG = new Object();
	
	private OutputStream out = null;
	public void init(Object caller){
		try{
			out = new FileOutputStream("c:/xjs.log");
		} catch (FileNotFoundException e){
			e.printStackTrace();
		}
	}

	public CmdInfo[] readCmd(ConnectSocketInfo connInfo) throws Exception{
//		ͷʼ�մ�������Ϣ������.������δ��������ȡʱ���뵽������Ϣ������,���ڷ�����Ϣ����ʱȥ��.
		ByteBuffer head = (ByteBuffer)connInfo.getInfo(HEAD_BUFF_FLAG);
		ByteBuffer data = (ByteBuffer)connInfo.getInfo(DATA_BUFF_FLAG);
		if(head == null){
			head = ByteBuffer.allocate(9);
			head.order(ByteOrder.LITTLE_ENDIAN);
			connInfo.setInfo(HEAD_BUFF_FLAG, head);
		}
		
		List cmdList = (List)connInfo.getInfo(CMD_LIST_FLAG);
		if(cmdList == null){
			cmdList = new ArrayList(4);
			connInfo.setInfo(CMD_LIST_FLAG, cmdList);
		}
		
		int msg,flag,dataLen,readNum;
		CmdInfo cmdInfo;
		byte[] headByteArr;
		while(true){
			cmdInfo = null;
			
			//�����Ϣͷ�Ѿ���ȡ����,�������
			if(head.remaining() == 0){
				readNum = connInfo.read(data);
				if(readNum < 0 && cmdList.size() == 0){
					throw new ClosedChannelException();
				}

				out.write(data.array(),data.position()-readNum,readNum);
				//�������δ������,������,�ȴ��´ζ�.
				if(data.remaining() != 0){
					connInfo.setInfo(DATA_BUFF_FLAG, data);
					break;
				}
				
				data.position(data.position()-4);
				if(data.getInt() != -1){
					throw new Exception("Э�����!");
				}
				//���ݶ�ȡ����,�����������.
				headByteArr = head.array();
				cmdInfo = new CmdInfo();
				cmdInfo.setInfo(CmdInfo.CMD_FLAG, (headByteArr[4]&0xFF)+"");
				cmdInfo.setInfo(CmdInfo.SOCKET_FLAG, connInfo);
				data.position(0);
				cmdInfo.setInfo(CmdInfo.DATA_FLAG, data);
			}else{ //�����Ϣͷδ������,�����Ϣͷ
				readNum = connInfo.read(head);
				if(readNum < 0 && cmdList == null){
					throw new ClosedChannelException();
				}
				out.write(head.array(),head.position()-readNum,readNum);
				
				//������ܶ�������Ϣͷ,��ζ������ȱ��,����.�ȴ��´ζ�.
				if(head.remaining() != 0){
					break;
				}
				
				//����ͷ��Ϣ��֪������Ϣ��.���������Ϣ����Ϊ0,ֱ�ӷ�������,��֮����������Ϣ����,�ȴ�������.��ʱͷ��Ϣ����.
				head.position(0);
				flag = head.getInt();
				msg = head.get()&0xFF;
				dataLen = head.getInt() + 4;
				
				if(flag != -1){
					throw new Exception("Э�����!");
				}
				
				//������Ϣ����Ϊ0,ֱ�ӷ�������
				if(dataLen == 0){
					cmdInfo = new CmdInfo();
					cmdInfo.setInfo(CmdInfo.CMD_FLAG, msg+"");
					cmdInfo.setInfo(CmdInfo.SOCKET_FLAG, connInfo);
				}else{ //�������ݿռ�,�Ա������ж�ȡ����.
					data = ByteBuffer.allocate(dataLen);
					data.order(ByteOrder.LITTLE_ENDIAN);
				}
			}
			
			//����һ����Ϣ����.�������������Ϣ,�����ͷ��Ϣ,ͬʱ�����Ӷ������Ƴ�������Ϣ.
			if(cmdInfo != null){
				head.position(0);
				connInfo.removeInfo(DATA_BUFF_FLAG);
				
				cmdList.add(cmdInfo);
			}
		}
		
		//�ѵ�ǰ��װ�����������з���.
		CmdInfo[] allCmd = null;
		if(cmdList != null && cmdList.size() > 0){
			allCmd = new CmdInfo[cmdList.size()];
			cmdList.toArray(allCmd);
		}
		cmdList.clear();
		
		return allCmd;
	}
}
