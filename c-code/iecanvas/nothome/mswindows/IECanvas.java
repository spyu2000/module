package nothome.mswindows;

import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.awt.*;
import java.awt.peer.*;
import sun.awt.*;
import java.util.*;

public class IECanvas extends Canvas {
    
    protected Vector listeners;
    protected boolean useMozilla;
    
    static {
        System.loadLibrary("IECanvas");
    }
    
    // Native entry points.
    public native void initialize(boolean useMozilla);
    public native void sendurl(String strURL);
    
    public native void resizeControl();
    public native void gohome();
    public native void rld();
    public native void gofwd();
    public native void gobackwd();
    public native void setfocus();
    public native void setInnerHtml(String id, String html);
    public native String getInnerHtml(String id);
	public native void setClassName(String id, String className);
    public native String getClassName(String id);
    
    public native void executeJavascript(String javaScript); 
    
    
    public IECanvas() { this(false); }

    public IECanvas(boolean useMozilla) {
        listeners = new Vector();
        this.useMozilla = useMozilla;
    }

    // Navigation methods
 
    public void setURL(String urlString) {
        sendurl(urlString);
    }
    public void goHome() { gohome(); }
    public void reload() { rld(); }
    public void goForward() { gofwd(); }
    public void goBackward() { gobackwd(); }
    
    // event notification
    
    public void addIEEventListener(IEEventListener listener)  {
        listeners.add(listener);
    }
    
    public void removeIEEventListener(IEEventListener listener)  {
        listeners.remove(listener);
    }
    
    // We initialize the IECanvas on addNotify.
    public void addNotify() {
        super.addNotify();
        System.out.println("using mozilla: " + useMozilla);
        initialize(useMozilla);
    }
  
    
    public void setSize(int width, int height) {
        super.setSize(width,height);
        resizeControl();
    }
    
    public void setSize(Dimension d) {
        super.setSize(d);
        resizeControl();
    }
    
    public void setBounds(int x, int y, int width, int height) {
        super.setBounds(x,y,width,height);
        resizeControl();
    }
    
    public void setBounds(Rectangle r) {
        super.setBounds(r);
        resizeControl();
    }
    
    // Callback notifiers
    public void onStatusTextChange(final String status) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                Enumeration enum = listeners.elements();
                while (enum.hasMoreElements())  {
                    IEEventListener listener = (IEEventListener) enum.nextElement();
                    listener.onStatusTextChange(status);
                }
            }
        });
    }
    
    public void onTitleChange(final String status) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                Enumeration enum = listeners.elements();
                while (enum.hasMoreElements())  {
                    IEEventListener listener = (IEEventListener) enum.nextElement();
                    listener.onTitleChange(status);
                }
            }
        });
    }
    
    public void onDocumentComplete(final String status) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                Enumeration enum = listeners.elements();
                while (enum.hasMoreElements())  {
                    IEEventListener listener = (IEEventListener) enum.nextElement();
                    listener.onDocumentComplete(status);
                }
            }
        });
    }
    
    public void onBeforeNavigate2(final String url) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                Enumeration enum = listeners.elements();
                while (enum.hasMoreElements())  {
                    IEEventListener listener = (IEEventListener) enum.nextElement();
                    listener.onBeforeNavigate2(url);
                }
            }
        });
    }
    
    public void onNavigateComplete2(final String status) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                Enumeration enum = listeners.elements();
                while (enum.hasMoreElements())  {
                    IEEventListener listener = (IEEventListener) enum.nextElement();
                    listener.onNavigateComplete2(status);
                }
            }
        });
    }
    
    public void onDownloadComplete() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                Enumeration enum = listeners.elements();
                while (enum.hasMoreElements())  {
                    IEEventListener listener = (IEEventListener) enum.nextElement();
                    listener.onDownloadComplete();
                }
            }
        });
    }
    
    
    public void onProgressChange(final int progress, final int max)  {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                Enumeration enum = listeners.elements();
                while (enum.hasMoreElements())  {
                    IEEventListener listener = (IEEventListener) enum.nextElement();
                    listener.onProgressChange(progress, max);
                }
            }
        });
    }
    
    public void onCommandStateChange(final int command, final boolean enabled)  {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                Enumeration enum = listeners.elements();
                while (enum.hasMoreElements())  {
                    IEEventListener listener = (IEEventListener) enum.nextElement();
                    listener.onCommandStateChange(command, enabled);
                }
            }
        });
    }
    
    public void onQuit() {
        System.out.println("java: OnQuit");
    }
    
	/**
	 *	@returns true if the default IE context menu is to be displayed, false
	 *  if we provide our own.
	 */
    public boolean showContextMenu(int x, int y)  {
        System.out.println("java: ShowContextMenu at x: " + x + ", y: " + y);
		
		return true; // Use IE's context menu.
    }
    
	public String oneParamCallBack(final String param1) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				Enumeration enum = listeners.elements();
				while (enum.hasMoreElements())  {
					IEEventListener listener = (IEEventListener) enum.nextElement();
					listener.oneParamCallBack(param1);
				}
			}
		});
		return "";
	}
	
	public String twoParamCallBack(final String param1, final String param2) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				Enumeration enum = listeners.elements();
				while (enum.hasMoreElements())  {
					IEEventListener listener = (IEEventListener) enum.nextElement();
					listener.twoParamCallBack(param1, param2);
				}
			}
		});
		return "";
	}
}
