package server.scan;

import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.regex.Pattern;

import com.fleety.base.GeneralConst;
import com.fleety.server.BasicServer;

public class FolderScanServer extends BasicServer {
	public static final String DIR_FLAG = "scan_dir";
	public static final String INCLUDES_FLAG = "includes";
	public static final String EXCLUDES_FLAG = "excludes";
	public static final String OUT_MODE_STR_FLAG = "out_str";

	private ArrayList includesPatternList = null;
	private ArrayList excludesPatternList = null;
	private File dir = null;
	private MessageFormat outputFormat = null;
	public boolean startServer() {
		String tempStr;
		
		tempStr = this.getStringPara(DIR_FLAG);
		if(tempStr != null){
			this.dir = new File(tempStr.trim());
		}
		tempStr = this.getStringPara(INCLUDES_FLAG);
		if(tempStr != null){
			this.includesPatternList = this.createPattern(tempStr);
		}
		tempStr = this.getStringPara(EXCLUDES_FLAG);
		if(tempStr != null){
			this.excludesPatternList = this.createPattern(tempStr);
		}

		tempStr = this.getStringPara(OUT_MODE_STR_FLAG);
		if(tempStr == null || tempStr.trim().length() == 0){
			return false;
		}
		this.outputFormat = new MessageFormat(tempStr);
		this.scanDir(this.dir);
		
		return true;
	}
	
	private void scanDir(File dir){
		if(dir.isFile()){
			this.scanDir(dir);
		}else if(dir.isDirectory()){
			File[] fArr = dir.listFiles();
			for(int i=0;i<fArr.length;i++){
				if(fArr[i].isDirectory()){
					this.scanDir(fArr[i]);
				}else if(fArr[i].isFile()){
					this.scanFile(fArr[i]);
				}
			}
		}
	}
	private void scanFile(File f){
		String name = f.getName();
		if(this.includesPatternList != null && !this.isContain(this.includesPatternList, name)){
			return ;
		}
		if(this.excludesPatternList != null && this.isContain(this.excludesPatternList, name)){
			return ;
		}

		System.out.println(this.outputFormat.format(new String[]{name,GeneralConst.YYYY_MM_DD_HH_MM_SS.format(f.lastModified())}));
	}

	private ArrayList createPattern(String pStr){
		if(pStr == null || pStr.trim().length() == 0){
			return null;
		}
		String[] arr = pStr.trim().split(",");
		ArrayList list = new ArrayList(arr.length);
		Pattern p ;
		for(int i=0;i<arr.length;i++){
			p = Pattern.compile("^"+arr[i]+"$");
			list.add(p);
		}
		return list;
	}
	
	private boolean isContain(ArrayList regList,String str){
		Pattern p;
		for(Iterator itr = regList.iterator();itr.hasNext();){
			p = (Pattern)itr.next();
			if(p.matcher(str).find()){
				return true;
			}
		}
		return false;
	}
	
	public static void main(String[] argv) throws Exception{
		FolderScanServer server = new FolderScanServer();
		server.addPara(DIR_FLAG, "E:/projects/module_interface/lib");
		server.addPara(INCLUDES_FLAG, "*.jar,*.zip");
		server.addPara(OUT_MODE_STR_FLAG, "./lib/{0}");
		server.startServer();
	}
}
