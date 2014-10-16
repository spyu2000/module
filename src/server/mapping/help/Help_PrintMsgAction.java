/**
 * 绝密 Created on 2008-1-28 by edmund
 */
package server.mapping.help;

import server.mapping.BasicAction;
import server.mapping.IAction;
import com.fleety.base.InfoContainer;

public class Help_PrintMsgAction extends BasicAction{
	private static Help_PrintMsgAction singleInstance = null;
	public static Help_PrintMsgAction getSingleInstance(){
		if(singleInstance == null){
			synchronized(Help_PrintMsgAction.class){
				if(singleInstance == null){
					singleInstance = new Help_PrintMsgAction();
				}
			}
		}
		return singleInstance;
	}
	
	protected String headStr = "到达消息";
	public void init() throws Exception{
		super.init();
		
		String temp = this.getStringPara("head_str");
		if(temp != null){
			this.headStr = temp.trim();
		}

		String tempStr = this.getStringPara("is_print");
		if(tempStr != null && tempStr.trim().equalsIgnoreCase("true")){
			this.isPrint = true;
		}else{
			this.isPrint = false;
		}
	}
	
	public boolean execute(InfoContainer infos) throws Exception{
		String msg = infos.getString(IAction.MSG_FLAG);
		if(!this.isInclude(msg)){
			return true;
		}
		if(isPrint())
		{
			System.out.println(headStr+":"+msg);
		}
		return true;
	}

	private boolean isPrint = false;
	public boolean isPrint(){
		return this.isPrint;
	}
}
