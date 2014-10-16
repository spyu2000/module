package server.net.transfer.server;

import java.io.File;

public class DefaultRemoteFileCapture implements IRemoteFileCapture {
	public File getRemoteFile(String appendInfo){
		return new File(appendInfo);
	}
}
