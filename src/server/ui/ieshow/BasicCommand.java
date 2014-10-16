package server.ui.ieshow;

import nothome.mswindows.IECanvas;

public class BasicCommand implements ICommand{
	private IECanvas canvas = null;
	public void setIECanvas(IECanvas canvas){
		this.canvas = canvas;
	}
	
	//发送命令到IE
	public boolean sendCommand(String msg,String para){
		if(this.canvas == null){
			return false;
		}

		msg = msg.replaceAll("'", "\\\\'");
		para = para.replaceAll("'", "\\\\'");
		this.canvas.executeJavascript("commandArrived('"+msg+"','"+para+"');");
		
		return true;
	}
	
	//IE发送命令到java
	public void commandArrived(String msg,String para){
		
	}
}
