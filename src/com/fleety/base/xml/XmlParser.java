/**
 * ¾øÃÜ Created on 2008-5-12 by edmund
 */
package com.fleety.base.xml;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.fleety.base.GeneralConst;

public class XmlParser{
	public static Element parse(String xmlStr,String charset){
		try{
			if(charset != null){
				xmlStr = "<?xml version=\"1.0\" encoding=\""+charset+"\"?>\n"+xmlStr;
			}
			byte[] buff = xmlStr.getBytes();
			InputStream in = new ByteArrayInputStream(buff);
			
			DocumentBuilderFactory domfac=DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = domfac.newDocumentBuilder();
			Document document = builder.parse(in);
			in.close();
			
			return document.getDocumentElement();
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}
	
	public static Element parse(InputStream in){
		try{			
			DocumentBuilderFactory domfac=DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = domfac.newDocumentBuilder();
			Document document = builder.parse(in);
			in.close();
			
			return document.getDocumentElement();
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}
	
	public static Document parse(File xmlFile){
		try{			
			DocumentBuilderFactory domfac=DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = domfac.newDocumentBuilder();
			return builder.parse(xmlFile);
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}
	
	public static String obj2Xml(Object rootObj,String rootName){
		StringBuffer buff = new StringBuffer(1024);
		
		XmlNode root = new XmlNode(rootName);
		obj2Xml(null,root,rootObj);
		
		root.toXmlString(buff, "");
		return buff.toString();
	}
	
	private static void obj2Xml(XmlNode parentNode,XmlNode curNode,Object obj){
		Method[] allMethod = obj.getClass().getMethods();
		Method method;
		String methodName;
		int num = allMethod.length;
		Object childObj;
		for(int i=0;i<num;i++){
			method = allMethod[i];
			if(method.getParameterTypes().length > 0){
				continue;
			}
			methodName = method.getName().toLowerCase();
			if(!methodName.startsWith("get")){
				continue;
			}
			if(method.getReturnType() == null){
				continue;
			}
			if(method.getName().equalsIgnoreCase("getClass")){
				continue;
			}
			try{
				childObj = method.invoke(obj, new Object[0]);
				if(childObj == null){
					curNode.addNode(new XmlNode(methodName.substring(3).toLowerCase(),""));
				}else if(childObj instanceof List){
					XmlNode childNode;
					String name = methodName.substring(3).toLowerCase();
					XmlNode listNode = new XmlNode("all_"+name);
					curNode.addNode(listNode);
					for(Iterator itr = ((List)childObj).iterator();itr.hasNext();){
						childNode = new XmlNode(name);
						obj2Xml(listNode,childNode,itr.next());
					}
				}else if(childObj instanceof Date){
					GeneralConst.YYYY_MM_DD_HH_MM_SS.format((Date)childObj);
				}else if(childObj instanceof String || childObj instanceof Integer || childObj instanceof Double || childObj instanceof Boolean){
					curNode.addNode(new XmlNode(methodName.substring(3).toLowerCase(),childObj.toString()));
				}else{
					XmlNode childNode = new XmlNode(methodName.substring(3).toLowerCase());
					
					obj2Xml(curNode,childNode,childObj);
				}
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		
		if(parentNode != null){
			parentNode.addNode(curNode);
		}
	}
}
