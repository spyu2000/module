/**
 * ¾øÃÜ Created on 2010-5-6 by edmund
 */
package com.fleety.track;

import java.io.RandomAccessFile;

public interface ModifyFilter{
	public void filter(RandomAccessFile accessFile,int infoFlag) throws Exception;
}
