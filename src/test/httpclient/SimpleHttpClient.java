package test.httpclient;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.HttpVersion;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.util.EntityUtils;

public class SimpleHttpClient {

	public static void simpleHttpClient() {

		CloseableHttpClient httpClient = HttpClients.createDefault();
		HttpGet httpGet = new HttpGet("http://www.baidu.com");
		HttpResponse response = null;
		try {
			response = httpClient.execute(httpGet);
			HttpEntity entity = response.getEntity();
			Header[] heads = response.getAllHeaders();
			for (Header header : heads) {
				System.out.println(header.toString());
			}
			System.out.println(EntityUtils.toString(entity));
		} catch (ClientProtocolException e) {

			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
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
	public static void simpleHttpResponse(){
		HttpResponse response=new BasicHttpResponse(HttpVersion.HTTP_1_1, HttpStatus.SC_OK,"OK");


				  

				System.out.println(response.getProtocolVersion());  

				System.out.println(response.getStatusLine().getStatusCode());  

				System.out.println(response.getStatusLine().getReasonPhrase());  

				System.out.println(response.getStatusLine().toString());  
				}

	public static void main(String[] args) {
		SimpleHttpClient.simpleHttpClient();
		SimpleHttpClient.simpleHttpClientUrl();
	}
}
