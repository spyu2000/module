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
//		头始终存在在信息对象中.数据在未能完整读取时加入到连接信息对象中,而在发布信息命令时去除.
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
				headByteArr = head.array();
				cmdInfo = new CmdInfo();
				cmdInfo.setInfo(CmdInfo.SOCKET_FLAG, connInfo);
				ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(data.array()));
				cmdInfo.setInfo(CmdInfo.DATA_FLAG, in.readObject());
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
				headByteArr = head.array();
				dataLen = head.getInt(0);
				
				//数据信息长度为0,直接发布命令
				if(dataLen == 0){
					throw new Exception("Zero Length Data!");
				}else{ //产生数据空间,以便向其中读取数据.
					data = ByteBuffer.allocate(dataLen);
				}
			}
			
			//附加一个信息对象.如果发布命令信息,则清除头信息,同时从连接对象中移除数据信息.
			if(cmdInfo != null){
				head.position(0);
				connInfo.removeInfo(DATA_BUFF_FLAG);
				
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
