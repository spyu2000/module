package com.fleety.base.ext;

/**
 * 字符串扩展类，提供一些Java标准库字符串类中没有带的方法
 * 方便开发时使用
 * 参考Ruby语言中的String类的方法功能编写
 * 命名方式也是参考Ruby的String类的方法
 * @author jimy.gu
 *
 */

public class ExtString {
	/**
	 * 把数组连接成字符串，没有分隔符
	 * 如{"a", "b", "c"} => "abc"
	 * @param arr String数组
	 * @return 连接后的字符串
	 */
	public static String join(String[] arr) {
		return ExtString.join(arr, "");
	}
	
	/**
	 * 把数组连接成字符串
	 * 如: {"a", "b", "c"} => “a,b,c"
	 * @param arr String数组
	 * @param separator 分隔字符串
	 * @return 连接后的字符串
	 */
	public static String join(String[] arr, String separator) {
		StringBuffer sb = new StringBuffer();
		if (arr.length == 0)
			return "";
		else if (arr.length == 1)
			return arr[0];
		else {
			sb.append(arr[0]);
			for (int i = 1; i < arr.length; i++) {
				sb.append(separator);
				sb.append(arr[i]);
			}
			
			return sb.toString();
		}
	}
	
	/**
	 * 把数组连接成字符串，没有分隔符
	 * 如 join({1, 2, 3}) => "123"
	 * @param arr int数组
	 * @return 连接后的字符串
	 */
	public static String join(int[] arr) {
		return ExtString.join(arr, "");
	}
	
	/**
	 * 把数组连接成字符串
	 * 如: join({1, 2, 3}, ",") => “1,2,3"
	 * @param arr int数组
	 * @param separator 分隔字符串
	 * @return 连接后的字符串
	 */
	public static String join(int[] arr, String separator) {
		return ExtString.join(ExtString.toStringArray(arr), separator);
	}
	
	/**
	 * 转换成首字母大写的字符串
	 * 如果是中文，不变
	 * @param str
	 * @return
	 */
	public static String capitalize(String str) {
		if (str == null) {
			return null;
		} else if (str.length() == 0) {
			return "";
		} else {
			return str.substring(0, 1).toUpperCase() + str.substring(1);
		}
	}
	
	/**
	 * 字符串乘法
	 * multipy("abc", 3) => "abcabcabc"
	 * @param str
	 * @param copies
	 * @return
	 */
	public static String multipy(String str, int copies) {
		if (copies <= 0)
			return str;
		
		if (str == null)
			return str;
		
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < copies; i++) {
			sb.append(str);
		}
		
		return sb.toString();
	}
	
	/**
	 * 把字符串放到指定字符串的中心
	 * center("hello", 4, "-")     => "hello"
	 * center("hello", 20, "12345") => "1234512hello12345123"
	 * center("hello", 20, "-")    => "-------hello--------"
	 * @param str 原字符串
	 * @param width 填充后的长度
	 * @param pad 填充字符串
	 * @return
	 */
	public static String center(String str, int width, String pad) {
		if (str.length() >= width) {
			return str;
		} else {
			String base = ExtString.multipy(pad, width / pad.length() + 1);
			int frontLength = (width - str.length()) / 2;
			return base.substring(0, frontLength) + str + base.substring(0, width - frontLength - str.length());
		}
	}
	
	/**
	 * 默认以空格填充
	 * @param str
	 * @param width
	 * @return
	 */
	public static String ljust(String str, int width) {
		return ExtString.ljust(str, width, " ");
	}
	
	/**
	 * 把填充字符串填充到后面
	 * ljust("hello", 4, " ") => "hello"
	 * ljust("hello", 20, " ") => "hello               "
	 * ljust("hello", 20, "*") => "hello***************"
	 * ljust("hello", 20, " dolly") => "hello dolly dolly do"
	 * @param str
	 * @param width
	 * @param pad
	 * @return
	 */
	public static String ljust(String str, int width, String pad) {
		if (str == null || str.length() >= width)
			return str;

		if (pad == null || pad.length() == 0)
			return str;
		
		String base = str + ExtString.multipy(pad, width / pad.length() + 1);
		return base.substring(0, width);
	}
	
	/**
	 * 默认以空格填充
	 * @param str
	 * @param width
	 * @return
	 */
	public static String rjust(String str, int width) {
		return ExtString.rjust(str, width, " ");
	}
	
	/**
	 * 把填充字符串填充到前面
	 * rjust("hello", 4)             => "hello"
	 * rjust("hello", 20, " ")       => "               hello"
	 * rjust("hello", 20, "")        => "hello"
	 * rjust("hello", 20, "padding") => "paddingpaddingphello"
	 * @param str
	 * @param width
	 * @param pad
	 * @return
	 */
	public static String rjust(String str, int width, String pad) {
		if (str == null || str.length() >= width)
			return str;
		
		if (pad == null || pad.length() == 0)
			return str;
		
		String base = ExtString.multipy(pad, width / pad.length() + 1);
		
		return base.substring(0, width - str.length()) + str;
	}
	
	/**
	 * 把int[]数组转换成String[]数组
	 * @param arr int[]数组
	 * @return String[]数组
	 */
	public static String[] toStringArray(int[] arr) {
		String[] result = new String[arr.length];
		for (int i = 0; i < arr.length; i++)
			result[i] = arr[i] + "";
		
		return result;
	}
}
