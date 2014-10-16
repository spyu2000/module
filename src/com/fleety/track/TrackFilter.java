/**
 * ���� Created on 2008-4-23 by edmund
 */
package com.fleety.track;

import com.fleety.base.InfoContainer;

public interface TrackFilter{
	/**
	 * ������һ��;���Ե�ǰ���;���Ե�ǰ������жϺ�����
	 */
	public static final int CONTINUE_FLAG = 1;
	public static final int IGNORE_FLAG = 2;
	public static final int BREAK_FLAG = 3;
	
	public int filterTrack(InfoContainer info);
}
