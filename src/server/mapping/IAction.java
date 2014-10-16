/**
 * ���� Created on 2007-12-8 by edmund
 */
package server.mapping;

import com.fleety.base.InfoContainer;

public interface IAction{
	//��Ϣ��ʶ
	public static final Object MSG_FLAG = new Object();
	//���͵�������Ϣ
	public static final Object SOURCE_DATA_FLAG = new Object();
	//�������ݶ�����Ϣ
	public static final Object OPTIONAL_FLAG = new Object();
	//�첽ִ������ı�ʶkey
	public static final Object TASK_FLAG = new Object();
	
	//�౾��Ĳ�����Ϣ
	public static final Object _MSG_FLAG = new Object();
	public static final Object _NAME_FLAG = new Object();
	public static final Object _FILTER_FLAG = new Object();
	public static final Object _SYNC_FLAG = new Object();
	public static final Object _ACTION_CONTAINER_FLAG = new Object();
	
	public boolean execute(InfoContainer infos) throws Exception;

	public void addPara(Object key,Object value);
	public Object getPara(Object key);
	
	public String getMsg();
	public String getName();
	public boolean isSynchronized();
	public boolean isFilter();
	public boolean isInclude(String msg);
	public ActionContainerServer getActionContainer();
	
	public void init() throws Exception;
}
