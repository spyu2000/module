/**
 * ¾øÃÜ Created on 2008-6-3 by edmund
 */
package test;

import server.i18n.I18nServer;

public class I18nTest{
	public static void main(String[] argv){
//		System.out.println(I18nServer.getSingleInstance().formatInfo("test_group.test_info_1",new String[]{"okok"}));
//		System.out.println(I18nServer.getSingleInstance().formatInfo("test_group.test_info_1","chinese"));
//		System.out.println(I18nServer.getSingleInstance().formatInfo("test_group.test_info_1",new String[]{"okok"},"english"));
//		System.out.println(I18nServer.getSingleInstance().formatInfo("test_group.test_info_1","default"));
//		System.out.println(I18nServer.getSingleInstance().formatInfo("test_group.test_info_1",new String[]{"okok"},"abcdef"));
		

		try{
			String[] arr = I18nServer.singleInstance.getAllLang();
			System.out.println(arr[0]+"  "+arr[1]);
			
			Thread.sleep(10000);
			
			I18nServer.getSingleInstance().formatInfoWithAllLang("test_group.test_info_1");
			arr = I18nServer.singleInstance.getAllLang();
			System.out.println(arr[0]+"  "+arr[1]);
	
			Thread.sleep(10000);
			
			I18nServer.getSingleInstance().formatInfoWithAllLang("test_group.test_info_1");
			arr = I18nServer.singleInstance.getAllLang();
			System.out.println(arr[0]+"  "+arr[1]);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
