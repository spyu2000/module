package com.fleety.base;

import java.io.UnsupportedEncodingException;

public class FleetyBase64 {
	private String base64Char = "0123456789:;ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
	private char[] base64EncodeChars = null;

	private void init() {
		base64EncodeChars = base64Char.toCharArray();
	}

	public String encode(byte[] data) {
		return encode(data, true);
	}
	public String encode(byte[] data,int offSet,int len) {
		return encode(data,offSet,len, true);
	}
	public String encode(byte[] data, boolean isAppend) {
		return encode(data, 0,data.length,isAppend,'=');
	}
	public String encode(byte[] data, int offSet,int len,boolean isAppend) {
		return encode(data,offSet,len,isAppend,'=');
	}
	// 编码
	//每三个字节用4个字符表示，如果总长度不是3的整数倍，则通过isAppend判断是否在后面加上对应字符a
	public String encode(byte[] data,int offSet,int len, boolean isAppend,char a) {
		if (base64EncodeChars == null)
			init();
		StringBuffer sb = new StringBuffer();
		int i = offSet;
		int b1, b2, b3;
		while (i < len) {
			b1 = data[i++] & 0xff;
			if (i == len) {
				sb.append(base64EncodeChars[b1 >>> 2]);
				sb.append(base64EncodeChars[(b1 & 0x3) << 4]);
				if (isAppend) {
					sb.append(""+a+a);
				}
				break;
			}
			b2 = data[i++] & 0xff;
			if (i == len) {
				sb.append(base64EncodeChars[b1 >>> 2]);
				sb.append(base64EncodeChars[((b1 & 0x03) << 4)
						| ((b2 & 0xf0) >>> 4)]);
				sb.append(base64EncodeChars[(b2 & 0x0f) << 2]);
				if (isAppend) {
					sb.append(""+a);
				}
				break;
			}
			b3 = data[i++] & 0xff;
			sb.append(base64EncodeChars[b1 >>> 2]);
			sb.append(base64EncodeChars[((b1 & 0x03) << 4)
					| ((b2 & 0xf0) >>> 4)]);
			sb.append(base64EncodeChars[((b2 & 0x0f) << 2)
					| ((b3 & 0xc0) >>> 6)]);
			sb.append(base64EncodeChars[b3 & 0x3f]);
		}
		return sb.toString();
	}

	// 解码
	public byte[] decode(String value) throws UnsupportedEncodingException {
		byte[] ass = null;
		try {

			int len = value.length();
			int addLen = 0;
			if (value.contains("=") && value.contains("==") == false) {
				addLen = 1;
				value = value.substring(0, len - addLen);
			}

			if (value.contains("==")) {
				addLen = 2;
				value = value.substring(0, len - addLen);
			}
			int[] base10 = new int[value.length()];
			for (int i = 0; i < base10.length; i++) {
				base10[i] = base64Char.indexOf(value.charAt(i));
			}
			int[] base2 = new int[len * 6];
			int k = 5;
			int count = 0;
			for (int i = 0; i < base10.length; i++) {
				int result = base10[i];
				int[] temp = new int[6];
				while (result != 0) {
					temp[k] = result % 2;
					result = result / 2;
					k--;
				}
				k = 5;
				int d = 0;
				for (int q = count; q < count + 6; q++) {
					base2[q] = temp[d];
					d++;
				}
				count = count + 6;
			}

			count = 0;
			ass = new byte[(base2.length) / 8 - addLen];
			int result = 0;
			int x = 0;
			int index = 0;
			while (count < ass.length * 8) {
				x = 0;
				for (int i = count; i < (count + 8) && i < base2.length; i++) {
					if (base2[i] == 1) {
						result = result + (int) Math.pow(2, 7 - x);
					}
					x++;
				}
				ass[index] = (byte) result;
				result = 0;
				index++;
				count = count + 8;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ass;
	}

	public String getBase64Char() {
		return base64Char;
	}

	public boolean setBase64Char(String base64Char) {
		if (base64Char==null||base64Char.length() != 64) {
			return false;
		} else
		{
			this.base64Char = base64Char;
			this.base64EncodeChars=null;
			return true;
		}
	}
}