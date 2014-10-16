import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import com.csvreader.CsvWriter; 



public class CsvDemo {
   
//    public static void main(String[] args) throws IOException {
//       
//        final String[] header = new String[]{"name", "sex", "age"};
//        final String[][] data = new String[][]{header, {"»’»’", "F", "22"}, {"Tom", "M", "25"}, {"Lily", "F", "19"}};
//
//         File tempFile = new File("./Output.csv");        
//
//         CsvWriter writer = new CsvWriter(new FileWriter(tempFile));
//        for (int i = 0; i < data.length; i++) {
//             writer.writeNext(data[i]);
//         }
//         writer.close();
//        
//     }
    public static void main(String[] args) {
        try {
        	CsvWriter w;
            FileWriter fw = new FileWriter("d:\\helloworld.csv");
            fw.write("aaa,bbb,hhh\r\n");
            fw.write("aa1,bb1,hh1\r\n");
            fw.write("aaa\r\n");
            fw.write("aa2,bb2,hh2\r\n");
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

