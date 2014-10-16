package server.net.transfer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import com.fleety.base.Util;

import server.net.transfer.container.QueueContainer.QueueItemInfo;

/**
 * 协议格式：0xFF 0xFF 0xFF 0xFF 8字节数据长度 1字节传输协议号
 * @author Administrator
 *
 */
public class TransferProtocol {
	public static final long PROTOCOL_HEAD_FLAG = 0x7FEEDDCCBBAA9988l;
	
	public static final byte HEART_MSG = 0x01;
	public static final byte NEST_TRANSFER_DATA_MSG = 0x02;
	public static final byte USER_TRANSFER_DATA_MSG = 0x03;
	public static final byte UPLOAD_STATUS_REQUEST_MSG = 0x04;
	public static final byte UPLOAD_STATUS_RESPONSE_MSG = 0x05;
	public static final byte DOWNLOAD_STATUS_REQUEST_MSG = 0x06;
	public static final byte DOWNLOAD_STATUS_RESPONSE_MSG = 0x07;
	public static final byte DOWNLOAD_DATA_REQUEST_MSG = 0x08;
	public static final byte DISCONNECT_MSG = 0x79;
	
	public static byte[] createHeadInfo(long dataLen,byte msg){
		ByteBuffer buff = ByteBuffer.allocate(17);
		buff.order(ByteOrder.LITTLE_ENDIAN);
		buff.putLong(PROTOCOL_HEAD_FLAG);
		buff.put(msg);
		buff.putLong(dataLen);
		return buff.array();
	}
	
	public static byte[] createHeartData(){
		byte[] head = createHeadInfo(8,HEART_MSG);
		ByteBuffer buff = ByteBuffer.allocate(head.length + 8);
		buff.order(ByteOrder.LITTLE_ENDIAN);
		buff.put(head);
		buff.putLong(System.currentTimeMillis());
		return appendHTTPHead(buff.array());
	}
	public static byte[] createDisconnectData(){
		byte[] head = createHeadInfo(8,DISCONNECT_MSG);
		ByteBuffer buff = ByteBuffer.allocate(head.length + 8);
		buff.order(ByteOrder.LITTLE_ENDIAN);
		buff.put(head);
		buff.putLong(System.currentTimeMillis());
		return appendHTTPHead(buff.array());
	}

	public static byte[] createUserSendData(byte[] data,String appendInfo){
		if(data == null){
			return null;
		}
		return appendHTTPHead(createUserSendData(data,0,data.length,appendInfo));
	}
	
	public static byte[] createUserSendData(byte[] data,int offset,int len,String appendInfo){
		byte[] aData = null;
		int alen = 0;
		if(appendInfo != null){
			aData = appendInfo.getBytes();
			alen = aData.length;
		}
		byte[] head = createHeadInfo(len,USER_TRANSFER_DATA_MSG);
		ByteBuffer buff = ByteBuffer.allocate(head.length + 4 + alen + len);
		buff.order(ByteOrder.LITTLE_ENDIAN);
		buff.put(head);
		if(aData != null){
			buff.putInt(aData.length);
			buff.put(aData);
		}else{
			buff.putInt(0);
		}
		buff.put(data,offset,len);
		
		return appendHTTPHead(buff.array());
	}

	public static byte[] createNestSendData(byte[] data,int offset,int len,String uniqueName,int blockNumber){
		if(uniqueName == null){
			return null;
		}
		byte[] aData = uniqueName.getBytes();
		int alen = aData.length;

		byte[] head = createHeadInfo(4 + alen + 8 + len,NEST_TRANSFER_DATA_MSG);
		ByteBuffer buff = ByteBuffer.allocate(head.length + 4 + alen + 8 + len);
		buff.order(ByteOrder.LITTLE_ENDIAN);
		buff.put(head);
		buff.putInt(aData.length);
		buff.put(aData);
		buff.putLong(blockNumber);

		buff.put(data,offset,len);
		
		return appendHTTPHead(buff.array());
	}
	
	/**
	 * 发送接收状态查询消息
	 * @param sInfo  封装服务器方为数据块分配的唯一性标识，如果还未分配，则上传零长度字符串
	 * 					封装客户方为数据块分配的唯一性标识
	 * 					封装文件的大小，单位字节
	 * 					封装文件的最后修改时间，单位毫秒
	 * @return
	 */
	public static byte[] createUploadStatusRequest(QueueItemInfo sInfo){
		StringBuffer strBuff = new StringBuffer(256);
		strBuff.append(sInfo.id);
		strBuff.append("\t");
		strBuff.append(sInfo.name);
		strBuff.append("\t");
		strBuff.append(sInfo.size);
		strBuff.append("\t");
		strBuff.append(sInfo.lastModifiedTime);
		strBuff.append("\t");
		byte[] aData = sInfo.appendInfo.getBytes();
		strBuff.append(Util.byteArr2BcdStr(aData,0,aData.length));
		
		byte[] data = strBuff.toString().getBytes();
		byte[] head = createHeadInfo(data.length,UPLOAD_STATUS_REQUEST_MSG);
		
		ByteBuffer buff = ByteBuffer.allocate(head.length + data.length);
		buff.order(ByteOrder.LITTLE_ENDIAN);
		buff.put(head);
		buff.put(data);
		
		return appendHTTPHead(buff.array());
	}
	
	/**
	 * 文件接收状态查询响应
	 * @param id                服务器分配的数据块唯一性标识，如果客户端上传时已包含，则不再重新分配
	 * @param name              客户方为数据块分配的唯一性标识
	 * @param concurrentNum		并发上传数
	 * @param blockSize         分块上传的单块大小，单位M
	 * @param finishDataFlag    以bit描述的块完成状态，1代表完成 0代表未完成
	 * @return
	 */
	public static byte[] createUploadStatusResponse(String id,String name,int blockSize,int concurrentNum,byte[] finishDataFlag){
		StringBuffer strBuff = new StringBuffer(256);
		strBuff.append(id);
		strBuff.append("\t");
		strBuff.append(name);
		strBuff.append("\t");
		strBuff.append(concurrentNum);
		strBuff.append("\t");
		strBuff.append(blockSize);
		strBuff.append("\t");
		strBuff.append(Util.byteArr2BcdStr(finishDataFlag,0,finishDataFlag.length));
		
		byte[] data = strBuff.toString().getBytes();
		byte[] head = createHeadInfo(data.length,UPLOAD_STATUS_RESPONSE_MSG);
		
		ByteBuffer buff = ByteBuffer.allocate(head.length + data.length);
		buff.order(ByteOrder.LITTLE_ENDIAN);
		buff.put(head);
		buff.put(data);
		
		return appendHTTPHead(buff.array());
	}

	
	public static byte[] createDownloadStatusRequest(QueueItemInfo sInfo){
		StringBuffer strBuff = new StringBuffer(256);
		strBuff.append(sInfo.id);
		strBuff.append("\t");
		strBuff.append(sInfo.name);
		strBuff.append("\t");
		strBuff.append(sInfo.size);
		strBuff.append("\t");
		strBuff.append(sInfo.lastModifiedTime);
		strBuff.append("\t");
		strBuff.append(sInfo.blockSize);
		strBuff.append("\t");
		strBuff.append(sInfo.concurrentNum);
		strBuff.append("\t");
		byte[] statusArr = sInfo.finishStatus;
		if(statusArr == null){
			statusArr = new byte[0];
		}
		strBuff.append(Util.byteArr2BcdStr(statusArr,0,statusArr.length));
		strBuff.append("\t");
		if(sInfo.appendInfo == null){
			sInfo.appendInfo = "";
		}
		byte[] aData = sInfo.appendInfo.getBytes();
		strBuff.append(Util.byteArr2BcdStr(aData,0,aData.length));
		byte[] data = strBuff.toString().getBytes();
		byte[] head = createHeadInfo(data.length,DOWNLOAD_STATUS_REQUEST_MSG);
		
		ByteBuffer buff = ByteBuffer.allocate(head.length + data.length);
		buff.order(ByteOrder.LITTLE_ENDIAN);
		buff.put(head);
		buff.put(data);
		
		return appendHTTPHead(buff.array());
	}

	public static byte[] createDownloadStatusResponse(String id,String name,boolean result,long size,long lastModifiedTime,int blockSize,int concurrentNum,byte[] finishStatus){
		StringBuffer strBuff = new StringBuffer(256);
		strBuff.append(id);
		strBuff.append("\t");
		strBuff.append(name);
		strBuff.append("\t");
		strBuff.append(result?1:0);
		strBuff.append("\t");
		strBuff.append(size);
		strBuff.append("\t");
		strBuff.append(lastModifiedTime);
		strBuff.append("\t");
		strBuff.append(concurrentNum);
		strBuff.append("\t");
		strBuff.append(blockSize);
		strBuff.append("\t");
		strBuff.append(Util.byteArr2BcdStr(finishStatus, 0, finishStatus.length));
		
		byte[] data = strBuff.toString().getBytes();
		byte[] head = createHeadInfo(data.length,DOWNLOAD_STATUS_RESPONSE_MSG);
		
		ByteBuffer buff = ByteBuffer.allocate(head.length + data.length);
		buff.order(ByteOrder.LITTLE_ENDIAN);
		buff.put(head);
		buff.put(data);
		
		return appendHTTPHead(buff.array());
	}
	public static byte[] createDownloadDataRequest(String id){
		StringBuffer strBuff = new StringBuffer(256);
		strBuff.append(id);
		
		byte[] data = strBuff.toString().getBytes();
		byte[] head = createHeadInfo(data.length,DOWNLOAD_DATA_REQUEST_MSG);
		
		ByteBuffer buff = ByteBuffer.allocate(head.length + data.length);
		buff.order(ByteOrder.LITTLE_ENDIAN);
		buff.put(head);
		buff.put(data);
		
		return appendHTTPHead(buff.array());
	}
	
	/**
	 * POST / HTTP/1.1\n
	 * Connection: keep-alive\n
	 * Content-length: "+len+"\n\n
	 */
	private static final String httpHead = "POST / HTTP/1.1\nConnection: keep-alive\nContent-length: ";
	private static byte[] appendHTTPHead(byte[] data){
		return appendHTTPHead(data,0,data.length);
	}
	private static byte[] appendHTTPHead(byte[] data,int offset,int len){
		String httpHeadInfo = httpHead+len+"\n\n";
		byte[] httpHeadByteArr = httpHeadInfo.getBytes();
		byte[] httpData = new byte[httpHeadByteArr.length+len];

		System.arraycopy(httpHeadByteArr, 0, httpData, 0, httpHeadByteArr.length);
		System.arraycopy(data, offset, httpData, httpHeadByteArr.length, len);
		return httpData;
	}
}
