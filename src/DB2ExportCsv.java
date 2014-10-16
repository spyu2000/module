import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import com.csvreader.CsvReader;
import com.csvreader.CsvWriter;
 
public class DB2ExportCsv
{
    /**
    * ��ȡCSV�ļ�
    */
    public static void  readCsv(){
        try {    
				ArrayList<String[]> csvList = new ArrayList<String[]>(); //������������
				String csvFilePath = "D:/demo.csv";
				CsvReader reader = new CsvReader(csvFilePath,',',Charset.forName("gb2312"));    //һ�����������Ϳ�����    
				 
			//	reader.readHeaders(); // ������ͷ   �����Ҫ��ͷ�Ļ�����Ҫд��䡣
				 
				while(reader.readRecord()){ //���ж������ͷ������    
					csvList.add(reader.getValues());
				}            
				reader.close();
				 
				for(int row=0;row<csvList.size();row++){
				     String  cell = csvList.get(row)[0]; //ȡ�õ�row�е�0�е�����
				     System.out.println(cell);
				}     
			} catch (Exception ex) {
					System.out.println(ex);
				}
    }
    
    /**
     * д��CSV�ļ�
     */
    public static void WriteCsv(){
        try {
				String csvFilePath = "D:/demo.csv";
				CsvWriter wr =new CsvWriter(csvFilePath,',',Charset.forName("gb2312"));
				String[] contents = {"�澯��Ϣ","�Ƿ�����","û��Ȩ��","����ʧ��"};                    
				wr.writeRecord(contents);
				wr.close();
         } catch (IOException e) {
            e.printStackTrace();
            
         }
    }
    
    public static void main(String[] args){
    	//DB2ExportCsv.WriteCsv();
    	DB2ExportCsv.readCsv();
    }
}
