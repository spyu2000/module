package nothome.mswindows;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.event.*;
import java.io.*;

import nothome.util.*;

public class IEDhtmlTester extends JFrame implements ActionListener, 
    IEEventListener {
    
    protected IECanvas      canvas;
    protected JTextField    jsText = new JTextField(10);
    
	protected JTextField    htmlId = new JTextField(5);
	protected JTextField    htmlId2 = new JTextField(5);
	protected JTextArea     htmlValue = new JTextArea(3, 10);
	protected JTextField    htmlClass = new JTextField(5);
	protected JButton       btnSetHtml = new JButton("Set innerHtml");
	protected JButton       btnGetHtml = new JButton("Get innerHtml");
	protected JButton       btnSetClass = new JButton("Set class");
	protected JButton       btnGetClass = new JButton("Get class");

	public class LightLabel extends JLabel {
		public LightLabel(String text) {
			super(text);
			this.setFont(this.getFont().deriveFont(Font.PLAIN));
		}
	}
	// This panel is uitable for use in a BoxLayout
	public class OneLinePanel extends JPanel
	{
		public OneLinePanel()
		{
			super(new BorderLayout());	
		}
		// return the preferred size for the maximum size
		public Dimension getMaximumSize() {
			Dimension size = getPreferredSize();
			size.width = Short.MAX_VALUE;
			return size;
		}
	}
	
    public IEDhtmlTester() {

        getContentPane().setLayout(new BorderLayout());
        setTitle("IEDhtmlTester");

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); 
    
        JPanel upperPanel = new JPanel();
        upperPanel.setLayout(new BoxLayout(upperPanel, BoxLayout.Y_AXIS));        
        getContentPane().add(upperPanel, BorderLayout.NORTH);

		int vgap = 10;
		
		upperPanel.add(Box.createVerticalStrut(vgap));

    
    	{
	    	// Create the panel for the javascript to java
	    	OneLinePanel pan = new OneLinePanel();
			upperPanel.add(pan);

			pan.setBorder(BorderFactory.createTitledBorder("Test1: Call Javascript from Java"));

	    	JLabel label = new LightLabel("Type in some javascript and press return:");
			pan.add(label, BorderLayout.WEST);
			pan.add(jsText, BorderLayout.CENTER);
			jsText.setText("alert('Hello')");
			jsText.addActionListener(this);
		}
    	
    	upperPanel.add(Box.createVerticalStrut(vgap));

		{
			// Create the panel for the javascript to java
			OneLinePanel pan = new OneLinePanel();
			upperPanel.add(pan);

			pan.setBorder(BorderFactory.createTitledBorder("Test2: Call Java from Javascript"));

			JLabel label = new LightLabel("Click on the button \"Javascript to Java\" button below:");
			pan.add(label, BorderLayout.WEST);
		}
    	
		upperPanel.add(Box.createVerticalStrut(vgap));

		{
			// Create the panel for the second test    	
			OneLinePanel pan = new OneLinePanel();
			upperPanel.add(pan);
			
			pan.setBorder(BorderFactory.createTitledBorder("Test3: Get/Set innerHtml"));

			JLabel label = new LightLabel("Press get/set to obtain/specify the innerHtml of the DIV below");
			pan.add(label, BorderLayout.NORTH);

			JPanel pan1 = new JPanel();
			pan1.add(new LightLabel("ID of the DIV:"));
			pan1.add(htmlId);
			htmlId.setText("Id1");
			pan1.add(new LightLabel(" InnerHtml:"));
			pan.add(pan1, BorderLayout.WEST);

			pan.add(new JScrollPane(htmlValue), BorderLayout.CENTER);

			JPanel pan3 = new JPanel(new FlowLayout(FlowLayout.CENTER));
			pan3.add(btnSetHtml);
			pan3.add(btnGetHtml);
			btnGetHtml.addActionListener(this);
			btnSetHtml.addActionListener(this);
			pan.add(pan3, BorderLayout.SOUTH);
		}

		upperPanel.add(Box.createVerticalStrut(vgap));

		
		{
			// Create the panel for the second test    	
			OneLinePanel pan = new OneLinePanel();
			upperPanel.add(pan);
			
			pan.setBorder(BorderFactory.createTitledBorder("Test3: Get/Set class"));

			JLabel label = new LightLabel("Press get/set to obtain/specify the class of the DIV below");
			pan.add(label, BorderLayout.NORTH);

			JPanel pan1 = new JPanel();
			pan1.add(new LightLabel("ID of the DIV:"));
			pan1.add(htmlId2);
			htmlId2.setText("Id1");
			pan1.add(new LightLabel(" ClassName:"));
			pan.add(pan1, BorderLayout.WEST);

			label = new LightLabel("Try 'GreenBox' or 'RedBox' or both");
			label.setFont(label.getFont().deriveFont(9.0f));
			pan.add(label, BorderLayout.EAST);
			htmlClass.setText("GreenBox");
			pan.add(htmlClass, BorderLayout.CENTER);

			JPanel pan3 = new JPanel(new FlowLayout(FlowLayout.CENTER));
			pan3.add(btnSetClass);
			pan3.add(btnGetClass);
			btnGetClass.addActionListener(this);
			btnSetClass.addActionListener(this);
			pan.add(pan3, BorderLayout.SOUTH);
		
		}
		upperPanel.add(Box.createVerticalStrut(10));
								
		canvas = new IECanvas();
		canvas.setSize(500,400);
		canvas.addIEEventListener(this);
        getContentPane().add(canvas, BorderLayout.CENTER);
        
        JPopupMenu.setDefaultLightWeightPopupEnabled(false);        
    }
    
    public void actionPerformed(ActionEvent evt) {
    	if (evt.getSource() == jsText) {
    		String js = jsText.getText();
    		canvas.executeJavascript(js);
    	}
		else if (evt.getSource() == btnSetHtml) {
			canvas.setInnerHtml(htmlId.getText(), htmlValue.getText());
		}
		else if (evt.getSource() == btnGetHtml) {
			htmlValue.setText(canvas.getInnerHtml(htmlId.getText()));
		}
		else if (evt.getSource() == btnSetClass) {
			canvas.setClassName(htmlId2.getText(), htmlClass.getText());
		}
		else if (evt.getSource() == btnGetClass) {
			htmlClass.setText(canvas.getClassName(htmlId2.getText()));
		}
    }
        
    public static void main(String[] argv) {
        IEDhtmlTester d = new IEDhtmlTester();
		
		d.pack();
		d.show();        
    }
    
    // IEEventListener impl.
    
    public void onStatusTextChange(String status) {
    }
    
    public void onTitleChange(String status) {
    }
    
	protected boolean firstTime = true;

    public void onDocumentComplete(String status) {
		if (firstTime){
			canvas.setURL(System.getProperty("user.dir") + File.separator + "dhtmlTester1.html");
			canvas.resizeControl();
			firstTime = false;
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
    	return false;
    }

	public void oneParamCallBack(String param1) {
		JOptionPane.showMessageDialog(this, "You typed in " + param1);
	}

	public void twoParamCallBack(String param1, String param2) {
	}

}
