package server.flow;

public interface IFlow {
	public static final ThreadLocal globalThreadLocal = new ThreadLocal();
	
	public static final Object OPERATOR_USER_ID_FLAG = new Object();
	public static final Object FLOW_TASK_ID_FLAG = new Object();
}
