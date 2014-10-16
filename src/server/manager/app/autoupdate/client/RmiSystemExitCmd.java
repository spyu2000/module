package server.manager.app.autoupdate.client;

import com.fleety.base.InfoContainer;

import server.remote_execute_by_rmi.IRemoteExecute;

public class RmiSystemExitCmd implements IRemoteExecute {
	public Object execute(InfoContainer para) {
		System.exit(23893);
		return null;
	}
}
