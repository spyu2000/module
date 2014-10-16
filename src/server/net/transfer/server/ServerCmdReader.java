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
//		头始终存在在信息对象中.数据在未能完整读取时加入到连接信息对象中,而在发布信息命令时去除.
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
			
			//如果信息头已经读取完整,则读数据
			if(head.remaining() == 0){
				readNum = connInfo.read(data);
				if(readNum < 0 && cmdList == null){
					throw new ClosedChannelException();
				}
				
				//如果数据未读完整,则跳出,等待下次读.
				if(data.remaining() != 0){
					connInfo.setInfo(DATA_BUFF_FLAG, data);
					break;
				}
				//数据读取完整,产生命令对象.
				cmdInfo = new CmdInfo();
				cmdInfo.setInfo(CmdInfo.CMD_FLAG, (head.get(8)&0xFF)+"");
				cmdInfo.setInfo(CmdInfo.SOCKET_FLAG, connInfo);
				data.position(0);
				cmdInfo.setInfo(CmdInfo.DATA_FLAG, data);
			}else{ //如果信息头未读完整,则读信息头
				readNum = connInfo.read(head);
				if(readNum < 0 && cmdList == null){
					throw new ClosedChannelException();
				}
				
				//如果不能读完整信息头,意味着数据缺乏,跳出.等待下次读.
				if(head.remaining() != 0){
					break;
				}
				//根据头信息得知数据信息长.如果数据信息长度为0,直接发布命令,反之产生数据信息对象,等待读数据.此时头信息已满.
				if(head.getLong(0) != TransferProtocol.PROTOCOL_HEAD_FLAG){
					throw new Exception("Error Protocol Head");
				}
				dataLen = head.getLong(9);
				//数据信息长度为0,直接发布命令
				if(dataLen == 0){
					cmdInfo = new CmdInfo();
					cmdInfo.setInfo(CmdInfo.CMD_FLAG, (head.get(8)&0xFF)+"");
					cmdInfo.setInfo(CmdInfo.SOCKET_FLAG, connInfo);
				}else{ //产生数据空间,以便向其中读取数据.
					data = ByteBuffer.allocate((int)dataLen);
					data.order(ByteOrder.LITTLE_ENDIAN);
				}
			}
			
			//附加一个信息对象.如果发布命令信息,则清除头信息,同时从连接对象中移除数据信息.
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
		
		//把当前组装的命令对象进行返回.
		CmdInfo[] allCmd = null;
		if(cmdList != null){
			allCmd = new CmdInfo[cmdList.size()];
			cmdList.toArray(allCmd);
		}
		
		return allCmd;
	}

}
