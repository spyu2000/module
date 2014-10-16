package server.flow;

import server.flow.task.sql.IFlowTaskSqlCreator;

public interface ISystemInfo extends IFlow{
	public FlowOperator[] getAllFlowOperator();
	public FlowOperator getFlowOperator(int opId);
	public IFlowTaskSqlCreator getSqlCreator(String creator);
}
