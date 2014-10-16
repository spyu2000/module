/**
 * ¾øÃÜ Created on 2008-1-28 by edmund
 */
package server.mapping.help;

import server.mapping.BasicAction;
import com.fleety.base.InfoContainer;


public class Help_FalseFilter extends BasicAction
{
	private static Help_FalseFilter singleInstance = null;
	public static Help_FalseFilter getSingleInstance(){
		if(singleInstance == null){
			synchronized(Help_FalseFilter.class){
				if(singleInstance == null){
					singleInstance = new Help_FalseFilter();
				}
			}
		}
		return singleInstance;
	}
	
	public boolean execute(InfoContainer infos) throws Exception{
		String msg = infos.getString(MSG_FLAG);
		if(!this.isInclude(msg)){
			return true;
		}
		
		return false;
	}
}
