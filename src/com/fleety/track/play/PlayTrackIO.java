package com.fleety.track.play;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;

import server.threadgroup.ThreadPoolGroupServer;

import com.fleety.base.GeneralConst;
import com.fleety.base.InfoContainer;
import com.fleety.base.Util;
import com.fleety.server.BasicServer;
import com.fleety.track.TrackFilter;
import com.fleety.util.pool.timer.FleetyTimerTask;
import com.fleety.util.pool.timer.TimerPool;

public class PlayTrackIO extends BasicServer{
	private static final int START_FLAG = -1;
	private static final int END_FLAG = 0x79797979;
	private static final int SCALE = 1000000;

	public static final Object QUERY_START_TIME_FLAG_DATE = new Object();
	public static final Object QUERY_END_TIME_FLAG_DATE = new Object();
	public static final Object REAL_FLAG_BOOLEAN = new Object();
	
	public static final Integer PLAY_ID_FLAG_STR = new Integer(0);
	public static final Integer PLAY_DEST_NO_FLAG_STR = new Integer(1);
	public static final Integer PLAY_TIME_FLAG_DATE = new Integer(2);
	public static final Integer PLAY_LO_FLAG_DOUBLE = new Integer(3);
	public static final Integer PLAY_LA_FLAG_DOUBLE = new Integer(4);
	public static final Integer PLAY_RECORD_TIME_FLAG_DATE = new Integer(255);
	
	
	private static final byte VERSION_FLAG = 'V';
	private static final byte VERSION_1 = 1;
	
	private static PlayTrackIO singleInstance = new PlayTrackIO();
	public static PlayTrackIO getSingleInstance(){
		return singleInstance;
	}
	
	private String charset = "utf-8";
	private String savePath = null;
	private TimerPool timerPool = null;
	private String timerPoolName = "play_count_detect_timer_name";
	public boolean startServer(){
		this.timerPool = ThreadPoolGroupServer.getSingleInstance().createTimerPool(timerPoolName,true);
		this.timerPool.schedule(new FleetyTimerTask(){
			public void run(){
				scanOutStream();
			}
		}, 60000, 60000);
		
		
		this.savePath = this.getStringPara("savePath");
		String tempStr = this.getStringPara("charset");
		if(tempStr != null && tempStr.trim().length() > 0){
			this.charset = tempStr.trim();
		}
		
		
		this.isRunning = true;
		return this.isRunning();
	}
	public void stopServer(){
		ThreadPoolGroupServer.getSingleInstance().destroyTimerPool(this.timerPoolName);
		this.timerPool = null;
		super.stopServer();
	}
	
	private HashMap streamMapping = new HashMap();
	private void scanOutStream(){
		Object[] arr = null;
		synchronized(this.streamMapping){
			arr = this.streamMapping.values().toArray();
		}
		
		TrackOutputStream streamObj;
		for(int i=0;i<arr.length;i++){
			streamObj = (TrackOutputStream)arr[i];
			if(streamObj.isUnvalid(300000)){
				this.closeStream(streamObj.getStreamFlag());
			}
		}
	}
	
	private void closeStream(String streamFlag){
		TrackOutputStream streamObj = null;
		synchronized(this.streamMapping){
			streamObj = (TrackOutputStream)this.streamMapping.remove(streamFlag);
		}
		if(streamObj != null){
			streamObj.closeStream();
		}
	}
	
	private TrackOutputStream getStream(String streamFlag,String dateStr) throws Exception{
		TrackOutputStream streamObj = null;
		synchronized(this.streamMapping){
			streamObj = (TrackOutputStream)this.streamMapping.get(streamFlag);
		}
		if(streamObj != null){
			if(!streamObj.isSameDate(dateStr)){
				this.closeStream(streamFlag);
				streamObj = null;
			}
		}
		if(streamObj == null){
			File f = this.getTrackFile(dateStr, streamFlag);
			f.getParentFile().mkdirs();
			OutputStream out = new FileOutputStream(f,true);
			streamObj = new TrackOutputStream(streamFlag,dateStr,out);
			synchronized(this.streamMapping){
				this.streamMapping.put(streamFlag, streamObj);
			}
		}
		return streamObj;
	}
	
	private File getTrackFile(String dateStr,String streamFlag){
		File dir = new File(this.savePath,dateStr);
		return new File(dir,streamFlag+".data");
	}
	
	public boolean addNewPlay(InfoContainer info,boolean isReal){
		if(info == null){
			return false;
		}
		String streamFlag = info.getString(PLAY_ID_FLAG_STR);
		if(streamFlag == null){
			return false;
		}
		
		Date time = (Date)info.getDate(PLAY_TIME_FLAG_DATE);
		String dateStr = GeneralConst.YYYY_MM_DD.format(time);
		ByteBuffer buff = ByteBuffer.allocate(1024);
		buff.putInt(START_FLAG);
		buff.put(VERSION_FLAG);
		buff.put(VERSION_1);
		int position = buff.position();
		buff.putShort((short)0);
		try{
			this.putString((byte)PLAY_DEST_NO_FLAG_STR.intValue(),info.getString(PLAY_DEST_NO_FLAG_STR), buff);
			this.putLong((byte)PLAY_TIME_FLAG_DATE.intValue(), time.getTime(), buff);
			this.putLong((byte)PLAY_RECORD_TIME_FLAG_DATE.intValue(), System.currentTimeMillis(), buff);
			this.putInt((byte)PLAY_LO_FLAG_DOUBLE.intValue(), (int)Math.round(info.getDouble(PLAY_LO_FLAG_DOUBLE).doubleValue()*SCALE), buff);
			this.putInt((byte)PLAY_LA_FLAG_DOUBLE.intValue(), (int)Math.round(info.getDouble(PLAY_LA_FLAG_DOUBLE).doubleValue()*SCALE), buff);
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}
		buff.putShort(position,(short)(buff.position()-position-2));
		
		buff.putInt(END_FLAG);
		buff.flip();
		
		streamFlag = this.getRealName(streamFlag, isReal);
		try{
			TrackOutputStream streamObj = this.getStream(streamFlag, dateStr);
			streamObj.writeData(buff);
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}
		
		return true;
		
	}
	
	public void clearSimulateData(InfoContainer clearInfo){
		Calendar sDate = Calendar.getInstance();
		sDate.setTime(clearInfo.getDate(QUERY_START_TIME_FLAG_DATE));
		sDate.set(Calendar.HOUR_OF_DAY, 0);
		sDate.set(Calendar.MINUTE, 0);
		sDate.set(Calendar.SECOND, 0);
		sDate.set(Calendar.MILLISECOND, 0);
		Calendar eDate = Calendar.getInstance();
		eDate.setTime(clearInfo.getDate(QUERY_END_TIME_FLAG_DATE));
		eDate.set(Calendar.HOUR_OF_DAY, 0);
		eDate.set(Calendar.MINUTE, 0);
		eDate.set(Calendar.SECOND, 0);
		eDate.set(Calendar.MILLISECOND, 0);
		
		String streamFlag = clearInfo.getString(PLAY_ID_FLAG_STR);
		
		String dateStr = null;
		File f;
		while(!sDate.after(eDate)){
			dateStr = GeneralConst.YYYY_MM_DD.format(sDate.getTime());
			
			f = this.getTrackFile(dateStr, this.getRealName(streamFlag, false));
			if(f.exists()){
				f.delete();
			}
			
			sDate.add(Calendar.DAY_OF_MONTH, 1);
		}
	}
	public InfoContainer[] readPlay(InfoContainer queryInfo,TrackFilter filter){
		Calendar sDate = Calendar.getInstance();
		sDate.setTime(queryInfo.getDate(QUERY_START_TIME_FLAG_DATE));
		Calendar countDate = Calendar.getInstance();
		countDate.set(Calendar.HOUR_OF_DAY, 0);
		countDate.set(Calendar.MINUTE, 0);
		countDate.set(Calendar.SECOND, 0);
		countDate.set(Calendar.MILLISECOND, 0);
		
		countDate.setTimeInMillis(sDate.getTimeInMillis());
		Calendar eDate = Calendar.getInstance();
		eDate.setTime(queryInfo.getDate(QUERY_END_TIME_FLAG_DATE));
		
		Boolean isReal = queryInfo.getBoolean(REAL_FLAG_BOOLEAN);
		
		String streamFlag = queryInfo.getString(PLAY_ID_FLAG_STR);
		
		ArrayList list = new ArrayList(1024);
		String dateStr = null;
		while(!countDate.after(eDate)){
			dateStr = GeneralConst.YYYY_MM_DD.format(countDate.getTime());

			if(isReal == null || isReal.booleanValue()){
				if(!this.readDetail(sDate.getTime(), eDate.getTime(), dateStr, streamFlag, true,list,filter)){
					break;
				}
			}
			if(isReal == null || !isReal.booleanValue()){
				if(!this.readDetail(sDate.getTime(), eDate.getTime(), dateStr, streamFlag, false, list,filter)){
					break;
				}
			}
			
			countDate.add(Calendar.DAY_OF_MONTH, 1);
		}
		
		Collections.sort(list,new SortCompararor());
		InfoContainer[] arr = new InfoContainer[list.size()];
		list.toArray(arr);
		return arr;
	}
	private boolean readDetail(Date sDate,Date eDate,String dateStr,String streamFlag,boolean isReal,ArrayList list,TrackFilter filter){
		File f = this.getTrackFile(dateStr, this.getRealName(streamFlag, isReal));
		if(!f.exists()){
			return true;
		}
		
		InputStream in = null;
		try{
			in = new BufferedInputStream(new FileInputStream(f));
			
			ByteBuffer head = ByteBuffer.allocate(8);
			ByteBuffer data = ByteBuffer.allocate(1024);
			int dataLen;
			int version;
			while(true){
				if(!Util.readFull(in, head.array(), 0, head.capacity())){
					break;
				}
				if(head.getInt(0) != START_FLAG){
					System.out.println("Error Data Head!");
					//正确的做法是寻找下一个开始头，待补充
					break;
				}
				
				version = head.get(5)&0xFF;
				if(version != VERSION_1){
					System.out.println("Not Support Data Version!");
					return false;
				}
				dataLen = head.getShort(6);
				if(dataLen < 0){
					System.out.println("Error Data Len! "+dataLen);
					break;
				}
				dataLen += 4;
				if(!Util.readFull(in, data.array(), 0, dataLen)){
					break;
				}
				if(data.getInt(dataLen - 4) != END_FLAG){
					System.out.println("Error Data Tail!");
					//正确的做法是寻找下一个开始头，待补充
					break;
				}
				
				int flag,len;
				data.position(0);
				data.limit(dataLen - 4);
				InfoContainer rInfo = new InfoContainer();
				rInfo.setInfo(PLAY_ID_FLAG_STR, streamFlag);
				rInfo.setInfo(REAL_FLAG_BOOLEAN, isReal?Boolean.TRUE:Boolean.FALSE);
				
				Date rTime = null;
				while(data.hasRemaining()){
					flag = data.get()&0xFF;
					len = data.get()&0xFF;
					if(flag == PLAY_DEST_NO_FLAG_STR.intValue()){
						rInfo.setInfo(PLAY_DEST_NO_FLAG_STR, new String(data.array(),data.position(),len,charset));
					}else if(flag == PLAY_TIME_FLAG_DATE.intValue()){
						rInfo.setInfo(PLAY_TIME_FLAG_DATE, rTime = new Date(data.getLong(data.position())));
					}else if(flag == PLAY_LO_FLAG_DOUBLE.intValue()){
						rInfo.setInfo(PLAY_LO_FLAG_DOUBLE, new Double(data.getInt(data.position())*1.0/SCALE));
					}else if(flag == PLAY_LA_FLAG_DOUBLE.intValue()){
						rInfo.setInfo(PLAY_LA_FLAG_DOUBLE, new Double(data.getInt(data.position())*1.0/SCALE));
					}else if(flag == PLAY_RECORD_TIME_FLAG_DATE.intValue()){
						rInfo.setInfo(PLAY_RECORD_TIME_FLAG_DATE, new Date(data.getLong(data.position())));
					}
					data.position(data.position()+len);
				}
				data.clear();
				
				if(rTime == null){
					continue;
				}
				if(rTime.before(sDate) || rTime.after(eDate)){
					continue;
				}
				
				if(filter != null){
					int rFlag = filter.filterTrack(rInfo);
					if(rFlag == TrackFilter.BREAK_FLAG){
						return false;
					}else if(rFlag == TrackFilter.CONTINUE_FLAG){
						list.add(rInfo);
					}else if(rFlag == TrackFilter.IGNORE_FLAG){
						
					}
				}
			}
			
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			if(in != null){
				try{
					in.close();
				}catch(Exception e){}
			}
		}
		return true;
		
	}


	private void putInt(byte flag,int value,ByteBuffer buff){
		buff.put(flag);
		buff.put((byte)4);
		buff.putInt(value);
	}
	private void putLong(byte flag,long value,ByteBuffer buff){
		buff.put(flag);
		buff.put((byte)8);
		buff.putLong(value);
	}
	
	private void putString(byte flag,String str,ByteBuffer buff) throws Exception{
		if(str == null){
			str = "";
		}
		
		byte[] arr = str.getBytes(charset);
		buff.put(flag);
		buff.put((byte)arr.length);
		buff.put(arr);
	}
	
	private String getRealName(String streamFlag,boolean isReal){
		if(isReal){
			return streamFlag+"-0";
		}else{
			return streamFlag+"-1";
		}
	}
	
	private class TrackOutputStream{
		private String streamFlag = null;
		private OutputStream out = null;
		private String dateStr = null;
		private long updateTime = System.currentTimeMillis();

		public TrackOutputStream(String streamFlag,String dateStr,OutputStream out){
			this.streamFlag = streamFlag;
			this.dateStr = dateStr;
			this.out = out;
		}
		
		public boolean isUnvalid(long limitTime){
			return System.currentTimeMillis() - this.updateTime > limitTime;
		}
		
		public boolean isSameDate(String dateStr){
			return this.dateStr.equals(dateStr);
		}
		
		public synchronized void closeStream(){
			if(this.out != null){
				try{
					this.out.close();
				}catch(Exception e){
					e.printStackTrace();
				}finally{
					this.out = null;
				}
			}
		}
		
		public String getStreamFlag(){
			return this.streamFlag;
		}
		
		public synchronized boolean writeData(ByteBuffer data) throws Exception{
			if(this.out == null){
				return false;
			}
			this.out.write(data.array(),0,data.limit());
			this.out.flush();
			this.updateTime = System.currentTimeMillis();
			return true;
		}
	}
	
	private class SortCompararor implements Comparator{
		public int compare(Object a,Object b){
			Date rTime1 = ((InfoContainer)a).getDate(PLAY_TIME_FLAG_DATE);
			Date rTime2 = ((InfoContainer)b).getDate(PLAY_TIME_FLAG_DATE);
			long offset = rTime1.getTime()-rTime2.getTime();
			if(offset == 0){
				return 0;
			}
			if(offset > 0){
				return 1;
			}
			return -1;
		}
		public boolean equal(Object c){
			return false;
		}
	}
	
	
	public static void main(String[] argv) throws Exception{
		PlayTrackIO.getSingleInstance().addPara("savePath", "./play_track");
		PlayTrackIO.getSingleInstance().startServer();
		
		simulateWrite();
		readData();
	}
	private static void readData()  throws Exception{
		InfoContainer queryInfo = new InfoContainer();
		
		queryInfo.setInfo(PLAY_ID_FLAG_STR, "adv_1");
		queryInfo.setInfo(QUERY_START_TIME_FLAG_DATE, GeneralConst.YYYY_MM_DD.parse("2013-07-22"));
		queryInfo.setInfo(QUERY_END_TIME_FLAG_DATE, GeneralConst.YYYY_MM_DD_HH_MM_SS.parse("2013-07-23 14:00:00"));
		
		InfoContainer[] arr = PlayTrackIO.getSingleInstance().readPlay(queryInfo, new TrackFilter(){
			public int filterTrack(InfoContainer info){
//				System.out.println(info.getString(PLAY_DEST_NO_FLAG_STR)
//							+"\t"+GeneralConst.YYYY_MM_DD_HH_MM_SS.format(info.getDate(PLAY_TIME_FLAG_DATE))
//							+"\t"+info.getString(PLAY_LO_FLAG_DOUBLE)
//							+"\t"+info.getString(PLAY_LA_FLAG_DOUBLE)
//							+"\t"+info.getString(REAL_FLAG_BOOLEAN));
				
				return TrackFilter.CONTINUE_FLAG;
			}
		});
		
		InfoContainer info;
		for(int i=0;i<arr.length;i++){
			info = arr[i];
			
			System.out.println(info.getString(PLAY_DEST_NO_FLAG_STR)
					+"\t"+GeneralConst.YYYY_MM_DD_HH_MM_SS.format(info.getDate(PLAY_TIME_FLAG_DATE))
					+"\t"+info.getString(PLAY_LO_FLAG_DOUBLE)
					+"\t"+info.getString(PLAY_LA_FLAG_DOUBLE)
					+"\t"+info.getString(REAL_FLAG_BOOLEAN));
		}
	}
	private static void simulateWrite() throws Exception{
		InfoContainer info = new InfoContainer();
		info.setInfo(PLAY_ID_FLAG_STR, "adv_1");
		long t  = GeneralConst.YYYY_MM_DD.parse("2013-07-22").getTime();
		for(int i=0;i<1000;i++){
			info.setInfo(PLAY_DEST_NO_FLAG_STR,"沪B"+Math.round(Math.random()*100000));
			info.setInfo(PLAY_TIME_FLAG_DATE,new Date(t + Math.round(Math.random()*58*60*60*1000l)));
			info.setInfo(PLAY_LO_FLAG_DOUBLE,new Double(121+Math.random()));
			info.setInfo(PLAY_LA_FLAG_DOUBLE,new Double(31+Math.random()));
			PlayTrackIO.getSingleInstance().addNewPlay(info, true);
			info.setInfo(PLAY_TIME_FLAG_DATE,new Date(t + Math.round(Math.random()*58*60*60*1000l)));
			PlayTrackIO.getSingleInstance().addNewPlay(info, false);
		}
	}
}
