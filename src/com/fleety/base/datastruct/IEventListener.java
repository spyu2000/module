package com.fleety.base.datastruct;

public interface IEventListener {
	/**
	 * ���¼���Ҫ����ʱ������֪ͨ�����������true������ɼ������������false�����������ø��¼���ʵ������
	 * @param eventType    �¼�����
	 * @param paraInfoArr  �¼���صĶ��������Ϣ�����������¼�
	 * @param source       �¼����������Ķ���
	 * @return             true����ɼ�����flase�����������
	 */
	public boolean eventWillHappen(int eventType,Object[] paraInfoArr,Object source);
	
	/**
	 * �¼��������֪ͨ
	 * @param eventType    �¼�����
	 * @param paraInfoArr  �¼���صĶ��������Ϣ�����������¼�
	 * @param source       �¼����������Ķ���
	 */
	public void eventHappened(int eventType,Object[] paraInfoArr,Object source);
}
