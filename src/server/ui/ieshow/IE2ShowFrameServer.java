/**
 * ¾øÃÜ Created on 2010-2-25 by edmund
 */
package server.ui.ieshow;

import javax.swing.SwingUtilities;

import chrriis.dj.nativeswing.swtimpl.NativeInterface;

import com.fleety.server.BasicServer;

public class IE2ShowFrameServer extends BasicServer{
	protected IE2Frame ieFrame = null;
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
		
		NativeInterface.open();
		SwingUtilities.invokeLater(new InvokeObject());
		NativeInterface.runEventPump();
		
		this.isRunning = true;
		return true;
	}
	
	public void setCommand(ICommand command){
		this.ieFrame.setCommand(command);
	}

	public void stopServer(){
		this.isRunning = false;
	}
	
	private class InvokeObject implements Runnable{
		public void run(){
			ieFrame = new IE2Frame(getStringPara("frame_title"),getStringPara("icon_name"));
			ieFrame.setUrlPath(getStringPara("default_url"));
			ieFrame.init();
		}
	}
}
