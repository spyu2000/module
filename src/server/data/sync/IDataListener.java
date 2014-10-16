package server.data.sync;

public interface IDataListener {
	/**
	 * 处理发布的数据，放置数据到对应的数据结构
	 * @param type  数据类型
	 * @param data  数据对象
	 */
	public void receiveData(SyncObj obj);
}
