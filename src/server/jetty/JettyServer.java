package server.jetty;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jetty.util.component.LifeCycle;
import org.eclipse.jetty.xml.XmlConfiguration;

import com.fleety.server.BasicServer;

public class JettyServer extends BasicServer {
	private ArrayList jettyList = new ArrayList(4);
	public synchronized boolean startServer() {
		if(this.isRunning()){
			return true;
		}
		try {
			this.initJetty();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		this.isRunning = true;
		return true;
	}

	private void initJetty() throws Exception {
		Object jettyObj = this.getPara("jetty");
		if (jettyObj == null) {
			return;
		}
		if (jettyObj instanceof List) {
			for (Iterator itr = ((List) jettyObj).iterator(); itr.hasNext();) {
				this.startJetty(itr.next().toString());
			}
		} else {
			this.startJetty(jettyObj.toString());
		}
	}

	private boolean startJetty(String jettyPath) throws Exception {
		URL url = null;
		if(jettyPath.indexOf("://") < 0){
			url = new File(jettyPath).toURI().toURL();
		}else{
			url = new URL(jettyPath);
		}

		InputStream in = url.openStream();
		try {
			XmlConfiguration configure = new XmlConfiguration(in);
			Object obj = configure.configure();
			if (obj instanceof LifeCycle) {
				LifeCycle lc = (LifeCycle) obj;
				if (!lc.isRunning()) {
					lc.start();
					this.jettyList.add(lc);
				}
			} else {
				throw new Exception("Error Jetty File! " + jettyPath);
			}
		} finally {
			if (in != null) {
				in.close();
			}
		}

		return true;
	}

	public synchronized void stopServer() {
		super.stopServer();
		LifeCycle lc = null;
		for(Iterator itr = this.jettyList.iterator();itr.hasNext();){
			lc = (LifeCycle)itr.next();
			if(lc.isRunning()){
				try{
					lc.stop();
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		}
		this.jettyList.clear();
	}

	public static void main(String[] argv){
		JettyServer server = new JettyServer();
		server.addPara("jetty", "jetty/jetty.xml");
		server.startServer();
	}
}
