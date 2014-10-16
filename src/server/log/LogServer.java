/**
 * 绝密 Created on 2008-4-8 by edmund
 */
package server.log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

import server.threadgroup.PoolInfo;
import server.threadgroup.ThreadPoolGroupServer;

import com.fleety.base.GeneralConst;
import com.fleety.server.BasicServer;
import com.fleety.util.pool.thread.BasicTask;
import com.fleety.util.pool.thread.ThreadPool;

public class LogServer extends BasicServer{
	private static ThreadPool logPool = null;
	private static final Object LOG_TASK_POOL_NAME = "LogServer";
	private PrintStream oldOut = null, oldErr = null;

	private String logDir = null;
	private boolean isFullClsName = false;
	private boolean isAppend = false;
	private CustomPrintStream customPrintStream = null;

	private int keepLogDays = 5;
	private boolean forceDeleteOtherFile = false;

	private static LogServer singleInstance = null;

	private HashMap streamMap = new HashMap();
	private static String defaultOutFile = "day.log";

	public static LogServer getSingleInstance(){
		if(singleInstance == null){
			singleInstance = new LogServer();
		}
		return singleInstance;
	}

	public LogServer(){
		singleInstance = this;
	}

	public void println(String key, String info){
		if(!this.isRunning()){
			System.out.println(key + ":" + info);
			return;
		}
		ArrayList list = (ArrayList) streamMap.get(key);

		if(list != null){
			for(int i = 0; i < list.size(); i++){
				CustomPrintStream customSt = (CustomPrintStream) list.get(i);
				customSt.println(info);
			}
		} else{
			System.out.println(key + ":" + info);
		}

		//TO DO find stream object for write  $key$_log_name
	}

	public boolean startServer(){
		PoolInfo pInfo = new PoolInfo();
		pInfo.taskCapacity = 100000;
		
		try{
			logPool = ThreadPoolGroupServer.getSingleInstance().createThreadPool(LOG_TASK_POOL_NAME, pInfo);
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}
		
		Boolean tempObj = this.getBooleanPara("append_head");
		this.isAppend = tempObj == null ? false : tempObj.booleanValue();
		this.logDir = this.getStringPara("log_dir");

		int tmp = this.getIntegerPara("keepLogDays") == null ? 20 : this.getIntegerPara("keepLogDays").intValue();
		if(tmp > 0){
			this.keepLogDays = tmp;
		}
		tempObj = this.getBooleanPara("force_delete_other");
		if(tempObj != null){
			this.forceDeleteOtherFile = tempObj.booleanValue();
		}

		tempObj = this.getBooleanPara("full_name");
		this.isFullClsName = tempObj == null ? false : tempObj.booleanValue();

		this.moveFile(defaultOutFile);

		this.oldOut = System.out;
		this.oldErr = System.err;

		HashMap fileNameToStreamMap = new HashMap();
		try{
			this.customPrintStream = new CustomPrintStream(isAppend);
			fileNameToStreamMap.put(this.customPrintStream.fileName, this.customPrintStream);
		} catch(Exception e){
			e.printStackTrace();
			return false;
		}

		System.setOut(customPrintStream);
		System.setErr(customPrintStream);

		HashMap fileNameMap = initStreamNode();
		HashMap tempStreamMap = new HashMap();
		Iterator it = fileNameMap.keySet().iterator();
		while(it.hasNext()){
			String key = (String) it.next();
			String fileName = (String) fileNameMap.get(key);
			String[] fileNameArr = fileName.split(",");
			ArrayList streamList = new ArrayList();
			for(int i = 0; i < fileNameArr.length; i++){
				String tempFileName = fileNameArr[i];
				this.moveFile(tempFileName);
				CustomPrintStream customSt = (CustomPrintStream) fileNameToStreamMap.get(key);
				if(customSt == null){
					try{
						customSt = new CustomPrintStream(isAppend, tempFileName);
						fileNameToStreamMap.put(tempFileName, customSt);
					} catch(Exception e){
						e.printStackTrace();
					}
				}
				streamList.add(customSt);
			}

			tempStreamMap.put(key, streamList);
		}
		streamMap = tempStreamMap;
		
		this.isRunning = true;
		return true;
	}

	private HashMap initStreamNode(){
		HashMap fileNameMap = new HashMap();
		String temp = this.getStringPara("log_type");
		if(temp != null){
			String[] logType = temp.split(",");
			for(int i = 0; i < logType.length; i++){
				String fileName = this.getStringPara(logType[i]
						+ "_log_file");
				fileNameMap.put(logType[i], fileName);
			}
		}
		return fileNameMap;
	}

	private void moveFile(String FileName){
		File f = new File(logDir, FileName);
		f.getParentFile().mkdirs();

		Date curDate = new Date();
		String dateStr = GeneralConst.YYYY_MM_DD.format(curDate);

		String oldDateStr;
		if(f.exists()){
			long t = f.lastModified() - 60000;
			oldDateStr = GeneralConst.YYYY_MM_DD.format(new Date(t));
			if(!dateStr.equals(oldDateStr)){
				File oldFile = new File(logDir, FileName + "."
						+ oldDateStr);
				if(!oldFile.exists()){
					f.renameTo(oldFile);
				}
			}
		}
	}

	public void stopServer(){
		System.setOut(this.oldOut);
		System.setErr(this.oldErr);

		this.isRunning = false;
	}

	private class CustomPrintStream extends PrintStream{
		private boolean isAppend = false;
		private int outStreamYear = -1;
		private int outStreamMonth = -1;
		private int outStreamDay = -1;
		private String fileName = "";

		public CustomPrintStream(boolean isAppend) throws Exception{
			this(isAppend, defaultOutFile);
		}

		public CustomPrintStream(boolean isAppend, String fileName)
				throws Exception{
			super(new FileOutputStream(new File(LogServer.this.logDir, fileName), true));
			Calendar calendar = Calendar.getInstance();

			this.outStreamYear = calendar.get(Calendar.YEAR);
			this.outStreamMonth = calendar.get(Calendar.MONTH) + 1;
			this.outStreamDay = calendar.get(Calendar.DAY_OF_MONTH);
			this.fileName = fileName;

			this.isAppend = isAppend;
			
			this.removeExpiredLogs();
		}

		private void updateOutputStream(Calendar calendar){
			try{
				if(this.out != null){
					this.out.close();
				}
			} catch(Exception e){
			}

			this.out = null;

			String oldFile = fileName + "." + this.outStreamYear + "-"
					+ (this.outStreamMonth < 10 ? "0" : "")
					+ this.outStreamMonth + "-"
					+ (this.outStreamDay < 10 ? "0" : "")
					+ this.outStreamDay;
			File f = new File(LogServer.this.logDir, fileName);
			if(f.exists()){
				f.renameTo(new File(LogServer.this.logDir, oldFile));
			}

			try{
				this.out = new FileOutputStream(new File(LogServer.this.logDir, fileName), true);

				this.outStreamYear = calendar.get(Calendar.YEAR);
				this.outStreamMonth = calendar.get(Calendar.MONTH) + 1;
				this.outStreamDay = calendar.get(Calendar.DAY_OF_MONTH);
			} catch(Exception e){
				e.printStackTrace();
			}

			//扫描日志目录删除在设定的保存天数之前的日志
			removeExpiredLogs();

		}

		private void removeExpiredLogs(){
			Calendar calendar = Calendar.getInstance();
			calendar.add(Calendar.DAY_OF_MONTH, -LogServer.this.keepLogDays);
			calendar.set(Calendar.HOUR_OF_DAY, 0);
			calendar.set(Calendar.MINUTE, 0);
			calendar.set(Calendar.SECOND, 0);
			calendar.set(Calendar.MILLISECOND, 0);
			Date pointTime = calendar.getTime();

			File f = new File(LogServer.this.logDir);
			if(f.exists()){
				String[] filenames = f.list();
				if(filenames == null){
					return;
				}

				for(int i = 0; i < filenames.length; i++){
					File file = new File(LogServer.this.logDir, filenames[i]);
					if(this.isNeedDelete(file, filenames[i], pointTime)){
						file.delete();
					}
				}
			}
		}
		
		private boolean isNeedDelete(File f,String name,Date limitTime){
			if(!name.startsWith(this.fileName)){
				return false;
			}
			if(new Date(f.lastModified()).before(limitTime)){
				return true;
			}
			
			try{
				if(name.length() > 10){
					Date d = GeneralConst.YYYY_MM_DD.parse(name.substring(name.length() - 10));
					if(d.before(limitTime)){
						return true;
					}
				}
			}catch(Exception e){}
			
			return false;
		}

		public void print(Object obj){
			if(obj != null){
				this.writeData(obj.toString());
			}
		}

		public void print(String str){
			if(isAppend){
				StackTraceElement[] eles = new Exception("").getStackTrace();
				int index = 0;
				String className;
				for(index = 0; index < eles.length; index++){
					className = eles[index].getClassName();

					if(className.equals("java.lang.Throwable")){
						index = eles.length;
						break;
					}

					if(!className.equals(this.getClass().getName())){
						break;
					}
				}
				if(index < eles.length){
					String clsName = eles[index].getClassName();
					if(!isFullClsName){
						int sindex = clsName.lastIndexOf('.');
						if(sindex >= 0){
							clsName = clsName.substring(sindex + 1);
						}
					}
					StringBuffer buff = new StringBuffer(str.length()
							+ clsName.length() + 32);
					buff.append(clsName);
					buff.append("[");
					buff.append(eles[index].getMethodName());
					buff.append("]:");
					buff.append(str);
					str = buff.toString();
				}
			}

			this.writeData(str);
		}

		public void println(Object obj){
			if(obj != null){
				this.print(obj.toString() + "\n");
			}
		}

		public void println(String str){
			this.print(str + "\n");
		}

		public synchronized void write(int b){
			super.write(b);
		}
		public synchronized void write(byte[] b) throws IOException{
			super.write(b);
		}
		public synchronized void write(byte[] buf,int off,int len){
			super.write(buf, off, len);
		}
		private synchronized void writeData(String str){
			if(this.out == null){
				return;
			}

			Calendar calendar = Calendar.getInstance();
			int curDay = calendar.get(Calendar.DAY_OF_MONTH);
			if(this.outStreamDay != curDay){
				this.updateOutputStream(calendar);
			}
			StringBuffer buff = new StringBuffer(str.length() + 32);
			buff.append(GeneralConst.YYYY_MM_DD_HH_MM_SS_SSS.format(calendar.getTime()));
			buff.append("\t");
			buff.append(str);
			str = buff.toString();
			
			logPool.addTask(new LogTask(this.out,str));
		}
	}
	
	private class LogTask extends BasicTask{
		private OutputStream out = null;
		private String info = null;
		public LogTask(OutputStream out,String info){
			this.out = out;
			this.info = info;
		}
		public boolean execute(){
			try{

				byte[] data = this.info.getBytes();
				this.out.write(data);
				this.out.flush();
			} catch(Exception e){
			}
			
			return true;
		}
	}
}
