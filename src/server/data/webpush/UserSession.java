package server.data.webpush;

import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.LinkedList;

import org.json.JSONObject;

import com.fleety.base.Util;
import com.fleety.base.xml.XmlNode;

import server.socket.inter.ConnectSocketInfo;

public class UserSession {
	public static final Object SESSION_FLAG = new Object();
	
	private String userFlag = null;
	private JSONObject optionalInfo = null;
	private ConnectSocketInfo connInfo = null;
	private long heartDuration = 120000;
	private long validDuration = 0;
	
	private Object userInfo = null;
	
	private int connectTimes = 0;
	private int accessTimes = 0;
	private long createTime = System.currentTimeMillis();
	private long activeTime = System.currentTimeMillis();
	
	private LinkedList dataList = new LinkedList();
	private int maxDataSize = 1000;
	
	public UserSession(String _userFlag,long _validDuration){
		this.userFlag = _userFlag;
		this.validDuration = _validDuration;
	}
	
	public synchronized void detectHeart(){
		long offset = System.currentTimeMillis() - this.activeTime;
		
		//如果达到心跳时长还保持着连接，则无数据方式释放连接，让客户端重新请求
		if(offset >= this.heartDuration){
			if(this.connInfo != null){
				this._sendData();
			}
		}
	}
	
	public boolean isValid(){
		long offset = System.currentTimeMillis() - this.activeTime;
		
		return offset < this.validDuration;
	}
	
	public String getUserFlag() {
		return userFlag;
	}
	
	public synchronized void updateConnectSocketInfo(ConnectSocketInfo _connInfo){
		this.connInfo = _connInfo;
		
		this.enscapeSendData();
	}
	
	public synchronized void sendData(String jsonStr){
		this.addData(jsonStr);
		
		this.enscapeSendData();
	}
	private void addData(String jsonStr){
		if(this.dataList.size() >= this.maxDataSize){
			this.dataList.remove(0);
		}
		this.dataList.add(jsonStr);
	}
	
	private void enscapeSendData(){
		if(this.connInfo == null){
			return ;
		}
		if(this.dataList.size() == 0){
			return ;
		}
		
		this._sendData();
	}
	private void _sendData(){
		XmlNode root = new XmlNode("push");
		for(Iterator itr = this.dataList.iterator();itr.hasNext();){
			root.addNode(new XmlNode("msg",(String)itr.next()));
		}
		StringBuffer strBuff = new StringBuffer(1024);
		strBuff.append("<?xml version=\"1.0\" encoding=\""+Charset.defaultCharset().displayName()+"\"?>\n\n");
		root.toXmlString(strBuff, "", false);
		try{
			byte[] sData = strBuff.toString().getBytes();
			sData = Util.encodeResponse2HttpProtocol("push", sData, 0, sData.length);
			this.connInfo.writeData(sData,0,sData.length);
			this.dataList.clear();
		}catch(Exception e){
			e.printStackTrace();
		}
		this.connInfo = null;
	}
	
	public void updateOptionalInfo(JSONObject optional){
		this.optionalInfo = optional;
	}
	public JSONObject getOptionalInfo(){
		return this.optionalInfo;
	}

	public Object getUserInfo() {
		return userInfo;
	}
	public void setUserInfo(Object userInfo) {
		this.userInfo = userInfo;
	}
	public int getConnectTimes() {
		return connectTimes;
	}
	public void addConnectTimes() {
		this.connectTimes++;
	}
	public int getAccessTimes() {
		return accessTimes;
	}
	public void addAccessTimes() {
		this.accessTimes++;
	}
	
	public long getActiveTime() {
		return activeTime;
	}
	public void updateActiveTime() {
		this.activeTime = System.currentTimeMillis();
	}

	public long getCreateTime() {
		return createTime;
	}
}
