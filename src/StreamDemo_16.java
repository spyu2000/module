
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


//��iText����word�ļ�
public class StreamDemo_16 {

	public void createDocFile(String file) throws DocumentException,
			IOException {
		// ����ֽ�Ŵ�С
		Document document = new Document(PageSize.A4);
		// ����һ����д��(Writer)��document���������ͨ����д��(Writer)���Խ��ĵ�д�뵽������
		RtfWriter2.getInstance(document, new FileOutputStream(file));
		document.open();
		// ������������
		BaseFont macintosh = BaseFont.createFont(
				"C:\\WINDOWS\\Fonts\\STFANGSO.TTF", BaseFont.IDENTITY_H,
				BaseFont.EMBEDDED);
		// ����������
		Font ThemeFont = new Font(macintosh, 18, Font.BOLD);
		// ���ñ����������ɫ
		ThemeFont.setColor(Color.RED);
		// ����������
		Font bodyFont = new Font(macintosh, 14, Font.NORMAL);
		// ���������������ɫ
		bodyFont.setColor(56, 94, 15);
		// ���������Phrases����
		Paragraph theme = new Paragraph("������");
		// �����úõ�������ӵ�����Ķ̾���
		theme.setAlignment(Element.ALIGN_CENTER);
		//theme.setFont(ThemeFont);
		// ��Phrases��ӵ�document�ĵ���
		document.add(theme);
		// ����word�����е�����
		String bodyText = "Java����ͨ�������������/���������������������Ϣ�ĳ���"
				+ "��ͨ��Java������/���ϵͳ�������豸���ӣ��������������ӵ������豸������ͬ��"
				+ "��������������Ϊ����ͬ���ķ�ʽ����������ͬ������/�����ͷ����������������͵��ⲿ�豸��"
				+ "����ζ��һ���������ܹ�������ֲ�ͬ���͵����룺�Ӵ����ļ����Ӽ��̻�������׽��֣�"
				+ "ͬ����һ��������������������̨�������ļ������������硣"
				+ "���Ǵ�������/�����һ���ྻ�ķ���������������Ҫ���������̺�����Ĳ�ͬ��"
				+ "Java������ʵ���ǻ���java.io����������νṹ�ġ�";
		// �������ĵ�Phrases����
		Paragraph context = new Paragraph(bodyText);
		// ���ĸ�ʽ�����
		context.setAlignment(Element.ALIGN_LEFT);
		// ���������������ɫ
		//context.setFont(bodyFont);
		// ����һ���䣨���⣩�յ�����
		context.setSpacingBefore(3);
		// ���õ�һ�пյ�����
		context.setFirstLineIndent(20);
		// // ��Phrases��ӵ�document�ĵ���
		document.add(context);
		// ������FontFactory���Font��Color�������ø��ָ���������ʽ
		Paragraph line = new Paragraph("�»��ߵ�ʵ��", FontFactory.getFont(
				FontFactory.HELVETICA_BOLDOBLIQUE, 18, Font.UNDERLINE,
				new Color(0, 0, 255)));
		document.add(line);

		// ����Table���
		Table table = new Table(5);// �����ñ����������ڱ���������Ϊ5��
		int width[] = { 25, 25, 25, 25, 25 };// ÿ�еĵ�Ԫ��Ŀ��
		table.setWidths(width);// ����ÿ����ռ����
		table.setWidth(90); // ռҳ���� 90%���൱��html��width����
		table.setAlignment(Element.ALIGN_CENTER);// ���øñ���е�Ԫ��ˮƽ���������ʾ
		table.setAlignment(Element.ALIGN_MIDDLE);// ���øñ���е�Ԫ�ش�ֱ�������������ʾ
		table.setAutoFillEmptyCells(true); // �Զ�����
		table.setBorderWidth(1); // �߿���
		table.setBorderColor(new Color(160, 32, 240)); // �߿���ɫ
		table.setPadding(2);// ��Ԫ���ڲ��Ŀհ׾��룬�൱��html�е�cellpadding����
		table.setSpacing(3);// ��Ԫ��֮��ļ�࣬�൱��html�е�cellspacing
		table.setBorder(2);// �߿�Ŀ��
		// ���ñ�ͷ
		Cell haderCell = new Cell("��iText�����ı��-��ͷ");// ������Ԫ��
		haderCell.setBackgroundColor(Color.pink);// ���ô˵�Ԫ��ı���ɫ
		haderCell.setHeader(true);// ����Ϊ��ͷ
		haderCell.setColspan(5);// �ϲ��е�����
		haderCell.setHorizontalAlignment(haderCell.ALIGN_CENTER);// ˮƽ��ʾ��λ��
		table.addCell(haderCell);// ����Ԫ����ӵ������
		table.endHeaders();// ������ͷ������
		Font fontChinese = new Font(macintosh, 15, Font.NORMAL, Color.blue);// ����������
		Cell cell = new Cell(new Phrase("����һ��3��1�кϲ��ı��", fontChinese));// ������Ԫ��
		cell.setVerticalAlignment(Element.ALIGN_TOP);
		cell.setBorderColor(new Color(255, 215, 0));
		cell.setRowspan(3);// ���úϲ�������
	
		// ��ӵ�Ԫ��
		table.addCell(cell);
		table.addCell(new Cell("��һ�е�һ��"));
		table.addCell(new Cell("��һ�еڶ���"));
		table.addCell(new Cell("��һ�е�����"));
		table.addCell(new Cell("��һ�е�����"));
		table.addCell(new Cell("�ڶ��е�һ��"));
		table.addCell(new Cell("�ڶ��еڶ���"));
		table.addCell(new Cell("�ڶ��е�����"));
		table.addCell(new Cell("�ڶ��е�����"));
		table.addCell(new Cell("�����е�һ��"));
		table.addCell(new Cell("�����еڶ���"));
		table.addCell(new Cell("�����е�����"));
		table.addCell(new Cell("�����е�����"));
		// ����һ���ϲ�5�еĵ�Ԫ��
		Cell cell3 = new Cell(new Phrase("һ��5�кϲ��ı��", fontChinese));
		cell3.setColspan(5);
		cell3.setVerticalAlignment(Element.ALIGN_CENTER);
		table.addCell(cell3);
		document.add(table);
		// ���ͼƬ
//		Image img = Image.getInstance("D:\\ͼƬ\\2.jpg");
//		img.setAbsolutePosition(0, 0);//
//		document.add(img);
		
		
		document.close();
	}

	public static void main(String[] args) {
		StreamDemo_16 word = new StreamDemo_16();
		String file = "d:/demo2.doc";
		try {
			word.createDocFile(file);
		} catch (DocumentException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}