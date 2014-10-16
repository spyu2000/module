package server.net.transfer;

import server.net.transfer.container.QueueContainer;
import server.net.transfer.container.QueueContainer.QueueItemInfo;

public interface ITransfer {
	public void addDataReceiveListener(IDataListener listener);
	public void newSendTaskArrived();
	
	public String getSavePath(QueueItemInfo itemInfo);
	
	public QueueContainer getSendQueueContainer();
	public QueueContainer getReceiveQueueContainer();
	public void cancelTask(QueueItemInfo itemInfo);
}
