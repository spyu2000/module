package server.tray.listener;

public class NotifyCmdListener extends CmdAdapter {

	public void cmdHappened(String cmd) {
		this.server.showInfo(cmd);
	}

}
