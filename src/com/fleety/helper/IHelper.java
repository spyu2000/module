package com.fleety.helper;

import com.fleety.base.InfoContainer;

public interface IHelper {
	public boolean init(Object caller);
	public HelpResult help(InfoContainer info);
	public void destroy();
}
