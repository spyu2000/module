package com.fleety.base.export.data;

import com.fleety.base.InfoContainer;

public class TextUnit extends Unit {
	
	private InfoContainer arrInfo = new InfoContainer();
	private String text;
	
	public TextUnit(String text) {
		this.text = text;
	}

	public void addObjAttr(String attr, Object value) {
		arrInfo.setInfo(attr, value);
	}

	public int getType() {
		return Unit.TEXT_UNIT;
	}

	public InfoContainer getObjAttr() {
		return this.arrInfo;
	}

	public String getText(){
		return this.text;
	}

	
	public void setFontSize(int size) {
		this.arrInfo.setInfo("font-size", size);
		
	}

	
	public void setFontStyle(int style) {
		this.arrInfo.setInfo("font-style", style);
		
	}


	public void setPosition(int x, int y) {
		this.arrInfo.setInfo("position", x+","+y);
		
	}
	public void setBounds(int x, int y,int width,int height) {
		this.arrInfo.setInfo("bounds", x+","+y+","+width+","+height);
		
	}

	public void setTextAlign(int align) {
		this.arrInfo.setInfo("text-align", align);
		
	}
}
