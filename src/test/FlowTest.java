package test;

import java.util.Iterator;
import java.util.List;

import server.db.DbServer;
import server.flow.FlowEngineServer;
import server.flow.FlowOperator;
import server.flow.IFlow;
import server.flow.ISystemInfo;
import server.flow.inst.FlowInstance;
import server.flow.inst.FlowNode;
import server.flow.inst.IFlowTaskChangeListener;
import server.flow.inst.QueryTaskInfoContainer;
import server.flow.task.FlowTask;
import server.flow.task.sql.IFlowTaskSqlCreator;
import server.flow.task.sql.TaskSql;

public class FlowTest {

	public static void main(String[] args) throws Exception{
		DbServer.getSingleInstance().addPara(DbServer.DRIVER_FLAG, "oracle.jdbc.driver.OracleDriver");
		DbServer.getSingleInstance().addPara(DbServer.DB_URL_FLAG, "jdbc:oracle:thin:@192.168.0.31:1521:demo");
		DbServer.getSingleInstance().addPara(DbServer.DB_USER_FLAG, "gps_test");
		DbServer.getSingleInstance().addPara(DbServer.DB_PWD_FLAG, "gps_test");
		DbServer.getSingleInstance().startServer();
		
		FlowEngineServer.getSingleInstance().addPara("system_info_cls", SystemInfo.class.getName());
		FlowEngineServer.getSingleInstance().startServer();
		
		FlowEngineServer.getSingleInstance().addFlowTaskChangeListener(new IFlowTaskChangeListener(){
			public boolean flowTaskWillChanged(FlowTask flowTask){
				return true;
			}
			
			public void flowTaskChanged(FlowTask flowTask,FlowNode fNode){
				System.out.println("Engine:"+flowTask.getFlowTaskId()+" Change To "+fNode.getCode());
			}
		});
		
		new FlowTest().testAddFlowInstance();
//		new FlowTest().testAddFlowTask();
		new FlowTest().testAddFlowTaskUpdate();
		new FlowTest().testFlowTaskHistoryQuery();
	}

	private void testAddFlowInstance(){
		FlowInstance inst = FlowEngineServer.getSingleInstance().getEmptyFlowInstance();
		inst.setFlowCode("HR_2");
		inst.setFlowName("员工信息管理");
		
		FlowNode node1 = inst.getEmptyFlowNode();
		node1.setCode("10");
		node1.setName("初始状态");
		node1.setNodeType(FlowNode.START_NODE);
		node1.setAlarmDuration(60*24);
		node1.setNextActionOperator("HR_1_SH_1");
		node1.setAllNotifiers(new String[]{"1","2"});
		node1.setNotifyMethod(FlowNode.NOTIFY_METHOD_MAIL|FlowNode.NOTIFY_METHOD_SMS);
		inst.addFlowNode(node1);
		FlowNode node2 = inst.getEmptyFlowNode();
		node2.setCode("20");
		node2.setName("审核状态1");
		node2.setNodeType(FlowNode.PENDING_NODE);
		node2.setAlarmDuration(60*24);
		node2.setNextActionOperator("HR_1_SH_2");
		node2.setAllNotifiers(new String[]{"1","2","3"});
		node2.setNotifyMethod(FlowNode.NOTIFY_METHOD_MAIL|FlowNode.NOTIFY_METHOD_SMS);
		inst.addFlowNode(node2);
		FlowNode node3 = inst.getEmptyFlowNode();
		node3.setCode("30");
		node3.setName("审核状态2");
		node3.setNodeType(FlowNode.PENDING_NODE);
		node3.setAlarmDuration(60*24);
		node3.setNextActionOperator("HR_1_SH_3");
		node3.setAllNotifiers(new String[]{"2","3"});
		node3.setNotifyMethod(FlowNode.NOTIFY_METHOD_MAIL|FlowNode.NOTIFY_METHOD_SMS);
		inst.addFlowNode(node3);
		FlowNode node4 = inst.getEmptyFlowNode();
		node4.setCode("40");
		node4.setName("结束状态");
		node4.setNodeType(FlowNode.END_NODE);
		node4.setAllNotifiers(new String[]{"1","2","3","4"});
		node4.setNotifyMethod(FlowNode.NOTIFY_METHOD_MAIL|FlowNode.NOTIFY_METHOD_SMS);
		inst.addFlowNode(node4);
		
		node1.addNextNode(node2);
		node2.addNextNode(node3);
		node3.addNextNode(node4);
		
		inst.setInfo(IFlow.OPERATOR_USER_ID_FLAG, new Integer(1));
		System.out.println("Save Instance:" + inst.save());
		

		FlowEngineServer.getSingleInstance().getFlowInstance(1).addFlowTaskChangeListener(new IFlowTaskChangeListener(){
			public boolean flowTaskWillChanged(FlowTask flowTask){
				return true;
			}
			
			public void flowTaskChanged(FlowTask flowTask,FlowNode fNode){
				System.out.println("Inst:"+flowTask.getFlowTaskId()+" Change To "+fNode.getCode());
			}
		});
	}
	
	private void testAddFlowTask(){
		FlowInstance inst = FlowEngineServer.getSingleInstance().getFlowInstance("HR_2");
		FlowTask task = inst.getEmptyFlowTask();
		task.setInfo(IFlow.OPERATOR_USER_ID_FLAG, new Integer(1));
		System.out.println("Save Inst Task:"+task.save());
	}
	
	private void testAddFlowTaskUpdate(){
		FlowInstance inst = FlowEngineServer.getSingleInstance().getFlowInstance("HR_2");
		FlowTask task = inst.queryFlowTask(2);
		task.setInfo(IFlow.OPERATOR_USER_ID_FLAG, new Integer(1));
		System.out.println(task.toNext("没问题"));
	}
	
	private void testFlowTaskHistoryQuery(){
		FlowInstance inst = FlowEngineServer.getSingleInstance().getFlowInstance("HR_2");
		FlowTask task = inst.queryFlowTask(2);
		
		List list = task.getFlowHistory();
		for(Iterator itr = list.iterator();itr.hasNext();){
			System.out.println(itr.next());
		}
	}
	
	public static class SystemInfo implements ISystemInfo{
		public FlowOperator[] getAllFlowOperator(){
			return new FlowOperator[0];
		}
		public FlowOperator getFlowOperator(int opId){
			return new FlowOperator(1,"Account","Name");
		}
		public IFlowTaskSqlCreator getSqlCreator(String creator){
			return new TestSqlCreator();
		}
	}
	
	public static class TestSqlCreator extends IFlowTaskSqlCreator{
		public TaskSql [] createQuerySql(FlowInstance flowObj, QueryTaskInfoContainer queryInfo){
			return new TaskSql[]{new TaskSql("select * from "+flowObj.getFlowEngine().getFlowInstanceStatusTableName(flowObj)+" where flow_task_id="+queryInfo.getString(QueryTaskInfoContainer.TASK_ID_FLAG))};
		}
		public TaskSql [] createDeleteSql(FlowInstance flowObj,FlowTask task){
			
			return null;
		}
		public TaskSql [] createSaveSql(FlowInstance flowObj,FlowTask task){
			task.setInfo(IFlow.FLOW_TASK_ID_FLAG, new Integer(2));
			return null;
		}
		public TaskSql [] createToNextSql(FlowInstance flowObj,FlowTask task,FlowNode nextNode){
			return null;
		}
		public TaskSql [] createToBackSql(FlowInstance flowObj,FlowTask task,FlowNode backNode){
			return null;
		}
	}
}
