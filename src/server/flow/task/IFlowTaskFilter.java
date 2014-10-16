package server.flow.task;

import server.flow.IFlow;

public interface IFlowTaskFilter extends IFlow{
	public static final int CONTINUE_FLAG = 1; //返回当前结果并继续
	public static final int IGNORE_FLAG = 2;   //忽略当前结果并继续
	public static final int BREAK_FLAG = 3;    //忽略当前结果并中断
		
	public int filterFlowTask(FlowTask flowTask);

}
