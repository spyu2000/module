package server.dic;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.fleety.base.InfoContainer;
import com.fleety.base.Util;
import com.fleety.base.xml.XmlParser;
import com.fleety.server.BasicServer;

public class MarkTypeEnumServer extends BasicServer {
	public static final String MARK_TYPE_PATH_KEY = "mark_type_path";
	public static final int POINT_TYPE = 1;
	public static final int LINE_TYPE = 2;
	public static final int REGION_TYPE = 3;

	private List pointMarkList = new ArrayList(16);
	private List lineMarkList = new ArrayList(16);
	private List regionMarkList = new ArrayList(16);
	
	private static MarkTypeEnumServer singleInstance = new MarkTypeEnumServer();
	public static MarkTypeEnumServer getSingleInstance(){
		return singleInstance;
	}
	public boolean startServer() {
		try{
			this.isRunning = this.loadInfo(this.getStringPara(MARK_TYPE_PATH_KEY));
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}
		return this.isRunning();
	}
	private boolean loadInfo(String path) throws Exception{
		if(path == null || path.trim().length() == 0){
			return false;
		}
		File f = new File(path);
		if(!f.exists() || !f.isFile()){
			return false;
		}
		
		Document doc = XmlParser.parse(f);
		Element root = doc.getDocumentElement();
		
		Node[] arr;

		arr = Util.getElementsByTagName(Util.getSingleElementByTagName(root, "point"),"mark");
		this.initMark(POINT_TYPE,arr);
		arr = Util.getElementsByTagName(Util.getSingleElementByTagName(root, "line"),"mark");
		this.initMark(LINE_TYPE,arr);
		arr = Util.getElementsByTagName(Util.getSingleElementByTagName(root, "region"),"mark");
		this.initMark(REGION_TYPE,arr);
		
		return true;
	}
	
	private void initMark(int markType,Node[] markNodeArr){
		if(markNodeArr == null){
			return ;
		}
		
		for(int i=0;i<markNodeArr.length;i++){
			this.initOneMark(markType,markNodeArr[i]);
		}
	}
	
	private void initOneMark(int markType,Node markNode){
		if(markNode == null){
			return ;
		}
		
		String code,name;
		
		code = Util.getNodeAttr(markNode, "code");
		name = Util.getNodeAttr(markNode, "name");
		if(code == null || name == null){
			return ;
		}
		MarkType mark = new MarkType(code,name);
		Node[] arr = Util.getAllAttrNode(markNode);
		Node node;
		for(int i=0;i<arr.length;i++){
			node = arr[i];
			mark.setInfo(node.getNodeName(),node.getNodeValue());
		}
		
		List tempList = null;
		switch(markType){
			case POINT_TYPE:
				tempList = this.pointMarkList;
				break;
			case LINE_TYPE:
				tempList = this.lineMarkList;
				break;
			case REGION_TYPE:
				tempList = this.regionMarkList;
				break;
			default:
				return ;
		}
		
		tempList.add(mark);
	}

	public List getMarkType(int markType){
		if(!this.isRunning()){
			return null;
		}
		
		switch(markType){
			case POINT_TYPE:
				return this.pointMarkList;
			case LINE_TYPE:
				return this.lineMarkList;
			case REGION_TYPE:
				return this.regionMarkList;
		}
		return null;
	}
	
	public class MarkType extends InfoContainer{
		private String code = null;
		private String name = null;
		public MarkType(String code,String name){
			this.code = code;
			this.name = name;
		}
		
		public String getCode(){
			return this.code;
		}
		public String getName(){
			return this.name;
		}
	}
	
	public static void main(String[] argv){
		MarkTypeEnumServer server = new MarkTypeEnumServer();
		server.addPara("mark_type_path", "resource/mark_type.xml");
		server.startServer();
	}
}
