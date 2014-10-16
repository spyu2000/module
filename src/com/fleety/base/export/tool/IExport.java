package com.fleety.base.export.tool;

import com.fleety.base.export.data.ExportDataDesc;

public interface IExport {
	public boolean exportFile(String filePath,ExportDataDesc desc);
}
