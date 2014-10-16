package com.fleety.base.export.tool;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import com.csvreader.CsvWriter;
import com.fleety.base.export.data.ExportDataDesc;
import com.fleety.base.export.data.TableUnit;
import com.fleety.base.export.data.TextUnit;
import com.fleety.base.export.data.Unit;

public class ExportCSV implements IExport {

	public boolean exportFile(String filePath, ExportDataDesc desc) {
		File  file = new File(filePath);
		if(!file.exists()){
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if(!".CSV".equalsIgnoreCase(filePath.substring(filePath.indexOf("."),filePath.length()))){
			System.out.println("ExportCSV:文件名类型错误！");
			return false;
		}
		Unit[] units = desc.getAllUnits();
		try {
			CsvWriter wr =new CsvWriter(filePath,',',Charset.forName("gb2312")); 
			String[] contents;
			String[][] data;
			for(int i =0 ;i<units.length;i++)
			{
				Unit unit = units[i];
				int type = unit.getType();
				if(type == Unit.TABLE_UNIT ){
					TableUnit tUnit = (TableUnit) unit;
					data = tUnit.getTableData();
					for(int j=0;j<data.length;j++){
						contents = data[j];
						wr.writeRecord(contents);
					}
				}else{
					System.out.println("不被支持的数据类型:"+type);
				}
			}
			
			wr.close();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

}
