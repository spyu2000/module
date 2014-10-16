package server.flow.task;

import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import com.fleety.base.InfoContainer;
import com.fleety.util.pool.db.DbConnPool.DbHandle;
import com.fleety.util.pool.db.DbConnPool.StatementHandle;

import server.db.DbServer;
import server.flow.FlowOperator;
import server.flow.IFlow;
import server.flow.ISystemInfo;
import server.flow.inst.FlowInstance;
import server.flow.inst.FlowNode;
import server.flow.task.sql.IFlowTaskSqlCreator;
import server.flow.task.sql.TaskSql;

public class FlowTask extends InfoContainer implements IFlow{
	private FlowInstance flowInstance = null;
	private int taskId = 0;
	
	public FlowTask(FlowInstance flowInstance,Object key) throws Exception{
		this(flowInstance,0,key);
	}
	public FlowTask(FlowInstance flowInstance,int taskId,Object key) throws Exception{
		if(key != globalThreadLocal.get()){
			throw new Exception("Can't Create because key!");
		}
		this.taskId = taskId;
		this.flowInstance = flowInstance;
	}
	
	public FlowInstance getFlowInstance(){
		return this.flowInstance;
	}
	public int getFlowTaskId(){
		return this.taskId;
	}

	public boolean delete(){
		DbHandle conn = null;
		try{
			
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}finally{
			DbServer.getSingleInstance().releaseConn(conn);
		}
		return true;
	}
	public boolean save(){
		DbHandle conn = null;
		try{
			conn = DbServer.getSingleInstance().getConn();
			conn.setAutoCommit(false);
			
			if(this.taskId < 0){
				if(!this.flowInstance.triggerFlowTaskStatusWillChanged(this)){
					return false;
				}
			}
			
			IFlowTaskSqlCreator sqlCreator = this.flowInstance.getFlowTaskSqlCreator();
			TaskSql[] sqlArr = sqlCreator.createSaveSql(this.flowInstance, this);
			
			StatementHandle stmt = conn.createStatement();
			TaskSql sql;
			String sqlStr;
			if(sqlArr != null){
				for(int i=0;i<sqlArr.length;i++){
					sql = sqlArr[i];	
					sql.execute(conn);
					sqlStr = sql.getSql();
					if(sqlStr != null){
						stmt.execute(sqlStr);
					}
				}
			}
			
			if(this.taskId <= 0 && this.getInteger(FLOW_TASK_ID_FLAG) != null){
				this.taskId = this.getInteger(FLOW_TASK_ID_FLAG).intValue();

				this.updateFlowStatus(conn,this.flowInstance.getFirstFlowNode(),null);
			}
			if(this.taskId <= 0){
				throw new Exception("Can't Get Task ID!");
			}
			
			conn.commit();
		}catch(Exception e){
			e.printStackTrace();
			try{
				if(conn != null){
					conn.rollback();
				}
			}catch(Exception ee){}
			return false;
		}finally{
			DbServer.getSingleInstance().releaseConn(conn);
		}
		return true;
	}
	
	public boolean toNext(){
		return this.toNext(null,null);
	}
	public boolean toNext(String reason){
		return this.toNext(null,reason);
	}
	
	public boolean toNext(FlowNode flowNode,String reason){
		if(flowNode == null){
			flowNode = this.getNextNode();
		}
		if(flowNode == null){
			return false;
		}
		
		DbHandle conn = null;
		try{
			if(!this.flowInstance.triggerFlowTaskStatusWillChanged(this)){
				return false;
			}
			
			conn = DbServer.getSingleInstance().getConn();
			conn.setAutoCommit(false);
			IFlowTaskSqlCreator sqlCreator = this.flowInstance.getFlowTaskSqlCreator();
			TaskSql[] sqlArr = sqlCreator.createToNextSql(this.flowInstance, this, flowNode);
			
			StatementHandle stmt = conn.createStatement();
			TaskSql sql;
			String sqlStr;
			if(sqlArr != null){
				for(int i=0;i<sqlArr.length;i++){
					sql = sqlArr[i];	
					sql.execute(conn);
					sqlStr = sql.getSql();
					if(sqlStr != null){
						stmt.execute(sqlStr);
					}
				}
			}

			this.updateFlowStatus(conn,flowNode,reason);
			
			conn.commit();
		}catch(Exception e){
			e.printStackTrace();
			try{
				if(conn != null){
					conn.rollback();
				}
			}catch(Exception ee){}
			return false;
		}finally{
			DbServer.getSingleInstance().releaseConn(conn);
		}
		return true;
	}
	
	private FlowNode getNextNode(){
		DbHandle conn = null;
		try{
			conn = DbServer.getSingleInstance().getConn();
			int fNodeId;
			FlowNode fNode;
			StatementHandle stmt = conn.prepareStatement("select flow_node from "+this.flowInstance.getFlowEngine().getFlowInstanceStatusTableName(this.flowInstance)+" where flow_task_id=?");
			stmt.setInt(1, this.taskId);
			ResultSet sets = stmt.executeQuery();
			if(sets.next()){
				fNodeId = sets.getInt("flow_node");
				fNode = this.flowInstance.getFlowNode(fNodeId);
				if(fNode == null){
					return null;
				}
				FlowNode[] nArr = fNode.getNextNodeArr();
				if(nArr.length == 0){
					return null;
				}
				return nArr[0];
			}
			stmt.close();
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			DbServer.getSingleInstance().releaseConn(conn);
		}
		return null;
	}
	
	public boolean toBack(FlowNode flowNode,String reason){
		if(flowNode == null){
			flowNode = this.flowInstance.getFirstFlowNode();
		}
		
		DbHandle conn = null;
		try{
			if(!this.flowInstance.triggerFlowTaskStatusWillChanged(this)){
				return false;
			}
			
			conn = DbServer.getSingleInstance().getConn();
			conn.setAutoCommit(false);
			
			IFlowTaskSqlCreator sqlCreator = this.flowInstance.getFlowTaskSqlCreator();
			TaskSql[] sqlArr = sqlCreator.createToBackSql(this.flowInstance, this, flowNode);
			
			StatementHandle stmt = conn.createStatement();
			TaskSql sql;
			String sqlStr;
			if(sqlArr != null){
				for(int i=0;i<sqlArr.length;i++){
					sql = sqlArr[i];	
					sql.execute(conn);
					sqlStr = sql.getSql();
					if(sqlStr != null){
						stmt.execute(sqlStr);
					}
				}
			}

			this.updateFlowStatus(conn,flowNode,reason);
			
			conn.commit();
		}catch(Exception e){
			e.printStackTrace();
			try{
				if(conn != null){
					conn.rollback();
				}
			}catch(Exception ee){}
			return false;
		}finally{
			DbServer.getSingleInstance().releaseConn(conn);
		}
		return true;
	}
	
	public List getFlowHistory(){
		LinkedList list = new LinkedList();
		DbHandle conn = null;
		try{
			TaskHistory his;
			conn = DbServer.getSingleInstance().getConn();
			StatementHandle stmt = conn.prepareStatement("select * from "+this.flowInstance.getFlowEngine().getFlowInstanceHistoryStatusTableName(this.flowInstance)+" where flow_task_id=? order by main_id asc");
			stmt.setInt(1, this.taskId);
			ResultSet sets = stmt.executeQuery();
			while(sets.next()){
				his = new TaskHistory();
				
				his.setInfo(TaskHistory.ID_FLAG, new Integer(sets.getInt("main_id")));
				his.setInfo(TaskHistory.FLOW_TASK_ID_FLAG, new Integer(sets.getInt("flow_task_id")));
				his.setInfo(TaskHistory.FLOW_INSTANCE_ID_FLAG, new Integer(this.flowInstance.getFlowId()));
				his.setInfo(TaskHistory.FLOW_NODE_FLAG, new Integer(sets.getInt("flow_node")));
				his.setInfo(TaskHistory.FLOW_ARRIVE_TIME_FLAG, new Date(sets.getTimestamp("arrive_time").getTime()));
				his.setInfo(TaskHistory.FLOW_REASON_FLAG, sets.getString("reason"));
				his.setInfo(TaskHistory.FLOW_OPERATOR_FLAG, sets.getString("operator"));
				
				list.add(his);
			}
			stmt.close();
			
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			DbServer.getSingleInstance().releaseConn(conn);
		}
		return list;
	}

	private void updateFlowStatus(DbHandle conn,FlowNode fNode,String reason) throws Exception{
		if(fNode == null){
			return ;
		}
		StatementHandle stmt = conn.prepareStatement("select * from "+this.flowInstance.getFlowInstanceStatusTableName()+" where flow_task_id=?");
		stmt.setInt(1, this.taskId);
		ResultSet sets = stmt.executeQuery();
		boolean isExist = sets.next();
		stmt.close();

		long arriveTime = System.currentTimeMillis();
		
		if(isExist){
			stmt = conn.prepareStatement("update "+this.flowInstance.getFlowInstanceStatusTableName()+" set Flow_Node=?,Arrive_Time=?,Operator=?,reason=? where Flow_Task_ID=?");
		}else{
			stmt = conn.prepareStatement("insert into "+this.flowInstance.getFlowInstanceStatusTableName()+"(Flow_Node,Arrive_Time,Operator,reason,Flow_Task_ID) values(?,?,?,?,?)");
		}
		
		String account = this.getString(IFlow.OPERATOR_USER_ID_FLAG);
		ISystemInfo systemInfo = this.flowInstance.getFlowEngine().getSystemInfo();
		if(systemInfo != null){
			if(account != null){
				FlowOperator op = systemInfo.getFlowOperator(Integer.parseInt(account));
				if(op != null){
					account = op.getCode();
				}
			}
		}
		
		stmt.setInt(1, fNode.getId());
		stmt.setTimestamp(2, new Timestamp(arriveTime));
		stmt.setString(3, account);
		stmt.setString(4, reason);
		stmt.setInt(5, this.taskId);
		stmt.execute();
		stmt.close();
		
		stmt = conn.prepareStatement("insert into "+this.flowInstance.getFlowInstanceHistoryStatusTableName()+"(Main_ID,Flow_Task_ID,Flow_Node,Arrive_Time,reason,Operator) values(?,?,?,?,?,?)");
		stmt.setInt(1, (int)DbServer.getSingleInstance().getAvaliableId(conn, this.flowInstance.getFlowInstanceHistoryStatusTableName(), "main_id"));
		stmt.setInt(2, this.taskId);
		stmt.setInt(3, fNode.getId());
		stmt.setTimestamp(4, new Timestamp(arriveTime));
		stmt.setString(5, reason);
		stmt.setString(6, account);
		stmt.execute();
		stmt.close();
		
		this.flowInstance.triggerFlowTaskStatusChanged(this,fNode);
	}
}
