package com.fleety.base.export.data;

import com.fleety.base.InfoContainer;

public class TableUnit extends Unit {

	private String[][] data;
	private InfoContainer arrInfo = new InfoContainer();
	
	public TableUnit(){}
	
	public TableUnit(String[][] data) {
		this.data = data;
	}
	
	public void setTableData(String[][] data){
		this.data = data;
	} 
	
	public String[][] getTableData(){
		return this.data;
	}
	
	public void addObjAttr(String attr,Object value){
		this.arrInfo.setInfo(attr, value);
	}
	
	public InfoContainer getObjAttr(){
		return this.arrInfo;
	}
	
	public int getType(){ 
		return Unit.TABLE_UNIT;
	}
	
	public void setSpan(int x, int y, int rowspan, int colspan){
		this.arrInfo.setInfo(x+"-"+y+"-span", rowspan+","+colspan);
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

	
	public void setSize(int width, int height) {
		this.arrInfo.setInfo("size", width+","+height);
		
	}

	
	public void setTextAlign(int align) {
		this.arrInfo.setInfo("text-align", align);
		
	}
}
