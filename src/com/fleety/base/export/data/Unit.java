package com.fleety.base.export.data;

import com.fleety.base.InfoContainer;

public abstract class Unit {
	public static final int TEXT_UNIT = 1;
	public static final int IMAGE_UNIT = 2;
	public static final int TABLE_UNIT = 3;
	
	public abstract int getType();
	public abstract void addObjAttr(String attr,Object value);
	public abstract InfoContainer getObjAttr();
}
