package server.manager.app.autoupdate.client;

import java.io.File;
import java.io.FileOutputStream;

import server.manager.app.autoupdate.client.AutoUpdateClient.AppInfo;
import server.manager.app.autoupdate.info.IObjForInter;
import server.manager.app.autoupdate.info.RequestRestartObject;
import server.manager.app.autoupdate.info.ResponseUpdateObject;
import server.socket.inter.CmdInfo;
import server.socket.inter.ConnectSocketInfo;
import server.socket.inter.ICmdReleaser;

public class CmdReleaser implements ICmdReleaser {
	private AutoUpdateClient server = null;
	public void init(Object caller) {
		this.server = (AutoUpdateClient)caller;
	}
	
	public void releaseCmd(CmdInfo info) {
		Object cmd = info.getInfo(CmdInfo.CMD_FLAG);
		if(cmd == CmdInfo.SOCKET_CONNECT_CMD){
			try{
				ConnectSocketInfo conn = (ConnectSocketInfo)info.getInfo(CmdInfo.SOCKET_FLAG);
				conn.setMinCachSize(1024*1024);
				conn.switchSendMode2Thread(1024*1024);
			}catch(Exception e){}
		}else if(cmd == CmdInfo.SOCKET_DISCONNECT_CMD){
			
		}else{
			Object obj = info.getInfo(CmdInfo.DATA_FLAG);
			if(obj instanceof IObjForInter){
				if(obj instanceof ResponseUpdateObject){
					this.disposeResponseUpdate(info,(ResponseUpdateObject)obj);
				}else if(obj instanceof RequestRestartObject){
					this.disposeRestart((RequestRestartObject)obj);
				}
			}else{
				return ;
			}
		}
	}

	private void disposeRestart(RequestRestartObject response){
		String appName = response.getAppName();
		AppInfo appInfo = this.server.getAppInfo(appName);
		if(appInfo == null){
			return ;
		}
		
		appInfo.restart();
	}
	private void disposeResponseUpdate(CmdInfo info,ResponseUpdateObject response){
		String appName = response.getAppName();
		AppInfo appInfo = this.server.getAppInfo(appName);
		if(appInfo == null){
			return ;
		}
		if(!response.isUpdate()){
			if(!appInfo.detectExist()){
				System.out.println("Program Not Start,Will Start!");
				appInfo.startApp();
			}
			return ;
		}
		
		FileOutputStream out = null;
		try{
			File dir = new File("temp");
			dir.mkdirs();
			File jarFile = new File("temp/"+appName+".jar");
			out = new FileOutputStream(jarFile);
			out.write(response.getJarData(),0,response.getJarData().length);
			out.close();
			
			String appPath = appInfo.getPath();
			File f = new File(appPath);
			f.mkdirs();
			
			String cmdStr = "jar -xf "+jarFile.getAbsolutePath();
			System.out.println("Cmd Str:"+cmdStr);
			Process p = Runtime.getRuntime().exec(cmdStr,null,f);
			p.waitFor();
			if (p.exitValue() == 0){
				appInfo.restart();
				appInfo.updateVersion(response.getNewVersion());
				this.server.saveAppUpdateInfo();
			}else{
				System.out.println("Update Failure For Jar! ");
			}
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			try{
				if(out != null){
					out.close();
				}
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		
	}
}
