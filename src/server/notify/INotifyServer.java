package server.notify;

import java.util.Date;

import server.mail.MailServer;

import com.fleety.base.FleetyThread;
import com.fleety.base.GeneralConst;
import com.fleety.server.BasicServer;
import com.fleety.server.IServer;
import com.fleety.server.ServerContainer;

public class INotifyServer extends BasicServer {
	public static final int ERROR_LEVEL = 1;
	public static final int WARN_LEVEL = 10;
	public static final int INFO_LEVEL = 20;
	public static final int DEBUG_LEVEL = 30;
	
	private static final INotifyServer singleInstance = new INotifyServer();
	public static INotifyServer getSingleInstance(){
		return singleInstance;
	}
	
	private int sendLevel = DEBUG_LEVEL;
	private INotifyServer(){
		this.startServer();
	}
	public boolean startServer() {
		if(this.getIntegerPara("send_level") != null){
			this.sendLevel = this.getIntegerPara("send_level").intValue();
		}
		this.isRunning = true;
		return this.isRunning();
	}

	public void stopServer(){
		super.stopServer();
	}
	
	
	public void notifyInfo(String title,String content,int level){
		if(level > this.sendLevel){
			return;
		}
		
		IServer server = ServerContainer.getSingleInstance().getServer(FleetyThread.MODULE_MONITOR_MAIL_NAME);
		if(server != null && server.isRunning() && server instanceof MailServer){
			String mailStr = (String)server.getPara("receiver");
			if(mailStr == null){
				return ;
			}
			title = title +"["+server.getPara("customer_name")+"]" + GeneralConst.YYYY_MM_DD_HH_MM_SS.format(new Date());
			((MailServer)server).sendMail(mailStr.split(";"), title, content);
		}
	}
}
