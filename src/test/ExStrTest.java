/**
 * ���� Created on 2008-6-3 by edmund
 */
package test;

import java.net.InetAddress;

import com.fleety.base.ext.ExtString;

public class ExStrTest
{
	/**
	 * @param args
	 */
	public static void main(String[] args){
//		test1();
	}
	
	public static void test1(){
//		System.out.println(ExtString.center("���", 10, "12345"));
//		System.out.println(ExtString.center("���", 10, ""));
//		System.out.println(ExtString.ljust("aa", 9,""));
//		System.out.println(ExtString.rjust("aa", 9,"123"));
//		System.out.println(ExtString.capitalize("nihao"));
//		System.out.println(ExtString.capitalize("���"));
//		System.out.println(ExtString.join(new int[]{1,2,4,6,820,42342},","));
//		System.out.println(ExtString.join(new String[]{"432","4132"}, "-"));
		
		long t = System.currentTimeMillis();
		
		long t1 = System.currentTimeMillis();
		for(int i=0;i<1000000;i++){
			ExtString.multipy(" ", 20);
		}

		long t2 = System.currentTimeMillis();
		for(int i=0;i<1000000;i++){
			ExtString.multipy(" ", 20);
		}
		long t3 = System.currentTimeMillis();
		
		System.out.println("ԭ:"+(t2-t1)+"�Ż�:"+(t3-t2));
		
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	public static String multipy(String str, int copies) {
		if (copies <= 0)
			return str;
		
		if (str == null)
			return str;

		int strLen = str.length();
		StringBuffer sb = new StringBuffer(strLen*copies);
		int count = 0;
		int totalNum = copies;
		while(totalNum > 0){
			if(count == 0){
				sb.append(str);
				count ++;
				totalNum -- ;
			}else{
				if(count >= totalNum){
					sb.append(sb.substring(0, totalNum*strLen));
					count += totalNum;
					totalNum = 0;
				}else{
					sb.append(sb);
					totalNum -= count;
					count += count;
				}
			}
		}

		return sb.toString();
	}
}
