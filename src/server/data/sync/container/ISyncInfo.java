package server.data.sync.container;

import java.io.Serializable;

public interface ISyncInfo extends Serializable {
	/**
	 * �õ��ö��������ϼ��������Լ���SyncDataContainer�е�key����
	 * SyncDataContainer��������key
	 * @return
	 */
	public Serializable[] getFlagArr();
}
