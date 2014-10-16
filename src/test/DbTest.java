/**
 * ¾øÃÜ Created on 2008-6-3 by edmund
 */
package test;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import server.db.DbServer;
import server.mail.MailServer;
import server.mapping.ActionContainerServer;
import com.fleety.base.xml.XmlNode;
import com.fleety.server.BasicServer;
import com.fleety.server.ServerContainer;

import java.util.Date;

import server.track.TrackServer;

import com.fleety.base.GeneralConst;
import com.fleety.base.InfoContainer;
import com.fleety.track.TrackIO;

import com.fleety.util.pool.db.DbConnPool;
import com.fleety.util.pool.db.DbConnPool.DbHandle;
import com.fleety.util.pool.db.DbConnPool.StatementHandle;

public class DbTest extends BasicServer
{
	static DbHandle conn  = null;
	StatementHandle stmt = null;
	public boolean startServer(){
		try{
			conn = DbServer.getSingleInstance().getConn();
			StatementHandle stmt = conn.createStatement();
			
			HashMap mapping = new HashMap(1000*100);
			
			int count = 1;
			StatementHandle pstmt = conn.prepareStatement("insert into v3_city_mark_bak values(?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
			ResultSet sets = stmt.executeQuery("select * from v3_city_mark where rownum<100000");
			while(sets.next()){
				pstmt.setInt(1, sets.getInt(1));
				pstmt.setInt(2, sets.getInt(2));
				pstmt.setString(3, sets.getString(3));
				pstmt.setString(4, sets.getString(4));
				pstmt.setString(5, sets.getString(5));
				pstmt.setInt(6, sets.getInt(6));
				pstmt.setString(7, sets.getString(7));
				pstmt.setString(8, sets.getString(8));
				pstmt.setInt(9, sets.getInt(9));
				pstmt.setString(10, sets.getString(10));
				pstmt.setDouble(11, sets.getDouble(11));
				pstmt.setDouble(12, sets.getDouble(12));
				pstmt.setString(13, sets.getString(13));
				pstmt.setDate(14, sets.getDate(14));
				
				pstmt.executeUpdate();
//				pstmt.addBatch();
//				
//				if(count % 5000 == 0){
//					pstmt.executeBatch();
					System.out.println("Ö´ÐÐÊýÁ¿:"+count);
//					count = 1;
//				}
				count ++;
			}
			sets.close();
			pstmt.executeBatch();
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			DbServer.getSingleInstance().releaseConn(conn);
		}
		return true;
	}
	public void stopServer(){
		
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
//		MailServer mail = new MailServer();
//		mail.setServerName("module monitor mail server");
//		mail.addPara("smtp_host", "203.156.205.46");
//		mail.addPara("mail_account", "customer@fleety.com");
//		mail.addPara("mail_password", "123fleety");
//		mail.addPara("mail_save_path", "mailStore");
//		mail.addPara("polling_interval", "10");
//		mail.addPara("interval_days", "2");
//		mail.addPara("is_delete_history_mail", "true");
//		mail.addPara("save_mail_days", "5");
//		mail.addPara("customer_name", "xjs_test");
//		mail.addPara("receiver", "edmund.xu@fleety.com;shipeng.yu@fleety.com;sunny.zhang@fleety.com;zhenquan.jiang@fleety.com");
//		mail.startServer();
//		ServerContainer.getSingleInstance().addServer(mail);
		
		DbServer server = DbServer.getSingleInstance();

		server.addPara("driver", "oracle.jdbc.driver.OracleDriver");
		server.addPara("url_bak", "jdbc:oracle:thin:@192.168.0.22:1521:demo");
		server.addPara("url", "jdbc:oracle:thin:@192.168.0.31:1521:demo");
		server.addPara("user", "iflow");
		server.addPara("pwd", "iflow123");
		server.addPara("init_num", "2");
		server.addPara("min_num", "2");
		server.addPara("max_num", "5");
		server.addPara("enable_stack", "true");
		
		System.out.println(server.startServer());

		
		Thread.sleep(10000000);
	}
	
}
