/**
 * 绝密 Created on 2008-9-4 by edmund
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
		// 头始终存在在信息对象中.数据在未能完整读取时加入到连接信息对象中,而在发布信息命令时去除.
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

			// 如果信息头已经读取完整,则读数据
			if (head.remaining() == 0) {
				readNum = connInfo.read(data);
				if (readNum < 0 && cmdList.size() == 0) {
					System.out.println("读取消息体出现错误");
					throw new ClosedChannelException();
				}
				
				out.write(data.array(), data.position() - readNum, readNum);				
				
				dataByteArr=data.array();
				String msglog="";				
				for(int i=0;i<dataByteArr.length;i++){
					msglog+=(int)dataByteArr[i]+" ";
				}
				System.out.println(msglog);

				// 如果数据未读完整,则跳出,等待下次读.
				if (data.remaining() != 0) {
					System.out.println("消息体读取不完整");
					connInfo.setInfo(DATA_BUFF_FLAG, data);
					break;
				}

				data.position(data.position() - 4);
				if (data.getInt() != -1) {
					System.out.println("消息最外层的壳尾部信息不正确。");
					throw new Exception("协议错误!");
				}
				// 数据读取完整,产生命令对象.
				headByteArr = head.array();
				cmdInfo = new CmdInfo();
				cmdInfo.setInfo(CmdInfo.CMD_FLAG, (headByteArr[4] & 0xFF) + "");
				cmdInfo.setInfo(CmdInfo.SOCKET_FLAG, connInfo);
				data.position(0);
				cmdInfo.setInfo(CmdInfo.DATA_FLAG, data);
			} else { // 如果信息头未读完整,则读信息头
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

				// 如果不能读完整信息头,意味着数据缺乏,跳出.等待下次读.
				if (head.remaining() != 0) {
					System.out.println("读取消息头不完整");
					break;
				}

				// 根据头信息得知数据信息长.如果数据信息长度为0,直接发布命令,反之产生数据信息对象,等待读数据.此时头信息已满.
				head.position(0);
				flag = head.getInt();
				msg = head.get() & 0xFF;
				bodyLen = head.getInt();
				dataLen = bodyLen + 4;

				if (flag != -1) {
					System.out.println("消息最外层的壳头部信息不正确。");
					throw new Exception("协议错误!");
				}

				// 数据信息长度为0,直接发布命令
				if (bodyLen == 0) {
					cmdInfo = new CmdInfo();
					cmdInfo.setInfo(CmdInfo.CMD_FLAG, msg + "");
					cmdInfo.setInfo(CmdInfo.SOCKET_FLAG, connInfo);
				} else { // 产生数据空间,以便向其中读取数据.
					data = ByteBuffer.allocate(dataLen);
					data.order(ByteOrder.LITTLE_ENDIAN);
				}
			}

			// 附加一个信息对象.如果发布命令信息,则清除头信息,同时从连接对象中移除数据信息.
			if (cmdInfo != null) {
				// 清空head和data
				head.position(0);
				connInfo.removeInfo(DATA_BUFF_FLAG);
				cmdList.add(cmdInfo);
			}
		}

		// 把当前组装的命令对象进行返回.
		CmdInfo[] cmdArr = null;
		if (cmdList != null && cmdList.size() > 0) {
			cmdArr = new CmdInfo[cmdList.size()];
			cmdList.toArray(cmdArr);
		}
		cmdList.clear();

		return cmdArr;
	}
}
