package server.flow.task.sql;

import com.fleety.base.InfoContainer;

import server.flow.IFlow;
import server.flow.inst.FlowInstance;
import server.flow.inst.FlowNode;
import server.flow.inst.QueryTaskInfoContainer;
import server.flow.task.FlowTask;

public abstract class IFlowTaskSqlCreator extends InfoContainer implements IFlow{
	public abstract TaskSql [] createQuerySql(FlowInstance flowObj, QueryTaskInfoContainer queryInfo);
	public abstract TaskSql [] createDeleteSql(FlowInstance flowObj,FlowTask task);
	public abstract TaskSql [] createSaveSql(FlowInstance flowObj,FlowTask task);
	public abstract TaskSql [] createToNextSql(FlowInstance flowObj,FlowTask task,FlowNode nextNode);
	public abstract TaskSql [] createToBackSql(FlowInstance flowObj,FlowTask task,FlowNode backNode);

}
