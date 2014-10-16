package server.net.transfer.client.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

import com.fleety.base.ui.XjsButton;
import com.fleety.base.ui.XjsTable;

import server.net.transfer.IDataListener;
import server.net.transfer.ITransfer;
import server.net.transfer.container.QueueContainer.QueueItemInfo;
import server.socket.inter.ConnectSocketInfo;

public class SendQueueStatusPanel extends JPanel implements IDataListener,ActionListener{
	private ITransfer transferClient = null;
	private SendAndReceiveMonitorServer server = null;
	public SendQueueStatusPanel(SendAndReceiveMonitorServer server){
		this.server = server;
		this.initPanel();
		this.transferClient = this.server.getNetTransferClient();
		if(this.transferClient != null){
			this.transferClient.addDataReceiveListener(this);
		}
		this.updateStatus();
	}
	
	private String[] colNameArr = new String[]{"序号","姓 名","数据信息","本地路径","传输进度(%)"};
	private XjsTable sendTable = null;
	private XjsButton cancelBtn = new XjsButton("取 消");
	private void initPanel(){
		this.setLayout(null);
		this.sendTable = new XjsTable(colNameArr);
		JScrollPane scrollPane = new JScrollPane(this.sendTable);
		this.sendTable.getColumn(colNameArr[0]).setPreferredWidth(60);
		this.sendTable.getColumn(colNameArr[1]).setPreferredWidth(80);
		this.sendTable.getColumn(colNameArr[2]).setPreferredWidth(220);
		this.sendTable.getColumn(colNameArr[3]).setPreferredWidth(350);
		this.sendTable.getColumn(colNameArr[4]).setPreferredWidth(120);
		scrollPane.setBounds(20, 20, 750, 500);
		this.add(scrollPane);
		
		this.cancelBtn.setBounds(350, 530, 80, 30);
		this.cancelBtn.addActionListener(this);
		this.add(this.cancelBtn);
	}

	public void actionPerformed(ActionEvent e){
		if(e.getSource() == this.cancelBtn){
			try{
				if(this.sendTable.getSelectedRow() >= 0){
					QueueItemInfo itemInfo = this.arr[this.sendTable.getSelectedRow()];
					if(JOptionPane.showConfirmDialog(this, "是否确认取消上传?","上传取消确认",JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION){
						this.transferClient.cancelTask(itemInfo);
						this.updateStatus();
					}
				}
			}catch(Exception ee){
				ee.printStackTrace();
			}
		}
	}
	
	private QueueItemInfo[] arr = null;
	private void updateStatus(){
		if(this.transferClient == null){
			return ;
		}
		this.clearTable();
		
		this.arr = this.transferClient.getSendQueueContainer().getAllQueueItemInfo();
		DefaultTableModel model = (DefaultTableModel)this.sendTable.getModel();
		String appInfo;
		String[] infoArr;
		String name,id;
		for(int i=0;i<arr.length;i++){
			appInfo = arr[i].appendInfo;
			if(appInfo != null){
				infoArr = appInfo.split("\n");
				if(infoArr.length > 0){
					id = infoArr[0];
				}else{
					id = "";
				}
				if(infoArr.length > 2){
					name = infoArr[2];
				}else{
					name = "";
				}
			}else{
				name = "";
				id = "";
			}
			
			model.addRow(new String[]{(i+1)+"",name,id,arr[i].name,arr[i].getProgress()+""});
		}
	}
	
	private void clearTable(){
		DefaultTableModel model = (DefaultTableModel)this.sendTable.getModel();
		while(model.getRowCount() > 0){
			model.removeRow(0);
		}
	}
	
	public void connect(ConnectSocketInfo connInfo,boolean isPrimary){}
	public void disconnect(ConnectSocketInfo connInfo,boolean isPrimary){}
	
	public void dataArrived(ITransfer transfer,QueueItemInfo dataInfo,int process){}
	private QueueItemInfo changedDataInfo = null;
	public void dataSended(ITransfer transfer,QueueItemInfo dataInfo,int process){
		if(this.transferClient == null){
			return ;
		}
		if(dataInfo.getQueueContainer() != this.transferClient.getSendQueueContainer()){
			return ;
		}
		this.changedDataInfo = dataInfo;
		SwingUtilities.invokeLater(new Runnable(){
			public void run(){
				if(arr != null && arr.length > 0){
					if(changedDataInfo == arr[0]){
						sendTable.setValueAt(changedDataInfo.getProgress()+"", 0, 4);
					}
				}
			}
		});
	}
	public void taskChanged(ITransfer transfer,QueueItemInfo dataInfo){
		if(dataInfo.getQueueContainer() != this.transferClient.getSendQueueContainer()){
			return ;
		}
		SwingUtilities.invokeLater(new Runnable(){
			public void run(){
				updateStatus();
			}
		});
	}
}
