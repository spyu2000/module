package nothome.mswindows;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.event.*;

import nothome.util.*;

public class IEBrowser extends JFrame implements ActionListener, 
    IEEventListener {
    
    protected JTextField    urlField;
    protected JLabel        statusLabel;
    protected IECanvas      canvas;
    protected JButton       fwdButton, backwdButton, reloadButton, homeButton;
    protected BusyBlip      blip;
    
    public IEBrowser(String urlString) {
        getContentPane().setLayout(new BorderLayout());
        setTitle("IEBrowser");
        
        //FocusManager.disableSwingFocusManager();
        urlField = new JTextField(urlString);
        urlField.addActionListener(this);
                
        urlField.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                canvas.setfocus();
            }
        });        

        JPanel upperPanel = new JPanel();
        upperPanel.setLayout(new GridLayout());
        
        JPanel statusPanel = new JPanel();
        statusPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        
        blip = new BusyBlip();
        blip.setPreferredSize(new Dimension(100, 20));
        blip.setOpaque (true);
        blip.setBorder (new CompoundBorder (
            new CompoundBorder (
            new EmptyBorder (new Insets(1, 1, 1, 1)),
            new LineBorder (getForeground())), //(Color.black)),
            new EmptyBorder (new Insets(1, 1, 1, 1))));
        blip.setForeground (getForeground());
                
        statusLabel = new JLabel("ready...");
        statusPanel.add(blip);
        statusPanel.add(statusLabel);
        
        JToolBar toolBar = new JToolBar();
        
        backwdButton = new JButton("Back"); backwdButton.addActionListener(this);
        fwdButton = new JButton("Forward"); fwdButton.addActionListener(this);
        reloadButton = new JButton("Reload"); reloadButton.addActionListener(this);
        homeButton = new JButton("Home"); homeButton.addActionListener(this);
        toolBar.add(backwdButton);
        toolBar.add(fwdButton);
        toolBar.add(reloadButton);
        toolBar.add(homeButton);
        
        upperPanel.add(toolBar);
        upperPanel.add(urlField);
        getContentPane().add(upperPanel, BorderLayout.NORTH);
        getContentPane().add(statusPanel, BorderLayout.SOUTH);
        
        JPopupMenu.setDefaultLightWeightPopupEnabled(false);
        
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        menuBar.add(fileMenu);
        
        
        fileMenu.add(new AbstractAction("Home") {
            public void actionPerformed(ActionEvent evt) {
            }
        });
        
        fileMenu.add(new AbstractAction("Reload") {
            public void actionPerformed(ActionEvent evt) {
            }
        });
        
        fileMenu.add(new AbstractAction("Exit") {
            public void actionPerformed(ActionEvent evt) {
                System.exit(0);
            }
        });
            
            
        setJMenuBar(menuBar);
        
        WindowListener l = new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        };
        addWindowListener(l);
    }
    
    public void actionPerformed(ActionEvent evt) {
        if (evt.getSource() == urlField) {
            String str = urlField.getText();
            //System.err.println("setting url to: " + str);
            canvas.setURL(str);
        } else if (evt.getSource() == backwdButton) {
            canvas.goBackward();
        } else if (evt.getSource() == fwdButton) {
            canvas.goForward();
        } else if (evt.getSource() == reloadButton) {
            canvas.reload();
        } else if (evt.getSource() == homeButton) {
            canvas.goHome();
        }
    }
    
    public void addBrowserWindow(IECanvas w) {
        canvas = w;
        getContentPane().add(w, BorderLayout.CENTER);

        // hack focus problems..
//        urlField.setNextFocusableComponent(canvas);
    }
    
    public static void main(String[] argv) {
        String urlString = "";
        
        if(argv.length > 0)
            urlString = argv[0];
        
        IEBrowser b = new IEBrowser(urlString);
        
        IECanvas canvas = new IECanvas(false);
        canvas.addIEEventListener(b);
        
        b.addBrowserWindow(canvas);
        b.setBounds(300, 300, 500, 300);
        b.setVisible(true);
    }
    
    // IEEventListener impl.
    
    public void onStatusTextChange(String status) {
        if (status.length() == 0)
            statusLabel.setText(" ");
        else
            statusLabel.setText(status);
    }
    
    public void onTitleChange(String status) {
        setTitle(status + " - IEBrowser");
    }
    
    public void onDocumentComplete(String status) {
        //System.out.println("java: OnDocumentComplete: " + status);
        //statusLabel.setText(status);
    }
    
    public void onBeforeNavigate2(String url) {
        System.out.println("java: OnBeforeNaviate2: " + url);
        urlField.setText(url);
    }
    
    public void onNavigateComplete2(String status) {
        System.out.println("java: OnNaviateComplete2: " + status);
        statusLabel.setText(status);
    }
    
    public void onDownloadComplete() {
        System.out.println("java: OnDownloadComplete");
    }
    
    public void onProgressChange(int progress, int max) {
        System.out.println("java: progress: " + progress + ", max: " + max);
        //if (max == 0)
        //    blip.stop();
        //else if (progress != max) 
    //            blip.start();
        blip.setProgress(progress, max);
    }
    
    public void onCommandStateChange(int command, boolean enabled) {
        if (command == 2) {
            backwdButton.setEnabled(enabled);
        } else if (command == 1) {
            fwdButton.setEnabled(enabled);
        } else  {
            //System.out.println("java: command " + command + " set to: " + enabled);
        }
    }
    
    
    public void onQuit() {
        
    }
    
    public boolean showContextMenu()  {
        return false;
    }

	public void oneParamCallBack(String param1) {
	}

	public void twoParamCallBack(String param1, String param2) {
	}

}
