package server.tts;

import java.io.FileOutputStream;
import java.util.HashMap;

import server.socket.serversocket.FleetySocketServer;

public class Text2AudioServer extends FleetySocketServer {
	public static final int WAV_AUDIO_FORMAT = 1;
	public static final int MP3_AUDIO_FORMAT = 2;
	
	private static Text2AudioServer singleInstance = new Text2AudioServer();
	public static Text2AudioServer getSingleInstance(){
		return singleInstance;
	}
	
	private String langName = "default";
	private HashMap userMaping = new HashMap();
	public boolean startServer() {
		try{
			System.loadLibrary("Text2AudioServer");
		}catch(Throwable e){
			e.printStackTrace();
			return false;
		}
		
		String userStr = this.getStringPara("user");
		if(userStr != null){
			String[] userStrArr = userStr.split(";"),arr;
			for(int i=0;i<userStrArr.length;i++){
				arr = userStrArr[i].split(",");
				if(arr.length == 2){
					this.userMaping.put(arr[0], arr[1]);
				}
			}
		}
		
		if(this.getStringPara("lang_name") != null){
			this.langName = this.getStringPara("lang_name").trim();
		}
		
		this.isRunning = super.startServer();
		return true;
	}


	public byte[] text2Audio(String text,int audioFormat){
		return this.text2Audio(text, audioFormat, this.langName);
	}
	public synchronized native byte[] text2Audio(String text,int audioFormat,String language);
	
	
	public boolean validUser(String account,String pwd){
		Object value = this.userMaping.get(account);
		if(value == null){
			return false;
		}
		return value.equals(pwd);
	}
	
	public static void main(String[] argv) throws Exception{
		String langName = "VW Lily";
		langName = "VW Liang";
		System.loadLibrary("Text2AudioServer");
		
		long t = System.currentTimeMillis();
		byte[] data = Text2AudioServer.getSingleInstance().text2Audio("徐新，你好，这是一个测试数据.徐新，你好，这是一个测试数据.徐新，你好，这是一个测试数据.徐新，你好，这是一个测试数据.", MP3_AUDIO_FORMAT, langName);
		System.out.println("len"+data.length);
		FileOutputStream out = new FileOutputStream("c:/tts.mp3");
		out.write(data);
		out.close();
		System.out.println(System.currentTimeMillis()-t);
		t = System.currentTimeMillis();

		data = Text2AudioServer.getSingleInstance().text2Audio("hello,我们出发了", MP3_AUDIO_FORMAT, langName);
		System.out.println("len"+data.length);
		out = new FileOutputStream("c:/tts2.mp3");
		out.write(data);
		out.close();
		System.out.println(System.currentTimeMillis()-t);
		
		t = System.currentTimeMillis();
		data = Text2AudioServer.getSingleInstance().text2Audio("徐新，你好，这是一个测试数据.徐新，你好，这是一个测试数据.徐新，你好，这是一个测试数据.徐新，你好，这是一个测试数据.徐新，你好，这是一个测试数据.徐新，你好，这是一个测试数据.徐新，你好，这是一个测试数据.徐新，你好，这是一个测试数据.徐新，你好，这是一个测试数据.徐新，你好，这是一个测试数据.徐新，你好，这是一个测试数据.徐新，你好，这是一个测试数据.徐新，你好，这是一个测试数据.徐新，你好，这是一个测试数据.徐新，你好，这是一个测试数据.徐新，你好，这是一个测试数据.", MP3_AUDIO_FORMAT, langName);
		System.out.println("len"+data.length);
		out = new FileOutputStream("c:/tts1.mp3");
		out.write(data);
		out.close();
		System.out.println(System.currentTimeMillis()-t);
		t = System.currentTimeMillis();
		
		System.out.println("Finish");
		Thread.sleep(10000);
	}
}
