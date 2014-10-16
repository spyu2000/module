/**
 * 绝密 Created on 2008-6-23 by edmund
 */
package server.db;

import java.sql.ResultSet;
import java.util.HashMap;
import com.fleety.server.BasicServer;
import com.fleety.util.pool.db.DbConnPool;
import com.fleety.util.pool.db.DbConnPool.DbHandle;
import com.fleety.util.pool.db.DbConnPool.StatementHandle;

public class DbServer extends BasicServer{
	/**
	 * 可配置的参数信息
	 */
	public static final String DRIVER_FLAG = "driver";
	public static final String DB_URL_FLAG = "url";
	public static final String DB_URL_BAK_FLAG = "url_bak";
	public static final String DB_USER_FLAG = "user";
	public static final String DB_PWD_FLAG = "pwd";
	public static final String CONN_INIT_NUM_FLAG = "init_num";
	public static final String CONN_MIN_NUM_FLAG = "min_num";
	public static final String CONN_MAX_NUM_FLAG = "max_num";
	public static final String DEFAULT_WAIT_TIME_FLAG = "wait_time";
	public static final String DEFAULT_USE_TIME_FLAG = "use_time";
	public static final String MAX_IDLE_TIME_FLAG = "max_idle_time";
	public static final String DETECT_CYCLE_TIME_FLAG = "detect_cycle_time";
	public static final String DATA_BASE_TYPE_FLAG = "database_type";
	public static final String ORACLE = "oracle";
	public static final String SQLSERVER = "sqlserver";
	
	public static final String ENABLE_STACK_FLAG = "enable_stack";
	
	private DbConnPool connPool = null;
	
	private static DbServer singleInstance = null;
	public static DbServer getSingleInstance(){
		if(singleInstance == null){
			synchronized(DbServer.class){
				if(singleInstance == null){
					singleInstance = new DbServer();
				}
			}
		}
		
		return singleInstance;
	}
	
	public boolean startServer(){
		if(this.isRunning()){
			return true;
		}
		this.connPool = new DbConnPool();
		
		String driver = null,url = null,urlBak = null,user=null,pwd = null;
		try{
			driver = this.getStringPara(DRIVER_FLAG);
			url = this.getStringPara(DB_URL_FLAG);
			urlBak = this.getStringPara(DB_URL_BAK_FLAG);
			user = this.getStringPara(DB_USER_FLAG);
			pwd = this.getStringPara(DB_PWD_FLAG);
			
			if(driver == null || url == null || user == null){
				throw new Exception("数据库参数错误:driver="+driver+"; url="+url+"; user="+user+"!");
			}
			if(urlBak != null && urlBak.trim().length() == 0){
				urlBak = null;
			}
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}
		if(urlBak == null){
			this.connPool.setup(driver, url, user, pwd);
		}else{
			this.connPool.setup(driver, new String[]{url,urlBak}, user, pwd);
		}
		
		String temp;
		int initNum = 2;
		int minNum = 2;
		int maxNum = 10;
		try{
			temp = this.getStringPara(CONN_INIT_NUM_FLAG);
			if(temp != null && temp.trim().length() > 0){
				initNum = Integer.parseInt(temp.trim(),10);
			}
			
			temp = this.getStringPara(CONN_MIN_NUM_FLAG);
			if(temp != null && temp.trim().length() > 0){
				minNum = Integer.parseInt(temp.trim(),10);
			}
			
			temp = this.getStringPara(CONN_MAX_NUM_FLAG);
			if(temp != null && temp.trim().length() > 0){
				maxNum = Integer.parseInt(temp.trim(),10);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		this.connPool.setConnNum(initNum, minNum, maxNum);
		

		int waitTime = 1000;
		int useTime = 10000;
		int maxIdleTime = 600000;
		int detectCycleTime = 60000;
		try{
			temp = this.getStringPara(DEFAULT_WAIT_TIME_FLAG);
			if(temp != null && temp.trim().length() > 0){
				waitTime = Integer.parseInt(temp.trim(),10);
			}
			
			temp = this.getStringPara(DEFAULT_USE_TIME_FLAG);
			if(temp != null && temp.trim().length() > 0){
				useTime = Integer.parseInt(temp.trim(),10);
			}
			
			temp = this.getStringPara(MAX_IDLE_TIME_FLAG);
			if(temp != null && temp.trim().length() > 0){
				maxIdleTime = Integer.parseInt(temp.trim(),10);
			}
			
			temp = this.getStringPara(DETECT_CYCLE_TIME_FLAG);
			if(temp != null && temp.trim().length() > 0){
				detectCycleTime = Integer.parseInt(temp.trim(),10);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		this.connPool.setTimeInfo(waitTime, useTime, maxIdleTime, detectCycleTime);
		
		temp = this.getStringPara("heart_sql");
		if(temp!= null){
			this.connPool.setHeartSql(temp);
		}
		
		temp = this.getStringPara(ENABLE_STACK_FLAG);
		if(temp != null){
			this.connPool.enableStack(temp.equalsIgnoreCase("true"));
		}
		
		temp = this.getStringPara("is_only_read");
		if(temp != null && temp.equals("true")){
			this.connPool.setOnlyRead(true);
		}
		try{
			this.connPool.init();
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}
		
		this.isRunning = true;
		return true;
	}
	
	private HashMap avaliableMapping = new HashMap();
	public synchronized long getAvaliableId(StatementHandle stmtHandle, String tableName,String fieldName) throws Exception{
		return this.getAvaliableId(stmtHandle, tableName, fieldName, false);
	}
	public synchronized long getAvaliableId(StatementHandle stmtHandle, String tableName,String fieldName, boolean isForce) throws Exception{
		String flag = tableName+"\n"+fieldName;
		Long value = (Long)avaliableMapping.get(flag);
		long resultValue = 1;
		
		if(value == null || isForce){
			String sql = "select max("+fieldName+") maxId from "+tableName;
			ResultSet sets = null;
			
			sets = stmtHandle.executeQuery(sql);
			if(sets.next()){
				resultValue = sets.getLong("maxId") + 1;
			}
			sets.close();
		}else{
			resultValue = value.longValue();
		}
		avaliableMapping.put(flag, new Long(resultValue+1));
		
		return resultValue;
	}
	public synchronized long getAvaliableId(DbHandle dbHandle, String tableName,String fieldName) throws Exception{
		return this.getAvaliableId(dbHandle, tableName, fieldName, false);
	}
	public synchronized long getAvaliableId(DbHandle dbHandle, String tableName,String fieldName, boolean isForce) throws Exception{
		DbHandle tempHandle = dbHandle;
		if(tempHandle == null){
			tempHandle = this.getConn();
		}
		StatementHandle stmtHandle = null;
		try{
			stmtHandle = tempHandle.createStatement();
			
			return this.getAvaliableId(stmtHandle, tableName, fieldName, isForce);
		}finally{
			tempHandle.closeStatement(stmtHandle);
			if(dbHandle == null){
				this.releaseConn(tempHandle);
			}
		}
	}
	
	public int getConnNum(){
		if(this.connPool == null){
			return -1;
		}
		return this.connPool.getCurNum();
	}
	
	public int getIdleNum(){
		if(this.connPool == null){
			return -1;
		}
		return this.connPool.getIdleNum();
	}
	
	public DbHandle getConn(){
		if(this.connPool == null){
			return null;
		}
		return this.getConn(this.connPool.getDefaultWaitTime(),this.connPool.getDefaultUseTime());
	}
	
	public DbHandle getConnWithWaitTime(int waitTime){
		if(this.connPool == null){
			return null;
		}
		return this.getConn(waitTime,this.connPool.getDefaultUseTime());
	}
	
	public DbHandle getConnWithUseTime(long useTime){
		if(this.connPool == null){
			return null;
		}
		return this.getConn(this.connPool.getDefaultWaitTime(),useTime);
	}
	
	public DbHandle getConn(int waitTime,long useTime){
		if(this.connPool == null){
			return null;
		}
		
		DbHandle dbHandle = this.connPool.getConn(waitTime,useTime);
		if(dbHandle == null){
			System.err.println("数据库连接已耗尽，不能获取到数据库连接!");
		}
		return dbHandle;
	}
	
	public void releaseConn(DbHandle dbHandle){
		if(this.connPool == null){
			return ;
		}
		this.connPool.releaseConn(dbHandle);
	}

	public void stopServer(){
		if(this.connPool != null){
			this.connPool.destroy();
		}
		this.connPool = null;
		this.isRunning = false;
	}
	public boolean isOracle() {
		if (this.getStringPara(DATA_BASE_TYPE_FLAG) == null)
			return true;
		return this.getStringPara(DATA_BASE_TYPE_FLAG).trim().equals(ORACLE);
	}
	public boolean isSqlServer() {
		if (this.getStringPara(DATA_BASE_TYPE_FLAG) == null)
			return false;
		return this.getStringPara(DATA_BASE_TYPE_FLAG).trim().equals(SQLSERVER);
	}	
	public boolean isMain_current()
	{
		return this.connPool.isMain_current();
	}
	public void printAllStackInfo(){
		this.connPool.printAllStackInfo();
	}
}
