/**
 * ���� Created on 2007-11-8 by edmund
 */
package server.timer;

import java.io.File;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import server.threadgroup.ThreadPoolGroupServer;

import com.fleety.base.Util;
import com.fleety.server.BasicServer;
import com.fleety.util.pool.timer.FleetyTimerTask;
import com.fleety.util.pool.timer.TimerPool;

/**
 * �౾����Ϊ�����ṩ��ʱ���񣬶����ڵ�cfgTimer����Ϊ�����ļ������õĶ�ʱ����
 * ��Ϊ���߿��ܻᱻ�ؽ���������Ҫ�ͱ��������ʱ���ֿ���
 * @title
 * @description
 * @version      1.0
 * @author       edmund
 *
 */
public class DefaultTimerServer extends BasicServer{
	private static DefaultTimerServer singleInstance = null;
	public static DefaultTimerServer getSingleInstance(){
		if(singleInstance == null){
			synchronized(DefaultTimerServer.class){
				if(singleInstance == null){
					singleInstance = new DefaultTimerServer();
				}
			}
		}
		return singleInstance;
	}
	
	private DefaultTimerServer(){
		
	}
	
	private String timerName = DefaultTimerServer.class.getName()+"["+this.hashCode()+"]";
	private String systemTimerName = "global_default_timer_system["+this.hashCode()+"]";
	private TimerPool cfgTimer = null,systemTimer = null;
	private Timer timer = null;
	public boolean startServer(){
		this.timer = new Timer();
		try{
			this.init();
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}
		this.timer.schedule(new TimerDetectTask(), LOOP_DETECT_INTERVAL, LOOP_DETECT_INTERVAL);
		
		this.systemTimer = ThreadPoolGroupServer.getSingleInstance().createTimerPool(this.systemTimerName);

		this.isRunning = true;
		return true;
	}
	
	public void stopServer(){
		if(this.timer != null){
			this.timer.cancel();
		}
		this.timer = null;
		
		ThreadPoolGroupServer.getSingleInstance().destroyTimerPool(this.timerName);
		this.cfgTimer = null;
		ThreadPoolGroupServer.getSingleInstance().destroyTimerPool(this.systemTimerName);
		this.systemTimer = null;
		
		this.isRunning = false;
	}
	
	/**
	 * ����ʹ��public void schedule(FleetyTimerTask task,long delay,long period)
	 * @param task
	 * @param delay
	 * @param period
	 */
	@Deprecated
	public void schedule(TimerTask task,long delay,long period){
		if(!this.isRunning()){
			return ;
		}
		if(this.timer != null){
			this.timer.schedule(task, delay, period);
		}
	}
	/**
	 * ����ʹ��public void scheduleAtFixedRate(FleetyTimerTask task,long delay,long period)
	 * @param task
	 * @param delay
	 * @param period
	 */
	@Deprecated
	public void scheduleAtFixedRate(TimerTask task,long delay,long period){
		if(!this.isRunning()){
			return ;
		}
		if(this.timer != null){
			this.timer.scheduleAtFixedRate(task, delay, period);
		}
	}

	public void schedule(FleetyTimerTask task,long delay,long period){
		if(!this.isRunning()){
			return ;
		}
		if(this.systemTimer != null){
			this.systemTimer.schedule(task, delay, period);
		}
	}
	public void scheduleAtFixedRate(FleetyTimerTask task,long delay,long period){
		if(!this.isRunning()){
			return ;
		}
		if(this.systemTimer != null){
			this.systemTimer.scheduleAtFixedRate(task, delay, period);
		}
	}
	
	private static final long LOOP_DETECT_INTERVAL = 5*60*1000l;
	private long lastLoadTime = 0;
	/**
	 * ��ʼ��������Ҫ��ʱִ�е�����
	 */
	private void init(){
		File f = new File(this.getPara("timer_path").toString());
		if(!f.exists() || f.lastModified() == this.lastLoadTime){
			return ;
		}
		this.lastLoadTime = f.lastModified();
		
		
		int timerNum = 2;
		Integer timerNumObj = this.getIntegerPara("timer_num");
		if(timerNumObj != null){
			timerNum = timerNumObj.intValue();
		}
		if(timerNum <= 0){
			timerNum = 2;
		}
		
		//ֹͣԭ���Ķ�ʱ��
		ThreadPoolGroupServer.getSingleInstance().destroyTimerPool(timerName);
		//�����µĶ�ʱ�������������������
		this.cfgTimer = ThreadPoolGroupServer.getSingleInstance().createTimerPool(timerName,timerNum);

		System.out.println("��ʼ����ʱ�������ļ�!");
		this.loadAndAddTimerTask(f);
	}
	
	private void loadAndAddTimerTask(File f){
		try{
			//���������ļ�
			DocumentBuilderFactory domfac=DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = domfac.newDocumentBuilder();
			Document document = builder.parse(f);
			
			Element root = document.getDocumentElement();
			
			NodeList allTaskNodeList = root.getElementsByTagName("task");
			int taskNum = allTaskNodeList.getLength();
			for(int i=0;i<taskNum;i++){
				this.loadTask(allTaskNodeList.item(i));
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	private static final String month_cycle = "MM";
	private static final String HOUR_CYCLE = "HH";
	private static final String DAY_CYCLE = "dd";

	private static final long HOUR_MILSEC = 60*60*1000l;
	private static final long DAY_MILSEC = 24l*60*60*1000l;
	
	private void loadTask(Node taskNode) throws Exception{
		if(taskNode == null){
			return ;
		}
		Node temp;
		String name = Util.getNodeAttr(taskNode, "name");
		
		temp = Util.getSingleElementByTagName(taskNode, "enable");
		String enable = Util.getNodeText(temp);
		if(enable != null && enable.trim().equalsIgnoreCase("false")){
			System.out.println("��ʱ����"+name+"��������!");
			return ;
		}
		
		temp = Util.getSingleElementByTagName(taskNode, "cycle");
		String cycle = Util.getNodeText(temp);
		if(cycle == null || cycle.trim().length() == 0){
			return ;
		}
		
		temp = Util.getSingleElementByTagName(taskNode, "start-time");
		String startTime = Util.getNodeText(temp);
		if(startTime == null || startTime.trim().length() == 0){
			return ;
		}
		
		temp = Util.getSingleElementByTagName(taskNode, "class-name");
		String className = Util.getNodeText(temp);
		if(className == null || className.trim().length() == 0){
			return ;
		}
		
		Object obj;
		obj = Class.forName(className.trim()).newInstance();
		if(!(obj instanceof ATimerTask)){
			return ;
		}
		((ATimerTask)obj).setName(name);

		Node[] paraArr = Util.getElementsByTagName(taskNode, "para");
		Node paraNode;
		int paraNum = paraArr.length;
		for(int j=0;j<paraNum;j++){
			paraNode = paraArr[j];
			((ATimerTask)obj).addPara(Util.getNodeAttr(paraNode, "key"), Util.getNodeAttr(paraNode, "value"));
		}
		
		((ATimerTask)obj).init();

		Calendar calendar = Calendar.getInstance();
		long delay;
		if(cycle.endsWith(HOUR_CYCLE)){
			String[] timeStrArr = startTime.split(":");
			if(timeStrArr.length != 2){
				return ;
			}
			delay = Integer.parseInt(timeStrArr[0]) * 60 * 1000l
					+ Integer.parseInt(timeStrArr[1]) * 1000l
					- calendar.get(Calendar.MINUTE) * 60 * 1000l
					- calendar.get(Calendar.SECOND) * 1000l;
			if (delay < 0){
				delay += HOUR_MILSEC;
			}
			int num = 1;
			try{
				String numStr = cycle.substring(0,cycle.length()-2);
				if(numStr.length() > 0){
					num = Integer.parseInt(numStr);
				}
			}catch(Exception e){
				e.printStackTrace();
			}
			this.cfgTimer.schedule((ATimerTask)obj, delay, num*HOUR_MILSEC);
			System.out.println("["+((ATimerTask)obj).getName()+"]������!�ӳ�="+delay+";����="+(num*HOUR_MILSEC));
		}else if(cycle.endsWith(DAY_CYCLE)){
			String[] timeStrArr = startTime.split(":");
			if(timeStrArr.length != 3){
				return ;
			}
			delay = Integer.parseInt(timeStrArr[0]) * 60l * 60 * 1000l
					+ Integer.parseInt(timeStrArr[1]) * 60 * 1000l
					+ Integer.parseInt(timeStrArr[2]) * 1000l
					- calendar.get(Calendar.HOUR_OF_DAY) * 60l * 60 * 1000l
					- calendar.get(Calendar.MINUTE) * 60 * 1000l
					- calendar.get(Calendar.SECOND) * 1000l;
			if (delay < 0){
				delay += DAY_MILSEC;
			}
			int num = 1;
			try{
				String numStr = cycle.substring(0,cycle.length()-2);
				if(numStr.length() > 0){
					num = Integer.parseInt(numStr);
				}
			}catch(Exception e){
				e.printStackTrace();
			}
			
			this.cfgTimer.schedule((ATimerTask)obj, delay, num*DAY_MILSEC);
			System.out.println("["+((ATimerTask)obj).getName()+"]������!�ӳ�="+delay+";����="+(num*DAY_MILSEC));
		}else if(cycle.endsWith(month_cycle)){
			String[] timeStrArr = startTime.split("/");
			calendar.set(Integer.parseInt(timeStrArr[0]),Integer.parseInt(timeStrArr[1])-1,Integer.parseInt(timeStrArr[2]),0,0,0);
			long starttime = calendar.getTimeInMillis();
			long nowtime = System.currentTimeMillis();
			delay = starttime-nowtime;
			if(delay<0){//�����ǰʱ����ڼƻ�ʱ��
				if(delay>-1000*3600*24){//��������ʱ����Ǽƻ���ʱ�䵱�죬��ô��ʱ10���ִ�мƻ�
					delay=10000;
				}else{//�������ڵ�ʱ���Ǽƻ�ʱ��һ��֮����ôÿ���ִ��һ�μƻ����ڼƻ��л��ж��Ƿ��Ѿ����ڱ��ݵı�
					delay = DAY_MILSEC;
				}
			}
			//����ƻ�ʱ�仹δ������ôdelay���ƻ�ʱ��ȥִ�мƻ���֮��ÿ���ظ�ִ�мƻ�ȥ�ж��Ƿ��Ѿ����ڵ��±��ݵı�
			this.cfgTimer.schedule(new ATask((ATimerTask)obj), delay);
		}
	}
	
	private class TimerDetectTask extends FleetyTimerTask{
		public void run(){
			DefaultTimerServer.this.init();
		}
	}
	
	public static void main(String[] argv){
		DefaultTimerServer.getSingleInstance().startServer();
	}
	
	private class ATask extends FleetyTimerTask{
		private ATimerTask task;
		
		public ATask(ATimerTask task){
			this.task = task;
		}
		public void run(){
			try{
				this.task.run();
			}catch(Exception e){
				e.printStackTrace();
			}
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.MONTH, 1);
			cfgTimer.schedule(new ATask(this.task), cal.getTime());
		}
	}
}
