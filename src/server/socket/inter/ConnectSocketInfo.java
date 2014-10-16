/**
 * 绝密 Created on 2008-5-15 by edmund
 */
package server.socket.inter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import server.socket.serversocket.FleetySocketServer;
import server.socket.socket.FleetySocket;

import com.fleety.base.GeneralConst;
import com.fleety.base.InfoContainer;

public class ConnectSocketInfo extends InfoContainer{
	//是否对接收和发送的数据进行记录，支持进出通道的同时记录
	public static final Object CONN_RECORD_FLAG = "conn_record_flag";

	public static final Object SOCKET_DESC_FLAG = new Object();
	
	//进数据和出数据记录值
	public static final int IN_DATA_RECORD = 1;
	public static final int OUT_DATA_RECORD = 2;
	
	/**
	 * 注:数值越小优先级越高
	 */
	public static final int PRIORITY_LEVEL_1 = 1;
	public static final int PRIORITY_LEVEL_15 = 15;
	
	private int recordFlag = 0;
	private OutputStream in_fout = null;
	private OutputStream out_fout = null;
	
	private SocketChannel channel = null;
	private SelectionKey sKey = null;
	private long lastActiveTime = 0;

	private long lastWriteTime = 0;
	private Object listLock = null;
	private int maxCachSize = 64*1024;
	
	//进入过的数据量总大小
	private long allSize = 0;
	//当前待发送的数据量
	private int curSize = 0;

	//消息队列
	private List priorityList = new LinkedList();
	//优先级的消息数量
	private int lowerPriorityNum = 0,higherPriorityNum = 0;
	
	private boolean hasThread = false;
	
	private ICmdReader reader = null;
	private ICmdReleaser releaser = null;
	private Object optionalInfo = null;
	
	private FleetySocketServer server = null;
	private FleetySocket client = null;
	private Date createDate = new Date();
	
	private DataPacket curPacket = null;
	public ConnectSocketInfo(SocketChannel channel,ICmdReader reader,ICmdReleaser releaser,Object optionalInfo){
		this(channel,reader,releaser,optionalInfo,null);
	}
	
	public ConnectSocketInfo(SocketChannel channel,ICmdReader reader,ICmdReleaser releaser,Object optionalInfo,FleetySocketServer server){
		this.channel = channel;
		this.reader = reader;
		this.releaser = releaser;
		this.optionalInfo = optionalInfo;
		this.lastActiveTime = System.currentTimeMillis();
		this.server = server;

		if(this.server != null){
			if(this.server.getIntegerPara(CONN_RECORD_FLAG)!=null){
				this.recordFlag = this.server.getIntegerPara(CONN_RECORD_FLAG).intValue();
			}
		}
	}
	
	public ConnectSocketInfo(SocketChannel channel){
		this(channel,null);
	}
	
	public ConnectSocketInfo(SocketChannel channel,FleetySocket client){
		this.channel = channel;
		this.client = client;
		this.lastActiveTime = System.currentTimeMillis();
		
		if(this.client != null){
			if(this.client.getIntegerPara(CONN_RECORD_FLAG)!=null){
				this.recordFlag = this.client.getIntegerPara(CONN_RECORD_FLAG).intValue();
			}
		}
	}
	
	public ConnectSocketInfo(SocketChannel channel,int maxCachSize){
		this.channel = channel;
		this.lastActiveTime = System.currentTimeMillis();
		if(maxCachSize >= 0){
			this.maxCachSize = maxCachSize;
		}
		System.out.println("this.maxCachSize:"+this.maxCachSize+" "+this.channel.socket().getRemoteSocketAddress());
	}
	
	private String getLogFileName(boolean isIn){
		String name ;
		SocketAddress addr = this.getRemoteSocketAddress();
		if(addr == null){
			name = "unknown";
		}else{
			name = addr.toString().replaceAll(":", "_");
		}
		if(isIn){
			return "in_"+GeneralConst.YYYYMMDDHHMMSSsss.format(this.createDate)+"_"+name+".log";
		}else{
			return "out_"+GeneralConst.YYYYMMDDHHMMSSsss.format(this.createDate)+"_"+name+".log";
		}
	}
	private void recordData(byte[] data,int offset,int len,boolean isIn){
		File dir = new File("_conn_data_record_");
		dir.mkdirs();
		OutputStream out = null;
		try{
			out = isIn?this.in_fout:this.out_fout;
			if(out == null){
				out = new FileOutputStream(new File(dir,this.getLogFileName(isIn)),true);
				if(isIn){
					this.in_fout = out;
				}else{
					this.out_fout = out;
				}
			}
			out.write(data, offset, len);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public FleetySocket getFleetySocket(){
		return this.client;
	}
	public FleetySocketServer getFleetySocketServer(){
		return this.server;
	}
	
	public void switchSendMode2Thread(int maxCachSize) throws Exception{
		this.initThread(maxCachSize);
	}
	
	private void initThread(int maxCachSize) throws Exception{
		if(this.hasThread){
			return ;
		}
		this.listLock = new Object();
		
		this.hasThread = true;
		this.maxCachSize = maxCachSize;
		if(this.maxCachSize <= 0){
			this.maxCachSize = 8*1024;
		}
	}
	
	public void setSocketDesc(String desc){
		if(desc == null){
			this.removeInfo(SOCKET_DESC_FLAG);
		}else{
			this.setInfo(SOCKET_DESC_FLAG, desc);
		}
	}
	public String getSocketDesc(){
		String desc = this.getString(SOCKET_DESC_FLAG);
		if(desc == null){
			SocketAddress addr = this.getRemoteSocketAddress();
			if(addr != null){
				desc = addr.toString();
			}
		}
		return desc;
	}
	
	public void setMinCachSize(int minCachSize){
		
	}
	public int getMinCachSize(){
		return 1024;
	}
	public void setMaxCachSize(int maxCachSize){
		this.maxCachSize = maxCachSize;
		System.out.println("this.maxCachSize:"+this.maxCachSize+" "+this.channel.socket().getRemoteSocketAddress());
	}
	public int getMaxCachSize(){
		return this.maxCachSize;
	}
	
	public long getLastActiveTime(){
		return this.lastActiveTime;
	}
	public void updateLastActiveTime(){
		this.lastActiveTime = System.currentTimeMillis();
	}
	
	public void setSelectionKey(SelectionKey sKey){
		this.sKey = sKey;
	}
	public SelectionKey getSelectionKey(){
		return this.sKey;
	}
	public int remaining(){
		if(this.hasThread){
			return this.curSize;
		}else{
			return 0;
		}
	}
	public long getAllSize(){
		return this.allSize;
	}
	
	private SocketAddress address = null;
	public SocketAddress getRemoteSocketAddress(){
		if(this.channel == null){
			return address;
		}
		if(address==null){
			address = this.channel.socket().getRemoteSocketAddress();
		}
		return address;
	}
	private SocketAddress localAddress = null;
	public SocketAddress getLocalSocketAddress(){
		if(this.channel == null){
			return null;
		}
		if(localAddress==null){
			localAddress = this.channel.socket().getLocalSocketAddress();
		}
		return localAddress;
	}

	int localPort=0;
	public int getLocalSocketPort(){
		if(this.channel == null){
			return 0;
		}
		if(localPort==0){
			localPort = this.channel.socket().getLocalPort();
		}
		return localPort;
	}
	private InetAddress iadd = null;
	public InetAddress getInetAddress(){
		if(this.channel == null){
			return null;
		}
		if(iadd==null)
			iadd = this.channel.socket().getInetAddress();
		return iadd;
	}
	private int socketPort=0;
	public int getPort(){
		if(this.channel == null){
			return -1;
		}
		if(socketPort==0)
			socketPort= this.channel.socket().getPort();
		return socketPort;
	}

	public boolean isConnected(){
		if(this.channel == null){
			return false;
		}
		return this.channel.isConnected();
	}
	public boolean isOpen(){
		if(this.channel == null){
			return false;
		}
		return this.channel.isOpen();
	}
	
	public int read(ByteBuffer buff) throws IOException{
		int num = this.channel.read(buff);
		if(num < 0){
			throw new IOException("Stream Eof!");
		}
		
		if((this.recordFlag & IN_DATA_RECORD) > 0){
			this.recordData(buff.array(),buff.position()-num,num,true);
		}

		return num;
	}
	
	private IDataFilter dataFilter = null;
	public void setDataFilter(IDataFilter dataFilter){
		this.dataFilter = dataFilter;
	}
	
	public int writeData(ByteBuffer dataBuff,int priorityLevel) throws Exception{
		if(this.dataFilter != null){
			dataBuff = this.dataFilter.filter(dataBuff);
		}
		if(dataBuff == null){
			throw new NullPointerException();
		}
		int len = dataBuff.remaining();
		if(len > this.maxCachSize){
			throw new Exception("待写数据长度超过最大缓存允许! len:"+len+"  this.maxCachSize:"+this.maxCachSize);
		}
		
		if(this.hasThread){
			//首先写缓存中的数据
			if(!this.flush()){
				throw new Exception("Closed Channel!");
			}
			
			synchronized(this.listLock){
				//记录数据
				if((this.recordFlag & OUT_DATA_RECORD) > 0){
					this.recordData(dataBuff.array(),dataBuff.position(),len,false);
				}
				
				//如果缓存中的数据无剩余,则直接写待写数据,如果缓存中有数据剩余 ,则不书写
				int num = 0;
				if(!this.hasRemainData()){
					num = this.channel.write(dataBuff);
					if(num > 0){
						this.lastWriteTime = System.currentTimeMillis();
					}
				}
				//如果当前待写数据有剩余
				if(dataBuff.hasRemaining()){
					int remainNum = dataBuff.remaining();
					if(remainNum + this.curSize > this.maxCachSize){
						if(priorityLevel != PRIORITY_LEVEL_1){
							this.printCachFullError();
							return 0;
						}
						//清空低优先级数据,返回true代表在该处理的基础上能满足放入,返回false代表不能满足,则直接抛弃该数据.
						if(!this.clearLowerPriority(remainNum)){
							this.printCachFullError();
							return 0;
						}
					}
					
					//构建数据包并放置到待发送list中.
					DataPacket packet = new DataPacket(priorityLevel);
					packet.dataBuff = ByteBuffer.allocate(remainNum);
					System.arraycopy(dataBuff.array(), dataBuff.arrayOffset()+dataBuff.position(), packet.dataBuff.array(), 0, remainNum);
					
					if(num > 0){
						this.curPacket = packet;
					}else{
						this.addDataPacket(packet);
					}
					
					this.curSize += remainNum;
				}

				this.allSize += len;
			}
			
			//把该连接对象添加到数据下发保证服务中.
			if(this.hasRemainData()){
				DataSendHelper.getSingleInstance().add2Help(this);
			}
		}else{
			int num;
			synchronized(this.channel){
				num = this.channel.write(dataBuff);
				this.allSize += num;
			}
			return num; 
		}
		
		return len;
	}	
	private long lastPrintCachFullTime = 0;
	private void printCachFullError(){
		if(System.currentTimeMillis() - lastPrintCachFullTime > 120000){
			this.lastPrintCachFullTime = System.currentTimeMillis();
			System.err.println("%%%%%%%%%%%%%%%%%["+this.getRemoteSocketAddress()+"]已使用了最大缓存["+this.maxCachSize+"],数据将丢失.");
		}
	}

	/**
	 * 以最低优先级的数据进行写
	 * @param dataBuff
	 * @return 写入的字节量,如果是线程模式,则返回一个未写或全部写入
	 * @throws Exception
	 */
	public int writeData(ByteBuffer dataBuff) throws Exception{
		return this.writeData(dataBuff,PRIORITY_LEVEL_15);
	}
	public int writeData(byte[] data,int offset,int len,int priorityLevel) throws Exception{
		if(data == null){
			return 0;
		}
		if(offset < 0){
			throw new Exception("错误起始位置!");
		}
		
		int totalLen = data.length;
		if(totalLen < offset + len){
			throw new Exception("长度不足!");
		}
		
		ByteBuffer dataBuff = ByteBuffer.wrap(data,offset,len);
		int num = this.writeData(dataBuff,priorityLevel);
		return num;
	}
	
	public int writeData(byte[] data,int offset,int len) throws Exception{
		return this.writeData(data, offset, len, PRIORITY_LEVEL_15);
	}
	
	/**
	 * 添加数据包在list中合适的位置.
	 * @param packet
	 */
	private void addDataPacket(DataPacket packet){
		int index = this.priorityList.size();
		
		if(packet.priorityLevel == PRIORITY_LEVEL_1){
			index = this.higherPriorityNum;
			
			this.higherPriorityNum ++;
		}else{
			this.lowerPriorityNum ++;
		}

		this.priorityList.add(index, packet);
	}
	private boolean clearLowerPriority(int destRemainNum){
		int destCurSize = this.maxCachSize - destRemainNum;
		Iterator itr = this.priorityList.listIterator(this.higherPriorityNum);
		
		//存在的第一个数据可能是高优先级的,也有可能是正在写的数据.考虑0索引位置的数据是个低优先级的正在写数据的可能.
		DataPacket pack;
		while(this.curSize > destCurSize && itr.hasNext()){
			pack = (DataPacket)itr.next();
			itr.remove();
			
			this.curSize -= pack.remaining();
		}
		
		return this.curSize <= destCurSize;
	}
	
	public void destroy(){
		if(this.isClosed){
			return;
		}
		this.isClosed = true;
		
		try{
			if(this.in_fout != null){
				this.in_fout.close();
			}
			this.in_fout = null;
			if(this.out_fout != null){
				this.out_fout.close();
			}
			this.out_fout = null;
		}catch(Exception e){
			e.printStackTrace();
		}
		
		try{
			if(this.channel != null){
				this.channel.close();
			}
			this.channel = null;
		}catch(Exception e){
			
		}
		try{
			if(this.sKey != null){
				this.sKey.cancel();
			}
		}catch(Exception e){
			
		}
		this.clearList();
		
		if(this.server != null){
			this.server.destroySocketChannel(this, CmdInfo.CLOSE_IO_ERROR_CODE);
		}
		if(this.client != null){
			this.client.closeSocket();
		}
	}
	public void closeSocket(){
		this.destroy();
	}
	/**
	 * 把当前应用层中的数据向底层进行书写.写尽量多的数据.
	 * @return 是否书写成功.只要Socket未出现异常,都将表现为成功.
	 */
	public boolean flush(){
		if(!this.hasThread){
			return true;
		}
		
		if(this.channel == null){
			return false;
		}
		
		synchronized(this.listLock){
			try{
				this.curPacket = this.writeCurPacket();
				if(this.curPacket != null){
					return true;
				}
				
				Iterator itr;
				for(itr = this.priorityList.iterator();itr.hasNext();){
					this.curPacket = (DataPacket)itr.next();
					itr.remove();
					
					if(this.curPacket.priorityLevel == PRIORITY_LEVEL_1){
						this.higherPriorityNum --;
					}else{
						this.lowerPriorityNum --;
					}
					
					this.curPacket = this.writeCurPacket();
					if(this.curPacket != null){
						break;
					}
				}
			}catch(Exception e){
				e.printStackTrace();
				this.clearList();
				return false;
			}
		}
		
		return true;
	}
	
	private DataPacket writeCurPacket() throws Exception{
		if(this.curPacket == null){
			return this.curPacket;
		}
		
		int num = this.channel.write(this.curPacket.dataBuff);
		this.curSize -= num;
		if(num > 0){
			lastWriteTime = System.currentTimeMillis();
		}
		if(this.curPacket.hasRemaining()){
			return this.curPacket;
		}
		return null;
	}

	private void clearList(){
		if(hasThread){
			this.curSize = 0;
			this.curPacket = null;
			synchronized(this.listLock){
				this.priorityList.clear();
			}
		}
	}
	
	/**
	 * 当前该Socket对象的应用程序中是否有待书写的数据
	 * @return
	 */
	public boolean hasRemainData(){
		if(!this.hasThread){
			return false;
		}

		return this.curSize > 0;
	}
	
	/**
	 * 最近一次写距离当前的时间差
	 * @return
	 */
	public long getLastWriteTimeDiff(){
		return System.currentTimeMillis()-this.lastWriteTime;
	}
	
	public void finalize() throws Throwable{
		super.finalize();
		this.destroy();
	}
	
	private class DataPacket{
		//待书写的数据,从用户空间拷贝到模块空间,用户不再引用
		public ByteBuffer dataBuff = null;
		//包的优先级
		public int priorityLevel = PRIORITY_LEVEL_15;
		
		public DataPacket(){
			
		}
		public DataPacket(int priorityLevel){
			this.priorityLevel = priorityLevel;
		}

		public boolean hasRemaining(){
			if(this.dataBuff == null){
				return false;
			}
			return this.dataBuff.hasRemaining();
		}
		public int remaining(){
			return this.dataBuff.remaining();
		}
	}
	private boolean isClosed = false;
	
	public boolean isClosed(){
		return this.isClosed;
	}
	public ICmdReader getReader() {
		return reader;
	}

	public void setReader(ICmdReader reader) {
		this.reader = reader;
	}

	public ICmdReleaser getReleaser() {
		return releaser;
	}

	public void setReleaser(ICmdReleaser releaser) {
		this.releaser = releaser;
	}

	public Object getOptionalInfo() {
		return optionalInfo;
	}

	public void setOptionalInfo(Object optionalInfo) {
		this.optionalInfo = optionalInfo;
	}
	public int getCurSize(){
		return curSize;
	}
}
