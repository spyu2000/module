/**
 * ¾øÃÜ Created on 2011-1-12 by edmund
 */
package server.db.test;

import java.util.Timer;
import java.util.TimerTask;

import server.db.DbServer;
import server.threadgroup.ThreadPoolGroupServer;

import com.fleety.base.FleetyThread;
import com.fleety.base.test.TestUnit;
import com.fleety.util.pool.db.DbConnPool.DbHandle;
import com.fleety.util.pool.db.DbConnPool.StatementHandle;

public class Test{

	private DbServer createServer(){
		DbServer server = new DbServer();

		server.addPara("driver", "oracle.jdbc.driver.OracleDriver");
		server.addPara("url", "jdbc:oracle:thin:@192.168.0.31:1521:demo");
		server.addPara("detect_cycle_time","100000");
		server.addPara("user", "gps");
		server.addPara("pwd", "gps");
		server.addPara("use_time", "10000");
		server.addPara("init_num", "5");
		server.addPara("min_num", "5");
		server.addPara("max_num", "10");
		server.addPara("enable_stack", "true");
		
		if(!server.startServer()){
			server.stopServer();
			return null;
		}
		
		return server;
	}
	
	public void test_release(){
		
	}
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args){
		TestUnit.executeTest(new Test());
	}

}
