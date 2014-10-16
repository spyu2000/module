package com.fleety.base.export.data;

import java.io.File;

import com.fleety.base.InfoContainer;
import com.lowagie.text.Image;

public class ImageUnit extends Unit {
	
	public static final int FileWay = 1;//传文件方式
	public static final int ImageWay = 2;//传Image对象方式
	
	private int way;
	private File f ;
	private Image img;
	private InfoContainer arrInfo = new InfoContainer();
	
	public ImageUnit(Image img) {
		this.img = img;
		this.way = ImageWay;
	}

	public ImageUnit(File f) {
		this.f = f;
		this.way = FileWay;
	}

	public void addObjAttr(String attr, Object value) {
		arrInfo.setInfo(attr, value);
	}

	public int getType() {
		return Unit.IMAGE_UNIT;
	}

	public InfoContainer getObjAttr() {
		return this.arrInfo;
	}

	public File getFile(){
		return this.f;
	}
	
	public Image getImage(){
		return this.img;
	}
	
	public int getWay(){
		return this.way;
	}

	
	public void setBounds(int x, int y, int width, int height) {
		this.arrInfo.setInfo("bounds", x+","+y+","+width+","+height);	
	}


	public void setPosition(int x, int y) {
		this.arrInfo.setInfo("position", x+","+y);
		
	}


	public void setSize(int width, int height) {
		this.arrInfo.setInfo("size", width+","+height);
	}


	public void setTextAlign(int align) {
		this.arrInfo.setInfo("text-align", align);
		
	}
}

