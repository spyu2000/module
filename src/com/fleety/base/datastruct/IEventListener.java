package com.fleety.base.datastruct;

public interface IEventListener {
	/**
	 * 在事件将要发生时，进行通知。如果反馈回true，代表可继续，如果返回false，代表无需让该事件真实发生。
	 * @param eventType    事件类型
	 * @param paraInfoArr  事件相关的多个参数信息，依赖具体事件
	 * @param source       事件发生所属的对象
	 * @return             true代表可继续，flase代表无需继续
	 */
	public boolean eventWillHappen(int eventType,Object[] paraInfoArr,Object source);
	
	/**
	 * 事件发生后的通知
	 * @param eventType    事件类型
	 * @param paraInfoArr  事件相关的多个参数信息，依赖具体事件
	 * @param source       事件发生所属的对象
	 */
	public void eventHappened(int eventType,Object[] paraInfoArr,Object source);
}
