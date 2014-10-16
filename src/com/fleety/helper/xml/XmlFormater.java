package com.fleety.helper.xml;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Iterator;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.fleety.base.InfoContainer;
import com.fleety.base.Util;
import com.fleety.base.xml.XmlNode;
import com.fleety.base.xml.XmlParser;
import com.fleety.helper.HelpResult;
import com.fleety.helper.HelperAdapter;

public class XmlFormater extends HelperAdapter {
	public static final Object FILTER_FLAG = "filter";
	public static final Object SRC_XML_FILE_PATH_FLAG = "src_xml_file_path";
	public static final Object DEST_XML_FILE_PATH_FLAG = "dest_xml_file_path";
	
	public HelpResult help(InfoContainer info) {
		String srcXmlFilePath = info.getString(SRC_XML_FILE_PATH_FLAG);
		IFilter filter = (IFilter)info.getInfo(FILTER_FLAG);
		
		try{
			File f = new File(srcXmlFilePath);
			if(!f.exists()){
				return new HelpResult(false,"File Not Exist");
			}
			Document document = XmlParser.parse(f);

			Element root = document.getDocumentElement();
			XmlNode sRoot = this.createObj(root,filter);
			
			if(filter != null){
				this.filterWriteNode(sRoot,filter);
			}
			
			File destFile = new File(info.getString(DEST_XML_FILE_PATH_FLAG));
			destFile.getParentFile().mkdirs();
			
			String encoding = document.getXmlEncoding();
			StringBuffer strBuff = new StringBuffer((int)f.length());
			strBuff.append("<?xml version=\"");
			strBuff.append(document.getXmlVersion());
			strBuff.append("\"  encoding=\"");
			strBuff.append(encoding);
			strBuff.append("\">\n");
			sRoot.toXmlString(strBuff, "");
			
			OutputStream out = new BufferedOutputStream(new FileOutputStream(destFile));
			out.write(strBuff.toString().getBytes(encoding));
			out.close();
		}catch(Exception e){
			e.printStackTrace();
			return new HelpResult(false,"Execute Error");
		}finally{
			
		}
		
		return new HelpResult();
	}
	
	private void filterWriteNode(XmlNode node,IFilter filter){
		if(node.getChildNum() == 0){
			return ;
		}
		
		XmlNode subNode;
		for(Iterator itr = node.getChildList().iterator();itr.hasNext();){
			subNode = (XmlNode)itr.next();
			
			this.filterWriteNode(subNode, filter);
			
			if(!filter.isAccept(subNode)){
				itr.remove();
			}
		}
	}
	
	private XmlNode createObj(Node node,IFilter filter){
		XmlNode xmlNode = this.createObjOnlySelf(node);
		
		Node subNode;
		NodeList list = node.getChildNodes();
		for(int i=0;list != null && i < list.getLength();i++){
			subNode = list.item(i);
			
			if(filter != null && !filter.isAccept(subNode)){
				continue;
			}
			
			if(subNode.getNodeType() == Node.TEXT_NODE){
				String text = Util.getNodeText(subNode.getParentNode());
				if(text != null && text.trim().length() > 0){
					xmlNode.setText(text.trim());
				}
			}else if(subNode.getNodeType() == Node.ELEMENT_NODE){
				xmlNode.addNode(this.createObj(subNode,filter));
			}
		}
		
		return xmlNode;
	}
	
	private XmlNode createObjOnlySelf(Node node){		
		XmlNode xmlNode = new XmlNode(node.getNodeName());
		
		NamedNodeMap mapping = node.getAttributes();
		Node attrNode;
		for(int i=0;mapping != null && i<mapping.getLength();i++){
			attrNode = mapping.item(i);
			xmlNode.addAttribute(attrNode.getNodeName(), attrNode.getNodeValue());
		}
		
		return xmlNode;
	}

	public static void main(String[] argv) {
		new XmlFormater().help(new InfoContainer().setInfo(
				SRC_XML_FILE_PATH_FLAG, "D:/xjs/temp/web_purview.xml").setInfo(
				DEST_XML_FILE_PATH_FLAG, "D:/xjs/temp/web_purview_1.xml")
				.setInfo(FILTER_FLAG, new IFilter(){
					public boolean isAccept(Node node){
						String isVisible = Util.getNodeAttr(node, "visible");
						return !(isVisible!=null && isVisible.equalsIgnoreCase("false"));
					}
					public boolean isAccept(XmlNode node){
						if(node.getTagName().equals("all_link")){
							return node.getChildNum() > 0;
						}else if(node.getTagName().equals("group")){
							return node.getChildNum() > 1;
						}
						return true;
					}
				}));
	}
}
