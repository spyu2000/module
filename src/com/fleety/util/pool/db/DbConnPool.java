/**
 * ���� Created on 2008-6-11 by edmund
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
 * ���ݿ����ӳء�֧�ֵ����Ͷ���ģʽ��
 * ʹ��ǰ��Ҫ����init������
 * init��������ǰ��Ҫ�趨��ز����������趨�Ĳ���ͨ��setup�ӿ��趨��
 * setup�ӿ���Ҫ�趨���ݿ��������Ϣ��
 * ����ɸ����趨�Ĳ�����ͨ���ӿ�setConnNum,setTimeInfo�趨��
 * setConnNum�����趨���ӵĳ�ʼ����������С�������������
 * setTimeInfo�����趨��ȡ���ӵ�Ĭ�ϵȴ�ʱ�䣬���ӵ�Ĭ��ʹ��ʱ�䣬���ӵ�������ʱ���Լ��ػ��̵߳��������
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
				return "�޷���ȡ����";
			case STMT_CREATE_ERROR:
				return "��������ʧ��";
			case SQL_EXEC_ERROR:
				return "SQLִ�д���";
			default:
				return "δ֪����";
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
	 * ��װ���ݿ����Ӳ���
	 * @param dbDriver  ���ݿ���������
	 * @param dbUrl     ���ݿ�����URL
	 * @param dbUser    ���ݿ��û���
	 * @param dbPwd     ���ݿ�����
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
	 * �趨���ݿ����ӵ�ά��������Ϣ
	 * ��ʼ���������С����С���������趨Ϊ��С��������������������С�ڳ�ʼ�����������趨Ϊ��ʼ��������
	 * @param initNum  ��ʼ�����������������ֵС�ڵ���0����ȡĬ��ֵ2��
	 * @param minNum   ���ٵ������������ֵС�ڵ���0����ȡĬ��ֵ2��
	 * @param maxNum   ��������������ֵС�ڵ���0����ȡĬ��ֵ20��
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
		System.out.println("���ӳ���������Ϣ��initNum="+initNum+";minNum="+minNum+";maxNum="+maxNum);
		
		return true;
	}
	
	/**
	 * �趨ʱ����Ϣ���ֱ��ǻ�ȡ����ʱ��Ĭ�ϵȴ�ʱ�䣬���ӻ�ȡ��Ĭ��ӵ��ʱ�� ���ӵ�������ʱ�� �ػ��̵߳��������
	 * @param defaultWaitTime  ��СΪ0�����Ϊ0�����ȴ�������0��Ĭ��ֵ(1��)��
	 * @param defaultUseTime   ��СΪ1�룬����1�밴Ĭ��ֵ(10��)��
	 * @param maxIdleTime      ������ʱ�䣬������ʱ��ûʹ�ù������ӽ��ͷš���СΪ1���ӡ�����1���Ӱ�Ĭ��ֵ(10����)��
	 * @param detectCycleTime  ������ڡ�����Ϊ10�룬����10�밴Ĭ��ֵ(1����)�ơ�
	 */
	public void setTimeInfo(int defaultWaitTime,long defaultUseTime,long maxIdleTime,long detectCycleTime){
		this.defaultWaitTime = (defaultWaitTime < 0?1000:defaultWaitTime);
		this.defaultUseTime = ((defaultUseTime < 1000 && defaultUseTime != 0)?10000:defaultUseTime);
		this.maxIdleTime = (maxIdleTime < 60000?600000:maxIdleTime);
		this.detectCycleTime = (detectCycleTime < 10000?60000:detectCycleTime);
		
		System.out.println("���ӳ�ʱ����Ϣ��defaultWaitTime="+defaultWaitTime+";defaultUseTime="+defaultUseTime+";maxIdleTime="+maxIdleTime+";detectCycleTime="+detectCycleTime);
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
	 * �����趨�Ĳ�����ʼ�����ӳ�
	 * @throws Exception
	 */
	public synchronized void init() throws Exception{
		if(this.isInit){
			throw new Exception("���ݿ���Ѿ���ʼ��!");
		}
		this.isInit = true;
		
		this.curNum = 0;
		this.dbConnArr = new ArrayList(this.maxNum);
		
		Class.forName(this.dbDriver);
		
		//�ڳ��д������Ӷ���
		int validNum = this.addNewConn(this.initNum);
		if(validNum != this.initNum){
			this.destroy();
			throw new Exception("���ܳ�ʼ���㹻�����ݿ�����["+this.initNum+"],�ɳ�ʼ��["+validNum+"]!");
		}
		
		//�����ػ��̡߳�
		this.detectThread = new DetectThread();
		this.detectThread.start();
	}
	
	/**
	 * ����µ����Ӷ��󵽳��С�
	 * @param newConnNum  ����ӵ�������
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
	 * �Ƴ�һ��ָ�������Ӷ���
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
		System.out.println("�����ӶϿ�,������!" + dbInfo.hashCode()+" "+this.curDbUrl);

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
	 * �������ӳ��е��������Ӷ���
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
	 * �õ�һ�����Ӿ��
	 * @return ����ֵ����Ϊnull,�������е����Ӷ���ʹ���У������Ѵ������������
	 */
	public DbHandle getConn(){
		return this.getConn(this.defaultWaitTime, this.defaultUseTime);
	}
	
	/**
	 * �õ����Ӿ����
	 * @param waitTime  ������������ȴ�,��һ��Ҫ�õ�һ�����Ӷ���.0�����ȴ���
	 * @return
	 */
	public DbHandle getConnWithWaitTime(int waitTime){
		return this.getConn(waitTime, this.defaultUseTime);
	}
	
	/**
	 * �õ����Ӿ����
	 * @param useTime  0��������ʹ�ã��ػ��߳̽����������ж��䳬ʱʹ�á�
	 * @return
	 */
	public DbHandle getConnWithUseTime(long useTime){
		return this.getConn(this.defaultWaitTime, useTime);
	}
	
	/**
	 * �õ����Ӿ����
	 * @param waitTime  ������������ȴ�,��һ��Ҫ�õ�һ�����Ӷ���.0�����ȴ���
	 * @param useTime   0��������ʹ�ã��ػ��߳̽����������ж��䳬ʱʹ�á�
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
		
		if(waitTime < 0){  //��Ҫ����ȴ���һ����Ҫ���һ�����ӡ�
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
		}else{  //�ȴ�ָ��ʱ����ܻ�������
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
		buff.append("��ӡ���ݿ����ӵĻ�ȡ����Ϣ��\n");
		for(int i=0;i<num;i++){
			buff.append("��"+(i+1)+"����");
			buff.append(Util.getStackStr(eArr[i])+"\n");
		}
		buff.append("idle Num:"+this.idleNum);
		System.out.println(buff.toString());
	}
	
	/**
	 * ѭ�����ҿ��е����ӣ������򷵻أ����ܻ��������һ���������գ�
	 * Ȼ�������δ�ﵽ������������򴴽�һ���µķ��ء������������null.
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
		

		//�����������꣬�������������գ��Ƿ�������©�ͷ����ݿ����Ӿ����
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
	 * ���󴴽�ָ�����������ӣ��÷�����һ������������������������
	 * ������Ϊ���ݿ�������������ԭ�����
	 * @param num  ����������
	 * @return     ���Ӷ�������
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
		//δ���ñ����
		if(this.dbUrlArr.length <= 1){
			return null;
		}
		//�����ǰ���ӵ����Ǳ��⣬���ٽ����л�����Ϊֻ��һ�����⡣
		if(this.curDbUrl.equals(this.dbUrlArr[1])){
			return null;
		}

		//�����������ʧ�ܣ�Ҳ�������л�
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
		String content = "�л������ݿ⣺"+this.curDbUrl+" StartTime="+GeneralConst.YYYY_MM_DD_HH_MM_SS.format(sDate)+" EndTime="+GeneralConst.YYYY_MM_DD_HH_MM_SS.format(new java.util.Date());
		content += "\nDB LIST:\n";
		for(int i=0;i<this.dbUrlArr.length;i++){
			content += "\t"+this.dbUrlArr[i]+"\n";
		}
		INotifyServer.getSingleInstance().notifyInfo("�л����ݿ�.", content, INotifyServer.ERROR_LEVEL);
	}

	/**
	 * �ͷ����Ӿ��
	 * @param dbHandle
	 */
	public void releaseConn(DbHandle dbHandle){
		if(dbHandle == null){
			return ;
		}

		synchronized(this){
			DbInfo dbInfo = dbHandle.getDbInfo();
			//�ͷŵĹ����н������õ�DbInfo����Ϊnull,������Ҫ�Ȼ�ȡ������
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
				INotifyServer.getSingleInstance().notifyInfo("���ݿ��쳣("+errorDesc+").", strBuff.toString(), INotifyServer.ERROR_LEVEL);
			}else{
				INotifyServer.getSingleInstance().notifyInfo("���ݿ��쳣("+errorDesc+").", strBuff.toString(), INotifyServer.WARN_LEVEL);
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
	 * ����������Ӷ������Ϣ��
	 * ��Ϣ���������Ӷ��󣬴���ʱ�䣬�����ʼ��ʹ��ʱ�䣬�����ʼ����ʱ��
	 * ������ӱ�ʹ���У����ӵı�ʹ��ʱ��
	 * �Ƿ����ӱ�ʹ����
	 * ����������Զ��ύ״̬
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
		 * ����DbInfo�е�startIdleTime,isUsed.
		 * ���統ǰconnection���Զ��ύģʽ������DbInfo��ģʽ�����޸�֮��
		 * @param isUse ���µ���ʹ��״̬���ǿ���״̬��true������µ���ʹ��״̬��false������µ�����״̬��
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
	 * �ṩ����ʹ�õ����ݿ����Ӿ�����ڲ���װ��Statement�Լ�ResultSet
	 * ����ҪĿ�����ڰ����ⲿ�����߿��ƴ򿪵�Statement�Լ�ResultSet
	 * ��ʹ�ⲿ�����˹ر��α꣬�����Ӷ����ͷŵ�ʱ��Ҳ���ر����д򿪵��α����
	 * ���⣬�����������ٵ�ʱ�򣬽��ͷ������õ����Ӷ����������԰�����Щ�����ͷ����Ӷ����ʹ���߽����ͷš�
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

		
		//ÿ��ִ�ж����½�һ��Statement
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
			//�ر����д򿪵�Statement
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
				System.out.println("��ͨ���������������վ���ķ�ʽ�������ݿ����ӵ��ͷŴ��ڡ�");
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
				throw new SQLException("��PreparedStatement����!");
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
				throw new SQLException("��PreparedStatement����!");
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
				throw new SQLException("��PreparedStatement����!");
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
				throw new SQLException("��PreparedStatement����!");
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
				throw new SQLException("��PreparedStatement����!");
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

		//��������stmt��Ϊnull�Ļ�����ʹ�ô����stmt.
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
				throw new SQLException("��PreparedStatement����!");
			}
		}
		private void validCallStatement() throws SQLException{
			if(!this.isCallStmt){
				throw new SQLException("��CallableStatement����!");
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
			//�ر����д򿪵�ResultSet
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
	 * ����̣߳���ʱ��⣬��ⳬʱʹ�õĻ��߿���ʱ������ġ�
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
					//�������е����Ӷ���
					for(int i=0;i<num;i++){
						dbInfo = infos[i];
						if(dbInfo.isUsed()){  //���õ����Ӷ�����ܲ���ʹ�ó�ʱ�����ʹ�ó�ʱ��ֱ�ӹر�֮��������һ���µĵ����С�
							if(dbInfo.getUseTime() > 0 && curTime - dbInfo.getStartUseTime() > dbInfo.getUseTime()){
								System.out.println("������ʹ�ó�ʱ!");
								if(DbConnPool.this.enableStack){
									System.out.println(Util.getStackStr(dbInfo.getStackInfo()));
								}
							}
							if(!dbInfo.isAlive()){
								DbConnPool.this.updateConn(dbInfo);
							}
						}else{                //�����е����ӿ��ܿ���ʱ�����������������������Ӳ���������������С��������������ر�֮��
							if(DbConnPool.this.curNum > DbConnPool.this.minNum){
								if(curTime - dbInfo.getStartIdleTime() > DbConnPool.this.maxIdleTime){
									System.out.println("�����ӿ��г�ʱ�����ͷ�!");
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