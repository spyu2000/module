package server.tray.listener;

public class DefaultCmdListener extends CmdAdapter {
	
	public void cmdHappened(String cmd) {
		if(cmd == null){
			return ;
		}
		
		if(cmd.equals(SYSTEM_EXIT_CMD)){
			System.exit(0);
		}
	}

}
