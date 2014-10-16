package server.webservice.base;

public abstract class BaseWebService implements IWebService {

	protected BaseWebServiceAction action = null;

	/**
	 * ���ô�����Ϊ��ӿ�
	 * @param action
	 */
	public abstract void setAction(BaseWebServiceAction action);

	/**
	 * webService�������ʵ�ֵĽӿ�
	 * @param cmd
	 * @param para
	 * @return
	 */
	public abstract String executeCmd(String cmd, String para);

	/**
	 * ����Ϣת����������Ϊ��
	 * @param cmd
	 * @param para
	 * @return
	 */
	public String executeAction(String cmd, String para) {
		return this.action.executeAction(cmd, para);
	}
}
