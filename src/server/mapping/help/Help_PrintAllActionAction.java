/**
 * ���� Created on 2008-1-28 by edmund
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
		buff.append("��Ϣ="+msg+"");
		buff.append("\nʵ��:");
		
		List allActionList = this.getActionContainer().getAllAction(msg);
		Iterator iterator = allActionList.iterator();
		IAction action ;
		while(iterator.hasNext()){
			action = (IAction)iterator.next();
			buff.append("\n����="+action.getClass().getName());
			buff.append(";��Ϊ��="+action.getName());
			buff.append(";��Ϣ="+action.getMsg());
			buff.append(";�˾�="+action.isFilter());
			buff.append(";ͬ��="+action.isSynchronized());
		}
		System.out.println(buff.toString());
		
		return true;
	}
}
