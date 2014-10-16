package com.fleety.base.export.tool;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;

import com.fleety.base.InfoContainer;
import com.fleety.base.StrFilter;
import com.fleety.base.export.data.ExportDataDesc;
import com.fleety.base.export.data.ImageUnit;
import com.fleety.base.export.data.TableUnit;
import com.fleety.base.export.data.TextUnit;
import com.fleety.base.export.data.Unit;
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
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.rtf.RtfWriter2;

public class ExportPDF implements IExport {

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
		if(!".pdf".equalsIgnoreCase(filePath.substring(filePath.indexOf("."),filePath.length()))){
			System.out.println("ExportPDF:文件名类型错误！");
			return false;
		}
		Unit[] units = desc.getAllUnits();
		Document document = new Document(PageSize.A4);
		try {
			PdfWriter.getInstance(document, new FileOutputStream(filePath));
			document.open();
			for(int i = 0;i < units.length;i ++)
			{
				Unit unit = units[i];
				int type = unit.getType();
				switch(type)
				{
					case Unit.TABLE_UNIT:
						PdfPTable table = getTable(unit);
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

	private Paragraph getParagraph(Unit unit) throws Exception {
		BaseFont bfChinese = BaseFont.createFont("STSongStd-Light",
			     "UniGB-UCS2-H", BaseFont.NOT_EMBEDDED);// 设置中文字体
		Font defaultFont = new Font(bfChinese, 10, Font.NORMAL);// 设置字体大小
		TextUnit tUnit = (TextUnit) unit;
		String str = tUnit.getText(); 
		
		System.out.println("str:"+str);
		
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

	private Image getImage(Unit unit) {
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
//		img.setAbsolutePosition(0, 0);
//		img.setAlignment(Image.ALIGN_CENTER);//设置图片显示位置
//		img.scaleAbsolute(80,80);//直接设定显示尺寸
//		img.scalePercent(50);//表示显示的大小为原尺寸的50%
//		img.scalePercent(25, 12);//图像高宽的显示比例
//		img.setRotation(30);//图像旋转一定角度
		return img;
	}

	private PdfPTable getTable(Unit unit) throws Exception {
		BaseFont bfChinese = BaseFont.createFont("STSongStd-Light",
				"UniGB-UCS2-H", BaseFont.NOT_EMBEDDED);
		Font defaultFont = new Font(bfChinese, 10, Font.NORMAL);// 设置字体大小
		TableUnit tUnit = (TableUnit) unit;
		String[][] data = tUnit.getTableData();
		InfoContainer info = tUnit.getObjAttr();
		if(info.getInteger("font-size")!=null){
			defaultFont.setSize(info.getInteger("font-size"));
		}
		if(info.getInteger("font-style")!=null){
			defaultFont.setStyle(info.getInteger("font-style"));
		}
		float[] widths = new float[data.length];// 设置表格的列宽   
		for(int i=0;i<widths.length;i++){
			widths[i]=120f;
		}
		PdfPTable table = new PdfPTable(widths);// 建立一个pdf表格
		table.setTotalWidth(500);// 设置表格的宽度
		table.setLockedWidth(true);// 设置表格的宽度固定
		table.getDefaultCell().setBorder(1);// 设置表格默认为边框1
		PdfPCell cell;
		Paragraph p;
		for(int i=0;i<data.length;i++)
		{
			for(int j=0;j<data[i].length;j++)
			{
				if(StrFilter.hasValue(data[i][j]))
				{
					p = new Paragraph(data[i][j],defaultFont);
					cell = new PdfPCell(p);
					if(info.getInfo("text-align")!=null){
						cell.setHorizontalAlignment((Integer) info.getInfo("text-align"));// 设置内容水平居中显示
					}
					if(info.getInfo(i+"-"+j+"-span")!=null)
					{
						String spanStr = (String) info.getInfo(i+"-"+j+"-span");
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

}
