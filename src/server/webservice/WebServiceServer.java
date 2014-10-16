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
	 * 1.自定义webService服务接口，必须实现IWebService接口
	 * 2.所有的自定义webServiceAction，必须继承BaseWebServiceAction抽象类
	 */
	private static WebServiceServer instance = null;

	public static WebServiceServer getSingleInstance() {
		if (instance == null) {
			instance = new WebServiceServer();
		}
		return instance;
	}

	public boolean startServer() {
		// webService服务ip
		String temp = this.getStringPara("ip");
		if (StrFilter.hasValue(temp)) {
			this.ip = temp;
		} else {
			return false;
		}
		// webService服务端口
		temp = this.getStringPara("port");
		if (StrFilter.hasValue(temp)) {
			try {
				this.port = Integer.parseInt(temp);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		// webService服务名路径
		temp = this.getStringPara("ws_path");
		if (StrFilter.hasValue(temp)) {
			this.wsPath = temp;
		}
		// webService服务实现类
		temp = this.getStringPara("service_cls_name");
		if (StrFilter.hasValue(temp)) {
			this.webServiceClassName = temp;
		}
		// webService行为处理类
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
	 * 开启webService服务
	 * 
	 * @return
	 */
	public boolean startWebService() {
		Provider provider = (Provider) FactoryFinder.find(
				"javax.xml.ws.spi.Provider",
				"com.sun.xml.internal.ws.spi.ProviderImpl");
		String wsUrl = "http://" + ip + ":" + port + wsPath;
		try {
			// 初始化自定义webService
			Class webServiceCls = Class.forName(webServiceClassName);
			BaseWebService webServiceObj = (BaseWebService) webServiceCls
					.newInstance();

			// 初始化自定义行为webServiceAction
			Class webServiceActionCls = BaseWebServiceAction.class;
			BaseWebServiceAction actionObj = (BaseWebServiceAction) Class
					.forName(actionClassName).newInstance();

			// 为自定义webService设置自定义行为类webServiceAction
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
