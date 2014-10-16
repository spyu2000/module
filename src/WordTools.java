import java.awt.Color;
import java.io.FileOutputStream;
import java.io.IOException;

import com.lowagie.text.Cell;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Table;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.rtf.RtfWriter2;

public class WordTools {

 public void createDocContext(String file) throws DocumentException,
 IOException {
 // ����ֽ�Ŵ�С
 Document document = new Document(PageSize.A4);
 // ����һ����д��(Writer)��document���������ͨ����д��(Writer)���Խ��ĵ�д�뵽������
 RtfWriter2.getInstance(document, new FileOutputStream(file));
 document.open();
 // ������������
 BaseFont bfChinese = BaseFont.createFont("STSongStd-Light",
 "UniGB-UCS2-H", BaseFont.NOT_EMBEDDED);
 // ����������
 Font titleFont = new Font(bfChinese, 12, Font.BOLD);
 // ����������
 Font contextFont = new Font(bfChinese, 10, Font.NORMAL);
 Paragraph title = new Paragraph("����");
 // ���ñ����ʽ���뷽ʽ
 title.setAlignment(Element.ALIGN_CENTER);
 title.setFont(titleFont);
 document.add(title);

 String contextString = "iText��һ���ܹ����ٲ���PDF�ļ���java��⡣"
 + " \n"// ����
 + "iText��java�������ЩҪ���������ı���"
 + "���ͼ�ε�ֻ���ĵ��Ǻ����õġ��������������java Servlet�кܺõĸ��ϡ�"
 + "ʹ��iText��PDF�ܹ�ʹ����ȷ�Ŀ���Servlet�������";
 Paragraph context = new Paragraph(contextString);
 // ���ĸ�ʽ�����
 context.setAlignment(Element.ALIGN_LEFT);
 context.setFont(contextFont);
 // ����һ���䣨���⣩�յ�����
 context.setSpacingBefore(5);
 // ���õ�һ�пյ�����
 context.setFirstLineIndent(20);
 document.add(context);

         //������FontFactory���Font��Color�������ø��ָ���������ʽ
 /**
 * Font.UNDERLINE �»��ߣ�Font.BOLD ����
 */
 Paragraph underline = new Paragraph("�»��ߵ�ʵ��", FontFactory.getFont(
 FontFactory.HELVETICA_BOLDOBLIQUE, 18, Font.UNDERLINE,
 new Color(0, 0, 255)));
 document.add(underline);
 
 // ���� Table ���
 Table aTable = new Table(3);
 int width[] = {25,25,50};
 aTable.setWidths(width);//����ÿ����ռ����
 aTable.setWidth(90); // ռҳ���� 90%

 aTable.setAlignment(Element.ALIGN_CENTER);//������ʾ
 aTable.setAlignment(Element.ALIGN_MIDDLE);//���������ʾ
 aTable.setAutoFillEmptyCells(true); //�Զ�����
 aTable.setBorderWidth(1); //�߿���
 aTable.setBorderColor(new Color(0, 125, 255)); //�߿���ɫ
 aTable.setPadding(0);//�ľ࣬��Ч����֪��ʲô��˼��
 aTable.setSpacing(0);//����Ԫ��֮��ļ��
 aTable.setBorder(2);//�߿�

 //���ñ�ͷ
 /**
 * cell.setHeader(true);�ǽ��õ�Ԫ����Ϊ��ͷ��Ϣ��ʾ��
 * cell.setColspan(3);ָ���˸õ�Ԫ��ռ3�У�
 * Ϊ�����ӱ�ͷ��Ϣʱ��Ҫע�����һ����ͷ��Ϣ�������֮�� \
 * ������� endHeaders()���������򵱱���ҳ�󣬱�ͷ��Ϣ��������ʾ
 */
 Cell haderCell = new Cell("����ͷ");
 haderCell.setHeader(true);
 haderCell.setColspan(3);
 aTable.addCell(haderCell);
 aTable.endHeaders();

 Font fontChinese = new Font(bfChinese, 12, Font.NORMAL, Color.GREEN);
 Cell cell = new Cell(new Phrase("����һ�����Ե� 3*3 Table ����", fontChinese ));
 cell.setVerticalAlignment(Element.ALIGN_TOP);
 cell.setBorderColor(new Color(255, 0, 0));
 cell.setRowspan(2);
 aTable.addCell(cell);

 aTable.addCell(new Cell("#1"));
 aTable.addCell(new Cell("#2"));
 aTable.addCell(new Cell("#3"));
 aTable.addCell(new Cell("#4"));
 Cell cell3 = new Cell(new Phrase("һ����������", fontChinese ));
 cell3.setColspan(3);
 cell3.setVerticalAlignment(Element.ALIGN_CENTER);
 aTable.addCell(cell3);

 document.add(aTable);
 document.add(new Paragraph("\n"));
 //���ͼƬ
 //Image img=Image.getInstance("http://127.0.0.1:8080/testSystem/images/1_r1_c1.png");
 Image img=Image.getInstance("d:/1.jpg");
 img.setAbsolutePosition(0, 0);
 img.setAlignment(Image.RIGHT);//����ͼƬ��ʾλ��
 img.scaleAbsolute(12,35);//ֱ���趨��ʾ�ߴ�
 img.scalePercent(50);//��ʾ��ʾ�Ĵ�СΪԭ�ߴ��50%
 img.scalePercent(25, 12);//ͼ��߿����ʾ����
 img.setRotation(30);//ͼ����תһ���Ƕ�
 document.add(img);

 document.close();
 } 
 public static void main(String[] args){
  WordTools b=new WordTools();
   try {
    b.createDocContext("d:/demo.doc");
   } catch (DocumentException e) {
    // TODO Auto-generated catch block
    e.printStackTrace();
   } catch (IOException e) {
    // TODO Auto-generated catch block
    e.printStackTrace();
   }
}
}

