package server.mail;
import java.net.UnknownHostException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;
public class Mail {
	private Properties prop; // 系统属性
	private MimeMessage mimeMsg; // MIME邮件对象
	private Multipart mp; // Multipart对象,邮件内容,标题,附件等内容均添加到其中后再生成MimeMessage对象
	private Transport transport = null;
	private String userName = ""; // smtp认证用户名和密码
	private String password = "";
    //是否要求安全连接
    private  boolean is_need_ssl=false;
    //发送邮件端口
    private int smtp_port;

	public Mail(String smtp,boolean is_need_ssl,int smtp_port) {
	    this.is_need_ssl=is_need_ssl;
	    this.smtp_port=smtp_port;
        if (prop == null){
        	prop = new Properties();
        	prop.putAll(System.getProperties());// 获得系统属性对象
        }
		setNeedAuth(true);
		setSmtpHost(smtp);// 如果没有指定邮件服务器,就从getConfig类中获取
		createMimeMessage();
	}

	public void setSmtpHost(String hostName) {
		System.out.println("设置系统属性：mail.smtp.host =" + hostName);
		prop.put("mail.smtp.host", hostName); // 设置SMTP主机

        if(is_need_ssl)
        {
            prop.setProperty("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            prop.setProperty("mail.smtp.socketFactory.fallback", "false");
        }
        if(smtp_port!=0)
        {
            prop.setProperty("mail.smtp.port", smtp_port+"");
            prop.setProperty("mail.smtp.socketFactory.port", smtp_port+"");
        }
	}

	public boolean createMimeMessage() {
		Session session; // 邮件会话对象
		try {
			System.out.println("准备获取邮件会话对象！");
			session = Session.getDefaultInstance(prop, null); // 获得邮件会话对象
		} catch (Exception e) {
			System.err.println("获取邮件会话对象时发生错误！" + e);
			return false;
		}
		System.out.println("准备创建MIME邮件对象！");
		try {
			mimeMsg = new MimeMessage(session);// 创建MIME邮件对象
			return true;
		} catch (Exception e) {
			System.err.println("创建MIME邮件对象失败！" + e);
			return false;
		}
	}

	public void setNeedAuth(boolean need) {
		if (prop == null){
			prop = new Properties();
        	prop.putAll(System.getProperties());// 获得系统属性对象
		}
		if (need) {
			prop.put("mail.smtp.auth", "true");
		} else {
			prop.put("mail.smtp.auth", "false");
		}
	}

	public void setNamePass(String userName, String password) {
		this.userName = userName;
		this.password = password;
	}

	public boolean setSubject(String subject) {
		try {
			mimeMsg.setSubject(subject);
			return true;
		} catch (Exception e) {
			System.err.println("设置邮件主题发生错误！");
			return false;
		}
	}

	public boolean setBody(String body) {
		try {
			BodyPart bp = new MimeBodyPart();
			bp.setContent(
					"<meta http-equiv=Content-Type content=text/html; charset=GB2312>"
							+ body, "text/html;charset=GB2312");
			mp.addBodyPart(bp);
			return true;
		} catch (Exception e) {
			System.err.println("设置邮件正文时发生错误！" + e);
			return false;
		}
	}

	public boolean addFileAffix(String fileName) {
		try {
			BodyPart bp = new MimeBodyPart();
			FileDataSource fileds = new FileDataSource(fileName);
			bp.setDataHandler(new DataHandler(fileds));
			bp.setFileName(MimeUtility.encodeText(fileds.getName()));
			mp.addBodyPart(bp);
			return true;
		} catch (Exception e) {
			System.err.println("增加邮件附件：" + fileName + "发生错误！" + e);
			return false;
		}
	}

	public boolean setFrom(String from) {
		try {
			mimeMsg.setFrom(new InternetAddress(from));// 设置发信人
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public boolean setTo(String to) {
		if (to == null)
			return false;
		try {
			mimeMsg.setRecipients(Message.RecipientType.TO, InternetAddress
					.parse(to));
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public boolean setTo(InternetAddress[] to) {
		if (to == null)
			return false;
		try {
			mimeMsg.setRecipients(Message.RecipientType.TO, to);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public boolean setCopyTo(String copyTo) {
		if (copyTo == null)
			return false;
		try {
			mimeMsg.setRecipients(Message.RecipientType.CC,
					(Address[]) InternetAddress.parse(copyTo));
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * 发送email给特定的用户(同时发给多人)
	 * 
	 * @param receiver
	 *            接收email的人
	 * @param subject
	 *            邮件主题
	 * @param content
	 *            发送的内容
	 * @throws SQLException
	 * @throws Exception
	 * @return falg 1:成功， 2：email地址错误，3：网络异常
	 */
	public int sendEmail(String[] receiver, String subject, Object content,
			String[] fileName) throws Exception {
		int flag = 3;
		try {
			//每发送一封邮件新建一个MimeMultipart对象
			mp = new MimeMultipart();
			List address = new ArrayList(receiver.length);
			for (int i = 0; i < receiver.length; i++) {
				if (receiver[i] == null || receiver[i].equals(""))
					continue;
				address.add(InternetAddress.parse(receiver[i].trim())[0]);
			}
			if(content==null)
				content="";
			if (address.size() == 0)
				return 1;
			InternetAddress[] to = new InternetAddress[address.size()];
			address.toArray(to);
			setBody("<meta http-equiv=Content-Type content=text/html; charset=gb2312>"+ content.toString());
			setTo(to);
			setSubject(subject);
			if (!isNullorEmpty(fileName)) {
				for (int i = 0; i < fileName.length; i++) {
					if (!isNullorEmpty(fileName[i]))
						addFileAffix(fileName[i]);
				}
			}
			//设置好邮件标题，内容附件之后，将其放入MimeMessage对象（如果次序颠倒会出现<div align=center>之类的东西在邮件中）
			mimeMsg.setContent(mp);
			mimeMsg.saveChanges();
			flag = sendOut();
		} catch (Exception e) {
			throw e;
		}
		return flag;
	}

	public void connect() throws Exception{
		try {
			Session mailSession =Session.getDefaultInstance(prop); 
			transport = mailSession.getTransport("smtp");
			System.out.println((String) prop.get("mail.smtp.host")+" "+userName+" "+password)			;
			transport.connect((String) prop.get("mail.smtp.host"),userName,
					password);
		}  catch (Exception e) {
//			e.printStackTrace();
			throw e;
		}
	}

	public void closeConnect() {
		try {
			if(transport!=null)
			transport.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public int sendOut() throws Exception {
		try {
			if (transport == null||!transport.isConnected())
			{
				connect();
			}
			transport.sendMessage(mimeMsg, mimeMsg
					.getRecipients(Message.RecipientType.TO));
			System.out.println("发送邮件成功！");
			return 1;
		}  catch (UnknownHostException e) {
			System.out.println("网络异常1");
			e.printStackTrace();
			return 3;
		} catch (IllegalStateException e) {
			System.out.println("网络异常2");
			e.printStackTrace();
			return 3;
		}catch (Exception e) {
			Throwable ourCause = e.getCause();
            if (ourCause != null){
    			 if((" "+ourCause.getMessage()+" ").indexOf(" 550 ")!=-1){
    				 ourCause.printStackTrace();
    				 return 2;
    			 }
            }
			throw e;
		}
	}

	//
	public static boolean isNullorEmpty(Object temp) {
		if (temp == null || (String.valueOf(temp)).trim().equals(""))
			return true;
		return false;
	}
}
