package server.net.transfer.server;

import java.io.File;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.JOptionPane;

import server.help.SingleServer;
import server.net.transfer.IDataListener;
import server.net.transfer.ITransfer;
import server.net.transfer.container.QueueContainer;
import server.net.transfer.container.QueueContainer.QueueItemInfo;
import server.socket.inter.ConnectSocketInfo;
import server.socket.serversocket.FleetySocketServer;

public class NetTransferServer extends FleetySocketServer implements ITransfer {
	private long t;
	private QueueContainer receiveContainer = null;
	private QueueContainer sendContainer = null;
	private DataDownloadHelper dataHelper = null;
	private File dir = new File("_receive_data_dir_");
	public boolean startServer() {
		SingleServer s = new SingleServer();
		s.addPara("single_port", this.getStringPara("single_port"));
		s.startServer();
		
		dir.mkdirs();
		
		try{
			this.sendContainer = new QueueContainer(this,new File("_server_send_queue_file_"));
			this.receiveContainer = new QueueContainer(this,new File("_server_receive_queue_file_"));
			
			this.dataHelper = new DataDownloadHelper(this);
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}
		
		this.addDataReceiveListener(new IDataListener(){
			public void connect(ConnectSocketInfo connInfo,boolean isPrimary){}
			public void disconnect(ConnectSocketInfo connInfo,boolean isPrimary){}
			
			public void dataArrived(ITransfer transfer,QueueItemInfo dataInfo,int progress){
				if(progress == 0){
					t = System.currentTimeMillis();
				}else if(progress == 100){
					System.out.println("ReceiveDuration:"+(System.currentTimeMillis()-t)+" "+dataInfo.appendInfo+" "+dataInfo.size+" "+dataInfo.name+" "+dataInfo.id);
				}
				
				System.out.println("Finish:"+dataInfo.appendInfo+" "+dataInfo.id+"  "+progress);
			}
			public void dataSended(ITransfer transfer,QueueItemInfo dataInfo,int process){}
			public void taskChanged(ITransfer transfer,QueueItemInfo dataInfo){}
		});

		this.addPara(FleetySocketServer.CMD_READER_FLAG, ServerCmdReader.class.getName());
		if(this.getPara(FleetySocketServer.CMD_RELEASER_FLAG) == null){
			this.addPara(FleetySocketServer.CMD_RELEASER_FLAG, ServerCmdReleaser.class.getName());
		}
		
		return super.startServer();
	}
	
	public void stopServer(){
		if(this.dataHelper != null){
			this.dataHelper.destroy();
		}
		super.stopServer();
	}

	
	private Vector listeners = new Vector();
	public void addDataReceiveListener(IDataListener listener){
		if(listener == null){
			return ;
		}
		
		if(listeners.contains(listener)){
			return ;
		}
		listeners.add(listener);
	}
	
	public void triggerReceiveProgress(QueueItemInfo itemInfo){
		IDataListener listener;
		synchronized(listeners){
			for(Iterator itr = listeners.iterator();itr.hasNext();){
				listener = (IDataListener)itr.next();
				listener.dataArrived(this, itemInfo, itemInfo.getProgress());
			}
		}
	}

	public QueueContainer getReceiveQueueContainer(){
		return this.receiveContainer;
	}
	public QueueContainer getSendQueueContainer(){
		return this.sendContainer;
	}
	public DataDownloadHelper getDataDownloadHelper(){
		return this.dataHelper;
	}
	public synchronized File getUniqueFile(){
		long t = System.currentTimeMillis();;
		File f;
		int count = 1;
		while(true){			
			f = new File(this.dir,t+"-"+count+".temp");
			if(!f.exists()){
				break;
			}
			count ++;
		}
		return f;
	}
	
	public String getSavePath(QueueItemInfo itemInfo){
		return null;
	}

	public void newSendTaskArrived(){
		
	}
	
	private IRemoteFileCapture remoteFileCapture = new DefaultRemoteFileCapture();
	public IRemoteFileCapture getRemoteFileCapture(){
		return this.remoteFileCapture;
	}
	public void setRemoteFileCapture(IRemoteFileCapture remoteFileCapture){
		this.remoteFileCapture = remoteFileCapture;
	}
	public void cancelTask(QueueItemInfo itemInfo){
		JOptionPane.showMessageDialog(null, "不支持取消操作!");
	}
}
