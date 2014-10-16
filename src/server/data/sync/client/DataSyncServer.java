package server.data.sync.client;

import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import server.data.sync.FleetyByteArrayOutputStream;
import server.data.sync.IDataListener;
import server.data.sync.IDataSync;
import server.data.sync.SyncObj;
import server.data.sync.container.ISyncInfo;
import server.data.sync.container.SyncDataContainer;
import server.socket.inter.ConnectSocketInfo;
import server.socket.socket.FleetySocket;

public class DataSyncServer extends FleetySocket implements IDataSync{
	private static DataSyncServer singleInstance = null;
	public static DataSyncServer getSingleInstance(){
		if(singleInstance == null){
			singleInstance = new DataSyncServer();
		}
		return singleInstance;
	}
	
	private Object waitLock = new Object();
	private String nodeFlag = "default";
	public DataSyncServer(){
		
	}
	
	public String getNodeFlag(){
		return this.nodeFlag;
	}

	private FleetyByteArrayOutputStream out = null;
	private ArrayList listenerList = null;
	private byte[] headArr = new byte[4];
	public boolean startServer() {
		this.listenerList = new ArrayList(2);
		
		Integer streamSize = this.getIntegerPara("init_stream_size");
		if(streamSize == null){
			streamSize = new Integer(1024*1024);
		}
		if(streamSize.intValue() < 1024){
			streamSize = new Integer(1024);
		}
		this.out = new FleetyByteArrayOutputStream(streamSize.intValue());
		
		this.nodeFlag = this.getStringPara("node_flag");
		
		this.removePara(CMD_READER_FLAG);
		this.removePara(CMD_RELEASER_FLAG);
		this.addPara(CMD_READER_FLAG, "server.socket.help.ObjectReader");
		this.addPara(CMD_RELEASER_FLAG, "server.data.sync.client.DataReleaser");
		
		if(!super.startServer()){
			return false;
		}
		
		synchronized(waitLock){
			if(SyncDataContainer.getSingleInstance() == null){
				try{
					waitLock.wait(30000);
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		}
		
		return SyncDataContainer.getSingleInstance() != null;
	}
	
	public void startLogin(){
		this.releaseDataObject(new SyncObj(this.nodeFlag,SyncObj.CLIENT_LOGIN_MSG,1,null,false));
	}
	
	public synchronized boolean releaseDataObject(SyncObj obj){
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
			
			this.sendData(bBuff.array(),0,bBuff.remaining());
			
			this.out.reset();
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	public void dataArrived(SyncObj obj,ConnectSocketInfo conn){
		if(!this.isRunning()){
			return ;
		}
		
		if(obj.getMsg() == SyncObj.DATA_SYNC_MSG){
			if(obj.isFullDataContainer()){
				synchronized(this.waitLock){
					((SyncDataContainer)obj.getData()).register(this);
					this.waitLock.notifyAll();
				}
			}else{
				SyncDataContainer.getSingleInstance().updateInfo((ISyncInfo)obj.getData(),false);
			}
		}else if(obj.getMsg() == SyncObj.INFO_PRINT_MSG){
			String info = SyncDataContainer.getSingleInstance().getPrintInfo((ISyncInfo)obj.getData());
			this.releaseDataObject(new SyncObj(this.nodeFlag,SyncObj.INFO_PRINT_MSG,1,info,false));
		}
		
		IDataListener listener;
		for(int i=0;i<this.listenerList.size();i++){
			listener = (IDataListener)this.listenerList.get(i);
			listener.receiveData(obj);
		}
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
}
