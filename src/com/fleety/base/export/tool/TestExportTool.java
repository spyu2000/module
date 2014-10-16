package com.fleety.base.export.tool;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;

import jxl.format.Alignment;

import com.fleety.base.export.data.ExportDataDesc;
import com.fleety.base.export.data.ImageUnit;
import com.fleety.base.export.data.TableUnit;
import com.fleety.base.export.data.TextUnit;
import com.lowagie.text.BadElementException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Image;

public class TestExportTool {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String[][] data =new String[5][5];
		for(int i=0;i<5;i++){
			for(int j=0;j<5;j++){
				data[i][j]=i+"ÐÐ"+j+"ÁÐ";
			}
		}
		//data[0][1]="";
		data[1][0]="";
		data[2][0]="";
		
		ITools tools = new ExportFileTools();
		ExportDataDesc desc = new ExportDataDesc();
		TextUnit text = desc.addString("±êÌâ");
		text.setFontSize(25);
		text.setFontStyle(Font.BOLD);
		text.setTextAlign(Element.ALIGN_CENTER);
		
		text = desc.addString("adasdffffaaaaaaaaaaaaaaf¹þ°¡°¡°¡°¡°¡°¡°¡°¡°¡°¡°¡°¡°¡°¡¹þ°¡°¡°¡¹þ¹þ¹þ\nada°¢ÈøµÂf·´·´¸´·´¸´·´¸´·´¸´·´·´¸´·´¸´·´¸´·´¸´·¢µÄËµ·¨");
		text.setFontSize(15);
		text.setFontStyle(Font.NORMAL);
		text.setTextAlign(Element.ALIGN_LEFT);
		text.setBounds(1, 1, 1, 2);
	
		TableUnit table = desc.addTableData(data);
		table.setSpan(0, 0, 3, 1);
		table.setFontSize(10);
		table.setFontStyle(Font.BOLD);
		table.setPosition(2, 2);
		table.setTextAlign(Element.ALIGN_CENTER);
		
		ImageUnit image = desc.addImageFile(new File("d:/1.jpg"));
		image.setTextAlign(Image.ALIGN_CENTER);
		image.setSize(80, 80);
		image.setBounds(10, 10, 3, 4);
		
		Image img = null;
		try {
			img = Image.getInstance("d:/1.jpg");
		} catch (Exception e) {
			e.printStackTrace();
		} 
		image = desc.addImage(img);
		image.setTextAlign(Image.ALIGN_CENTER);
		image.setSize(80, 80);
		image.setBounds(10, 20, 3, 4);
		
		tools.export("d:/jason3/test.doc", desc, "doc");
		tools.export("d:/jason3/test.pdf", desc, "pdf");
		tools.export("d:/jason3/test.xls", desc, "xls");
		tools.export("d:/jason3/test.csv", desc, "csv");
//		int id =0;
//		id = desc.addString("±êÌâ");
//		desc.addObjAttr(id, "text-align", Element.ALIGN_CENTER);
//		desc.addObjAttr(id, "font-size", 25);
//		desc.addObjAttr(id, "font-style", Font.BOLD);
//		
//		id = desc.addString("adasdffffaaaaaaaaaaaaaaf¹þ°¡°¡°¡°¡°¡°¡°¡°¡°¡°¡°¡°¡°¡°¡¹þ°¡°¡°¡¹þ¹þ¹þ\nada°¢ÈøµÂf·´·´¸´·´¸´·´¸´·´¸´·´·´¸´·´¸´·´¸´·´¸´·¢µÄËµ·¨");
//		desc.addObjAttr(id, "text-align", Element.ALIGN_LEFT);
//		desc.addObjAttr(id, "font-size", 15);
//		desc.addObjAttr(id, "font-style", Font.NORMAL);
//	
//		id = desc.addTableData(data);
//		desc.addObjAttr(id, "0-0-span", "3,1");
//		desc.addObjAttr(id, "text-align", Element.ALIGN_CENTER);
//		desc.addObjAttr(id, "font-size", 10);
//		desc.addObjAttr(id, "font-style", Font.BOLD);
//		desc.addObjAttr(id, "position-xls", "1,1");
//		
////		id = desc.addImageFile(new File("d:/1.jpg"));
////		desc.addObjAttr(id, "img-align", Image.ALIGN_CENTER);
////		desc.addObjAttr(id, "img-size", "80,80");
////		//desc.addObjAttr(id, "img-position", "1,1");
////		desc.addObjAttr(id, "img-position-xls", "10,10,3,4");
//		
//		Image img = null;
//		try {
//			img = Image.getInstance("d:/1.jpg");
//		} catch (Exception e) {
//			e.printStackTrace();
//		} 
//		id = desc.addImage(img);
//		desc.addObjAttr(id, "img-align", Image.ALIGN_CENTER);
//		desc.addObjAttr(id, "img-size", "80,80");
//		//desc.addObjAttr(id, "img-position", "1,1");
//		desc.addObjAttr(id, "img-position-xls", "10,15,3,4");
		
		
		
//		IExport exp = tools.getTool("csv");
//		if(exp != null){
//			exp.setData(data);
//			exp.setCellSpan(0, 0, 1, 3);
//			exp.exportFile("d:/jason2", "test.csv");
//		}
//		
//		
//		obj = new ExportDataObj();
//		obj.addData(IData);
//		obj.addData(IData);
//		obj.addData(IData);
//		
//		boolean isRight = obj.save(outputStream,format);
//		boolean isRight = obj.save(filePath,format);
//		
	}

}
