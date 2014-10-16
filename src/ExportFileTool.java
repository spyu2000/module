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
	 * ���õ�������
	 * @param data
	 */
	public void setData(String[][] data){
		this.data = data;
	}
	
	/**
	 * ���õ����ļ�����
	 * @param type
	 */
	public void setExportFileType(int type){
		this.type = type;
	}
	
	/**
	 * ���úϲ���Ԫ��x/y��������,colspan�кϲ�,rowspan�кϲ�
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
				data[i][j]=i+"��"+j+"��";
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
	 * �����ļ�,����ExportDataAndRules����String[][] data����,
	 * data�����Ӧtable������ĳ�����Ϊ�գ��������Ӧλ������Ϊ���ַ���
	 * ͨ��ExportDataAndRules��setCellSpan�����Ա��ĳ����Ԫ�����úϲ�
	 * @param filePath
	 * @param fileName
	 * @param suffix
	 * @param edar
	 * @return
	 */
	public boolean exportFile(String filePath,String fileName){
		//��֤·�����ڡ��ļ�����׺�����Ͳ����Ƿ�һ��
		File  file = new File(filePath);
		if(!file.exists()){
			file.mkdirs();
		}
		String sufStr = (String) tMap.get(this.type);
		if(!sufStr.equalsIgnoreCase(fileName.substring(fileName.indexOf("."),fileName.length()))){
			System.out.println("ExportFileTool:�ļ������ʹ���");
			return false;
		}
		//��ͬ���͵ĵ����ļ�
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
				System.out.println("ExportFileTool:����֧�ֵ��ļ�����!");
				return false;
		}
	}
	
	private boolean exportPDF(String filePath, String fileName, ExportDataAndRules edar){
		String[][] data = edar.getData();
		HashMap ruleMap = edar.getRuleMap();
		Document document = new Document();// ����һ��Document����
		document.setPageSize(PageSize.A4);// ����ҳ���С
		try {
			// ����һ��PdfWriter����
			PdfWriter.getInstance(document, new FileOutputStream(filePath+File.separator+fileName));
			document.open();
			BaseFont bfChinese = BaseFont.createFont("STSong-Light","UniGB-UCS2-H", BaseFont.NOT_EMBEDDED);// ������������
			Font font = new Font(bfChinese, 10, Font.BOLD);// ���������С
			float[] widths = new float[data.length];// ���ñ����п�   
			for(int i=0;i<widths.length;i++){
				widths[i]=120f;
			}
			PdfPTable table = new PdfPTable(widths);// ����һ��pdf���
			table.setTotalWidth(500);// ���ñ��Ŀ��
			table.setLockedWidth(true);// ���ñ��Ŀ�ȹ̶�
			table.getDefaultCell().setBorder(1);// ���ñ��Ĭ��Ϊ�߿�1
			PdfPCell cell;
			RuleCell ruleCell;
			for(int i=0;i<data.length;i++){
				for(int j=0;j<data[i].length;j++){
					if(StrFilter.hasValue(data[i][j])){
						cell = new PdfPCell(new Paragraph(data[i][j],font));
						cell.setHorizontalAlignment(Element.ALIGN_CENTER);// ��������ˮƽ������ʾ
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
            
            for(int i=0;i<data.length;i++){//��   	
	             for(int j=0;j<data[i].length;j++){  
	            	 format=new WritableCellFormat();
	            	 format.setAlignment(Alignment.CENTRE);
	            	 cell=new Label(j,i,data[i][j],format);
	            	 ws.addCell(cell);
	             }
            } 
            //�ϲ���Ԫ��
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
            workbook.close();    //һ��Ҫ�ر�, ����û�б���Excel
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
			aTable.setWidths(width);// ����ÿ����ռ����
			aTable.setWidth(90); // ռҳ���� 90%
			aTable.setAlignment(Element.ALIGN_CENTER);// ������ʾ
			aTable.setAlignment(Element.ALIGN_MIDDLE);// ���������ʾ
			aTable.setAutoFillEmptyCells(true); // �Զ�����
			aTable.setBorderWidth(1); // �߿���
			aTable.setBorderColor(new Color(0, 125, 255)); // �߿���ɫ
			aTable.setPadding(0);// �ľ�
			aTable.setSpacing(0);// ����Ԫ��֮��ļ��
			aTable.setBorder(2);// �߿�

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
