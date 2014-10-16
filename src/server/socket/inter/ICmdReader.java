/**
 * ¾øÃÜ Created on 2008-5-15 by edmund
 */
package server.socket.inter;


public interface ICmdReader{
	public void init(Object caller);
	public CmdInfo[] readCmd(ConnectSocketInfo connInfo) throws Exception;
}
