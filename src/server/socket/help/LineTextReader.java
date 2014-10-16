package server.socket.help;

import java.nio.ByteBuffer;
import java.util.ArrayList;

import server.socket.inter.CmdInfo;
import server.socket.inter.ConnectSocketInfo;
import server.socket.inter.ICmdReader;

public class LineTextReader implements ICmdReader {
	private static final Object DATA_BUFFER_FLAG = new Object();
	
	public void init(Object caller) {

	}

	public CmdInfo[] readCmd(ConnectSocketInfo connInfo) throws Exception {
		ByteBuffer buff = (ByteBuffer)connInfo.getInfo(DATA_BUFFER_FLAG);
		if(buff == null){
			buff = ByteBuffer.allocate(1024*512);
			connInfo.setInfo(DATA_BUFFER_FLAG, buff);
		}
		if(!buff.hasRemaining()){
			StringBuffer strBuff = new StringBuffer();
			for(int i=0;i<buff.capacity();i++){
				strBuff.append(Integer.toHexString(buff.get(i)&0xFF));
				if((i+1)%16 == 0){
					strBuff.append("\n");
				}else{
					strBuff.append(" ");
				}
			}
			System.out.println("FullBuff:"+strBuff);
			
			throw new Exception("Buff Full");
		}
		connInfo.read(buff);
		
		CmdInfo cmdInfo;
		int startIndex = 0;
		ArrayList cmdList = new ArrayList(8);
		for(int i=0;i<buff.position();i++){
			if(buff.getShort(i) == 0x0D0A){
				cmdInfo = new CmdInfo();
				cmdInfo.setInfo(CmdInfo.CMD_FLAG, "CMD");
				cmdInfo.setInfo(CmdInfo.DATA_FLAG, new String(buff.array(),startIndex,i-startIndex));
				cmdList.add(cmdInfo);

				startIndex = i + 2;
				i += 1;
			}
		}
		if(startIndex > 0){
			System.arraycopy(buff.array(), startIndex, buff.array(), 0, buff.position()-startIndex);
			buff.position(buff.position()-startIndex);
		}
		
		CmdInfo[] arr = new CmdInfo[cmdList.size()];
		cmdList.toArray(arr);
		return arr;
	}

}
