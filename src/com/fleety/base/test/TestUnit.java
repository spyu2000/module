/**
 * ¾øÃÜ Created on 2010-9-25 by edmund
 */
package com.fleety.base.test;

import java.lang.reflect.Method;

public class TestUnit{
	private static final String TEST_PRE_NAME_FLAG = "test_";
	public static void executeTest(Object test){
		Method method;
		Method[] arr = test.getClass().getMethods();
		for(int i=0;i<arr.length;i++){
			method = arr[i];
			
			if((method.getModifiers() & 0x01) != 0x01){
				continue;
			}
			if(!method.getName().startsWith(TEST_PRE_NAME_FLAG)){
				continue;
			}

			try{
				method.invoke(test, null);
			}catch(Throwable e){
				e.printStackTrace();
			}
		}
	}
}
