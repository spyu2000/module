package server.net.transfer.client.ui;

import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRootPane;

import com.fleety.server.ServerContainer;

import server.net.transfer.ITransfer;
import server.tray.WindowTrayServer;

public class SendAndReceiveMonitorServer extends WindowTrayServer {
	public SendAndReceiveMonitorServer(){
		
	}
	
	public boolean startServer() {
		super.startServer();
		
		this.initFrame();
		
		this.isRunning = true;
		return this.isRunning();
	}

	private ContentPanel cPanel = null;
	private JFrame cfgFrame = null;
	private void initFrame(){
		this.cfgFrame = new JFrame(this.getStringPara("title")+" "+VersionInfo.CLIENT_VERSION);
		this.cfgFrame.setIconImage(Toolkit.getDefaultToolkit().getImage(this.getStringPara("icon")));
		
		this.cfgFrame.setResizable(false);
		this.cfgFrame.setUndecorated(true);
		this.cfgFrame.getRootPane().setWindowDecorationStyle(JRootPane.INFORMATION_DIALOG);
		this.cfgFrame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		this.cfgFrame.pack();
		
		this.cPanel = new ContentPanel(this);
		this.cfgFrame.setBounds(0, 0, 810, 640);
		this.cfgFrame.setContentPane(cPanel);
		
		this.cfgFrame.setVisible(false);
		
		cPanel.initPanel();
	}
	
	public ITransfer getNetTransferClient(){
		return (ITransfer)ServerContainer.getSingleInstance().getServer(this.getStringPara("transfer_client_name"));
	}
	
	private boolean isFirstShow = true;
	public void enableVisible(boolean isShow){
		if(isShow && isFirstShow){
			Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
			this.cfgFrame.setLocation((dim.width-this.cfgFrame.getWidth())/2, (dim.height-this.cfgFrame.getHeight())/2);
			
			isFirstShow = false;
		}
		this.cfgFrame.setVisible(isShow);
		this.cfgFrame.toFront();
	}
	public void switchPanel(int index){
		this.cPanel.setSelectedIndex(index);
	}
	public void addPanel(String name,JPanel panel){
		this.cPanel.addTab(name, panel);
	}
}
