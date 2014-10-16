/**
 * ���� Created on 2008-1-14 by edmund
 */
package server.mapping;

import java.util.HashMap;

import com.fleety.base.InfoContainer;

import server.var.VarManageServer;

public abstract class BasicAction implements IAction{
	/**
	 * �ų��Ͱ���ĳЩ��Ϣ�����ų�����������Ϣ����
	 * ���������ʶ�в����κ���Ϣ����ζ�Ű�������
	 * ����ų���ʶ�в����κ���Ϣ����ζ�Ų��ų��κ�
	 */
	public static final String EXCLUDES_FLAG = "excludes";
	public static final String INCLUDES_FLAG = "includes";
	
	
	private HashMap paraInfo = new HashMap();
	
	public String getMsg(){
		return (String)this.paraInfo.get(BasicAction._MSG_FLAG);
	}
	public String getName(){
		return (String)this.paraInfo.get(BasicAction._NAME_FLAG);
	}
	public boolean isSynchronized(){
		Boolean isSync = (Boolean)this.paraInfo.get(BasicAction._SYNC_FLAG);
		if(isSync == null){
			return true;
		}
		return isSync.booleanValue();
	}
	public boolean isFilter(){
		Boolean isFilter = (Boolean)this.paraInfo.get(BasicAction._FILTER_FLAG);
		if(isFilter == null){
			return false;
		}
		return isFilter.booleanValue();
	}
	
	public void addPara(Object key,Object value){
		if(VarManageServer.getSingleInstance().isRunning()){
			value = VarManageServer.getSingleInstance().updateValueByVar(value);
		}
		this.paraInfo.put(key, value);
	}
	
	public Object getPara(Object key){
		return this.paraInfo.get(key);
	}
	
	public Boolean getBooleanPara(Object key){
		if(this.paraInfo == null){
			return null;
		}
		Object obj = this.paraInfo.get(key);
		
		if(obj == null){
			return null;
		}else if(obj instanceof Boolean){
			return (Boolean)obj;
		}
		
		return new Boolean(obj.toString().equalsIgnoreCase("true"));
	}

	public String getStringPara(Object key,boolean isFilterNull){
		String para=getStringPara(key);
		if(isFilterNull)
			para=(para==null?"":para);
		return para;
	}
	
	public String getStringPara(Object key){
		if(this.paraInfo == null){
			return null;
		}
		Object obj = this.paraInfo.get(key);
		
		return obj==null?null:obj.toString();
	}
	
	public ActionContainerServer getActionContainer(){
		return (ActionContainerServer)this.getPara(IAction._ACTION_CONTAINER_FLAG);
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
	
	public void init() throws Exception{
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
	public boolean execute(InfoContainer infos) throws Exception {
		// TODO Auto-generated method stub
		return false;
	}
}
