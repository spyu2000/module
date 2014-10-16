import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import com.csvreader.CsvReader;
import com.csvreader.CsvWriter;
 
public class DB2ExportCsv
{
    /**
    * 读取CSV文件
    */
    public static void  readCsv(){
        try {    
				ArrayList<String[]> csvList = new ArrayList<String[]>(); //用来保存数据
				String csvFilePath = "D:/demo.csv";
				CsvReader reader = new CsvReader(csvFilePath,',',Charset.forName("gb2312"));    //一般用这编码读就可以了    
				 
			//	reader.readHeaders(); // 跳过表头   如果需要表头的话，不要写这句。
				 
				while(reader.readRecord()){ //逐行读入除表头的数据    
					csvList.add(reader.getValues());
				}            
				reader.close();
				 
				for(int row=0;row<csvList.size();row++){
				     String  cell = csvList.get(row)[0]; //取得第row行第0列的数据
				     System.out.println(cell);
				}     
			} catch (Exception ex) {
					System.out.println(ex);
				}
    }
    
    /**
     * 写入CSV文件
     */
    public static void WriteCsv(){
        try {
				String csvFilePath = "D:/demo.csv";
				CsvWriter wr =new CsvWriter(csvFilePath,',',Charset.forName("gb2312"));
				String[] contents = {"告警信息","非法操作","没有权限","操作失败"};                    
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
