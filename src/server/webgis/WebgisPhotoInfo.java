package server.webgis;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

public class WebgisPhotoInfo{
	public final static double LO_SCALE = 0.010506;
	public final static double LA_SCALE = 0.009;
	private static final String FONT_FAMILY = "����";  //����
	private static final int FONT_WEIGHT = Font.PLAIN;  //�����ϸ

	private BufferedImage buffImg = null;
	private Graphics2D g = null;
	private double lo;
	private double la;
	private int scaleLevel;

	public WebgisPhotoInfo(BufferedImage buffImg){
		this.buffImg=buffImg;
		this.g = (Graphics2D) buffImg.getGraphics();
		
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
		g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
		g.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
		g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
	}
	
	public void setInfo(double lo,double la,int scaleLevel){
		this.lo=lo;
		this.la=la;
		this.scaleLevel=scaleLevel;
	}
	
	public WebgisPhotoInfo setColor(Color c){
		g.setBackground(c);
		return this;
	}
	/** ��ʵ�ĵĵ�
	 * @param lo ����
	 * @param la γ��
	 * @param color ��ɫ
	 * @param radius �뾶
	 */
	public Point drawPoint(double lo ,double la , Color color,int radius){
		int x, y;
		x = WebgisPhotoServer.getPxWidthOrHeight(lo-this.lo, true,scaleLevel)+buffImg.getWidth()/2;
		y = buffImg.getHeight()-(WebgisPhotoServer.getPxWidthOrHeight(la-this.la, false, scaleLevel)+buffImg.getHeight()/2);
		g.setColor(color);
		g.fillOval(x-radius, y-radius, radius*2, radius*2);
		return new Point(x,y);
	}
	
	/** �����ĵĵ�
	 * @param lo ����
	 * @param la γ��
	 * @param color ��ɫ
	 * @param radius �뾶
	 */
	public void drawHollowPoint(double lo ,double la ,Color color, int radius)
	{
		int x, y;
		x = WebgisPhotoServer.getPxWidthOrHeight(lo-this.lo, true,scaleLevel)+buffImg.getWidth()/2;
		y = buffImg.getHeight()-(WebgisPhotoServer.getPxWidthOrHeight(la-this.la, false, scaleLevel)+buffImg.getHeight()/2);
		g.setColor(color);
		g.drawOval(x-radius, y-radius,radius*2, radius*2);
		
		return ;
	}
	/** ���ݾ���γ��д��
	 * @param write Ҫд������
	 * @param lo ����
	 * @param la γ��
	 * @param color ��ɫ
	 * @param size �����С
	 * @param a ����x����
	 * @param b ����y����
	 */
	public void drawLoLaString(String write,double lo,double la,Color color,int size,int a, int b){
		int x = WebgisPhotoServer.getPxWidthOrHeight(lo-this.lo, true,scaleLevel)+buffImg.getWidth()/2;
		int y = buffImg.getHeight()-(WebgisPhotoServer.getPxWidthOrHeight(la-this.la, false, scaleLevel)+buffImg.getHeight()/2);
		FontRenderContext frc = g.getFontRenderContext();
        TextLayout tl = new TextLayout(write, new Font(FONT_FAMILY,FONT_WEIGHT,size), frc);
        Shape sha = tl.getOutline(AffineTransform.getTranslateInstance(x+a,y+b));
        g.setStroke(new BasicStroke(3.0f));
		g.setColor(Color.WHITE);
		g.draw(sha);
		g.setColor(color);
		g.fill(sha);
	}
	/** ����ͼƬ����д��
	 * @param write Ҫд������
	 * @param x x����
	 * @param y y����
	 * @param color ��ɫ
	 * @param size �����С
	 * @param a ����x����
	 * @param b ����y����
	 * 
	 */
	public void drawString(String write,int x,int y,Color color,int size,int a,int b){
		
		FontRenderContext frc = g.getFontRenderContext();  
        TextLayout tl = new TextLayout(write, new Font(FONT_FAMILY,FONT_WEIGHT,size), frc);  
		Shape sha = tl.getOutline(AffineTransform.getTranslateInstance(x+a,y+b));  
		g.setStroke(new BasicStroke(3.0f));  
		g.setColor(Color.WHITE);  
		g.draw(sha);  
		g.setColor(color);  
		g.fill(sha);  
	}

	/** ����������
	 * @param outLos ���е���ʼ�㾭γ��
	 * @param outLas ���еĽ����㾭γ��
	 * @param color ��ɫ
	 * @param width ���
	 */
	public void drawContinuousLine(double[] outLos ,double[] outLas, Color color,float width)
	{	
		int []x = new int[outLos.length];
		int []y = new int[outLos.length] ;
		for(int i=0;i<outLos.length;i++){
			x[i] = WebgisPhotoServer.getPxWidthOrHeight(outLos[i]-this.lo, true,scaleLevel)+buffImg.getWidth()/2;
			y[i] = buffImg.getHeight()/2-WebgisPhotoServer.getPxWidthOrHeight(outLas[i]-this.la, false, scaleLevel);
		}
		g.setStroke(new BasicStroke(width)); 
		g.setColor(color);
		for(int i=1;i<x.length;i++){
			g.drawLine(x[i-1], y[i-1], x[i], y[i]);
		}
		
	}
	/** ����2�㻭��
	 * @param beginlo beginla ��ʼ�㾭γ��
	 * @param endlo endla �����㾭γ��	
	 * @param color ��ɫ
	 * @param width ���
	 */
	public void drawLine(double beginlo ,double beginla,double endlo,double endla, Color color,float width)
	{	
		int x = WebgisPhotoServer.getPxWidthOrHeight(beginlo-this.lo, true,scaleLevel)+buffImg.getWidth()/2;
		int y = buffImg.getHeight()/2-WebgisPhotoServer.getPxWidthOrHeight(beginla-this.la, false, scaleLevel);
		int x2 = WebgisPhotoServer.getPxWidthOrHeight(endlo-this.lo, true,scaleLevel)+buffImg.getWidth()/2;
		int y2 = buffImg.getHeight()/2-WebgisPhotoServer.getPxWidthOrHeight(endla-this.la, false, scaleLevel);
		g.setStroke(new BasicStroke(width)); 
		g.setColor(color);
		g.drawLine(x, y, x2, y2);
		
	}
	public void drawLine(int x1,int y1,int x2,int y2,Color color,float width){
		g.setStroke(new BasicStroke(width)); 
		g.setColor(color);
		g.drawLine(x1, y1, x2, y2);
	}
	
	
	/** ������
	 * @param newminlo��newmaxla��newmaxlo��newminla �Ǿ��εĶԽǵ� �ֱ�Ϊ���ϽǺ����½�
	 * @param color ��ɫ
	 */
	public void drawRect(double newminlo,double newmaxla,double newmaxlo,double newminla,Color color)
	{
		int x = WebgisPhotoServer.getPxWidthOrHeight(newminlo-this.lo, true,scaleLevel)+buffImg.getWidth()/2;
		int y = buffImg.getHeight()/2-WebgisPhotoServer.getPxWidthOrHeight(newmaxla-this.la, false, scaleLevel);
		int x2 = WebgisPhotoServer.getPxWidthOrHeight(newmaxlo-this.lo, true,scaleLevel)+buffImg.getWidth()/2;
		int y2  = buffImg.getHeight()/2-WebgisPhotoServer.getPxWidthOrHeight(newminla-this.la, false, scaleLevel);
		int width = x2 -x;
		int height = y2 - y;
		g.setColor(color);		
		g.drawRect(x-1, y, width, height);
	}
	/** ��������
	 * @param newminlo��newmaxla��newmaxlo��newminla �Ǿ��εĶԽǵ� �ֱ�Ϊ���ϽǺ����½�
	 * @param f ͸���� Խ��Խ͸��
	 * @param color ��ɫ
	 */
	public void drawfillRect(double newminlo,double newmaxla,double newmaxlo,double newminla,float f,Color color)
	{
		int x = WebgisPhotoServer.getPxWidthOrHeight(newminlo-this.lo, true,scaleLevel)+buffImg.getWidth()/2;
		int y = buffImg.getHeight()/2-WebgisPhotoServer.getPxWidthOrHeight(newmaxla-this.la, false, scaleLevel);
		int x2 = WebgisPhotoServer.getPxWidthOrHeight(newmaxlo-this.lo, true,scaleLevel)+buffImg.getWidth()/2;
		int y2  = buffImg.getHeight()/2-WebgisPhotoServer.getPxWidthOrHeight(newminla-this.la, false, scaleLevel);
		int width = x2 -x;
		int height = y2 - y;
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, f));
		g.setColor(color);		
		g.fillRect(x-1, y, width, height);
	}
	/** ��Բ
	 * @param lo ���ĵ㾭��
	 * @param la ���ĵ�γ��
	 * @param radius �뾶 ��λkm
	 * @param f ͸���� Խ��Խ͸��
	 * @param color ��ɫ
	 */
	public void fillOval(double lo ,double la ,double radius, float f, Color color)
	{	
		double elo = lo+LO_SCALE*radius;
		int	x = WebgisPhotoServer.getPxWidthOrHeight(lo-this.lo, true,scaleLevel)+buffImg.getWidth()/2;
		int	y = buffImg.getHeight()/2 - WebgisPhotoServer.getPxWidthOrHeight(la-this.la, false, scaleLevel);
		int	a = WebgisPhotoServer.getPxWidthOrHeight(elo-this.lo, true,scaleLevel)+buffImg.getWidth()/2;
		int c =a-x;
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f));
		g.setColor(Color.GREEN);
		g.fillOval(x-c, y-c,c*2,c*2);
	}
	/** ������εı߿�
	 * @param los  �����ľ���
	 * @param loe  ������γ�� color ������ɫ
	 */
	public void drawPolygon(double[] los ,double[] las ,Color color)
	{
		int []x = new int[los.length];
		int []y = new int[los.length] ;
		for(int i=0;i<los.length;i++){
			x[i] = WebgisPhotoServer.getPxWidthOrHeight(los[i]-this.lo, true,scaleLevel)+buffImg.getWidth()/2;
			y[i] = buffImg.getHeight()/2 - WebgisPhotoServer.getPxWidthOrHeight(las[i]-this.la, false, scaleLevel);
		}
		g.setColor(color);
		g.drawPolygon(x, y, x.length);
	}
	/** �������
	 * @param los  �����ľ���
	 * @param loe  ������γ�� 
	 * @param  color ������ɫ 
	 * @param  f����͸����
	 */
	public void fillPolygon(double[] los ,double[] las ,Color color,float f){
		int []x = new int[los.length];
		int []y = new int[los.length] ;
		for(int i=0;i<los.length;i++){
			x[i] = WebgisPhotoServer.getPxWidthOrHeight(los[i]-this.lo, true,scaleLevel)+buffImg.getWidth()/2;
			y[i] = buffImg.getHeight() - (WebgisPhotoServer.getPxWidthOrHeight(las[i]-this.la, false, scaleLevel)+buffImg.getHeight()/2);
		}
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, f));
		g.setColor(color);
		g.fillPolygon(x, y, x.length);
	}
	
	public BufferedImage getBufferedImage(){
		return buffImg;
	}
	
	public boolean save(String path) throws Exception{
		return ImageIO.write(this.buffImg, "png", new File(path));
	}
}
