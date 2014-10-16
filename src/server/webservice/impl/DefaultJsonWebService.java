package server.webservice.impl;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import server.webservice.WebServiceServer;
import server.webservice.base.BaseWebService;
import server.webservice.base.BaseWebServiceAction;

@WebService(targetNamespace = "www.fleety.com", serviceName = "jsonWebServiceInterface")
@SOAPBinding(style = SOAPBinding.Style.RPC)
public class DefaultJsonWebService extends BaseWebService {

	/**
	 * ����ʵ�ֵķ�����������Ϊ������
	 */
	public void setAction(BaseWebServiceAction action) {
		this.action = action;
	}

	/**
	 * webService�ӿ�
	 * 
	 * @param cmd
	 * @param jsonPara
	 * @return
	 */
	@WebMethod(action = "", operationName = "executeCmd")
	@WebResult(partName = "set")
	public String executeCmd(@WebParam(name = "cmd")
	String cmd, @WebParam(name = "jsonPara")
	String jsonPara) {
		if (WebServiceServer.getSingleInstance().isPrint()) {
			System.out.println("receiveMsg,cmd:" + cmd + ",jsonPara:"
					+ jsonPara);
		}
		return this.executeAction(cmd, jsonPara);
	}
}
