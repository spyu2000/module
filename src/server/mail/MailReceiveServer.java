/**
 * 绝密 Created on 2010-5-21 by edmund
 */
package server.mail;

import java.security.Security;
import java.util.Properties;
import javax.mail.*;
import javax.net.ssl.SSLSocketFactory;

import server.threadgroup.ThreadPoolGroupServer;

import com.fleety.server.BasicServer;
import com.fleety.util.pool.timer.FleetyTimerTask;
import com.fleety.util.pool.timer.TimerPool;

public class MailReceiveServer extends BasicServer{
	private Session session = null;
	private Store store = null;
	private Folder folder = null;
	
	private String folderName = null;
	private String protocol = null;
	private String mailHost = null;
	private int port = 110;
	private String userName = null;
	private String password = null;
	
	private TimerPool timer = null;
	private long detectCycle = 60*1000;
	
	public boolean startServer(){
		this.protocol = this.getStringPara("protocol");
		if(this.protocol == null || this.protocol.trim().length() == 0){
			this.protocol = "pop3";
		}
		this.folderName = this.getStringPara("folder");
		if(this.folderName == null){
			this.folderName = "INBOX";
		}
		
		this.mailHost = this.getStringPara("mail_host");
		this.userName = this.getStringPara("user_name");
		this.password = this.getStringPara("user_pwd");
		
		
		Integer mailPort = this.getIntegerPara("mail_port");
		if(mailPort != null){
			this.port = mailPort.intValue();
		}

		String tempStr = null;
		tempStr = this.getStringPara("detect_cycle");
		if(tempStr != null && tempStr.trim().length() > 0){
			try{
				this.detectCycle = Long.parseLong(tempStr.trim());
			}catch(Exception e){
				e.printStackTrace();
			}
		}

		Properties props = System.getProperties();
		
		tempStr = this.getStringPara("ssl");
		if(tempStr!=null && tempStr.trim().equals("true")){
			Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());
			props.setProperty("mail.pop3.socketFactory.fallback", "false");
			props.setProperty("mail.pop3.socketFactory.class", SSLSocketFactory.class.getName()); 
		}
		this.session = Session.getDefaultInstance(props,null);
		
		
		//邮件收取服务，定时器必须是作为非守护的，因为其可能独立存在。
		this.timer = ThreadPoolGroupServer.getSingleInstance().createTimerPool(MailReceiveServer.class.getName()+"["+this.hashCode()+"]");
		this.timer.schedule(new FleetyTimerTask(){
			public void run(){
				receiveMail();
			}
		}, 0,this.detectCycle);
		
		this.listener = new IMailListener(){
			public void mailReceived(Message mail) throws Exception{
				System.out.println("\n\n\n\nMail:");
				System.out.println(mail.getFrom()[0]);
				System.out.println(mail.getSubject());
				Object content = mail.getContent();
				BodyPart bpart;
				 if (content instanceof Multipart){
					 for(int i=0;i<((Multipart) content).getCount();i++){
						 bpart = ((Multipart) content).getBodyPart(i);
						 System.out.println(bpart.getClass().getName());
						 System.out.println(bpart.getContent().getClass().getName());
						 System.out.println("---- "+bpart.getContentType()+"---"+bpart.getContent());
					 }
			     }else{
			    	 System.out.println(content);
			     }
			}
		};
        
		return true;
	}
	
	private IMailListener listener = null;
	public void setMailListener(IMailListener listener){
		this.listener = listener;
	}
	
	private void receiveMail(){
		try{
			if(!this.connect()){
				return ;
			}
			Message[] msgArr = this.folder.getMessages();
			if(msgArr == null){
				return ;
			}
			for(int i=0;i<msgArr.length;i++){
				try{
					this.listener.mailReceived(msgArr[i]);
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			this.disconnect();
		}
		
	}
	
	private boolean connect(){
		this.disconnect();
		
		try{
			this.store = session.getStore(this.protocol);
			
			this.store.connect(this.mailHost, this.port, this.userName, this.password);
			if(this.folderName == null){
				this.folder = this.store.getDefaultFolder();
			}else{
				this.folder = this.store.getFolder(this.folderName);
			}
			this.folder.open(Folder.READ_WRITE);
			return true;
		}catch(Exception e){
			e.printStackTrace();
			this.store = null;
		}
		return false;
	}
	private void disconnect(){
		try{
			if(this.folder != null){
				this.folder.close(true);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		try{
			if(this.store != null){
				this.store.close();
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		this.folder = null;
		this.store = null;
	}

	public void stopServer(){
		this.session = null;
		this.disconnect();
		this.timer.cancel();
	}
	

	public static void main(String[] argv){
		MailReceiveServer server = new MailReceiveServer();
		
		server.addPara("mail_host", "72.14.213.109");
		server.addPara("mail_port", "995");
		server.addPara("user_name", "jababekaiflow");
		server.addPara("user_pwd", "fleety123");
		server.addPara("ssl", "true");
		
		server.startServer();
	}
}
