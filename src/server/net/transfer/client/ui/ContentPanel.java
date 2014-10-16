package server.net.transfer.client.ui;

import javax.swing.JTabbedPane;

public class ContentPanel extends JTabbedPane {
	private SendAndReceiveMonitorServer server = null;
	public ContentPanel(SendAndReceiveMonitorServer server){
		this.server = server;
	}
	public void initPanel(){
		this.addTab("发送队列", new SendQueueStatusPanel(server));
		this.addTab("接收队列", new ReceiveQueueStatusPanel(server));
//		this.addTab("管理测试", new ManageTestPanel(server));
	}
}
