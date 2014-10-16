package server.manager.app.autoupdate.client;

import com.fleety.base.InfoContainer;

import server.remote_execute_by_rmi.IRemoteExecute;

public class DetectCmd implements IRemoteExecute {

	public Object execute(InfoContainer para) {
		return new Integer(0);
	}

}
