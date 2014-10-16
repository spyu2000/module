/**
 * ¾øÃÜ Created on 2010-1-21 by edmund
 */
package server.socket.help;

import java.nio.ByteBuffer;
import com.fleety.base.Util;
import server.socket.inter.CmdInfo;
import server.socket.inter.ConnectSocketInfo;
import server.socket.inter.ICmdReader;

public class PrintCmdReader implements ICmdReader
{
	private String logPath = "cmd_data.log";
	private int count = 0;
	public void init(Object caller){
		try{
			Util.openLog(logPath, true);
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	public CmdInfo[] readCmd(ConnectSocketInfo connInfo) throws Exception
	{
		ByteBuffer data = ByteBuffer.allocate(1024);
		int readNum = connInfo.read(data);

		Util.log(logPath, data.array(), 0, readNum);
		for(int i=0;i<readNum;i++){
			count ++;
			
			System.out.print((data.get(i)&0xFF) + " ");
			if((count %16) == 0){
				System.out.println();
			}
		}
		return null;
	}
}
