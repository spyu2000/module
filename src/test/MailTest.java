package test;

import java.util.Date;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Vector;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;

public class MailTest
{
	public MailTest() {
		super();
	}

	static String username="";
	static String password="";
	public static void main(String[] args) {

		try
		{
			String host="192.168.0.42";
			String from = "monitor@monitor.com";
			String to="edmund.xu@fleety.com";
			String subject="Monitor Test";
			String content = "���²���";
			Vector file = new Vector();
			file.add("c:/data.txt");
			String filename;
			
			Properties props = System.getProperties();
			  props.put("mail.smtp.host",host);
			  props.put("mail.smtp.auth","false");
			  Session session=Session.getDefaultInstance(props, new Authenticator(){
			   public PasswordAuthentication getPasswordAuthentication(){
			    return new PasswordAuthentication(username,password); 
			   }
			  });
			  
			    //����MimeMessage ���趨������ֵ
			    MimeMessage msg = new MimeMessage(session);
			    msg.setFrom(new InternetAddress(from));
			    InternetAddress[] address={new InternetAddress(to)};
			    msg.setRecipients(Message.RecipientType.TO,address);
			    subject = transferChinese(subject);
			    msg.setSubject(subject);
			    
			    //����Multipart
			    Multipart mp = new MimeMultipart();
			    
			    //��Multipart�������
			    MimeBodyPart mbpContent = new MimeBodyPart();
			    mbpContent.setText(content);
			    //��MimeMessage��ӣ�Multipart�������ģ�
			    mp.addBodyPart(mbpContent);
			    
			    //��Multipart��Ӹ���
			    Enumeration efile=file.elements();
			    while(efile.hasMoreElements()){
			    
			      MimeBodyPart mbpFile = new MimeBodyPart();
			      filename=efile.nextElement().toString();
			      FileDataSource fds = new FileDataSource(filename);
			      mbpFile.setDataHandler(new DataHandler(fds));
			      mbpFile.setFileName(fds.getName());
			      //��MimeMessage��ӣ�Multipart��������
			      mp.addBodyPart(mbpFile);

			    }
			    
			    file.removeAllElements();
			    //��Multipart���MimeMessage
			    msg.setContent(mp);
			    msg.setSentDate(new Date());
			    //�����ʼ�
			    Transport.send(msg);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	public static String transferChinese(String strText){
		  try{
		    strText = MimeUtility.encodeText(new String(strText.getBytes(), "GB2312"), "GB2312", "B");
		  }catch(Exception e){
		    e.printStackTrace();
		  }
		  return strText;
		}
}
