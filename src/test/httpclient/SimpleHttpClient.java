package test.httpclient;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;








import java.util.concurrent.TimeUnit;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HeaderElementIterator;
import org.apache.http.HeaderIterator;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.ProtocolVersion;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.DefaultConnectionReuseStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultConnectionKeepAliveStrategy;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.message.BasicHeaderElementIterator;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.util.EntityUtils;

public class SimpleHttpClient {
	
	/**
	 * 适合多线程的HttpClient,用httpClient4.2.1实现
	 * @return DefaultHttpClient
	 */
	public static DefaultHttpClient getHttpClient()
	{  		
	    // 设置组件参数, HTTP协议的版本,1.1/1.0/0.9 
		HttpParams params = new BasicHttpParams(); 
	    HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1); 
	    HttpProtocolParams.setUserAgent(params, "HttpComponents/1.1"); 
	    HttpProtocolParams.setUseExpectContinue(params, true); 	  

	    //设置连接超时时间 
	    int REQUEST_TIMEOUT = 10*1000;	//设置请求超时10秒钟 
		int SO_TIMEOUT = 10*1000; 		//设置等待数据超时时间10秒钟 
		long CONN_MANAGER_TIMEOUT=500L;//获取连接超时时间
	    params.setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, REQUEST_TIMEOUT);  
	    params.setParameter(CoreConnectionPNames.SO_TIMEOUT, SO_TIMEOUT); 
	    params.setLongParameter(ClientPNames.CONN_MANAGER_TIMEOUT, CONN_MANAGER_TIMEOUT);
	    params.setBooleanParameter(CoreConnectionPNames.STALE_CONNECTION_CHECK, true);
	    
		//设置访问协议 
		SchemeRegistry schreg = new SchemeRegistry();  
		schreg.register(new Scheme("http",80,PlainSocketFactory.getSocketFactory())); 
		schreg.register(new Scheme("https", 443, SSLSocketFactory.getSocketFactory())); 	  
		
		//多连接的线程安全的管理器 
		PoolingClientConnectionManager pccm = new PoolingClientConnectionManager(schreg);
		pccm.setDefaultMaxPerRoute(100);	//每个主机的最大并行链接数 
		pccm.setMaxTotal(200);			//客户端总并行链接最大数    
		
		
		DefaultHttpClient httpClient = new DefaultHttpClient(pccm, params);  
		httpClient.setHttpRequestRetryHandler(new DefaultHttpRequestRetryHandler());
		httpClient.setReuseStrategy(new DefaultConnectionReuseStrategy());
		httpClient.setKeepAliveStrategy(new DefaultConnectionKeepAliveStrategy());
		new IdleConnectionMonitorThread(pccm);
		return httpClient;
	}
	
	public static void httpGet(){
		HttpResponse response = null;
		HttpEntity entity = null;
		try {
		  HttpGet get = new HttpGet();
		  String url = "http://hc.apache.org/";
		  get.setURI(new URI(url));
		  response = getHttpClient().execute(get);
		} catch (Exception e) {
		  //处理异常
		} finally {
		  if(response != null) { 
		    try {
				EntityUtils.consume(response.getEntity());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} //会自动释放连接
		  }
		}
	}

	public static void simpleHttpClient() {

		CloseableHttpClient httpClient = HttpClients.createDefault();
		HttpGet httpGet = new HttpGet("http://www.baidu.com");
		CloseableHttpResponse  response = null;
		try {
			response = httpClient.execute(httpGet);
			HttpEntity entity = response.getEntity();
			Header[] heads = response.getAllHeaders();
			for (Header header : heads) {
				System.out.println(header.toString());
			}
			System.out.println(EntityUtils.toString(entity));
			EntityUtils.consume(entity);
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			
			try {
				response.close();
				httpGet.releaseConnection();
				httpClient.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	public static void simpleHttpClientUrl() {
		CloseableHttpClient httpClient = HttpClients.createDefault();
		HttpGet httpGet = new HttpGet(
				"http://www.google.com/search?hl=en&q=httpclient&btnG=Google+Search&aq=f&oq=");

		try {
			URI uri = new URIBuilder().setScheme("http")
					.setHost("www.google.com").setPath("/search")
					.setParameter("hl", "en").setParameter("q", "httpclient")
					.setParameter("btnG", "Google+Search")
					.setParameter("aq", "f").build();
			HttpGet get = new HttpGet(uri);

			System.out.println(get.getURI());
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static void simpleHttpResponse() {
		HttpResponse response = new BasicHttpResponse(HttpVersion.HTTP_1_1,
				HttpStatus.SC_OK, "OK");

		System.out.println(response.getProtocolVersion());

		System.out.println(response.getStatusLine().getStatusCode());

		System.out.println(response.getStatusLine().getReasonPhrase());

		System.out.println(response.getStatusLine().toString());
	}

	public static void simpleHttpResponseHead() {
		HttpResponse response = new BasicHttpResponse(
				org.apache.http.HttpVersion.HTTP_1_1,
				org.apache.http.HttpStatus.SC_OK, "OK");
		response.addHeader("Set-Cookie","c1=a;path=/;domain=localhost");
		response.addHeader("Set-Cookie","c2=b;path=\"/\";domain=\"localhost\"");
		
		Header header1=response.getFirstHeader("Set-Cookie");
		Header header2=response.getLastHeader("Set-Cookie");
		
		Header[] heads=response.getHeaders("Set-Cookie");
		
		System.out.println(heads.length);
		
		HeaderIterator itr=response.headerIterator("Set-Cookie");
		HeaderElementIterator itr1=new BasicHeaderElementIterator(response.headerIterator("Set-Cookie"));
		
		HeaderElement element=null;
		while(itr1.hasNext()){
			element= itr1.nextElement();
			System.out.println(element.getName()+"    "+element.getValue());
			
			NameValuePair[] params=element.getParameters();
			for (int i = 0; i < params.length; i++) {  

		        System.out.println(" " + params[i]);  

		    }  
			
		}
	}

	public static void main(String[] args) {
		SimpleHttpClient.simpleHttpClient();
		SimpleHttpClient.simpleHttpClientUrl();
		SimpleHttpClient.simpleHttpResponse();
		SimpleHttpClient.simpleHttpResponseHead();

	}
}
