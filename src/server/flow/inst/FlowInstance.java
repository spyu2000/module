package server.flow.inst;

import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import com.fleety.base.InfoContainer;
import com.fleety.base.StrFilter;
import com.fleety.util.pool.db.DbConnPool.DbHandle;
import com.fleety.util.pool.db.DbConnPool.StatementHandle;

import server.db.DbServer;
import server.flow.FlowEngineServer;
import server.flow.IFlow;
import server.flow.task.FlowTask;
import server.flow.task.IFlowTaskFilter;
import server.flow.task.sql.IFlowTaskSqlCreator;
import server.flow.task.sql.SqlParaInfo;
import server.flow.task.sql.TaskSql;

public class FlowInstance extends InfoContainer implements IFlow{
	private static final Object SECURITY_KEY = new Object();
	
	private FlowEngineServer engine = null;
	private ArrayList flowNodeList = null;
	private ArrayList flowTaskListenerList = null;
	private IFlowTaskSqlCreator sqlCreator = null;
	
	public FlowInstance(FlowEngineServer engine,Object key) throws Exception{
		this(engine,0,key);
	}
	public FlowInstance(FlowEngineServer engine,int flowId,Object key) throws Exception{
		if(key != globalThreadLocal.get()){
			throw new Exception("Can't Create because key!");
		}
		globalThreadLocal.remove();
		
		this.engine = engine;
		this.flowId = flowId;
		this.flowTaskListenerList = new ArrayList(4);
		this.flowNodeList = new ArrayList(4);
	}
	
	private int flowId = 0;
	private String flowCode = null;
	private String flowName = null;
	
	public FlowEngineServer getFlowEngine(){
		return this.engine;
	}
	
	public int getFlowId(){
		return this.flowId;
	}
	public String getFlowCode(){
		return this.flowCode;
	}
	public String getFlowName(){
		return this.flowName;
	}
	
	public void setFlowCode(String flowCode){
		this.flowCode = flowCode;
	}
	public void setFlowName(String flowName){
		this.flowName = flowName;
	}
	
	public void addFlowNode(FlowNode fNode){
		this.flowNodeList.add(fNode);
	}
	public void clearFlowNode(){
		this.flowNodeList.clear();
	}
	public void initNextFlowNode(HashMap id2NextMapping){
		String nextIdStr;
		String[] arr;
		FlowNode fNode,tNode;
		for(Iterator itr = this.flowNodeList.iterator();itr.hasNext();){
			fNode = (FlowNode)itr.next();
			nextIdStr = (String)id2NextMapping.get(new Integer(fNode.getId()));
			if(nextIdStr != null && nextIdStr.trim().length() > 0){
				arr = nextIdStr.trim().split(",");
				for(int i=0;i<arr.length;i++){
					tNode = this.getFlowNode(Integer.parseInt(arr[i]));
					if(tNode != null){
						fNode.addNextNode(tNode);
					}
				}
			}
		}
	}
	
	public boolean save(){
		if(!StrFilter.hasValue(this.flowCode)){
			return false;
		}
		if(!StrFilter.hasValue(this.flowName)){
			return false;
		}
		String userIdStr = this.getString(OPERATOR_USER_ID_FLAG);
		if(userIdStr == null || userIdStr.trim().length() == 0){
			return false;
		}
		DbHandle conn = DbServer.getSingleInstance().getConn();
		try{
			conn.setAutoCommit(false);
			StatementHandle stmt = null;
			
			boolean isUpdate = (this.flowId > 0);
			if(isUpdate){
				stmt = conn.prepareStatement("select * from "+FlowEngineServer.FLOW_INSTANCE_TABLE_NAME+" where id = ?");
				stmt.setInt(1, this.flowId);
				ResultSet sets = stmt.executeQuery();
				if(!sets.next()){
					isUpdate = false;
				}
				stmt.close();
			}
			
			stmt = conn.prepareStatement("select * from "+FlowEngineServer.FLOW_INSTANCE_TABLE_NAME+" where code = ?");
			stmt.setString(1, this.flowCode);
			ResultSet sets = stmt.executeQuery();
			if(sets.next()){
				if(sets.getInt("id") != this.flowId){
					return false;
				}
			}
			stmt.close();
			
			if(isUpdate){
				stmt = conn.prepareStatement("update "+FlowEngineServer.FLOW_INSTANCE_TABLE_NAME+" set name=?,code=?,update_time=? where id=?");

				stmt.setString(1, this.flowName);
				stmt.setString(2, this.flowCode);
				stmt.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
				stmt.setInt(4, this.flowId);
				stmt.execute();
				stmt.close();
			}else{
				int id = (int)DbServer.getSingleInstance().getAvaliableId(conn, FlowEngineServer.FLOW_INSTANCE_TABLE_NAME, "id");
				stmt = conn.prepareStatement("insert into "+FlowEngineServer.FLOW_INSTANCE_TABLE_NAME+"(id,name,code,creator,create_time) values(?,?,?,?,?)");
				stmt.setInt(1, id);
				stmt.setString(2, this.flowName);
				stmt.setString(3, this.flowCode);
				stmt.setInt(4, this.getInteger(IFlow.OPERATOR_USER_ID_FLAG).intValue());
				stmt.setTimestamp(5, new Timestamp(System.currentTimeMillis()));
				stmt.execute();
				stmt.close();

				this.flowId = id;
			}
			
			stmt = conn.prepareStatement("delete from "+ FlowEngineServer.FLOW_INSTANCE_NODE_TABLE_NAME+" where flow_id=?");
			stmt.setInt(1, this.flowId);
			stmt.execute();
			
			FlowNode fNode;
			for(int i=0;i<this.flowNodeList.size();i++){
				fNode = (FlowNode)this.flowNodeList.get(i);
				fNode.save(conn);
			}
			for(int i=0;i<this.flowNodeList.size();i++){
				fNode = (FlowNode)this.flowNodeList.get(i);
				fNode.updateNextNode(conn);
			}
			
			
			this.createFlowInstanceTable(conn);
			
			conn.commit();
			
			this.engine.addFlowInstance(this);
		}catch(Exception e){
			try{
				if(conn != null){
					conn.rollback();
				}
			}catch(Exception ee){}
			
			e.printStackTrace();
			return false;
		}finally{
			DbServer.getSingleInstance().releaseConn(conn);
		}
		return true;
	}
	
	private void createFlowInstanceTable(DbHandle conn) throws Exception{
		StatementHandle stmt = conn.createStatement();
		ResultSet sets = null;
		
		String tTableName = this.getFlowInstanceStatusTableName();
		String sql = null;
		sets = stmt.executeQuery("select * from user_tables where table_name='"+tTableName+"'");
		if(!sets.next()){
			sql = "create table "+tTableName;
			sql += "(";
			sql += "Flow_Task_ID NUMBER(10) not null,";
			sql += "Flow_Node NUMBER(10) not null,";
			sql += "Arrive_Time DATE not null,";
			sql += "REASON VARCHAR2(1024),";
			sql += "Operator VARCHAR2(64) not null";
			sql += ")";
			stmt.execute(sql);
			
			sql = "alter table "+tTableName+" add constraint P_FLOW_TASK_"+this.flowId+" primary key (Flow_Task_ID)";
			stmt.execute(sql);
			
			sql = "create index P_FLOW_TASK_"+this.flowId+"_ind1 on "+tTableName+"(Arrive_Time)";
			stmt.execute(sql);
		}
		sets.close();
		
		tTableName = this.getFlowInstanceHistoryStatusTableName();
		sets = stmt.executeQuery("select * from user_tables where table_name='"+tTableName+"'");
		if(!sets.next()){
			sql = "create table "+tTableName;
			sql += "(";
			sql += "Main_ID NUMBER(10) not null,";
			sql += "Flow_Task_ID NUMBER(10) not null,";
			sql += "Flow_Node NUMBER(10) not null,";
			sql += "Arrive_Time DATE not null,";
			sql += "REASON VARCHAR2(1024),";
			sql += "Operator VARCHAR2(64) not null";
			sql += ")";
			stmt.execute(sql);
			
			sql = "alter table "+tTableName+" add constraint P_FLOW_TASK_HIS_"+this.flowId+" primary key (Main_ID)";
			stmt.execute(sql);

			sql = "create index P_FLOW_TASK_HIS_"+this.flowId+"_ind1 on "+tTableName+"(Flow_Task_ID)";
			stmt.execute(sql);
			sql = "create index P_FLOW_TASK_HIS_"+this.flowId+"_ind2 on "+tTableName+"(Arrive_Time)";
			stmt.execute(sql);
		}
		sets.close();
	}
	private void deleteFlowInstanceTable(DbHandle conn) throws Exception{
		StatementHandle stmt = conn.createStatement();
		try{
			stmt.execute("drop table " + this.getFlowInstanceStatusTableName());
		}catch(Exception e){
			e.printStackTrace();
		}
		try{
			stmt.execute("drop table " + this.getFlowInstanceHistoryStatusTableName());
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public boolean delete(){
		DbHandle conn = DbServer.getSingleInstance().getConn();
		try{
			StatementHandle stmt = conn.prepareStatement("update "+FlowEngineServer.FLOW_INSTANCE_TABLE_NAME+" set is_used=1 where id = ?");
			stmt.setInt(1, this.flowId);
			stmt.execute();
			stmt.close();

			this.engine.removeFlowInstance(this.flowCode);
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}finally{
			DbServer.getSingleInstance().releaseConn(conn);
		}
		return true;
	}
	
	public String getFlowInstanceStatusTableName(){
		return this.engine.getFlowInstanceStatusTableName(this);
	}
	public String getFlowInstanceHistoryStatusTableName(){
		return this.engine.getFlowInstanceHistoryStatusTableName(this);
	}
	
	public void setFlowTaskSqlCreator(IFlowTaskSqlCreator creator){
		this.sqlCreator = creator;
	}
	public IFlowTaskSqlCreator getFlowTaskSqlCreator(){
		return this.sqlCreator;
	}

	public FlowTask getEmptyFlowTask(){
		return this.getEmptyFlowTask(0);
	}
	public FlowTask getEmptyFlowTask(int taskId){
		try{
			globalThreadLocal.set(SECURITY_KEY);
			return new FlowTask(this,taskId,SECURITY_KEY);
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}finally{
			globalThreadLocal.set(null);
		}
	}

	public FlowNode getEmptyFlowNode(){
		return this.getEmptyFlowNode(0);
	}
	public FlowNode getEmptyFlowNode(int nodeId){
		try{
			globalThreadLocal.set(SECURITY_KEY);
			return new FlowNode(this,nodeId,SECURITY_KEY);
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}finally{
			globalThreadLocal.set(null);
		}
	}
	
	public FlowTask queryFlowTask(int taskId){
		QueryTaskInfoContainer queryInfo = new QueryTaskInfoContainer();
		queryInfo.setInfo(QueryTaskInfoContainer.TASK_ID_FLAG, new Integer(taskId));
		FlowTask[] arr = this.queryFlowTask(queryInfo, null, null);
		if(arr.length > 0){
			return arr[0];
		}
		return null;
	}
	public FlowTask[] queryAllFlowTask(QueryTaskInfoContainer queryInfo){
		return this.queryFlowTask(queryInfo, null, null);
	}
	public FlowTask[] queryAllFlowTask(QueryTaskInfoContainer queryInfo ,IFlowTaskFilter filter){
		return this.queryFlowTask(queryInfo, null, filter);
	}
	public FlowTask[] queryFlowTask(QueryTaskInfoContainer queryInfo,FlowNode flowNode){
		return this.queryFlowTask(queryInfo, flowNode, null);
	}
	public FlowTask[] queryFlowTask(QueryTaskInfoContainer queryInfo,FlowNode flowNode,IFlowTaskFilter filter){
		TaskSql[] sqlArr = this.sqlCreator.createQuerySql(this, queryInfo);
		
		HashMap mapping = new HashMap();
		DbHandle conn = null;
		int flag;
		FlowTask task;
		int[] fieldTypeArr;
		try{
			conn = DbServer.getSingleInstance().getConn();
			StatementHandle stmt = conn.createStatement();
			ResultSet sets;
			for(int i=0;sqlArr != null && i < sqlArr.length;i++){
				sets = stmt.executeQuery(sqlArr[i].getSql());
				fieldTypeArr = SqlParaInfo.getDataType(sets);
				
				while(sets.next()){
					flag = sets.getInt(1);
					task = (FlowTask)mapping.get(flag);
					if(task == null){
						task = this.getEmptyFlowTask(flag);
						mapping.put(new Integer(flag), task);
					}
					SqlParaInfo.initInfo(sets, task, fieldTypeArr);
				}
				
				sets.close();
			}
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			DbServer.getSingleInstance().releaseConn(conn);
		}
		
		FlowTask[] arr = new FlowTask[mapping.size()];
		mapping.values().toArray(arr);
		return arr;
	}
	
	public FlowNode getFlowNode(String flowCode){
		FlowNode fNode;
		for(Iterator itr = this.flowNodeList.iterator();itr.hasNext();){
			fNode = (FlowNode)itr.next();
			if(fNode.getCode().equals(flowCode)){
				return fNode;
			}
		}
		return null;
	}
	public FlowNode getFlowNode(int flowNodeId){
		FlowNode fNode;
		for(Iterator itr = this.flowNodeList.iterator();itr.hasNext();){
			fNode = (FlowNode)itr.next();
			if(fNode.getId() == flowNodeId){
				return fNode;
			}
		}
		return null;
	}
	
	public FlowNode getFirstFlowNode(){
		if(this.flowNodeList.size() == 0){
			return null;
		}
		return (FlowNode)this.flowNodeList.get(0);
	}

	public boolean addFlowTaskChangeListener(IFlowTaskChangeListener listener){
		if(this.flowId <= 0){
			return false;
		}
		if(listener == null){
			return false;
		}
		synchronized(this.flowTaskListenerList){
			if(!this.flowTaskListenerList.contains(listener)){
				this.flowTaskListenerList.add(listener);
			}
		}
		
		return true;
	}
	public IFlowTaskChangeListener[] getAllFlowTaskListener(){
		if(this.flowId <= 0){
			return null;
		}
		synchronized(this.flowTaskListenerList){
			IFlowTaskChangeListener[] arr = new IFlowTaskChangeListener[this.flowTaskListenerList.size()];
			this.flowTaskListenerList.toArray(arr);
			return arr;
		}
	}
	public boolean removeFlowTaskListener(IFlowTaskChangeListener listener){
		if(this.flowId <= 0){
			return false;
		}
		if(listener == null){
			return false;
		}
		synchronized(this.flowTaskListenerList){
			return this.flowTaskListenerList.remove(listener);
		}
	}

	public boolean triggerFlowTaskStatusWillChanged(FlowTask task){
		boolean isOk = true;
		synchronized(this.flowTaskListenerList){
			for(int i=0;i<this.flowTaskListenerList.size();i++){
				isOk &= ((IFlowTaskChangeListener)this.flowTaskListenerList.get(i)).flowTaskWillChanged(task);
			}
		}
		isOk &= this.engine.triggerFlowTaskStatusWillChanged(task);
		
		return isOk;
	}
	public void triggerFlowTaskStatusChanged(FlowTask task,FlowNode fNode){
		synchronized(this.flowTaskListenerList){
			for(int i=0;i<this.flowTaskListenerList.size();i++){
				((IFlowTaskChangeListener)this.flowTaskListenerList.get(i)).flowTaskChanged(task,fNode);
			}
		}
		this.engine.triggerFlowTaskStatusChanged(task,fNode);
	}
}
