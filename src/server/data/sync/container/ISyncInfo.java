package server.data.sync.container;

import java.io.Serializable;

public interface ISyncInfo extends Serializable {
	/**
	 * 得到该对象所属上级，包括自己在SyncDataContainer中的key描述
	 * SyncDataContainer对象本身无key
	 * @return
	 */
	public Serializable[] getFlagArr();
}
