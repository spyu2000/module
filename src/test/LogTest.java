/**
 * ¾øÃÜ Created on 2009-10-21 by edmund
 */
package test;

import com.fleety.base.MD5;

import server.log.LogServer;

public class LogTest
{
	/**
	 * @param args
	 */
	public static void main(String[] args){
		try{
			System.out.println(new MD5().getHexDigest("tmdh"));
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
