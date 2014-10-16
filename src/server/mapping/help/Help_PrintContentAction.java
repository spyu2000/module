/**
 * ¾øÃÜ Created on 2008-4-29 by edmund
 */
package server.mapping.help;

import com.fleety.base.InfoContainer;
import server.mapping.BasicAction;

public class Help_PrintContentAction extends BasicAction{
	private String content = null;
	public void init() throws Exception{
		super.init();
		this.content = this.getStringPara("content");
	}
	
	public boolean execute(InfoContainer infos) throws Exception{
		System.out.println(content);
		return true;
	}
}
