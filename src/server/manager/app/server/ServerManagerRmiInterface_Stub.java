// Stub class generated by rmic, do not edit.
// Contents subject to change without notice.

package server.manager.app.server;

public final class ServerManagerRmiInterface_Stub
    extends java.rmi.server.RemoteStub
    implements server.manager.app.server.IAppServer, java.rmi.Remote
{
    private static final long serialVersionUID = 2;
    
    private static java.lang.reflect.Method $method_cmdExecute_0;
    private static java.lang.reflect.Method $method_heartConnect_1;
    private static java.lang.reflect.Method $method_registClient_2;
    private static java.lang.reflect.Method $method_requestFile_3;
    
    static {
	try {
	    $method_cmdExecute_0 = server.manager.app.server.IAppServer.class.getMethod("cmdExecute", new java.lang.Class[] {int.class, java.lang.String[].class});
	    $method_heartConnect_1 = server.manager.app.server.IAppServer.class.getMethod("heartConnect", new java.lang.Class[] {java.lang.String.class});
	    $method_registClient_2 = server.manager.app.server.IAppServer.class.getMethod("registClient", new java.lang.Class[] {java.lang.String.class, java.lang.String.class, int.class});
	    $method_requestFile_3 = server.manager.app.server.IAppServer.class.getMethod("requestFile", new java.lang.Class[] {java.lang.String.class, java.lang.String.class, long.class, long.class});
	} catch (java.lang.NoSuchMethodException e) {
	    throw new java.lang.NoSuchMethodError(
		"stub class initialization failed");
	}
    }
    
    // constructors
    public ServerManagerRmiInterface_Stub(java.rmi.server.RemoteRef ref) {
	super(ref);
    }
    
    // methods from remote interfaces
    
    // implementation of cmdExecute(int, String[])
    public boolean cmdExecute(int $param_int_1, java.lang.String[] $param_arrayOf_String_2)
	throws java.rmi.RemoteException
    {
	try {
	    Object $result = ref.invoke(this, $method_cmdExecute_0, new java.lang.Object[] {new java.lang.Integer($param_int_1), $param_arrayOf_String_2}, 4399747623934799893L);
	    return ((java.lang.Boolean) $result).booleanValue();
	} catch (java.lang.RuntimeException e) {
	    throw e;
	} catch (java.rmi.RemoteException e) {
	    throw e;
	} catch (java.lang.Exception e) {
	    throw new java.rmi.UnexpectedException("undeclared checked exception", e);
	}
    }
    
    // implementation of heartConnect(String)
    public void heartConnect(java.lang.String $param_String_1)
	throws java.rmi.RemoteException
    {
	try {
	    ref.invoke(this, $method_heartConnect_1, new java.lang.Object[] {$param_String_1}, 7011309389245417911L);
	} catch (java.lang.RuntimeException e) {
	    throw e;
	} catch (java.rmi.RemoteException e) {
	    throw e;
	} catch (java.lang.Exception e) {
	    throw new java.rmi.UnexpectedException("undeclared checked exception", e);
	}
    }
    
    // implementation of registClient(String, String, int)
    public boolean registClient(java.lang.String $param_String_1, java.lang.String $param_String_2, int $param_int_3)
	throws java.rmi.RemoteException
    {
	try {
	    Object $result = ref.invoke(this, $method_registClient_2, new java.lang.Object[] {$param_String_1, $param_String_2, new java.lang.Integer($param_int_3)}, -4577600343607997163L);
	    return ((java.lang.Boolean) $result).booleanValue();
	} catch (java.lang.RuntimeException e) {
	    throw e;
	} catch (java.rmi.RemoteException e) {
	    throw e;
	} catch (java.lang.Exception e) {
	    throw new java.rmi.UnexpectedException("undeclared checked exception", e);
	}
    }
    
    // implementation of requestFile(String, String, long, long)
    public byte[] requestFile(java.lang.String $param_String_1, java.lang.String $param_String_2, long $param_long_3, long $param_long_4)
	throws java.rmi.RemoteException
    {
	try {
	    Object $result = ref.invoke(this, $method_requestFile_3, new java.lang.Object[] {$param_String_1, $param_String_2, new java.lang.Long($param_long_3), new java.lang.Long($param_long_4)}, 8790443657451615259L);
	    return ((byte[]) $result);
	} catch (java.lang.RuntimeException e) {
	    throw e;
	} catch (java.rmi.RemoteException e) {
	    throw e;
	} catch (java.lang.Exception e) {
	    throw new java.rmi.UnexpectedException("undeclared checked exception", e);
	}
    }
}
