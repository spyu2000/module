import java.io.File;
import java.io.IOException;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.format.Alignment;
import jxl.read.biff.BiffException;
import jxl.write.Font;
import jxl.write.Label;
import jxl.write.WritableCellFormat;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

import org.apache.commons.lang.StringUtils;;//.lang.xwork.StringUtils;

public class ExcelTools {
 /**
  * ��excel
  * @param filePath
  * @param sheetNum
  */
 public static void readExcel(String filePath,int sheetNum){
  try {
   Workbook  book=Workbook.getWorkbook(new File(filePath));
   Sheet sheet=book.getSheet(sheetNum);   
            for (int i = 0; i < sheet.getRows(); i++) {   
                //�����   
                Cell[] row = sheet.getRow(i);   
//                Map<String, String> rowMap=new HashMap<String, String>();   
                for (int j = 0; j < row.length; j++) {   
                    //��õ�Ԫ������   
                    String content=row[j].getContents();   
                    if(StringUtils.isNotBlank(content)){   
                        System.out.print(content + "  ");  
                    }   
                }   
                System.out.println();         
            } 
  } catch (BiffException e) {
   // TODO Auto-generated catch block
   e.printStackTrace();
  } catch (IOException e) {
   // TODO Auto-generated catch block
   e.printStackTrace();
  }   
 }
 
 public static void writeExcel(String filePath) {      
        try {
            WritableWorkbook workbook = Workbook.createWorkbook(new File(filePath));
            WritableSheet ws = workbook.createSheet("��һҳ", 0);
            WritableCellFormat format=new WritableCellFormat();
            format.setAlignment(Alignment.CENTRE);
            Label cell=new Label(0,0,"�����ļ�",format);
            
            for(int i=1;i<11;i++){//��
             for(int j=0;j<5;j++){  
               format=new WritableCellFormat();
               
              format.setAlignment(Alignment.CENTRE);
              cell=new Label(j,i,i+"��" +j + "��",format);
             
              ws.addCell(cell);
             }
            }    
            ws.mergeCells(0, 0, 1, 0);
            workbook.write();
            workbook.close();    //һ��Ҫ�ر�, ����û�б���Excel
        } catch (RowsExceededException e) {
            System.out.println("jxl write RowsExceededException: "+e.getMessage());
        } catch (WriteException e) {
            System.out.println("jxl write WriteException: "+e.getMessage());
        } catch (IOException e) {
            System.out.println("jxl write file i/o exception!, cause by: "+e.getMessage());
        }
    }
 
 public static void main(String[] args){
  //ExcelTools.readExcel("d:/Book1.xls", 0);
  ExcelTools.writeExcel("d:/Book1.xls");
 
 }
}

