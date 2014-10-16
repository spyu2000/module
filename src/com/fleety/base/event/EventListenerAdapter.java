/**
 * ���� Created on 2008-12-5 by edmund
 */
package com.fleety.base.event;

import java.util.HashMap;

public abstract class EventListenerAdapter  implements IEventListener{
	/**
	 * �ų��Ͱ���ĳЩ��Ϣ�����ų�����������Ϣ����
	 * ���������ʶ�в����κ���Ϣ����ζ�Ű�������
	 * ����ų���ʶ�в����κ���Ϣ����ζ�Ų��ų��κ�
	 */
	public static final String EXCLUDES_FLAG = "excludes";
	public static final String INCLUDES_FLAG = "includes";
	
	private HashMap mapping = null;
	public void setPara(Object key, Object value){
		if(key == null || value == null){
			return ;
		}
		if(mapping == null){
			mapping = new HashMap();
		}
		
		mapping.put(key, value);
	}
	
	public Object getPara(Object key){
		if(mapping == null){
			return null;
		}
		return mapping.get(key);
	}
	private String includeStr = null;
	private String excludeStr = null;
	public boolean isInclude(String msg){
		if(excludeStr != null && excludeStr.indexOf(","+msg+",") >= 0){
			return false;
		}
		if(includeStr != null && includeStr.indexOf(","+msg+",") < 0){
			return false;
		}
		
		return true;
	}
	
	public void init(){
		//��ʼ�����ų�����Ϣ
		this.excludeStr = (String)this.getPara(EXCLUDES_FLAG);
		if(this.excludeStr != null){
			if(this.excludeStr.trim().length() == 0){
				this.excludeStr = null;
			}else{
				this.excludeStr = "," + this.excludeStr + ",";
			}
		}
		
		//��ʼ������������Ϣ
		this.includeStr = (String)this.getPara(INCLUDES_FLAG);
		if(this.includeStr != null){
			if(this.includeStr.trim().length() == 0){
				this.includeStr = null;
			}else{
				this.includeStr = "," + this.includeStr + ",";
			}
		}
	
		
	}
	
	public void destroy(){
		
	}
}
