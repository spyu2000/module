package test;

import java.io.File;
import java.io.RandomAccessFile;
import java.text.ParseException;
import java.util.Date;

import com.fleety.base.GeneralConst;

public class TimeTest {
	public static void main(String[] args) throws Exception{

		RandomAccessFile file = new RandomAccessFile(new File("e:/ÐìÐÂÏîÄ¿.rar"),"r");
		file.seek(100);
		System.out.println(file.read());
		
	}
}
