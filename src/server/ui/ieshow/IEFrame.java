/**
 * ¾øÃÜ Created on 2010-2-25 by edmund
 */
package server.ui.ieshow;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import javax.swing.JFrame;
import javax.swing.JPanel;
import nothome.mswindows.IECanvas;
import nothome.mswindows.IEEventListener;

public class IEFrame extends JFrame implements IEEventListener{
	private String urlPath = null;
	private IECanvas iePanel = null;
	private ICommand command = null;
	public IEFrame(String title,String imgResource){
		this(title,imgResource,null);
	}
	public IEFrame(String title,String imgResource,ICommand command){
		super(title);
		if(imgResource != null){
		    this.setIconImage(Toolkit.getDefaultToolkit().getImage(IEFrame.class.getClassLoader().getResource(imgResource)));
		}
		this.setCommand(command);
	}
	
	public void setCommand(ICommand command){
		this.command = command;
		if(this.command != null){
			this.command.setIECanvas(this.iePanel);
		}
	}
	
	public void setUrlPath(String urlPath){
		this.urlChanged = true;
		this.urlPath = urlPath;
	}
	private void go(){
		try{
			if(this.urlPath != null && this.iePanel != null && this.ieReady){
				this.ieReady = false;
				this.urlChanged = false;
				this.iePanel.setURL(this.urlPath);
				this.iePanel.resizeControl();
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public void init(){
		this.setResizable(false);
	    this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    this.pack();
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		this.setBounds(0, 0, dim.width, dim.height);
		
		this.iePanel = new IECanvas(false);
		this.iePanel.addIEEventListener(this);
		
		JPanel contentPanel = (JPanel)this.getContentPane();
		contentPanel.setLayout(new BorderLayout());
		contentPanel.add(this.iePanel, BorderLayout.CENTER);
		
		this.go();
		
		if(this.command != null){
			this.command.setIECanvas(this.iePanel);
		}
		
		this.setVisible(true);
		
		synchronized(this){
			if(!this.ieReady){
				try{
					this.wait(10000);
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		}
	}
	
	public void onStatusTextChange(String status) {
    }
    
    public void onTitleChange(String status) {
    }
    
    private boolean urlChanged = false;
    private boolean ieReady = false;
    public void onDocumentComplete(String status) {
		ieReady = true;
		
		if(urlChanged){
			this.go();
		}else{
			synchronized(this){
				this.notifyAll();
			}
		}
    }
    
    public void onBeforeNavigate2(String url) {
    }
    
    public void onNavigateComplete2(String status) {
    }
    
    public void onDownloadComplete() {
    }
    
    public void onProgressChange(int progress, int max) {
    }
    
    public void onCommandStateChange(int command, boolean enabled) {
    	
    }
    
    public void onQuit() {
    	
    }
    
    public boolean showContextMenu()  {
    	return true;
    }

	public void oneParamCallBack(String param1) {
		if(param1 == null){
			return ;
		}
		
		if(param1.equals("close")){
			System.exit(0);
		}
	}

	public void twoParamCallBack(String param1, String param2) {
		if(this.command != null){
			this.command.commandArrived(param1, param2);
		}
	}
    
    public static void main(String[] args){
        	IEFrame ieFrame = null;
            
            ieFrame = new IEFrame("test",null);
            ieFrame.setUrlPath("http://www.sina.com.cn");
            ieFrame.init();
            ieFrame.setVisible(true);
    }
}
