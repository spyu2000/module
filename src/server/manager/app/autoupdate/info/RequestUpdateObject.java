package server.manager.app.autoupdate.info;

public class RequestUpdateObject implements IObjForInter {
	private String appName = null;
	private String version = null;
	private String flag = null;
	
	public RequestUpdateObject(String appName, String version, String flag){
		this.appName = appName;
		this.version = version;
		if(this.version == null){
			this.version = "null";
		}
		this.flag = flag;
	}
	
	public String getAppName(){
		return this.appName;
	}
	
	public String getVersion() {
		return version;
	}

	public String getFlag() {
		return flag;
	}

	public String getInfoType() {
		return "C2S_request_update";
	}

}
