package com.fleety.base.export.tool;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;

import javax.imageio.ImageIO;

import com.fleety.base.InfoContainer;
import com.fleety.base.export.data.ExportDataDesc;
import com.fleety.base.export.data.ImageUnit;
import com.fleety.base.export.data.TableUnit;
import com.fleety.base.export.data.TextUnit;
import com.fleety.base.export.data.Unit;
import com.lowagie.text.Image;

import jxl.Workbook;
import jxl.format.Alignment;
import jxl.write.Label;
import jxl.write.WritableCellFormat;
import jxl.write.WritableImage;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;

public class ExportEXCEL implements IExport {

	public boolean exportFile(String filePath, ExportDataDesc desc) {
		//验证路径存在、文件名后缀与类型参数是否一致
		File  file = new File(filePath);
		if(!file.exists()){
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if(!".xls".equalsIgnoreCase(filePath.substring(filePath.indexOf("."),filePath.length()))){
			System.out.println("ExportEXCEL:文件名类型错误！");
			return false;
		}
		Unit[] units = desc.getAllUnits();
		try {
			WritableWorkbook workbook = Workbook.createWorkbook(new File(filePath));
			WritableSheet ws = workbook.createSheet("sheet_1", 0);
			Unit unit;
			int type;
			for(int i=0;i<units.length;i++)
			{
				unit = units[i];
				type = unit.getType();
				switch(type)
				{
					case Unit.TABLE_UNIT:
						writeTableData(unit,ws);
						break;
					case Unit.IMAGE_UNIT:
						writeImage(unit,ws);
						break;
					case Unit.TEXT_UNIT:
						writeText(unit,ws);
						break;
				}
			}
			workbook.write();
			workbook.close();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	
		return true;
	}

	private void writeText(Unit unit, WritableSheet ws) throws Exception {
		WritableCellFormat format = new WritableCellFormat();
		format.setAlignment(Alignment.CENTRE);
		TextUnit tUnit = (TextUnit) unit;
		String text = tUnit.getText();
		InfoContainer info = tUnit.getObjAttr();
		String position = info.getString("bounds");
		int x = 0, y = 0, rowspan = 1, colspan = 1;
		if(position != null)
		{
			x = Integer.parseInt(position.split(",")[0]);
			y = Integer.parseInt(position.split(",")[1]);
			rowspan = Integer.parseInt(position.split(",")[2]);
			colspan = Integer.parseInt(position.split(",")[3]);
		}
		Label cell = new Label(x,y,text,format);
		ws.addCell(cell);
		if(rowspan > 1 || colspan >1){
			ws.mergeCells(y, x, y+(colspan-1), x+(rowspan-1));
		}
		
	}

	private void writeImage(Unit unit, WritableSheet ws) {
		ImageUnit iUnit = (ImageUnit) unit;
		InfoContainer attrInfo = iUnit.getObjAttr();
		String position = attrInfo.getString("bounds");//起始坐标
		double x = 0, y = 0,rowspan = 1 ,colspan =1;
		if(position != null)
		{
			x = Double.parseDouble(position.split(",")[0]);
			y = Double.parseDouble(position.split(",")[1]);
			rowspan = Double.parseDouble(position.split(",")[2]);
			colspan = Double.parseDouble(position.split(",")[3]);
		}
		System.out.println(""+x+" "+y+" "+rowspan+" "+colspan);
		
		int way = iUnit.getWay();
		File fileImage = null;
		Image img = null;
		byte[] dd = null;
		if(way == iUnit.FileWay){
			fileImage = iUnit.getFile(); 
		}else{
			img = iUnit.getImage();
			fileImage = new File(img.getUrl().getPath());
		}
		dd = new byte[(int)fileImage.length()];
		
		try{
			//excel仅支持png格式，故将图片转为png格式
			BufferedImage bfi = ImageIO.read(fileImage);
			ImageIO.write(bfi, "png", fileImage);
			new FileInputStream(fileImage).read(dd);
			WritableImage image=new WritableImage(x, y,rowspan,colspan,dd);//从A1开始 跨2行3个单元格
			ws.addImage(image);//ws是Sheet
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	private void writeTableData(Unit unit, WritableSheet ws) throws Exception {
		WritableCellFormat format = new WritableCellFormat();
		format.setAlignment(Alignment.CENTRE);
		TableUnit tUnit = (TableUnit) unit;
		Label cell;
		String[][] data = tUnit.getTableData();
		InfoContainer attrInfo = tUnit.getObjAttr();
		int x = 0 , y = 0;
		String position = attrInfo.getString("position");
		if(position != null){
			x = Integer.parseInt(position.split(",")[0]);
			y = Integer.parseInt(position.split(",")[1]);
		}
		for (int i = 0; i < data.length; i++) {// 行
			for (int j = 0; j < data[i].length; j++) {
				cell = new Label(y + j, x + i, data[i][j], format);
				ws.addCell(cell);
			}
		}
		// 合并单元格
		Iterator it = attrInfo.keys();
		String key,value;
		int colspan, rowspan, row, col;
		while (it.hasNext()) 
		{
			key = (String) it.next();
			if(key.contains("span"))
			{
				row = Integer.parseInt(key.split("-")[0]);
				col = Integer.parseInt(key.split("-")[1]);
				value = attrInfo.getString(key);
				rowspan = Integer.parseInt(value.split(",")[0]);
				colspan = Integer.parseInt(value.split(",")[1]);
				ws.mergeCells(y+col, x+row, y+col + (colspan - 1), x+row + (rowspan - 1));
			}
		}
	}
}
