/**
 * ¾øÃÜ Created on 2008-7-13 by edmund
 */
package com.fleety.server;

public class DetectDog implements IDog{
	public DetectDog(){
		nt.sck.scott.Parrot par = nt.sck.scott.Parrot.getInstance();
		par.start();
	}
	public boolean detect(){
		return true;
	}
}
