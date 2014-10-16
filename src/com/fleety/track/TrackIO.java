/**
 * ���� Created on 2008-1-24 by edmund
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
	
	//�켣�洢�е������
	private static final int PADDING_BYTE = 0xFF;

	//�汾
	public static final String VERSION_1_FLAG = "V1";
	//���ĸ�ʽ�汾
	public static final String VERSION_255_FLAG = "V255";
	
	//��ƽ̨�汾
	public static final String VERSION_ICLOUD_FLAG="VICLOUD";
	//��¼�İ汾
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
	//Ŀ�궨λ��ʶ0��λ1����λ
	public static final Integer DEST_LOCATE_FLAG = new Integer((int)Math.pow(2, 14));
	//��¼ʱ��
	public static final Integer DEST_RECORD_TIME_FLAG = new Integer((int)Math.pow(2, 15));
	//�¼�ID
	public static final Integer DEST_EVENT_ID_FLAG = new Integer((int)Math.pow(2, 16));
	public static final Integer DEST_ALARM_TYPE_FLAG = new Integer((int)Math.pow(2, 17));
	
	public static final Integer DEST_PEOPLE_NUM_FLAG = new Integer((int)Math.pow(2, 18));   //V1:2���ֽ�
	public static final Integer DEST_DEVICE_WORK_FLAG = new Integer((int)Math.pow(2, 19));  //V1��1���ֽڣ�λ0Ϊ1������1����λ1Ϊ1������2��,λ3����С��ˢ״̬,λ4��������ˢ״̬,λ5�������ˢ״̬
	public static final Integer DEST_IN_OUT_NUM_FLAG = new Integer((int)Math.pow(2, 20));   //V1:�ĸ��ֽڣ��ӵͶ�->�߶�,ÿ���ֽڷֱ������1�����ˣ���2������
	//V1��ʽ: 1�ֽ�ͨ���� 1�ֽ�ͼƬ��ʽ 1�ֽ�ͼƬ���� 8�ֽ�����ʱ��.��11���ֽ�
	public static final Integer DEST_PHOTO_INFO_FLAG = new Integer((int)Math.pow(2, 21));
	//��ʻԱ��Ϣ
	public static final Integer DRIVER_NO_INFO_FLAG = new Integer((int)Math.pow(2, 22));
	//add by leo 2008-11-12
//	������Ƶ��״̬
//	����ͨ��Bit0:CH1,Bit1:CH2, Bit2:CH3,Bit3:CH4
//	������Ƶ��״̬
//	����ͨ��Bit4:CH1, Bit5:CH2 
//	��bit=1:��ʾ��Ƶ��ʧ,=0:��ʾ��Ƶ����
	public static final Integer CAMERAL_STATUS_FLAG = new Integer((int)Math.pow(2, 23));//1���ֽڣ�����ͷ״̬

	//�豸״̬
	public static final Integer EQUIPMENT_STATUS_FLAG = new Integer((int)Math.pow(2, 24));
	//����״̬
	public static final Integer ALARM_STATUS_FLAG = new Integer((int)Math.pow(2, 25));
	//����2
	public static final Integer DEST_OIL1_FLAG = new Integer((int)Math.pow(2, 26));
	
	//V1��ʽ: �ñ�־��Ϣ��Ҫ1�ֽڳ��ȣ�֧���������������256�ֽ�
	public static final Integer REMARK_FLAG = new Integer((int)Math.pow(2, 27));
	//����״̬λ����
	public static final Integer GB_STATUS_FLAG = new Integer((int)Math.pow(2, 28));
	//�켣��¼�����ͣ�����ͨ���͡��������͡����������͵ȵ�
	public static final Integer RECORD_TYPE_FLAG = new Integer((int)Math.pow(2, 30));
	//ICLOUDʹ�ã���Ӫ�̰���Э���ϴ���״̬λ�ͱ���λ�����
	public static final Integer DEST_ALL_STATUS_FLAG = new Integer((int)Math.pow(2, 31));
	
	public static HashMap INFO_LOCATION_MAPPING = new HashMap();
	public static HashMap LOCATION_INFO_MAPPING = new HashMap();
	static{
		LOCATION_INFO_MAPPING.put(DEST_LO_FLAG, "����");
		LOCATION_INFO_MAPPING.put(DEST_LA_FLAG, "γ��");
		LOCATION_INFO_MAPPING.put(DEST_TIME_FLAG, "�㱨ʱ��");
		LOCATION_INFO_MAPPING.put(DEST_STATUS_FLAG, "״̬");
		LOCATION_INFO_MAPPING.put(DEST_SPEED_FLAG, "�ٶ�");
		LOCATION_INFO_MAPPING.put(DEST_DIRECTION_FLAG,"����");
		LOCATION_INFO_MAPPING.put(DEST_KILO_FLAG, "������");
		LOCATION_INFO_MAPPING.put(DEST_TEMP1_FLAG, "�¶�1");
		LOCATION_INFO_MAPPING.put(DEST_TEMP2_FLAG, "�¶�2");
		LOCATION_INFO_MAPPING.put(DEST_TEMP3_FLAG, "�¶�3");
		LOCATION_INFO_MAPPING.put(DEST_TEMP4_FLAG, "�¶�4");
		LOCATION_INFO_MAPPING.put(DEST_TEMP5_FLAG, "�¶�5");
		LOCATION_INFO_MAPPING.put(DEST_TEMP6_FLAG, "�¶�6");
		LOCATION_INFO_MAPPING.put(DEST_OIL_FLAG, "����");
		LOCATION_INFO_MAPPING.put(DEST_OIL1_FLAG, "����1");
		LOCATION_INFO_MAPPING.put(DEST_LOCATE_FLAG, "��λ��Ϣ");
		LOCATION_INFO_MAPPING.put(DEST_RECORD_TIME_FLAG, "��¼ʱ��");
		LOCATION_INFO_MAPPING.put(DEST_ALARM_TYPE_FLAG, "��������");
		LOCATION_INFO_MAPPING.put(DEST_PEOPLE_NUM_FLAG, "��������");
		LOCATION_INFO_MAPPING.put(DEST_DEVICE_WORK_FLAG, "�豸����");
		LOCATION_INFO_MAPPING.put(DEST_IN_OUT_NUM_FLAG, "��������");
		LOCATION_INFO_MAPPING.put(DEST_PHOTO_INFO_FLAG, "��Ƭ��Ϣ");
		LOCATION_INFO_MAPPING.put(DRIVER_NO_INFO_FLAG,"��ʻԱ");
		LOCATION_INFO_MAPPING.put(RECORD_TYPE_FLAG,"��¼����");
		LOCATION_INFO_MAPPING.put(CAMERAL_STATUS_FLAG, "����ͷ״̬");//add by leo 2008-11-12;
		LOCATION_INFO_MAPPING.put(EQUIPMENT_STATUS_FLAG,"�豸״̬");
		LOCATION_INFO_MAPPING.put(ALARM_STATUS_FLAG, "����״̬");//add by sunny 2009.11.10
		LOCATION_INFO_MAPPING.put(REMARK_FLAG, "��ע��Ϣ");
		LOCATION_INFO_MAPPING.put(GB_STATUS_FLAG,"����״̬λ");//add by zhuyouzhi 20130311
		
		Iterator iterator = LOCATION_INFO_MAPPING.keySet().iterator();
		Object key;
		while(iterator.hasNext()){
			key = iterator.next();
			INFO_LOCATION_MAPPING.put(LOCATION_INFO_MAPPING.get(key), key);
		}
	}
	
	/**
	 * �����ļ��İ汾�ţ�����Ǿɸ�ʽ���汾��Ϊnull��ͬʱ������¼�ĳ��ȡ���������жϳ����򷵻�null
	 * @param f            �������ļ�
	 * @return             null �� 2�ֽڳ����ַ������顣������ڰ汾�ţ��������������塣��������ڰ汾�ţ���ζ�žɸ�ʽ����һ���ַ���������¼����
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
	 * д�켣����
	 * @param out          �����
	 * @param recordInfos  �켣��¼��Ϣ
	 * @param version      �汾��Ϣ
	 * @param heads        ��Ҫд����Ϣ
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
			throw new Exception("����֧�ֵĹ켣��¼��ʽ�汾��!");
		}
	}
	
	/**
	 * д�켣�汾1           �汾�ţ��������ȣ�meta��Ϣ����������
	 * @param out          �����
	 * @param recordInfos  ��¼��Ϣ
	 * @param heads        ��Ҫд���ͷ��Ϣ
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
					throw new Exception("δ�������Ϣ��ʶ!");
				}
				value = info.getInfo(locValue);
				
				//���⴦��Ŀ���¼ʱ�䣬�ⲿ�����ݵĻ���ֱ��ʹ��ϵͳ��ǰʱ�䡣
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
				}else if(locValue.equals(DEST_OIL1_FLAG)){//add by zhh2010-03-0,��¼�ڶ������������켣
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
				    buff.putInt(Integer.parseInt(value.toString()));//���д�����״̬λ���� add by zhuyouzhi
				}else{
					throw new Exception("����֧�ֵ�����!"+heads[i]);
				}
			}
			
			int totalLen = buff.position();
			//��λ���ܳ�λ�ã�д���ܳ���
			buff.position(2);
			buff.putShort((short)(totalLen-4));
			buff.putInt(infoMeta);
			
			out.write(buff.array(),0,totalLen);
			out.flush();
		}
	}
	
	/**
	 * д�켣�汾255��Ҳ�����İ汾
	 * @param out
	 * @param recordInfos
	 * @param heads
	 * @throws Exception
	 */
	private void writeTrackRecordByVersion255(OutputStream out,InfoContainer[] recordInfos,Integer[] heads) throws Exception{
		StringBuffer buff = new StringBuffer(256);
		

		buff.append("�汾��");
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
	 * дicloud�Ĺ켣�ļ�
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
	 * ���켣��Ϣ
	 * @param in      �켣������
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
						throw new Exception("���ӿڲ�֧��255�汾�����������ӿ�!");
					default:
						throw new Exception("����֧�ֵĹ켣��¼��ʽ�汾��!");
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
						throw new Exception("���ӿڲ�֧��255�汾�����������ӿ�!");
					default:
						throw new Exception("����֧�ֵĹ켣��¼��ʽ�汾��!");
				}
			}else{
				throw new Exception("����֧�ֵĹ켣��¼��ʽ�汾��!");
			}
		}
	}
	
	/**
	 * �趨��¼���ݳ����ò���ֻ�Ծɸ�ʽ��Ч
	 * @param recordLen
	 */
	public void setRecordDataLen(int recordLen){
		this.OLD_RECORD_LENGTH = recordLen;
	}
	private int OLD_RECORD_LENGTH = 39;
	
	//��¼39���ȵĹ켣��iflow�Ļ���icloud��
	private String OLD_RECORD_VERSION="";
	
	public String getOLD_RECORD_VERSION() {
		return OLD_RECORD_VERSION;
	}

	public void setOLD_RECORD_VERSION(String old_record_version) {
		OLD_RECORD_VERSION = old_record_version;
	}
	/**
	 * ���ɸ�ʽ��Ŀǰ֧��39��21��22��26,37���ȵ�
	 * 21�ֽ�ΪtaxiOne�Ĺ켣�ļ���ʽ��22Ϊitopԭ�ȹ켣��ʽ����21�Ļ�����������һ���ֽڶ�λ��Ϣ��26��itop���޸ĵĹ켣��ʽ����22�������������ĸ��ֽڵĹ�����
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
				//��ƽ̨�켣�ļ����������¶�֮ǰ
				TrackIO.addInt(buff, info, DEST_KILO_FLAG);
				TrackIO.addShort(buff, info, DEST_OIL_FLAG);
				
				TrackIO.addShort(buff, info, DEST_TEMP1_FLAG);
				TrackIO.addShort(buff, info, DEST_TEMP2_FLAG);
				TrackIO.addInt(buff, info, DEST_ALL_STATUS_FLAG);
				TrackIO.addInt(buff, info, DEST_EVENT_ID_FLAG);
			}else{
				//iflow��ʷ�켣�����������һ��λ��
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
	 * ���汾1��ʽ��������Ϣ
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
				throw new Exception("����֧�ֵ�����!"+heads[i]);
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
				throw new Exception("����֧�ֵ�����!"+heads[i]);
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
					throw new Exception("����֧�ֵ������ʶ!");
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
	 * ���汾255�ĸ�ʽ����
	 * @param in
	 * @return
	 * @throws Exception
	 */
	public InfoContainer[] readTrackRecordByVersion255(InputStream in) throws Exception{
		return null;
	}
	
	/**
	 * ��ȡ��ǰ��¼�İ汾��
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