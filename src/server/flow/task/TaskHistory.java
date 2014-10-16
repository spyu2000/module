package server.flow.task;

import com.fleety.base.GeneralConst;
import com.fleety.base.InfoContainer;

import server.flow.IFlow;

public class TaskHistory extends InfoContainer implements IFlow {
	public static final Object ID_FLAG = new Object();
	public static final Object FLOW_INSTANCE_ID_FLAG = new Object();
	public static final Object FLOW_TASK_ID_FLAG = new Object();
	public static final Object FLOW_NODE_FLAG = new Object();
	public static final Object FLOW_ARRIVE_TIME_FLAG = new Object();
	public static final Object FLOW_REASON_FLAG = new Object();
	public static final Object FLOW_OPERATOR_FLAG = new Object();
	
	
	public String toString(){
		StringBuffer strBuff = new StringBuffer(256);
		strBuff.append(this.getString(FLOW_INSTANCE_ID_FLAG));
		strBuff.append("\t");
		strBuff.append(this.getString(ID_FLAG));
		strBuff.append("\t");
		strBuff.append(this.getString(FLOW_TASK_ID_FLAG));
		strBuff.append("\t");
		strBuff.append(this.getString(FLOW_NODE_FLAG));
		strBuff.append("\t");
		strBuff.append(GeneralConst.YYYY_MM_DD_HH_MM_SS.format(this.getDate(FLOW_ARRIVE_TIME_FLAG)));
		strBuff.append("\t");
		strBuff.append(this.getString(FLOW_REASON_FLAG));
		strBuff.append("\t");
		strBuff.append(this.getString(FLOW_OPERATOR_FLAG));
		return strBuff.toString();
	}
}
