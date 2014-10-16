/**
 * ¾øÃÜ Created on 2010-7-2 by edmund
 */
package server.var.help;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import server.var.IValueCreator;

public abstract class BasicValueCreator implements IValueCreator{
	private HashMap paraMapping = null;

	public void addPara(Object key,Object value){
		if(key == null || value == null){
			return ;
		}
		
		if(this.paraMapping == null){
			this.paraMapping = new HashMap();
		}
		Object existValue = this.paraMapping.get(key);
		if(existValue == null){
			this.paraMapping.put(key, value);
		}else if(existValue instanceof List){
			((List)existValue).add(value);
		}else{
			List tempList = new LinkedList();
			tempList.add(existValue);
			tempList.add(value);
			this.paraMapping.put(key, tempList);
		}
	}
	
	public Object getPara(Object key){
		if(this.paraMapping == null){
			return null;
		}
		return this.paraMapping.get(key);
	}
	
	public Boolean getBooleanPara(Object key){
		if(this.paraMapping == null){
			return null;
		}
		Object obj = this.paraMapping.get(key);
		
		if(obj == null){
			return null;
		}else if(obj instanceof Boolean){
			return (Boolean)obj;
		}
		
		return new Boolean(obj.toString().equalsIgnoreCase("true"));
	}
	
	public String getStringPara(Object key){
		if(this.paraMapping == null){
			return null;
		}
		Object obj = this.paraMapping.get(key);
		
		return obj==null?null:obj.toString();
	}
	
	public Integer getIntegerPara(Object key){
		if(this.paraMapping == null){
			return null;
		}
		Object obj = this.paraMapping.get(key);
		
		if (obj == null){
			return null;
		} else if (obj instanceof Integer){
			return (Integer) obj;
		} else{
			try{
				String str = obj.toString().trim();
				if(str.length() == 0){
					return null;
				}
				if(str.startsWith("0x")){
					return new Integer(Integer.parseInt(str.substring(2),16));
				}else{
					return new Integer(str);
				}
			} catch (Exception e){
				e.printStackTrace();
				return null;
			}
		}
	}
}
