import java.io.FileOutputStream;

import java.io.IOException;

import com.lowagie.text.Document;
import com.lowagie.text.Image;

import com.lowagie.text.DocumentException;

import com.lowagie.text.Element;

import com.lowagie.text.Font;

import com.lowagie.text.PageSize;

import com.lowagie.text.Paragraph;

import com.lowagie.text.pdf.BaseFont;

import com.lowagie.text.pdf.PdfPCell;

import com.lowagie.text.pdf.PdfPTable;

import com.lowagie.text.pdf.PdfWriter;

public class PdfTools {
 /**
  *
  * ����PDF�ķ���
  *
  * @return boolean
  *
  */

 public static boolean createPDF(String pdfPath) {

  Document document = new Document();// ����һ��Document����

  document.setPageSize(PageSize.A4);// ����ҳ���С

  try {

   PdfWriter.getInstance(document, new FileOutputStream(pdfPath));// ����һ��PdfWriter����
   document.open();
   BaseFont bfChinese = BaseFont.createFont("STSong-Light",
     "UniGB-UCS2-H", BaseFont.NOT_EMBEDDED);// ������������
   Font titleFont = new Font(bfChinese, 15, Font.BOLD);// ���������С
   Font headFont = new Font(bfChinese, 10, Font.BOLD);// ���������С
   Font headFont1 = new Font(bfChinese, 10, Font.BOLD);// ���������С
   Font headFont2 = new Font(bfChinese, 10, Font.NORMAL);// ���������С
   document.add(new Paragraph("����",
     headFont));
   
   float[] widths = { 140f, 60f, 320f, 120f, 110f, 110f, 190f };// ���ñ����п�
   
   PdfPTable table = new PdfPTable(widths);// ����һ��pdf���

   table.setSpacingBefore(20f);// ���ñ������հ׿��

   table.setTotalWidth(535);// ���ñ��Ŀ��

   table.setLockedWidth(true);// ���ñ��Ŀ�ȹ̶�

    table.getDefaultCell().setBorder(1);//���ñ��Ĭ��Ϊ�߿�1

   PdfPCell cell = new PdfPCell(new Paragraph("Taony125 testPdf ��������",
     headFont));// ����һ����Ԫ��

   // cell.setBorder(0);//���õ�Ԫ���ޱ߿�

//   cell.setColspan(7);// ���úϲ���Ԫ�������

   table.addCell(cell);// ���ӵ�Ԫ��

   cell = new PdfPCell(
     new Paragraph("Taony125 testPdf ��������", headFont));

   // cell.setBorder(0);

   cell.setFixedHeight(20);

   cell.setColspan(7);// ���úϲ���Ԫ�������

   cell.setHorizontalAlignment(Element.ALIGN_CENTER);// ��������ˮƽ������ʾ

   cell.setVerticalAlignment(Element.ALIGN_MIDDLE);

   table.addCell(cell);

   cell = new PdfPCell(new Paragraph("Taony125 testPdf ��������",
     headFont1));

   // cell.setBorder(0);

   cell.setFixedHeight(20);

//   cell.setColspan(7);// ���úϲ���Ԫ�������

   cell.setHorizontalAlignment(Element.ALIGN_CENTER);// ��������ˮƽ������ʾ

   cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
   
   table.addCell(cell);

   table.addCell(new Paragraph("Taony125 testPdf ��������", headFont2));

   document.add(table);
   
   Image img=Image.getInstance("d:/1.jpg");
   img.setAbsolutePosition(0, 0);
   img.setAlignment(Image.RIGHT);//����ͼƬ��ʾλ��
   img.scaleAbsolute(12,35);//ֱ���趨��ʾ�ߴ�
   img.scalePercent(50);//��ʾ��ʾ�Ĵ�СΪԭ�ߴ��50%
   img.scalePercent(25, 12);//ͼ��߿����ʾ����
   img.setRotation(30);//ͼ����תһ���Ƕ�
   document.add(img);

  } catch (DocumentException de) {

   System.err.println(de.getMessage());

   return false;

  }

  catch (IOException ioe) {

   System.err.println(ioe.getMessage());

   return false;

  }

  document.close();

  return true;

 }

 /**
  *
  * @param args
  *
  */

 public static void main(String[] args) {

  // TODO �Զ����ɷ������

  PdfTools.createPDF("d:/test.pdf");

 }

}
