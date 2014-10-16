package com.fleety.base.ext;

/**
 * �ַ�����չ�࣬�ṩһЩJava��׼���ַ�������û�д��ķ���
 * ���㿪��ʱʹ��
 * �ο�Ruby�����е�String��ķ������ܱ�д
 * ������ʽҲ�ǲο�Ruby��String��ķ���
 * @author jimy.gu
 *
 */

public class ExtString {
	/**
	 * ���������ӳ��ַ�����û�зָ���
	 * ��{"a", "b", "c"} => "abc"
	 * @param arr String����
	 * @return ���Ӻ���ַ���
	 */
	public static String join(String[] arr) {
		return ExtString.join(arr, "");
	}
	
	/**
	 * ���������ӳ��ַ���
	 * ��: {"a", "b", "c"} => ��a,b,c"
	 * @param arr String����
	 * @param separator �ָ��ַ���
	 * @return ���Ӻ���ַ���
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
	 * ���������ӳ��ַ�����û�зָ���
	 * �� join({1, 2, 3}) => "123"
	 * @param arr int����
	 * @return ���Ӻ���ַ���
	 */
	public static String join(int[] arr) {
		return ExtString.join(arr, "");
	}
	
	/**
	 * ���������ӳ��ַ���
	 * ��: join({1, 2, 3}, ",") => ��1,2,3"
	 * @param arr int����
	 * @param separator �ָ��ַ���
	 * @return ���Ӻ���ַ���
	 */
	public static String join(int[] arr, String separator) {
		return ExtString.join(ExtString.toStringArray(arr), separator);
	}
	
	/**
	 * ת��������ĸ��д���ַ���
	 * ��������ģ�����
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
	 * �ַ����˷�
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
	 * ���ַ����ŵ�ָ���ַ���������
	 * center("hello", 4, "-")     => "hello"
	 * center("hello", 20, "12345") => "1234512hello12345123"
	 * center("hello", 20, "-")    => "-------hello--------"
	 * @param str ԭ�ַ���
	 * @param width ����ĳ���
	 * @param pad ����ַ���
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
	 * Ĭ���Կո����
	 * @param str
	 * @param width
	 * @return
	 */
	public static String ljust(String str, int width) {
		return ExtString.ljust(str, width, " ");
	}
	
	/**
	 * ������ַ�����䵽����
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
	 * Ĭ���Կո����
	 * @param str
	 * @param width
	 * @return
	 */
	public static String rjust(String str, int width) {
		return ExtString.rjust(str, width, " ");
	}
	
	/**
	 * ������ַ�����䵽ǰ��
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
	 * ��int[]����ת����String[]����
	 * @param arr int[]����
	 * @return String[]����
	 */
	public static String[] toStringArray(int[] arr) {
		String[] result = new String[arr.length];
		for (int i = 0; i < arr.length; i++)
			result[i] = arr[i] + "";
		
		return result;
	}
}
