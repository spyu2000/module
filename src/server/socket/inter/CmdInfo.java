/**
 * 绝密 Created on 2008-5-15 by edmund
 */
package server.socket.inter;

import com.fleety.base.InfoContainer;

public class CmdInfo extends InfoContainer{
	public static final String SOCKET_CONNECT_CMD = "connect";
	public static final String SOCKET_DISCONNECT_CMD = "disconnect";

	//Socket被关闭的四个可能性枚举原因.正常关闭.超时关闭.IO读写错误关闭.其它情况的关闭
	public static final Object CLOSE_OK_CODE = new Object(){
		public String toString(){return "正常关闭";}
	};
	public static final Object CLOSE_TIMEOUT_CODE = new Object(){
		public String toString(){return "超时关闭";}
	};
	public static final Object CLOSE_IO_ERROR_CODE = new Object(){
		public String toString(){return "读写错误关闭";}
	};
	public static final Object CLOSE_REGIST_ERROR_CODE = new Object(){
		public String toString(){return "注册错误关闭";}
	};
	public static final Object CLOSE_OTHER_CODE = new Object(){
		public String toString(){return "未知原因关闭";}
	};
	
	public static final Object CMD_FLAG = new Object();
	public static final Object HEAD_FLAG=new Object();
	public static final Object DATA_FLAG = new Object();
	public static final Object SOCKET_FLAG = new Object();
	public static final Object SOCKET_CLOSE_CODE_FLAG = new Object();
	public static final Object MSG_ID = new Object();
}
