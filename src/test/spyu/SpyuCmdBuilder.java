/**
 * 绝密 Created on 2009-10-14 by spyu
 */
package test.spyu;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Calendar;

public class SpyuCmdBuilder {

	private static int outerLen = 13;

	/**
	 * 心跳
	 */
	public static byte[] getHeartMsg() {

		int msgLen = 0, bodyLen = 0;
		msgLen = 20;
		bodyLen = msgLen - outerLen;
		Calendar calendar = Calendar.getInstance();

		ByteBuffer msg = ByteBuffer.allocate(msgLen);
		msg.order(ByteOrder.LITTLE_ENDIAN);
		msg.putInt(SpyuMsgDefinition.COMPLETE_SAFE_FLAG);
		msg.put((byte) SpyuMsgDefinition.HEART_FLAG);
		msg.putInt(bodyLen);
		Util.setTimeStruct(calendar, msg);
		msg.putInt(SpyuMsgDefinition.COMPLETE_SAFE_FLAG);
		msg.flip();
		return msg.array();
	}

	/**
	 * 确认消息
	 * 
	 * @param msgId
	 * @param seqId
	 * @return
	 */
	public static byte[] getConfirmMsg(int msgId, int seq) {

		int msgLen = 0, bodyLen = 0;
		msgLen = 15;
		bodyLen = msgLen - outerLen;

		ByteBuffer msg = ByteBuffer.allocate(msgLen);
		msg.order(ByteOrder.LITTLE_ENDIAN);
		msg.putInt(SpyuMsgDefinition.COMPLETE_SAFE_FLAG);
		msg.put((byte) SpyuMsgDefinition.CONFIRM_FLAG);
		msg.putInt(bodyLen);
		msg.put((byte) msgId);
		msg.put((byte) seq);
		msg.putInt(SpyuMsgDefinition.COMPLETE_SAFE_FLAG);
		msg.flip();
		return msg.array();
	}

	/**
	 * 发送车辆基本信息
	 * 
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public static byte[] getCarInfo(int seq, int mdtId, String carNo,
			String companyName, String phone, String simCard, String mdtType)
			throws UnsupportedEncodingException {
		int msgLen = 0, bodyLen = 0, carNoLen = 0, companyNameLen = 0, phoneLen = 0, simCardLen = 0, mdtTypeLen = 0;
		byte[] carNoByte, companyNameByte, phoneByte, simCardByte, mdtTypeByte;
		if (carNo == null) {
			return null;
		}
		carNoByte = carNo.getBytes("utf-8");
		carNoLen = carNoByte.length;

		companyNameByte = companyName.getBytes("utf-8");
		companyNameLen = companyNameByte.length;

		phoneByte = phone.getBytes("utf-8");
		phoneLen = phoneByte.length;

		simCardByte = simCard.getBytes("utf-8");
		simCardLen = simCardByte.length;

		mdtTypeByte = mdtType.getBytes("utf-8");
		mdtTypeLen = mdtTypeByte.length;

		msgLen = 23 + carNoLen + companyNameLen + phoneLen + simCardLen
				+ mdtTypeLen;
		bodyLen = msgLen - outerLen;

		ByteBuffer msg = ByteBuffer.allocate(msgLen);
		msg.order(ByteOrder.LITTLE_ENDIAN);
		msg.putInt(SpyuMsgDefinition.COMPLETE_SAFE_FLAG);
		msg.put((byte) SpyuMsgDefinition.IN_CAR_BASE_INFO_FLAG);
		msg.putInt(bodyLen);
		msg.put((byte) seq);

		msg.putInt(mdtId);
		msg.put((byte) carNoLen);
		msg.put(carNoByte);

		msg.put((byte) companyNameLen);
		msg.put(companyNameByte);

		msg.put((byte) phoneLen);
		msg.put(phoneByte);

		msg.put((byte) simCardLen);
		msg.put(simCardByte);

		msg.put((byte) mdtTypeLen);
		msg.put(mdtTypeByte);

		msg.putInt(SpyuMsgDefinition.COMPLETE_SAFE_FLAG);
		msg.flip();
		return msg.array();
	}

	/**
	 * 得到车辆的位置汇报
	 * 
	 * @param carNo
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public static byte[] getCarLocation(int seq, String carNo)
			throws UnsupportedEncodingException {
		int msgLen = 0, bodyLen = 0, carNoLen = 0;

		if (carNo == null) {
			return null;
		}
		byte[] carNoByte = carNo.getBytes("utf-8");
		carNoLen = carNoByte.length;

		msgLen = 15 + carNoLen;
		bodyLen = msgLen - outerLen;

		ByteBuffer msg = ByteBuffer.allocate(msgLen);
		msg.order(ByteOrder.LITTLE_ENDIAN);
		msg.putInt(SpyuMsgDefinition.COMPLETE_SAFE_FLAG);
		msg.put((byte) SpyuMsgDefinition.IN_CAR_LOCATION_INFO_FLAG);
		msg.putInt(bodyLen);
		msg.put((byte) seq);
		msg.put((byte) carNoLen);
		msg.put(carNoByte);
		msg.putInt(SpyuMsgDefinition.COMPLETE_SAFE_FLAG);
		msg.flip();
		return msg.array();
	}

	/**
	 * 查询站点信息
	 * 
	 * @param seq
	 * @param queryInfo
	 * @return
	 */
	public static byte[] getStationInfo(int seq, int queryInfo) {
		int msgLen = 0, bodyLen = 0;

		msgLen = 18;
		bodyLen = msgLen - outerLen;
		ByteBuffer msg = ByteBuffer.allocate(msgLen);
		msg.order(ByteOrder.LITTLE_ENDIAN);
		msg.putInt(SpyuMsgDefinition.COMPLETE_SAFE_FLAG);
		msg.put((byte) SpyuMsgDefinition.IN_STATION_INFO_FLAG);
		msg.putInt(bodyLen);
		msg.put((byte) seq);
		msg.putInt(queryInfo);
		msg.putInt(SpyuMsgDefinition.COMPLETE_SAFE_FLAG);
		msg.flip();
		return msg.array();
	}

	/**
	 * 得到站点的空车
	 * 
	 * @param seq
	 * @param stationId
	 * @return
	 */
	public static byte[] getStationFreeCar(int seq, int stationId) {
		int msgLen = 0, bodyLen = 0;

		msgLen = 18;
		bodyLen = msgLen - outerLen;
		ByteBuffer msg = ByteBuffer.allocate(msgLen);
		msg.order(ByteOrder.LITTLE_ENDIAN);
		msg.putInt(SpyuMsgDefinition.COMPLETE_SAFE_FLAG);
		msg.put((byte) SpyuMsgDefinition.IN_FREE_CAR_FLAG);
		msg.putInt(bodyLen);
		msg.put((byte) seq);
		msg.putInt(stationId);
		msg.putInt(SpyuMsgDefinition.COMPLETE_SAFE_FLAG);
		msg.flip();
		return msg.array();

	}
	
	/**
	 * 车辆状态改变消息
	 * @param seq
	 * @param carNo
	 * @param status
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public static byte[] getCarStatusChange(int seq, String carNo,int status) throws UnsupportedEncodingException {
		int msgLen = 0, bodyLen = 0,carNoLen=0;
		if (carNo == null) {
			return null;
		}
		byte[] carNoByte = carNo.getBytes("utf-8");
		carNoLen = carNoByte.length;
		msgLen = 16+carNoLen;
		bodyLen = msgLen - outerLen;
		ByteBuffer msg = ByteBuffer.allocate(msgLen);
		msg.order(ByteOrder.LITTLE_ENDIAN);
		msg.putInt(SpyuMsgDefinition.COMPLETE_SAFE_FLAG);
		msg.put((byte) SpyuMsgDefinition.IN_CAR_STATUS_FLAG);
		msg.putInt(bodyLen);
		msg.put((byte) seq);
		msg.put((byte) carNoLen);
		msg.put(carNoByte);
		msg.put((byte) status);
		msg.putInt(SpyuMsgDefinition.COMPLETE_SAFE_FLAG);
		msg.flip();
		return msg.array();
	}
	
	/**
	 * 发送任务消息
	 * @param seq
	 * @param carNo
	 * @param taskName
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public static byte[] getTaskInfo(int seq, String carNo,String taskName,int taskId) throws UnsupportedEncodingException {
		int msgLen = 0, bodyLen = 0,carNoLen=0,taskNameLen=0;
		if (carNo == null) {
			return null;
		}
		byte[] carNoByte = carNo.getBytes("utf-8");
		carNoLen = carNoByte.length;
		byte[] taskNameByte = taskName.getBytes("utf-8");
		taskNameLen = taskNameByte.length;
		
		msgLen = 89+carNoLen+taskNameLen;
		bodyLen = msgLen - outerLen;
		ByteBuffer msg = ByteBuffer.allocate(msgLen);
		msg.order(ByteOrder.LITTLE_ENDIAN);
		msg.putInt(SpyuMsgDefinition.COMPLETE_SAFE_FLAG);
		msg.put((byte) SpyuMsgDefinition.IN_TASK_FLAG);
		msg.putInt(bodyLen);
		msg.put((byte) seq);
		msg.put((byte) carNoLen);
		msg.put(carNoByte);
		msg.putInt(taskId);
		msg.put((byte) taskNameLen);
		msg.put(taskNameByte);
		Calendar calendar=Calendar.getInstance();
		Util.setTimeStruct(calendar, msg);
		Util.setTimeStruct(calendar, msg);
		Util.setTimeStruct(calendar, msg);
		msg.put((byte)5);
		Util.setTimeStruct(calendar, msg);
		msg.put((byte)5);
		msg.put((byte)2);
		
		msg.putShort((short)17);
		msg.put((byte)1);
		msg.putInt(100);
		msg.putInt(100);
		Util.setTimeStruct(calendar, msg);
		msg.put((byte)5);
		
		msg.putShort((short)17);
		msg.put((byte)2);
		msg.putInt(200);
		msg.putInt(200);
		Util.setTimeStruct(calendar, msg);
		msg.put((byte)5);
		
		
		
		
		msg.putInt(SpyuMsgDefinition.COMPLETE_SAFE_FLAG);
		msg.flip();
		return msg.array();
	}
}
