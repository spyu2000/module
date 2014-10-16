package server.track;

import java.io.File;
import java.util.Date;
import com.fleety.base.InfoContainer;
import com.fleety.server.IServer;
import com.fleety.track.TrackFilter;
import com.fleety.util.pool.thread.ThreadPoolEventListener;

public interface ITrack {
	public static final String timerName = "无保障定时器";
	public static final String trackPoolName = "Module_Track_Save_Pool";
	
	public void init(IServer server) throws Exception;
	public void destory();
	
	public void addThreadPoolEventListener(ThreadPoolEventListener listener);
	public void addTrackInfo(InfoContainer info);	
	public InfoContainer[] getTrackInfo(InfoContainer queryInfo);
	public InfoContainer[] getTrackInfo(InfoContainer queryInfo,TrackFilter filter);	
	public File getTrackFile(String destNo,Date destDate);
	public File getZipTrackFile(String destNo,Date destDate);
	
	public void closeAllStream();
	public void closeStream(String destNo);

}
