/**
 * ���� Created on 2007-12-6 by edmund
 */
package com.fleety.server;

public interface IServer{
	public void setServerName(String serverName);
	public String getServerName();
	
	/**
	 * ��������ӿ�
	 */
	public boolean startServer();
	
	/**
	 * �رշ���ӿ�
	 */
	public void stopServer();
	
	/**
	 * �Ƿ������������
	 * @return true����������,false����δ����
	 */
	public boolean isRunning();
	
	/**
	 * ��Ӳ���
	 * @param key   ������ʶ
	 * @param value ����ֵ
	 */
	public void addPara(Object key,Object value);
	
	/**
	 * �õ�ָ����ʶ�Ĳ���ֵ
	 * @param key
	 * @return
	 */
	public Object getPara(Object key);
}
