package com.fleety.util.pool.db.redis;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.HashMap;

import org.json.JSONObject;

import com.fleety.base.GeneralConst;

/**
 * 注意：只支持get、set方法，不支持boolean型的is接口
 * @author fleety
 *
 */
public class RedisTableBean implements Cloneable{
	private String uid = null;
	private long expire = 0;
	
	public String getTableName(){
		return "T_"+this.getClass().getSimpleName();
	}
	
	public String getUid() {
		return uid;
	}
	public void setUid(String uid) {
		this.uid = uid;
	}
	
	public long getExpire() {
		return expire;
	}

	public void setExpire(long expire) {
		this.expire = expire;
	}

	public String toString(){
		StringBuffer strBuff = new StringBuffer(256);
		strBuff.append("uid="+this.uid+" ");
		Field[] fArr = this.getClass().getDeclaredFields();
		Field f;
		String name;
		Method method;
		Object val;
		for(int i=0;i<fArr.length;i++){
			f = fArr[i];
			name = f.getName();
			if(name.equals("uid")){
				continue;
			}
			try{
				method = this.getClass().getMethod("get"+name.substring(0, 1).toUpperCase()+name.substring(1), null);
				if(method != null){
					val = method.invoke(this,null);
					if(val instanceof Date){
						val = GeneralConst.YYYYMMDDHHMMSS.format((Date)val);
					}
					strBuff.append(name+"="+val+" ");
				}
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		return strBuff.toString();
	}
	
	public String toJSONString(){
		Class cls = this.getClass(),rCls;
		Method[] mArr = cls.getMethods();
		JSONObject json = new JSONObject();
		String name;
		Method method;
		Object val;
		
		for(int i=0;i<mArr.length;i++){
			method = mArr[i];
			name = method.getName();
			if(name.equals("getClass") || name.equals("getTableName") || name.equals("getExpire")){
				continue;
			}
			rCls = method.getReturnType();
			if(name.length() < 4 || !name.startsWith("get") && method.getReturnType() != void.class || rCls == void.class){
				continue;
			}
			try{
				val = method.invoke(this, null);
				if(val instanceof Date){
					val = new Long(((Date)val).getTime());
				}
				name = name.substring(3);
				name = name.substring(0, 1).toLowerCase()+name.substring(1);
				json.put(name, val);
			}catch(Exception e){
				continue;
			}
		}
		
		return json.toString();
	}
	public void parseJSONString(String jsonStr) throws Exception{
		JSONObject json = new JSONObject(jsonStr);
		String[] nameArr = JSONObject.getNames(json);
		String name;

		Class cls = this.getClass(),fCls;
		HashMap mapping = new HashMap();
		Method[] mArr = cls.getMethods();
		for(int i=0;i<mArr.length;i++){
			mapping.put(mArr[i].getName(), mArr[i]);
		}
		Method method;
		Object val = null;
		for (int i = 0; i < nameArr.length; i++) {
			name = nameArr[i];
			method = (Method) mapping.get("set"
					+ name.substring(0, 1).toUpperCase() + name.substring(1));
			if (method == null || method.getParameterTypes().length != 1) {
				continue;
			}
			try {
				fCls = method.getParameterTypes()[0];
				if(fCls == Date.class){
					val = new Date(json.getLong(name));
				}else if (fCls == Integer.class || fCls == int.class) {
					val = new Integer(json.getInt(name));
				} else if (fCls == Short.class || fCls == short.class) {
					val = new Short((short) json.getInt(name));
				} else if (fCls == Byte.class || fCls == byte.class) {
					val = new Byte((byte) json.getInt(name));
				} else if (fCls == Long.class || fCls == long.class) {
					val = new Long(json.getLong(name));
				} else if (fCls == Character.class || fCls == char.class) {
					val = new Character(json.getString(name).charAt(0));
				} else if (fCls == Boolean.class || fCls == boolean.class) {
					val = new Boolean(json.getBoolean(name));
				} else if (fCls == Double.class || fCls == double.class) {
					val = new Double(json.getDouble(name));
				} else if (fCls == Float.class || fCls == float.class) {
					val = new Float(json.getDouble(name));
				} else {
					val = json.getString(name);
				}
				method.invoke(this, new Object[] { val });
			} catch (Exception e) {
				e.printStackTrace();
				continue;
			}
		}
	}
	
	public Object clone() throws CloneNotSupportedException{
		return super.clone();
	}
}
