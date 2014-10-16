/**
 * ¾øÃÜ Created on 2009-10-26 by edmund
 */
package test.yinni;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;
import com.fleety.base.FleetyThread;
import server.socket.inter.CmdInfo;
import server.socket.inter.ConnectSocketInfo;
import server.socket.inter.ICmdReleaser;

public class CmdReleaser implements ICmdReleaser{
	private Timer timer = new Timer();
	private static final Object TIMER_FLAG = new Object();
	
	public void init(Object caller){
		
	}

	public void releaseCmd(CmdInfo info){
		Object cmdObj = info.getInfo(CmdInfo.CMD_FLAG);
		ConnectSocketInfo socketInfo = (ConnectSocketInfo)info.getInfo(CmdInfo.SOCKET_FLAG);
		
		if(cmdObj == CmdInfo.SOCKET_CONNECT_CMD){
			System.out.println("connect");
			TimerTask task = new HeartTask(socketInfo);
			socketInfo.setInfo(TIMER_FLAG, task);
			this.timer.schedule(task, 60000,60000);
			
			new SendThread(socketInfo).start();
		}else if(cmdObj == CmdInfo.SOCKET_DISCONNECT_CMD){
			System.out.println("disconnect");
			TimerTask task = (TimerTask)socketInfo.getInfo(TIMER_FLAG);
			task.cancel();
		}else{
			int cmd = Integer.parseInt(cmdObj.toString());
			
			try{
				if(cmd == 0x01){
					this.disposeLogin(info);
				}else if(cmd == 0xFF){
					this.disposeHeart(info);
				}else{
					this.printData(cmd,info);
				}
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}
	
	private void disposeHeart(CmdInfo info) throws Exception{
		ByteBuffer data = (ByteBuffer)info.getInfo(CmdInfo.DATA_FLAG);
		System.out.println("year="+data.getShort());
		System.out.println("month="+data.get());
		System.out.println("date="+data.get());
		System.out.println("hour="+data.get());
		System.out.println("minute="+data.get());
		System.out.println("second="+data.get());
	}
	
	private void printData(int msg, CmdInfo info) throws Exception{
		ConnectSocketInfo socketInfo = (ConnectSocketInfo)info.getInfo(CmdInfo.SOCKET_FLAG);
		ByteBuffer data = (ByteBuffer)info.getInfo(CmdInfo.DATA_FLAG);
		
		int seq = data.get(0)&0xFF;
		StringBuffer buff = new StringBuffer(256);
		for(;data.hasRemaining();){
			buff.append(data.get()&0xFF);
			buff.append(" ");
		}
		System.out.println(buff.toString());
		
		if(msg == 0x02 || msg == 0xFE){
			
		}else{
			ByteBuffer resData = ByteBuffer.allocate(15);
			resData.order(ByteOrder.LITTLE_ENDIAN);
			resData.putInt(-1);
			resData.put((byte)0xFE);
			resData.putInt(2);
			resData.put((byte)msg);
			resData.put((byte)seq);
			resData.putInt(-1);
			
			resData.flip();
			socketInfo.writeData(resData);
		}
	}
	
	private void disposeLogin(CmdInfo info) throws Exception{
		ConnectSocketInfo socketInfo = (ConnectSocketInfo)info.getInfo(CmdInfo.SOCKET_FLAG);
		ByteBuffer data = (ByteBuffer)info.getInfo(CmdInfo.DATA_FLAG);
		
		int userNameLen = data.get()&0xFF;
		String userName = new String(data.array(),data.position(),userNameLen,"utf-8");
		data.position(data.position()+userNameLen);
		int userPwdLen = data.get()&0xFF;
		String userPwd = new String(data.array(),data.position(),userPwdLen,"utf-8");
		data.position(data.position()+userPwdLen);
		
		System.out.println(userName+","+userPwd);
		
		ByteBuffer buff = ByteBuffer.allocate(15+userNameLen);
		buff.order(ByteOrder.LITTLE_ENDIAN);
		buff.putInt(-1);
		buff.put((byte)0x01);
		buff.putInt(2+userNameLen);
		buff.put((byte)userNameLen);
		buff.put(userName.getBytes("utf-8"));
		buff.put((byte)0);
		buff.putInt(-1);
		
		
		buff.flip();
		socketInfo.writeData(buff);
	}
	
	private class SendThread extends FleetyThread{
		private ConnectSocketInfo socketInfo = null;
		public SendThread(ConnectSocketInfo socketInfo){
			this.socketInfo = socketInfo;	
		}
		
		public void run(){
			try{
				this.sleep(5000);
				System.out.println("send command!");
//				this.sendUpdateStatus();
				this.sendTask();
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		
		private void sendTask() throws Exception{
			String destNo = "»¦A34234";
			String taskName = "ÈÎÎñÃû³Æ";
			ByteBuffer buff = ByteBuffer.allocate(1024);
			buff.order(ByteOrder.LITTLE_ENDIAN);

			buff.putInt(-1);
			buff.put((byte)0x03);
			buff.putInt(0);
			buff.put((byte)0xA7);
			buff.put((byte)destNo.getBytes("utf-8").length);
			buff.put(destNo.getBytes("utf-8"));
			buff.putInt(478);
			buff.put((byte)taskName.getBytes("utf-8").length);
			buff.put(taskName.getBytes("utf-8"));

			Calendar calendar = Calendar.getInstance();
			
			buff.putShort((short)calendar.get(Calendar.YEAR));
			buff.put((byte)(calendar.get(Calendar.MONTH)+1));
			buff.put((byte)calendar.get(Calendar.DAY_OF_MONTH));
			buff.put((byte)calendar.get(Calendar.HOUR_OF_DAY));
			buff.put((byte)calendar.get(Calendar.MINUTE));
			buff.put((byte)calendar.get(Calendar.SECOND));

			buff.putShort((short)calendar.get(Calendar.YEAR));
			buff.put((byte)(calendar.get(Calendar.MONTH)+1));
			buff.put((byte)calendar.get(Calendar.DAY_OF_MONTH));
			buff.put((byte)calendar.get(Calendar.HOUR_OF_DAY));
			buff.put((byte)calendar.get(Calendar.MINUTE));
			buff.put((byte)calendar.get(Calendar.SECOND));
			
			buff.putShort((short)calendar.get(Calendar.YEAR));
			buff.put((byte)(calendar.get(Calendar.MONTH)+1));
			buff.put((byte)calendar.get(Calendar.DAY_OF_MONTH));
			buff.put((byte)calendar.get(Calendar.HOUR_OF_DAY));
			buff.put((byte)calendar.get(Calendar.MINUTE));
			buff.put((byte)calendar.get(Calendar.SECOND));
			
			buff.put((byte)5);

			buff.putShort((short)calendar.get(Calendar.YEAR));
			buff.put((byte)(calendar.get(Calendar.MONTH)+1));
			buff.put((byte)calendar.get(Calendar.DAY_OF_MONTH));
			buff.put((byte)(calendar.get(Calendar.HOUR_OF_DAY)+1));
			buff.put((byte)calendar.get(Calendar.MINUTE));
			buff.put((byte)calendar.get(Calendar.SECOND));
			
			buff.put((byte)10);
			
			buff.put((byte)3);
			
			for(int i=0;i<3;i++){
				buff.putShort((short)17);
				buff.put((byte)i);
				buff.putInt(1211234000);
				buff.putInt(215678000);

				buff.putShort((short)calendar.get(Calendar.YEAR));
				buff.put((byte)(calendar.get(Calendar.MONTH)+1));
				buff.put((byte)calendar.get(Calendar.DAY_OF_MONTH));
				buff.put((byte)(calendar.get(Calendar.HOUR_OF_DAY)+1));
				buff.put((byte)calendar.get(Calendar.MINUTE));
				buff.put((byte)calendar.get(Calendar.SECOND));
				
				buff.put((byte)(i+4));
			}
			
			
			buff.putInt(-1);
			
			buff.putInt(5,buff.position()-13);

			buff.flip();
			
			this.socketInfo.writeData(buff);
		}
		
		private void sendUpdateStatus() throws Exception{
			String destNo = "»¦A34234";
			ByteBuffer buff = ByteBuffer.allocate(16+destNo.getBytes("utf-8").length);
			buff.order(ByteOrder.LITTLE_ENDIAN);

			buff.putInt(-1);
			buff.put((byte)0x04);
			buff.putInt(buff.capacity()-13);
			buff.put((byte)4);
			buff.put((byte)destNo.getBytes("utf-8").length);
			buff.put(destNo.getBytes("utf-8"));
			buff.put((byte)1);
			
			buff.putInt(-1);

			buff.flip();
			
			this.socketInfo.writeData(buff);
		}
		
		private void sendOutJobQuery() throws Exception{
			ByteBuffer buff = ByteBuffer.allocate(24);
			buff.order(ByteOrder.LITTLE_ENDIAN);

			buff.putInt(-1);
			
			buff.put((byte)0x02);
			buff.putInt(11);
			buff.put((byte)3);
			buff.putInt(1211234000);
			buff.putInt(215678000);
			buff.putShort((short)500);
			
			buff.putInt(-1);
			
			buff.flip();
			
			this.socketInfo.writeData(buff);
		}
	}
	private class HeartTask extends TimerTask{
		private ConnectSocketInfo socketInfo = null;
		public HeartTask(ConnectSocketInfo socketInfo){
			this.socketInfo = socketInfo;	
		}
		public void run(){
			ByteBuffer buff = ByteBuffer.allocate(20);
			buff.order(ByteOrder.LITTLE_ENDIAN);
			
			Calendar calendar = Calendar.getInstance();
			
			buff.putInt(-1);
			buff.put((byte)0xFF);
			buff.putInt(7);
			buff.putShort((short)calendar.get(Calendar.YEAR));
			buff.put((byte)(calendar.get(Calendar.MONTH)+1));
			buff.put((byte)calendar.get(Calendar.DAY_OF_MONTH));
			buff.put((byte)calendar.get(Calendar.HOUR_OF_DAY));
			buff.put((byte)calendar.get(Calendar.MINUTE));
			buff.put((byte)calendar.get(Calendar.SECOND));
			buff.putInt(-1);
			
			buff.flip();
			
			try{
				socketInfo.writeData(buff);
			}catch(Exception e){
				e.printStackTrace();
				this.cancel();
			}
		}
	}
}
