package server.data.sync;

public interface IDataListener {
	/**
	 * �����������ݣ��������ݵ���Ӧ�����ݽṹ
	 * @param type  ��������
	 * @param data  ���ݶ���
	 */
	public void receiveData(SyncObj obj);
}
