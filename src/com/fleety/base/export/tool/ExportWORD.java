package com.fleety.base.export.tool;

import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;

import com.fleety.base.InfoContainer;
import com.fleety.base.StrFilter;
import com.fleety.base.export.data.ExportDataDesc;
import com.fleety.base.export.data.ImageUnit;
import com.fleety.base.export.data.TableUnit;
import com.fleety.base.export.data.TextUnit;
import com.fleety.base.export.data.Unit;
import com.lowagie.text.BadElementException;
import com.lowagie.text.Cell;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Table;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.rtf.RtfWriter2;

public class ExportWORD implements IExport {

	public boolean exportFile(String filePath, ExportDataDesc desc) {
		File  file = new File(filePath);
		if(!file.exists()){
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if(!".doc".equalsIgnoreCase(filePath.substring(filePath.indexOf("."),filePath.length()))){
			System.out.println("ExportWORD:文件名类型错误！");
			return false;
		}
		Unit[] units = desc.getAllUnits();
		Document document = new Document(PageSize.A4);
		try {
			RtfWriter2.getInstance(document, new FileOutputStream(filePath));
			document.open();
			BaseFont bfChinese = BaseFont.createFont("STSongStd-Light",
					"UniGB-UCS2-H", BaseFont.NOT_EMBEDDED);
			for(int i = 0;i < units.length;i ++)
			{
				Unit unit = units[i];
				int type = unit.getType();
				switch(type)
				{
					case Unit.TABLE_UNIT:
						Table table = getTable(unit);
						document.add(table);
						break;
					case Unit.IMAGE_UNIT:
						Image img = getImage(unit);
						document.add(img);
						break;
					case Unit.TEXT_UNIT:
						Paragraph p = getParagraph(unit);
						document.add(p);
						break;
				}
			}
			document.close();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	private Table getTable(Unit unit) throws Exception
	{
		BaseFont bfChinese = BaseFont.createFont("STSongStd-Light",
				"UniGB-UCS2-H", BaseFont.NOT_EMBEDDED);
		Font defaultFont = new Font(bfChinese, 10, Font.NORMAL);// 设置字体大小
		TableUnit tUnit = (TableUnit) unit;
		String[][] data = tUnit.getTableData();
		InfoContainer attrInfo = tUnit.getObjAttr();
		if(attrInfo.getInteger("font-size")!=null){
			defaultFont.setSize(attrInfo.getInteger("font-size"));
		}
		if(attrInfo.getInteger("font-style")!=null){
			defaultFont.setStyle(attrInfo.getInteger("font-style"));
		}
		Table table = new Table(data.length);
		int width[] = new int[data.length];
		for (int i = 0; i < width.length; i++) {
			width[i] = 100 / width.length;
		}
		table.setWidths(width);// 设置每列所占比例
		table.setWidth(90); // 占页面宽度 90%
		if(attrInfo.getInfo("text-align")!=null){
			table.setAlignment((Integer) attrInfo.getInfo("text-align"));
		}
		table.setAutoFillEmptyCells(true); // 自动填满
		table.setBorderWidth(1); // 边框宽度
		table.setBorderColor(new Color(0, 125, 255)); // 边框颜色
		table.setPadding(0);// 衬距
		table.setSpacing(0);// 即单元格之间的间距
		table.setBorder(2);// 边框

		Cell cell;
		Phrase phrase;
		for (int i = 0; i < data.length; i++) 
		{
			for (int j = 0; j < data[i].length; j++) 
			{
				if(StrFilter.hasValue(data[i][j]))
				{
					phrase = new Phrase(data[i][j],defaultFont);
					cell = new Cell(phrase);
					if(attrInfo.getInfo("text-align")!=null){
						cell.setHorizontalAlignment((Integer) attrInfo.getInfo("text-align"));// 设置内容水平居中显示
					}
					if(attrInfo.getInfo(i+"-"+j+"-span")!=null)
					{
						String spanStr = (String) attrInfo.getInfo(i+"-"+j+"-span");
						int rowspan = Integer.parseInt(spanStr.split(",")[0]);
						int colspan = Integer.parseInt(spanStr.split(",")[1]);
						cell.setRowspan(rowspan);
						cell.setColspan(colspan);
					}
					table.addCell(cell);
				}	
			}
		}
		return table;
	}
	
	private Image getImage(Unit unit)
	{
		ImageUnit iUnit = (ImageUnit) unit;
		Image img = null;
		try {
			if(iUnit.getWay() == iUnit.FileWay){
				img = Image.getInstance(iUnit.getFile().getAbsolutePath());
			}else{
				img = iUnit.getImage();
			}
			img.setAlignment(Image.ALIGN_CENTER);//设置图片显示位置
			img.scaleAbsolute(80,80);//直接设定显示尺寸
		} catch (Exception e) {
			e.printStackTrace();
		} 
		
		InfoContainer attr = iUnit.getObjAttr();
		if(attr.getInteger("text-align") != null){
			img.setAlignment(attr.getInteger("text-align"));
		}
		if(attr.getString("size") != null){
			float w =Float.parseFloat(attr.getString("size").split(",")[0]);
			float h =Float.parseFloat(attr.getString("size").split(",")[1]);
			img.scaleAbsolute(w, h);
		}
		if(attr.getString("position") != null){
			float x =Float.parseFloat(attr.getString("position").split(",")[0]);
			float y =Float.parseFloat(attr.getString("position").split(",")[1]);
			img.setAbsolutePosition(x, y);
		}
	//	img.setAbsolutePosition(0, 0);	
//		img.scalePercent(50);//表示显示的大小为原尺寸的50%
//		img.scalePercent(25, 12);//图像高宽的显示比例
//		img.setRotation(30);//图像旋转一定角度
		return img;
	}
	
	private Paragraph getParagraph(Unit unit) throws Exception
	{
		BaseFont bfChinese = BaseFont.createFont("STSongStd-Light",
				"UniGB-UCS2-H", BaseFont.NOT_EMBEDDED);
		Font defaultFont = new Font(bfChinese, 10, Font.NORMAL);// 设置字体大小
		TextUnit tUnit = (TextUnit) unit;
		String str = tUnit.getText(); 
		
		
		InfoContainer info = tUnit.getObjAttr();
		if(info.getInteger("font-size")!=null){
			defaultFont.setSize(info.getInteger("font-size"));
		}
		if(info.getInteger("font-style")!=null){
			defaultFont.setStyle(info.getInteger("font-style"));
		}
		Paragraph p = new Paragraph(str,defaultFont);
		
		if(info.getInteger("text-align") != null){
			p.setAlignment(info.getInteger("text-align"));
		}
		return p;
	}
	
}
