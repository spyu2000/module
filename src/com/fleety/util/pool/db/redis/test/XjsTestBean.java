package com.fleety.util.pool.db.redis.test;

import java.util.Date;

import com.fleety.util.pool.db.redis.RedisTableBean;

public class XjsTestBean extends RedisTableBean {
	private String name = null;
	private Date date = new Date();
	private boolean male = false;
	private int int1 = 100;
	private long long1 = 100000000000000l;
	private byte byte1 = 1;
	private short short1 = (short)6324;
	private char char1 = 'Y';
	private char char2 = 'Ðì';
	private float float1 = 1.234f;
	private double double1 = 233.54354354;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
		this.date = date;
	}
	public boolean getMale() {
		return male;
	}
	public void setMale(boolean male) {
		this.male = male;
	}
	public int getInt1() {
		return int1;
	}
	public void setInt1(int int1) {
		this.int1 = int1;
	}
	public long getLong1() {
		return long1;
	}
	public void setLong1(long long1) {
		this.long1 = long1;
	}
	public byte getByte1() {
		return byte1;
	}
	public void setByte1(byte byte1) {
		this.byte1 = byte1;
	}
	public short getShort1() {
		return short1;
	}
	public void setShort1(short short1) {
		this.short1 = short1;
	}
	public char getChar1() {
		return char1;
	}
	public void setChar1(char char1) {
		this.char1 = char1;
	}
	public char getChar2() {
		return char2;
	}
	public void setChar2(char char2) {
		this.char2 = char2;
	}
	public float getFloat1() {
		return float1;
	}
	public void setFloat1(float float1) {
		this.float1 = float1;
	}
	public double getDouble1() {
		return double1;
	}
	public void setDouble1(double double1) {
		this.double1 = double1;
	}
	
	
	
}
