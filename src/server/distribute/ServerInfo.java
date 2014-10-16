/**
 * ¾øÃÜ Created on 2009-8-11 by edmund
 */
package server.distribute;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class ServerInfo implements Serializable{
	private String serverName = null;
	private List jarList = new ArrayList(4);
	private String mainClass = null;
	private HashMap paraMapping = null;
	
	
	public ServerInfo setServerName(String serverName){
		this.serverName = serverName;
		return this;
	}
	public String getServerName(){
		return this.serverName;
	}
	
	public ServerInfo addJarInfo(JarInfo jarInfo){
		this.jarList.add(jarInfo);
		return this;
	}
	public List getJarInfoList(){
		return this.jarList;
	}
	
	public String getMainClass(){
		return this.mainClass;
	}
	public ServerInfo setMainClass(String mainClass){
		this.mainClass = mainClass;
		return this;
	}
	
	public void addPara(String key,String value){
		if(this.paraMapping == null){
			this.paraMapping = new HashMap();
		}
		this.paraMapping.put(key, value);
	}
	public void appendPara(TaskServer server){
		if(this.paraMapping != null){
			Object key,value;
			for(Iterator itr = this.paraMapping.keySet().iterator();itr.hasNext();){
				key = itr.next();
				value = this.paraMapping.get(key);
				server.addPara(key, value);
			}
		}
	}
}
