// Stub class generated by rmic, do not edit.
// Contents subject to change without notice.

package server.manager.app.client;

public final class ClientManagerRmiInterface_Stub
    extends java.rmi.server.RemoteStub
    implements server.manager.app.client.IAppClient, java.rmi.Remote
{
    private static final long serialVersionUID = 2;
    
    private static java.lang.reflect.Method $method_endDispatchApplication_0;
    private static java.lang.reflect.Method $method_fileChanged_1;
    private static java.lang.reflect.Method $method_restart_2;
    private static java.lang.reflect.Method $method_startDispatchApplication_3;
    private static java.lang.reflect.Method $method_stop_4;
    
    static {
	try {
	    $method_endDispatchApplication_0 = server.manager.app.client.IAppClient.class.getMethod("endDispatchApplication", new java.lang.Class[] {java.lang.String.class});
	    $method_fileChanged_1 = server.manager.app.client.IAppClient.class.getMethod("fileChanged", new java.lang.Class[] {java.lang.String.class, java.lang.String.class, long.class, long.class});
	    $method_restart_2 = server.manager.app.client.IAppClient.class.getMethod("restart", new java.lang.Class[] {java.lang.String.class});
	    $method_startDispatchApplication_3 = server.manager.app.client.IAppClient.class.getMethod("startDispatchApplication", new java.lang.Class[] {java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class});
	    $method_stop_4 = server.manager.app.client.IAppClient.class.getMethod("stop", new java.lang.Class[] {java.lang.String.class});
	} catch (java.lang.NoSuchMethodException e) {
	    throw new java.lang.NoSuchMethodError(
		"stub class initialization failed");
	}
    }
    
    // constructors
    public ClientManagerRmiInterface_Stub(java.rmi.server.RemoteRef ref) {
	super(ref);
    }
    
    // methods from remote interfaces
    
    // implementation of endDispatchApplication(String)
    public void endDispatchApplication(java.lang.String $param_String_1)
	throws java.rmi.RemoteException
    {
	try {
	    ref.invoke(this, $method_endDispatchApplication_0, new java.lang.Object[] {$param_String_1}, 8924844718181321118L);
	} catch (java.lang.RuntimeException e) {
	    throw e;
	} catch (java.rmi.RemoteException e) {
	    throw e;
	} catch (java.lang.Exception e) {
	    throw new java.rmi.UnexpectedException("undeclared checked exception", e);
	}
    }
    
    // implementation of fileChanged(String, String, long, long)
    public void fileChanged(java.lang.String $param_String_1, java.lang.String $param_String_2, long $param_long_3, long $param_long_4)
	throws java.rmi.RemoteException
    {
	try {
	    ref.invoke(this, $method_fileChanged_1, new java.lang.Object[] {$param_String_1, $param_String_2, new java.lang.Long($param_long_3), new java.lang.Long($param_long_4)}, 8184241281602052954L);
	} catch (java.lang.RuntimeException e) {
	    throw e;
	} catch (java.rmi.RemoteException e) {
	    throw e;
	} catch (java.lang.Exception e) {
	    throw new java.rmi.UnexpectedException("undeclared checked exception", e);
	}
    }
    
    // implementation of restart(String)
    public void restart(java.lang.String $param_String_1)
	throws java.rmi.RemoteException
    {
	try {
	    ref.invoke(this, $method_restart_2, new java.lang.Object[] {$param_String_1}, -4179368474160842221L);
	} catch (java.lang.RuntimeException e) {
	    throw e;
	} catch (java.rmi.RemoteException e) {
	    throw e;
	} catch (java.lang.Exception e) {
	    throw new java.rmi.UnexpectedException("undeclared checked exception", e);
	}
    }
    
    // implementation of startDispatchApplication(String, String, String, String)
    public void startDispatchApplication(java.lang.String $param_String_1, java.lang.String $param_String_2, java.lang.String $param_String_3, java.lang.String $param_String_4)
	throws java.rmi.RemoteException
    {
	try {
	    ref.invoke(this, $method_startDispatchApplication_3, new java.lang.Object[] {$param_String_1, $param_String_2, $param_String_3, $param_String_4}, 1951055859006733177L);
	} catch (java.lang.RuntimeException e) {
	    throw e;
	} catch (java.rmi.RemoteException e) {
	    throw e;
	} catch (java.lang.Exception e) {
	    throw new java.rmi.UnexpectedException("undeclared checked exception", e);
	}
    }
    
    // implementation of stop(String)
    public void stop(java.lang.String $param_String_1)
	throws java.rmi.RemoteException
    {
	try {
	    ref.invoke(this, $method_stop_4, new java.lang.Object[] {$param_String_1}, -1941243546222654391L);
	} catch (java.lang.RuntimeException e) {
	    throw e;
	} catch (java.rmi.RemoteException e) {
	    throw e;
	} catch (java.lang.Exception e) {
	    throw new java.rmi.UnexpectedException("undeclared checked exception", e);
	}
    }
}
