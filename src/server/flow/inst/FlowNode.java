package server.flow.inst;

import java.sql.ResultSet;
import java.util.ArrayList;

import com.fleety.util.pool.db.DbConnPool.DbHandle;
import com.fleety.util.pool.db.DbConnPool.StatementHandle;

import server.db.DbServer;
import server.flow.FlowEngineServer;
import server.flow.IFlow;

public class FlowNode implements IFlow{
	public static final int START_NODE = 1;
	public static final int PENDING_NODE = 2;
	public static final int END_NODE = 3;

	public static final int NOTIFY_METHOD_MAIL = 1;
	public static final int NOTIFY_METHOD_SMS = 2;
	
	private FlowInstance flowInstance = null;
	private int id = 0;
	private int nodeType = 0;
	private String code = null;
	private String name = null;
	private int notifyMethod = 0;
	//单位分钟，小于等于0代表无时长限制
	private int alarmDuration = -1;
	private String[] notifiers = null;
	//下一步处理人的虚拟岗位ID
	private String nextActionOperator = null;
	private ArrayList nextNodeList = new ArrayList(2);

	public FlowNode(FlowInstance flowInstance,Object key) throws Exception{
		this(flowInstance,0,key);
	}
	public FlowNode(FlowInstance flowInstance,int flowNodeId,Object key) throws Exception{
		if(key != globalThreadLocal.get()){
			throw new Exception("Can't Create because key!");
		}
		
		this.flowInstance = flowInstance;
		this.id = flowNodeId;
	}
	
	public int getId() {
		return id;
	}
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getNodeType() {
		return nodeType;
	}
	public void setNodeType(int nodeType) {
		this.nodeType = nodeType;
	}
	
	public boolean isStartPoint(){
		return false;
	}
	public Boolean isEndPoint(){
		return false;
	}
	public FlowNode[] getNext(){
		return null;
	}

	public void setAlarmDuration(int alarmDuration){
		this.alarmDuration = alarmDuration;
	}
	public int getAlarmDuration(){
		return this.alarmDuration;
	}
	public void setAllNotifiers(String[] notifiers){
		this.notifiers = notifiers;
	}
	public String[] getAllNotifiers(){
		return this.notifiers;
	}
	public void setNextActionOperator(String nextActionOperator){
		this.nextActionOperator = nextActionOperator;
	}
	public String getNextActionOperator(){
		return this.nextActionOperator;
	}
	public void setNotifyMethod(int notifyMethod){
		this.notifyMethod = notifyMethod;
	}
	public int getNotifyMethod(){
		return this.notifyMethod;
	}
	
	public void addNextNode(FlowNode fNode){
		if(this.nextNodeList.contains(fNode)){
			return ;
		}
		this.nextNodeList.add(fNode);
	}
	public void removeNextNode(FlowNode fNode){
		this.nextNodeList.remove(fNode);
	}

	public boolean save(DbHandle conn) throws Exception{
		if(this.id <= 0){
			this.id = (int)DbServer.getSingleInstance().getAvaliableId(conn, FlowEngineServer.FLOW_INSTANCE_NODE_TABLE_NAME, "id");
		}
		
		StatementHandle stmt = conn.prepareStatement("select id from "+FlowEngineServer.FLOW_INSTANCE_NODE_TABLE_NAME+" where id=?");
		stmt.setInt(1, this.id);
		ResultSet sets = stmt.executeQuery();
		boolean isExist = sets.next();
		stmt.close();
		
		if(isExist){
			stmt = conn.prepareStatement("update "+FlowEngineServer.FLOW_INSTANCE_NODE_TABLE_NAME+" set flow_id=?,NAME=?,code=?,node_type=?,NEXT_ACTION_OPERATOR=?,ALARM_DURATION=?,NOTIFIERS=?,NOTIFY_METHOD=? where id=?");
		}else{
			stmt = conn.prepareStatement("insert into "+FlowEngineServer.FLOW_INSTANCE_NODE_TABLE_NAME+"(flow_id,NAME,code,node_type,NEXT_ACTION_OPERATOR,ALARM_DURATION,NOTIFIERS,NOTIFY_METHOD,id) values(?,?,?,?,?,?,?,?,?)");
		}
		
		stmt.setInt(1, this.flowInstance.getFlowId());
		stmt.setString(2, this.getName());
		stmt.setString(3, this.getCode());
		stmt.setInt(4, this.getNodeType());
		stmt.setString(5, this.getNextActionOperator());
		stmt.setInt(6, this.getAlarmDuration());
		stmt.setString(7, this.getAllNotifierStr());
		stmt.setInt(8, this.getNotifyMethod());
		stmt.setInt(9, this.id);
		stmt.execute();
		stmt.close();
		
		return true;
	}
	
	public boolean updateNextNode(DbHandle conn) throws Exception{
		StatementHandle stmt = conn.prepareStatement("update "+FlowEngineServer.FLOW_INSTANCE_NODE_TABLE_NAME+" set NEXT_FLOW_IDS=? where id=?");
		stmt.setString(1, this.getNextNodeIdStr());
		stmt.setInt(2, this.id);
		stmt.execute();
		stmt.close();
		
		return true;
	}
	
	public String getNextNodeIdStr(){
		StringBuffer strBuff = new StringBuffer(128);
		FlowNode fNode;
		for(int i=0;i<this.nextNodeList.size();i++){
			if(i > 0){
				strBuff.append(",");
			}
			fNode = (FlowNode)this.nextNodeList.get(i);
			strBuff.append(fNode.getId());
		}
		return strBuff.toString();
	}
	
	public FlowNode[] getNextNodeArr(){
		FlowNode[] arr = new FlowNode[this.nextNodeList.size()];
		this.nextNodeList.toArray(arr);
		return arr;
	}
	
	public String getAllNotifierStr(){
		if(this.notifiers == null){
			return "";
		}
		StringBuffer strBuff = new StringBuffer(128);
		for(int i=0;i<this.notifiers.length;i++){
			if(i > 0){
				strBuff.append(",");
			}
			strBuff.append(this.notifiers[i]);
		}
		return strBuff.toString();
	}
}
