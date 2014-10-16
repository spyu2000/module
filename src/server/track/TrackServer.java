/**
 * 绝密 Created on 2008-4-23 by edmund
 */
package server.track;

import java.io.*;
import java.util.*;
import com.fleety.base.InfoContainer;
import com.fleety.server.BasicServer;
import com.fleety.track.TrackFilter;
import com.fleety.util.pool.thread.ThreadPoolEventListener;

public class TrackServer extends BasicServer {

	public static final Object COMPANY_ID_FLAG = new Object();

	public static final Object DEST_NO_FLAG = new Object();

	public static final Object START_DATE_FLAG = new Object();

	public static final Object END_DATE_FLAG = new Object();

	private String trackClassName = "server.track.BasicTrack";

	private ITrack trackObject = null;

	private static TrackServer singleInstance = null;

	public static TrackServer getSingleInstance() {
		if (singleInstance == null) {
			synchronized (TrackServer.class) {
				if (singleInstance == null) {
					singleInstance = new TrackServer();
				}
			}
		}
		return singleInstance;
	}

	public boolean startServer() {
		try {
			String temp = (String) this.getPara("track_class_name");
			if (temp != null && !temp.equals("")) {
				this.trackClassName = temp;
			}
			this.trackObject = (ITrack) Class.forName(trackClassName)
					.newInstance();
			this.trackObject.init(this);
		} catch (Exception e) {
			e.printStackTrace();
		}
		this.isRunning = true;
		return true;
	}

	public void stopServer() {
		this.trackObject.destory();
		this.isRunning = false;
	}

	public void addThreadPoolEventListener(ThreadPoolEventListener listener) {
		if(!this.isRunning()){
			return ;
		}
		this.trackObject.addThreadPoolEventListener(listener);
	}

	/**
	 * 添加轨迹信息,该服务将自动把该轨迹写入到轨迹文件中
	 * 
	 * @param info
	 */
	public void addTrackInfo(InfoContainer info) {
		if(!this.isRunning()){
			return ;
		}
		this.trackObject.addTrackInfo(info);
	}

	/**
	 * 查询目标在指定时间段内的轨迹信息 需要提供DEST_NO_FLAG START_DATE_FLAG END_DATE_FLAG三个信息
	 * 
	 * @param queryInfo
	 * @return
	 */
	public InfoContainer[] getTrackInfo(InfoContainer queryInfo) {
		if(!this.isRunning()){
			return null;
		}
		return this.trackObject.getTrackInfo(queryInfo, null);
	}

	public InfoContainer[] getTrackInfo(InfoContainer queryInfo,
			TrackFilter filter) {
		if(!this.isRunning()){
			return null;
		}
		return this.trackObject.getTrackInfo(queryInfo, filter);
	}

	/**
	 * 得到指定日期,指定号牌的目标轨迹文件对象
	 * 
	 * @param destNo
	 * @param destDate
	 * @return
	 */
	public File getTrackFile(String destNo, Date destDate) {
		if(!this.isRunning()){
			return null;
		}
		return this.trackObject.getTrackFile(destNo, destDate);
	}
	public void closeAllStream() {
		if(this.trackObject != null){
			this.trackObject.closeAllStream();
		}
	}

	public void closeStream(String destNo) {
		this.trackObject.closeStream(destNo);
	}
}
