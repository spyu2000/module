package server.tray.listener;

public class PrintCmdListener extends CmdAdapter {

	public void cmdHappened(String cmd) {
		System.out.println("TrayCmd:"+cmd);
	}

}
