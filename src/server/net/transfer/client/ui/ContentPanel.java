package server.net.transfer.client.ui;

import javax.swing.JTabbedPane;

public class ContentPanel extends JTabbedPane {
	private SendAndReceiveMonitorServer server = null;
	public ContentPanel(SendAndReceiveMonitorServer server){
		this.server = server;
	}
	public void initPanel(){
		this.addTab("���Ͷ���", new SendQueueStatusPanel(server));
		this.addTab("���ն���", new ReceiveQueueStatusPanel(server));
//		this.addTab("�������", new ManageTestPanel(server));
	}
}
