package server.webgis;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.net.URL;

import javax.imageio.ImageIO;

import com.fleety.server.BasicServer;

public class WebgisPhotoServer extends BasicServer {
	private String webgisPicAddr = null;
	private String user = null;
	private String pwd = null;
	private final static double LO_SCALE = 0.010506;
	private final static double LA_SCALE = 0.009;
	
	private static WebgisPhotoServer singleInstance = null;

	public static WebgisPhotoServer getSingleInstance() {
		if (singleInstance == null) {
			synchronized (WebgisPhotoServer.class) {
				if (singleInstance == null) {
					singleInstance = new WebgisPhotoServer();
				}
			}
		}
		return singleInstance;
	}
	
	public WebgisPhotoServer(){
		
	}

	public boolean startServer() {
		this.isRunning = true;
		
		this.webgisPicAddr = this.getStringPara("webgis_pic_addr");
		this.user = this.getStringPara("user");
		this.pwd = this.getStringPara("pwd");
		
		return true;
	}

    public void stopServer(){
    	super.stopServer();   
    }

	// 得到图片 lo la是中心点
	public WebgisPhotoInfo getWegGisPhotoInfo(double lo, double la,
			int imgWidth, int imgHeight, int scaleLevel) {
		if(!this.isRunning()){
			return null;
		}
		StringBuffer strBuff = new StringBuffer(128);
		strBuff.append(this.webgisPicAddr);
		strBuff.append("?");
		strBuff.append("clo=" + lo);
		strBuff.append("&cla=" + la);
		strBuff.append("&scale=" + scaleLevel);
		strBuff.append("&imgwidth=" + imgWidth);
		strBuff.append("&imgheight=" + imgHeight);
		strBuff.append("&userid=" + this.user);
		strBuff.append("&userpwd=" + this.pwd);

		BufferedImage buffImg = null;
		try {
			buffImg = ImageIO.read(new URL(strBuff.toString()));
			WebgisPhotoInfo photoInfo = new WebgisPhotoInfo(buffImg);
			photoInfo.setInfo(lo, la, scaleLevel);

			return photoInfo;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
    

	// 缩放比 loWidth和laHeight是 经度纬度的跨度
	public static int getFixedScaleLevel(double loWidth, double laHeight,
			int imgWidth, int imgHeight) {
		double m_weight = Math.abs(loWidth / LO_SCALE * 1000 / imgWidth);
		int w_scale = getScale(m_weight);
		double m_height = Math.abs(laHeight / LA_SCALE * 1000 / imgHeight);
		int h_scale = getScale(m_height);
		if (w_scale < h_scale)
			return w_scale + 1;
		else
			return h_scale + 1;
	}

	// 图片的宽和高
	public static Dimension getFixedImgWidthHeight(double loWidth,
			double laHeight, int scaleLevel) {
		int width = getPxWidthOrHeight(loWidth, true, scaleLevel);
		int height = getPxWidthOrHeight(laHeight, false, scaleLevel);
		return new Dimension(width, height);

	}

	private static double[] SCALE_RATIO = new double[] { 40000, 20000, 10000,
			5000, 2333.33333333, 1333.33333333, 666.66666667, 333.33333333,
			133.33333333, 66.66666667, 33.33333333, 16.66666667, 6.66666667,
			3.33333333, 1.8, 0.9 };
	public static double getLoLaFromUnitM(int unitM, boolean isLo) {
		if (isLo) {
			return unitM / 1000.0 * LO_SCALE;
		} else {
			return unitM / 1000.0 * LA_SCALE;
		}
	}

	/**
	 * 根据经纬度和缩放比得到图片宽度或高度
	 * 
	 * @param loOrLaWidth
	 * @param isLo
	 * @param scaleLevel
	 * @return
	 */
	public static int getPxWidthOrHeight(double loOrLaWidth, boolean isLo,
			int scaleLevel) {
		double result = loOrLaWidth;
		if (isLo) {
			result = result * 1000 / LO_SCALE;
		} else {
			result = result * 1000 / LA_SCALE;
		}
		return getPxWidthOrHeight((int) Math.round(result), scaleLevel);
	}

	/**
	 * 根据宽度或高度得到图片宽度或高度
	 * 
	 * @param widthOrHeight
	 * @param scaleLevel
	 * @return
	 */
	public static int getPxWidthOrHeight(int widthOrHeight, int scaleLevel) {
		return (int) Math.round(widthOrHeight / SCALE_RATIO[scaleLevel - 1]);
	}

	public static int getScale(double mile) {
		int len = SCALE_RATIO.length;
		int i = len - 1;
		for (; i >= 0; i--) {
			if (SCALE_RATIO[i] > mile) {
				break;
			}
		}
		return i;
	}

	public static void main(String[] args) throws Exception {
		WebgisPhotoServer.getSingleInstance().addPara("webgis_pic_addr",
				"http://61.152.124.150:5226/webgis/jsp/interface/gis_pic.jsp");
		WebgisPhotoServer.getSingleInstance().addPara("user", "xjs");
		WebgisPhotoServer.getSingleInstance().addPara("pwd", "_xjs");
		WebgisPhotoServer.getSingleInstance().startServer();
		
		WebgisPhotoServer.getSingleInstance().getWegGisPhotoInfo(106.8254,-6.1408,1024,768,12).save("c:/a.png");
	}
}
