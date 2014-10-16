package server.remote_execute_by_rmi.server;

public class ByteClassLoader extends ClassLoader{
    public ByteClassLoader(ClassLoader parent){
        super(parent);
    }
    
    public Class registerClass(String clsName,byte[] clsData){
        try{
            return this.defineClass(clsName, clsData, 0, clsData.length);
        }catch(LinkageError er){
            try{
                return Class.forName(clsName,false,this);
            }catch(Exception e){}
        }catch(Throwable e){
            e.printStackTrace();
        }
        return null;
    }
}
