package server.tray;

import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.TrayIcon.MessageType;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import server.threadgroup.ThreadPoolGroupServer;
import server.tray.listener.ICmdListener;


import com.fleety.server.BasicServer;
import com.fleety.util.pool.timer.FleetyTimerTask;

public class WindowTrayServer extends BasicServer {
	private static WindowTrayServer singleInstance = new WindowTrayServer();
	public static WindowTrayServer getSingleInstance(){
		return singleInstance;
	}
	private ArrayList listenerList = new ArrayList(4);
	private TrayIcon trayIcon = null;
	private PopupMenu menu = null;
	public boolean startServer() {
		Image img = Toolkit.getDefaultToolkit().getImage(this.getStringPara("icon"));
		String title = this.getStringPara("title");
		if(title == null){
			title = "";
		}
		
		
		
		List tempList = null;
		Object obj = this.getPara("listener");
		if(obj != null){
			if(obj instanceof String){
				tempList = new ArrayList(4);
				tempList.add(obj);
			}else{
				tempList = (List)obj;
			}
		}
		String str;
		if(tempList != null){
			for(Iterator itr = tempList.iterator();itr.hasNext();){
				str = (String)itr.next();
				try{
					obj = Class.forName(str).newInstance();
					if(obj instanceof ICmdListener){
						this.addListener((ICmdListener)obj);
					}
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		}
		
		
		obj = this.getPara("menu_item");
		if(obj != null){
			if(obj instanceof String){
				tempList = new ArrayList(4);
				tempList.add(obj);
			}else{
				tempList = (List)obj;
			}
		}
		if(tempList != null){
			menu = new PopupMenu();
			menu.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e){
					String cmd = e.getActionCommand();
					if(cmd != null){
						WindowTrayServer.this.triggerCmd(cmd);
					}
				}
			});
			int index;
			MenuItem item;
			for(Iterator itr = tempList.iterator();itr.hasNext();){
				str = (String)itr.next();
				index = str.indexOf(",");
				if(index > 0){
					item = new MenuItem(str.substring(index+1));
					item.setActionCommand(str.substring(0, index));
					menu.add(item);
				}
			}
		}
		if(menu.getItemCount() == 0){
			menu = null;
		}
		this.trayIcon = new TrayIcon(img,title,menu);
		this.trayIcon.setImageAutoSize(true);
		this.trayIcon.addMouseListener(new MouseAdapter(){
			public void mouseClicked(MouseEvent e) {
				if(e.getButton() == MouseEvent.BUTTON1){
					WindowTrayServer.this.triggerCmd(ICmdListener.SHOW_WINDOW_CMD);
				}
			}
		});
		try{
			SystemTray.getSystemTray().add(trayIcon);
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}
		
		this.isRunning = true;
		return this.isRunning();
	}
	
	public void addListener(ICmdListener listener){
		if(listener == null){
			return ;
		}
		if(this.listenerList.contains(listener)){
			return ;
		}
		
		listener.setTrayServer(this);
		this.listenerList.add(listener);
	}
	
	public void removeListener(ICmdListener listener){
		this.listenerList.remove(listener);
	}

	private FleetyTimerTask infoCancelTask = null;
	public void showInfo(String text){
		this.showInfo("Notify", text);
	}
	public void showInfo(String text,int showDuration){
		this.showInfo("Notify", text, showDuration);
	}
	public void showInfo(String caption,String text){
		this.showInfo(caption, text, 10);
	}
	public void showInfo(String caption,String text,int duration){
		this.showInfo(caption,text,TrayIcon.MessageType.INFO, duration);
	}
	public void showInfo(String caption,String text,MessageType infoType,int duration){
		this.trayIcon.displayMessage(caption, text, infoType);
		if(infoCancelTask != null){
			infoCancelTask.cancel();
			infoCancelTask = null;
			if(text != null && text.length() > 0){
				ThreadPoolGroupServer.getSingleInstance().createTimerPool("tracy_notify_timer").schedule(infoCancelTask = new FleetyTimerTask(){
					public void run(){
						WindowTrayServer.this.showInfo("");
					}
				}, duration*1000l);
			}
		}
	}
	
	private void triggerCmd(String cmd){
		ICmdListener listener;
		for(Iterator itr=this.listenerList.iterator();itr.hasNext();){
			listener = (ICmdListener)itr.next();
			listener.cmdHappened(cmd);
		}
	}
}
