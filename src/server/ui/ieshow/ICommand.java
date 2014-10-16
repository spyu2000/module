package server.ui.ieshow;

import nothome.mswindows.IECanvas;

public interface ICommand {
	public void setIECanvas(IECanvas canvas);
	
//	发送命令到IE
	public boolean sendCommand(String msg,String para);
	
//	IE发送命令到java
	public void commandArrived(String msg,String para);
}
