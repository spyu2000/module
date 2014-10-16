package com.fleety.util.pool.db.redis;

import java.util.List;

public abstract class BasicRedisObserver implements IRedisObserver {
	private List patternList = null;
	public BasicRedisObserver(List patternList){
		this.patternList = patternList;
	}
	
	public List getPatternList() {
		return this.patternList;
	}
	public boolean isObserve(String msg) {
		return this.patternList.contains(msg);
	}
}
