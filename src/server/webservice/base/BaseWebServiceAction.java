package server.webservice.base;

public abstract class BaseWebServiceAction implements IWebServiceAction {
	/**
	 * 真实业务逻辑处理行为
	 * @param cmd
	 * @param para
	 * @return
	 */
	public abstract String executeAction(String cmd, String para);

}
