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

public class BufferTrack implements ITrack {

	private TrackIO trackIo = new TrackIO();

	private Integer[] heads = new Integer[0];

	private String version = TrackIO.VERSION_ICLOUD_FLAG;
	private String old_record_version = TrackIO.VERSION_ICLOUD_FLAG;

	private int threadNum = 5;

	private int taskCapacity = 5000;

	private String path = "TRACK";

	private String diskVolume = "data";

	private int volumeCount = 1;

	private int monthsInVolume = 1;

	private ThreadPool trackThreadPool = null;

	// 默认超过当前60天的进行压缩存放
	private int daysWithZip = 60;

	// 轨迹缓存集合管理器，如果intevalTime=0或者maxSize=0,即时输出轨迹信息
	private HashMap trackInfoMapping = new HashMap();

	private int cacheMaxSize = 15;// 缓存轨迹的最大数量

	private long cacheMaxTime = 5;// 轮询输出的最大时间

	private boolean isAutoCloseOut = false;// 是否需要缓存轨迹,只有在输出的时候需要判断是否缓存轨迹

	// 是否使用机构作为子目录
	private boolean isCompanySubDir = false;

	// 轨迹缓存输出管理器
	private TrackStreamInfoManager trackStreamInfoManager = new TrackStreamInfoManager();

	//最后一次更新位置汇报时间
	private Hashtable trackInfoLastUpdateTable=new Hashtable();
	/**
	 * 构造函数
	 * 
	 */
	public BufferTrack() {
	}

	public void init(IServer server) throws Exception {
		IServer trackServer = server;
		String temp = null;
		try {
			temp = (String) trackServer.getPara("thread_num");
			if (StrFilter.hasValue(temp)) {
				this.threadNum = Integer.parseInt(temp.trim());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			// 版本信息
			temp = (String) trackServer.getPara("version");
			if (StrFilter.hasValue(temp)) {
				this.version = temp;
			}
			// 版本信息
			temp = (String) trackServer.getPara("old_record_version");
			if (StrFilter.hasValue(temp)) {
				this.old_record_version = temp;
			}
			// 路径
			this.path = (String) trackServer.getPara("path");
			// 卷名
			this.diskVolume = (String) trackServer.getPara("disk_volume");
			// 分几个卷
			temp = (String) trackServer.getPara("volume_count");
			if (temp != null && !temp.equals("")) {
				this.volumeCount = Integer.parseInt(temp);
			}
			// 一个卷内存放几个月的轨迹
			temp = (String) trackServer.getPara("months_in_volume");
			if (temp != null && !temp.equals("")) {
				this.monthsInVolume = Integer.parseInt(temp);
			}
			// 缓存轨迹点的最大个数
			temp = (String) trackServer.getPara("cache_max_size");
			if (temp != null && !temp.equals("")) {
				this.cacheMaxSize = Integer.parseInt(temp);
			}
			// 缓存轨迹输出的时间间隔
			temp = (String) trackServer.getPara("cache_max_time");
			if (temp != null && !temp.equals("")) {
				this.cacheMaxTime = Integer.parseInt(temp) * 60 * 1000;
			}
			// 是否立刻关闭输出流
			temp = (String) trackServer.getPara("is_auto_close_out");
			if (temp != null && !temp.equals("")) {
				this.isAutoCloseOut = Boolean.parseBoolean(temp);
			}

			PoolInfo sInfo = new PoolInfo(ThreadPool.MULTIPLE_TASK_LIST_POOL,
					this.threadNum, this.taskCapacity, true);
			this.trackThreadPool = ThreadPoolGroupServer.getSingleInstance()
					.createThreadPool(trackPoolName, sInfo);
		} catch (Exception e) {
			this.destory();
			throw e;
		}

		String tempStr = null;

		tempStr = (String) server.getPara("company_sub_dir");
		if (tempStr != null && tempStr.trim().equalsIgnoreCase("true")) {
			isCompanySubDir = true;
		}

		tempStr = (String) server.getPara("days_with_zip");
		if (StrFilter.hasValue(tempStr)) {
			try {
				this.daysWithZip = Integer.parseInt(tempStr.trim());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		// 是否启用轨迹自动压缩功能，轨迹读取支持该压缩文件的读取
		tempStr = (String) server.getPara("auto_zip");
		if (tempStr != null && tempStr.equals("true")) {
			System.out.println("启动自动轨迹压缩功能!");
			Calendar cal = Calendar.getInstance();
			cal.set(Calendar.HOUR_OF_DAY, 2);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			long delay = cal.getTimeInMillis() - System.currentTimeMillis();
			if (delay < 0) {
				delay += GeneralConst.ONE_DAY_TIME;
			}
			ThreadPoolGroupServer.getSingleInstance().createTimerPool(timerName)
					.schedule(new FleetyTimerTask() {
						public void run() {
							BufferTrack.this.zipTrackByDay();
						}
					}, delay, GeneralConst.ONE_DAY_TIME);
		}

		Object key, value;
		Boolean hasInfo;
		List headList = new LinkedList();
		for (Iterator infoIterator = TrackIO.INFO_LOCATION_MAPPING.keySet()
				.iterator(); infoIterator.hasNext();) {
			key = infoIterator.next();
			hasInfo = new Boolean((String) trackServer.getPara(key));
			if (hasInfo != null && hasInfo.booleanValue()) {
				value = TrackIO.INFO_LOCATION_MAPPING.get(key);
				if (!headList.contains(value)) {
					headList.add(value);
				}
			}
		}
		if (!this.version.equals(TrackIO.VERSION_ICLOUD_FLAG)
				&& headList.size() == 0) {
			System.out.println("不存在需要写入的轨迹信息!");
		}
		// 强制可记录 记录的类型
		if (!headList.contains(TrackIO.RECORD_TYPE_FLAG)) {
			headList.add(TrackIO.RECORD_TYPE_FLAG);
		}

		this.heads = new Integer[headList.size()];
		headList.toArray(this.heads);
		Arrays.sort(this.heads);

		// 如果用户配置周期性输出缓存轨迹，需要设定一个timer周期性工作
		if (this.cacheMaxSize > 0 && this.cacheMaxTime > 0) {
			ThreadPoolGroupServer.getSingleInstance().createTimerPool(BufferTrack.class.getName()+"["+this.hashCode()+"]")
					.schedule(new TrackInfoTimerTask(), 0, this.cacheMaxTime);
		}

	}

	private void zipTrackByDay() {
		if (this.volumeCount <= 1) {
			this.zipTrackForOneColumn(new File(this.path));
		} else {
			for (int i = 1; i <= this.volumeCount; i++) {
				this.zipTrackForOneColumn(new File((this.diskVolume + i)
						+ File.separator + this.path));
			}
		}
	}

	private FilenameFilter fFilter = new FilenameFilter() {
		public boolean accept(File dir, String name) {
			if (!name.startsWith("TRK")) {
				return false;
			}
			if (name.length() != 11) {
				return false;
			}
			return true;
		}
	};

	private void zipTrackForOneColumn(File dir) {
		System.out.println("Scan Track Dir:" + dir.getAbsolutePath());
		if (!dir.exists()) {
			System.out.println("Dir:" + dir.getAbsolutePath() + " not exist!");
			return;
		}

		File[] trkDirArr = dir.listFiles(fFilter);

		File trkDir = null;
		File zipFile, destFile, tempFile = new File(this.path, "_t_e_m_p_.zip");
		String fName;
		Date trkDate;
		Date limitDate = new Date(System.currentTimeMillis()
				- (daysWithZip + 1) * GeneralConst.ONE_DAY_TIME);
		for (int i = 0; i < trkDirArr.length; i++) {
			trkDir = trkDirArr[i];
			fName = trkDir.getName();
			try {
				trkDate = GeneralConst.YYYYMMDD.parse(fName.substring(3));
			} catch (Exception e) {
				e.printStackTrace();
				continue;
			}

			if (trkDate.after(limitDate)) {
				continue;
			}

			zipFile = this.getZipTrackFile(null, trkDate);
			if (zipFile.exists()) {
				continue;
			}
			if (tempFile.exists()) {
				tempFile.delete();
			}
			try {
				tempFile.delete();
				// 进行zip压缩时，会输出到控制台很多内容，这些信息会输出到虚拟机的一个缓存中，这个缓存有限，如果数据不从该缓存中读取掉，程序将卡住，直到缓存有空间。
				System.out.println("Zip Dir:" + "zip -qr " + tempFile.getName()
						+ " " + fName);
				Process p = Runtime.getRuntime().exec(
						"zip -qr " + tempFile.getName() + " " + fName, null,
						dir);
				// Util.printInfoFromStream(p.getInputStream(), true);
				p.waitFor();
				if (p.exitValue() == 0) {
					destFile = new File(this.path, fName + ".zip");

					p = Runtime.getRuntime().exec(
							"mv " + tempFile.getName() + " " + fName + ".zip",
							null, dir);
					p.waitFor();
					if (p.exitValue() == 0) {
						System.out.println("Rename File:" + tempFile.getName()
								+ " -> " + destFile.getName() + " Success");

						p = Runtime.getRuntime().exec("rm -rf " + fName, null,
								dir);
						p.waitFor();
						if (p.exitValue() == 0) {
							System.out.println("Delete Dir:" + fName
									+ " Success");
						} else {
							System.out.println("Delete Dir:" + fName
									+ " Failure");
						}
					} else {
						System.out.println("Rename File:"
								+ tempFile.getAbsolutePath() + " -> "
								+ destFile.getAbsolutePath() + " Failure");
					}
				} else {
					System.out.println("Zip Dir:" + fName + " Failure");
				}
				p.destroy();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void destory() {
		ThreadPoolGroupServer.getSingleInstance().removeThreadPool(
				trackPoolName);
		ThreadPoolGroupServer.getSingleInstance().destroyTimerPool(timerName);
		trackStreamInfoManager.closeAllOutputStream();
	}

	public void addThreadPoolEventListener(ThreadPoolEventListener listener) {
		if (this.trackThreadPool != null) {
			this.trackThreadPool.addEventListener(listener);
		}
	}

	/**
	 * 添加轨迹信息,该服务将自动把该轨迹写入到轨迹文件中
	 * 
	 * @param info
	 */
	public void addTrackInfo(InfoContainer info) {
		Object destNo = info.getInfo(TrackServer.DEST_NO_FLAG);
		TrackInfo trackInfo = null;
		synchronized (this.trackInfoMapping) {
			trackInfo = (TrackInfo) trackInfoMapping.get(destNo);
			if (trackInfo == null) {
				trackInfo = new TrackInfo(destNo.toString(), this.cacheMaxSize,
						this.cacheMaxTime);
				this.trackInfoMapping.put(destNo, trackInfo);
			}
		}
		
		this.trackInfoLastUpdateTable.put(destNo, new Long(System.currentTimeMillis()));
		
		trackInfo.addInfo(info);
		trackInfo.checkFlush();
	}

	/**
	 * 查询目标在指定时间段内的轨迹信息 需要提供DEST_NO_FLAG START_DATE_FLAG END_DATE_FLAG三个信息
	 * 
	 * @param queryInfo
	 * @return
	 */
	public InfoContainer[] getTrackInfo(InfoContainer queryInfo) {
		return getTrackInfo(queryInfo, null);
	}

	/**
	 * 查询轨迹必须提供车牌号码，开始时间，结束时间，如果公司id不为空，表示查询按公司归类的大平台轨迹
	 * 
	 * @param queryInfo
	 * @param filter
	 * @return
	 */
	public InfoContainer[] getTrackInfo(InfoContainer queryInfo,
			TrackFilter filter) {
		String companyId = queryInfo.getString(TrackServer.COMPANY_ID_FLAG);
		String destNo = queryInfo.getString(TrackServer.DEST_NO_FLAG);
		Date startDate = queryInfo.getDate(TrackServer.START_DATE_FLAG);
		Date endDate = queryInfo.getDate(TrackServer.END_DATE_FLAG);

		if (!this.isCompanySubDir) {
			companyId = null;
		}

		Date curDate = new Date(startDate.getTime());
		List trackList = new LinkedList();
		InputStream in = null;
		byte[] buff = null;
		TrackInputStream trackFile = null;

		// 设置过滤滤镜
		TrackTimeFilter trackFilter = new TrackTimeFilter(startDate, endDate) {
			public int filterTrack(InfoContainer info) {
				int flag = super.filterTrack(info);

				if (flag == TrackFilter.CONTINUE_FLAG) {
					if (this.optionalInfo != null) {
						return ((TrackFilter) this.optionalInfo)
								.filterTrack(info);
					}
				}
				return flag;
			}
		};
		if (filter != null)
			trackFilter.setOptional(filter);

		while (curDate.before(endDate)) {
			try {
				trackFile = this.getTrackFileInputStream(destNo, curDate,
						companyId);
				if (trackFile != null) {
					buff = new byte[trackFile.getFileSize()];
					int count = 0, tempCount;
					while (count < buff.length) {
						tempCount = trackFile.getInputStream().read(buff,
								count, buff.length - count);
						if (tempCount < 0) {
							throw new Exception("Error File End");
						}
						count += tempCount;
					}
					trackFile.close();

					in = new ByteArrayInputStream(buff);
					if(this.version.equals(TrackIO.VERSION_ICLOUD_FLAG)
							|| this.old_record_version.equals(TrackIO.VERSION_ICLOUD_FLAG)){
						trackIo.setOLD_RECORD_VERSION(TrackIO.VERSION_ICLOUD_FLAG);
					}					
					trackList.addAll(trackIo.readTrackRecord(in, trackFilter));
					in.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (trackFile != null) {
					trackFile.close();
				}
			}

			curDate.setTime(curDate.getTime() + GeneralConst.ONE_DAY_TIME);
			try {
				curDate = GeneralConst.YYYYMMDD.parse(GeneralConst.YYYYMMDD
						.format(curDate));
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}

		InfoContainer[] infos = new InfoContainer[trackList.size()];
		trackList.toArray(infos);
		Arrays.sort(infos, timeComparator);

		return infos;
	}
	public void closeAllStream(){
		this.trackStreamInfoManager.closeAllOutputStream();
	}
	public void closeStream(String destNo){
		this.trackStreamInfoManager.closeOutPutStream(destNo);
	}

	/**
	 * 日期比较器
	 */
	private Comparator timeComparator = new Comparator() {
		public int compare(Object o1, Object o2) {
			InfoContainer a1 = (InfoContainer) o1;
			InfoContainer a2 = (InfoContainer) o2;
			long diff = a1.getDate(TrackIO.DEST_TIME_FLAG).getTime()
					- a2.getDate(TrackIO.DEST_TIME_FLAG).getTime();

			if (diff > 0) {
				return 1;
			} else if (diff < 0) {
				return -1;
			} else {
				return 0;
			}
		}
	};

	/**
	 * 得到指定日期,指定号牌的目标轨迹文件对象， iflow版本
	 * 
	 * @param destNo
	 * @param destDate
	 * @return
	 */
	public File getTrackFile(String destNo, Date destDate) {
		return this.getTrackFile(destNo, destDate, null);
	}

	public File getZipTrackFile(String destNo, Date destDate) {
		return getZipTrackFile(destNo, destDate, null);
	}

	/**
	 * 兼容大平台按照机构id目录划分,有公司id指定为大平台版本
	 * 
	 * @param destNo
	 * @param destDate
	 * @param companyId
	 * @return
	 */
	public File getTrackFile(String destNo, Date destDate, String companyId) {
		return this.trackStreamInfoManager.getTrackFile(destNo, destDate,
				companyId);
	}

	public File getZipTrackFile(String destNo, Date destDate, String companyId) {
		return this.trackStreamInfoManager.getZipTrackFile(destNo, destDate,
				companyId);
	}

	private TrackInputStream getTrackFileInputStream(String destNo,
			Date destDate, String companyId) throws Exception {
		File f = this.getTrackFile(destNo, destDate, companyId);
		if (f.exists()) {
			return new TrackInputStream(new FileInputStream(f), (int) f
					.length());
		} else {
			String dirName = f.getParentFile().getParentFile().getName();
			String subDirName = f.getParentFile().getName();
			String fname = f.getName();
			f = this.getZipTrackFile(destNo, destDate, companyId);
			if (f.exists()) {
				ZipFile zf = new ZipFile(f);
				ZipEntry entry = zf.getEntry(dirName + "/" + subDirName + "/"
						+ fname);
				if (entry == null) {
					return null;
				}
				return new TrackInputStream(zf.getInputStream(entry),
						(int) entry.getSize());
			}
		}
		return null;
	}

	private class TrackInputStream {
		private InputStream in = null;

		private int fSize = 0;

		public TrackInputStream(InputStream in, int fSize) {
			this.in = in;
			this.fSize = fSize;
		}

		public InputStream getInputStream() {
			return this.in;
		}

		public int getFileSize() {
			return this.fSize;
		}

		public void close() {
			try {
				this.in.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 轨迹管理器
	 * 
	 * @author spyu
	 * 
	 */
	private class TrackStreamInfoManager {
		private HashMap trackOutMapping = null;

		/**
		 * 构造函数
		 * 
		 */
		public TrackStreamInfoManager() {
			trackOutMapping = new HashMap();
			if(!isAutoCloseOut){
				//周期性关闭数据流
				ThreadPoolGroupServer.getSingleInstance().createTimerPool(BufferTrack.class.getName()+"-AutoClose["+this.hashCode()+"]").schedule(new FleetyTimerTask() {
					public void run() {
						Object[] keys = null;
						synchronized(trackInfoLastUpdateTable){
							keys = new Object[trackInfoLastUpdateTable.size()];
							trackInfoLastUpdateTable.keySet().toArray(keys);
						}								
							String destNo = null;
							Long lastUpdateTime=null;
							for(int i=0;i<keys.length;i++) {
								destNo=(String)keys[i];
								lastUpdateTime=(Long)trackInfoLastUpdateTable.get(destNo);
								try {
									if (lastUpdateTime != null && System.currentTimeMillis()
											- lastUpdateTime.longValue() > 300000) {									
										closeOutPutStream(destNo);
										trackInfoLastUpdateTable.remove(destNo);
									}
								} catch (Exception e) {
									e.printStackTrace();
								}
							}

					}

				}, 60000);
			}
			
		}

		/**
		 * 获取一个输出流对象
		 * 
		 * @param info
		 * @return
		 */
		public TrackStreamInfo getTrackOutputStream(InfoContainer info) {
			String companyId = info.getString(TrackServer.COMPANY_ID_FLAG);
			Object destNo = info.getInfo(TrackServer.DEST_NO_FLAG);
			if (destNo == null) {
				return null;
			}
			TrackStreamInfo outInfo = null;

			Date destDate = info.getDate(TrackIO.DEST_TIME_FLAG);
			Calendar calendar = Calendar.getInstance();
			calendar.setTimeInMillis(destDate.getTime());
			int day = calendar.get(Calendar.DAY_OF_MONTH);
			
			synchronized (trackOutMapping) {
				outInfo = (TrackStreamInfo) trackOutMapping.get(destNo);
			}
			
			try {
				if (outInfo == null) {
					File trackFile = this.getTrackFile(destNo.toString(),
								destDate, companyId);
					trackFile.getParentFile().mkdirs();
					OutputStream out = new FileOutputStream(trackFile, true);
					outInfo = new TrackStreamInfo(out, day);
					if (!isAutoCloseOut) {
						synchronized (trackOutMapping) {
							trackOutMapping.put(destNo, outInfo);
						}
					}
				} else if (outInfo.day != day) {
					File trackFile = this.getTrackFile(destNo.toString(),
								destDate, companyId);
					trackFile.getParentFile().mkdirs();
					OutputStream out = new FileOutputStream(trackFile, true);
					outInfo.updateInfo(out, day);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
				
			return outInfo;
		}

		/**
		 * 关闭全部流对象
		 * 
		 */
		public void closeAllOutputStream() {
			synchronized (trackOutMapping) {
				Iterator tempIterator = trackOutMapping.values().iterator();
				TrackStreamInfo out = null;
				while (tempIterator.hasNext()) {
					out = (TrackStreamInfo) tempIterator.next();
					try {
						out.close();
						tempIterator.remove();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}

		/**
		 * 关闭一辆车轨迹的数据流
		 * 
		 * @param destNo
		 * @return
		 */
		public boolean closeOutPutStream(String destNo) {
			TrackStreamInfo out = null;
			synchronized (trackOutMapping) {
				out = (TrackStreamInfo) trackOutMapping.get(destNo);
			}
			if (out != null) {
				synchronized(out){
					out.close();
				}
				synchronized(trackOutMapping){
					if(trackOutMapping.get(destNo) == out){
						trackOutMapping.remove(destNo);
					}
				}
				return true;
			} else {
				return false;
			}
		}

		/**
		 * 得到轨迹文件,如果companyId存在，表示读取大平台的按机构分目录的版本轨迹
		 * 
		 * @param destNo
		 * @param destDate
		 * @param version
		 * @param companyId
		 * @return
		 */
		public File getTrackFile(String destNo, Date destDate, String companyId) {
			String filePath = "";
			String diskVolumeStr = null;

			Calendar cal = Calendar.getInstance();
			cal.setTime(destDate);
			int month = cal.get(Calendar.MONTH) + 1;

			diskVolumeStr = this.getVolumnName(month);
			if (diskVolumeStr == null) {
				filePath = path + File.separator + "TRK"
						+ GeneralConst.YYYYMMDD.format(destDate);
			} else {
				filePath = diskVolumeStr + File.separator + path
						+ File.separator + "TRK"
						+ GeneralConst.YYYYMMDD.format(destDate);
			}
			String sep = this.getSubDirName(companyId, destNo);

			filePath = filePath + File.separator + sep + File.separator
					+ destNo + ".LOG";

			return new File(filePath);
		}

		public File getTrackPath(Date destDate) {
			String filePath = null;

			Calendar cal = Calendar.getInstance();
			cal.setTime(destDate);
			int month = cal.get(Calendar.MONTH) + 1;

			String diskPartitionName = this.getVolumnName(month);
			if (diskPartitionName == null) {
				filePath = path;
			} else {
				filePath = diskPartitionName + File.separator + path;
			}

			return new File(filePath);
		}

		public File getZipTrackFile(String destNo, Date destDate,
				String companyId) {
			String filePath = null;

			Calendar cal = Calendar.getInstance();
			cal.setTime(destDate);
			int month = cal.get(Calendar.MONTH) + 1;

			String diskPartitionName = this.getVolumnName(month);
			if (diskPartitionName == null) {
				filePath = path + File.separator + "TRK"
						+ GeneralConst.YYYYMMDD.format(destDate) + ".zip";
			} else {
				filePath = diskPartitionName + File.separator + path
						+ File.separator + "TRK"
						+ GeneralConst.YYYYMMDD.format(destDate) + ".zip";
			}

			return new File(filePath);
		}

		private String getSubDirName(String companyId, String destNo) {
			if (companyId != null && !companyId.equals("")) {
				return companyId;
			} else {
				if (destNo.length() < 4) {
					return "others";
				} else {
					return destNo.substring(1, 4);
				}
			}
		}

		private String getVolumnName(int month) {
			if (volumeCount <= 1) {
				return null;
			} else {
				int volumeNum = ((((month - 1) / monthsInVolume + 1) - 1) % volumeCount) + 1;
				return diskVolume + volumeNum;
			}
		}
	}

	/**
	 * 轨迹输出流对象类
	 * 
	 * @author spyu
	 * 
	 */
	private class TrackStreamInfo {
		public OutputStream out = null;
		public int day = 0;


		public TrackStreamInfo(OutputStream out, int day) {
			this.out = out;
			this.day = day;
		}

		public void updateInfo(OutputStream out, int day) {
			try {
				if (this.out != null) {
					this.out.close();
				}
			} catch (Exception e) {
			}

			this.out = out;
			this.day = day;
		}

		public void close() {
			try {
				if (this.out != null) {
					this.out.flush();
					this.out.close();
				}
			} catch (Exception e) {
			}
		}
	}

	/**
	 * 写轨迹任务，类
	 * 
	 * @author spyu
	 * 
	 */
	private class TrackTask implements ITask {
		private Object flag = null;

		private InfoContainer[] infos = null;

		public TrackTask(Object flag, InfoContainer[] infos) {
			this.flag = flag;
			this.infos = infos;
		}

		public Object getFlag() {
			return this.flag;
		}

		public String getDesc() {
			return null;
		}

		/**
		 * 信息分类器，按照天进行分类
		 */
		private List getDaysInfoList(InfoContainer[] infos) {
			List resultList = new ArrayList();
			HashMap mapping = new HashMap();

			InfoContainer info = null;
			List infoList = null;
			for (int i = 0; i < infos.length; i++) {
				info = infos[i];
				Date destDate = info.getDate(TrackIO.DEST_TIME_FLAG);
				Calendar calendar = Calendar.getInstance();
				calendar.setTimeInMillis(destDate.getTime());
				int day = calendar.get(Calendar.DAY_OF_MONTH);
				infoList = (List) mapping.get(new Integer(day));
				if (infoList == null) {
					infoList = new ArrayList();
					mapping.put(new Integer(day), infoList);
				}
				infoList.add(info);
			}

			Iterator itr = mapping.keySet().iterator();
			InfoContainer[] tempInfos = null;

			while (itr.hasNext()) {
				infoList = ((ArrayList) mapping.get(itr.next()));
				tempInfos = new InfoContainer[infoList.size()];
				infoList.toArray(tempInfos);
				resultList.add(tempInfos);
			}

			return resultList;
		}

		public boolean execute() throws Exception {
			// 对轨迹按照天进行分类
			List infosList = this.getDaysInfoList(this.infos);
			InfoContainer[] tempInfos = null;
			if (infosList == null || infosList.size() == 0) {
				return false;
			}

			for (Iterator itr = infosList.iterator(); itr.hasNext();) {
				tempInfos = (InfoContainer[]) itr.next();
				if (tempInfos.length > 0) {
					TrackStreamInfo outInfo = trackStreamInfoManager
							.getTrackOutputStream(tempInfos[0]);
					try{
						synchronized (outInfo) {
							if (outInfo.out == null) {
								return false;
							}
							long t = System.currentTimeMillis();
							trackIo.writeTrackRecord(outInfo.out, tempInfos,
									version, heads);
							long t1 = System.currentTimeMillis();
							if(t1-t > 10000){
								System.out.println("Long Time:"+(t1-t));
							}
						}
					}finally{
						// 如果是缓存轨迹，写入后立即关闭输出流
						if (isAutoCloseOut) {
							outInfo.close();
						}
					}
				}
			}

			return true;
		}
	}

	/**
	 * 轨迹信息管理器类
	 * 
	 * @author spyu
	 * 
	 */
	private class TrackInfo {

		private String destNo = null;

		private Vector infoVector = null;

		private long lastOutTime = 0;

		private int cacheMaxSize = 0;

		private long cacheMaxTime = 0;

		public TrackInfo(String destNo, int cacheMaxSize, long cacheMaxTime) {
			this.destNo = destNo;
			this.lastOutTime = System.currentTimeMillis();
			this.cacheMaxSize = cacheMaxSize;
			this.cacheMaxTime = cacheMaxTime;
			this.infoVector = new Vector(Math.max(8,Math.min(64,this.cacheMaxSize)));
		}

		/**
		 * 得到缓冲池中的所有信息
		 * 
		 * @return
		 */
		private InfoContainer[] getInfos() {
			InfoContainer[] infos = null;
			infos = new InfoContainer[this.infoVector.size()];
			this.infoVector.toArray(infos);
			return infos;
		}

		/**
		 * 向缓冲池中添加信息
		 * 
		 * @param info
		 */
		public void addInfo(InfoContainer info) {
			this.infoVector.add(info);
		}

		/**
		 * 检查缓冲池
		 * 
		 * @param cacheMaxSize
		 * @param cacheMaxTime
		 * @return
		 */
		public synchronized boolean checkFlush() {

			if (this.infoVector.size() == 0) {
				return false;
			} else if (this.infoVector.size() >= this.cacheMaxSize
					|| System.currentTimeMillis() - this.lastOutTime >= this.cacheMaxTime) {
				this.outTrackInfo();
				return true;
			}
			return false;
		}

		/**
		 * 缓冲池对外输出
		 * 
		 */
		private void outTrackInfo() {
			if (trackThreadPool == null) {
				return;
			}
			String key = this.destNo;
			InfoContainer[] tArr = this.getInfos();
			while(true){
				boolean isOk = trackThreadPool.addTaskWithReturn(new TrackTask(key, tArr),false);
				if(!isOk){
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
			this.reSet();
		}

		private void reSet() {
			this.lastOutTime = System.currentTimeMillis();
			this.infoVector.clear();
		}
	}

	/**
	 * 轨迹信息周期性输出Timer
	 * 
	 * @author spyu
	 * 
	 */
	private class TrackInfoTimerTask extends FleetyTimerTask {
		public void run() {
			Object[] keys = null;
			synchronized(trackInfoMapping){
				keys = new Object[trackInfoMapping.size()];
				trackInfoMapping.keySet().toArray(keys);
			}

			TrackInfo trackInfo = null;
			for(int i=0;i<keys.length;i++){
				synchronized(trackInfoMapping){
					trackInfo =  (TrackInfo)trackInfoMapping.get(keys[i]);
				}
				if (trackInfo == null) {
					continue;
				}
				trackInfo.checkFlush();
			}
		}
	}

	

}
