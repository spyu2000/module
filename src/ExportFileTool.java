import java.awt.Color;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Iterator;

import jxl.Workbook;
import jxl.format.Alignment;
import jxl.write.Label;
import jxl.write.WritableCellFormat;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;


import com.csvreader.CsvWriter;
import com.fleety.base.StrFilter;
import com.lowagie.text.Cell;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Table;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.rtf.RtfWriter2;


public class ExportFileTool {

	public static final int PDF_TYPE = 1;
	public static final int EXCEL_TYPE = 2;
	public static final int WORD_TYPE = 3;
	public static final int CSV_TYPE = 4;
	private HashMap tMap = new HashMap();
	private ExportDataAndRules edar = new ExportDataAndRules();
	private int type = 1;
	private String[][] data ;
	
	public ExportFileTool(){
		tMap.put(PDF_TYPE, ".pdf");
		tMap.put(EXCEL_TYPE, ".xls");
		tMap.put(WORD_TYPE, ".doc");
		tMap.put(CSV_TYPE, ".csv");
	}
	public ExportFileTool(String[][] data,int type)
	{
		this();
		this.edar.setData(data);
		this.type = type;
	}
	
	/**
	 * 设置导出数据
	 * @param data
	 */
	public void setData(String[][] data){
		this.data = data;
	}
	
	/**
	 * 设置导出文件类型
	 * @param type
	 */
	public void setExportFileType(int type){
		this.type = type;
	}
	
	/**
	 * 设置合并单元格，x/y代表坐标,colspan列合并,rowspan行合并
	 * @param x
	 * @param y
	 * @param colSpan
	 * @param rowSpan
	 */
	public void setCellSpan(int x,int y,int colSpan,int rowSpan){
		this.edar.setCellSpan(x, y, colSpan, rowSpan);
	}
	
	public static void main(String[] args)
	{
		String fileName = "demo.pdf";
		System.out.println(fileName.indexOf("."));
		System.out.println(fileName.substring(fileName.indexOf("."),fileName.length()));
		ExportFileTool eft = new ExportFileTool();
		String[][] data =new String[5][100];
		for(int i=0;i<5;i++){
			for(int j=0;j<5;j++){
				data[i][j]=i+"行"+j+"列";
			}
		}
		//data[0][1]="";
		data[1][0]="";
		data[2][0]="";
		
		
		eft.setData(data);
		eft.setCellSpan(0, 0, 1, 3);
		eft.exportFile("d:/jason1", "demo.pdf");
		eft.exportFile("d:/jason1", "demo.xls");
		eft.exportFile("d:/jason1", "demo.doc");
		eft.exportFile("d:/jason1", "demo.csv");
		
	}
	
	/**
	 * 导出文件,对象ExportDataAndRules传递String[][] data数组,
	 * data数组对应table表格，如果某个表格为空，则数组对应位置设置为空字符串
	 * 通过ExportDataAndRules的setCellSpan方法对表格某个单元格设置合并
	 * @param filePath
	 * @param fileName
	 * @param suffix
	 * @param edar
	 * @return
	 */
	public boolean exportFile(String filePath,String fileName){
		//验证路径存在、文件名后缀与类型参数是否一致
		File  file = new File(filePath);
		if(!file.exists()){
			file.mkdirs();
		}
		String sufStr = (String) tMap.get(this.type);
		if(!sufStr.equalsIgnoreCase(fileName.substring(fileName.indexOf("."),fileName.length()))){
			System.out.println("ExportFileTool:文件名类型错误！");
			return false;
		}
		//不同类型的导出文件
		switch(this.type)
		{
			case PDF_TYPE:
				return exportPDF(filePath, fileName,edar);
			case EXCEL_TYPE:
				return exportEXCEL(filePath, fileName, edar);
			case WORD_TYPE:
				return exportWORD(filePath, fileName, edar);
			case CSV_TYPE:
				return exportCSV(filePath, fileName, edar);
			default:
				System.out.println("ExportFileTool:不被支持的文件类型!");
				return false;
		}
	}
	
	private boolean exportPDF(String filePath, String fileName, ExportDataAndRules edar){
		String[][] data = edar.getData();
		HashMap ruleMap = edar.getRuleMap();
		Document document = new Document();// 建立一个Document对象
		document.setPageSize(PageSize.A4);// 设置页面大小
		try {
			// 建立一个PdfWriter对象
			PdfWriter.getInstance(document, new FileOutputStream(filePath+File.separator+fileName));
			document.open();
			BaseFont bfChinese = BaseFont.createFont("STSong-Light","UniGB-UCS2-H", BaseFont.NOT_EMBEDDED);// 设置中文字体
			Font font = new Font(bfChinese, 10, Font.BOLD);// 设置字体大小
			float[] widths = new float[data.length];// 设置表格的列宽   
			for(int i=0;i<widths.length;i++){
				widths[i]=120f;
			}
			PdfPTable table = new PdfPTable(widths);// 建立一个pdf表格
			table.setTotalWidth(500);// 设置表格的宽度
			table.setLockedWidth(true);// 设置表格的宽度固定
			table.getDefaultCell().setBorder(1);// 设置表格默认为边框1
			PdfPCell cell;
			RuleCell ruleCell;
			for(int i=0;i<data.length;i++){
				for(int j=0;j<data[i].length;j++){
					if(StrFilter.hasValue(data[i][j])){
						cell = new PdfPCell(new Paragraph(data[i][j],font));
						cell.setHorizontalAlignment(Element.ALIGN_CENTER);// 设置内容水平居中显示
						ruleCell = (RuleCell) ruleMap.get(i+"-"+j);
						if(ruleCell!=null){
							cell.setColspan(ruleCell.getColspan());
							cell.setRowspan(ruleCell.getRowspan());
						}
						table.addCell(cell);
					}
				}
			}
			document.add(table);
			document.close();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	private boolean exportEXCEL(String filePath, String fileName, ExportDataAndRules edar){
		String[][] data = edar.getData();
		try {
            WritableWorkbook workbook = Workbook.createWorkbook(new File(filePath+File.separator+fileName));
            WritableSheet ws = workbook.createSheet("sheet_1", 0);
            WritableCellFormat format=new WritableCellFormat();
            format.setAlignment(Alignment.CENTRE);
            Label cell;
            
            for(int i=0;i<data.length;i++){//行   	
	             for(int j=0;j<data[i].length;j++){  
	            	 format=new WritableCellFormat();
	            	 format.setAlignment(Alignment.CENTRE);
	            	 cell=new Label(j,i,data[i][j],format);
	            	 ws.addCell(cell);
	             }
            } 
            //合并单元格
            Iterator it = edar.getRuleMap().keySet().iterator();
            String point;
            int colspan,rowspan,row,col;
            RuleCell ruleCell;
            while(it.hasNext())
            {
            	point = (String) it.next();
            	ruleCell = (RuleCell) edar.getRuleMap().get(point);
            	colspan = ruleCell.getColspan();
            	rowspan = ruleCell.getRowspan();
            	row = Integer.parseInt(point.split("-")[0]);
            	col = Integer.parseInt(point.split("-")[1]);
            	ws.mergeCells(col, row, col+(colspan-1), row+(rowspan-1));
            }
            workbook.write();
            workbook.close();    //一定要关闭, 否则没有保存Excel
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } 
        return true;
	}
	
	private boolean exportWORD(String filePath, String fileName, ExportDataAndRules edar){
		String[][] data = edar.getData();
		Document document = new Document(PageSize.A4);
		try {
			RtfWriter2.getInstance(document, new FileOutputStream(filePath
					+ File.separator + fileName));
			document.open();
			BaseFont bfChinese = BaseFont.createFont("STSongStd-Light",
					"UniGB-UCS2-H", BaseFont.NOT_EMBEDDED);
			Font contextFont = new Font(bfChinese, 10, Font.NORMAL);
			Table aTable = new Table(data.length);
			int width[] = new int[data.length];
			for (int i = 0; i < width.length; i++) {
				width[i] = 100 / width.length;
			}
			aTable.setWidths(width);// 设置每列所占比例
			aTable.setWidth(90); // 占页面宽度 90%
			aTable.setAlignment(Element.ALIGN_CENTER);// 居中显示
			aTable.setAlignment(Element.ALIGN_MIDDLE);// 纵向居中显示
			aTable.setAutoFillEmptyCells(true); // 自动填满
			aTable.setBorderWidth(1); // 边框宽度
			aTable.setBorderColor(new Color(0, 125, 255)); // 边框颜色
			aTable.setPadding(0);// 衬距
			aTable.setSpacing(0);// 即单元格之间的间距
			aTable.setBorder(2);// 边框

			Cell cell;
			RuleCell ruleCell;
			for (int i = 0; i < data.length; i++) {
				for (int j = 0; j < data[i].length; j++) {
					if(StrFilter.hasValue(data[i][j])){
						cell = new Cell(new Phrase(data[i][j],contextFont));
						ruleCell = (RuleCell) edar.getRuleMap().get(i+"-"+j);
						if(ruleCell!=null){
							cell.setColspan(ruleCell.getColspan());
							cell.setRowspan(ruleCell.getRowspan());
						}
						aTable.addCell(cell);
					}	
				}
			}
			document.add(aTable);
			document.close();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	private boolean exportCSV(String filePath, String fileName, ExportDataAndRules edar){		
		String[][] data = edar.getData();
		try {
			CsvWriter wr =new CsvWriter(filePath+File.separator+fileName,',',Charset.forName("gb2312")); 
			String[] contents;
			for(int i=0;i<data.length;i++){
				contents = data[i];
				wr.writeRecord(contents);
			}
			wr.close();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

}
