/**
 * 绝密 Created on 2008-11-18 by edmund
 */
package com.fleety.base;

import java.nio.ByteOrder;

/**
 * 该类提供动态ByteBuffer的功能，初始化的时候无需知道确切的大小
 * @author edmund
 *
 */
public class FleetyByteBuffer{
	private ByteOrder order = ByteOrder.BIG_ENDIAN;
	
	private byte[] hb;
	private int position;
	private int capacity;
	private int capacityStep;
	private FleetyByteBuffer(int capacity){
		this.hb = new byte[capacity];
		
		this.capacity = capacity;
		this.capacityStep = capacity;
		this.position = 0;
	}
	private FleetyByteBuffer(int capacity,int capacityStep){
		this.hb = new byte[capacity];
		
		this.capacity = capacity;
		this.capacityStep = capacityStep;
		this.position = 0;
	}
	
	public static FleetyByteBuffer allocate(int capacity){
		return new FleetyByteBuffer(capacity);
	}
	
	public static FleetyByteBuffer allocate(int capacity,int stepCapacity){
		return new FleetyByteBuffer(capacity,stepCapacity);
	}
	
	public FleetyByteBuffer clear(){
		this.position = 0;
		return this;
	}
	
	public byte[] array(){
		return this.hb;
	}
	
	public int capacity(){
		return this.capacity;
	}
	
	public int position(){
		return this.position;
	}
	
	public FleetyByteBuffer position(int newPosition){
		this.position = newPosition;
		return this;
	}
	
	public FleetyByteBuffer put(byte data){
		this.validCapacity(1);
		
		this.hb[this.position] = data;
		this.position ++;
		
		return this;
	}
	
	public FleetyByteBuffer putShort(short data){
		this.validCapacity(2);

		if(this.order == ByteOrder.LITTLE_ENDIAN){
			this.hb[this.position] = (byte)(data&0xFF);
			this.hb[this.position + 1] = (byte)((data>>8)&0xFF);
		}else{
			this.hb[this.position] = (byte)((data>>8)&0xFF);
			this.hb[this.position + 1] = (byte)(data&0xFF);
		}
		this.position += 2;
		
		return this;
	}
	
	public FleetyByteBuffer putInt(int data){
		this.validCapacity(4);

		if(this.order == ByteOrder.LITTLE_ENDIAN){
			this.hb[this.position] = (byte)(data&0xFF);
			this.hb[this.position + 1] = (byte)((data>>8)&0xFF);
			this.hb[this.position + 2] = (byte)((data>>16)&0xFF);
			this.hb[this.position + 3] = (byte)((data>>24)&0xFF);
		}else{
			this.hb[this.position] = (byte)((data>>24)&0xFF);
			this.hb[this.position + 1] = (byte)((data>>16)&0xFF);
			this.hb[this.position + 2] = (byte)((data>>8)&0xFF);
			this.hb[this.position + 3] = (byte)(data&0xFF);
		}
		this.position += 4;
		
		return this;
	}
	
	public FleetyByteBuffer putLong(long data){
		this.validCapacity(8);

		if(this.order == ByteOrder.LITTLE_ENDIAN){
			this.hb[this.position] = (byte)(data&0xFF);
			this.hb[this.position + 1] = (byte)((data>>8)&0xFF);
			this.hb[this.position + 2] = (byte)((data>>16)&0xFF);
			this.hb[this.position + 3] = (byte)((data>>24)&0xFF);
			this.hb[this.position + 4] = (byte)((data>>32)&0xFF);
			this.hb[this.position + 5] = (byte)((data>>40)&0xFF);
			this.hb[this.position + 6] = (byte)((data>>48)&0xFF);
			this.hb[this.position + 7] = (byte)((data>>56)&0xFF);
		}else{
			this.hb[this.position] = (byte)((data>>56)&0xFF);
			this.hb[this.position + 1] = (byte)((data>>48)&0xFF);
			this.hb[this.position + 2] = (byte)((data>>40)&0xFF);
			this.hb[this.position + 3] = (byte)((data>>32)&0xFF);
			this.hb[this.position + 4] = (byte)((data>>24)&0xFF);
			this.hb[this.position + 5] = (byte)((data>>16)&0xFF);
			this.hb[this.position + 6] = (byte)((data>>8)&0xFF);
			this.hb[this.position + 7] = (byte)(data&0xFF);
		}
		this.position += 8;
		
		return this;
	}
	
	public FleetyByteBuffer putDouble(double data){
		this.putLong(Double.doubleToRawLongBits(data));
		
		return this;
	}
	
	public FleetyByteBuffer putFloat(float data){
		this.putInt(Float.floatToRawIntBits(data));

		return this;
	}
	
	public FleetyByteBuffer put(byte[] data){
		if(data == null){
			return this;
		}
		this.validCapacity(data.length);
		System.arraycopy(data, 0, this.hb, this.position, data.length);
		this.position += data.length;

		return this;
	}
	
	public FleetyByteBuffer put(byte[] data,int offset,int len){
		if(data == null){
			return this;
		}
		this.validCapacity(len);
		System.arraycopy(data, offset, this.hb, this.position, len);
		this.position += len;

		return this;
	}
	
	public ByteOrder order(){
		return this.order;
	}
	
	public FleetyByteBuffer order(ByteOrder order){
		this.order = order;
		return this;
	}
	
	private void validCapacity(int remainLength){
		if(this.position + remainLength <= this.capacity){
			return;
		}
		for(;this.position + remainLength > this.capacity;){//增加容量直到可放下后继数据
			this.capacity += capacityStep;
		}
		byte[] newHb = new byte[capacity];
		System.arraycopy(this.hb, 0, newHb, 0, this.hb.length);
		this.hb = newHb;
	}
}
