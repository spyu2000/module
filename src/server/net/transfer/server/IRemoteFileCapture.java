package server.net.transfer.server;

import java.io.File;

public interface IRemoteFileCapture {
	public File getRemoteFile(String appendInfo);
}
