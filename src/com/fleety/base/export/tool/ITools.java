package com.fleety.base.export.tool;

import com.fleety.base.export.data.ExportDataDesc;

public interface ITools {
	public void registerTool(IExport exp,String format);
	public void removeTool(String format);
	public boolean canSupport(String format);
	public boolean export(String filePath,ExportDataDesc desc,String format);
}
