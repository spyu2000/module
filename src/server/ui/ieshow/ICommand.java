package server.ui.ieshow;

import nothome.mswindows.IECanvas;

public interface ICommand {
	public void setIECanvas(IECanvas canvas);
	
//	�������IE
	public boolean sendCommand(String msg,String para);
	
//	IE�������java
	public void commandArrived(String msg,String para);
}
