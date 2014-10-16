/**
 * ���� Created on 2008-9-4 by edmund
 */
package test.spyu;

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

public class SpyuCmdReader implements ICmdReader {
	private static final Object HEAD_BUFF_FLAG = new Object();

	private static final Object DATA_BUFF_FLAG = new Object();

	private static final Object CMD_LIST_FLAG = new Object();

	private OutputStream out = null;

	public void init(Object caller) {
		try {
			out = new FileOutputStream("home/fleety/spyu.log");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	public CmdInfo[] readCmd(ConnectSocketInfo connInfo) throws Exception {
		// ͷʼ�մ�������Ϣ������.������δ��������ȡʱ���뵽������Ϣ������,���ڷ�����Ϣ����ʱȥ��.
		ByteBuffer head = (ByteBuffer) connInfo.getInfo(HEAD_BUFF_FLAG);
		ByteBuffer data = (ByteBuffer) connInfo.getInfo(DATA_BUFF_FLAG);
		if (head == null) {
			head = ByteBuffer.allocate(9);
			head.order(ByteOrder.LITTLE_ENDIAN);
			connInfo.setInfo(HEAD_BUFF_FLAG, head);
		}

		List cmdList = (List) connInfo.getInfo(CMD_LIST_FLAG);
		if (cmdList == null) {
			cmdList = new ArrayList(4);
			connInfo.setInfo(CMD_LIST_FLAG, cmdList);
		}

		int msg, flag, bodyLen, dataLen, readNum;
		CmdInfo cmdInfo;
		byte[] headByteArr,dataByteArr;
		while (true) {
			cmdInfo = null;

			// �����Ϣͷ�Ѿ���ȡ����,�������
			if (head.remaining() == 0) {
				readNum = connInfo.read(data);
				if (readNum < 0 && cmdList.size() == 0) {
					System.out.println("��ȡ��Ϣ����ִ���");
					throw new ClosedChannelException();
				}
				
				out.write(data.array(), data.position() - readNum, readNum);				
				
				dataByteArr=data.array();
				String msglog="";				
				for(int i=0;i<dataByteArr.length;i++){
					msglog+=(int)dataByteArr[i]+" ";
				}
				System.out.println(msglog);

				// �������δ������,������,�ȴ��´ζ�.
				if (data.remaining() != 0) {
					System.out.println("��Ϣ���ȡ������");
					connInfo.setInfo(DATA_BUFF_FLAG, data);
					break;
				}

				data.position(data.position() - 4);
				if (data.getInt() != -1) {
					System.out.println("��Ϣ�����Ŀ�β����Ϣ����ȷ��");
					throw new Exception("Э�����!");
				}
				// ���ݶ�ȡ����,�����������.
				headByteArr = head.array();
				cmdInfo = new CmdInfo();
				cmdInfo.setInfo(CmdInfo.CMD_FLAG, (headByteArr[4] & 0xFF) + "");
				cmdInfo.setInfo(CmdInfo.SOCKET_FLAG, connInfo);
				data.position(0);
				cmdInfo.setInfo(CmdInfo.DATA_FLAG, data);
			} else { // �����Ϣͷδ������,�����Ϣͷ
				readNum = connInfo.read(head);
				if (readNum < 0 && cmdList == null) {
					throw new ClosedChannelException();
				}
				
				out.write(head.array(), head.position() - readNum, readNum);
				
				headByteArr=head.array();
				String msglog="";				
				for(int i=0;i<headByteArr.length;i++){
					msglog+=(int)headByteArr[i]+" ";
				}
				System.out.println(msglog);

				// ������ܶ�������Ϣͷ,��ζ������ȱ��,����.�ȴ��´ζ�.
				if (head.remaining() != 0) {
					System.out.println("��ȡ��Ϣͷ������");
					break;
				}

				// ����ͷ��Ϣ��֪������Ϣ��.���������Ϣ����Ϊ0,ֱ�ӷ�������,��֮����������Ϣ����,�ȴ�������.��ʱͷ��Ϣ����.
				head.position(0);
				flag = head.getInt();
				msg = head.get() & 0xFF;
				bodyLen = head.getInt();
				dataLen = bodyLen + 4;

				if (flag != -1) {
					System.out.println("��Ϣ�����Ŀ�ͷ����Ϣ����ȷ��");
					throw new Exception("Э�����!");
				}

				// ������Ϣ����Ϊ0,ֱ�ӷ�������
				if (bodyLen == 0) {
					cmdInfo = new CmdInfo();
					cmdInfo.setInfo(CmdInfo.CMD_FLAG, msg + "");
					cmdInfo.setInfo(CmdInfo.SOCKET_FLAG, connInfo);
				} else { // �������ݿռ�,�Ա������ж�ȡ����.
					data = ByteBuffer.allocate(dataLen);
					data.order(ByteOrder.LITTLE_ENDIAN);
				}
			}

			// ����һ����Ϣ����.�������������Ϣ,�����ͷ��Ϣ,ͬʱ�����Ӷ������Ƴ�������Ϣ.
			if (cmdInfo != null) {
				// ���head��data
				head.position(0);
				connInfo.removeInfo(DATA_BUFF_FLAG);
				cmdList.add(cmdInfo);
			}
		}

		// �ѵ�ǰ��װ�����������з���.
		CmdInfo[] cmdArr = null;
		if (cmdList != null && cmdList.size() > 0) {
			cmdArr = new CmdInfo[cmdList.size()];
			cmdList.toArray(cmdArr);
		}
		cmdList.clear();

		return cmdArr;
	}
}
