/**
 * 绝密 Created on 2010-7-2 by edmund
 */
package server.var.help;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * is_name配置为true代表取机器的名称而不是IP地址
 * ip_part只在获取ip时有效,代表取ip的那一段,范围在0-3
 * ip 配置可接纳的结果值
 * @title
 * @description
 * @version      1.0
 * @author       edmund
 *
 */
public class IpValueCreator extends BasicValueCreator{
	public Object createValue(){
		Boolean isNameObj = this.getBooleanPara("is_name");
		boolean isName = (isNameObj != null && isNameObj.booleanValue());
		Integer ipPartObj = this.getIntegerPara("ip_part");
		int ipPart = -1;
		if(ipPartObj != null){
			ipPart = ipPartObj.intValue();
		}
		
		Object ipInfo = this.getPara("ip");
		if(ipInfo == null){
			return null;
		}
		
		List ipList = null;
		if(ipInfo instanceof List){
			ipList = (List)ipInfo;
		}else{
			ipList = new ArrayList(1);
			ipList.add(ipInfo);
		}
		
		try{
			InetAddress ip;
			String _value ;
			String[] arr;
			Enumeration netInterfaces = NetworkInterface.getNetworkInterfaces(),netEnum;
		    while (netInterfaces.hasMoreElements()) {
		    	NetworkInterface ni = (NetworkInterface) netInterfaces.nextElement();
		    	netEnum = ni.getInetAddresses();
		    	while(netEnum.hasMoreElements()){
			    	ip = (InetAddress)netEnum.nextElement();
			    	if(isName){
			    		_value = ip.getHostName();
			    	}else{
			    		_value = ip.getHostAddress();
			    		arr = _value.split("\\.");
			    		if(ipPart >= 0 && ipPart < arr.length){
			    			_value = arr[ipPart];
			    		}
			    	}
			    	
			    	if(ipList.contains(_value)){
			    		return _value;
			    	}
		    	}
		    }
	    }catch(Exception e){
	    	e.printStackTrace();
	    }
	    
		return null;
	}

	
	public static void main(String[] argv){
		new IpValueCreator().createValue();
	}
}
