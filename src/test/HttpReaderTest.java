/**
 * ¾øÃÜ Created on 2008-11-24 by edmund
 */
package test;

import java.net.Socket;
import com.fleety.base.Util;

public class HttpReaderTest
{
	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		// TODO Auto-generated method stub
		
		try{
			String info = "ÄãÃÇ¶ººÅ";
			byte[] a = Util.encodePost2HttpProtocol("1",info.getBytes(), 0, info.getBytes().length, "192.168.0.116", 1980);
			
			info = "fdsjkfdsjkdjf·ğµÃ½ÇÈößÇß´·ù¶Èfdsklfdjk ewruewii·ù¶ÈÈö  ·ù¶ÈÈö·ç¼Í¿ÛÂÃ¾Ófdsjkjfdksjdfksfjdksjfdksjkfdjskdfjsk";
			for(int i=0;i<14;i++){
				info += info;
			}
			System.out.println(info.getBytes().length);
			byte[] b = Util.encodePost2HttpProtocol("2",info.getBytes(), 0, info.getBytes().length, "192.168.0.116", 1980);
			
			info = "fdsfdsfds·ù¶ÈÈöfdd";
			byte[] c = Util.encodePost2HttpProtocol("3",info.getBytes(), 0, info.getBytes().length, "192.168.0.116", 1980);
			
			byte[] d = new byte[a.length + b.length + c.length];
			System.arraycopy(a, 0, d, 0, a.length);
			System.arraycopy(b, 0, d, a.length, b.length);
			System.arraycopy(c, 0, d, a.length+b.length, c.length);

			Socket socket = new Socket("192.168.0.116",1980);
			socket.getOutputStream().write(d);

			Thread.sleep(2000);
			
			info = "ÄãÃÇ¶ººÅ";
			a = Util.encodePost2HttpProtocol("4",info.getBytes(), 0, info.getBytes().length, "192.168.0.116", 1980);
			socket.getOutputStream().write(a);
			socket.getOutputStream().flush();
			
			Thread.sleep(10000);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
