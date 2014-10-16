/**
 * 绝密 Created on 2008-6-3 by edmund
 */
package server.i18n;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import com.fleety.base.i18n.I18n;
import com.fleety.base.i18n.I18n.ILoadListener;
import com.fleety.server.BasicServer;

public class I18nServer extends BasicServer implements ILoadListener{
	public static I18nServer singleInstance = null;
	public static I18nServer getSingleInstance(){
		if(singleInstance == null){
			synchronized(I18nServer.class){
				if(singleInstance == null){
					singleInstance = new I18nServer();
				}
			}
		}
		return singleInstance;
	}

	protected HashMap i18nMapping = null;
	private String[] langArr = null;
	public boolean startServer(){
		try{
			String i18nPathSet = this.getStringPara("i18n_path");
			if(i18nPathSet == null){
				return false;
			}
			String[] i18nPathArr = i18nPathSet.split(",");
			int num = i18nPathArr.length;
			
			HashMap tempMapping = new HashMap();
			I18n i18n;
			ArrayList infoList;
			for(int i=0;i<num;i++){
				File f = new File(i18nPathArr[i]);
				if(!f.exists()){
					return false;
				}
				i18n = new I18n(f);
				i18n.initInfo();
				
				//对同一语言文件,支持多个配置文件,后者覆盖前者
				infoList = (ArrayList)tempMapping.get(i18n.getLang());
				if(infoList == null){
					infoList = new ArrayList(4);
					tempMapping.put(i18n.getLang(), infoList);
				}
				infoList.add(i18n);
				i18n.addLoadListener(this);
			}

			this.langArr = new String[tempMapping.size()];
			tempMapping.keySet().toArray(this.langArr);
			this.i18nMapping = tempMapping;
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}

		this.isRunning = true;
		return true;
	}
	
	public String[] getAllLang(){
		return this.langArr;
	}
	
	public String formatInfo(String key){
		return this.formatInfo(key,I18n.DEFAULT_LANG);
	}
	
	public String formatInfo(String key,String lang){
		if(this.i18nMapping == null){
			return null;
		}
		List infoList = (List)this.i18nMapping.get(lang);
		if(infoList == null){
			infoList = (List)this.i18nMapping.get(I18n.DEFAULT_LANG);
			if(infoList != null){
				this.i18nMapping.put(lang, infoList);
			}
		}
		
		if(infoList == null){
			return null;
		}
		
		String resultStr = null;
		I18n i18n;
		for(int i=infoList.size()-1;i>=0;i--){
			i18n = (I18n)infoList.get(i);
			resultStr = i18n.formatInfo(key);
			if(resultStr != null){
				break;
			}
		}
		return resultStr;
	}
	
	public String formatInfo(String key,String[] paraArr){
		return this.formatInfo(key, paraArr, I18n.DEFAULT_LANG);
	}
	
	public String formatInfo(String key,String[] paraArr,String lang){
		if(this.i18nMapping == null){
			return null;
		}
		List infoList = (List)this.i18nMapping.get(lang);
		if(infoList == null){
			infoList = (List)this.i18nMapping.get(I18n.DEFAULT_LANG);
			if(infoList != null){
				this.i18nMapping.put(lang, infoList);
			}
		}
		
		if(infoList == null){
			return null;
		}		

		String resultStr = null;
		I18n i18n;
		for(int i=infoList.size()-1;i>=0;i--){
			i18n = (I18n)infoList.get(i);
			resultStr = i18n.formatInfo(key,paraArr);
			if(resultStr != null){
				break;
			}
		}
		return resultStr;
	}
	
	/**
	 * 使用当前服务中存在所有语言格式化这个key锁所对应的信息.
	 * @param key
	 * @return  以语言作为key,格式化后的文本作为值的mapping
	 */
	public HashMap formatInfoWithAllLang(String key){
		String[] arr = this.langArr;
		int num = arr.length;
		HashMap mapping = new HashMap();
		for(int i=0;i<num;i++){
			mapping.put(arr[i], this.formatInfo(key, arr[i]));
		}
		return mapping;
	}

	/**
	 * 使用当前服务中存在所有语言格式化这个key锁所对应的信息.
	 * @param key
	 * @return  以语言作为key,格式化后的文本作为值的mapping
	 */
	public HashMap formatInfoWithAllLang(String key,String[] paraArr){
		String[] arr = this.langArr;
		int num = arr.length;
		HashMap mapping = new HashMap();
		for(int i=0;i<num;i++){
			mapping.put(arr[i], this.formatInfo(key, paraArr, arr[i]));
		}
		return mapping;
	}

	private String modeFlag = "#";
	public void setModeFlag(String modeFlag){
		this.modeFlag = modeFlag;
	}
	public String getModeFlag(){
		return this.modeFlag;
	}
	public String formatStrWithMode(String text){
		return this.formatStrWithMode(text, I18n.DEFAULT_LANG);
	}
	
	public String formatStrWithMode(String text,String language){
		return this.updateValueByI18n(text,language);
	}
	
	private String updateValueByI18n(String _value,String language){
		if(!this.isRunning()){
			return _value;
		}
		if(!(_value instanceof String)){
			return _value;
		}
		String value = (String)_value;
		if(_value == null){
			return value;
		}
		
		int flagLen = this.modeFlag.length();
		
		String varName,varValue;
		int index1,index2;
		index1 = value.indexOf(modeFlag);
		while(index1 >= 0){
			index2 = value.indexOf(modeFlag, index1+flagLen);
			if(index2 > 0){
				varName = value.substring(index1+flagLen, index2);
				varValue = this.formatInfo(varName,language);
				if(varValue == null){
					index1 = index1+flagLen;
				}else{
					value = value.substring(0, index1)+varValue+value.substring(index2+flagLen);
					index1 = index1 + varValue.length();
				}
			}else{
				break;
			}
			index1 = value.indexOf(modeFlag,index1);
		}

		return value;
	}
	

	public void stopServer(){
		if(this.i18nMapping != null){
			this.i18nMapping.clear();
		}
		this.i18nMapping = null;
		this.langArr = null;
		this.isRunning = false;
	}
	
	public synchronized void infoReload(I18n source){
		if(this.i18nMapping == null){
			this.i18nMapping = new HashMap();
		}
		
		String lang = source.getLang();
		List infoList = (List)this.i18nMapping.get(lang);
		if(infoList != null && infoList.contains(source)){
			return ;
		}
		
		Object key;
		List value;
		Iterator keyIterator = this.i18nMapping.keySet().iterator();
		boolean isContinue = true;
		while(isContinue && keyIterator.hasNext()){
			key = keyIterator.next();
			value = (List)this.i18nMapping.get(key);
			
			for(int i=0;i<value.size();i++){
				if(value.get(i) == source){
					value.remove(i);
					isContinue = false;
					break;
				}
			}
		}
		
		//此处存在先后的覆盖问题.只在存在修改语言类别时可能发生错误.
		infoList = (List)this.i18nMapping.get(lang);
		if(infoList == null){
			infoList = new ArrayList(4);
			this.i18nMapping.put(lang, infoList);
		}
		infoList.add(source);
		
		String[] tempLangArr = new String[this.i18nMapping.size()];
		this.i18nMapping.keySet().toArray(tempLangArr);
		this.langArr = tempLangArr;
	}
}
