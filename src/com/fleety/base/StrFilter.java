package com.fleety.base;

import java.util.ArrayList;
import java.util.HashMap;

public class StrFilter {
	public static String filterSqlStr(String str){
		if(str==null){
			return "";
		}
		
		return str.replaceAll("'", "''");
	}
	
	public static String filterLikeSqlStr(String str){
		if(str==null){
			return "";
		}
		
		return str.replaceAll("'", "''").replaceAll("%", "%%");
	}

	public static String filterXmlStr(String str){
		return filterXmlStr(str,false);
	}

	public static String filterXmlStr(String str,boolean allowControlChar){
		return StrFilter.filterXmlStr(str, allowControlChar, null);
	}
	public static String filterXmlStr(String str,boolean allowControlChar,HashMap excludeMapping){
		if(str == null){
			return "";
		}

		StringBuffer buff = new StringBuffer(str.length() + 64);
		char ch;
		for(int i=0;i<str.length();i++){
			ch = str.charAt(i);
			if(Character.getType(ch) == Character.CONTROL){
				if(allowControlChar){
					buff.append(ch);
				}
				continue;
			}

			if(excludeMapping!=null&&excludeMapping.containsKey(new Integer(ch))){
				buff.append(ch);
				continue;
			}
			
			if(ch == '&'){
				buff.append("&amp;");
			}else if(ch == '<'){
				buff.append("&lt;");
			}else if(ch == '>'){
				buff.append("&gt;");
			}else if(ch == '\''){
				buff.append("&apos;");
			}else if(ch == '\"'){
				buff.append("&quot;");
			}else if(ch == 65533){
				buff.append("");
			}else{
				buff.append(ch);
			}
		}
		
		return buff.toString();
//		return str.replaceAll("&", "&amp;").replaceAll("<", "&lt;").replaceAll(">", "&gt;").replaceAll("'", "&apos;").replaceAll("\"", "&quot;");
	}
	
	public static String trim(String str){
		return str == null?null:str.trim();
	}
	
	public static boolean hasValue(String str) {
		if (str == null || str.trim().equals(""))
			return false;
		else
			return true;
	}

	public static String getNotNullString(String str) {
		if (str == null) {
			str = "";
		}
		return str;
	}
	public static String getNotNullIntByZero(String str) {
		if (str == null) {
			str = "0";
		}
		return str;
	}
    public static String formatNumberDecimal(int decimal,double source){
        String format="%."+decimal+"f";
        String result = String.format(format,source);
        return result;
    }
    
    public static String[] split(String info,String split){
		if(info == null || split == null){
			return null;
		}
		
		ArrayList list = new ArrayList(64);
		int pIndex = 0,index = 0, splitLen = split.length();
		while((index = info.indexOf(split, pIndex)) >= 0){
			list.add(info.substring(pIndex,index));
			index += splitLen;
			pIndex = index;
		}
		list.add(info.substring(pIndex));
		
		String[] infoarray = new String[list.size()];
		list.toArray(infoarray);
		return infoarray;
	}
    
	public static String getPageSql(String sql,String orderBySql,int curPage,int pageSize){
        int startIndex = pageSize * (curPage - 1) + 1;
        int endIndex = startIndex + pageSize - 1;        
        StringBuffer buff = new StringBuffer(256);           
        buff.append("select * from (");
        buff.append("select b.*,rownum row_num from (");
        buff.append("select * from (" + sql + ") a "+orderBySql);
        buff.append(") b");
        buff.append(") c where row_num between " + startIndex + " and "
                + endIndex + " " +orderBySql);
        System.out.println(buff);    
        return buff.toString();
    }
    
    public static String javaString2JsVarFilter(String javaString){
        javaString=javaString.replace("\"", "\\\"");
        javaString=javaString.replace("'", "\\'");
        return javaString;
    }
	public static void main(String[] argv){
		
	}
}
