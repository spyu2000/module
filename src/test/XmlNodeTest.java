/**
 * ¾øÃÜ Created on 2008-5-7 by edmund
 */
package test;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import com.fleety.base.xml.XmlNode;

public class XmlNodeTest{
	public static void main(String[] args){
		XmlNode root = new XmlNode("Document"),tempNode;
		
		try{
			BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream("c:/799up.txt")));
			String lineStr = in.readLine();
			String[] arr;
			int index = 1;
			while(lineStr != null){
				arr = lineStr.split(",");
				
				root.addNode(tempNode = new XmlNode("StandData"));

				tempNode.addNode(new XmlNode("SLNO",index+""));
				tempNode.addNode(new XmlNode("SLLon",arr[0].trim()));
				tempNode.addNode(new XmlNode("SLLat",arr[1].trim()));
				tempNode.addNode(new XmlNode("SRLon",arr[2].trim()));
				tempNode.addNode(new XmlNode("SRLat",arr[3].trim()));
				tempNode.addNode(new XmlNode("SName",arr[4].trim()));
				
				lineStr = in.readLine();
				
				index ++;
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		
		StringBuffer buff = new StringBuffer(1024);
		root.toXmlString(buff, "");
		System.out.println(buff.toString());
	}
}
