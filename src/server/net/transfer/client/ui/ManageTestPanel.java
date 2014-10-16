package server.net.transfer.client.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JPanel;

import server.net.transfer.ITransfer;
import server.net.transfer.client.NetTransferClient;

import com.fleety.base.GeneralConst;
import com.fleety.base.ui.XjsButton;
import com.fleety.base.ui.XjsTextField;

public class ManageTestPanel extends JPanel implements ActionListener{
	private ITransfer transferClient = null;
	private SendAndReceiveMonitorServer server = null;
	
	public ManageTestPanel(SendAndReceiveMonitorServer server){
		this.server = server;
		this.transferClient = this.server.getNetTransferClient();
		if(this.transferClient instanceof NetTransferClient){
			this.initPanel();
		}
	}
	
	private JFileChooser chooser = null;
	private XjsTextField infoField = null;
	private XjsButton fileTestSendBtn = null;
	private void initPanel(){
		this.setLayout(null);
		this.infoField = new XjsTextField();
		this.infoField.setText("test-send");
		this.fileTestSendBtn = new XjsButton("文件发送测试");
		
		this.infoField.setBounds(20, 20, 400, 25);
		this.add(this.infoField);
		this.fileTestSendBtn.setBounds(420, 20, 100, 25);
		this.add(this.fileTestSendBtn);
		
		this.fileTestSendBtn.addActionListener(this);
		
		this.chooser = new JFileChooser();
		this.chooser.setApproveButtonText("确定");
		this.chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
	}
	
	public void actionPerformed(ActionEvent e){
		int op = this.chooser.showOpenDialog(this);
		if(op == JFileChooser.APPROVE_OPTION){
			File sendFile = this.chooser.getSelectedFile();
			if(sendFile.length() > 0){
				((NetTransferClient)this.transferClient).addUploadTask(sendFile, GeneralConst.ONE_DAY_TIME, this.infoField.getText());
			}
		}
	}
}
