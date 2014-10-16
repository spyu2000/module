/**
 * 绝密 Created on 2008-1-24 by edmund
 */
package com.fleety.track;

import java.io.*;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.*;
import com.fleety.base.InfoContainer;

public class TrackIO{
	private static final SimpleDateFormat sdf19 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	private static final byte VERSION_BYTE_1 = (byte)'V';
	private static final String VERSION_STRING_1 = "V";
	
	//轨迹存储中的填充字
	private static final int PADDING_BYTE = 0xFF;

	//版本
	public static final String VERSION_1_FLAG = "V1";
	//明文格式版本
	public static final String VERSION_255_FLAG = "V255";
	
	//大平台版本
	public static final String VERSION_ICLOUD_FLAG="VICLOUD";
	//记录的版本
	public static final String RECORD_VERSION_FLAG = "version";
	
	
	public static final Integer DEST_LO_FLAG = new Integer((int)Math.pow(2, 0));
	public static final Integer DEST_LA_FLAG = new Integer((int)Math.pow(2, 1));
	public static final Integer DEST_TIME_FLAG = new Integer((int)Math.pow(2, 2));
	public static final Integer DEST_STATUS_FLAG = new Integer((int)Math.pow(2, 3));
	public static final Integer DEST_SPEED_FLAG = new Integer((int)Math.pow(2, 4));
	public static final Integer DEST_DIRECTION_FLAG = new Integer((int)Math.pow(2, 5));
	public static final Integer DEST_KILO_FLAG = new Integer((int)Math.pow(2, 6));
	public static final Integer DEST_TEMP1_FLAG = new Integer((int)Math.pow(2, 7));
	public static final Integer DEST_TEMP2_FLAG = new Integer((int)Math.pow(2, 8));
	public static final Integer DEST_TEMP3_FLAG = new Integer((int)Math.pow(2, 9));
	public static final Integer DEST_TEMP4_FLAG = new Integer((int)Math.pow(2, 10));
	public static final Integer DEST_TEMP5_FLAG = new Integer((int)Math.pow(2, 11));
	public static final Integer DEST_TEMP6_FLAG = new Integer((int)Math.pow(2, 12));
	public static final Integer DEST_OIL_FLAG = new Integer((int)Math.pow(2, 13));
	//目标定位标识0定位1不定位
	public static final Integer DEST_LOCATE_FLAG = new Integer((int)Math.pow(2, 14));
	//记录时间
	public static final Integer DEST_RECORD_TIME_FLAG = new Integer((int)Math.pow(2, 15));
	//事件ID
	public static final Integer DEST_EVENT_ID_FLAG = new Integer((int)Math.pow(2, 16));
	public static final Integer DEST_ALARM_TYPE_FLAG = new Integer((int)Math.pow(2, 17));
	
	public static final Integer DEST_PEOPLE_NUM_FLAG = new Integer((int)Math.pow(2, 18));   //V1:2个字节
	public static final Integer DEST_DEVICE_WORK_FLAG = new Integer((int)Math.pow(2, 19));  //V1：1个字节，位0为1代表门1开，位1为1代表门2开,位3代表小雨刷状态,位4代表中雨刷状态,位5代表大雨刷状态
	public static final Integer DEST_IN_OUT_NUM_FLAG = new Integer((int)Math.pow(2, 20));   //V1:四个字节，从低端->高端,每个字节分别代码门1上下人，门2上下人
	//V1格式: 1字节通道号 1字节图片格式 1字节图片类型 8字节拍摄时间.共11个字节
	public static final Integer DEST_PHOTO_INFO_FLAG = new Integer((int)Math.pow(2, 21));
	//驾驶员信息
	public static final Integer DRIVER_NO_INFO_FLAG = new Integer((int)Math.pow(2, 22));
	//add by leo 2008-11-12
//	拍照视频的状态
//	拍照通道Bit0:CH1,Bit1:CH2, Bit2:CH3,Bit3:CH4
//	分析视频的状态
//	分析通道Bit4:CH1, Bit5:CH2 
//	该bit=1:表示视频丢失,=0:表示视频正常
	public static final Integer CAMERAL_STATUS_FLAG = new Integer((int)Math.pow(2, 23));//1个字节，摄像头状态

	//设备状态
	public static final Integer EQUIPMENT_STATUS_FLAG = new Integer((int)Math.pow(2, 24));
	//报警状态
	public static final Integer ALARM_STATUS_FLAG = new Integer((int)Math.pow(2, 25));
	//油量2
	public static final Integer DEST_OIL1_FLAG = new Integer((int)Math.pow(2, 26));
	
	//V1格式: 该标志信息需要1字节长度，支持最大中文描述度256字节
	public static final Integer REMARK_FLAG = new Integer((int)Math.pow(2, 27));
	//国标状态位定义
	public static final Integer GB_STATUS_FLAG = new Integer((int)Math.pow(2, 28));
	//轨迹记录的类型，如普通类型、拍照类型、开关门类型等等
	public static final Integer RECORD_TYPE_FLAG = new Integer((int)Math.pow(2, 30));
	//ICLOUD使用，运营商按照协议上传的状态位和报警位的组合
	public static final Integer DEST_ALL_STATUS_FLAG = new Integer((int)Math.pow(2, 31));
	
	public static HashMap INFO_LOCATION_MAPPING = new HashMap();
	public static HashMap LOCATION_INFO_MAPPING = new HashMap();
	static{
		LOCATION_INFO_MAPPING.put(DEST_LO_FLAG, "经度");
		LOCATION_INFO_MAPPING.put(DEST_LA_FLAG, "纬度");
		LOCATION_INFO_MAPPING.put(DEST_TIME_FLAG, "汇报时间");
		LOCATION_INFO_MAPPING.put(DEST_STATUS_FLAG, "状态");
		LOCATION_INFO_MAPPING.put(DEST_SPEED_FLAG, "速度");
		LOCATION_INFO_MAPPING.put(DEST_DIRECTION_FLAG,"方向");
		LOCATION_INFO_MAPPING.put(DEST_KILO_FLAG, "公里数");
		LOCATION_INFO_MAPPING.put(DEST_TEMP1_FLAG, "温度1");
		LOCATION_INFO_MAPPING.put(DEST_TEMP2_FLAG, "温度2");
		LOCATION_INFO_MAPPING.put(DEST_TEMP3_FLAG, "温度3");
		LOCATION_INFO_MAPPING.put(DEST_TEMP4_FLAG, "温度4");
		LOCATION_INFO_MAPPING.put(DEST_TEMP5_FLAG, "温度5");
		LOCATION_INFO_MAPPING.put(DEST_TEMP6_FLAG, "温度6");
		LOCATION_INFO_MAPPING.put(DEST_OIL_FLAG, "油量");
		LOCATION_INFO_MAPPING.put(DEST_OIL1_FLAG, "油量1");
		LOCATION_INFO_MAPPING.put(DEST_LOCATE_FLAG, "定位信息");
		LOCATION_INFO_MAPPING.put(DEST_RECORD_TIME_FLAG, "记录时间");
		LOCATION_INFO_MAPPING.put(DEST_ALARM_TYPE_FLAG, "报警类型");
		LOCATION_INFO_MAPPING.put(DEST_PEOPLE_NUM_FLAG, "车上人数");
		LOCATION_INFO_MAPPING.put(DEST_DEVICE_WORK_FLAG, "设备工作");
		LOCATION_INFO_MAPPING.put(DEST_IN_OUT_NUM_FLAG, "上下人数");
		LOCATION_INFO_MAPPING.put(DEST_PHOTO_INFO_FLAG, "照片信息");
		LOCATION_INFO_MAPPING.put(DRIVER_NO_INFO_FLAG,"驾驶员");
		LOCATION_INFO_MAPPING.put(RECORD_TYPE_FLAG,"记录类型");
		LOCATION_INFO_MAPPING.put(CAMERAL_STATUS_FLAG, "摄像头状态");//add by leo 2008-11-12;
		LOCATION_INFO_MAPPING.put(EQUIPMENT_STATUS_FLAG,"设备状态");
		LOCATION_INFO_MAPPING.put(ALARM_STATUS_FLAG, "报警状态");//add by sunny 2009.11.10
		LOCATION_INFO_MAPPING.put(REMARK_FLAG, "备注信息");
		LOCATION_INFO_MAPPING.put(GB_STATUS_FLAG,"国标状态位");//add by zhuyouzhi 20130311
		
		Iterator iterator = LOCATION_INFO_MAPPING.keySet().iterator();
		Object key;
		while(iterator.hasNext()){
			key = iterator.next();
			INFO_LOCATION_MAPPING.put(LOCATION_INFO_MAPPING.get(key), key);
		}
	}
	
	/**
	 * 测试文件的版本号，如果是旧格式，版本号为null，同时给出记录的长度。如果不能判断出，则返回null
	 * @param f            待检测的文件
	 * @return             null 或 2字节长的字符串数组。如果存在版本号，后续长度无意义。如果不存在版本号，意味着旧格式，后一个字符串给出记录长度
	 * @throws Exception
	 */
	public String[] testVersion(File f) throws Exception{
		String version = null;
		String recordLen= "0";
		InputStream in = new FileInputStream(f);
		try{
			byte[] buff = new byte[2];
			if(in.read(buff) != buff.length){
				return null;
			}
			if(buff[0] != VERSION_BYTE_1){
				in.skip(37);
				int a = in.read(),b = in.read();
				if((buff[0]&0xFF) == a && (buff[1]&0xFF) == b){
					recordLen = "39";
				}else{
					recordLen = "22";
				}
			}else{
				version = VERSION_STRING_1+(buff[1]&0xFF);
			}
		}catch(Exception e){
			throw e;
		}finally{
			in.close();
		}
		
		return new String[]{version,recordLen};
	}
	
	/**
	 * 写轨迹数据
	 * @param out          输出流
	 * @param recordInfos  轨迹记录信息
	 * @param version      版本信息
	 * @param heads        需要写的信息
	 * @throws Exception
	 */
	public void writeTrackRecord(OutputStream out,InfoContainer[] recordInfos,String version,Integer[] heads) throws Exception{
		if(version.equals(VERSION_1_FLAG)){
			this.writeTrackRecordByVersion1(out, recordInfos,heads);
		}else if(version.equals(VERSION_255_FLAG)){
			this.writeTrackRecordByVersion255(out, recordInfos, heads);
		}else if(version.equals(VERSION_ICLOUD_FLAG)){
			this.writeTrackRecordByVersionIClound(out, recordInfos);
		}else{
			throw new Exception("不被支持的轨迹记录格式版本号!");
		}
	}
	
	/**
	 * 写轨迹版本1           版本号：后续长度：meta信息：数据内容
	 * @param out          输出流
	 * @param recordInfos  记录信息
	 * @param heads        需要写入的头信息
	 * @throws Exception
	 */
	private void writeTrackRecordByVersion1(OutputStream out,InfoContainer[] recordInfos,Integer[] heads) throws Exception{		
		int headNum = heads.length;
		int infoMeta = 0;
		
		ByteBuffer buff = ByteBuffer.allocate(512);
		int rNum = recordInfos.length;
		InfoContainer info;
		Object value;
		for(int l = 0;l < rNum;l++){
			info = recordInfos[l];
			buff.clear();
			
			buff.put(new byte[]{VERSION_BYTE_1,0x01});
			buff.putShort((short)0);
			buff.putInt(0);
			
			infoMeta = 0;
			Integer locValue;
			for(int i=0;i<headNum;i++){
				locValue = heads[i];
				if(!LOCATION_INFO_MAPPING.containsKey(locValue)){
					throw new Exception("未定义的信息标识!");
				}
				value = info.getInfo(locValue);
				
				//特殊处理目标记录时间，外部不传递的话，直接使用系统当前时间。
				if(locValue.equals(DEST_RECORD_TIME_FLAG)){
					if(value == null){
						value = new Date();
					}
				}
				
				if(value == null){
					continue;
				}
				infoMeta += locValue.intValue();
				
				if(locValue.equals(DEST_LO_FLAG)){
					buff.putFloat(Float.parseFloat(value.toString()));
				}else if(locValue.equals(DEST_LA_FLAG)){
					buff.putFloat(Float.parseFloat(value.toString()));
				}else if(locValue.equals(DEST_TIME_FLAG)){
					buff.putLong(((Date)value).getTime());
				}else if(locValue.equals(DEST_STATUS_FLAG)){
					buff.put((byte)(Integer.parseInt(value.toString())));
				}else if(locValue.equals(DEST_SPEED_FLAG)){
					buff.putShort((short)(Integer.parseInt(value.toString())));
				}else if(locValue.equals(DEST_DIRECTION_FLAG)){
					buff.putShort((short)(Integer.parseInt(value.toString())));
				}else if(locValue.equals(DEST_KILO_FLAG)){
					buff.putInt((Integer.parseInt(value.toString())));
				}else if(locValue.equals(DEST_TEMP1_FLAG)){
					buff.putShort((short)(Integer.parseInt(value.toString())));
				}else if(locValue.equals(DEST_TEMP2_FLAG)){
					buff.putShort((short)(Integer.parseInt(value.toString())));
				}else if(locValue.equals(DEST_TEMP3_FLAG)){
					buff.putShort((short)(Integer.parseInt(value.toString())));
				}else if(locValue.equals(DEST_TEMP4_FLAG)){
					buff.putShort((short)(Integer.parseInt(value.toString())));
				}else if(locValue.equals(DEST_TEMP5_FLAG)){
					buff.putShort((short)(Integer.parseInt(value.toString())));
				}else if(locValue.equals(DEST_TEMP6_FLAG)){
					buff.putShort((short)(Integer.parseInt(value.toString())));
				}else if(locValue.equals(DEST_OIL_FLAG)){
					buff.putShort((short)(Integer.parseInt(value.toString())));
				}else if(locValue.equals(DEST_OIL1_FLAG)){//add by zhh2010-03-0,记录第二邮箱油量到轨迹
					buff.putShort((short)Integer.parseInt(value.toString()));
				}else if(locValue.equals(DEST_PEOPLE_NUM_FLAG)){
					buff.putShort((short)(Integer.parseInt(value.toString())));
				}else if(locValue.equals(DEST_DEVICE_WORK_FLAG)){
					buff.put((byte)(Integer.parseInt(value.toString())));
				}else if(locValue.equals(DEST_IN_OUT_NUM_FLAG)){
					buff.putInt(Integer.parseInt(value.toString()));
				}else if(locValue.equals(DEST_ALARM_TYPE_FLAG)){
					buff.put((byte)(Integer.parseInt(value.toString())));
				}else if(locValue.equals(DEST_PHOTO_INFO_FLAG)){
					byte[] photoInfoArr = (byte[])value;
					buff.put((byte)photoInfoArr.length);
					buff.put(photoInfoArr);
				}else if(locValue.equals(DRIVER_NO_INFO_FLAG)){
					String driverNo = (String)value;
					byte[] arr = driverNo.getBytes();
					buff.put((byte)arr.length);
					buff.put(arr);
				}else if(locValue.equals(DEST_LOCATE_FLAG)){
					buff.put((byte)(Integer.parseInt(value.toString())));
				}else if(locValue.equals(DEST_RECORD_TIME_FLAG)){
					buff.putLong(((Date)value).getTime());
				}else if(locValue.equals(RECORD_TYPE_FLAG)){
					buff.put(((byte)Integer.parseInt(value.toString())));
				}else if(locValue.equals(CAMERAL_STATUS_FLAG)){//add by leo 2008-11-12
					buff.put((byte)Integer.parseInt(value.toString()));
				}else if(locValue.equals(EQUIPMENT_STATUS_FLAG)){
					buff.putInt(Integer.parseInt(value.toString()));
				}else if(locValue.equals(ALARM_STATUS_FLAG)){
					buff.putInt(Integer.parseInt(value.toString()));
				}else if(locValue.equals(REMARK_FLAG)){				   
				    String remark=(String)value;
					byte[] remarkArr = (byte[])remark.getBytes();
					buff.put((byte)remarkArr.length);
					buff.put(remarkArr);
				}else if(locValue.equals(DEST_EVENT_ID_FLAG)){
					buff.putInt(Integer.parseInt(value.toString()));
				}else if(locValue.equals(DEST_ALL_STATUS_FLAG)){
					buff.putInt(Integer.parseInt(value.toString()));
				}else if(locValue.equals(GB_STATUS_FLAG)){
				    buff.putInt(Integer.parseInt(value.toString()));//添加写入国标状态位定义 add by zhuyouzhi
				}else{
					throw new Exception("不被支持的子项!"+heads[i]);
				}
			}
			
			int totalLen = buff.position();
			//定位到总长位置，写入总长。
			buff.position(2);
			buff.putShort((short)(totalLen-4));
			buff.putInt(infoMeta);
			
			out.write(buff.array(),0,totalLen);
			out.flush();
		}
	}
	
	/**
	 * 写轨迹版本255，也是明文版本
	 * @param out
	 * @param recordInfos
	 * @param heads
	 * @throws Exception
	 */
	private void writeTrackRecordByVersion255(OutputStream out,InfoContainer[] recordInfos,Integer[] heads) throws Exception{
		StringBuffer buff = new StringBuffer(256);
		

		buff.append("版本号");
		int headNum = heads.length;
		for(int i=0;i<headNum;i++){
			buff.append("\t");
			buff.append(LOCATION_INFO_MAPPING.get(heads[i]).toString());
		}
		buff.append("\n");
		out.write(buff.toString().getBytes());
		
		
		int num = recordInfos.length;
		InfoContainer recordInfo;
		Object value;
		for(int i=0;i<num;i++){
			recordInfo = recordInfos[i];
			
			buff.delete(0, buff.length());
			buff.append(VERSION_255_FLAG);
			for(int j=0;j<headNum;j++){
				buff.append("\t");
				value = recordInfo.getInfo(heads[j]);
				
				if(value instanceof Date){
					buff.append(sdf19.format((Date)value));
				}else{
					buff.append(String.valueOf(value));
				}
			}
			buff.append("\n");
			
			out.write(buff.toString().getBytes());
		}
	}
	/**
	 * 写icloud的轨迹文件
	 * @param out
	 * @param recordInfos
	 * @param heads
	 * @throws Exception
	 */
	private void writeTrackRecordByVersionIClound(OutputStream out,InfoContainer[] recordInfos) throws Exception{
		
		if (recordInfos == null) {
			return;
		}
		ByteBuffer buff = ByteBuffer.allocate(39 * recordInfos.length);
		int num = recordInfos.length;
		InfoContainer recordInfo;
		Object temp = null;
		int status,alarmType;
		for (int i = 0; i < num; i++) {
			recordInfo = recordInfos[i];
			temp=recordInfo.getInfo(TrackIO.DEST_LO_FLAG);
			if(temp!=null){
				buff.putFloat(Float.parseFloat(temp.toString()));
			}else{
				buff.putFloat(0);
			}	
			temp=recordInfo.getInfo(TrackIO.DEST_LA_FLAG);
			if(temp!=null){
				buff.putFloat(Float.parseFloat(temp.toString()));
			}else{
				buff.putFloat(0);
			}
			temp=recordInfo.getInfo(TrackIO.DEST_TIME_FLAG);
			if(temp!=null){
				buff.putLong(((Date)temp).getTime());
			}else{
				buff.putLong(0);
			}			
			temp=recordInfo.getInfo(TrackIO.DEST_STATUS_FLAG);
			if(temp!=null){
				status = Integer.parseInt(temp.toString());
			}else{
				status=0;
			}			
			temp=recordInfo.getInfo(TrackIO.DEST_ALARM_TYPE_FLAG);
			if(temp!=null){
				alarmType =Integer.parseInt(temp.toString());
			}else{
				alarmType=0;
			}	
			byte result = (byte) ((status & (byte) 0x07) | (alarmType << 3));
			buff.put(result);
			
			temp=recordInfo.getInfo(TrackIO.DEST_SPEED_FLAG);
			if(temp!=null){
				buff.putShort((short)Integer.parseInt(temp.toString()));
			}else{
				buff.putShort((short)0);
			}		
			temp=recordInfo.getInfo(TrackIO.DEST_DIRECTION_FLAG);
			if(temp!=null){
				buff.putShort((short)Integer.parseInt(temp.toString()));
			}else{
				buff.putShort((short)0);
			}			
			temp=recordInfo.getInfo(TrackIO.DEST_KILO_FLAG);
			if(temp!=null){
				buff.putInt(Integer.parseInt(temp.toString()));
			}else{
				buff.putInt(0);
			}			
			temp = recordInfo.getInfo(TrackIO.DEST_OIL_FLAG);
			if(temp!=null){
				buff.putShort((short)Integer.parseInt(temp.toString()));
			}else{
				buff.putShort((short)0);
			}			
			temp = recordInfo.getInfo(TrackIO.DEST_TEMP1_FLAG);
			if (temp != null) {
				buff.putShort(((Integer) temp).shortValue());
			} else {
				buff.putShort((short) -12800);
			}
			temp = recordInfo.getInfo(TrackIO.DEST_TEMP2_FLAG);
			if (temp != null) {
				buff.putShort(((Integer) temp).shortValue());
			} else {
				buff.putShort((short) -12800);
			}
			temp = recordInfo.getInfo(TrackIO.DEST_ALL_STATUS_FLAG);
			if (temp != null) {
				buff.putInt(((Integer) temp).intValue());
			} else{
				buff.putInt(0);
			}
			
			temp = recordInfo.getInfo(TrackIO.DEST_EVENT_ID_FLAG);
			if (temp != null) {
				buff.putInt(((Integer) temp).intValue());
			} else{
				buff.putInt(0);
			}
			
		}
		out.write(buff.array());
	}
	
	public List readTrackRecord(InputStream in) throws Exception{
		return this.readTrackRecord(in, null);
	}
	
	/**
	 * 读轨迹信息
	 * @param in      轨迹输入流
	 * @return
	 * @throws Exception
	 */
	public List readTrackRecord(InputStream in,TrackFilter filter) throws Exception{
		byte[] versionByte = null;
		
		List recordList = new LinkedList();
		InfoContainer info = null;
		boolean isBreak = false;
		while(!isBreak){
			versionByte = this.getRecordVersion(in);
			if(versionByte == null){
				break;
			}
			
			if(versionByte[0] == VERSION_BYTE_1){
				switch(versionByte[1]&0xFF){
					case 1:
						info = this.readTrackRecordByVersion1(in);
						break;
					case 255:
						throw new Exception("本接口不支持255版本，请走其它接口!");
					default:
						throw new Exception("不被支持的轨迹记录格式版本号!");
				}
			}else{
				info = this.readTrackRecordWithOld(in, versionByte);
			}
			
			if(info == null){
				isBreak = true;
				break;
			}
			
			if(filter != null){
				int returnValue = filter.filterTrack(info);
				switch(returnValue){
					case TrackFilter.CONTINUE_FLAG:
						break;
					case TrackFilter.IGNORE_FLAG:
						info = null;
						break;
					case TrackFilter.BREAK_FLAG:
						info = null;
						isBreak = true;
						break;
					default :
						break;
				}
			}
			if(info != null){
				recordList.add(info);
			}
		}
		
		return recordList;
	}
	
	public void modifyTrackRecord(RandomAccessFile accessFile,ModifyFilter filter) throws Exception{
		if(filter == null){
			return ;
		}
		
		byte[] versionByte = null;
		while(true){
			versionByte = this.getRecordVersion(accessFile);
			if(versionByte == null){
				break;
			}
			
			if(versionByte[0] == VERSION_BYTE_1){
				switch(versionByte[1]&0xFF){
					case 1:
						this.modifyTrackRecordByVersion1(accessFile,filter);
						break;
					case 255:
						throw new Exception("本接口不支持255版本，请走其它接口!");
					default:
						throw new Exception("不被支持的轨迹记录格式版本号!");
				}
			}else{
				throw new Exception("不被支持的轨迹记录格式版本号!");
			}
		}
	}
	
	/**
	 * 设定记录数据长，该参数只对旧格式有效
	 * @param recordLen
	 */
	public void setRecordDataLen(int recordLen){
		this.OLD_RECORD_LENGTH = recordLen;
	}
	private int OLD_RECORD_LENGTH = 39;
	
	//记录39长度的轨迹是iflow的还是icloud的
	private String OLD_RECORD_VERSION="";
	
	public String getOLD_RECORD_VERSION() {
		return OLD_RECORD_VERSION;
	}

	public void setOLD_RECORD_VERSION(String old_record_version) {
		OLD_RECORD_VERSION = old_record_version;
	}
	/**
	 * 读旧格式，目前支持39、21、22和26,37长度的
	 * 21字节为taxiOne的轨迹文件格式，22为itop原先轨迹格式，在21的基础上增加了一个字节定位信息，26是itop新修改的轨迹格式，在22基础上增加了四个字节的公里数
	 * @param in
	 * @param versionByte
	 * @return
	 * @throws Exception
	 */
	private InfoContainer readTrackRecordWithOld(InputStream in,byte[] versionByte) throws Exception{
		InfoContainer info = new InfoContainer();
		ByteBuffer buff = ByteBuffer.allocate(OLD_RECORD_LENGTH);
		int count ;
		if((versionByte[0]&0xFF) == VERSION_BYTE_1){
			count = in.read(buff.array());
		}else{
			buff.put(versionByte[0]);
			buff.put(versionByte[1]);
			count = 2;
			count += in.read(buff.array(),count,OLD_RECORD_LENGTH-2);
		}
		
		if(count != OLD_RECORD_LENGTH){
			return null;
		}
		
		buff.position(0);
		TrackIO.addFloat(buff, info, DEST_LO_FLAG);
		TrackIO.addFloat(buff, info, DEST_LA_FLAG);
		TrackIO.addDate(buff, info, DEST_TIME_FLAG);
		
		int status = buff.get()&0xFF;
        if(this.OLD_RECORD_LENGTH == 26){
            info.setInfo(DEST_STATUS_FLAG, new Integer(status));
            info.setInfo(DEST_ALARM_TYPE_FLAG, new Integer(0));
        }else{
            info.setInfo(DEST_STATUS_FLAG, new Integer(status&0x07));
            info.setInfo(DEST_ALARM_TYPE_FLAG, new Integer((status&0xF8)>>3));
        }	
		
		TrackIO.addUnshort(buff, info, DEST_SPEED_FLAG);
		TrackIO.addUnshort(buff, info, DEST_DIRECTION_FLAG);
		
		if(this.OLD_RECORD_LENGTH == 39){
			if(this.OLD_RECORD_VERSION.equals(TrackIO.VERSION_ICLOUD_FLAG)){
				//大平台轨迹文件中油量在温度之前
				TrackIO.addInt(buff, info, DEST_KILO_FLAG);
				TrackIO.addShort(buff, info, DEST_OIL_FLAG);
				
				TrackIO.addShort(buff, info, DEST_TEMP1_FLAG);
				TrackIO.addShort(buff, info, DEST_TEMP2_FLAG);
				TrackIO.addInt(buff, info, DEST_ALL_STATUS_FLAG);
				TrackIO.addInt(buff, info, DEST_EVENT_ID_FLAG);
			}else{
				//iflow历史轨迹中油量在最后一个位置
				TrackIO.addInt(buff, info, DEST_KILO_FLAG);				
				TrackIO.addShort(buff, info, DEST_TEMP1_FLAG);
				TrackIO.addShort(buff, info, DEST_TEMP2_FLAG);
				TrackIO.addShort(buff, info, DEST_TEMP3_FLAG);
				TrackIO.addShort(buff, info, DEST_TEMP4_FLAG);
				TrackIO.addShort(buff, info, DEST_TEMP5_FLAG);
				TrackIO.addShort(buff, info, DEST_TEMP6_FLAG);				
				TrackIO.addShort(buff, info, DEST_OIL_FLAG);
			}
			
		}else if(this.OLD_RECORD_LENGTH == 22){
			TrackIO.addByte(buff, info, DEST_LOCATE_FLAG);
		}else if(this.OLD_RECORD_LENGTH == 26){
			TrackIO.addByte(buff, info, DEST_LOCATE_FLAG);
			TrackIO.addInt(buff, info, DEST_KILO_FLAG);
		}else if(this.OLD_RECORD_LENGTH == 37){
			TrackIO.addInt(buff, info, DEST_KILO_FLAG);				
			TrackIO.addShort(buff, info, DEST_TEMP1_FLAG);
			TrackIO.addShort(buff, info, DEST_TEMP2_FLAG);
			TrackIO.addShort(buff, info, DEST_TEMP3_FLAG);
			TrackIO.addShort(buff, info, DEST_TEMP4_FLAG);
			TrackIO.addShort(buff, info, DEST_TEMP5_FLAG);
			TrackIO.addShort(buff, info, DEST_TEMP6_FLAG);
		}
		
		return info;
	}
	
	/**
	 * 读版本1格式的数据信息
	 * @param in
	 * @return
	 * @throws Exception
	 */
	private InfoContainer readTrackRecordByVersion1(InputStream in) throws Exception{
	    ByteBuffer headData = ByteBuffer.allocate(6);
		int count = in.read(headData.array());
		
		if(count < 6){
			return null;
		}
		int len = headData.getShort();
		
		Integer[] heads = this.getRecordHead(headData.getInt());
		len -= 4;
		
		ByteBuffer data = ByteBuffer.allocate(len);
		count = in.read(data.array());
		if(count < len){
			return null;
		}
		
		InfoContainer info = new InfoContainer();
		int headNum = heads.length;
		Integer curHead;
		for(int i=0;i<headNum;i++){
			curHead = heads[i];

			if(curHead.equals(DEST_LO_FLAG)){
				TrackIO.addFloat(data, info, curHead);
			}else if(curHead.equals(DEST_LA_FLAG)){
				TrackIO.addFloat(data, info, curHead);
			}else if(curHead.equals(DEST_TIME_FLAG)){
				TrackIO.addDate(data, info, curHead);
			}else if(curHead.equals(DEST_STATUS_FLAG)){
				TrackIO.addByte(data, info, curHead);
			}else if(curHead.equals(DEST_SPEED_FLAG)){
				TrackIO.addUnshort(data, info, curHead);
			}else if(curHead.equals(DEST_DIRECTION_FLAG)){
				TrackIO.addUnshort(data, info, curHead);
			}else if(curHead.equals(DEST_KILO_FLAG)){
				TrackIO.addInt(data, info, curHead);
			}else if(curHead.equals(DEST_TEMP1_FLAG)){
				TrackIO.addShort(data, info, curHead);
			}else if(curHead.equals(DEST_TEMP2_FLAG)){
				TrackIO.addShort(data, info, curHead);
			}else if(curHead.equals(DEST_TEMP3_FLAG)){
				TrackIO.addShort(data, info, curHead);
			}else if(curHead.equals(DEST_TEMP4_FLAG)){
				TrackIO.addShort(data, info, curHead);
			}else if(curHead.equals(DEST_TEMP5_FLAG)){
				TrackIO.addShort(data, info, curHead);
			}else if(curHead.equals(DEST_TEMP6_FLAG)){
				TrackIO.addShort(data, info, curHead);
			}else if(curHead.equals(DEST_OIL_FLAG)){
				TrackIO.addShort(data, info, curHead);
			}else if(curHead.equals(DEST_OIL1_FLAG)){
				TrackIO.addShort(data, info, curHead);
			}else if(curHead.equals(DEST_PEOPLE_NUM_FLAG)){
				TrackIO.addUnshort(data, info, curHead);
			}else if(curHead.equals(DEST_DEVICE_WORK_FLAG)){
				TrackIO.addByte(data, info, curHead);
			}else if(curHead.equals(DEST_IN_OUT_NUM_FLAG)){
				TrackIO.addInt(data, info, curHead);
			}else if(curHead.equals(DEST_ALARM_TYPE_FLAG)){
				TrackIO.addByte(data, info, curHead);
			}else if(curHead.equals(DEST_PHOTO_INFO_FLAG)){
				TrackIO.addByteArr(data,data.get()&0xFF,info,curHead);
			}else if(curHead.equals(DRIVER_NO_INFO_FLAG)){
				TrackIO.addString(data,data.get()&0xFF,info,curHead);
			}else if(curHead.equals(DEST_LOCATE_FLAG)){
				TrackIO.addByte(data, info, curHead);
			}else if(curHead.equals(DEST_RECORD_TIME_FLAG)){
				TrackIO.addDate(data, info, curHead);
			}else if(curHead.equals(RECORD_TYPE_FLAG)){
				TrackIO.addByte(data, info, curHead);
			}else if(curHead.equals(CAMERAL_STATUS_FLAG)){//ad by leo 2008-11-12
				TrackIO.addByte(data, info, curHead);
			}else if(curHead.equals(EQUIPMENT_STATUS_FLAG)){
				TrackIO.addInt(data, info, curHead);
			}else if(curHead.equals(ALARM_STATUS_FLAG)){
				TrackIO.addInt(data, info, curHead);
			}else if(curHead.equals(REMARK_FLAG)){
				TrackIO.addString(data,data.get()&0xFF,info,curHead);
			}else if(curHead.equals(DEST_EVENT_ID_FLAG)){
				TrackIO.addInt(data, info, curHead);
			}else if(curHead.equals(DEST_ALL_STATUS_FLAG)){
				TrackIO.addInt(data, info, curHead);
			}else if(curHead.equals(GB_STATUS_FLAG)){
			    TrackIO.addInt(data, info, curHead);
			}else{
				throw new Exception("不被支持的子项!"+heads[i]);
			}
		}
		info.setInfo(RECORD_VERSION_FLAG, VERSION_1_FLAG);
		
		return info;
	}
	
	private boolean modifyTrackRecordByVersion1(RandomAccessFile accessFile,ModifyFilter filter) throws Exception{
		ByteBuffer headData = ByteBuffer.allocate(6);
		int count = accessFile.read(headData.array());
		
		if(count < 6){
			return false;
		}
		int len = headData.getShort();
		
		Integer[] heads = this.getRecordHead(headData.getInt());
		len -= 4;
		
		int headNum = heads.length;
		Integer curHead;
		long pos ;
		for(int i=0;i<headNum;i++){
			curHead = heads[i];

			pos = accessFile.getFilePointer();
			
			filter.filter(accessFile, curHead.intValue());
			
			if(curHead.equals(DEST_LO_FLAG)){
				pos += 4;
			}else if(curHead.equals(DEST_LA_FLAG)){
				pos += 4;
			}else if(curHead.equals(DEST_TIME_FLAG)){
				pos += 8;
			}else if(curHead.equals(DEST_STATUS_FLAG)){
				pos += 1;
			}else if(curHead.equals(DEST_SPEED_FLAG)){
				pos += 2;
			}else if(curHead.equals(DEST_DIRECTION_FLAG)){
				pos += 2;
			}else if(curHead.equals(DEST_KILO_FLAG)){
				pos += 4;
			}else if(curHead.equals(DEST_TEMP1_FLAG)
					||curHead.equals(DEST_TEMP2_FLAG)
					||curHead.equals(DEST_TEMP3_FLAG)
					||curHead.equals(DEST_TEMP4_FLAG)
					||curHead.equals(DEST_TEMP5_FLAG)
					||curHead.equals(DEST_TEMP6_FLAG)
				){
				pos += 2;
			}else if(curHead.equals(DEST_OIL_FLAG)||curHead.equals(DEST_OIL1_FLAG)){
				pos += 2;
			}else if(curHead.equals(DEST_PEOPLE_NUM_FLAG)){
				pos += 2;
			}else if(curHead.equals(DEST_DEVICE_WORK_FLAG)){
				pos += 1;
			}else if(curHead.equals(DEST_IN_OUT_NUM_FLAG)){
				pos += 4;
			}else if(curHead.equals(DEST_ALARM_TYPE_FLAG)){
				pos += 1;
			}else if(curHead.equals(DEST_PHOTO_INFO_FLAG)){
				accessFile.seek(pos);
				pos += accessFile.read();
			}else if(curHead.equals(DRIVER_NO_INFO_FLAG)){
				accessFile.seek(pos);
				pos += accessFile.read();
			}else if(curHead.equals(DEST_LOCATE_FLAG)){
				pos += 1;
			}else if(curHead.equals(DEST_RECORD_TIME_FLAG)){
				pos += 8;
			}else if(curHead.equals(RECORD_TYPE_FLAG)){
				pos += 1;
			}else if(curHead.equals(CAMERAL_STATUS_FLAG)){//ad by leo 2008-11-12
				pos += 1;
			}else if(curHead.equals(EQUIPMENT_STATUS_FLAG)){
				pos += 4;
			}else if(curHead.equals(ALARM_STATUS_FLAG)){
				pos += 4;
			}else{
				throw new Exception("不被支持的子项!"+heads[i]);
			}
			accessFile.seek(pos);
		}
		
		return true;
	}
	
	private final Integer[] getRecordHead(int allFlag) throws Exception{
		List headList = new LinkedList();
		int flag = 1;
		Object flagObj;
		while(allFlag != 0){
			if((allFlag %2) == 1){
				flagObj = new Integer(flag);
				if(flagObj == null){
					throw new Exception("不被支持的子项标识!");
				}
				headList.add(flagObj);
			}
			flag *= 2;
			allFlag = allFlag/2;
		}
		
		Integer[] heads = new Integer[headList.size()];
		headList.toArray(heads);
		
		return heads;
	}
	
	/**
	 * 读版本255的格式数据
	 * @param in
	 * @return
	 * @throws Exception
	 */
	public InfoContainer[] readTrackRecordByVersion255(InputStream in) throws Exception{
		return null;
	}
	
	/**
	 * 获取当前记录的版本号
	 * @param in
	 * @return
	 * @throws Exception
	 */
	private final byte[] getRecordVersion(InputStream in) throws Exception{
		int a1;
		do{
			a1 = in.read();
			if(a1 < 0){
				return null;
			}
		}while(a1 == PADDING_BYTE);
		
		int a2 = in.read();
		if(a2 < 0){
			return null;
		}
		
		return new byte[]{(byte)a1,(byte)a2};
	}
	private final byte[] getRecordVersion(RandomAccessFile in) throws Exception{
		int a1 = in.read();
		if(a1 < 0){
			return null;
		}
		int a2 = in.read();
		if(a2 < 0){
			return null;
		}
		
		return new byte[]{(byte)a1,(byte)a2};
	}
	
	private static final void addFloat(ByteBuffer buff,InfoContainer info,Object key){
		float f = buff.getFloat();
		info.setInfo(key, new Float(f));
	}
	
	private static final void addByte(ByteBuffer buff,InfoContainer info,Object key){
		int i = buff.get()&0xFF;
		info.setInfo(key, new Integer(i));
	}
	
	private static final void addShort(ByteBuffer buff,InfoContainer info,Object key){
		int i = buff.getShort();
		info.setInfo(key, new Integer(i));
	}
	
	private static final void addUnshort(ByteBuffer buff,InfoContainer info,Object key){
		int i = buff.getShort()&0xFFFF;
		info.setInfo(key, new Integer(i));
	}
	
	private static final void addInt(ByteBuffer buff,InfoContainer info,Object key){
		int i = buff.getInt();
		info.setInfo(key, new Integer(i));
	}
	
	private static final void addByteArr(ByteBuffer buff,int arrLen,InfoContainer info,Object key){
		byte[] byteArr = new byte[arrLen];
		buff.get(byteArr);
		info.setInfo(key, byteArr);
	}
	
	private static final void addString(ByteBuffer buff,int infoLen,InfoContainer info,Object key){
		byte[] byteArr = new byte[infoLen];
		buff.get(byteArr);
		info.setInfo(key, new String(byteArr));
	}
	
	private static final void addDate(ByteBuffer buff,InfoContainer info,Object key){
		long l = buff.getLong();
		info.setInfo(key, new Date(l));
	}


}