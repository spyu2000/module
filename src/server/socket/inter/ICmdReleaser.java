/**
 * ¾øÃÜ Created on 2008-5-15 by edmund
 */
package server.socket.inter;

public interface ICmdReleaser{
	public void init(Object caller);
	
	public void releaseCmd(CmdInfo info);
}
