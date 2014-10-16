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
	private Properties prop; // ϵͳ����
	private MimeMessage mimeMsg; // MIME�ʼ�����
	private Multipart mp; // Multipart����,�ʼ�����,����,���������ݾ���ӵ����к�������MimeMessage����
	private Transport transport = null;
	private String userName = ""; // smtp��֤�û���������
	private String password = "";
    //�Ƿ�Ҫ��ȫ����
    private  boolean is_need_ssl=false;
    //�����ʼ��˿�
    private int smtp_port;

	public Mail(String smtp,boolean is_need_ssl,int smtp_port) {
	    this.is_need_ssl=is_need_ssl;
	    this.smtp_port=smtp_port;
        if (prop == null){
        	prop = new Properties();
        	prop.putAll(System.getProperties());// ���ϵͳ���Զ���
        }
		setNeedAuth(true);
		setSmtpHost(smtp);// ���û��ָ���ʼ�������,�ʹ�getConfig���л�ȡ
		createMimeMessage();
	}

	public void setSmtpHost(String hostName) {
		System.out.println("����ϵͳ���ԣ�mail.smtp.host =" + hostName);
		prop.put("mail.smtp.host", hostName); // ����SMTP����

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
		Session session; // �ʼ��Ự����
		try {
			System.out.println("׼����ȡ�ʼ��Ự����");
			session = Session.getDefaultInstance(prop, null); // ����ʼ��Ự����
		} catch (Exception e) {
			System.err.println("��ȡ�ʼ��Ự����ʱ��������" + e);
			return false;
		}
		System.out.println("׼������MIME�ʼ�����");
		try {
			mimeMsg = new MimeMessage(session);// ����MIME�ʼ�����
			return true;
		} catch (Exception e) {
			System.err.println("����MIME�ʼ�����ʧ�ܣ�" + e);
			return false;
		}
	}

	public void setNeedAuth(boolean need) {
		if (prop == null){
			prop = new Properties();
        	prop.putAll(System.getProperties());// ���ϵͳ���Զ���
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
			System.err.println("�����ʼ����ⷢ������");
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
			System.err.println("�����ʼ�����ʱ��������" + e);
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
			System.err.println("�����ʼ�������" + fileName + "��������" + e);
			return false;
		}
	}

	public boolean setFrom(String from) {
		try {
			mimeMsg.setFrom(new InternetAddress(from));// ���÷�����
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
	 * ����email���ض����û�(ͬʱ��������)
	 * 
	 * @param receiver
	 *            ����email����
	 * @param subject
	 *            �ʼ�����
	 * @param content
	 *            ���͵�����
	 * @throws SQLException
	 * @throws Exception
	 * @return falg 1:�ɹ��� 2��email��ַ����3�������쳣
	 */
	public int sendEmail(String[] receiver, String subject, Object content,
			String[] fileName) throws Exception {
		int flag = 3;
		try {
			//ÿ����һ���ʼ��½�һ��MimeMultipart����
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
			//���ú��ʼ����⣬���ݸ���֮�󣬽������MimeMessage�����������ߵ������<div align=center>֮��Ķ������ʼ��У�
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
			System.out.println("�����ʼ��ɹ���");
			return 1;
		}  catch (UnknownHostException e) {
			System.out.println("�����쳣1");
			e.printStackTrace();
			return 3;
		} catch (IllegalStateException e) {
			System.out.println("�����쳣2");
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
