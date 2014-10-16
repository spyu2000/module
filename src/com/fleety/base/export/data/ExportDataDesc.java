package com.fleety.base.export.data;
import java.io.File;
import java.util.ArrayList;

import com.lowagie.text.Image;

public class ExportDataDesc {

	private ArrayList dataObjList = new ArrayList();
	
	public TableUnit addTableData(String[][] data){
		TableUnit table = new TableUnit(data); 
		this.dataObjList.add(table);
		return table;
	}
	public ImageUnit addImage(Image img){
		ImageUnit image = new ImageUnit(img);
		this.dataObjList.add(image);
		return image;	
	}
	
	public ImageUnit addImageFile(File f){
		ImageUnit image = new ImageUnit(f) ;
		this.dataObjList.add(image);
		return image;	
	}
	
	public TextUnit addString(String text){
		TextUnit textUnit = new TextUnit(text); 
		this.dataObjList.add(textUnit);
		return textUnit;	
		
	}
	public void addObjAttr(int id,String attr,Object value){
		Unit unit = (Unit) dataObjList.get(id);
		unit.addObjAttr(attr, value);
	}
	
	public Unit[] getAllUnits(){
		return  (Unit[]) dataObjList.toArray(new Unit[0]);
	}
	
	public Unit getUnitById(int i){
		return (Unit) dataObjList.get(i);
	}
}
