package server.text;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.regex.Pattern;

import com.fleety.server.BasicServer;

public class CharsetDetectServer extends BasicServer {
	public static final String DIR_FLAG = "scan_dir";
	public static final String INCLUDES_FLAG = "includes";
	public static final String EXCLUDES_FLAG = "excludes";
	
	private ArrayList includesPatternList = null;
	private ArrayList excludesPatternList = null;
	private File dir = null;
	private boolean isPrint = false;
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
		Boolean printObj = this.getBooleanPara("is_print");
		if(printObj != null){
			this.isPrint = printObj.booleanValue();
		}
		
		long t = System.currentTimeMillis();
		System.out.println("Start Detect File!");
		this.seq = 1;
		this.scanDir(this.dir);
		System.out.println("End Detect File!"+(System.currentTimeMillis() - t));
		
		return true;
	}
	
	private int seq = 1;
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
			if(this.isPrint){
				System.out.println("Not Include:"+f.getAbsolutePath());
			}
			return ;
		}
		if(this.excludesPatternList != null && this.isContain(this.excludesPatternList, name)){
			if(this.isPrint){
				System.out.println("Exclude:"+f.getAbsolutePath());
			}
			return ;
		}
		BufferedReader reader = null;
		try{
			reader = new BufferedReader(new FileReader(f));
			String str;
			
			StringBuffer strBuff = new StringBuffer(1024);
			strBuff.append(f.getAbsolutePath());
			strBuff.append("\tlines=");
			int line = 0;
			boolean isExist = false;
			while((str=reader.readLine()) != null){
				line ++;
				if(str.length() != str.getBytes().length){
					isExist = true;
					strBuff.append(line+" ");
				}
			}
			if(isExist){
				System.out.println(this.seq++ +":"+strBuff);
			}
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			if(reader != null){
				try{
					reader.close();
				}catch(Exception ee){}
			}
		}
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
	
	public static void main(String[] argv){
		CharsetDetectServer server = new CharsetDetectServer();
		server.addPara(DIR_FLAG, "E:/projects/shell_monitor/src");
		server.addPara(EXCLUDES_FLAG, "*.class,*.java,*.jar,*.bak,*.gz,opmon_linux");
		server.startServer();
	}
}
