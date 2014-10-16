/**
 * ¾øÃÜ Created on 2010-2-25 by edmund
 */
package server.ui.ieshow;

import com.fleety.server.BasicServer;

public class IEShowFrameServer extends BasicServer{
	protected IEFrame ieFrame = null;
	public boolean startServer(){
		if(this.isRunning()){
			return true;
		}
		
		ICommand command = null;
		try{
			String cmdCls = this.getStringPara("command_class");
			if(cmdCls != null && cmdCls.trim().length() > 0){
				command = (ICommand)Class.forName(cmdCls.trim()).newInstance();
			}
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}
		
		ieFrame = new IEFrame(this.getStringPara("frame_title"),this.getStringPara("icon_name"),command);
		ieFrame.setUrlPath(this.getStringPara("default_url"));
		ieFrame.init();
		
		this.isRunning = true;
		return true;
	}
	
	public void setCommand(ICommand command){
		this.ieFrame.setCommand(command);
	}

	public void stopServer(){
		this.isRunning = false;
	}
}
