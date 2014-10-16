package server.net.transfer.container;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.Socket;

import server.net.transfer.IDataTask;
import server.net.transfer.TransferProtocol;
import server.net.transfer.container.QueueContainer.QueueItemInfo;

import com.fleety.util.pool.thread.BasicTask;

public class ClientDataSendTask extends BasicTask implements IDataTask{
	private int index = 0;
	private String  ip = null;
	private int port = 0;
	private QueueItemInfo sInfo = null;
	public ClientDataSendTask(int index,String ip,int port,QueueItemInfo sInfo){
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
			socket.setSoTimeout(60000);
			out = socket.getOutputStream();
			InputStream in = socket.getInputStream();
			
			long offset;
			int blockNumber,totalCount,blockSize = sInfo.getBlockSize(),count,tempCount;
			byte[] sData = new byte[blockSize];
			long t ;
			while(sInfo.isValid()){
				blockNumber = sInfo.getNextSendBlockNumber();
				if(blockNumber < 0){
					break;
				}
				
				t = System.currentTimeMillis();
				file = new RandomAccessFile(sInfo.getQueueSendFile(),"r");
				System.out.println("Start Send BlockNumber:["+this.index+"]"+blockNumber);
				offset = blockNumber*blockSize;
				file.seek(offset);
				totalCount = (int)Math.min(sInfo.size - offset, blockSize);
				
				count = 0;
				while(count < totalCount){
					tempCount = file.read(sData, count, totalCount-count);
					if(tempCount < 0){
						throw new Exception("Read File Error");
					}
					count += tempCount;
				}
				closeFile();
				
				byte[] rData = TransferProtocol.createNestSendData(sData, 0, totalCount, sInfo.id, blockNumber);
				
				count = 0;
				totalCount = rData.length;
				while(count < totalCount && sInfo.isValid()){
					tempCount = Math.min(10240, totalCount-count);
					out.write(rData,count,tempCount);
					count += tempCount;
					this.updateTime = System.currentTimeMillis();
				}
				System.out.println("Finish Send BlockNumber:["+this.index+"]"+blockNumber+" size="+totalCount+" time="+(System.currentTimeMillis()-t));
			}
			if(sInfo.isValid()){
				out.write(TransferProtocol.createDisconnectData());
				in.read();
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
}
