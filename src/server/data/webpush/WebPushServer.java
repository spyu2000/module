package server.data.webpush;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONObject;

import com.fleety.base.GeneralConst;

import server.socket.serversocket.FleetySocketServer;


public class WebPushServer extends FleetySocketServer {

	public boolean startServer() {
		this.isRunning = super.startServer();

		UserSessionContainer.getSingleInstance();
		
		return this.isRunning();
	}
	

	public static void main(String[] argv) throws Exception{
		WebPushServer pushServer = new WebPushServer();
		pushServer.setServerName("web_push_server");
		pushServer.addPara(WebPushServer.SERVER_IP_FLAG, "0.0.0.0");
		pushServer.addPara(WebPushServer.SERVER_PORT_FLAG, 12345);
		pushServer.addPara(WebPushServer.CMD_READER_FLAG, "server.socket.help.HttpCmdReader");
		pushServer.addPara(WebPushServer.CMD_RELEASER_FLAG, "server.data.webpush.DataRequestReleaser");
		pushServer.addPara(WebPushServer.TIME_OUT_FLAG, "300000");
		pushServer.startServer();
		
		new Timer().schedule(new TimerTask(){
			int count = 1;
			public void run(){
				try{
					System.out.println("Release Msg");
					
					JSONObject alarm = null;
					if(Math.random() > 0.5){
						alarm = new JSONObject();
						alarm.put("cmd", "alarm");
						alarm.put("seq", ""+ count++);
						alarm.put("dest", "»¦A39759");
						alarm.put("type", "Óö½Ù");
						alarm.put("time", GeneralConst.YYYY_MM_DD_HH_MM_SS.format(new Date()));
						System.out.println(alarm.toString());
					}
					JSONObject location = null;
					if(Math.random() > 0.5){
						location = new JSONObject();
						location.put("cmd", "gps");
						location.put("seq", ""+ count++);
						location.put("lo", "121.2343");
						location.put("la", "31.5332");
						location.put("speed", "80");
						location.put("time", GeneralConst.YYYY_MM_DD_HH_MM_SS.format(new Date()));
						System.out.println(location.toString());
					}
					
					UserSession[] arr = UserSessionContainer.getSingleInstance().getAllUserSession();
					System.out.println("Session:"+arr.length);
					for(int i=0;i<arr.length;i++){
						System.out.println(arr[i].getUserFlag()+"  "+arr[i].getConnectTimes()+"  "+arr[i].getAccessTimes());
						if(alarm != null){
							arr[i].sendData(alarm.toString());
						}
						if(location != null){
							arr[i].sendData(location.toString());
						}
					}
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		}, 10000,10000);
	}
}
