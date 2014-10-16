/**
 * ¾øÃÜ Created on 2009-8-13 by edmund
 */
package server.distribute;

import java.io.Serializable;

public class JarInfo implements Serializable{
	public String serverName = null;
	public String jarPath = null;
	public String name = null;
	public long modifyTime = 0;
	public long size = 0;
	
	public JarInfo(String serverName,String name,String jarPath,long modifyTime,long size){
		this.serverName = serverName;
		this.name = name;
		this.jarPath = jarPath;
		this.modifyTime = modifyTime;
		this.size = size;
	}
}
