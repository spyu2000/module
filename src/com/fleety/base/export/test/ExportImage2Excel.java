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
//		����,������ļ�������.
		//�õ���ǰ����
		SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");      
		Calendar rightNow = Calendar.getInstance();
		Date now = rightNow.getTime();
		String today = sdf.format(now).toString();
		String pathpath=System.getProperty("user.dir")+"/jxldemo"+today+".xls";
//		System.getProperty("user.dir") �ܵõ���ǰ���·��


		//�����ļ���,���ļ�
		   //��������д�ļ�ûʲô��һ���ġ�
		File file= new File(pathpath);//pathpath�ļ�·��
		jxl.write.WritableWorkbook wwb = jxl.Workbook.createWorkbook(file);//ת����excel����
		jxl.write.WritableSheet ws = wwb.createSheet("�ӿ�Ѳ���",0);//����<�ӿ�Ѳ���>Sheet
		Label labelC = null; //���������þ���д��ʲôλ��

		//��������ļ��Ѿ�����,��򿪿�һ�¾ͻᷢ����һ��������"�ӿ�Ѳ���"�Ŀ� Sheet


		//����ͼƬ:
		File fileImage=new File(System.getProperty("user.dir")+"/logo.png");
		WritableImage image=new WritableImage(0, 0,2,3,fileImage);//��A1��ʼ ��2��3����Ԫ��
		ws.addImage(image);//ws��Sheet

		//WritableImage ͼ�����,



		//�������ָ�ʽ,��������2�� ����
		WritableCellFormat timesBoldUnderline = null;
//		9�� ����
		WritableFont font9 = new WritableFont (WritableFont.TIMES,9,WritableFont.BOLD,false);
		timesBoldUnderline = new WritableCellFormat(font9);
//		ˮƽ����
		timesBoldUnderline.setAlignment(jxl.format.Alignment.CENTRE);
//		��ֱ����
		timesBoldUnderline.setVerticalAlignment(jxl.format.VerticalAlignment.CENTRE);
//		ȫ�߿�
		timesBoldUnderline.setBorder(jxl.format.Border.ALL,jxl.format.BorderLineStyle.THIN);

		   //����9�� �����
		WritableCellFormat BoldUnderlineLEFT = null;
		WritableFont times9ptBoldUnderlineLEFT = new WritableFont(WritableFont.createFont("����"),9);   
		BoldUnderlineLEFT = new WritableCellFormat(times9ptBoldUnderlineLEFT);
		BoldUnderlineLEFT.setAlignment(jxl.format.Alignment.LEFT);
		BoldUnderlineLEFT.setVerticalAlignment(jxl.format.VerticalAlignment.CENTRE);
		BoldUnderlineLEFT.setBorder(jxl.format.Border.ALL,jxl.format.BorderLineStyle.THIN);


		//BoldUnderlineLEFT �� timesBoldUnderline ���Ǳ༭�õ�2����ʽ,������ֵ�ʱ�� �Ϳ�������.


//		��������
//		 A1��Ԫ��д��, timesBoldUnderline��WritableCellFormat����
		labelC=new jxl.write.Label(0,0,"Ѳ���",timesBoldUnderline);
//		�ϲ���Ԫ��,��8����Ԫ��.3��
		ws.mergeCells(0,0,8,3);
//		��ӽ�ȥ
		ws.addCell(labelC);

		    labelC=new jxl.write.Label(1,1,"û�и�ʽ������д��");
		ws.addCell(labelC);
		labelC=new jxl.write.Label(1,2,"����������ʽ������д��",timesBoldUnderline);
		ws.addCell(labelC);
		labelC=new jxl.write.Label(1,3,"���ÿ��",timesBoldUnderline);
		ws.setColumnView(1,15); //���õ�Ԫ�� ��1, ��15
		ws.addCell(labelC);


		//д�� �ر�

		wwb.write();
		wwb.close();

	}
	
	
}
