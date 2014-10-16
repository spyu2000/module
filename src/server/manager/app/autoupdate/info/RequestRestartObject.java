package server.manager.app.autoupdate.info;

public class RequestRestartObject implements IObjForInter {
	private String appName = null;
	
	public RequestRestartObject(String appName){
		this.appName = appName;
	}
	
	public String getAppName(){
		return this.appName;
	}

	public String getInfoType() {
		return "S2C_request_restart";
	}

}
