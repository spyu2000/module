// Stub class generated by rmic, do not edit.
// Contents subject to change without notice.

package server.distribute.client;

public final class ClientRmiInterface_Stub
    extends java.rmi.server.RemoteStub
    implements server.distribute.IDistribute, java.rmi.Remote
{
    private static final long serialVersionUID = 2;
    
    private static java.lang.reflect.Method $method_c_dispatchServer_0;
    private static java.lang.reflect.Method $method_c_dispatchTask_1;
    private static java.lang.reflect.Method $method_c_heartConnect_2;
    private static java.lang.reflect.Method $method_s_heartConnect_3;
    private static java.lang.reflect.Method $method_s_registClient_4;
    private static java.lang.reflect.Method $method_s_requestJar_5;
    private static java.lang.reflect.Method $method_s_taskFinish_6;
    
    static {
	try {
	    $method_c_dispatchServer_0 = server.distribute.IDistribute.class.getMethod("c_dispatchServer", new java.lang.Class[] {server.distribute.ServerInfo[].class});
	    $method_c_dispatchTask_1 = server.distribute.IDistribute.class.getMethod("c_dispatchTask", new java.lang.Class[] {server.distribute.TaskInfo[].class});
	    $method_c_heartConnect_2 = server.distribute.IDistribute.class.getMethod("c_heartConnect", new java.lang.Class[] {java.lang.String.class});
	    $method_s_heartConnect_3 = server.distribute.IDistribute.class.getMethod("s_heartConnect", new java.lang.Class[] {java.lang.String.class, int.class});
	    $method_s_registClient_4 = server.distribute.IDistribute.class.getMethod("s_registClient", new java.lang.Class[] {java.lang.String.class, int.class, server.distribute.IDistribute.class});
	    $method_s_requestJar_5 = server.distribute.IDistribute.class.getMethod("s_requestJar", new java.lang.Class[] {server.distribute.JarInfo.class});
	    $method_s_taskFinish_6 = server.distribute.IDistribute.class.getMethod("s_taskFinish", new java.lang.Class[] {java.lang.String.class, java.lang.String.class, long.class, server.distribute.ResultInfo.class});
	} catch (java.lang.NoSuchMethodException e) {
	    throw new java.lang.NoSuchMethodError(
		"stub class initialization failed");
	}
    }
    
    // constructors
    public ClientRmiInterface_Stub(java.rmi.server.RemoteRef ref) {
	super(ref);
    }
    
    // methods from remote interfaces
    
    // implementation of c_dispatchServer(ServerInfo[])
    public void c_dispatchServer(server.distribute.ServerInfo[] $param_arrayOf_ServerInfo_1)
	throws java.rmi.RemoteException
    {
	try {
	    ref.invoke(this, $method_c_dispatchServer_0, new java.lang.Object[] {$param_arrayOf_ServerInfo_1}, 8553598380462078743L);
	} catch (java.lang.RuntimeException e) {
	    throw e;
	} catch (java.rmi.RemoteException e) {
	    throw e;
	} catch (java.lang.Exception e) {
	    throw new java.rmi.UnexpectedException("undeclared checked exception", e);
	}
    }
    
    // implementation of c_dispatchTask(TaskInfo[])
    public void c_dispatchTask(server.distribute.TaskInfo[] $param_arrayOf_TaskInfo_1)
	throws java.rmi.RemoteException
    {
	try {
	    ref.invoke(this, $method_c_dispatchTask_1, new java.lang.Object[] {$param_arrayOf_TaskInfo_1}, -7038058487150895375L);
	} catch (java.lang.RuntimeException e) {
	    throw e;
	} catch (java.rmi.RemoteException e) {
	    throw e;
	} catch (java.lang.Exception e) {
	    throw new java.rmi.UnexpectedException("undeclared checked exception", e);
	}
    }
    
    // implementation of c_heartConnect(String)
    public void c_heartConnect(java.lang.String $param_String_1)
	throws java.rmi.RemoteException
    {
	try {
	    ref.invoke(this, $method_c_heartConnect_2, new java.lang.Object[] {$param_String_1}, -1947360645529195603L);
	} catch (java.lang.RuntimeException e) {
	    throw e;
	} catch (java.rmi.RemoteException e) {
	    throw e;
	} catch (java.lang.Exception e) {
	    throw new java.rmi.UnexpectedException("undeclared checked exception", e);
	}
    }
    
    // implementation of s_heartConnect(String, int)
    public boolean s_heartConnect(java.lang.String $param_String_1, int $param_int_2)
	throws java.rmi.RemoteException
    {
	try {
	    Object $result = ref.invoke(this, $method_s_heartConnect_3, new java.lang.Object[] {$param_String_1, new java.lang.Integer($param_int_2)}, 4672280481065957972L);
	    return ((java.lang.Boolean) $result).booleanValue();
	} catch (java.lang.RuntimeException e) {
	    throw e;
	} catch (java.rmi.RemoteException e) {
	    throw e;
	} catch (java.lang.Exception e) {
	    throw new java.rmi.UnexpectedException("undeclared checked exception", e);
	}
    }
    
    // implementation of s_registClient(String, int, IDistribute)
    public void s_registClient(java.lang.String $param_String_1, int $param_int_2, server.distribute.IDistribute $param_IDistribute_3)
	throws java.rmi.RemoteException
    {
	try {
	    ref.invoke(this, $method_s_registClient_4, new java.lang.Object[] {$param_String_1, new java.lang.Integer($param_int_2), $param_IDistribute_3}, 2058485689294834043L);
	} catch (java.lang.RuntimeException e) {
	    throw e;
	} catch (java.rmi.RemoteException e) {
	    throw e;
	} catch (java.lang.Exception e) {
	    throw new java.rmi.UnexpectedException("undeclared checked exception", e);
	}
    }
    
    // implementation of s_requestJar(JarInfo)
    public byte[] s_requestJar(server.distribute.JarInfo $param_JarInfo_1)
	throws java.rmi.RemoteException
    {
	try {
	    Object $result = ref.invoke(this, $method_s_requestJar_5, new java.lang.Object[] {$param_JarInfo_1}, 2223067534048581830L);
	    return ((byte[]) $result);
	} catch (java.lang.RuntimeException e) {
	    throw e;
	} catch (java.rmi.RemoteException e) {
	    throw e;
	} catch (java.lang.Exception e) {
	    throw new java.rmi.UnexpectedException("undeclared checked exception", e);
	}
    }
    
    // implementation of s_taskFinish(String, String, long, ResultInfo)
    public server.distribute.TaskInfo s_taskFinish(java.lang.String $param_String_1, java.lang.String $param_String_2, long $param_long_3, server.distribute.ResultInfo $param_ResultInfo_4)
	throws java.rmi.RemoteException
    {
	try {
	    Object $result = ref.invoke(this, $method_s_taskFinish_6, new java.lang.Object[] {$param_String_1, $param_String_2, new java.lang.Long($param_long_3), $param_ResultInfo_4}, -7147141779858075969L);
	    return ((server.distribute.TaskInfo) $result);
	} catch (java.lang.RuntimeException e) {
	    throw e;
	} catch (java.rmi.RemoteException e) {
	    throw e;
	} catch (java.lang.Exception e) {
	    throw new java.rmi.UnexpectedException("undeclared checked exception", e);
	}
    }
}
