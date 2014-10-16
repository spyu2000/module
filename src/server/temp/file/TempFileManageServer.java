package server.temp.file;

import java.io.File;

import server.threadgroup.ThreadPoolGroupServer;

import com.fleety.base.GUIDCreator;
import com.fleety.base.GeneralConst;
import com.fleety.server.BasicServer;
import com.fleety.util.pool.timer.FleetyTimerTask;

public class TempFileManageServer extends BasicServer {
	private File tempDir = null;
	private long limitKeepDuration = GeneralConst.ONE_DAY_TIME;
	
	private static TempFileManageServer singleInstance = new TempFileManageServer();
	public static TempFileManageServer getSingleInstance(){
		return singleInstance;
	}
	
	private FleetyTimerTask task = null;
	public TempFileManageServer(){
		this.startServer();
	}
	
	public boolean startServer() {
		String tempStr = this.getStringPara("temp_dir");
		if(tempStr == null){
			this.tempDir = new File(".","temp_dir");
		}else{
			this.tempDir = new File(tempStr.trim(),"temp_dir");
		}
		this.tempDir.mkdirs();
		
		if(this.getIntegerPara("keep_duration")!=null){
			this.limitKeepDuration = this.getIntegerPara("keep_duration").intValue()*60000l;
			if(this.limitKeepDuration < 600000){
				this.limitKeepDuration = 600000;
			}
		}
		
		if(this.task == null){
			ThreadPoolGroupServer.getSingleInstance().createTimerPool(ThreadPoolGroupServer._QUICK_EXECUTE_OBJ_NAME_).schedule(this.task = new FleetyTimerTask(){
				public void run(){
					TempFileManageServer.this.clearTempFile();
				}
			}, 60000, this.limitKeepDuration/2);
		}
		
		this.isRunning = true;
		return true;
	}
	
	public void stopServer(){
		super.stopServer();
		if(this.task != null){
			this.task.cancel();
			this.task = null;
		}
	}

	private void clearTempFile(){
		System.out.println("Start Temp File Clear");
		try{
			File[] fArr = this.tempDir.listFiles();
			File f;
			for(int i=0;i<fArr.length;i++){
				f = fArr[i];
				if(f.isFile()){
					if(System.currentTimeMillis()- f.lastModified() >= this.limitKeepDuration){
						if(f.delete()){
							System.out.println("Delete:"+f.getName()+" Success!");
						}else{
							System.out.println("Delete:"+f.getName()+" Failure!");
						}
					}
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public File getTempFile(){
		return this.getTempFile(new GUIDCreator().createNewGuid(GUIDCreator.FORMAT_STRING));
	}
	
	public File getTempFile(String fileName){
		File f = new File(this.tempDir,fileName);
		
		return f;
	}
}
