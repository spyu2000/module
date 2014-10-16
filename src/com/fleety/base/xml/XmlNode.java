/**
 * ¾øÃÜ Created on 2008-2-26 by edmund
 */
package com.fleety.base.xml;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import com.fleety.base.StrFilter;

public class XmlNode{
	public static final String TAG_PRE_FLAG = "<";
	public static final String TAG_POST_FLAG = ">";
	public static final String END_TAG_FLAG = "/";
	public static final String ATTR_SEPARATE_FLAG = " ";
	public static final String ATTR_EQUAL_FLAG = "=";
	public static final String INDENT_STEP_FLAG = "\t";
	public static final String ENTER_STEP_FLAG = "\n";
	
	private List keyList = null;
	private HashMap attrs = null;
	private List childs = null;
	
	private String text = null;
	private String tagName = null;
	
	private boolean allowControlChar = false;
	private boolean isHTML = false;
	
	public XmlNode(String tagName){
		this(tagName,null);
	}
	
	public XmlNode(String tagName,String text){
		this(tagName,text,true);
	}
	public XmlNode(String tagName,String text,boolean allowControlChar){
		this(tagName,text,allowControlChar,true);
	}
	public XmlNode(String tagName,String text,boolean allowControlChar,boolean isHTML){
		this.tagName = tagName;
		this.text = text;
		this.allowControlChar = allowControlChar;
		this.isHTML = isHTML;
	}
	
	public void addAttribute(String key,String value){
		if(this.attrs == null){
			this.attrs = new HashMap();
			this.keyList = new LinkedList();
		}
		if(this.attrs.containsKey(key)){
			this.keyList.remove(key);
		}
		this.attrs.put(key, value);
		this.keyList.add(key);
	}
	
	public void setText(String text){
		this.text = text;
	}
	
	public void addNode(XmlNode child){
		if(this.childs == null){
			this.childs = new LinkedList();
		}
		this.childs.add(child);
	}
	public void removeNode(XmlNode child){
		if(this.childs == null){
			return ;
		}
		this.childs.remove(child);
	}
	
	public String getTagName(){
		return this.tagName;
	}
	public String getValue(){
		return this.text;
	}
	public List getChildList(){
		return this.childs;
	}
	public int getChildNum(){
		if(this.childs == null){
			return 0;
		}
		return this.childs.size();
	}
	public Iterator attrKeys(){
		if(this.attrs == null){
			return null;
		}
		return this.keyList.iterator();
	}
	public String getAttr(String key){
		if(this.attrs == null){
			return null;
		}
		return (String)this.attrs.get(key);
	}

	public void toXmlString(StringBuffer buff,String indent){
		this.toXmlString(buff, indent, true);
	}
	
	public void toXmlString(StringBuffer buff,String indent,boolean isFormat){
		if(indent == null){
			indent = "";
		}
		if(isFormat){
			buff.append(indent);
		}
		buff.append(TAG_PRE_FLAG);
		buff.append(this.tagName);
		if(this.attrs!=null){
			String key,value;
			Iterator ite = this.keyList.iterator();
			while(ite.hasNext()){
				key = (String)ite.next();
				value = (String)this.attrs.get(key);
				buff.append(ATTR_SEPARATE_FLAG);
				buff.append(key);
				buff.append(ATTR_EQUAL_FLAG);
				buff.append("\"");
				buff.append(StrFilter.filterXmlStr(value,this.allowControlChar));
				buff.append("\"");
			}
		}
		
		if(this.text == null && this.childs == null){
			buff.append(END_TAG_FLAG);
			buff.append(TAG_POST_FLAG);
		}else{
			buff.append(TAG_POST_FLAG);
			
			if(this.text != null){
				if(this.isHTML){
					buff.append(this.text);
				}else{
					buff.append(StrFilter.filterXmlStr(this.text,this.allowControlChar));
				}
			}else if(this.childs != null){
				if(isFormat){
					buff.append(ENTER_STEP_FLAG);
				}
				XmlNode child;
				Iterator ite = this.childs.iterator();
				while(ite.hasNext()){
					child = (XmlNode)ite.next();
					if(isFormat){
						child.toXmlString(buff,indent+INDENT_STEP_FLAG,isFormat);
					}else{
						child.toXmlString(buff,indent,isFormat);
					}
				}
				if(isFormat){
					buff.append(indent);
				}
			}
	
			buff.append(TAG_PRE_FLAG);
			buff.append(END_TAG_FLAG);
			buff.append(this.tagName);
			buff.append(TAG_POST_FLAG);
		}
		if(isFormat){
			buff.append(ENTER_STEP_FLAG);
		}
	}
}
