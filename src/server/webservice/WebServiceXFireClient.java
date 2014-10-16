package server.webservice;

import java.net.URL;
import org.codehaus.xfire.client.Client;
import org.codehaus.xfire.transport.http.CommonsHttpMessageSender;

import com.fleety.base.StrFilter;
import com.fleety.server.BasicServer;

public class WebServiceXFireClient extends BasicServer {

	private int timeOut = 10000;

	private String webServiceUrl = "http://localhost:9900/webservice/jsonWebServiceInterface?wsdl";

	private static WebServiceXFireClient instance = null;

	private Client client = null;

	private boolean isPrint = false;

	public static WebServiceXFireClient getSingleInstance() {
		if (instance == null) {
			instance = new WebServiceXFireClient();
		}
		return instance;
	}

	public boolean startServer() {
		String temp = this.getStringPara("web_service_url");
		if (StrFilter.hasValue(temp)) {
			this.webServiceUrl = temp;
		}
		temp = this.getStringPara("time_out");
		if (StrFilter.hasValue(temp)) {
			try {
				this.timeOut = Integer.parseInt(temp);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		this.init();
		this.isRunning = true;
		return this.isRunning;
	}

	/**
	 * 初始化client连接
	 * 
	 */
	private void init() {
		try {
			client = new Client(new URL(webServiceUrl));
			// 设置超时
			client.setProperty(CommonsHttpMessageSender.HTTP_TIMEOUT, timeOut
					+ "");
		} catch (Exception e) {
			e.printStackTrace();
			this.closeClient();
		}
	}

	/**
	 * 关闭client连接
	 * 
	 */
	public void closeClient() {
		if (client != null) {
			client.close();
		}
		client = null;
	}

	/**
	 * webService调用
	 * 
	 * @param cmd
	 * @param para
	 */
	public Object[] invokeWebService(String methodName, Object[] objArr)
			throws Exception {
		if (client == null) {
			init();
		}
		if (isPrint) {
			System.out.println("invoke:" + this.webServiceUrl + ",method:"
					+ methodName);
		}
		if (client == null) {
			if (isPrint) {
				System.out.println("连接失败");
			}
			throw new Exception("连接失败");
		}
		Object[] result = client.invoke(methodName, objArr);

		if (result == null) {
			if (isPrint) {
				System.out.println("result is null");
			}
			throw new Exception("调用失败");
		} else {
			if (isPrint) {
				if (result.length > 0) {
					System.out.println("recive result from:" + result[0]);
				}
			}
		}
		return result;
	}

	public static void main(String[] args) {
		String cmd = "TEST";
		Object[] objArr = new Object[2];
		objArr[0] = cmd;
		objArr[1] = null;
		try {
			WebServiceXFireClient.getSingleInstance().invokeWebService(
					"executeCmd", objArr);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			WebServiceXFireClient.getSingleInstance().closeClient();
		}
	}

}
