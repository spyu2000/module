package server.net.transfer;

import server.net.transfer.container.QueueContainer.QueueItemInfo;
import server.socket.inter.ConnectSocketInfo;

public interface IDataListener {
	public void connect(ConnectSocketInfo connInfo,boolean isPrimary);
	public void disconnect(ConnectSocketInfo connInfo,boolean isPrimary);
	
	public void dataArrived(ITransfer transfer,QueueItemInfo dataInfo,int process);
	public void dataSended(ITransfer transfer,QueueItemInfo dataInfo,int process);
	
	public void taskChanged(ITransfer transfer,QueueItemInfo dataInfo);
}
