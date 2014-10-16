/**
 * 绝密 Created on 2008-11-21 by edmund
 */
package server.socket.help;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import com.fleety.base.Util;
import server.socket.inter.CmdInfo;
import server.socket.inter.ConnectSocketInfo;
import server.socket.inter.ICmdReader;

public class HttpCmdReader implements ICmdReader{
	public static final Object MSG_BUFF_FLAG = new Object();
	public static final Object DATA_BUFF_FLAG = new Object();
	private static final Object CMD_LIST_FLAG = new Object();
	
	private int maxSize = 1024*1024;
	public void init(Object caller){
		
	}

	private static final String HTTP_HEAD_SPLIT = Util.httpSplitStr+Util.httpSplitStr;
	private static final int HTTP_HEAD_SPLIT_LENGTH = 4;
	public CmdInfo[] readCmd(ConnectSocketInfo connInfo) throws Exception{
		ByteBuffer headBuff = (ByteBuffer)connInfo.getInfo(MSG_BUFF_FLAG);
		if(headBuff == null){
			headBuff = ByteBuffer.allocate(1024);
			connInfo.setInfo(MSG_BUFF_FLAG, headBuff);
		}
		ByteBuffer dataBuff = (ByteBuffer)connInfo.getInfo(DATA_BUFF_FLAG);

		List cmdList = (List)connInfo.getInfo(CMD_LIST_FLAG);
		if(cmdList == null){
			cmdList = new ArrayList(4);
			connInfo.setInfo(CMD_LIST_FLAG, cmdList);
		}
		
		if(dataBuff != null){
			connInfo.read(dataBuff);
			
			if(dataBuff.hasRemaining()){
				//返回之前已读取的命令
			}else{
				CmdInfo info = new CmdInfo();
				
				ByteBuffer cmdHead = ByteBuffer.allocate(headBuff.limit());
				System.arraycopy(headBuff.array(), 0, cmdHead.array(), 0, headBuff.limit());
				this.updateHeadInfo(info, new String(cmdHead.array()));
				
				dataBuff.flip();
				HttpInfo httpInfo = new HttpInfo(cmdHead,dataBuff);
				info.setInfo(CmdInfo.SOCKET_FLAG, connInfo);
				info.setInfo(CmdInfo.DATA_FLAG, httpInfo);
				cmdList.add(info);
				
				connInfo.removeInfo(DATA_BUFF_FLAG);
				headBuff.clear();
			}
		}else{
			connInfo.read(headBuff);
			int index,lenStartIndex,lenEndIndex,dataLen,remainLen;
			while(true){
				String headStr = new String(headBuff.array(),0,headBuff.position());
				String flagHeadStr = headStr.toLowerCase();
				if(flagHeadStr.indexOf(Util.httpSplitStr) > 0){
					if(flagHeadStr.indexOf("http") < 0){
						connInfo.destroy();
						return null;
					}
				}
				index = headStr.indexOf(HTTP_HEAD_SPLIT);
				
				if(index >= 0){
					index += HTTP_HEAD_SPLIT_LENGTH;
					
					lenStartIndex = flagHeadStr.indexOf("content-length:");
					if(lenStartIndex >= 0){
						lenEndIndex = headStr.indexOf(Util.httpSplitStr, lenStartIndex);
						dataLen = Integer.parseInt(headStr.substring(lenStartIndex+"content-length:".length(), lenEndIndex).trim());
					}else{
						dataLen = 0;
					}
					
					if(dataLen > maxSize){
						connInfo.destroy();
						return null;
					}
					
					remainLen = headBuff.position() - index;

					dataBuff = ByteBuffer.allocate(dataLen);
					if(remainLen >= dataLen){
						System.arraycopy(headBuff.array(), index, dataBuff.array(), 0, dataLen);
						CmdInfo info = new CmdInfo();
						ByteBuffer cmdHead = ByteBuffer.allocate(index);
						System.arraycopy(headBuff.array(), 0, cmdHead.array(), 0, index);
						this.updateHeadInfo(info, new String(cmdHead.array()));
						
						info.setInfo(CmdInfo.SOCKET_FLAG, connInfo);
						info.setInfo(CmdInfo.DATA_FLAG, new HttpInfo(cmdHead,dataBuff));
						cmdList.add(info);
					}else{
						System.arraycopy(headBuff.array(), index, dataBuff.array(), 0, remainLen);
						dataBuff.position(remainLen);
						headBuff.position(0);
						headBuff.limit(index);
						connInfo.setInfo(DATA_BUFF_FLAG, dataBuff);
						break;
					}
					
					if(dataLen <= remainLen){
						System.arraycopy(headBuff.array(), index + dataLen, headBuff.array(), 0, remainLen - dataLen);
						headBuff.position(remainLen - dataLen);
					}
				}else{
					break;
				}
			}
		}
		
		CmdInfo[] cmdArr = new CmdInfo[cmdList.size()];
		cmdList.toArray(cmdArr);
		cmdList.clear();
		
		return cmdArr;
	}
	
	private void updateHeadInfo(CmdInfo cmdInfo,String headStr) throws Exception{
		String[] lineArr = headStr.split(Util.httpSplitStr);
		String[] arr1 = lineArr[0].split(" ");
		if(arr1.length < 2){
			throw new Exception("Error Http Head!"+lineArr[0]);
		}
		cmdInfo.setInfo(CmdInfo.CMD_FLAG, arr1[1].substring(1).trim());
		
		String[] attr;
		for(int i=1;i<lineArr.length;i++){
			attr = lineArr[i].split(":");
			if(attr.length == 2){
				cmdInfo.setInfo(attr[0].trim().toLowerCase(), attr[1].trim());
			}
		}
	}
	
	public class HttpInfo{
		public ByteBuffer headBuff = null;
		public ByteBuffer dataBuff = null;
		public HttpInfo(ByteBuffer headBuff,ByteBuffer dataBuff){
			this.headBuff = headBuff;
			this.dataBuff = dataBuff;
		}
		
		public String toString(){
			return "http head:"+this.headBuff.toString()+"\r\n\thttp data:"+this.dataBuff.toString();
		}
	}
}
