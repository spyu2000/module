/**
 * ���� Created on 2007-11-7 by edmund
 */
package com.fleety.base;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Util{

    public static String byteArrToHexString(byte[] bytes){
    	return Util.byteArrToHexString(bytes, 0, bytes.length);
    }
    public static String byteArrToHexString(byte[] bytes,int offset,int len){
        if(bytes == null){
        	return "";
        }
        StringBuffer infoBuff = new StringBuffer(len*2);
        int value;
        for(int i=0;i<len;i++,offset++){
        	if(i>0){
        		infoBuff.append(" ");
        	}
        	value = bytes[offset]&0xff;
            if(value < 16){
            	infoBuff.append("0");
            }
            infoBuff.append(Integer.toHexString(value));
        }
        return infoBuff.toString();
    }
	public static ByteBuffer readURL(URL url){
		if(url == null){
			return null;
		}
		InputStream in = null;
		try{
			URLConnection conn = url.openConnection();
			int len = conn.getContentLength();
			in = new BufferedInputStream(url.openStream());
			byte[] data = new byte[len];
			int count = 0,tempCount = 0;
			while(tempCount >= 0 && count < len){
				tempCount = in.read(data, count, len-count);
				if(tempCount > 0){
					count += tempCount;
				}
			}
			return ByteBuffer.wrap(data, 0, count);
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}finally{
			try{
				if(in != null){
					in.close();
				}
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}
	public static boolean readFull(InputStream in,byte[] buff,int offset,int len) throws Exception{
		if(offset + len > buff.length){
			throw new Exception("Length OverFlow");
		}
		int tCount ;
		while(len > 0){
			tCount = in.read(buff, offset, len);
			if(tCount < 0){
				return false;
			}
			offset += tCount;
			len -= tCount;
		}
		return true;
	}
	public static byte[] readFile(File f){
		if(f == null || !f.exists() || !f.isFile()){
			return null;
		}
		
		int len = (int)f.length();
		int count = 0,tempCount;
		
		InputStream in = null;
		try{
			byte[] data = new byte[len];
			in = new BufferedInputStream(new FileInputStream(f));
			while(count < len){
				tempCount = in.read(data, count, len-count);
				if(tempCount < 0){
					throw new Exception("File Eof Error!");
				}
				count += tempCount;
			}
			return data;
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}finally{
			try{
				if(in != null){
					in.close();
				}
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}
	/***************************************************************************
	 * DOM��װ����
	 */

	/**
	 * �õ��ڵ������ֵ
	 * 
	 * @param node
	 * @param attrName
	 * @return
	 */
	public static String getNodeAttr(Node node, String attrName){
		if(node == null || attrName == null){
			return null;
		}
		if(node.getNodeType() != Node.ELEMENT_NODE){
			return null;
		}
		
		Node attrNode = node.getAttributes().getNamedItem(attrName);
		if(attrNode == null){
			return null;
		}
		return attrNode.getNodeValue();
	}

	/**
	 * �õ��ڵ������ֵ
	 * 
	 * @param node
	 * @param attrName
	 * @return
	 */
	public static Node[] getAllAttrNode(Node node){
		if(node == null){
			return null;
		}
		NamedNodeMap nodeMap = node.getAttributes();
		if(nodeMap == null){
			return null;
		}
		Node[] nodes = new Node[nodeMap.getLength()];
		for(int i = 0; i < nodeMap.getLength(); i++){
			nodes[i] = nodeMap.item(i);
		}
		return nodes;
	}

	public static Node getSingleElementByTagName(Node ele, String tagName){
		if(!(ele instanceof Element)){
			return null;
		}
		NodeList all = ((Element) ele).getElementsByTagName(tagName);
		if(all.getLength() > 0){
			return all.item(0);
		}
		return null;
	}

	public static Node getSonSingleElementByTagName(Node ele, String tagName){
		if(!(ele instanceof Element)){
			return null;
		}
		NodeList all = ((Element) ele).getElementsByTagName(tagName);
		if(all.getLength() > 0){
			Node node = all.item(0);
			if(node.getParentNode() == ele){
				return node;
			}
		}
		return null;
	}

	/**
	 * ͨ����ʶ�õ���Ӧ�ı�
	 * 
	 * @param parentNode
	 * @param tagName
	 * @return
	 */
	public static String getSingleElementTextByTagName(Node parentNode,
										String tagName){
		String text = Util.getNodeText(Util.getSingleElementByTagName(parentNode, tagName));
		if(text == null){
			text = "";
		}
		return text;
	}

	public static Node[] getElementsByTagName(Node ele, String tagName){
		if(!(ele instanceof Element)){
			return null;
		}
		NodeList all = ((Element) ele).getElementsByTagName(tagName);

		int len = all.getLength();
		Node[] rs = new Node[len];
		for(int i = 0; i < len; i++){
			rs[i] = all.item(i);
		}

		return rs;
	}

	public static Node[] getSonElementsByTagName(Node ele, String tagName){
		if(!(ele instanceof Element)){
			return null;
		}
		NodeList all = ((Element) ele).getElementsByTagName(tagName);

		int len = all.getLength();
		ArrayList list = new ArrayList(len);
		Node node;
		for(int i = 0; i < len; i++){
			node = all.item(i);
			if(node.getParentNode() == ele){
				list.add(node);
			}
		}

		Node[] rs = new Node[list.size()];
		list.toArray(rs);

		return rs;
	}

	/**
	 * �õ��ڵ���ı�ֵ
	 * 
	 * @param node
	 * @return
	 */
	public static String getNodeText(Node node){
		if(node == null){
			return null;
		}
		Node temp = node.getFirstChild();
		if(temp == null){
			return null;
		}
		return temp.getNodeValue();
	}

	/***************************************************************************
	 * �����򻺴��ж�ȡ���ֵĺ���,��ָ���ֽ���.big or little
	 */
	public static int readByte(InputStream in) throws Exception{
		int value = in.read();
		if(value == -1){
			return value;
		}
		return value & 0xFF;
	}

	public static int readShort(InputStream in, boolean isBigEndian)
												throws Exception{
		byte[] buff = new byte[2];
		int num = in.read(buff);
		if(num != 2){
			return -1;
		}

		if(isBigEndian){
			return ((buff[0] & 0xFF) << 8) | (buff[1] & 0xFF);
		} else{
			return ((buff[1] & 0xFF) << 8) | (buff[0] & 0xFF);
		}
	}

	public static int readInt(InputStream in, boolean isBigEndian)
												throws Exception{
		byte[] buff = new byte[4];
		int num = in.read(buff);
		if(num != 4){
			return -1;
		}

		if(isBigEndian){
			return ((buff[0] & 0xFF) << 24) | ((buff[1] & 0xFF) << 16)
					| ((buff[2] & 0xFF) << 8)
					| ((buff[3] & 0xFF) << 0);
		} else{
			return ((buff[3] & 0xFF) << 24) | ((buff[2] & 0xFF) << 16)
					| ((buff[1] & 0xFF) << 8)
					| ((buff[0] & 0xFF) << 0);
		}
	}

	public static long readLong(InputStream in, boolean isBigEndian)
												throws Exception{
		byte[] buff = new byte[8];
		int num = in.read(buff);
		if(num != 8){
			return -1;
		}

		if(isBigEndian){
			return (((long) (buff[0] & 0xFF)) << 56)
					| (((long) (buff[1] & 0xFF)) << 48)
					| (((long) (buff[2] & 0xFF)) << 40)
					| (((long) (buff[3] & 0xFF)) << 32)
					| (((long) (buff[4] & 0xFF)) << 24)
					| (((long) (buff[5] & 0xFF)) << 16)
					| (((long) (buff[6] & 0xFF)) << 8)
					| (((long) (buff[7] & 0xFF)) << 0);
		} else{
			return (((long) (buff[7] & 0xFF)) << 56)
					| (((long) (buff[6] & 0xFF)) << 48)
					| (((long) (buff[5] & 0xFF)) << 40)
					| (((long) (buff[4] & 0xFF)) << 32)
					| (((long) (buff[3] & 0xFF)) << 24)
					| (((long) (buff[2] & 0xFF)) << 16)
					| (((long) (buff[1] & 0xFF)) << 8)
					| (((long) (buff[0] & 0xFF)) << 0);
		}
	}

	public static int getInt(byte[] buff, int offset, boolean isLittleEndian){
		if(isLittleEndian){
			return ((buff[offset] & 0xFF) << 0)
					| ((buff[offset + 1] & 0xFF) << 8)
					| ((buff[offset + 2] & 0xFF) << 16)
					| ((buff[offset + 3] & 0xFF) << 24);
		} else{
			return ((buff[offset] & 0xFF) << 24)
					| ((buff[offset + 1] & 0xFF) << 16)
					| ((buff[offset + 2] & 0xFF) << 8)
					| ((buff[offset + 3] & 0xFF) << 0);
		}
	}

	public static int getShort(byte[] buff, int offset, boolean isLittleEndian){
		if(isLittleEndian){
			return ((buff[offset] & 0xFF) << 0)
					| ((buff[offset + 1] & 0xFF) << 8);
		} else{
			return ((buff[offset] & 0xFF) << 8)
					| ((buff[offset + 1] & 0xFF) << 0);
		}
	}

	public static int getByte(byte[] buff, int offset){
		return buff[offset] & 0xFF;
	}

	/***************************************************************************
	 * ʱ�亯��
	 */
	private static final long MAX_DIFF_TIME_WITH_MINUTE = 30 * 60 * 1000l;

	private static final long MAX_DIFF_TIME_WITH_HOUR = 12 * 60 * 60 * 1000l;

	/**
	 * ������֪�ķ��룬���ϵͳ�������ʱ�䣬��֪���ʱ����ӽ��ĺ���Ӧ�����ʱ��
	 * 
	 * @param minute
	 * @param second
	 * @return
	 */
	public static long getTimeWithMISS(int minute, int second){
		return getTimeWithMISS(minute, second, Calendar.getInstance());
	}

	public static long getTimeWithMISS(int minute, int second,
							Calendar calendar){
		long curTime = calendar.getTimeInMillis();

		calendar.set(Calendar.MINUTE, minute);
		calendar.set(Calendar.SECOND, second);

		long gpsTime = calendar.getTimeInMillis();

		long diff = gpsTime - curTime;
		if(diff > MAX_DIFF_TIME_WITH_MINUTE
				|| -diff > MAX_DIFF_TIME_WITH_MINUTE){
			if(diff > 0){
				// ��ȥ1Сʱ
				gpsTime -= GeneralConst.ONE_HOUR_TIME;
			} else{
				// ����1Сʱ
				gpsTime += GeneralConst.ONE_HOUR_TIME;
			}
		}

		return gpsTime;
	}

	/**
	 * ������֪��Сʱ���룬���ϵͳ�������ʱ�䣬��֪���ʱ����ӽ��ĺ���ӦСʱ�����ʱ��
	 * 
	 * @param hour
	 * @param minute
	 * @param second
	 * @return
	 */
	public static long getTimeWithHH24MISS(int hour, int minute, int second){
		return getTimeWithHH24MISS(hour, minute, second, Calendar.getInstance());
	}

	public static long getTimeWithHH24MISS(int hour, int minute, int second,
								Calendar calendar){
		long curTime = calendar.getTimeInMillis();

		calendar.set(Calendar.HOUR_OF_DAY, hour);
		calendar.set(Calendar.MINUTE, minute);
		calendar.set(Calendar.SECOND, second);

		long gpsTime = calendar.getTimeInMillis();

		long diff = gpsTime - curTime;
		if(diff > MAX_DIFF_TIME_WITH_HOUR
				|| -diff > MAX_DIFF_TIME_WITH_HOUR){
			if(diff > 0){
				// ��ȥ1��
				gpsTime -= GeneralConst.ONE_DAY_TIME;
			} else{
				// ����1��
				gpsTime += GeneralConst.ONE_DAY_TIME;
			}
		}

		return gpsTime;
	}

	/***************************************************************************
	 * �õ����ݿ�ĳ���ֶε����ֵ�ټ�1,�������������ֶ�.
	 */
	private static HashMap avaliableMapping = null;

	public synchronized static long getAvaliableId(Statement stmt,
									String tableName,
									String fieldName)
												throws Exception{
		if(avaliableMapping == null){
			avaliableMapping = new HashMap(8);
		}
		String flag = tableName + "\n" + fieldName;
		Long value = (Long) avaliableMapping.get(flag);
		long resultValue = 1;
		if(value == null){
			String sql = "select max(" + fieldName + ") maxId from "
					+ tableName;
			ResultSet sets = null;
			try{
				sets = stmt.executeQuery(sql);
				if(sets.next()){
					resultValue = sets.getLong("maxId") + 1;
				}
			} catch(Exception e){
				e.printStackTrace();
				return -1;
			} finally{
				try{
					if(sets != null){
						sets.close();
					}
				} catch(Exception e){
				}
			}
		} else{
			resultValue = value.longValue();
		}
		avaliableMapping.put(flag, new Long(resultValue + 1));

		return resultValue;
	}

	/***************************************************************************
	 * BCD�ַ����ֽ�֮���ת������
	 */
	public static byte[] bcdStr2ByteArr(String bcdStr){
		if(bcdStr == null){
			return null;
		}
        if(bcdStr.length()%2==1){
            bcdStr="0"+bcdStr;
        }
		int num = bcdStr.length() / 2;
		byte[] data = new byte[num];

		try{
			for(int i = 0, index = 0; i < num; i++, index += 2){
				data[i] = (byte) (Integer.parseInt(bcdStr.substring(index, index + 2), 16) & 0xFF);
			}
		} catch(Exception e){
			e.printStackTrace();
			return null;
		}

		return data;
	}

	public static String byteArr2BcdStr(byte[] data, int offset, int len){
		if(data == null){
			return null;
		}
		if(offset < 0){
			return null;
		}
		int num = data.length;
		if(num < offset + len){
			return null;
		}
		StringBuffer buff = new StringBuffer(len * 2);

		len += offset;
		int value;
		for(int i = offset; i < len; i++){
			value = data[i] & 0xFF;
			if(value < 16){
				buff.append("0");
			}
			buff.append(Integer.toHexString(value));
		}

		return buff.toString();
	}

	public static String decodeBCD(byte[] data) {
        String tmp = "";

        for (int i = 0; i < data.length; i++) {
            int tmpByte = data[i];
            tmpByte = tmpByte & 0xff;

            int high = (tmpByte >>> 4);
            int low = (tmpByte & 0xf);
            tmp += (high + "" + low + "");
        }

        return tmp;
    }
       /**
     * ��byte����ת��Ϊint
     * 
     * @param b
     *            byte���飬���Ȳ��ܳ���4
     * @return ת���õ���int
     */
    public final static int bytesToInt(byte[] b)
    {
        int ival = 0;
        for (int i = b.length - 1; i >= 0; i--)
        {
            ival = (ival << 8) + new Integer(b[i] & 0xff).intValue();
        }
        return ival;
    }
	public static byte[] IntToBCD(int src)
	{
		String st = Integer.toString(src);
		if (st.length() % 2 != 0)
			st = '0' + st;
		byte[] result = new byte[st.length() / 2];
		for (int i = 0; i < result.length; i++)
		{
			String s = st.substring(2 * i, 2 * (i + 1));
			result[i] = (byte) Integer.parseInt(s, 16);
		}
		return result;
	}
	/***************************************************************************
	 * �õ�һ���쳣�Ķ�ջ�ı���Ϣ
	 * 
	 * @param e
	 * @return
	 */
	public static String getStackStr(Exception e){
		if(e == null){
			return null;
		}
		StringBuffer buff = new StringBuffer(256);
		StackTraceElement[] eles = e.getStackTrace();

		int len = eles.length;
		StackTraceElement ele;
		for(int i = 0; i < len; i++){
			ele = eles[i];
			buff.append(ele.getClassName() + "[" + ele.getMethodName()
					+ "](" + ele.getLineNumber() + ")\n");
		}

		return buff.toString();
	}

	/***************************************************************************
	 * ��������ΪhttpЭ��ͷ����֯����
	 */
	public static final String httpSplitStr = "\r\n";

	public static final String MULTIPART_FORM_DATA_TYPE = "multipart/form-data";
	public static final String FORM_URLENCODED_DATA_TYPE = "application/x-www-form-urlencoded";
	
	public static byte[] encodePost2HttpProtocol(String cmd, byte[] data,
									int offset, int len,
									String host, int port){
		return encodePost2HttpProtocol("1.1", cmd, MULTIPART_FORM_DATA_TYPE, data, offset, len, host, port);
	}

	public static byte[] encodePost2HttpProtocol(String protocol, String cmd,
									byte[] data, int offset,
									int len, String host,
									int port){
		return encodePost2HttpProtocol(protocol, cmd, MULTIPART_FORM_DATA_TYPE, data, offset, len, host, port);
	}
	public static byte[] encodePost2HttpProtocol(String protocol, String cmd,String contentType,
									byte[] data, int offset,
									int len, String host,
									int port){
		StringBuffer buff = new StringBuffer(256);
		buff.append("POST /" + cmd + " HTTP/" + protocol);
		buff.append(httpSplitStr);
		buff.append("Accept: */*");
		buff.append(httpSplitStr);
		buff.append("Content-Type: "+contentType);
		buff.append(httpSplitStr);
		buff.append("Accept-Encoding: gzip, deflate");
		buff.append(httpSplitStr);
		buff.append("Host: " + host + ":" + port);
		buff.append(httpSplitStr);
		buff.append("Content-Length: " + len);
		buff.append(httpSplitStr);
		buff.append("Connection: Keep-Alive");
		buff.append(httpSplitStr);
		buff.append(httpSplitStr);

		byte[] headByteArr = buff.toString().getBytes();
		int headLen = headByteArr.length;
		byte[] result = new byte[headLen + len];
		System.arraycopy(headByteArr, 0, result, 0, headLen);
		System.arraycopy(data, offset, result, headLen, len);

		return result;
	}

	public static byte[] encodeResponse2HttpProtocol(String cmd, byte[] data,
										int offset, int len){
		return encodeResponse2HttpProtocol("1.1", cmd, data, offset, len);
	}

	public static byte[] encodeResponse2HttpProtocol(String protocol,
										String cmd,
										byte[] data,
										int offset, int len){
		StringBuffer buff = new StringBuffer(128);
		buff.append("HTTP/" + protocol + " 200 OK");
		if(cmd != null){
			buff.append(httpSplitStr);
			buff.append("Cmd: " + cmd);
		}
		buff.append(httpSplitStr);
		buff.append("Accept-Ranges: bytes");
		buff.append(httpSplitStr);
		buff.append("Content-Length: " + len);
		buff.append(httpSplitStr);
		buff.append("Connection: Keep-Alive");
		buff.append(httpSplitStr);
		buff.append(httpSplitStr);

		byte[] headByteArr = buff.toString().getBytes();
		int headLen = headByteArr.length;
		byte[] result = new byte[headLen + len];
		System.arraycopy(headByteArr, 0, result, 0, headLen);
		System.arraycopy(data, offset, result, headLen, len);

		return result;
	}
	public static byte[] encodeResponse2HttpProtocol(String protocol, HashMap attrMapping, byte[] data, int offset, int len){
		StringBuffer buff = new StringBuffer(1024);
		buff.append("HTTP/" + protocol + " 200 OK");
		buff.append(httpSplitStr);
		Object key,value;
		if(attrMapping != null){
			for(Iterator itr = attrMapping.keySet().iterator();itr.hasNext();){
				key = itr.next();
				value = attrMapping.get(key);
				if(key == null || value == null){
					continue;
				}
				buff.append(key.toString()+": " + value.toString());
				buff.append(httpSplitStr);
			}
		}
		buff.append("Accept-Ranges: bytes");
		buff.append(httpSplitStr);
		buff.append("Content-Length: " + len);
		buff.append(httpSplitStr);
		buff.append("Connection: Keep-Alive");
		buff.append(httpSplitStr);
		buff.append(httpSplitStr);

		byte[] headByteArr = buff.toString().getBytes();
		int headLen = headByteArr.length;
		byte[] result = new byte[headLen + len];
		System.arraycopy(headByteArr, 0, result, 0, headLen);
		System.arraycopy(data, offset, result, headLen, len);

		return result;
	}

	/***************************************************************************
	 * ��������Ϊ�򵥵��ռǼ�¼ģ��ʹ��
	 */
	private static HashMap streamMapping = null;

	public synchronized static final void openLog(String filePath,
									boolean isAppend)
												throws Exception{
		if(streamMapping == null){
			streamMapping = new HashMap(4);
		}
		filePath = new File(filePath).getAbsolutePath();
		FileOutputStream out = (FileOutputStream) streamMapping.get(filePath);
		if(out == null){
			out = new FileOutputStream(filePath, isAppend);
			streamMapping.put(filePath, out);
		}
	}

	public static final void log(String filePath, byte[] data, int offset,
						int len) throws Exception{
		if(streamMapping == null){
			return;
		}
		FileOutputStream out = (FileOutputStream) streamMapping.get(new File(filePath).getAbsolutePath());
		if(out == null){
			return;
		}

		out.write(data, offset, len);
		out.flush();
	}

	public synchronized static final void closeLog(String filePath)
												throws Exception{
		if(streamMapping == null){
			return;
		}
		FileOutputStream out = (FileOutputStream) streamMapping.remove(new File(filePath).getAbsolutePath());
		if(out != null){
			out.close();
		}
	}

	// �����Ƿ�windowsϵͳ
	public static boolean isWindows(){
		boolean isWindows = true;
		String sysName = System.getProperty("os.name");
		if(sysName != null){
			isWindows = (sysName.toLowerCase().indexOf("windows") >= 0);
		}
		return isWindows;
	}

	// ���ز���ϵͳλ�� 32λ�� 64λ��
	public static int systemBits(){
		String bitName = System.getProperty("os.arch");
		if(bitName == null){
			return 32;
		} else if(bitName.indexOf("64") >= 0){
			return 64;
		}
		return 32;
	}

	public static boolean isAbsoluteLocalFilePath(String path){
		if(path == null){
			return false;
		}

		if(Util.isWindows()){
			return path.indexOf(':') > 0;
		} else{
			return path.startsWith("/");
		}
	}

	public final static String[] splitInfo(String info, String splitStr){
		if(info == null){
			return null;
		}
		if(splitStr == null || splitStr.length() == 0){
			return new String[]{info};
		}

		ArrayList list = new ArrayList(64);
		int pIndex = 0, index = 0;
		while((index = info.indexOf(splitStr, pIndex)) >= 0){
			list.add(info.substring(pIndex, index));
			index += splitStr.length();
			pIndex = index;
		}
		list.add(info.substring(pIndex));

		String[] infoarray = new String[list.size()];
		list.toArray(infoarray);
		return infoarray;
	}

	/**
	 * 
	 * @param flag,flag=1��ʾ��ȡ��ͨ��д����
	 *            flag=2��ʾ��ȡ����Ҵ�д����
	 * @param s
	 * @return
	 */
	public final static String numFormat(int flag, long num){
		String s = num + "";
		int sLength = s.length();
		// ���Ҵ�д��ʽ
		String bigLetter[] = { "��", "Ҽ", "��", "��", "��", "��", "½", "��", "��",
				"��" };
		// ��ͨ��д��ʽ
		String bigLetter1[] = { "��", "һ", "��", "��", "��", "��", "��", "��",
				"��", "��" };

		// ���ҵ�λ
		String unit[] = { "Ԫ", "ʰ", "��", "Ǫ", "��",
		// ʰ��λ��Ǫ��λ
				"ʰ", "��", "Ǫ",
				// ��λ������λ
				"��", "ʰ", "��", "Ǫ", "��" };
		// ��ͨ��λ
		String unit1[] = { "", "ʮ", "��", "Ǫ", "��",
		// ʰ��λ��Ǫ��λ
				"ʮ", "��", "Ǫ",
				// ��λ������λ
				"��", "ʮ", "��", "Ǫ", "��" };

		String regex1[] = { "��Ǫ", "���", "��ʰ" };
		String regex2[] = { "����", "����", "��" };
		String regex3[] = { "��", "��", "" };

		String regex11[] = { "��Ǫ", "����", "��ʮ" };
		String regex12[] = { "����", "����", "��" };
		String regex13[] = { "��", "��", "" };

		// �������ת��������ַ���
		String newS = "";
		// ��λ�滻Ϊ���Ĵ�д��ʽ
		for(int i = 0; i < sLength; i++){
			if(flag == 1){
				newS = newS + bigLetter1[s.charAt(i) - 48]
						+ unit1[sLength - i - 1];

				// ���������ǿմ���������ؿմ�
				if("".equals(newS)){
					return "";
				}
				// ����û���ʼ�����˺ܶ� 0 ȥ���ַ���ǰ������'��'��ʹ�俴��ȥ������ϰ��
				while(newS.charAt(0) == '��'){
					if(newS.length() == 1){
						return newS;
					}
					// ���ַ����е� "��" ������Ӧ�ĵ�λȥ��
					newS = newS.substring(2);
					// ����û����������ʱ��ֻ������ 0����ֻ����һ�� "��"
					if(newS.length() == 0){
						return "��";
					}
				}

				// ��һ��ת���� "��Ǫ", ���","��ʰ"���ַ����滻��һ��"��"
				for(int j = 0; j < 3; j++){
					newS = newS.replaceAll(regex11[j], "��");
				}
				// �ڶ���ת������ "����","����","��Ԫ"�����
				// "��","��","Ԫ"��Щ��λ��Щ����ǲ���ʡ�ģ���Ҫ��������
				for(int j = 0; j < 3; j++){
					// ����һ��ת�������п����кܶ�������һ��
					// Ҫ�Ѻܶ���ظ�������һ����
					newS = newS.replaceAll("������", "��");
					newS = newS.replaceAll("����", "��");
					newS = newS.replaceAll(regex12[j], regex13[j]);
				}

				// ��"��"��"��"֮��ȫ����"��"��ʱ�򣬺���"����"��λ��ֻ����һ��"��"
				newS = newS.replaceAll("����", "��");

			} else if(flag == 2){
				newS = newS + bigLetter[s.charAt(i) - 48]
						+ unit[sLength - i - 1];
				// ���������ǿմ���������ؿմ�
				if("".equals(newS)){
					return "";
				}
				// ����û���ʼ�����˺ܶ� 0 ȥ���ַ���ǰ������'��'��ʹ�俴��ȥ������ϰ��
				while(newS.charAt(0) == '��'){
					if(newS.length() == 1){
						return newS;
					}
					// ���ַ����е� "��" ������Ӧ�ĵ�λȥ��
					newS = newS.substring(2);
					// ����û����������ʱ��ֻ������ 0����ֻ����һ�� "��"
					if(newS.length() == 0){
						return "��";
					}
				}

				// ��һ��ת���� "��Ǫ", ���","��ʰ"���ַ����滻��һ��"��"
				for(int j = 0; j < 3; j++){
					newS = newS.replaceAll(regex1[j], "��");
				}
				// �ڶ���ת������ "����","����","��Ԫ"�����
				// "��","��","Ԫ"��Щ��λ��Щ����ǲ���ʡ�ģ���Ҫ��������
				for(int j = 0; j < 3; j++){
					// ����һ��ת�������п����кܶ�������һ��
					// Ҫ�Ѻܶ���ظ�������һ����
					newS = newS.replaceAll("������", "��");
					newS = newS.replaceAll("����", "��");
					newS = newS.replaceAll(regex2[j], regex3[j]);
				}

				// ��"��"��"��"֮��ȫ����"��"��ʱ�򣬺���"����"��λ��ֻ����һ��"��"
				newS = newS.replaceAll("����", "��");
			}
		}

		return newS;
	}

	public static String encode(String src)
								throws UnsupportedEncodingException{
		return encode(src, "GBK");
	}

	public static String encode(String src, String enc)
										throws UnsupportedEncodingException{
		if(src == null){
			return "";
		}
		String str = URLEncoder.encode(src, enc);
		return str.replaceAll("\\+", "%20");
	}

	public static String decode(String src, String enc)
										throws UnsupportedEncodingException{
		if(src == null){
			return "";
		}
		return URLDecoder.decode(src, enc);
	}
	
	public static boolean saveFileWithSecurity(byte[] fileData,String fileName){
		return saveFileWithSecurity(fileData,fileName,null);
	}
	public static boolean saveFileWithSecurity(byte[] fileData,String fileName,int andor){
		return saveFileWithSecurity(fileData,0,fileData.length,fileName,andor);
	}
	public static boolean saveFileWithSecurity(byte[] fileData,int offset,int len,String fileName){
		return saveFileWithSecurity(fileData,offset,len,fileName,null);
	}
	public static boolean saveFileWithSecurity(byte[] fileData,int offset,int len,String fileName,int andor){
		return saveFileWithSecurity(fileData,offset,len,fileName,null,andor);
	}
	public static boolean saveFileWithSecurity(byte[] fileData,String fileName,String bakFileName){
		return saveFileWithSecurity(fileData,0,fileData.length,fileName,bakFileName);
	}
	public static boolean saveFileWithSecurity(byte[] fileData,int offset,int len,String fileName,String bakFileName){
		return saveFileWithSecurity(fileData,offset,len,fileName,bakFileName,0);
	}
	private static final Object WRITE_LOCK = new Object();
	public static boolean saveFileWithSecurity(byte[] fileData,int offset,int len,String fileName,String bakFileName,int andor){
		if(fileName == null){
			return false;
		}
		if(bakFileName == null || bakFileName.trim().length() == 0){
			bakFileName = getDefaultBakFileName(fileName);
		}
		
		synchronized(WRITE_LOCK){
			File f = new File(fileName);
			File tempFile = new File(fileName+".bak");
			File bakFile = new File(bakFileName);
			
			if(f.exists()){
				if(bakFile.exists()){
					if(bakFile.lastModified() < f.lastModified()){
						f = bakFile;
					}
				}else{
					f = bakFile;
				}
			}
			f.getAbsoluteFile().getParentFile().mkdirs();
			
			FileOutputStream out = null;
			try{
				if(andor != 0){
					for(int i=0,count=offset;i<len;i++,count++){
						fileData[count] = (byte)(fileData[count]^andor);
					}
				}
				out = new FileOutputStream(tempFile);
				out.write(fileData,offset,len);
				out.close();
				
				f.delete();
				tempFile.renameTo(f);
			}catch(Exception e){
				e.printStackTrace();
				return false;
			}finally{
				if(out != null){
					try{
						out.close();
					}catch(Exception e){}
				}
			}
		}
		return true;
	}
	
	public static byte[] loadFileWithSecurity(String fileName){
		return loadFileWithSecurity(fileName,null);
	}
	public static byte[] loadFileWithSecurity(String fileName,int andor){
		return loadFileWithSecurity(fileName,null,andor);
	}
	public static byte[] loadFileWithSecurity(String fileName,String bakFileName){
		return loadFileWithSecurity(fileName,bakFileName,0);
	}
	
	public static byte[] loadFileWithSecurity(String fileName,String bakFileName,int andor){
		if(fileName == null){
			return null;
		}
		if(bakFileName == null || bakFileName.trim().length() == 0){
			bakFileName = getDefaultBakFileName(fileName);
		}
		
		File f = new File(fileName);
		File bakFile = new File(bakFileName);
		if(bakFile.exists()){
			if(f.exists()){
				if(bakFile.lastModified() > f.lastModified()){
					f = bakFile;
				}
			}else{
				f = bakFile;
			}
		}
		if(f.exists()){
			FileInputStream in = null;
			try{
				int len = (int)f.length();
				
				byte[] fileData = new byte[len];
				in = new FileInputStream(f);
				int count = 0,tempCount;
				while(count < len){
					tempCount = in.read(fileData, count, len-count);
					if(tempCount < 0){
						return null;
					}
					count += tempCount;
				}
				
				
				if(andor != 0){
					for(int i=0;i<len;i++){
						fileData[i] = (byte)(fileData[i]^andor);
					}
				}
				return fileData;
			}catch(Exception e){
				e.printStackTrace();
				return null;
			}finally{
				if(in != null){
					try{
						in.close();
					}catch(Exception e){}
				}
			}
		}
		return null;
	}
	
	private static String getDefaultBakFileName(String _fileName){
		File f = new File(_fileName);
		String fileName = f.getName();
		
		String bakFileName = null;
		
		int index = fileName.lastIndexOf('.');
		if(index >= 0){
			bakFileName = fileName.substring(0, index) + "_2"+fileName.substring(index);
		}else{
			bakFileName = fileName + "_2";
		}
		
		f = new File(f.getAbsoluteFile().getParentFile(),bakFileName);
		return f.getAbsolutePath();
	}
	
	public static String getFilePathReg(String path){
		if(path == null){
			return null;
		}
		
		char sourceChar = '/';
		if('/' == File.separatorChar){
			sourceChar = '\\';
		}
		while(path.indexOf(sourceChar)>=0){
			path = path.replace(sourceChar, File.separatorChar);
		}
		
		return path;
	}
	


	public static void main(String[] args) throws Exception{
	    
	    byte[] arr = Util.bcdStr2ByteArr("11040615");
	    System.out.println(arr[0]+" "+arr[1]+" "+arr[2]+" "+arr[3]);
	    byte[] data = new byte[]{(byte)18,(byte)18,(byte)0x04,(byte)0x06,(byte)0x15};
	    System.out.println(Util.byteArr2BcdStr(data, 1, 4));
	    System.out.println(GeneralConst.getSimpleDateFormat("yyMMddHH").parse(Util.byteArr2BcdStr(data, 1, 4)));
	    if(true)return ;
		for(int i=0;i<10;i++){
			Util.saveFileWithSecurity((""+i).getBytes(), "./1/2/adfs.data");
			
			System.out.println(new String(Util.loadFileWithSecurity("./1/2/adfs.data")));
			try{
				Thread.sleep(10000);
			}catch(Exception e){}
		}
	}

	public static boolean printInfoFromStream(InputStream in,boolean isText){
		try{
			if(isText){
				BufferedReader din = new BufferedReader(new InputStreamReader(in));
				String line = din.readLine();
				while(line != null){
					System.out.println(line);
					line = din.readLine();
				}
			}else{
				byte[] buff = new byte[1024];
				StringBuffer strBuffer = new StringBuffer();
				int count = in.read(buff);
				while(count > 0){
					strBuffer.delete(0, strBuffer.length());
					
					for(int i=0;i<count;i++){
						strBuffer.append(Integer.toHexString(buff[i]&0xFF));
						strBuffer.append(" ");
					}
					System.out.println(strBuffer);
					count = in.read(buff);
				}
			}
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}
		return true;
	}
	private static int	crc16_ccitt_update(int crc, byte v)
	{
	  return (crc >> 8) ^ gm_crc16_ccitt_table[(crc ^ v) & 0xFF];
	}
	

	/**
	 * �õ�ָ���ֽ������CRC16У����
	 * @param crc
	 * @param data
	 * @param offset
	 * @param length
	 * @return CRC16���������ֽڵ�У����
	 */
	public static short crc16_ccitt_calc(int crc, byte[]data,int offset,int length)
	{
	  for(int i = offset,j=0; j < length; i++,j++)
	  {
	    crc = crc16_ccitt_update(crc, data[i]);
	  }
	  return (short)crc;
	} 
//   ����һ���ֽڵ�У����
    public static byte crc8(byte crc, byte[] data, int offset, int length) {
        for (int i = offset,j=0; j < length; i++,j++) {
            crc=(byte)(crc ^ data[i]);
        }
        return crc;
    }
	// Polyn om: Crc nach CCITT = x^16 + x^12 + x^5 + 1
	// Tabelle nach CCITT 
	private static final int gm_crc16_ccitt_table[] = { 
	0x0000, 0x1021, 0x2042, 0x3063, 0x4084, 0x50a5, 0x60c6, 0x70e7, 
	0x8108, 0x9129, 0xa14a, 0xb16b, 0xc18c, 0xd1ad, 0xe1ce, 0xf1ef, 
	0x1231, 0x0210, 0x3273, 0x2252, 0x52b5, 0x4294, 0x72f7, 0x62d6, 
	0x9339, 0x8318, 0xb37b, 0xa35a, 0xd3bd, 0xc39c, 0xf3ff, 0xe3de, 
	0x2462, 0x3443, 0x0420, 0x1401, 0x64e6, 0x74c7, 0x44a4, 0x5485, 
	0xa56a, 0xb54b, 0x8528, 0x9509, 0xe5ee, 0xf5cf, 0xc5ac, 0xd58d, 
	0x3653, 0x2672, 0x1611, 0x0630, 0x76d7, 0x66f6, 0x5695, 0x46b4, 
	0xb75b, 0xa77a, 0x9719, 0x8738, 0xf7df, 0xe7fe, 0xd79d, 0xc7bc, 
	0x48c4, 0x58e5, 0x6886, 0x78a7, 0x0840, 0x1861, 0x2802, 0x3823, 
	0xc9cc, 0xd9ed, 0xe98e, 0xf9af, 0x8948, 0x9969, 0xa90a, 0xb92b, 
	0x5af5, 0x4ad4, 0x7ab7, 0x6a96, 0x1a71, 0x0a50, 0x3a33, 0x2a12, 
	0xdbfd, 0xcbdc, 0xfbbf, 0xeb9e, 0x9b79, 0x8b58, 0xbb3b, 0xab1a, 
	0x6ca6, 0x7c87, 0x4ce4, 0x5cc5, 0x2c22, 0x3c03, 0x0c60, 0x1c41, 
	0xedae, 0xfd8f, 0xcdec, 0xddcd, 0xad2a, 0xbd0b, 0x8d68, 0x9d49, 
	0x7e97, 0x6eb6, 0x5ed5, 0x4ef4, 0x3e13, 0x2e32, 0x1e51, 0x0e70, 
	0xff9f, 0xefbe, 0xdfdd, 0xcffc, 0xbf1b, 0xaf3a, 0x9f59, 0x8f78, 
	0x9188, 0x81a9, 0xb1ca, 0xa1eb, 0xd10c, 0xc12d, 0xf14e, 0xe16f, 
	0x1080, 0x00a1, 0x30c2, 0x20e3, 0x5004, 0x4025, 0x7046, 0x6067, 
	0x83b9, 0x9398, 0xa3fb, 0xb3da, 0xc33d, 0xd31c, 0xe37f, 0xf35e, 
	0x02b1, 0x1290, 0x22f3, 0x32d2, 0x4235, 0x5214, 0x6277, 0x7256, 
	0xb5ea, 0xa5cb, 0x95a8, 0x8589, 0xf56e, 0xe54f, 0xd52c, 0xc50d, 
	0x34e2, 0x24c3, 0x14a0, 0x0481, 0x7466, 0x6447, 0x5424, 0x4405, 
	0xa7db, 0xb7fa, 0x8799, 0x97b8, 0xe75f, 0xf77e, 0xc71d, 0xd73c, 
	0x26d3, 0x36f2, 0x0691, 0x16b0, 0x6657, 0x7676, 0x4615, 0x5634, 
	0xd94c, 0xc96d, 0xf90e, 0xe92f, 0x99c8, 0x89e9, 0xb98a, 0xa9ab, 
	0x5844, 0x4865, 0x7806, 0x6827, 0x18c0, 0x08e1, 0x3882, 0x28a3, 
	0xcb7d, 0xdb5c, 0xeb3f, 0xfb1e, 0x8bf9, 0x9bd8, 0xabbb, 0xbb9a, 
	0x4a75, 0x5a54, 0x6a37, 0x7a16, 0x0af1, 0x1ad0, 0x2ab3, 0x3a92, 
	0xfd2e, 0xed0f, 0xdd6c, 0xcd4d, 0xbdaa, 0xad8b, 0x9de8, 0x8dc9, 
	0x7c26, 0x6c07, 0x5c64, 0x4c45, 0x3ca2, 0x2c83, 0x1ce0, 0x0cc1, 
	0xef1f, 0xff3e, 0xcf5d, 0xdf7c, 0xaf9b, 0xbfba, 0x8fd9, 0x9ff8, 
	0x6e17, 0x7e36, 0x4e55, 0x5e74, 0x2e93, 0x3eb2, 0x0ed1, 0x1ef0 
	}; 
}
