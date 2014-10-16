/**
 * 绝密 Created on 2008-4-23 by edmund
 */
package com.fleety.track;

import com.fleety.base.InfoContainer;

public interface TrackFilter{
	/**
	 * 继续下一个;忽略当前这个;忽略当前这个并中断后续的
	 */
	public static final int CONTINUE_FLAG = 1;
	public static final int IGNORE_FLAG = 2;
	public static final int BREAK_FLAG = 3;
	
	public int filterTrack(InfoContainer info);
}
