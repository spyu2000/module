package server.webservice;

import java.lang.reflect.Method;
import javax.xml.ws.spi.Provider;
import server.webservice.base.BaseWebService;
import server.webservice.base.BaseWebServiceAction;
import server.webservice.base.FactoryFinder;

import com.fleety.base.StrFilter;
import com.fleety.server.BasicServer;

public class WebServiceServer extends BasicServer {

	private String ip = "localhost";

	private int port = 9900;

	private String wsPath = "/webservice/jsonWebServiceInterface";

	private String webServiceClassName = "server.webservice.impl.DefaultJsonWebService";

	private String actionClassName = "server.webservice.impl.DefaultJsonWebServiceAction";

	private boolean isPrint = false;

	/**
	 * 1.�Զ���webService����ӿڣ�����ʵ��IWebService�ӿ�
	 * 2.���е��Զ���webServiceAction������̳�BaseWebServiceAction������
	 */
	private static WebServiceServer instance = null;

	public static WebServiceServer getSingleInstance() {
		if (instance == null) {
			instance = new WebServiceServer();
		}
		return instance;
	}

	public boolean startServer() {
		// webService����ip
		String temp = this.getStringPara("ip");
		if (StrFilter.hasValue(temp)) {
			this.ip = temp;
		} else {
			return false;
		}
		// webService����˿�
		temp = this.getStringPara("port");
		if (StrFilter.hasValue(temp)) {
			try {
				this.port = Integer.parseInt(temp);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		// webService������·��
		temp = this.getStringPara("ws_path");
		if (StrFilter.hasValue(temp)) {
			this.wsPath = temp;
		}
		// webService����ʵ����
		temp = this.getStringPara("service_cls_name");
		if (StrFilter.hasValue(temp)) {
			this.webServiceClassName = temp;
		}
		// webService��Ϊ������
		temp = this.getStringPara("action_cls_name");
		if (StrFilter.hasValue(temp)) {
			this.actionClassName = temp;
		}
		temp = this.getStringPara("is_print");
		this.isPrint = Boolean.parseBoolean(temp);

		this.isRunning = this.startWebService();
		return this.isRunning;
	}

	/**
	 * ����webService����
	 * 
	 * @return
	 */
	public boolean startWebService() {
		Provider provider = (Provider) FactoryFinder.find(
				"javax.xml.ws.spi.Provider",
				"com.sun.xml.internal.ws.spi.ProviderImpl");
		String wsUrl = "http://" + ip + ":" + port + wsPath;
		try {
			// ��ʼ���Զ���webService
			Class webServiceCls = Class.forName(webServiceClassName);
			BaseWebService webServiceObj = (BaseWebService) webServiceCls
					.newInstance();

			// ��ʼ���Զ�����ΪwebServiceAction
			Class webServiceActionCls = BaseWebServiceAction.class;
			BaseWebServiceAction actionObj = (BaseWebServiceAction) Class
					.forName(actionClassName).newInstance();

			// Ϊ�Զ���webService�����Զ�����Ϊ��webServiceAction
			Method md = webServiceCls.getMethod("setAction",
					webServiceActionCls);
			md.invoke(webServiceObj, actionObj);

			provider.createAndPublishEndpoint(wsUrl, webServiceObj);
			return true;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}

	public static void main(String[] args) {
		WebServiceServer test = new WebServiceServer();
		test.startWebService();
	}

	public boolean isPrint() {
		return isPrint;
	}
}
