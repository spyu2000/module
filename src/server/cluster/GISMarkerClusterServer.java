package server.cluster;

import com.fleety.server.BasicServer;

public class GISMarkerClusterServer extends BasicServer {
	
	public boolean startServer() {
		
		return this.isRunning();
	}

	public GISMarkClusterInstance getMarkClusterInstance(){
		return new GISMarkClusterInstance(this.getIntegerPara("gridSize").intValue(),this.getBooleanPara("averageCenter").booleanValue());
	}
}
