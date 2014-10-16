/**
 * 绝密 Created on 2011-3-2 by spyu
 */
package server.lang;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import com.fleety.base.Util;
import com.fleety.base.xml.XmlParser;
import com.fleety.server.BasicServer;

public class StringFilterServer extends BasicServer {

	private HashMap matchMap = new HashMap();

	private long lastLoadTime = 0;

	public final static String DEFAULT_SET_NAME = "default";

	private static StringFilterServer instance = null;
	
	//private boolean isRunning=false;

	/**
	 * 唯一性实例
	 * 
	 * @return
	 */
	public static StringFilterServer getSingleInstance() {
		if (instance == null) {
			synchronized (StringFilterServer.class) {
				instance = new StringFilterServer();
			}
		}
		return instance;
	}

	public boolean startServer() {	
		this.isRunning = this.loadCfg();
		return this.isRunning;
	}

	public void stopServer() {
		this.isRunning = false;
	}

	/**
	 * 加载匹配文件
	 * 
	 * @return
	 */
	private boolean loadCfg() {
		
		String filePath = (String) this.getStringPara("cfg_file");
		//filePath = "./conf/match.xml";
		if (filePath == null || filePath.equals("")) {
			System.out.println("错误的配置文件路径!");
			return false;
		}
		File file = new File(filePath);
		if (!file.exists()) {
			System.out.println("错误的配置文件路径!");
			return false;
		}
		if (this.lastLoadTime == file.lastModified()) {
			return true;
		}

		this.lastLoadTime = file.lastModified();
		HashMap tempMap=new HashMap();
		FileInputStream in = null;
		try {
			in = new FileInputStream(file);
			Element root = XmlParser.parse(in);
			Node[] charSetNodes = Util.getElementsByTagName(root, "char-set");
			if (charSetNodes == null || charSetNodes.length == 0) {
				return false;
			}
			Node charSetNode = null;
			String charSetName = null;
			CharSetInfo setInfo = null;
			Node[] charNodes = null;
			Node charNode = null;
			for (int i = 0; i < charSetNodes.length; i++) {
				charSetNode = charSetNodes[i];
				charNodes = Util.getElementsByTagName(charSetNode, "char");
				if (charNodes == null || charNodes.length == 0) {
					continue;
				}
				charSetName = Util.getNodeAttr(charSetNode, "name");
				if (charSetName == null || charSetName.equals("")) {
					charSetName = StringFilterServer.DEFAULT_SET_NAME;
				}
				setInfo = new CharSetInfo();
				for (int j = 0; j < charNodes.length; j++) {
					charNode = charNodes[j];
					setInfo.putInfo(Util.getNodeAttr(charNode, "key"), Util
							.getNodeAttr(charNode, "value"));
				}
				tempMap.put(charSetName, setInfo);
			}

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		this.matchMap=tempMap;
		return true;

	}

	/**
	 * 使用公共的文件过滤器对文本逐个字进行过滤
	 * 
	 * @param src
	 * @return
	 */
	public String filterByChar(String src) {
		return this.filterByChar(src, StringFilterServer.DEFAULT_SET_NAME);
	}

	public String filterByChar(String src, String charSetName) {
		if (!this.isRunning) {
			return src;
		}	
		if (src == null || src.equals("")) {
			return src;
		}
		this.loadCfg();
		CharSetInfo setInfo = (CharSetInfo) this.matchMap.get(charSetName);
		if (setInfo == null) {
			return src;
		}
		StringBuffer destBuf = new StringBuffer();
		String key, value;
		for (int i = 0; i < src.length(); i++) {
			key = src.charAt(i) + "";
			value = setInfo.getInfo(key);
			if (value != null) {
				destBuf.append(value);
			} else {
				destBuf.append(key);
			}
		}
		return destBuf.toString();
	}
	public String filterByString(String src){
		return this.filterByString(src, StringFilterServer.DEFAULT_SET_NAME);
	}
	public String filterByString(String src,String charSetName){
		if (!this.isRunning) {
			return src;
		}		
		if (src == null || src.equals("")) {
			return src;
		}
		this.loadCfg();
		CharSetInfo setInfo = (CharSetInfo) this.matchMap.get(charSetName);		
		if (setInfo == null) {
			return src;
		}
		String[] keys=setInfo.getAllKeys();
		
		if(keys==null||keys.length==0){
			return src;
		}
		StringBuffer buff=new StringBuffer();
		int index=0;
		String key=null;
		String head=null,middle=null,end=null;		
		for(int i=0;i<keys.length;i++){	
			key=keys[i];
			index=src.indexOf(key);
			if(index>-1){
				head=src.substring(0,index);					
				middle=setInfo.getInfo(key);	
				end=src.substring(index+key.length());	
				
				src=head+middle+end;
				i=-1;
			}else{
				continue;
			}		
			
		}
		buff.append(src);
		return buff.toString();
	}	
	public class CharSetInfo {
		private HashMap charSetMap = new HashMap();

		public void putInfo(String key, String value) {
			this.charSetMap.put(key, value);
		}

		public String getInfo(String key) {
			return (String) this.charSetMap.get(key);
		}
		public String[] getAllKeys(){
			String[] keys=new String[this.charSetMap.size()];
			this.charSetMap.keySet().toArray(keys);
			return keys;
		}
	}

	public static void main(String[] args) {
		StringFilterServer.getSingleInstance().startServer();
		String test = StringFilterServer.getSingleInstance().filterByString("我A鎄美国de 中国的美国的人民");
		System.out.println(test);
		String src = "中国人民";
		char temp;
		for (int i = 0; i < src.length(); i++) {
			temp = src.charAt(i);
			System.out.println(temp);
		}
	}

}
