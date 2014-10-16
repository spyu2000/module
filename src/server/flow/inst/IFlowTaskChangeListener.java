package server.flow.inst;

import server.flow.IFlow;
import server.flow.task.FlowTask;

public interface IFlowTaskChangeListener extends IFlow{
	
	public boolean flowTaskWillChanged(FlowTask flowTask);
	
	public void flowTaskChanged(FlowTask flowTask,FlowNode fNode);

}
