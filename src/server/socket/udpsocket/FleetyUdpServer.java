package server.socket.udpsocket;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import server.threadgroup.PoolInfo;
import server.threadgroup.ThreadPoolGroupServer;
import com.fleety.server.BasicServer;
import com.fleety.util.pool.thread.BasicTask;
import com.fleety.util.pool.thread.ThreadPool;

public class FleetyUdpServer extends BasicServer {
	// 支持的参数标识。
	public static final String LOCALIP_FLAG = "localIp";
	public static final String LOCALPORT_FLAG = "localPort";
	public static final String SENDLOCALPORT_FLAG="sendLocalPort";
	public static final String DESTIP_FLAG = "destIp";
	public static final String DESTPORTFLAG = "destPort";
	public static final String DEAL_DATA_Listener_FLAG = "dealDataListener";
	public static final String BUFFER_SIZE_FLAG = "bufferSize";

	private String localIp = null;
	private String localPort = null;
	private String destIp = null;
	private String destPort = null;
	private String sendLocalPort=null;
	private DatagramSocket server = null;
	private DatagramSocket sendServer=null;
	private LinkedList listenerList = new LinkedList();
	private int buffer_size = 1024;

	private DetectThread readThread = null;

	public boolean startServer() {
		try {
			String temp = this.getStringPara(BUFFER_SIZE_FLAG);
			if (temp != null && !temp.trim().equals("")) {
				buffer_size = Integer.parseInt(temp.trim());
			}
			localIp = this.getStringPara(LOCALIP_FLAG);
			localPort = this.getStringPara(LOCALPORT_FLAG);
			sendLocalPort = this.getStringPara(SENDLOCALPORT_FLAG);
			
			destIp = this.getStringPara(DESTIP_FLAG);
			destPort = this.getStringPara(DESTPORTFLAG);
			Object dealDataListener = this.getPara(DEAL_DATA_Listener_FLAG);
			if (dealDataListener instanceof List) {
				List list = (List) dealDataListener;
				for (int i = 0; i < list.size(); i++) {
					if (list.get(i) == null || list.get(i).equals(""))
						continue;
					Class cls = Class.forName(((String) list.get(i)).trim());
					Object tempObj = cls.newInstance();
					((PacketListener)tempObj).init(this);
					listenerList.add(tempObj);
				}
			} else if (dealDataListener instanceof String) {
				String dealDataListenerStr = (String) dealDataListener;
				if (dealDataListenerStr != null
						&& !dealDataListenerStr.equals("")) {
					Class cls = Class.forName(dealDataListenerStr.trim());
					Object tempObj = cls.newInstance();
					((PacketListener)tempObj).init(this);
					listenerList.add(tempObj);
				}
			}
			boolean isSuccess = createDatagramSocket(localIp, localPort);
			if (isSuccess) {
				this.readThread = new DetectThread();
				this.readThread.start();
			}
			this.isRunning = true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;

		}
		return true;
	}

	public void stopServer() {
		if(server != null){
			server.close();
			server = null;
		}
		if(this.readThread != null){
			this.readThread.stopThread();
		}
		listenerList.clear();
		this.isRunning = false;
	}

	private boolean createDatagramSocket(String localIp, String localPort)
			throws Exception {
		try {
			if (localPort != null && !localPort.trim().equals("")) {
				server = new DatagramSocket(Integer.parseInt(localPort.trim()),
						localIp==null?null:InetAddress.getByName(localIp.trim()));
				System.out.println("udp端口["+localPort+"]开启成功!");
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		return false;
	}

	public boolean sendData(byte[] buff) {
		if ( destPort== null || destPort.trim().equals(""))
			return false;
		try {
			int port = Integer.parseInt(destPort.trim());
			return sendData(buff, destIp, port);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public boolean sendData(byte[] buff, String destIp, int destPort) {
		try{
			if(sendLocalPort != null && !sendLocalPort.equals(localPort)){
				if(sendServer == null){
					sendServer = new DatagramSocket(Integer.parseInt(sendLocalPort.trim()),
							InetAddress.getByName(localIp));
				}
			}else {
				if(server == null){
					sendServer = new DatagramSocket(0,InetAddress.getByName(localIp));
				}else{
					sendServer = server;
				}
			}
		}
		catch(Exception e){
			e.printStackTrace();
			return false;
		}
		return sendData(buff, destIp, destPort, sendServer);
	}

	public static boolean sendData(byte[] buff, String destIp, int destPort,
			DatagramSocket server) {
		try {
//			System.out.println("发送数据从：" + server.getLocalAddress() + ":"
//					+ server.getLocalPort() + " 到" + destIp + ":" + destPort);
			if (server == null || destIp == null || destIp.trim().equals("")
					|| buff == null || server == null || server.isClosed()) {
				return false;
			}
			DatagramPacket packet = new DatagramPacket(buff, buff.length);
			packet.setAddress(InetAddress.getByName(destIp.trim()));
			packet.setPort(destPort);
			server.send(packet);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public boolean addListener(PacketListener listener) {
		if (!this.isRunning)
			return false;
		listenerList.add(listener);
		return true;
	}

	private class DetectThread extends BasicTask {
		private boolean isStop = false;

		private String poolName = null;
		public void start(){
			try{
				this.poolName = "Fleety Udp Server["+FleetyUdpServer.this.hashCode()+"] Detect Thread";
				PoolInfo pInfo = new PoolInfo(ThreadPool.SINGLE_TASK_LIST_POOL,1,1,false);
				ThreadPool pool = ThreadPoolGroupServer.getSingleInstance().createThreadPool(poolName, pInfo);
				pool.addTask(this);
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		public boolean execute() throws Exception{
			try{
				this.run();
			}catch(Throwable e){
				e.printStackTrace();
			}finally{
				ThreadPoolGroupServer.getSingleInstance().removeThreadPool(this.poolName);
			}
			return true;
		}
		
		public void run() {
			while (!isStop) {
				try {
					byte[] buffer = new byte[buffer_size];
					while (true) {
						DatagramPacket packet = new DatagramPacket(buffer,
								buffer_size);
						server.receive(packet);

						if (listenerList != null) {
							synchronized (listenerList) {
								for (Iterator itr = listenerList.iterator(); itr
										.hasNext();) {
									((PacketListener) itr.next())
											.eventHappen(packet);
								}
							}
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				} catch (Throwable t) {
					System.err.println("should not happen!" + t.getMessage());
				}
			}
		}

		public void stopThread() {
			ThreadPoolGroupServer.getSingleInstance().removeThreadPool(this.poolName);
			this.isStop = true;
		}
	}
	
	public static void main(String[] argv){
		try{
			FleetyUdpServer server = new FleetyUdpServer();
			server.addPara(FleetyUdpServer.LOCALIP_FLAG, "192.168.0.116");
			server.addPara(FleetyUdpServer.LOCALPORT_FLAG, "19790");
			System.out.println(server.startServer());
			server.addListener(new PacketListener(){
				public void init(Object obj){}
				public void eventHappen(DatagramPacket packet){
					byte[] data = packet.getData();
					System.out.println(data[0]+" "+data[9]);
				}
			});
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
