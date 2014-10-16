/**
 * ¾øÃÜ Created on 2010-2-5 by edmund
 */
package test;

import server.socket.socket.FleetySocket;

public class SocketClientTest extends FleetySocket
{
	public boolean startServer(){
		super.startServer();
		
		try{
			Thread.sleep(1000);
			System.out.println("send");
			String str[] = new String[]{
					"*HQ20013813486839,AH&A0012263412030611712157470014050210&B0000000000&F0000#"
					,"*HQ20113685185337,AD1&A0012273418818711757641860000050210&B0000000000&C009=7#"
					,"*HQ20013852040637,AH&A0012363422658811820587060000050210&B0000000000&F0000#"
					,"*HQ20013852041092,AH&A0012363422684811820582660009050210&B0100000000&F0001#"
					,"*HQ20013813470832,AH&A0012393412034611712154070019050210&B0000000000&F0000#"
			};
			byte[] byteArr;
			for(int i=0;i<str.length;i++){
				byteArr = str[i].getBytes();
				this.sendData(byteArr,0,byteArr.length);
				Thread.sleep(1000);
			}
			Thread.sleep(100000000);
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return true;
	}
}
