package com.fleety.base.export.test;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import jxl.write.Label;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableImage;

public class ExportImage2Excel {
	
	public void exportImage() throws Exception
	{
//		首先,给这个文件起名字.
		//得到当前日期
		SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");      
		Calendar rightNow = Calendar.getInstance();
		Date now = rightNow.getTime();
		String today = sdf.format(now).toString();
		String pathpath=System.getProperty("user.dir")+"/jxldemo"+today+".xls";
//		System.getProperty("user.dir") 能得到当前类的路径


		//声明文件流,打开文件
		   //声明，和写文件没什么不一样的。
		File file= new File(pathpath);//pathpath文件路径
		jxl.write.WritableWorkbook wwb = jxl.Workbook.createWorkbook(file);//转换成excel像是
		jxl.write.WritableSheet ws = wwb.createSheet("接口巡检表",0);//创建<接口巡检表>Sheet
		Label labelC = null; //在这里设置具体写在什么位置

		//这个是有文件已经有了,你打开看一下就会发现有一个名字是"接口巡检表"的空 Sheet


		//插入图片:
		File fileImage=new File(System.getProperty("user.dir")+"/logo.png");
		WritableImage image=new WritableImage(0, 0,2,3,fileImage);//从A1开始 跨2行3个单元格
		ws.addImage(image);//ws是Sheet

		//WritableImage 图像操作,



		//设置文字格式,下面生成2个 例子
		WritableCellFormat timesBoldUnderline = null;
//		9号 粗体
		WritableFont font9 = new WritableFont (WritableFont.TIMES,9,WritableFont.BOLD,false);
		timesBoldUnderline = new WritableCellFormat(font9);
//		水平居中
		timesBoldUnderline.setAlignment(jxl.format.Alignment.CENTRE);
//		垂直居中
		timesBoldUnderline.setVerticalAlignment(jxl.format.VerticalAlignment.CENTRE);
//		全边框
		timesBoldUnderline.setBorder(jxl.format.Border.ALL,jxl.format.BorderLineStyle.THIN);

		   //宋体9号 左对齐
		WritableCellFormat BoldUnderlineLEFT = null;
		WritableFont times9ptBoldUnderlineLEFT = new WritableFont(WritableFont.createFont("宋体"),9);   
		BoldUnderlineLEFT = new WritableCellFormat(times9ptBoldUnderlineLEFT);
		BoldUnderlineLEFT.setAlignment(jxl.format.Alignment.LEFT);
		BoldUnderlineLEFT.setVerticalAlignment(jxl.format.VerticalAlignment.CENTRE);
		BoldUnderlineLEFT.setBorder(jxl.format.Border.ALL,jxl.format.BorderLineStyle.THIN);


		//BoldUnderlineLEFT 和 timesBoldUnderline 就是编辑好的2个格式,添加文字的时候 就可以用了.


//		插入文字
//		 A1单元格写入, timesBoldUnderline是WritableCellFormat字体
		labelC=new jxl.write.Label(0,0,"巡检表",timesBoldUnderline);
//		合并单元格,跨8个单元格.3列
		ws.mergeCells(0,0,8,3);
//		添加进去
		ws.addCell(labelC);

		    labelC=new jxl.write.Label(1,1,"没有格式的文字写入");
		ws.addCell(labelC);
		labelC=new jxl.write.Label(1,2,"带有文字样式的文字写入",timesBoldUnderline);
		ws.addCell(labelC);
		labelC=new jxl.write.Label(1,3,"设置宽高",timesBoldUnderline);
		ws.setColumnView(1,15); //设置单元格 列1, 宽15
		ws.addCell(labelC);


		//写入 关闭

		wwb.write();
		wwb.close();

	}
	
	
}
