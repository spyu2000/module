/**
 * ¾øÃÜ Created on 2008-4-8 by edmund
 */
package com.fleety.server;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class ServerContainer{
	private static ServerContainer singleInstance = null;
	public static ServerContainer getSingleInstance(){
		if(singleInstance == null){
			synchronized(ServerContainer.class){
				if(singleInstance == null){
					singleInstance = new ServerContainer();
				}
			}
		}
		return singleInstance;
	}
	
	private HashMap serverMapping = new HashMap();
	private List serverNameList = new LinkedList();
	public IServer getServer(String serverName){
		return (IServer)serverMapping.get(serverName);
	}
	
	public ServerContainer addServer(IServer server){
		if(!serverMapping.containsKey(server.getServerName())){
			serverNameList.add(0, server.getServerName());
			serverMapping.put(server.getServerName(), server);
		}
		return this;
	}
	
	public Iterator iteratorServerName(){
		return this.serverNameList.iterator();
	}
}
