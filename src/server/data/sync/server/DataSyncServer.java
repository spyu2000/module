package server.data.sync.server;

import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import com.fleety.util.pool.timer.FleetyTimerTask;

import server.data.sync.FleetyByteArrayOutputStream;
import server.data.sync.IDataListener;
import server.data.sync.IDataSync;
import server.data.sync.SyncObj;
import server.data.sync.container.ISyncInfo;
import server.data.sync.container.SyncDataContainer;
import server.socket.inter.ConnectSocketInfo;
import server.socket.serversocket.FleetySocketServer;
import server.threadgroup.ThreadPoolGroupServer;

public class DataSyncServer extends FleetySocketServer implements IDataSync {
	private FleetyByteArrayOutputStream out = null;
	private ArrayList listenerList = null;
	private byte[] headArr = new byte[4];
	private int maxObjSize = -1;
	private String maxObjName = null;
	private SyncDataContainer dataContainer = null;
	
	public boolean startServer() {
		this.dataContainer = new SyncDataContainer();
		
		this.listenerList = new ArrayList(2);
		
		Integer streamSize = this.getIntegerPara("init_stream_size");
		if(streamSize == null){
			streamSize = new Integer(1024*1024);
		}
		if(streamSize.intValue() < 1024){
			streamSize = new Integer(1024);
		}
		this.out = new FleetyByteArrayOutputStream(streamSize.intValue());
		
		this.removePara(CMD_READER_FLAG);
		this.removePara(CMD_RELEASER_FLAG);
		this.addPara(CMD_READER_FLAG, "server.socket.help.ObjectReader");
		this.addPara(CMD_RELEASER_FLAG, "server.data.sync.server.DataReleaser");
		
		ThreadPoolGroupServer.getSingleInstance().createTimerPool(ThreadPoolGroupServer._QUICK_EXECUTE_OBJ_NAME_).schedule(new FleetyTimerTask(){
			public void run(){
				StringBuffer buff = new StringBuffer(1024);
				buff.append("Server Info:");
				buff.append("\n");
				buff.append("\tMemory:");
				buff.append("Free="+Runtime.getRuntime().freeMemory()+" Total="+Runtime.getRuntime().totalMemory()+" Max="+Runtime.getRuntime().maxMemory());
				buff.append("\n");
				buff.append("\tmaxObjSize:size="+maxObjSize+" name="+maxObjName);
				System.out.println(buff);
			}
		}, 60000, 60000);
		
		return super.startServer();
	}
	
	public boolean addDataListener(IDataListener listener){
		if(!this.isRunning()){
			return false;
		}
		if(listener == null){
			return false;
		}
		
		if(this.listenerList.contains(listener)){
			return true;
		}
		this.listenerList.add(listener);
		
		return true;
	}
	
	public boolean removeDataListener(IDataListener listener){
		if(!this.isRunning()){
			return false;
		}
		if(listener == null){
			return false;
		}
		return this.listenerList.remove(listener);
	}

	public boolean releaseDataObject(SyncObj obj){
		return this.releaseDataObject(obj,null);
	}
	
	public synchronized boolean releaseDataObject(SyncObj obj,ConnectSocketInfo conn){
		if(!this.isRunning()){
			return false;
		}
		if(obj == null){
			return false;
		}
		
		try{
			this.out.reset();
			
			this.out.write(this.headArr);
			ObjectOutputStream objOut = new ObjectOutputStream(this.out);
			objOut.writeObject(obj);
			
			ByteBuffer bBuff = this.out.toByteBuffer();
			bBuff.putInt(0, bBuff.remaining()-4);
			if(bBuff.remaining() > this.maxObjSize){
				this.maxObjSize = bBuff.remaining();
				this.maxObjName = obj.getClass().getName();
			}
			
			ConnectSocketInfo[] connArr = this.getAllConnInfo();
			for(int i=0;i<connArr.length;i++){
				if(connArr[i] == conn){
					continue;
				}
				try{
					connArr[i].writeData(bBuff.array(),0,bBuff.remaining());
				}catch(Exception e){
					e.printStackTrace();
				}
			}
			
			this.out.reset();
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	private void updateData(SyncObj obj){
		if(obj == null){
			return ;
		}

		this.dataContainer.updateInfo((ISyncInfo)obj.getData(),false);
	}
	
	public void dataArrived(SyncObj obj,ConnectSocketInfo conn){
		if(!this.isRunning()){
			return ;
		}
		
		
		if(obj.getMsg() == SyncObj.DATA_SYNC_MSG){
			this.updateData(obj);
	
			this.releaseDataObject(obj,conn);
		}else if(obj.getMsg() == SyncObj.CLIENT_LOGIN_MSG){
			this.clientLogin(obj,conn);
		}else if(obj.getMsg() == SyncObj.INFO_PRINT_MSG){
			System.out.println("InfoPrint["+obj.getSource()+"]:"+obj.getData());
		}
		
		IDataListener listener;
		for(int i=0;i<this.listenerList.size();i++){
			listener = (IDataListener)this.listenerList.get(i);
			listener.receiveData(obj);
		}
	}
	private synchronized void clientLogin(SyncObj obj,ConnectSocketInfo conn){
		if(!this.isRunning()){
			return;
		}

		try{
			SyncObj syncObj = new SyncObj(null,SyncObj.DATA_SYNC_MSG,1,this.dataContainer,true);
			
			this.out.reset();
			
			this.out.write(this.headArr);
			ObjectOutputStream objOut = new ObjectOutputStream(this.out);
			objOut.writeObject(syncObj);
			
			ByteBuffer bBuff = this.out.toByteBuffer();
			bBuff.putInt(0, bBuff.remaining()-4);
			
			conn.writeData(bBuff.array(),0,bBuff.remaining());
			
			this.out.reset();
			
			System.out.println("Node["+obj.getSource()+"]Regist Success!");
		}catch(Exception e){
			e.printStackTrace();
			System.out.println("Node["+obj.getSource()+"]Regist Failure!");
			conn.destroy();
		}
	}
}
