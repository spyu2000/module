package server.net.transfer.container;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import server.net.transfer.IDataTask;
import server.net.transfer.TransferProtocol;
import server.net.transfer.client.NetTransferClient;
import server.net.transfer.container.QueueContainer.QueueItemInfo;

import com.fleety.base.Util;
import com.fleety.util.pool.thread.BasicTask;

public class ClientDataReceiveTask extends BasicTask implements IDataTask{
	private NetTransferClient transfer = null;
	private int index = 0;
	private String  ip = null;
	private int port = 0;
	private QueueItemInfo sInfo = null;
	public ClientDataReceiveTask(NetTransferClient transfer,int index,String ip,int port,QueueItemInfo sInfo){
		this.transfer = transfer;
		this.index = index;
		this.ip = ip;
		this.port = port;
		this.sInfo = sInfo;
	}
	private Socket socket = null;
	private OutputStream out = null;
	private RandomAccessFile file = null;
	private long updateTime = System.currentTimeMillis();
	public boolean execute() throws Exception {
		sInfo.socketAdd(this);
		
		try{
			socket = new Socket(this.ip,this.port);
			socket.setSoTimeout(10000);
			out = socket.getOutputStream();
			InputStream in = socket.getInputStream();
			
			
			byte[] rData = TransferProtocol.createDownloadDataRequest(sInfo.name);
			out.write(rData);			
			
			
			int dataLen;
			ByteBuffer head = ByteBuffer.allocate(17);
			head.order(ByteOrder.LITTLE_ENDIAN);
			ByteBuffer data;
			int idLen;
			long t;
			String rid;
			int blockNumber,offset;
			while(true){
				t = System.currentTimeMillis();
				//Ìø¹ýHTTP HEAD
				this.skipHttpHead(in);
				
				if(!Util.readFull(in, head.array(), 0, head.capacity())){
					throw new Exception("Stream Eof");
				}
				if(head.getLong(0) != TransferProtocol.PROTOCOL_HEAD_FLAG){
					throw new Exception("Error Head Flag");
				}

				this.updateTime = System.currentTimeMillis();
				dataLen = (int)head.getLong(9);
				data = ByteBuffer.allocate(dataLen);
				data.order(ByteOrder.LITTLE_ENDIAN);
				
				int rLen = 0,nrLen,sLen = 10240;
				while(rLen < dataLen && sInfo.isValid()){
					nrLen = Math.min(sLen, dataLen-rLen);
					if(!Util.readFull(in, data.array(), rLen, nrLen)){
						throw new Exception("Stream Eof");
					}
					rLen += nrLen;
					this.updateTime = System.currentTimeMillis();
				}
				
				if((head.get(8)&0xFF) == TransferProtocol.DISCONNECT_MSG){
					return true;
				}
				
				
				idLen = data.getInt(0);
				rid = new String(data.array(),4,idLen);
				blockNumber = (int)data.getLong(4+idLen);
				
				if(!rid.equals(sInfo.name)){
					throw new Exception("Error Remote Id Flag!" + rid+" "+sInfo.name);
				}

				if(!sInfo.isValid()){
					return false;
				}
				this.updateTime = System.currentTimeMillis();

				file = new RandomAccessFile(sInfo.getQueueReceiveTempFile(),"rw");
				file.seek(blockNumber*sInfo.blockSize);
				offset = 4+idLen+8;
				file.write(data.array(), offset, data.capacity() - offset);
				this.closeFile();

				this.updateTime = System.currentTimeMillis();
				sInfo.finishBlock(blockNumber);
				if(sInfo.isFinished()){
					this.transfer.getReceiveQueueContainer().updateAndSaveQueue();
					this.transfer.triggerTaskChanged(sInfo);
				}else{
					this.transfer.getReceiveQueueContainer().updateAndSaveQueue();
					this.transfer.triggerSendProgress(sInfo);
				}
				
				System.out.println("Finish Receive BlockNumber:["+this.index+"]"+blockNumber+" time="+(System.currentTimeMillis()-t));
				
				this.updateTime = System.currentTimeMillis();
			}
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			this.stop();
			sInfo.socketMinus(this);
		}
		
		return true;
	}
	
	public boolean isAlive(long limitTime){
		return System.currentTimeMillis()-this.updateTime < limitTime;
	}

	private void closeFile(){
		if(file != null){
			try{
				file.close();
			}catch(Exception e){}
		}
	}
	public void stop(){
		this.closeFile();
		if(socket != null){
			try{
				socket.close();
			}catch(Exception e){}
		}
	}
	
	private void skipHttpHead(InputStream in) throws Exception{
		int flag = 0;
		int value;
		while(true){
			value = in.read()&0xFF;
			if(value == '\n'){
				flag ++;
			}else{
				flag = 0;
			}
			if(flag >= 2){
				break;
			}
		}
		
	}
}
