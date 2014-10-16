package server.webservice.base;

public abstract class BaseWebService implements IWebService {

	protected BaseWebServiceAction action = null;

	/**
	 * 设置处理行为类接口
	 * @param action
	 */
	public abstract void setAction(BaseWebServiceAction action);

	/**
	 * webService服务必须实现的接口
	 * @param cmd
	 * @param para
	 * @return
	 */
	public abstract String executeCmd(String cmd, String para);

	/**
	 * 将消息转交给处理行为类
	 * @param cmd
	 * @param para
	 * @return
	 */
	public String executeAction(String cmd, String para) {
		return this.action.executeAction(cmd, para);
	}
}
