/*
 * 绝密 Created on 2006-12-27 by sunny
 */
package server.mail;

import java.io.File;
import java.util.Vector;

import server.threadgroup.PoolInfo;
import server.threadgroup.ThreadPoolGroupServer;

import com.fleety.server.BasicServer;
import com.fleety.util.pool.thread.ITask;
import com.fleety.util.pool.thread.ThreadPool;

/**@
 * @author sunny
 * 
 * 2006-12-19
 */
public class MailServer extends BasicServer {
	private  String smtp_host = "www.fleety.com";
	// email地址
	private  String mail_account = "sunny.zhang@fleety.com";
	// email密码
	private  String mail_password = "sunny.zhang";
	//是否要求安全连接
	private  boolean is_need_ssl=false;
	//是否匿名邮件
	private boolean is_anonymous = false;
	//发送邮件端口
	private int smtp_port=25;
	private  Mail mail = null;
	public static MailServer singleInstance = null;
	// 失败的队列
	private Vector queue = new Vector();
	// 发送邮件的线程池
	private String poolName = null;
	private ThreadPool pool = null;
	private StoreAndGetMail storeMailObj=null;
	

	public static MailServer getSingleInstance() {
		if (singleInstance == null) {
			synchronized (MailServer.class) {
				if (singleInstance == null) {
					singleInstance = new MailServer();
				}
			}
		}
		return singleInstance;
	}

	private void init() {
		try {
			String tempStr;
			tempStr = this.getStringPara("smtp_host");
			if (tempStr != null && !tempStr.equals("")) {
				smtp_host = tempStr;
			}
			tempStr = this.getStringPara("mail_account");
			if (tempStr != null && !tempStr.equals("")) {
				mail_account = tempStr;
			}
			tempStr = this.getStringPara("mail_password");
			if (tempStr != null && !tempStr.equals("")) {
				mail_password = tempStr;
			}
            tempStr = this.getStringPara("is_need_ssl");
            if (tempStr != null && !tempStr.equals("")) {
                is_need_ssl = tempStr.trim().equals("true");
            }
            tempStr = this.getStringPara("is_anonymous");
            if (tempStr != null && !tempStr.equals("")) {
                this.is_anonymous = tempStr.trim().equals("true");
            }
            tempStr = this.getStringPara("smtp_port");
            if (tempStr != null && !tempStr.equals("")) {
                try
                {
                    smtp_port = Integer.parseInt(tempStr);
                }
                catch(Exception e)
                {
                    e.printStackTrace();
                }
            }
			
			tempStr = this.getStringPara("mail_save_path");
			if (tempStr != null && !tempStr.equals("")) {
				storeMailObj.mail_save_path = tempStr;
			}
			tempStr = this.getStringPara("polling_interval");
			if (tempStr != null && !tempStr.equals("")) {
				storeMailObj.polling_interval = Long.parseLong(tempStr) * 60 * 1000;
			}
			tempStr = this.getStringPara("interval_days");
			if (tempStr != null && !tempStr.equals("")) {
				storeMailObj.interval_days = Integer.parseInt(tempStr);
				if (storeMailObj.interval_days < 1)
					storeMailObj.interval_days = 1;
			}
			tempStr = this.getStringPara("is_delete_history_mail");
			if (tempStr != null && !tempStr.equals("")) {
				storeMailObj.is_delete_history_mail = (tempStr
						.equals("true") ? true : false);
			}
			tempStr = this.getStringPara("save_mail_days");
			if (tempStr != null && !tempStr.equals("")) {
				storeMailObj.save_mail_days = Integer.parseInt(tempStr);
				if (storeMailObj.save_mail_days < storeMailObj.interval_days)
					storeMailObj.save_mail_days = storeMailObj.interval_days;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public boolean startServer() {
		try {
			 storeMailObj=new StoreAndGetMail();
			 storeMailObj.mailServer=this;
			init();
			setMailPara();
			this.isRunning = true;
			
			this.poolName = this.getServerName()+"["+this.hashCode()+"]";
			PoolInfo pInfo = new PoolInfo(ThreadPool.SINGLE_TASK_LIST_POOL,1,1000,true);
			this.pool = ThreadPoolGroupServer.getSingleInstance().createThreadPool(poolName, pInfo);

			storeMailObj.start();// 开启轮询线程，得到发送失败的邮件
			// 加载以前未发送的邮件
			storeMailObj.sendAllStroe(MailObj.STATE_INIT);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return true;
	}

	// 初始化邮件发送服务
	public void setMailPara() {
		try {
			System.out.println("初始化邮件发送服务::");
			mail = new Mail(smtp_host,is_need_ssl,smtp_port);
			mail.setFrom(mail_account);
			mail.setNeedAuth(!this.is_anonymous);
			mail.setNamePass(mail_account, mail_password);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void stopServer() {
		ThreadPoolGroupServer.getSingleInstance().removeThreadPool(this.poolName);
		if(this.pool != null){
			this.pool.stopWork();
		}
		if(this.storeMailObj != null){
			this.storeMailObj.stop();
		}
		super.stopServer();
	}
	
	// 是否群发
	public boolean is_Bulk = false;
	public synchronized boolean sendMail(String receiver, String head,
			String content) {
		return sendMail(new String[]{receiver}, head, content);
	}
	
	public synchronized boolean sendMail(String[] receiverArr, String head,
			String content) {
		return sendMail(receiverArr, head, content, null);
	}

	// 供外部调用
	public synchronized boolean sendMail(String[] receiverArr, String head,
			String content, File[] attachArr) {
		try {
			MailObj obj = new MailObj(receiverArr, head, content, attachArr);
			if (this.isRunning == false)
				return false;
			if (obj.getReceiverArr() == null
					|| obj.getReceiverArr().length == 0)
				return false;
			storeMailObj.storeMail(obj);
			if (obj.getReceiverArr().length > 1 && is_Bulk == false) {
				for (int i = 0; i < obj.getReceiverArr().length; i++) {
					if (obj.getReceiverArr()[i] == null || obj.getReceiverArr()[i].equals("")){
						continue;
					}
					MailObj objOne = new MailObj(new String[] { obj
							.getReceiverArr()[i] }, obj.getHead(), obj
							.getContent(), obj.getFile());
					objOne.setStorePath(obj.getStorePath());
					this.pool.addTask(new SendMailTask(objOne));
				}
			} else {
				this.pool.addTask(new SendMailTask(obj));
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	// 内部使用，获取以前保存未发送成功的邮件并发送
	protected void addMail(MailObj obj) {
		try {
			if (this.isRunning == false)
				return;
			this.pool.addTask(new SendMailTask(obj));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected void reSendAllFailMail() {
		while (!queue.isEmpty()) {
			SendMailTask task = (SendMailTask) queue.remove(0);
			this.pool.addTask(task);
		}
	}

	private class SendMailTask implements ITask {
		MailObj sended;

		public SendMailTask(MailObj sended) {
			this.sended = sended;
		}

		public Object getFlag() {
			return null;
		}

		public String getDesc() {
			return null;
		}

		public boolean execute() throws Exception {
			try {
				int flag = sendMail(sended);
				System.out.println("flag::" + flag + "  " + sended.getStorePath());
				// 发送成功，email地址错误，或重复发送次数达到指定的次数 将移出缓存
				if (sended.getState() != flag) {
					sended.setState(flag);
					storeMailObj.storeMailState(sended);
				}
				if (sended.getState() == MailObj.STATE_FAIL) {
					queue.add(this);
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				try {
					queue.add(this);
					Thread.sleep(10000);
					mail.closeConnect();
					mail.connect();
				} catch (Exception el) {
					el.printStackTrace();
				}
			}
			return true;
		}
	}

	private int sendMail(MailObj sended) throws Exception {
		int flag = 3;
		try {
			String[] receiverArr = sended.getReceiverArr();
			String head = sended.getHead();
			Object content = sended.getContent();
			File[] file = sended.getFile();
			String[] fileName = null;
			if (sended.getFile() != null) {
				fileName = new String[file.length];
				for (int i = 0; i < file.length; i++) {
					fileName[i] = file[i].getAbsolutePath();
				}
			}
			flag = mail.sendEmail(receiverArr, head, content, fileName);
		} catch (Exception e) {
			throw e;
		}
		return flag;
	}
}
