package server.webservice.base;

public abstract class BaseWebServiceAction implements IWebServiceAction {
	/**
	 * ��ʵҵ���߼�������Ϊ
	 * @param cmd
	 * @param para
	 * @return
	 */
	public abstract String executeAction(String cmd, String para);

}
