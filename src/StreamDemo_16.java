
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


//用iText生成word文件
public class StreamDemo_16 {

	public void createDocFile(String file) throws DocumentException,
			IOException {
		// 设置纸张大小
		Document document = new Document(PageSize.A4);
		// 建立一个书写器(Writer)与document对象关联，通过书写器(Writer)可以将文档写入到磁盘中
		RtfWriter2.getInstance(document, new FileOutputStream(file));
		document.open();
		// 设置中文字体
		BaseFont macintosh = BaseFont.createFont(
				"C:\\WINDOWS\\Fonts\\STFANGSO.TTF", BaseFont.IDENTITY_H,
				BaseFont.EMBEDDED);
		// 标题字体风格
		Font ThemeFont = new Font(macintosh, 18, Font.BOLD);
		// 设置标题字体的颜色
		ThemeFont.setColor(Color.RED);
		// 正文字体风格
		Font bodyFont = new Font(macintosh, 14, Font.NORMAL);
		// 设置正文字体的颜色
		bodyFont.setColor(56, 94, 15);
		// 创建主题的Phrases对象
		Paragraph theme = new Paragraph("主标题");
		// 将设置好的字体添加到主题的短句上
		theme.setAlignment(Element.ALIGN_CENTER);
		//theme.setFont(ThemeFont);
		// 将Phrases添加到document文档中
		document.add(theme);
		// 设置word正文中的内容
		String bodyText = "Java程序通过流来完成输入/输出，它是生产或消费信息的抽象。"
				+ "流通过Java的输入/输出系统与物理设备链接，尽管与它们链接的物理设备不尽相同，"
				+ "但是所有流的行为具有同样的方式，这样，相同的输入/输出类和方法适用于所有类型的外部设备，"
				+ "这意味着一个输入流能够抽象多种不同类型的输入：从磁盘文件，从键盘或从网络套接字，"
				+ "同样，一个输出流可以输出到控制台，磁盘文件或相连的网络。"
				+ "流是处理输入/输出的一个洁净的方法，例如它不需要代码理解键盘和网络的不同。"
				+ "Java中流的实现是基于java.io包定义的类层次结构的。";
		// 创建正文的Phrases对象
		Paragraph context = new Paragraph(bodyText);
		// 正文格式左对齐
		context.setAlignment(Element.ALIGN_LEFT);
		// 设置正文字体的颜色
		//context.setFont(bodyFont);
		// 离上一段落（标题）空的行数
		context.setSpacingBefore(3);
		// 设置第一行空的列数
		context.setFirstLineIndent(20);
		// // 将Phrases添加到document文档中
		document.add(context);
		// 利用类FontFactory结合Font和Color可以设置各种各样字体样式
		Paragraph line = new Paragraph("下划线的实现", FontFactory.getFont(
				FontFactory.HELVETICA_BOLDOBLIQUE, 18, Font.UNDERLINE,
				new Color(0, 0, 255)));
		document.add(line);

		// 创建Table表格
		Table table = new Table(5);// 创建该表格的列数，在本程序中设为5列
		int width[] = { 25, 25, 25, 25, 25 };// 每列的单元格的宽度
		table.setWidths(width);// 设置每列所占比例
		table.setWidth(90); // 占页面宽度 90%，相当于html中width属性
		table.setAlignment(Element.ALIGN_CENTER);// 设置该表格中的元素水平方向居中显示
		table.setAlignment(Element.ALIGN_MIDDLE);// 设置该表格中的元素垂直方向纵向居中显示
		table.setAutoFillEmptyCells(true); // 自动填满
		table.setBorderWidth(1); // 边框宽度
		table.setBorderColor(new Color(160, 32, 240)); // 边框颜色
		table.setPadding(2);// 单元格内部的空白距离，相当于html中的cellpadding属性
		table.setSpacing(3);// 单元格之间的间距，相当于html中的cellspacing
		table.setBorder(2);// 边框的宽度
		// 设置表头
		Cell haderCell = new Cell("用iText创建的表格-表头");// 创建单元格
		haderCell.setBackgroundColor(Color.pink);// 设置此单元格的背景色
		haderCell.setHeader(true);// 设置为表头
		haderCell.setColspan(5);// 合并列的列数
		haderCell.setHorizontalAlignment(haderCell.ALIGN_CENTER);// 水平显示的位置
		table.addCell(haderCell);// 将单元格添加到表格中
		table.endHeaders();// 结束表头的设置
		Font fontChinese = new Font(macintosh, 15, Font.NORMAL, Color.blue);// 设置字体风格
		Cell cell = new Cell(new Phrase("这是一个3行1列合并的表格", fontChinese));// 创建单元格
		cell.setVerticalAlignment(Element.ALIGN_TOP);
		cell.setBorderColor(new Color(255, 215, 0));
		cell.setRowspan(3);// 设置合并的行数
	
		// 添加单元格
		table.addCell(cell);
		table.addCell(new Cell("第一行第一列"));
		table.addCell(new Cell("第一行第二列"));
		table.addCell(new Cell("第一行第三列"));
		table.addCell(new Cell("第一行第四列"));
		table.addCell(new Cell("第二行第一列"));
		table.addCell(new Cell("第二行第二列"));
		table.addCell(new Cell("第二行第三列"));
		table.addCell(new Cell("第二行第四列"));
		table.addCell(new Cell("第三行第一列"));
		table.addCell(new Cell("第三行第二列"));
		table.addCell(new Cell("第三行第三列"));
		table.addCell(new Cell("第三行第四列"));
		// 创建一个合并5列的单元格
		Cell cell3 = new Cell(new Phrase("一行5列合并的表格", fontChinese));
		cell3.setColspan(5);
		cell3.setVerticalAlignment(Element.ALIGN_CENTER);
		table.addCell(cell3);
		document.add(table);
		// 添加图片
//		Image img = Image.getInstance("D:\\图片\\2.jpg");
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