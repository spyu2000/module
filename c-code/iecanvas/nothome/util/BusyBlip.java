package nothome.util;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class BusyBlip extends JComponent implements ActionListener {
    
    
    protected static final int DELAY = 18;
    protected static final int COLOURS = 15;
    
    protected final Timer timer = new Timer(DELAY, this);
    protected int count = 0;
    protected Color c[] = null;
    
    protected int pos1 = 0, pos2 = 0;
    protected boolean LtoR = true;
    
    protected boolean indicator = false;
    
    public BusyBlip() { super(); }
    
    public void start() {
        //System.err.println("starting blip!");
        synchronized(this) {
//            count ++;
//            if (count == 1) {
//                System.err.println("starting blip!");
                timer.start();
//            }
        }
    }
    
    public void stop() {
        synchronized(this) {
//            count --;
//            if (count < 1) {
//                System.err.println("stopping blip!");
                timer.stop();
                pos1 = pos2 = 0;
                count = 0;
//            }
        }
        repaint();
    }
    
    public void setProgress(int start, int stop) {
        if (stop == 0) {
            indicator = false;
            pos1 = pos2 = 0;
            repaint();
        } else { 
            indicator = true;

            int w = getWidth();
            Insets ins = getInsets();
            w -= (ins.left + ins.right);
            
            pos2 = ins.left;
            pos1 = w * start / stop;
            repaint();
        }
    }
    
    public void actionPerformed(ActionEvent e) {
        if (indicator)
            return;
        int w = getWidth();
        Insets ins = getInsets();
        w -= (ins.left + ins.right);
        
        if (LtoR) {
            //if (pos1 < w + w / 3) {
            if (pos1 < w) {
                pos1 += 1;
                if (pos1 > w / 3) // farther right than one third
                    pos2 += 1;
            } else { // restart at right
                pos1 = pos2 = w;
                LtoR = false;
            }
        } else {
            //if (pos1 > - w / 3) {
            if (pos1 > 0) {
                pos1 -= 1;
                if (pos1 < w - (w / 3))
                    pos2 -= 1;
            } else { // restart at left
                pos1 = pos2 = 0;
                LtoR = true;
            }
        }
        repaint();
    }
    
    public void paintComponent(Graphics g) {
        int w = getWidth();
        int h = getHeight();
        
        Color bg = getBackground();
        Color fg = getForeground();
        if (c == null) {
            c = new Color[COLOURS];
            
            for (int j = 0; j < COLOURS; j++) {
                
                int r = fg.getRed() - bg.getRed() * j/COLOURS + bg.getRed();
                //r = r > 0? r : -r;
                //			int minr = fg.getRed() < bg.getRed()? fg.getRed() : bg.getRed();
                
                int gr = fg.getGreen() - bg.getGreen() * j/COLOURS + bg.getGreen(); //gr = gr > 0? gr : -gr;
                //			int ming = fg.getGreen() < bg.getGreen()? fg.getGreen() : bg.getGreen();
                
                int b = fg.getBlue() - bg.getBlue() * j/COLOURS + bg.getBlue(); //b = b > 0? b : -b;
                //			int minb = fg.getBlue() < bg.getBlue()? fg.getBlue() : bg.getBlue();
                
                c[j] = new Color(r, gr, b);
                //				c[j] = new Color(r * j/COLOURS, gr * j/COLOURS, b * j/COLOURS);
                //				c[jx] = new Color(r * jx/10 + minr, gr * jx/10 + ming, b * jx/10 + minb);
            }
        }
        
        if (isOpaque()) {
            g.setColor(bg);
            g.fillRect(0, 0, w, h);
            g.setColor(fg);
        }
        Insets ins = getInsets();
        w -= (ins.left + ins.right);
        h -= (ins.top + ins.bottom);
        
        int width = pos1 > pos2 ? pos1 - pos2 : pos2 - pos1;
        
        if (LtoR) {
            int left = ins.left + pos1 - width;
            left = left > 0 ? left : 0;
/*			for (int i = width; i > 0; i--) {
                                int s = COLOURS - 1 - (((width - i) * COLOURS) / width );
                                g.setColor(c[s]);
                                g.fillRect(left + i, ins.top, 1, h);
 
                        }*/
            
            
            g.fillRect(left, ins.top, width, h);
        } else {
                        /*for (int i = 0 ; i < width; i++) {
                                int s = (((width - i - 1) * COLOURS) / width);
                                g.setColor(c[s]);
                                g.fillRect(ins.left + pos1 + i, ins.top, 1, h);
                        }*/
            g.fillRect(ins.left + pos1, ins.top, width, h);
        }
    }
}