package com.fleety.helper;

import com.fleety.base.InfoContainer;

public class HelperAdapter implements IHelper {

	public boolean init(Object caller) {
		return true;
	}

	public HelpResult help(InfoContainer info) {
		return null;
	}
	
	public void destroy() {

	}

}
