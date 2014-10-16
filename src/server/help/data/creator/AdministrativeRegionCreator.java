package server.help.data.creator;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;

import com.fleety.base.xml.XmlNode;

public class AdministrativeRegionCreator {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		BufferedReader reader = new BufferedReader(new FileReader("c:/AdcodeAndName.txt"));
		String str = reader.readLine();
		String[] arr ;
		XmlNode node1 = null,node2 = null,node3,root;
		root = new XmlNode("all_region");
		while(str != null){
			arr = str.split("  ");
			if(arr.length == 2){
				node1 = new XmlNode("com");
				node1.addAttribute("code", arr[0]);
				node1.addAttribute("name", arr[1]);
				root.addNode(node1);
			}else if(arr.length == 3){
				node2 = new XmlNode("com");
				node2.addAttribute("code", arr[0]);
				node2.addAttribute("name", arr[2]);
				if(node1 != null){
					node1.addNode(node2);
				}
			}else if(arr.length == 4){
				node3 = new XmlNode("com");
				node3.addAttribute("code", arr[0]);
				node3.addAttribute("name", arr[3]);
				if(node2 != null){
					node2.addNode(node3);
				}
			}else{
				
			}
			
			str = reader.readLine();
		}
		
		StringBuffer strBuff = new StringBuffer(1024*1024);
		root.toXmlString(strBuff, "");
		
		FileOutputStream out = new FileOutputStream("c:/administrative_region.xml");
		out.write("<?xml version=\"1.0\" encoding=\"GBK\"?>\n".getBytes());
		out.write(strBuff.toString().getBytes());
		out.close();
	}

}
