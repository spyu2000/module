/**
 * 绝密 Created on 2008-6-11 by edmund
 */
package com.fleety.util.pool.db;

import java.net.URL;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import server.notify.INotifyServer;
import server.threadgroup.PoolInfo;
import server.threadgroup.ThreadPoolGroupServer;

import com.fleety.base.FleetyThread;
import com.fleety.base.GeneralConst;
import com.fleety.base.Util;
import com.fleety.util.pool.IPool;
import com.fleety.util.pool.thread.BasicTask;
import com.fleety.util.pool.thread.ThreadPool;

/**
 * 数据库连接池。支持单例和多例模式。
 * 使用前需要调用init方法。
 * init方法调用前需要设定相关参数，必须设定的参数通过setup接口设定。
 * setup接口主要设定数据库的连接信息。
 * 额外可附加设定的参数可通过接口setConnNum,setTimeInfo设定。
 * setConnNum用于设定连接的初始化数量，最小数量，最大数量
 * setTimeInfo用于设定获取连接的默认等待时间，连接的默认使用时间，连接的最大空闲时间以及守护线程的侦测周期
 * 
 * @title
 * @description
 * @version      1.0
 * @author       edmund
 *
 */
public class DbConnPool implements IPool{
	public static final int NO_CONN_ERROR = 1;
	public static final int STMT_CREATE_ERROR = 2;
	public static final int SQL_EXEC_ERROR = 3;
	
	private static DbConnPool singleInstance = null;
	
	public static DbConnPool getSingleInstance(){
		if(singleInstance == null){
			synchronized(DbConnPool.class){
				if(singleInstance == null){
					singleInstance = new DbConnPool();
				}
			}
		}
		return singleInstance;
	}
	
	public DbConnPool(){
		this.isInit = false;
	}
	
	public String getErrorDesc(int errorCode){
		switch(errorCode){
			case NO_CONN_ERROR:
				return "无法获取连接";
			case STMT_CREATE_ERROR:
				return "声明创建失败";
			case SQL_EXEC_ERROR:
				return "SQL执行错误";
			default:
				return "未知错误";
		}
	}
	
	private DetectThread detectThread = null;
	private ArrayList dbConnArr = null;

	private boolean isInit = false;
	private String dbDriver = null;
	private String curDbUrl = null;
	private String dbUrlArr[] = null;
	private String dbUser = null;
	private String dbPwd = null;
	private int curNum = 0;
	private int idleNum = 0;
	private int initNum = 2;
	private int minNum = 2;
	private int maxNum = 20;
	private int defaultWaitTime = 1000;
	private long defaultUseTime = 10000;
	private long maxIdleTime = 600000;
	private long detectCycleTime = 60000;
	private boolean enableStack = false;
	
	private boolean isOnlyRead = false;
	
	private long lastPrintStackTime = 0;
	private long stackPrintCycleTime = 10*60*1000l;
	
	
	public void enableStack(boolean isEnable){
		this.enableStack = isEnable;
	}

	public void setOnlyRead(boolean isOnlyRead) {
		this.isOnlyRead = isOnlyRead;
		System.out.println("Database OnlyRead:"+this.isOnlyRead);
	}
	
	/**
	 * 安装数据库连接参数
	 * @param dbDriver  数据库连接驱动
	 * @param dbUrl     数据库连接URL
	 * @param dbUser    数据库用户名
	 * @param dbPwd     数据库密码
	 */
	public void setup(String dbDriver,String dbUrl,String dbUser,String dbPwd){
		this.setup(dbDriver, new String[]{dbUrl}, dbUser, dbPwd);
	}
	public void setup(String dbDriver,String[] dbUrlArr,String dbUser,String dbPwd){
		this.dbDriver = dbDriver;
		this.dbUrlArr = dbUrlArr;
		this.curDbUrl = this.dbUrlArr[0];
		this.dbUser = dbUser;
		this.dbPwd = dbPwd;
	}
	
	private String heartSql = "select sysdate from dual";
	public void setHeartSql(String heartSql){
		if(heartSql != null){
			heartSql = heartSql.trim();
		}
		this.heartSql = heartSql;
	}
	
	/**
	 * 设定数据库连接的维护数量信息
	 * 初始化数量如果小于最小数量，将设定为最小数量。最大连接数量如果小于初始化数量，将设定为初始化数量。
	 * @param initNum  初始化的连接数量。如果值小于等于0，则取默认值2。
	 * @param minNum   最少的连接数。如果值小于等于0，则取默认值2。
	 * @param maxNum   最多连接数。如果值小于等于0，则取默认值20。
	 * @return
	 */
	public boolean setConnNum(int initNum,int minNum,int maxNum){
		if(this.isInit){
			return false;
		}
		
		this.initNum = (initNum < 0?2:initNum);
		this.minNum = (minNum < 0?2:minNum);
		this.maxNum = (maxNum < 0?20:maxNum);
		
		if(this.initNum < this.minNum){
			this.initNum = this.minNum;
		}
		if(this.maxNum < this.initNum){
			this.maxNum = this.initNum;
		}
		System.out.println("连接池连接数信息：initNum="+initNum+";minNum="+minNum+";maxNum="+maxNum);
		
		return true;
	}
	
	/**
	 * 设定时间信息，分别是获取连接时的默认等待时间，连接获取后默认拥有时间 连接的最大空闲时间 守护线程的侦测周期
	 * @param defaultWaitTime  最小为0，如果为0代表不等待。少于0则按默认值(1秒)计
	 * @param defaultUseTime   最小为1秒，少于1秒按默认值(10秒)计
	 * @param maxIdleTime      最大空闲时间，超过该时间没使用过的连接将释放。最小为1分钟。少于1分钟按默认值(10分钟)计
	 * @param detectCycleTime  侦测周期。至少为10秒，少于10秒按默认值(1分钟)计。
	 */
	public void setTimeInfo(int defaultWaitTime,long defaultUseTime,long maxIdleTime,long detectCycleTime){
		this.defaultWaitTime = (defaultWaitTime < 0?1000:defaultWaitTime);
		this.defaultUseTime = ((defaultUseTime < 1000 && defaultUseTime != 0)?10000:defaultUseTime);
		this.maxIdleTime = (maxIdleTime < 60000?600000:maxIdleTime);
		this.detectCycleTime = (detectCycleTime < 10000?60000:detectCycleTime);
		
		System.out.println("连接池时间信息：defaultWaitTime="+defaultWaitTime+";defaultUseTime="+defaultUseTime+";maxIdleTime="+maxIdleTime+";detectCycleTime="+detectCycleTime);
	}
	
	public int getCurNum(){
		return this.curNum;
	}
	
	public int getIdleNum(){
		return this.idleNum;
	}
	
	public int getDefaultWaitTime(){
		return this.defaultWaitTime;
	}
	
	public long getDefaultUseTime(){
		return this.defaultUseTime;
	}
	
	/**
	 * 按照设定的参数初始化连接池
	 * @throws Exception
	 */
	public synchronized void init() throws Exception{
		if(this.isInit){
			throw new Exception("数据库池已经初始化!");
		}
		this.isInit = true;
		
		this.curNum = 0;
		this.dbConnArr = new ArrayList(this.maxNum);
		
		Class.forName(this.dbDriver);
		
		//在池中创建连接对象
		int validNum = this.addNewConn(this.initNum);
		if(validNum != this.initNum){
			this.destroy();
			throw new Exception("不能初始化足够的数据库连接["+this.initNum+"],可初始化["+validNum+"]!");
		}
		
		//启动守护线程。
		this.detectThread = new DetectThread();
		this.detectThread.start();
	}
	
	/**
	 * 添加新的连接对象到池中。
	 * @param newConnNum  新添加的连接数
	 */
	private int addNewConn(int newConnNum){
		int validNum = 0;
		Connection[] connArr = this.createNewConn(newConnNum);
		
		if(connArr != null){
			int num = connArr.length;
			for(int i=0;i<num;i++){
				try{
					this.dbConnArr.add(new DbInfo(connArr[i]));
				}catch(Exception e){
					e.printStackTrace();
					continue;
				}
				
				this.curNum ++ ;
				this.idleNum ++ ;
				
				validNum++;
				
				synchronized(this){
					this.notify();
				}
			}
		}
		
		return validNum;
	}
	
	/**
	 * 移除一个指定的连接对象
	 * @param dbInfo
	 */
	private void removeConn(DbInfo dbInfo){
		if(dbInfo == null){
			return ;
		}
		
		if(this.dbConnArr.remove(dbInfo)){
			this.curNum --;
			if(!dbInfo.isUsed()){
				this.idleNum -- ;
			}
		}
		dbInfo.close();
	}
	
	private void updateConn(DbInfo dbInfo){
		if(dbInfo == null){
			return ;
		}
		System.out.println("有连接断开,将重连!" + dbInfo.hashCode()+" "+this.curDbUrl);

		try{			
			Connection[] connArr = this.createNewConn(1);
			if(connArr != null && connArr.length > 0){
				dbInfo.updateConn(connArr[0]);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	/**
	 * 销毁连接池中的所有连接对象
	 */
	public synchronized void destroy(){
		if(!this.isInit){
			return ;
		}
		int num = this.curNum;
		for(int i=0;i<num;i++){
			this.removeConn((DbInfo)this.dbConnArr.get(0));
		}
		
		if(this.detectThread != null){
			this.detectThread.stopDetect();
		}
		this.isInit = false;
	}
	
	/**
	 * 得到一个连接句柄
	 * @return 返回值可能为null,假如所有的连接都在使用中，并且已达最大连接数。
	 */
	public DbHandle getConn(){
		return this.getConn(this.defaultWaitTime, this.defaultUseTime);
	}
	
	/**
	 * 得到连接句柄。
	 * @param waitTime  负数代表无穷等待,，一定要得到一个连接对象.0代表不等待。
	 * @return
	 */
	public DbHandle getConnWithWaitTime(int waitTime){
		return this.getConn(waitTime, this.defaultUseTime);
	}
	
	/**
	 * 得到连接句柄。
	 * @param useTime  0代表无穷使用，守护线程将从来不会判断其超时使用。
	 * @return
	 */
	public DbHandle getConnWithUseTime(long useTime){
		return this.getConn(this.defaultWaitTime, useTime);
	}
	
	/**
	 * 得到连接句柄。
	 * @param waitTime  负数代表无穷等待,，一定要得到一个连接对象.0代表不等待。
	 * @param useTime   0代表无穷使用，守护线程将从来不会判断其超时使用。
	 * @return
	 */
	public synchronized DbHandle getConn(int waitTime,long useTime){
		if(!this.isInit){
			return null;
		}
		
		DbInfo dbInfo = null;

		dbInfo = this.loopGetOrAddConn();
		if(dbInfo != null){
			dbInfo.updateInfo(true,useTime);
			this.idleNum --;
			return new DbHandle(dbInfo);
		}
		
		if(waitTime == 0){
			this.printAllStackInfo();
			this.sendMailNotify(NO_CONN_ERROR, null, null);
			return null;
		}
		
		if(waitTime < 0){  //需要无穷等待，一定需要获得一个连接。
			try{
				while(true){
					this.wait(Long.MAX_VALUE);
					
					dbInfo = this.loopGetOrAddConn();
					if(dbInfo != null){
						break;
					}
				}
			}catch(Exception e){
				e.printStackTrace();
			}
		}else{  //等待指定时间后不能获得则放弃
			try{
				this.wait(waitTime);
			}catch(Exception e){
				e.printStackTrace();
			}
			dbInfo = this.loopGetOrAddConn();
		}
		
		if(dbInfo != null){
			dbInfo.updateInfo(true,useTime);
			this.idleNum --;
			return new DbHandle(dbInfo);
		}
		
		this.printAllStackInfo();
		this.sendMailNotify(NO_CONN_ERROR, null, null);
		return null;
	}
	
	public void printAllStackInfo(){
		if(!this.enableStack){
			return ;
		}
		
		if(System.currentTimeMillis() - this.lastPrintStackTime < this.stackPrintCycleTime){
			return ;
		}
		this.lastPrintStackTime = System.currentTimeMillis();
		
		Exception[] eArr = this.getAllStackInfo();
		int num = eArr.length;
		
		StringBuffer buff = new StringBuffer(1024);
		buff.append("打印数据库连接的获取者信息：\n");
		for(int i=0;i<num;i++){
			buff.append("第"+(i+1)+"个：");
			buff.append(Util.getStackStr(eArr[i])+"\n");
		}
		buff.append("idle Num:"+this.idleNum);
		System.out.println(buff.toString());
	}
	
	/**
	 * 循环查找空闲的连接，发现则返回，不能获得则先做一次垃圾回收，
	 * 然后并且如果未达到最大连接数，则创建一个新的返回。其它情况返回null.
	 * @return
	 */
	private DbInfo loopGetOrAddConn(){
		DbInfo dbInfo = null;
		for(int i=0;i<this.curNum;i++){
			dbInfo = (DbInfo)this.dbConnArr.get(i);
			if(!dbInfo.isUsed()){
				return dbInfo;
			}
		}
		

		//连接已消耗完，进行下垃圾回收，是否有人遗漏释放数据库连接句柄？
		System.gc();
		
		dbInfo = null;
		if(this.curNum < this.maxNum){
			try{
				Connection[] connArr = this.createNewConn(1);
				
				if(connArr != null && connArr.length > 0){
					dbInfo = new DbInfo(connArr[0]);
					this.dbConnArr.add(dbInfo);
					this.curNum ++;
					this.idleNum ++;
				}
				
				return dbInfo;
			}catch(Exception e){
				e.printStackTrace();
				return null;
			}
		}
		
		return null;
	}
	
	/**
	 * 请求创建指定数量的连接，该方法不一定返回请求数量的连接数。
	 * 可能因为数据库连接数量满等原因造成
	 * @param num  请求连接数
	 * @return     连接对象数组
	 */
	private Connection[] createNewConn(int num){
		if(num <= 0){
			return null;
		}
		
		LinkedList list = new LinkedList();
		try{
			for(int i=0;i<num;i++){
				list.add(DriverManager.getConnection(this.curDbUrl, dbUser, dbPwd));
			}
		}catch(Exception e){
			e.printStackTrace();

			String message = e.getMessage();
			return this.createNewConnToBak(num,message);
		}
		
		Connection[] arr = new Connection[list.size()];
		list.toArray(arr);
		return arr;
	}
	private Connection[] createNewConnToBak(int num,String message){
		//未配置备库的
		if(this.dbUrlArr.length <= 1){
			return null;
		}
		//如果当前连接的已是备库，则不再进行切换。认为只有一个备库。
		if(this.curDbUrl.equals(this.dbUrlArr[1])){
			return null;
		}

		//如果不是连接失败，也不进行切换
		if(!this.isNeedSwitch(message)){
			return null;
		}
		this.curDbUrl = this.dbUrlArr[1];
		this.switchDb();
		
		if(num <= 0){
			return null;
		}
		
		LinkedList list = new LinkedList();
		try{
			for(int i=0;i<num;i++){
				list.add(DriverManager.getConnection(this.curDbUrl, dbUser, dbPwd));
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		
		Connection[] arr = new Connection[list.size()];
		list.toArray(arr);
		return arr;
	}
	private boolean isNeedSwitch(String message){
		if(message == null){
			return false;
		} 
		if(message.indexOf("establish") >= 0 
			&& message.indexOf("Network") >= 0
			&& message.indexOf("Adapter") >= 0
			&& message.indexOf("connection") >= 0){
			return true;
		}
		if(message.indexOf("Connection") >= 0 && message.indexOf("refused") >= 0){
			return true;
		}
		return false;
	}
	
	private void switchDb(){
		System.out.println("Switch To Db:"+this.curDbUrl);
		java.util.Date sDate = new java.util.Date();
		DbInfo dbInfo;
		synchronized(this){
			for(int i=0;i<this.dbConnArr.size();i++){
				dbInfo = (DbInfo)this.dbConnArr.get(i);
				this.updateConn(dbInfo);
			}
		}
		String content = "切换到数据库："+this.curDbUrl+" StartTime="+GeneralConst.YYYY_MM_DD_HH_MM_SS.format(sDate)+" EndTime="+GeneralConst.YYYY_MM_DD_HH_MM_SS.format(new java.util.Date());
		content += "\nDB LIST:\n";
		for(int i=0;i<this.dbUrlArr.length;i++){
			content += "\t"+this.dbUrlArr[i]+"\n";
		}
		INotifyServer.getSingleInstance().notifyInfo("切换数据库.", content, INotifyServer.ERROR_LEVEL);
	}

	/**
	 * 释放连接句柄
	 * @param dbHandle
	 */
	public void releaseConn(DbHandle dbHandle){
		if(dbHandle == null){
			return ;
		}

		synchronized(this){
			DbInfo dbInfo = dbHandle.getDbInfo();
			//释放的过程中将置引用的DbInfo对象为null,所以需要先获取出来。
			dbHandle.release();
			
			if(dbInfo != null){
				dbInfo.updateInfo(false,0);
					
				if(this.dbConnArr.contains(dbInfo)){
					this.idleNum ++;
				}
				
				this.notify();
			}
		}
	}
	
	public synchronized Exception[] getAllStackInfo(){
		if(!this.enableStack){
			return new Exception[0];
		}
		Exception[] eArr = new Exception[this.curNum];
		int num = this.curNum;
		for(int i=0;i<num;i++){
			eArr[i] = ((DbInfo)this.dbConnArr.get(i)).getStackInfo();
		}
		return eArr;
	}
	
	private HashMap errorCountMapping = new HashMap();
	private void sendMailNotify(int errorCode,Exception e,String sql){
		long lastSendTime = 0;
		Integer count;
		synchronized(errorCountMapping){
			count = (Integer)errorCountMapping.get(new Integer(errorCode));
			if(count == null){
				count = new Integer(0);
			}
			count = new Integer(count.intValue()+1);
			errorCountMapping.put(new Integer(errorCode), count);
			
			Long tempLong = (Long)errorCountMapping.get("SENDTIME"+errorCode);
			if(tempLong != null){
				lastSendTime = tempLong.longValue();
			}
		}
		
		if(System.currentTimeMillis() - lastSendTime > 30*60*1000){
			errorCountMapping.put("SENDTIME"+errorCode, new Long(System.currentTimeMillis()));
			
			synchronized(errorCountMapping){
				errorCountMapping.put(new Integer(errorCode), new Integer(0));
			}
			
			String errorDesc = this.getErrorDesc(errorCode);
			StringBuffer strBuff = new StringBuffer(64);
			strBuff.append(errorDesc);
			strBuff.append("=");
			strBuff.append(count);
			strBuff.append("\n");

			if(errorCode == NO_CONN_ERROR){
				INotifyServer.getSingleInstance().notifyInfo("数据库异常("+errorDesc+").", strBuff.toString(), INotifyServer.ERROR_LEVEL);
			}else{
				INotifyServer.getSingleInstance().notifyInfo("数据库异常("+errorDesc+").", strBuff.toString(), INotifyServer.WARN_LEVEL);
			}
		}
	}
	
	public static void main(String[] argv){
		DbConnPool pool = new DbConnPool();
		try{
			pool.setup("oracle.jdbc.driver.OracleDriver", "jdbc:oracle:thin:@192.168.0.184:1521:demo", "iflow", "iflow123");
			pool.init();
		}catch(Exception e){
			e.printStackTrace();
		}
		
		DbHandle db;
		while(true){
			db = pool.getConn();
			System.out.println(db);
			pool.releaseConn(db);
			
			try{
				FleetyThread.sleep(5000);
			}catch(Exception e){}
		}
	}
	
	/**
	 * 描述这个连接对象的信息。
	 * 信息包含：连接对象，创建时间，最近开始被使用时间，最近开始空闲时间
	 * 如果连接被使用中，连接的被使用时间
	 * 是否连接被使用中
	 * 连接最初的自动提交状态
	 * @title
	 * @description
	 * @version      1.0
	 * @author       edmund
	 *
	 */
	private class DbInfo{
		private Connection conn;
		private long createTime = 0;
		private long startUseTime = 0;
		private long startIdleTime = 0;
		private long useTime = 0;
		private boolean isAlive = true;
		private boolean isUsed = false;
		private boolean isAutoSave = true;
		private Exception stackInfo = null;
		
		public DbInfo(Connection conn) throws Exception{
			this.updateConn(conn);
		}
		
		public long getAvaliableTime(){
			long time = this.startUseTime + this.useTime - System.currentTimeMillis();
			if(time < 1000){
				time = 1000;
			}
			return time;
		}
		
		public void updateConn(Connection conn) throws Exception{
			this.close();
			
			this.conn = conn;
			this.createTime = System.currentTimeMillis();
			this.isAlive = true;
			this.startIdleTime = this.createTime;
			this.isAutoSave = conn.getAutoCommit();
		}
		
		public void setStackInfo(Exception stackInfo){
			this.stackInfo = stackInfo;
		}
		
		public Exception getStackInfo(){
			return this.stackInfo;
		}
		
		public void close(){
			try{
				if(this.conn != null){
					this.conn.close();
				}
			}catch(Exception e){
				e.printStackTrace();
			}
			this.conn = null;
			this.stackInfo = null;
		}
		
		public boolean isConnected(){
			if(this.conn == null){
				return false;
			}
			Statement stmt = null;
			try{
				if(DbConnPool.this.heartSql != null && DbConnPool.this.heartSql.length() > 0){
					stmt = this.conn.createStatement();
					stmt.setQueryTimeout(2000);
					stmt.execute(DbConnPool.this.heartSql);
					stmt.close();
				}
			}catch(Exception e){
				return false;
			}finally{
				if(stmt != null){
					try {
						stmt.close();
					} catch (Exception e) {
					}
				}
			}
			return true;
		}

		public void updateAliveStatus(boolean isAlive){
			this.isAlive = isAlive;
		}
		public boolean isAlive(){
			return this.isAlive;
		}
		
		public boolean isUsed(){
			return this.isUsed;
		}
		/**
		 * 更新DbInfo中的startIdleTime,isUsed.
		 * 假如当前connection的自动提交模式不等于DbInfo中模式，将修改之。
		 * @param isUse 更新到被使用状态还是空闲状态。true代表更新到被使用状态，false代表更新到空闲状态。
		 */
		public void updateInfo(boolean isUse,long useTime){
			this.isUsed = isUse;
			if(isUse){
				this.startUseTime = System.currentTimeMillis();
				this.useTime = useTime;
			}else{
				this.startIdleTime = System.currentTimeMillis();
				try{
					if(conn != null){
						if(conn.getAutoCommit() != true){
							conn.commit();
						}
						if(conn.getAutoCommit() != isAutoSave){
							conn.setAutoCommit(isAutoSave);
						}
					}
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		}
		
		public Connection getConn(){
			return this.conn;
		}
		
		public long getCreateTime(){
			return this.createTime;
		}
		
		public long getStartUseTime(){
			return this.startUseTime;
		}
		
		public long getUseTime(){
			return this.useTime;
		}
		
		public long getStartIdleTime(){
			return this.startIdleTime;
		}
	}
	
	/**
	 * 提供对外使用的数据库连接句柄，内部封装了Statement以及ResultSet
	 * 其主要目的在于帮助外部调用者控制打开的Statement以及ResultSet
	 * 即使外部忘记了关闭游标，则连接对象释放的时候也将关闭所有打开的游标对象。
	 * 另外，当本对象被销毁的时候，将释放其引用的连接对象。这样可以帮助那些忘记释放连接对象的使用者进行释放。
	 * @title
	 * @description
	 * @version      1.0
	 * @author       edmund
	 *
	 */
	public class DbHandle{
		private List stmtList = new LinkedList();
		private DbInfo dbInfo = null;
		public boolean isAlwaysPrintSql=false;
		
		public DbHandle(DbInfo dbInfo){
			this.dbInfo = dbInfo;
			if(DbConnPool.this.enableStack){
				this.dbInfo.setStackInfo(new Exception());
			}
		}
		
		public boolean isConnected(){
			if(this.dbInfo == null){
				return false;
			}
			return this.dbInfo.getConn() != null;
		}
		
		DbInfo getDbInfo()
        {
            return this.dbInfo;
        }

        public Connection getConnection()
        {
            return new NestConnection(DbConnPool.this,this.dbInfo.getConn(),this);
        }
		public boolean getAutoCommit() throws SQLException{
			return this.dbInfo.getConn().getAutoCommit();
		}
		
		public void setAutoCommit(boolean autoCommit) throws SQLException{
			this.dbInfo.getConn().setAutoCommit(autoCommit);
		}
		
		public void commit() throws SQLException{
			this.dbInfo.getConn().commit();
		}
		
		public void rollback() throws SQLException{
			this.dbInfo.getConn().rollback();
		}
		
		public synchronized StatementHandle prepareStatement(String sql) throws SQLException{
			PreparedStatement stmt = null;
			
			try{
				stmt = this.dbInfo.getConn().prepareStatement(sql);
			} catch(SQLException e){
				e.printStackTrace();
				System.out.println(sql);
				DbConnPool.this.updateConn(this.dbInfo);
				
				DbConnPool.this.sendMailNotify(STMT_CREATE_ERROR, e, sql);
				try{
					stmt = this.dbInfo.getConn().prepareStatement(sql);
				}catch(SQLException e1){
					this.dbInfo.updateAliveStatus(false);
					throw e1;
				}
			}

			StatementHandle stmtHandle = new StatementHandle(this,stmt);
			stmtHandle.setSql(sql);
			this.stmtList.add(stmtHandle);
			
			return stmtHandle;
		}
		
		public synchronized StatementHandle prepareStatement(String sql,String[] cols) throws SQLException{
			PreparedStatement stmt = null;
			try{
				stmt = this.dbInfo.getConn().prepareStatement(sql,cols);
			} catch(SQLException e){
				e.printStackTrace();
				System.out.println(sql);
				DbConnPool.this.updateConn(this.dbInfo);
				
				DbConnPool.this.sendMailNotify(STMT_CREATE_ERROR, e, sql);
				try{
					stmt = this.dbInfo.getConn().prepareStatement(sql,cols);
				}catch(SQLException e1){
					this.dbInfo.updateAliveStatus(false);
					throw e1;
				}
			}
			StatementHandle stmtHandle = new StatementHandle(this,stmt);
			this.stmtList.add(stmtHandle);
			
			return stmtHandle;
		}
		
		public synchronized StatementHandle prepareStatement(String sql,int resultSetType,int resultSetConcurrency) throws SQLException{
			PreparedStatement stmt = null; 

			try{
				stmt = this.dbInfo.getConn().prepareStatement(sql, resultSetType, resultSetConcurrency);
			} catch(SQLException e){
				e.printStackTrace();
				System.out.println(sql);
				DbConnPool.this.updateConn(this.dbInfo);
				DbConnPool.this.sendMailNotify(STMT_CREATE_ERROR, e, sql);
				try{
					stmt = this.dbInfo.getConn().prepareStatement(sql, resultSetType, resultSetConcurrency);
				}catch(SQLException e1){
					this.dbInfo.updateAliveStatus(false);
					throw e1;
				}
			}
			
			StatementHandle stmtHandle = new StatementHandle(this,stmt);
			this.stmtList.add(stmtHandle);
			
			return stmtHandle;
		}
		public synchronized StatementHandle prepareCall(String sql) throws SQLException{
			CallableStatement stmt = null; 

			try{
				stmt = this.dbInfo.getConn().prepareCall(sql);
			} catch(SQLException e){
				e.printStackTrace();
				System.out.println(sql);
				DbConnPool.this.updateConn(this.dbInfo);
				DbConnPool.this.sendMailNotify(STMT_CREATE_ERROR, e, sql);
				try{
					stmt = this.dbInfo.getConn().prepareCall(sql);
				}catch(SQLException e1){
					this.dbInfo.updateAliveStatus(false);
					throw e1;
				}
			}
			
			StatementHandle stmtHandle = new StatementHandle(this,stmt);
			this.stmtList.add(stmtHandle);
			
			return stmtHandle;
		}		
		public synchronized StatementHandle createStatement() throws SQLException{
			Statement stmt = null; 
			
			try{
				stmt = this.dbInfo.getConn().createStatement();
			} catch(SQLException e){
				e.printStackTrace();
				DbConnPool.this.updateConn(this.dbInfo);
				DbConnPool.this.sendMailNotify(STMT_CREATE_ERROR, e, null);
				try{
					stmt = this.dbInfo.getConn().createStatement();
				}catch(SQLException e1){
					this.dbInfo.updateAliveStatus(false);
					throw e1;
				}
			}
			
			StatementHandle stmtHandle = new StatementHandle(this,stmt);
			this.stmtList.add(stmtHandle);
			
			return stmtHandle;
		}
		public synchronized StatementHandle createStatement(int resultSetType,int resultSetConcurrency) throws SQLException{
			Statement stmt = null;

			try{
				stmt = this.dbInfo.getConn().createStatement(resultSetType, resultSetConcurrency);
			} catch(SQLException e){
				e.printStackTrace();
				DbConnPool.this.updateConn(this.dbInfo);
				try{
					stmt = this.dbInfo.getConn().createStatement(resultSetType, resultSetConcurrency);
				}catch(SQLException e1){
					this.dbInfo.updateAliveStatus(false);
					throw e1;
				}
			}
			
			StatementHandle stmtHandle = new StatementHandle(this,stmt);
			this.stmtList.add(stmtHandle);
			
			return stmtHandle;
		}
		
		public synchronized void closeStatement(StatementHandle stmtHandle){
			if(stmtHandle == null){
				return ;
			}
			this.stmtList.remove(stmtHandle);
			stmtHandle.release();
		}

		
		//每次执行都将新建一个Statement
		public boolean execute(String sql) throws SQLException{
			StatementHandle stmt = this.createStatement();
			try{
				return stmt.execute(sql);
			}catch(SQLException e){
				throw e;
			}finally{
				this.closeStatement(stmt);
			}
		}
		public int executeUpdate(String sql) throws SQLException{
			StatementHandle stmt = this.createStatement();
			try{
				return stmt.executeUpdate(sql);
			}catch(SQLException e){
				throw e;
			}finally{
				this.closeStatement(stmt);
			}
		}

		void release(){
			this.dbInfo = null;
			
			this.releaseStatement();
		}
		
		public synchronized void releaseStatement(){
			//关闭所有打开的Statement
			for(Iterator tempIterator = stmtList.iterator();tempIterator.hasNext();){
				try{
					((StatementHandle)tempIterator.next()).release();
				}catch(Exception e){
					e.printStackTrace();
				}
				tempIterator.remove();
			}
			
		}

		public void finalize() throws Throwable{
			super.finalize();
			if(this.dbInfo != null){
				System.out.println("有通过垃圾回收器回收句柄的方式进行数据库连接的释放存在。");
				if(DbConnPool.this.enableStack){
					System.out.println(Util.getStackStr(dbInfo.getStackInfo()));
				}
				DbConnPool.this.releaseConn(this);
			}
		}
		public void setIsAlwaysPrintSql(boolean isAlwaysPrintSql)
		{
			this.isAlwaysPrintSql=isAlwaysPrintSql;
		}
	}
	
	public class StatementHandle{
		private DbHandle conn = null;
		private Statement stmt = null;
		private boolean isPreparedStmt = false;
		private boolean isCallStmt = false;
		private List resultSetList = new LinkedList();
		private String sql=null;
		
		public StatementHandle(DbHandle conn,Statement stmt) throws SQLException{
			this.conn = conn;
			this.stmt = stmt;
			if(this.stmt instanceof PreparedStatement){
				this.isPreparedStmt = true;
			}
			if(this.stmt instanceof CallableStatement) {
				this.isCallStmt =true;
			}
			
			if(this.conn.getDbInfo().getUseTime() > 0){
				this.stmt.setQueryTimeout((int)Math.round(this.conn.getDbInfo().getAvaliableTime()/1000.0));
			}
		}
		
		Statement getStatement(){
			return this.stmt;
		}
		
		public void close(){
			this.conn.closeStatement(this);
		}
		
		public void setFetchSize(int size) throws SQLException{
			this.stmt.setFetchSize(size);
		}
		
		public  void registerOutParameter(int parameterIndex, int sqlType)throws SQLException{
			this.validCallStatement();
			((CallableStatement)stmt).registerOutParameter(parameterIndex, sqlType);
		}
		public void clearParameters() throws SQLException{
			if(!(this.stmt instanceof PreparedStatement)){
				throw new SQLException("非PreparedStatement对象!");
			}
			((PreparedStatement)stmt).clearParameters();
		}
		
		public void addBatch() throws SQLException{
			this.validStatement();
			try{
				((PreparedStatement)this.stmt).addBatch();
			}catch(SQLException e){
				DbConnPool.this.sendMailNotify(SQL_EXEC_ERROR, e, null);
				throw e;
			}
		}
		public void addBatch(String sql) throws SQLException{
			try{
				this.stmt.addBatch(sql);
			}catch(SQLException e){
				DbConnPool.this.sendMailNotify(SQL_EXEC_ERROR, e, sql);
				throw e;
			}
		}
		public void clearBatch() throws SQLException{
			try{
				this.stmt.clearBatch();
			}catch(SQLException e){
				DbConnPool.this.sendMailNotify(SQL_EXEC_ERROR, e, null);
				throw e;
			}
		}
		public int[] executeBatch() throws SQLException{
			try{
				if(isOnlyRead){
					this.clearBatch();
					return null;
				}
				return stmt.executeBatch();
			}catch(SQLException e){
				DbConnPool.this.sendMailNotify(SQL_EXEC_ERROR, e, null);
				throw e;
			}
		}
		public ResultSet executeQuery() throws SQLException{
			if(!(this.stmt instanceof PreparedStatement)){
				throw new SQLException("非PreparedStatement对象!");
			}
			try{
				if(this.conn.isAlwaysPrintSql){
					System.out.println("sql:"+sql);
				}
				ResultSet sets = ((PreparedStatement)stmt).executeQuery();
				this.resultSetList.add(sets);
				
				return sets;
			}catch(SQLException e){
				DbConnPool.this.sendMailNotify(SQL_EXEC_ERROR, e, null);
				throw e;
			}
		}
		public void execute() throws SQLException{
			if(!(this.stmt instanceof PreparedStatement)){
				throw new SQLException("非PreparedStatement对象!");
			}
			if(isOnlyRead){
				return ;
			}
			try{
				if(this.conn.isAlwaysPrintSql){
					System.out.println("sql:"+sql);
				}
				((PreparedStatement)stmt).execute();
			}catch(SQLException e){
				DbConnPool.this.sendMailNotify(SQL_EXEC_ERROR, e, null);
				throw e;
			}
		}
		public int executeUpdate() throws SQLException{
			if(!(this.stmt instanceof PreparedStatement)){
				throw new SQLException("非PreparedStatement对象!");
			}
			if(isOnlyRead){
				return 0;
			}
			
			try{
				if(this.conn.isAlwaysPrintSql){
					System.out.println("sql:"+sql);
				}
				return ((PreparedStatement)stmt).executeUpdate();
			}catch(SQLException e){
				DbConnPool.this.sendMailNotify(SQL_EXEC_ERROR, e, null);
				throw e;
			}
		}
		
		public ResultSet getGeneratedKeys() throws SQLException{
			if(!(this.stmt instanceof PreparedStatement)){
				throw new SQLException("非PreparedStatement对象!");
			}
			if(isOnlyRead){
				return null;
			}
			
			try{
				return ((PreparedStatement)stmt).getGeneratedKeys();
			}catch(SQLException e){
				DbConnPool.this.sendMailNotify(SQL_EXEC_ERROR, e, null);
				throw e;
			}
		}

		//如果传入的stmt不为null的话，将使用传入的stmt.
		public ResultSet executeQuery(String sql) throws SQLException{			
			ResultSet sets = null;
			
			try{
				if(this.conn.isAlwaysPrintSql){
					System.out.println("sql:"+sql);
				}
				sets = stmt.executeQuery(sql);
			}catch(SQLException e){
				System.out.println(sql);

				DbConnPool.this.sendMailNotify(SQL_EXEC_ERROR, e, sql);
				throw e;
			}
			
			this.resultSetList.add(sets);
			
			return sets;
		}
		public boolean execute(String sql) throws SQLException{
			if(isOnlyRead){
				return false;
			}
			boolean isSuccess = false;
			try{
				if(this.conn.isAlwaysPrintSql){
					System.out.println("sql:"+sql);
				}
				isSuccess = stmt.execute(sql);
			}catch(SQLException e){
				System.out.println(sql);
				DbConnPool.this.sendMailNotify(SQL_EXEC_ERROR, e, sql);
				throw e;
			}
			return isSuccess;
		}
		public int executeUpdate(String sql) throws SQLException{
			if(isOnlyRead){
				return 0;
			}
			
			int result = 0;
			try{
				if(this.conn.isAlwaysPrintSql){
					System.out.println("sql:"+sql);
				}
				result = stmt.executeUpdate(sql);
			}catch(SQLException e){
				System.out.println(sql);
				DbConnPool.this.sendMailNotify(SQL_EXEC_ERROR, e, sql);
				throw e;
			}
			return result;
		}
		public boolean getBoolean(int parameterIndex) throws SQLException
		{
			this.validCallStatement();
			return ((CallableStatement)this.stmt).getBoolean(parameterIndex);
		}
		public void setInt(int index,int value) throws SQLException{
			this.validStatement();
			
			((PreparedStatement)this.stmt).setInt(index, value);
		}
		
		public void setDouble(int index,double value) throws SQLException{
			this.validStatement();
			
			((PreparedStatement)this.stmt).setDouble(index, value);
		}
		
		public void setFloat(int index,float value) throws SQLException{
			this.validStatement();
			
			((PreparedStatement)this.stmt).setFloat(index, value);
		}
		
		public void setDate(int index,Date value) throws SQLException{
			this.validStatement();
			
			((PreparedStatement)this.stmt).setDate(index, value);
		}
		
		public void setTimestamp(int index,Timestamp value) throws SQLException{
			this.validStatement();
			
			((PreparedStatement)this.stmt).setTimestamp(index, value);
		}
		
		public void setLong(int index,long value) throws SQLException{
			this.validStatement();
			
			((PreparedStatement)this.stmt).setLong(index, value);
		}
		
		public void setString(int index,String value) throws SQLException{
			this.validStatement();
			
			((PreparedStatement)this.stmt).setString(index, value);
		}
		
		public void setBoolean(int index,boolean value) throws SQLException{
			this.validStatement();
			
			((PreparedStatement)this.stmt).setBoolean(index, value);
		}
		
		public void setByte(int index,byte value) throws SQLException{
			this.validStatement();
			
			((PreparedStatement)this.stmt).setByte(index, value);
		}
		
		public void setObject(int index, Object value) throws SQLException{
			this.validStatement();
			
			((PreparedStatement)this.stmt).setObject(index, value);
		}
		
		public void setTime(int index,Time value) throws SQLException{
			this.validStatement();
			
			((PreparedStatement)this.stmt).setTime(index, value);
		}
		
		public void setShort(int index,short value) throws SQLException{
			this.validStatement();
			
			((PreparedStatement)this.stmt).setShort(index, value);
		}
		
		public void setURL(int index,URL value) throws SQLException{
			this.validStatement();
			
			((PreparedStatement)this.stmt).setURL(index, value);
		}
		
		private void validStatement() throws SQLException{
			if(!this.isPreparedStmt){
				throw new SQLException("非PreparedStatement对象!");
			}
		}
		private void validCallStatement() throws SQLException{
			if(!this.isCallStmt){
				throw new SQLException("非CallableStatement对象!");
			}
		}
		protected void setSql(String sql)
		{
			this.sql=sql;
		}
		void release(){
			this.releaseResultSet();
			try{
				this.stmt.close();
			}catch(Exception e){
				e.printStackTrace();
			}
		}

		public void releaseResultSet(){
			//关闭所有打开的ResultSet
			for(Iterator tempIterator = resultSetList.iterator();tempIterator.hasNext();){
				try{
					((ResultSet)tempIterator.next()).close();
				}catch(Exception e){
					e.printStackTrace();
				}
				tempIterator.remove();
			}
		}
	}
	
	/**
	 * 侦测线程，定时侦测，侦测超时使用的或者空闲时间过长的。
	 * @title
	 * @description
	 * @version      1.0
	 * @author       edmund
	 *
	 */
	private class DetectThread extends BasicTask{
		private boolean isStop = false;
		public DetectThread(){
			
		}
		
		private String poolName = null;
		public void start(){
			try{
				this.poolName = "Db Connect Pool["+DbConnPool.this.hashCode()+"] Detect Thread";
				PoolInfo pInfo = new PoolInfo(ThreadPool.SINGLE_TASK_LIST_POOL,1,1,true);
				ThreadPool pool = ThreadPoolGroupServer.getSingleInstance().createThreadPool(this.poolName, pInfo);
				pool.addTask(this);
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		
		public boolean execute() throws Exception{
			try{
				this.run();
			}catch(Throwable e){
				e.printStackTrace();
			}finally{
				ThreadPoolGroupServer.getSingleInstance().removeThreadPool(this.poolName);
			}
			return true;
		}
		
		public void run(){
			this.isStop = false;
			
			while(!this.isStop){
				try{
					FleetyThread.sleep(DbConnPool.this.detectCycleTime);
				}catch(Exception e){}

				DbInfo dbInfo = null;
				long curTime = System.currentTimeMillis();
				
				synchronized(DbConnPool.this){
					DbInfo[] infos = new DbInfo[DbConnPool.this.curNum];
					DbConnPool.this.dbConnArr.toArray(infos);
					int num = DbConnPool.this.curNum;
					//遍历所有的连接对象
					for(int i=0;i<num;i++){
						dbInfo = infos[i];
						if(dbInfo.isUsed()){  //在用的连接对象可能产生使用超时。如果使用超时则直接关闭之，并构建一个新的到池中。
							if(dbInfo.getUseTime() > 0 && curTime - dbInfo.getStartUseTime() > dbInfo.getUseTime()){
								System.out.println("有连接使用超时!");
								if(DbConnPool.this.enableStack){
									System.out.println(Util.getStackStr(dbInfo.getStackInfo()));
								}
							}
							if(!dbInfo.isAlive()){
								DbConnPool.this.updateConn(dbInfo);
							}
						}else{                //空闲中的连接可能空闲时间过长，如果存在这样的连接并且连接数大于最小需求连接数，则关闭之。
							if(DbConnPool.this.curNum > DbConnPool.this.minNum){
								if(curTime - dbInfo.getStartIdleTime() > DbConnPool.this.maxIdleTime){
									System.out.println("有连接空闲超时，将释放!");
									DbConnPool.this.removeConn(dbInfo);
									continue;
								}
							}
							if(!dbInfo.isConnected()){
								DbConnPool.this.updateConn(dbInfo);
								continue;
							}
						}
					}
				}
				
				this.scanMainDb();
			}
		}
		
		private void scanMainDb(){
			try{
				if(DbConnPool.this.curDbUrl.equals(DbConnPool.this.dbUrlArr[0])){
					return ;
				}
				Connection conn = DriverManager.getConnection(DbConnPool.this.dbUrlArr[0], DbConnPool.this.dbUser, DbConnPool.this.dbPwd);
				if(conn != null){
					conn.close();
					
					DbConnPool.this.curDbUrl = DbConnPool.this.dbUrlArr[0];
					DbConnPool.this.switchDb();
				}
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		
		public void stopDetect(){
			ThreadPoolGroupServer.getSingleInstance().removeThreadPool(this.poolName);
			this.isStop = true;
		}
	}

	public boolean isMain_current(){
		return this.curDbUrl.equals(this.dbUrlArr[0]);
	}
}