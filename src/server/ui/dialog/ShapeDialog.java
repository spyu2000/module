package server.ui.dialog;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseMotionListener;
import java.io.File;

import javax.imageio.ImageIO;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;

import com.fleety.server.BasicServer;
import com.sun.awt.AWTUtilities;

public class ShapeDialog extends BasicServer {
	private Object dialog = null;
	private int width = 800,height = 600;
	private Image img = null;
	private JPanel panel = null;
	public ShapeDialog(){
		
	}
	
	private boolean isSupport = false;
	public boolean startServer(){
		String tempStr;
		
		try{
			this.img = ImageIO.read(new File(this.getStringPara("bg_path")).toURI().toURL());
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}
		
		this.width = this.img.getWidth(null);
		this.height = this.img.getHeight(null);
		
		tempStr = this.getStringPara("width");
		if(tempStr != null && tempStr.trim().length() > 0){
			width = Integer.parseInt(tempStr.trim());
		}
		tempStr = this.getStringPara("height");
		if(tempStr != null && tempStr.trim().length() > 0){
			height = Integer.parseInt(tempStr.trim());
		}


		this.panel = new JPanel(){
			public void paintComponent(Graphics g){
				super.paintComponent(g);
				
				g.drawImage(img, 0, 0, width, height, this);
			}
		};
		this.panel.setLayout(null);
		MoveListener lis = new MoveListener();
		this.panel.addMouseListener(lis);
		this.panel.addMouseMotionListener(lis);
		
		this.isSupport = AWTUtilities.isTranslucencySupported(AWTUtilities.Translucency.TRANSLUCENT)&&AWTUtilities.isTranslucencySupported(AWTUtilities.Translucency.PERPIXEL_TRANSLUCENT);
		System.out.println(this.isSupport);
		
		tempStr = this.getStringPara("dialog");
		if(tempStr != null && tempStr.equals("false")){
			this.dialog = new JFrame();
			this.initFrame((JFrame)this.dialog);
		}else{
			this.dialog = new JDialog();
			this.initDialog((JDialog)this.dialog);
		}
		
		this.isRunning = true;
		return this.isRunning();
	}
	
	private void initFrame(JFrame dialog){
		dialog.setUndecorated(true);
		dialog.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		if(this.isSupport){
			AWTUtilities.setWindowOpaque(dialog, false);
		}
		dialog.setContentPane(this.panel);
		dialog.pack();
		
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		dialog.setBounds((dim.width-width)/2, (dim.height - height)/3, width, height);
		
		dialog.setVisible(true);
	}
	
	private void initDialog(JDialog dialog){
		dialog.setUndecorated(true);
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		
		if(this.isSupport){
			AWTUtilities.setWindowOpaque(dialog, false);
		}
		dialog.setContentPane(this.panel);
		dialog.pack();

		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		dialog.setBounds((dim.width-width)/2, (dim.height - height)/3, width, height);
		
		dialog.setVisible(true);
	}
	
	public JPanel getContentPanel(){
		return this.panel;
	}
	
	private class MoveListener extends MouseAdapter implements MouseMotionListener{
		private int x,y;
		private boolean isDrag = false;
		
		public void mousePressed(MouseEvent e){
			this.x = e.getX();
			this.y = e.getY();
			this.isDrag = true;
		}

		public void mouseReleased(MouseEvent e){
			this.isDrag = false;
		}
		public void mouseDragged(MouseEvent e){
			if(!this.isDrag){
				return ;
			}
			int cx = e.getX();
			int cy = e.getY();
			
			int ox = cx - x;
			int oy = cy - y;
			if(ox == 0 && oy == 0){
				return ;
			}
			
			if(dialog instanceof JFrame){
				Point p = ((JFrame)dialog).getLocation();
				((JFrame)dialog).setLocation(p.x+ox,p.y+oy);
			}else{
				Point p = ((JDialog)dialog).getLocation();
				((JDialog)dialog).setLocation(p.x+ox,p.y+oy);
			}
			
		}
		public void mouseMoved(MouseEvent e){
			
		}
	}
	
	public static void main(String[] argv){
		ShapeDialog dialog = new ShapeDialog();
		dialog.addPara("dialog", "false");
		dialog.addPara("bg_path", "./bg.png");
		dialog.startServer();
	}
}
