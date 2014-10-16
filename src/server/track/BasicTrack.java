/**
 * 绝密 Created on 2008-4-23 by edmund
 */
package server.track;

import java.io.*;
import java.util.*;

import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipFile;

import server.threadgroup.PoolInfo;
import server.threadgroup.ThreadPoolGroupServer;

import com.fleety.base.GeneralConst;
import com.fleety.base.InfoContainer;
import com.fleety.base.StrFilter;
import com.fleety.server.IServer;
import com.fleety.track.TrackFilter;
import com.fleety.track.TrackIO;
import com.fleety.track.TrackTimeFilter;
import com.fleety.util.pool.thread.ITask;
import com.fleety.util.pool.thread.ThreadPool;
import com.fleety.util.pool.thread.ThreadPoolEventListener;
import com.fleety.util.pool.timer.FleetyTimerTask;

public class BasicTrack implements ITrack{
	
	private TrackIO trackIo = new TrackIO();
	
	private Integer[] heads = new Integer[0];
	private String path = null;
	private String version = null;
	private String oldVersionLength=null;
	private int threadNum = 5;
	private int taskCapacity = 5000;
	private HashMap trackOutMapping = new HashMap();	
	private ThreadPool trackThreadPool = null;
	
	//默认超过当前60天的进行压缩存放
	private int daysWithZip = 60;
	/**
	 * 构造函数
	 *
	 */
	public BasicTrack(){
		
	}
	
	public void init(IServer server) throws Exception{
		IServer trackServer=server;
		try{
			this.threadNum = Integer.parseInt((String)trackServer.getPara("thread_num"));
		}catch(Exception e){
			
		}
		try{
			this.version = (String)trackServer.getPara("version");
			this.oldVersionLength=(String)trackServer.getPara("old_version_length");
			if(this.oldVersionLength!=null&&!this.oldVersionLength.equals("")){
				this.trackIo.setRecordDataLen(Integer.parseInt(this.oldVersionLength));
			}
			this.path = (String)trackServer.getPara("path");
			
			PoolInfo sInfo = new PoolInfo(ThreadPool.MULTIPLE_TASK_LIST_POOL,this.threadNum,this.taskCapacity,true);
			this.trackThreadPool = ThreadPoolGroupServer.getSingleInstance().createThreadPool(trackPoolName, sInfo);
		}catch(Exception e){
			this.destory();
			throw e;
		}
		
		String tempStr = null;
		
		tempStr = (String)server.getPara("days_with_zip");
		if(StrFilter.hasValue(tempStr)){
			try{
				this.daysWithZip = Integer.parseInt(tempStr.trim());
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		
		//是否启用轨迹自动压缩功能，轨迹读取支持该压缩文件的读取
		tempStr = (String)server.getPara("auto_zip");
		if(tempStr != null && tempStr.equals("true")){
			System.out.println("启动自动轨迹压缩功能!");
			Calendar cal = Calendar.getInstance();
			cal.set(Calendar.HOUR_OF_DAY, 2);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			long delay = cal.getTimeInMillis() - System.currentTimeMillis();
			if(delay < 0){
				delay += GeneralConst.ONE_DAY_TIME;
			}
			ThreadPoolGroupServer.getSingleInstance().createTimerPool(timerName).schedule(new FleetyTimerTask(){
				public void run(){
					BasicTrack.this.zipTrackByDay();
				}
			}, delay, GeneralConst.ONE_DAY_TIME);
		}

		Object key,value;
		Boolean hasInfo;
		List headList = new LinkedList();
		for(Iterator infoIterator = TrackIO.INFO_LOCATION_MAPPING.keySet().iterator();infoIterator.hasNext();){
			key = infoIterator.next();
			hasInfo = new Boolean((String)trackServer.getPara(key));
			if(hasInfo != null && hasInfo.booleanValue()){
				value = TrackIO.INFO_LOCATION_MAPPING.get(key);
				if(!headList.contains(value)){
					headList.add(value);
				}
			}
		}
		if(headList.size() == 0){
			System.out.println("不存在需要写入的轨迹信息!");
		}
		//强制可记录 记录的类型
		if(!headList.contains(TrackIO.RECORD_TYPE_FLAG)){
			headList.add(TrackIO.RECORD_TYPE_FLAG);
		}
		
		this.heads = new Integer[headList.size()];
		headList.toArray(this.heads);
		Arrays.sort(this.heads);		
	}
	
	private FilenameFilter fFilter = new FilenameFilter(){
		public boolean accept(File dir, String name){
			if(!name.startsWith("TRK")){
				return false;
			}
			if(name.length() != 11){
				return false;
			}
			return true;
		}
	};
	
	private void zipTrackByDay(){
		File dir = new File(this.path);
		System.out.println("Scan Track Dir:"+dir.getAbsolutePath());
		
		File[] trkDirArr = dir.listFiles(fFilter);
		
		File trkDir = null;
		File zipFile,destFile,tempFile = new File(this.path,"_t_e_m_p_.zip");
		String fName;
		Date trkDate;
		Date limitDate = new Date(System.currentTimeMillis() - (daysWithZip+1)*GeneralConst.ONE_DAY_TIME);
		for(int i=0;i<trkDirArr.length;i++){
			trkDir = trkDirArr[i];
			fName = trkDir.getName();
			try{
				trkDate = GeneralConst.YYYYMMDD.parse(fName.substring(3));
			}catch(Exception e){
				e.printStackTrace();
				continue;
			}
			
			if(trkDate.after(limitDate)){
				continue;
			}
			
			zipFile = this.getZipTrackFile(null, trkDate);
			if(zipFile.exists()){
				continue;
			}
			if(tempFile.exists()){
				tempFile.delete();
			}
			try{
				tempFile.delete();
				System.out.println("Zip Dir:"+"zip -r "+tempFile.getName()+" "+fName);
				Process p = Runtime.getRuntime().exec("zip -qr "+tempFile.getName()+" "+fName, null, dir);
				p.waitFor();
				if(p.exitValue() == 0){
					destFile = new File(this.path,fName+".zip");
					
					p = Runtime.getRuntime().exec("mv "+tempFile.getName()+" "+fName+".zip", null, dir);
					p.waitFor();
					if(p.exitValue() == 0){
						System.out.println("Rename File:"+tempFile.getName()+" -> "+destFile.getName()+" Success");
						
						p = Runtime.getRuntime().exec("rm -rf "+fName, null, dir);
						p.waitFor();
						if(p.exitValue() == 0){
							System.out.println("Delete Dir:"+fName+" Success");
						}else{
							System.out.println("Delete Dir:"+fName+" Failure");
						}
					}else{
						System.out.println("Rename File:"+tempFile.getAbsolutePath()+" -> "+destFile.getAbsolutePath()+" Failure");
					}
				}else{
					System.out.println("Zip Dir:"+fName+" Failure");
				}
				p.destroy();
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}


	public void destory() {
		ThreadPoolGroupServer.getSingleInstance().removeThreadPool(trackPoolName);
		ThreadPoolGroupServer.getSingleInstance().destroyTimerPool(timerName);
		this.closeOutputStream();
		
	}
	public void addThreadPoolEventListener(ThreadPoolEventListener listener){
		if(this.trackThreadPool != null){
			this.trackThreadPool.addEventListener(listener);
		}
	}
	
	/**
	 * 添加轨迹信息,该服务将自动把该轨迹写入到轨迹文件中
	 * @param info
	 */
	public void addTrackInfo(InfoContainer info){
		if(this.trackThreadPool == null){
			return ;
		}
		
		Object destNo = info.getInfo(TrackServer.DEST_NO_FLAG);
		while(true){
			if(!this.trackThreadPool.addTaskWithReturn(new TrackTask(destNo,info),false)){
				System.out.println("Track Info Add Error!");
				try{
					Thread.sleep(5000);
				}catch(Exception e){
					e.printStackTrace();
				}
			}else{
				break;
			}
		}
	}
	
	/**
	 * 日期比较器
	 */
	private Comparator timeComparator = new Comparator(){
		public int compare(Object o1, Object o2){
			InfoContainer a1 = (InfoContainer)o1;
			InfoContainer a2 = (InfoContainer)o2;
			long diff = a1.getDate(TrackIO.DEST_TIME_FLAG).getTime() - a2.getDate(TrackIO.DEST_TIME_FLAG).getTime();
			
			if(diff > 0){
				return 1;
			}else if(diff < 0){
				return -1;
			}else{
				return 0;
			}
		}
	};
	
	/**
	 * 查询目标在指定时间段内的轨迹信息
	 * 需要提供DEST_NO_FLAG START_DATE_FLAG END_DATE_FLAG三个信息
	 * @param queryInfo
	 * @return
	 */
	public InfoContainer[] getTrackInfo(InfoContainer queryInfo){
		return getTrackInfo(queryInfo,null);
	}
	
	public InfoContainer[] getTrackInfo(InfoContainer queryInfo,TrackFilter filter){
		String destNo = queryInfo.getString(TrackServer.DEST_NO_FLAG);
		Date startDate = queryInfo.getDate(TrackServer.START_DATE_FLAG);
		Date endDate = queryInfo.getDate(TrackServer.END_DATE_FLAG);

		Date curDate = new Date(startDate.getTime());
		List trackList = new LinkedList();
		InputStream in = null;
		byte[] buff = null;
		TrackInputStream trackFile = null;
		
		//设置过滤滤镜
		TrackTimeFilter trackFilter = new TrackTimeFilter(startDate,endDate){
			public int filterTrack(InfoContainer info){
				int flag = super.filterTrack(info);
				
				if(flag == TrackFilter.CONTINUE_FLAG){
					if(this.optionalInfo != null){
						flag = ((TrackFilter)this.optionalInfo).filterTrack(info);
						if(flag == TrackFilter.BREAK_FLAG){
							this.setBreak(true);
						}
					}
				}
				
				return flag;
			}
		};
		if(filter!=null){
			trackFilter.setOptional(filter);
		}
		
		while(curDate.before(endDate) && !trackFilter.isBreak()){
			try{
				trackFile = getTrackFileInputStream(destNo,curDate);
				if(trackFile != null){
					buff = new byte[trackFile.getFileSize()];
					int count = 0,tempCount;
					while(count < buff.length){
						tempCount = trackFile.getInputStream().read(buff,count,buff.length - count);
						if(tempCount < 0){
							throw new Exception("Error File End");
						}
						count += tempCount;
					}
					trackFile.close();
						
					in = new ByteArrayInputStream(buff);
					trackList.addAll(trackIo.readTrackRecord(in, trackFilter));
					in.close();
				}
			}catch(Exception e){
				e.printStackTrace();
			}finally{
				if(trackFile != null){
					trackFile.close();
				}
			}
			
			curDate.setTime(curDate.getTime()+GeneralConst.ONE_DAY_TIME);
			try{
				curDate = GeneralConst.YYYYMMDD.parse(GeneralConst.YYYYMMDD.format(curDate));
			}catch(Exception e){
				e.printStackTrace();
				return null;
			}
		}
		
		InfoContainer[] infos = new InfoContainer[trackList.size()];
		trackList.toArray(infos);
		Arrays.sort(infos,timeComparator);
		
		return infos;
	}
	
	/**
	 * 得到指定日期,指定号牌的目标轨迹文件对象
	 * @param destNo
	 * @param destDate
	 * @return
	 */
	public File getTrackFile(String destNo,Date destDate){
		return new File(this.path,"TRK"+GeneralConst.YYYYMMDD.format(destDate)+"/"+destNo+".LOG");
	}
	
	public File getZipTrackFile(String destNo,Date destDate){
		return new File(this.path,"TRK"+GeneralConst.YYYYMMDD.format(destDate)+".zip");
	}
	
	private TrackInputStream getTrackFileInputStream(String destNo,Date destDate) throws Exception{
		File f = this.getTrackFile(destNo, destDate);
		if(f.exists()){
			return new TrackInputStream(new FileInputStream(f),(int)f.length());
		}else{
			String dirName = f.getParentFile().getName();
			String fname = f.getName();
			f = this.getZipTrackFile(destNo, destDate);
			if(f.exists()){
				ZipFile zf = new ZipFile(f);
				ZipEntry entry = zf.getEntry(dirName+"/"+fname);
				if(entry == null){
					return null;
				}
				return new TrackInputStream(zf.getInputStream(entry),(int)entry.getSize());
			}
		}
		return null;
	}
	
	private synchronized TrackStreamInfo getTrackOutputStream(InfoContainer info){
		Object destNo = info.getInfo(TrackServer.DEST_NO_FLAG);
		if(destNo == null){
			return null;
		}
		
		TrackStreamInfo outInfo = (TrackStreamInfo)this.trackOutMapping.get(destNo);
		Date destDate = info.getDate(TrackIO.DEST_TIME_FLAG);
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(destDate.getTime());
		int day = calendar.get(Calendar.DAY_OF_MONTH);
		
		try{
			if(outInfo == null){
				File trackFile = this.getTrackFile(destNo.toString(),destDate);
				trackFile.getParentFile().mkdirs();
				
				outInfo = new TrackStreamInfo(new FileOutputStream(trackFile,true),day);
				this.trackOutMapping.put(destNo, outInfo);
			}else if(outInfo.day != day){
				synchronized(outInfo){
					File trackFile = this.getTrackFile(destNo.toString(),destDate);
					trackFile.getParentFile().mkdirs();
					
					OutputStream out = new FileOutputStream(trackFile,true);
					outInfo.updateInfo(out, day);
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return outInfo;
	}
	
	private void closeOutputStream(){
		Iterator tempIterator = this.trackOutMapping.values().iterator();
		TrackStreamInfo out = null;
		while(tempIterator.hasNext()){
			out = (TrackStreamInfo)tempIterator.next();
			try{
				out.close();
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		
		this.trackOutMapping.clear();
	}
	
	private class TrackInputStream{
		private InputStream in = null;
		private int fSize = 0;
		
		public TrackInputStream(InputStream in,int fSize){
			this.in = in;
			this.fSize = fSize;
		}
		
		public InputStream getInputStream(){
			return this.in;
		}
		public int getFileSize(){
			return this.fSize;
		}
		
		public void close(){
			try{
				this.in.close();
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}
	
	private class TrackStreamInfo{
		public OutputStream out = null;
		public int day = 0;
		
		public TrackStreamInfo(OutputStream out,int day){
			this.out = out;
			this.day = day;
		}
		
		public void updateInfo(OutputStream out,int day){
			try{
				if(this.out != null){
					this.out.close();
				}
			}catch(Exception e){}
			
			this.out = out;
			this.day = day;
		}
		
		public void close(){
			try{
				if(this.out != null){
					this.out.close();
				}
			}catch(Exception e){}
		}
	}
	
	private class TrackTask implements ITask{
		private Object flag = null;
		private InfoContainer info = null;
		
		public TrackTask(Object flag,InfoContainer info){
			this.flag = flag;
			this.info = info;
		}
		
		public Object getFlag(){
			return this.flag;
		}
		
		public String getDesc(){
			return null;
		}
		
		public boolean execute() throws Exception{
			TrackStreamInfo outInfo = getTrackOutputStream(info);
			synchronized(outInfo){
				if(outInfo.out == null){
					return false;
				}
				trackIo.writeTrackRecord(outInfo.out, new InfoContainer[]{info}, version, heads);
			}
			
			return true;
		}
	}

	public void closeAllStream() {
		// TODO Auto-generated method stub
		
	}

	public void closeStream(String destNo) {
		// TODO Auto-generated method stub
		
	}


}
