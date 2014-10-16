package server.help;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Date;

import server.threadgroup.ThreadPoolGroupServer;

import com.fleety.base.FleetyThread;
import com.fleety.base.GeneralConst;
import com.fleety.server.BasicServer;
import com.fleety.util.pool.timer.FleetyTimerTask;

public class ThreadStackPrintServer extends BasicServer {
	private String timerName = "thread_stack_print_timer";
	private long cycle = 3600000;
	private String outFileName = null;
	private long fileMaxSize = 16*1024*1024;
	
	public boolean startServer() {
		Integer obj = this.getIntegerPara("cycle");
		if(obj != null){
			this.cycle = obj.intValue()*60000;
		}
		if(this.cycle <= 0){
			this.cycle = 3600000;
		}
		obj = this.getIntegerPara("file_max_size");
		if(obj != null){
			this.fileMaxSize = obj.intValue()*1024*1024;
		}
		if(this.fileMaxSize <= 0){
			this.fileMaxSize = 16*1024*1024;
		}
		
		ThreadPoolGroupServer.getSingleInstance().createTimerPool(timerName).schedule(new FleetyTimerTask(){
			public void run(){
				ThreadStackPrintServer.this.printThreadStack();
			}
		}, this.cycle, this.cycle);
		
		String tempStr = this.getStringPara("out_file_name");
		if(tempStr != null && tempStr.trim().length() > 0){
			this.outFileName = tempStr.trim();
		}
		
		
		System.out.println("Print Para: Cycle(mi)="+(this.cycle/60000)+" fileName="+this.outFileName+" maxSize="+(this.fileMaxSize/1024/1024));
		
		return true;
	}

	public void stopServer(){
		super.stopServer();
	}
	
	public void printThreadStack(){
		PrintStream out = null;
		try{
			if(this.outFileName != null){
				File f = new File(this.outFileName);
				f.getParentFile().mkdirs();
				if(f.exists() && f.length() > this.fileMaxSize){
					out = new PrintStream(new FileOutputStream(f));
				}else{
					out = new PrintStream(new FileOutputStream(f,true));
				}
				
				out.print("\n\n/*****"+GeneralConst.YYYY_MM_DD_HH_MM_SS_SSS.format(new Date())+"********************************************************/\n");
			}
			
			FleetyThread.printThreadInfo(out);
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			if(out != null){
				try{
					out.close();
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		}
	}
}
