/**
 * ���� Created on 2008-5-15 by edmund
 */
package server.socket.inter;

import com.fleety.base.InfoContainer;

public class CmdInfo extends InfoContainer{
	public static final String SOCKET_CONNECT_CMD = "connect";
	public static final String SOCKET_DISCONNECT_CMD = "disconnect";

	//Socket���رյ��ĸ�������ö��ԭ��.�����ر�.��ʱ�ر�.IO��д����ر�.��������Ĺر�
	public static final Object CLOSE_OK_CODE = new Object(){
		public String toString(){return "�����ر�";}
	};
	public static final Object CLOSE_TIMEOUT_CODE = new Object(){
		public String toString(){return "��ʱ�ر�";}
	};
	public static final Object CLOSE_IO_ERROR_CODE = new Object(){
		public String toString(){return "��д����ر�";}
	};
	public static final Object CLOSE_REGIST_ERROR_CODE = new Object(){
		public String toString(){return "ע�����ر�";}
	};
	public static final Object CLOSE_OTHER_CODE = new Object(){
		public String toString(){return "δ֪ԭ��ر�";}
	};
	
	public static final Object CMD_FLAG = new Object();
	public static final Object HEAD_FLAG=new Object();
	public static final Object DATA_FLAG = new Object();
	public static final Object SOCKET_FLAG = new Object();
	public static final Object SOCKET_CLOSE_CODE_FLAG = new Object();
	public static final Object MSG_ID = new Object();
}
