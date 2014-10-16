package server.manager.app.autoupdate.server;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Properties;

import com.fleety.base.Util;
import com.fleety.util.pool.timer.FleetyTimerTask;
import com.fleety.util.pool.timer.TimerPool;

import server.manager.app.autoupdate.info.IObjForInter;
import server.manager.app.autoupdate.info.RequestUpdateObject;
import server.manager.app.autoupdate.info.ResponseUpdateObject;
import server.manager.app.autoupdate.server.AutoUpdateServer.AppInfo;
import server.socket.help.ObjectReader;
import server.socket.inter.CmdInfo;
import server.socket.inter.ConnectSocketInfo;
import server.socket.inter.ICmdReleaser;
import server.threadgroup.ThreadPoolGroupServer;

public class CmdReleaser implements ICmdReleaser {
	private AutoUpdateServer server = null;
	private String appVersionFileName = "mapping/appVersion.map";


	private Properties appVersionMapping = new Properties();
	
	public void init(Object caller) {
		this.server = (AutoUpdateServer)caller;
		
		byte[] data = Util.loadFileWithSecurity(appVersionFileName);
		if(data != null){
			ByteArrayInputStream in = new ByteArrayInputStream(data);
			try{
				this.appVersionMapping.loadFromXML(in);
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		
		TimerPool timer = ThreadPoolGroupServer.getSingleInstance().createTimerPool("save_timer");
		timer.schedule(new FleetyTimerTask(){
			public void run(){
				try{
					ByteArrayOutputStream out = new ByteArrayOutputStream(1024*1024);
					appVersionMapping.storeToXML(out, "stop client app version mapping","GB2312");
					
					Util.saveFileWithSecurity(out.toByteArray(), appVersionFileName);
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		}, 600000, 600000);
	}
	
	public void releaseCmd(CmdInfo info) {
		Object cmd = info.getInfo(CmdInfo.CMD_FLAG);
		if(cmd == CmdInfo.SOCKET_CONNECT_CMD){
			try{
				ConnectSocketInfo conn = (ConnectSocketInfo)info.getInfo(CmdInfo.SOCKET_FLAG);
				conn.setMinCachSize(1024*1024);
				conn.switchSendMode2Thread(16*1024*1024);
			}catch(Exception e){}
		}else if(cmd == CmdInfo.SOCKET_DISCONNECT_CMD){
			
		}else{
			Object obj = info.getInfo(CmdInfo.DATA_FLAG);
			if(obj instanceof IObjForInter){
				if(obj instanceof RequestUpdateObject){
					this.disposeRequestUpdate(info,(RequestUpdateObject)obj);
				}
			}else{
				return ;
			}
		}
	}

	private void disposeRequestUpdate(CmdInfo info,RequestUpdateObject request){
		this.server.loadUpdateInfo();
		
		String appName = request.getAppName();
		String flag = request.getFlag();
		if(appName == null || flag == null){
			return ;
		}
		appName = appName.trim();
		flag = flag.trim();

		ConnectSocketInfo conn = (ConnectSocketInfo)info.getInfo(CmdInfo.SOCKET_FLAG);
		AppInfo appInfo = this.server.getAppInfo(appName);
		if(appInfo == null){
			System.out.println("Error AppName:"+appName);
			conn.destroy();
			return ;
		}
		boolean isUpdate = appInfo.needUpdate(request.getVersion());
		
		this.appVersionMapping.setProperty(appName+" ___ "+flag,request.getVersion());
		
		ResponseUpdateObject response = new ResponseUpdateObject(isUpdate,appName,flag,request.getVersion(),appInfo.getVersion(),isUpdate?appInfo.getData():null);

		System.out.println("App Update:flag="+flag+";appName="+appName+";version="+request.getVersion()+";isUpdate="+isUpdate+";size="+appInfo.getData().length);
		try{
			byte[] objData = ObjectReader.object2ByteArr(response, appInfo.getData().length + 1024*1024);
			
			conn.writeData(objData, 0, objData.length);
		}catch(Exception e){
			e.printStackTrace();
			if(conn != null){
				conn.destroy();
			}
		}
	}
}
