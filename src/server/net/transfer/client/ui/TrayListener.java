package server.net.transfer.client.ui;

import server.tray.listener.DefaultCmdListener;

public class TrayListener extends DefaultCmdListener {

	public void cmdHappened(String cmd) {
		super.cmdHappened(cmd);
		
		if(cmd.equals(SHOW_WINDOW_CMD)){
			((SendAndReceiveMonitorServer)this.server).enableVisible(true);
		}else if(cmd.equals(HIDE_WINDOW_CMD)){
			((SendAndReceiveMonitorServer)this.server).enableVisible(false);
		}
		
	}

}
