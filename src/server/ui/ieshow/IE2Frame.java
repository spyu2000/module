/**
 * ¾øÃÜ Created on 2010-2-25 by edmund
 */
package server.ui.ieshow;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import javax.swing.JFrame;
import javax.swing.JPanel;

import chrriis.dj.nativeswing.swtimpl.components.JWebBrowser;

public class IE2Frame extends JFrame{
	private String urlPath = null;
	private ICommand command = null;
	private final JWebBrowser webBrowser = new JWebBrowser();
	public IE2Frame(String title,String imgResource){
		this(title,imgResource,null);
	}
	public IE2Frame(String title,String imgResource,ICommand command){
		super(title);
		if(imgResource != null){
		    this.setIconImage(Toolkit.getDefaultToolkit().getImage(IE2Frame.class.getClassLoader().getResource(imgResource)));
		}
		this.setCommand(command);
	}
	
	public void setCommand(ICommand command){
		this.command = command;
		if(this.command != null){
			this.command.setIECanvas(null);
		}
	}
	
	public void setUrlPath(String urlPath){
		this.urlChanged = true;
		this.urlPath = urlPath;
	}
	private void go(){
		try{
			if(this.urlPath != null && this.webBrowser != null && this.ieReady){
				this.ieReady = false;
				this.urlChanged = false;

			    this.webBrowser.navigate(this.urlPath);
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
		this.setBounds(0, 0, dim.width, dim.height-30);
		
		JPanel contentPanel = (JPanel)this.getContentPane();
		contentPanel.setLayout(new BorderLayout());
		contentPanel.add(this.webBrowser, BorderLayout.CENTER);
		this.webBrowser.setBarsVisible(false);
		
		this.go();
		
		if(this.command != null){
			this.command.setIECanvas(null);
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
    private boolean ieReady = true;
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
        	IE2Frame ieFrame = null;
            
            ieFrame = new IE2Frame("test",null);
            ieFrame.setUrlPath("http://www.sina.com.cn");
            ieFrame.init();
            ieFrame.setVisible(true);
    }
}
