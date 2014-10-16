package server.tray.listener;

import server.tray.WindowTrayServer;

public abstract class CmdAdapter implements ICmdListener {
	protected WindowTrayServer server = null;

	public void init(){
		
	}
	public void setTrayServer(WindowTrayServer server){
		this.server = server;
	}

}
