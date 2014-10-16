package server.flow;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import server.db.DbServer;
import server.flow.inst.FlowInstance;
import server.flow.inst.FlowNode;
import server.flow.inst.IFlowTaskChangeListener;
import server.flow.task.FlowTask;
import server.flow.task.sql.IFlowTaskSqlCreator;

import com.fleety.server.BasicServer;
import com.fleety.util.pool.db.DbConnPool.DbHandle;
import com.fleety.util.pool.db.DbConnPool.StatementHandle;

public class FlowEngineServer extends BasicServer implements IFlow{
	public static final String FLOW_INSTANCE_TABLE_NAME = "FLEETY_FLOW_INSTANCE";
	public static final String FLOW_INSTANCE_NODE_TABLE_NAME = "FLEETY_FLOW_INST_NODE";

	private static final Object SECURITY_KEY = new Object();
	
	private HashMap instMapping = null;
	private ArrayList flowTaskListenerList = null;
	
	private ISystemInfo systemInfo = null;
	
	private static final FlowEngineServer singleInstance = new FlowEngineServer();
	public static FlowEngineServer getSingleInstance(){
		return singleInstance;
	}
	private FlowEngineServer(){
		
	}
	
	public boolean startServer() {
		if(!this.initDb()){
			return false;
		}
		this.instMapping = new HashMap();
		this.flowTaskListenerList = new ArrayList(4);
		
		try{
			this.systemInfo = (ISystemInfo)Class.forName(this.getStringPara("system_info_cls")).newInstance();
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}
		this.initFlowInstance();
		
		this.isRunning = true;
		return this.isRunning();
	}
	
	private void initFlowInstance(){
		DbHandle conn = null;
		IFlowTaskSqlCreator sqlCreator;
		try{
			FlowInstance inst;
			conn = DbServer.getSingleInstance().getConn();
			StatementHandle stmt = conn.createStatement();
			ResultSet sets = stmt.executeQuery("select * from "+FLOW_INSTANCE_TABLE_NAME+" where is_used=0");
			while(sets.next()){
				globalThreadLocal.set(SECURITY_KEY);
				inst = new FlowInstance(this,sets.getInt("id"),SECURITY_KEY);
				inst.setFlowCode(sets.getString("code"));
				inst.setFlowName(sets.getString("name"));
				sqlCreator = this.systemInfo.getSqlCreator(sets.getString("SQL_CREATOR"));
				if(sqlCreator == null){
					System.out.println("Flow Instance["+inst.getFlowName()+" "+inst.getFlowCode()+"] Init Sql Creator Failure!");
					continue;
				}
				inst.setFlowTaskSqlCreator(sqlCreator);

				System.out.println("Flow Instance["+inst.getFlowName()+" "+inst.getFlowCode()+"] Init Success!");
				this.instMapping.put(inst.getFlowCode(), inst);
			}
			sets.close();
			
			HashMap node2NextMapping = new HashMap();
			int flowId;
			FlowNode fNode;
			String notifiers,nextNodeStr;
			sets = stmt.executeQuery("select * from "+FLOW_INSTANCE_NODE_TABLE_NAME+" where flow_id in(select id from "+FLOW_INSTANCE_TABLE_NAME+" where is_used=0) order by flow_id,node_type asc");
			while(sets.next()){
				flowId = sets.getInt("flow_id");
				inst = this.getFlowInstance(flowId);
				if(inst == null){
					continue;
				}
				fNode = inst.getEmptyFlowNode(sets.getInt("id"));
				fNode.setCode(sets.getString("code"));
				fNode.setAlarmDuration(sets.getInt("alarm_duration"));
				notifiers = sets.getString("notifiers");
				if(notifiers != null){
					fNode.setAllNotifiers(notifiers.split(","));
				}
				fNode.setName(sets.getString("name"));
				fNode.setNextActionOperator(sets.getString("next_action_operator"));
				fNode.setNodeType(sets.getInt("node_type"));
				fNode.setNotifyMethod(sets.getInt("notify_method"));
				
				nextNodeStr = sets.getString("next_flow_ids");
				node2NextMapping.put(new Integer(fNode.getId()), nextNodeStr);
				
				inst.addFlowNode(fNode);
			}
			sets.close();
			stmt.close();
			
			
			FlowInstance[] arr = this.getAllFlowInstance();
			for(int i=0;i<arr.length;i++){
				arr[i].initNextFlowNode(node2NextMapping);
			}
			
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			DbServer.getSingleInstance().releaseConn(conn);
		}
	}
	
	public FlowInstance getEmptyFlowInstance(){
		try{
			globalThreadLocal.set(SECURITY_KEY);
			return new FlowInstance(this,SECURITY_KEY);
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}finally{
			globalThreadLocal.set(null);
		}
	}
	public void addFlowInstance(FlowInstance inst){
		if(inst == null){
			return ;
		}
		synchronized(this.instMapping){
			this.instMapping.put(inst.getFlowCode(), inst);
		}
	}
	public void removeFlowInstance(String flowCode){
		synchronized(this.instMapping){
			this.instMapping.remove(flowCode);
		}
	}
	public FlowInstance[] getAllFlowInstance(){
		if(this.instMapping == null){
			return null;
		}
		
		synchronized(this.instMapping){
			FlowInstance[] arr = new FlowInstance[this.instMapping.size()];
			this.instMapping.values().toArray(arr);
			return arr;
		}
	}
	public FlowInstance getFlowInstance(int flowId){
		if(this.instMapping == null){
			return null;
		}
		
		FlowInstance flow;
		synchronized(this.instMapping){
			for(Iterator itr = this.instMapping.values().iterator();itr.hasNext();){
				flow = (FlowInstance)itr.next();
				if(flow.getFlowId() == flowId){
					return flow;
				}
			}
		}
		return null;
	}
	public FlowInstance getFlowInstance(String flowCode){
		if(!this.isRunning()){
			return null;
		}
		if(flowCode == null || flowCode.trim().length()==0){
			return null;
		}
		if(this.instMapping == null){
			return null;
		}
		
		synchronized(this.instMapping){
			return (FlowInstance)this.instMapping.get(flowCode);
		}
	}
	public boolean addFlowTaskChangeListener(IFlowTaskChangeListener listener){
		if(!this.isRunning()){
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
		if(!this.isRunning()){
			return null;
		}
		synchronized(this.flowTaskListenerList){
			IFlowTaskChangeListener[] arr = new IFlowTaskChangeListener[this.flowTaskListenerList.size()];
			this.flowTaskListenerList.toArray(arr);
			return arr;
		}
	}
	public boolean removeFlowTaskListener(IFlowTaskChangeListener listener){
		if(!this.isRunning()){
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
		return isOk;
	}
	public void triggerFlowTaskStatusChanged(FlowTask task,FlowNode fNode){
		synchronized(this.flowTaskListenerList){
			for(int i=0;i<this.flowTaskListenerList.size();i++){
				((IFlowTaskChangeListener)this.flowTaskListenerList.get(i)).flowTaskChanged(task,fNode);
			}
		}
	}
	
	
	public void stopServer(){
		super.stopServer();
		this.instMapping = null;
	}
	
	public String getFlowInstanceStatusTableName(FlowInstance instance){
		if(instance.getFlowId() == 0){
			return null;
		}
		return "FLOW_INST_"+instance.getFlowId()+"_STATUS";
	}
	public String getFlowInstanceHistoryStatusTableName(FlowInstance instance){
		if(instance.getFlowId() == 0){
			return null;
		}
		return "FLOW_INST_"+instance.getFlowId()+"_STATUS_HIS";
	}
	
	public ISystemInfo getSystemInfo(){
		return this.systemInfo;
	}

	private boolean initDb(){
		DbHandle conn = DbServer.getSingleInstance().getConn();
		try{
			StatementHandle stmt = conn.createStatement();
			ResultSet sets = null;
			
			String sql = null;
			sets = stmt.executeQuery("select * from user_tables where table_name='"+FLOW_INSTANCE_TABLE_NAME+"'");
			if(!sets.next()){
				sql = "create table "+FLOW_INSTANCE_TABLE_NAME;
				sql += "(";
				sql += "ID NUMBER(10) not null,";
				sql += "NAME VARCHAR2(256) not null,";
				sql += "CODE VARCHAR2(64) not null,";
				sql += "SQL_CREATOR VARCHAR2(256),";
				sql += "CREATOR VARCHAR2(64) not null,";
				sql += "CREATE_TIME DATE default sysdate not null,";
				sql += "UPDATE_TIME DATE default sysdate,";
				sql += "is_used NUMBER(4) default 0";
				sql += ")";
				stmt.execute(sql);
				
				sql = "alter table "+FLOW_INSTANCE_TABLE_NAME+" add constraint P_FLOW_ID primary key (ID)";
				stmt.execute(sql);
			}
			sets.close();
			sets = stmt.executeQuery("select * from user_tables where table_name='"+FLOW_INSTANCE_NODE_TABLE_NAME+"'");
			if(!sets.next()){
				sql= "create table FLEETY_FLOW_INST_NODE";
				sql += "(";
				sql += "ID                   NUMBER(10) not null,";
				sql += "FLOW_ID              NUMBER(10) not null,";
				sql += "NAME                 VARCHAR2(256) not null,";
				sql += "CODE                 VARCHAR2(64),";
				sql += "NODE_TYPE            NUMBER(10) not null,";
				sql += "NEXT_FLOW_IDS        VARCHAR2(256),";
				sql += "NEXT_ACTION_OPERATOR VARCHAR2(32),";
				sql += "ALARM_DURATION       NUMBER(10),";
				sql += "NOTIFIERS            VARCHAR2(256),";
				sql += "NOTIFY_METHOD        NUMBER(10) default 0 not null";
				sql += ")";
				stmt.execute(sql);
				
				sql = "alter table "+FLOW_INSTANCE_NODE_TABLE_NAME+" add constraint P_FLOW_INS_ID primary key (ID)";
				stmt.execute(sql);
				
				sql = "create index FLOW_INST_INDEX1 on "+FLOW_INSTANCE_NODE_TABLE_NAME+"(FLOW_ID)";
				stmt.execute(sql);
			}
			sets.close();
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}finally{
			DbServer.getSingleInstance().releaseConn(conn);
		}
		return true;
	}
}
