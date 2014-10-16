/**
 * 绝密 Created on 2008-1-28 by edmund
 */
package server.mapping.help;

import java.util.Iterator;
import java.util.List;
import server.mapping.BasicAction;
import server.mapping.IAction;
import com.fleety.base.InfoContainer;

public class Help_PrintAllActionAction extends BasicAction{
	private static Help_PrintAllActionAction singleInstance = null;
	public static Help_PrintAllActionAction getSingleInstance(){
		if(singleInstance == null){
			synchronized(Help_PrintAllActionAction.class){
				if(singleInstance == null){
					singleInstance = new Help_PrintAllActionAction();
				}
			}
		}
		return singleInstance;
	}
	
	public boolean execute(InfoContainer infos) throws Exception{
		String msg = infos.getString(IAction.MSG_FLAG);

		if(!this.isInclude(msg)){
			return true;
		}
		
		StringBuffer buff = new StringBuffer(512);
		buff.append("消息="+msg+"");
		buff.append("\n实例:");
		
		List allActionList = this.getActionContainer().getAllAction(msg);
		Iterator iterator = allActionList.iterator();
		IAction action ;
		while(iterator.hasNext()){
			action = (IAction)iterator.next();
			buff.append("\n类名="+action.getClass().getName());
			buff.append(";行为名="+action.getName());
			buff.append(";消息="+action.getMsg());
			buff.append(";滤镜="+action.isFilter());
			buff.append(";同步="+action.isSynchronized());
		}
		System.out.println(buff.toString());
		
		return true;
	}
}
