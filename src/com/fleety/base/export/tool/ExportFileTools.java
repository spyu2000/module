package com.fleety.base.export.tool;

import java.util.HashMap;

import com.fleety.base.export.data.ExportDataDesc;

public class ExportFileTools implements ITools {

	private HashMap tools = new HashMap();
	
	public ExportFileTools(){
		try {
			tools.put("pdf", ExportPDF.class.newInstance());
			tools.put("xls", ExportEXCEL.class.newInstance());
			tools.put("doc", ExportWORD.class.newInstance());
			tools.put("csv", ExportCSV.class.newInstance());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public boolean canSupport(String format) {
		if(tools.containsKey(format)){
			return true;
		}
		return false;
	}
	public void registerTool(IExport exp, String format) {
		try {
			if(!tools.containsKey(format)){
				tools.put(format, exp.getClass().newInstance());
			}else{
				System.out.println("该文件格式已经被注册!");
			}
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}
	public void removeTool(String format) {
		if(tools.containsKey(format)){
			tools.remove(format);
		}
	}
	
	public boolean export(String filePath, ExportDataDesc desc, String format) {
		if(!canSupport(format)){
			System.out.println("不被支持的文件格式:"+format+"，请先注册!");
			return false;
		}else{
			IExport exp = (IExport) tools.get(format);
			return exp.exportFile(filePath, desc);
		}
	}
}
