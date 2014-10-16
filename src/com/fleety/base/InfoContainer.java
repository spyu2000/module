/**
 * ¾øÃÜ Created on 2007-10-25 by edmund
 */
package com.fleety.base;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

public class InfoContainer implements Serializable{
	private HashMap infos = null;

	public InfoContainer(){
		this.infos = new HashMap();
	}

	public InfoContainer setInfo(Object key, Object info){
		this.infos.put(key, info);
		return this;
	}
	
	public Object removeInfo(Object key){
		return this.infos.remove(key);
	}

	public Object getInfo(Object key){
		return this.infos.get(key);
	}
	
	public Object[] getAllKey(){
		return this.infos.keySet().toArray();
	}
	
	public Iterator keys(){
		return this.infos.keySet().iterator();
	}
	
	public Date getDate(Object key)
	{
		Object obj = this.getInfo(key);
		if(obj instanceof Date){
			return (Date)obj;
		}
		return null;
	}

	public String getString(Object key){
		Object obj = this.getInfo(key);
		if (obj == null){
			return null;
		} else{
			return obj.toString();
		}
	}

	public String getStringNotNull(Object key){
		Object obj = this.getInfo(key);
		if (obj == null){
			return "";
		} else{
			return obj.toString();
		}
	}

	public Integer getInteger(Object key){
		Object obj = this.getInfo(key);
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
	
	public Boolean getBoolean(Object key){
		Object obj = this.getInfo(key);
		if (obj == null){
			return null;
		} else if(obj instanceof Boolean){
			 return (Boolean)obj;
		}else{
			return new Boolean(obj.toString().equalsIgnoreCase("true"));
		}
	}
	
	public Double getDouble(Object key){
		Object obj = this.getInfo(key);
		if (obj == null){
			return null;
		} else if (obj instanceof Double){
			return (Double) obj;
		} else{
			try{
				return new Double(obj.toString());
			} catch (Exception e){
				e.printStackTrace();
				return null;
			}
		}
	}
	public Float getFloat(Object key){
		Object obj = this.getInfo(key);
		if (obj == null){
			return null;
		} else if (obj instanceof Float){
			return (Float) obj;
		} else{
			try{
				return new Float(obj.toString());
			} catch (Exception e){
				e.printStackTrace();
				return null;
			}
		}
	}

	public Long getLong(Object key){
		Object obj = this.getInfo(key);
		if (obj == null){
			return null;
		} else if (obj instanceof Long){
			return (Long) obj;
		} else{
			try{
				return new Long(obj.toString());
			} catch (Exception e){
				e.printStackTrace();
				return null;
			}
		}
	}
	
	public void addAll(InfoContainer info){
		this.infos.putAll(info.infos);
	}
	
	public InfoContainer cloneObj(){
		InfoContainer newObj = new InfoContainer();
		
		newObj.infos.putAll(this.infos);
		
		return newObj;
	}
}
