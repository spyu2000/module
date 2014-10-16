package server.data.webpush;

import org.json.JSONObject;

import server.socket.help.HttpCmdReader.HttpInfo;
import server.socket.inter.CmdInfo;
import server.socket.inter.ConnectSocketInfo;
import server.socket.inter.ICmdReleaser;

public class DataRequestReleaser implements ICmdReleaser {
	private	WebPushServer server = null;
	public void init(Object caller) {
		this.server = (WebPushServer)caller;
	}

	private static final String createPushRequest = "create_push_wait";
	private static final String destroyPushRequest = "destroy_push_wait";

	public void releaseCmd(CmdInfo info){
		try{
			this._releaseCmd(info);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	public void _releaseCmd(CmdInfo info) throws Exception{
		ConnectSocketInfo connInfo = (ConnectSocketInfo)info.getInfo(CmdInfo.SOCKET_FLAG);
		Object cmd = info.getInfo(CmdInfo.CMD_FLAG);
		if(cmd == null){
			return ;
		}
		
		if(cmd == CmdInfo.SOCKET_CONNECT_CMD){
			try{
				connInfo.switchSendMode2Thread(1024*1024);
			}catch(Exception e){
				e.printStackTrace();
			}
		}else if(cmd == CmdInfo.SOCKET_DISCONNECT_CMD){
			UserSession userSession = (UserSession)connInfo.getInfo(UserSession.SESSION_FLAG);
			if(userSession != null){
				userSession.updateConnectSocketInfo(null);
			}
		}else{
			if(cmd.equals(createPushRequest)){
				HttpInfo httpInfo = (HttpInfo)info.getInfo(CmdInfo.DATA_FLAG);
				String postInfo = new String(httpInfo.dataBuff.array(),0,httpInfo.dataBuff.limit());
				JSONObject root = (JSONObject)new JSONObject(postInfo);
				String userFlag = root.getString("flag");
				if(this.isValidUserFlag(userFlag)){
					UserSession userSession = UserSessionContainer.getSingleInstance().getUserSession(userFlag);
					try{
						if(root.isNull("opt")){
							userSession.updateOptionalInfo(null);
						}else{
							userSession.updateOptionalInfo(root.getJSONObject("opt"));
						}
						if(connInfo.getInfo(UserSession.SESSION_FLAG) == null){
							userSession.addConnectTimes();
						}
						connInfo.setInfo(UserSession.SESSION_FLAG, userSession);
						this.enscapeUserSession(userSession);
						
						
						userSession.addAccessTimes();
						userSession.updateActiveTime();
						
						userSession.updateConnectSocketInfo(connInfo);
					}catch(Exception e){
						e.printStackTrace();
						connInfo.destroy();
						return ;
					}
				}
			}else if(cmd.equals(destroyPushRequest)){
				/**
				 * JSON
				 * {flag:,opt:{}}
				 */
				HttpInfo httpInfo = (HttpInfo)info.getInfo(CmdInfo.DATA_FLAG);
				String postInfo = new String(httpInfo.dataBuff.array(),0,httpInfo.dataBuff.limit());
				JSONObject root = new JSONObject(postInfo);
				String userFlag = root.getString("flag");
				System.out.println("User Destroy User Session. "+userFlag);
				UserSessionContainer.getSingleInstance().removeSession(userFlag);
				connInfo.destroy();
			}
		}
	}
	
	public void enscapeUserSession(UserSession userSession) throws Exception{
		
	}
	
	public boolean isValidUserFlag(String userFlag){
		return true;
	}

}
