package server.manager.app.autoupdate.info;

public class ResponseUpdateObject implements IObjForInter {
	private String appName = null;
	private boolean isUpdate = false;
	private String oldVersion = null;
	private String newVersion = null;
	private String flag = null;
	private byte[] jarData = null;

	public ResponseUpdateObject(boolean isUpdate,String appName,String flag,String oldVersion,String newVersion,byte[] jarData){
		this.isUpdate = isUpdate;
		this.appName = appName;
		this.flag = flag;
		this.oldVersion = oldVersion;
		this.newVersion = newVersion;
		this.jarData = jarData;
	}
	
	public boolean isUpdate(){
		return this.isUpdate;
	}
	public String getAppName(){
		return this.appName;
	}
	
	public String getOldVersion() {
		return oldVersion;
	}

	public String getNewVersion() {
		return newVersion;
	}

	public String getFlag() {
		return flag;
	}

	public byte[] getJarData() {
		return jarData;
	}

	public String getInfoType() {
		return "S2C_response_update";
	}
}
