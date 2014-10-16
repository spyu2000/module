package server.flow.task;

import server.flow.IFlow;

public interface IFlowTaskFilter extends IFlow{
	public static final int CONTINUE_FLAG = 1; //���ص�ǰ���������
	public static final int IGNORE_FLAG = 2;   //���Ե�ǰ���������
	public static final int BREAK_FLAG = 3;    //���Ե�ǰ������ж�
		
	public int filterFlowTask(FlowTask flowTask);

}
