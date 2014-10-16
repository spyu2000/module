/**
 * ¾øÃÜ Created on 2008-5-15 by edmund
 */
package server.socket.help;

import server.socket.inter.CmdInfo;
import server.socket.inter.ICmdReleaser;

public class DefaultCmdReleaser implements ICmdReleaser{

	public void init(Object caller){
		
	}
	
//	private int count = -1;
	public void releaseCmd(CmdInfo info){
//		String str = (String)info.getInfo(CmdInfo.DATA_FLAG);
//		if(str != null){
//			int tempCount = Integer.parseInt(str.substring(0,str.indexOf("-")));
//			if(tempCount != count + 1){
//				System.out.println("ÒÅÂ©:"+count+" - "+tempCount);
//			}
//			count = tempCount;
//		}
//		if(info.getInfo(CmdInfo.CMD_FLAG) == CmdInfo.SOCKET_DISCONNECT_CMD){
//			System.out.println("last count:"+count);
//		}
		System.out.println(info.getInfo(CmdInfo.CMD_FLAG)+" "+info.getInfo(CmdInfo.DATA_FLAG));
	}
}
