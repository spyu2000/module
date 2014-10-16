/**
 * 绝密 Created on 2008-6-3 by edmund
 */
package com.fleety.base.i18n;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import com.fleety.base.Util;
import com.fleety.base.xml.XmlParser;

public class I18n{
	public static final String DEFAULT_LANG = "default";
	public static I18n singleInstance = null;
	public static I18n getSingleInstance(){
		if(singleInstance == null){
			synchronized(I18n.class){
				if(singleInstance == null){
					singleInstance = new I18n();
				}
			}
		}
		return singleInstance;
	}
	
	private File cfgFile = null;
	private long lastLoadTime = 0;
	private HashMap mapping = null;
	private String lang = null;
	
	private MessageFormat formater = null;
	public I18n(){
		this(null);
	}
	
	public I18n(File cfgFile){
		this.setI18nFile(cfgFile);
		this.mapping = null;
		this.formater = new MessageFormat("");
	}
	
	private List listenerList = new LinkedList();
	public synchronized void addLoadListener(ILoadListener listener){
		if(listener != null){
			this.listenerList.add(listener);
		}
	}
	
	private synchronized void triggerListener(){
		Iterator iterator = this.listenerList.iterator();
		while(iterator.hasNext()){
			((ILoadListener)iterator.next()).infoReload(this);
		}
	}
	
	/**
	 * 设定配置的xml文件.该文件格式为:
	 * <i18n lang="">
	 * 		<group name="" desc="">
	 * 			<info name=""></info>
	 * 		</group>
	 * </i18n>
	 * 
	 * lang描述这个配置文件中语言的信息.可能是chinese或者english,或者default.
	 * 文本中的参数描述{0}如此类似.
	 * 文本通过'转义.真实的单引号需要如下表示''.两个单引号之间的信息代表纯文本而不会是参数信息.
	 * @param cfgFile
	 */
	public void setI18nFile(File cfgFile){
		this.cfgFile = cfgFile;
		this.lastLoadTime = 0;
	}
	
	public String getLang(){
		return this.lang;
	}
	
	private void reInitInfo(){
		try{
			this.initInfo();
		}catch(Exception e){}
	}
	
	public void initInfo() throws Exception{
		if(this.cfgFile == null){
			throw new Exception("错误配置文件路径!");
		}
		
		InputStream in = null;
		try{
			if(this.cfgFile.lastModified() == this.lastLoadTime){
				return ;
			}
			this.lastLoadTime = this.cfgFile.lastModified();
			
			in = new FileInputStream(this.cfgFile);
			Element root = XmlParser.parse(in);
			in.close();
			
			this.lang = Util.getNodeAttr(root, "lang");
			if(this.lang == null || this.lang.trim().length() == 0){
				this.lang = DEFAULT_LANG;
			}else{
				this.lang = this.lang.trim().toLowerCase();
			}
			Node[] allGroupNode = Util.getElementsByTagName(root, "group");
			int num = allGroupNode.length;
			
			HashMap tempMapping = new HashMap();
			for(int i=0;i<num;i++){
				this.loadGroupInfo(allGroupNode[i],tempMapping);
			}
			this.mapping = tempMapping;
			
			this.triggerListener();
		}catch(Exception e){
			throw e;
		}finally{
			try{
				if(in != null){
					in.close();
				}
			}catch(Exception e){}
		}
	}
	
	private void loadGroupInfo(Node groupNode,HashMap tempMapping) throws Exception{
		String name = Util.getNodeAttr(groupNode, "name");
		if(name != null){
			name = name.trim();
			if(name.length() == 0){
				name = null;
			}
		}
		
		Node[] allInfoNode = Util.getElementsByTagName(groupNode, "info");
		int num = allInfoNode.length;
		
		for(int i=0;i<num;i++){
			this.loadOneInfo(name,allInfoNode[i],tempMapping);
		}
	}
	
	private void loadOneInfo(String groupName,Node infoNode,HashMap tempMapping) throws Exception{
		String name = Util.getNodeAttr(infoNode, "name");
		if(name == null){
			return ;
		}
		String value = Util.getNodeText(infoNode);
		if(value == null){
			value = "";
		}
		
		if(groupName != null){
			name = groupName+"."+name;
		}
		
		tempMapping.put(name.trim(), value);
	}
	public synchronized String formatInfo(String key){
		this.reInitInfo();
		
		if(this.mapping == null){
			return null;
		}
		String info = (String)this.mapping.get(key);
		if(info == null){
			return null;
		}
		try{
			formater.applyPattern(info);
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}
		return formater.format(null);
	}
	
	public synchronized String formatInfo(String key,String[] paraArr){
		this.reInitInfo();
		
		if(this.mapping == null){
			return null;
		}
		String info = (String)this.mapping.get(key);
		if(info == null){
			return null;
		}
		if(paraArr == null){
			paraArr = new String[0];
		}
		
		try{
			formater.applyPattern(info);
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}
		
		if(formater.getFormatsByArgumentIndex().length != paraArr.length){
			System.out.println("warnning:error para num.pattern="+info);
		}
		return formater.format(paraArr);
	}
	
	public void clearInfo(){
		this.cfgFile = null;
		this.formater = null;
		this.mapping = null;
	}
	
	public interface ILoadListener{
		public void infoReload(I18n source);
	}
}
