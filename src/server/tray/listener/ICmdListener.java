package server.tray.listener;

import server.tray.WindowTrayServer;

public interface ICmdListener {
	public static final String SYSTEM_EXIT_CMD = "_EXIT";
	public static final String SHOW_WINDOW_CMD = "_SHOW";
	public static final String HIDE_WINDOW_CMD = "_HIDE";

	public void init();
	public void setTrayServer(WindowTrayServer server);
	public void cmdHappened(String cmd);
	
}
