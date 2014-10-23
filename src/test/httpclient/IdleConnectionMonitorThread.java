package test.httpclient;

import java.util.concurrent.TimeUnit;

import org.apache.http.conn.ClientConnectionManager;

public class IdleConnectionMonitorThread extends Thread {

	private final ClientConnectionManager connMgr;
	private volatile boolean shutdown;

	public IdleConnectionMonitorThread(ClientConnectionManager connMgr) {
		super();
		this.setName("idle-connection-monitor");
		this.setDaemon(true);
		this.connMgr = connMgr;
		this.start();
	}

	@Override
	public void run() {
		try {
			while (!shutdown) {
				synchronized (this) {
					wait(5000);
					// Close expired connections
					connMgr.closeExpiredConnections();
					// Optionally, close connections
					// that have been idle longer than 30 sec
					connMgr.closeIdleConnections(60, TimeUnit.SECONDS);
				}
			}
		} catch (InterruptedException ex) {
			// terminate
		}
	}

	public void shutdown() {
		synchronized (this) {
			shutdown = true;
			notifyAll();
		}
	}

}
