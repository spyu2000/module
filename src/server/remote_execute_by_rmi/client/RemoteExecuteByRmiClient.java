package server.remote_execute_by_rmi.client;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.rmi.Naming;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import server.remote_execute_by_rmi.server.IClsRemote;
import server.remote_execute_by_rmi.server.RemoteExecuteByRmiServer;

import com.fleety.base.InfoContainer;
import com.fleety.server.BasicServer;

public class RemoteExecuteByRmiClient extends BasicServer{
    private static final String REGISTER_CLASS_NAME_FLAG = "cls_name";
    private static final String REGISTER_GROUP_NAME_FLAG = "group_name";
    
    private String groupName = "default";
    private Vector regsitClassList = null;
    private String systemClsPath = null;
    
    public boolean startServer(){
        this.regsitClassList = new Vector(8);
        String tempStr = this.getStringPara(REGISTER_GROUP_NAME_FLAG);
        if(tempStr != null && tempStr.trim().length() > 0){
            this.groupName = tempStr.trim();
        }
        
        tempStr = this.getStringPara("cls_path");
        if(tempStr != null && tempStr.trim().length() > 0){
        	this.systemClsPath = tempStr.trim();
        }
        
        this.initRmiConnect();
        
        this.isRunning = true;
        return this.isRunning();
    }

    public void stopServer(){
        
    }
    
    private void registerAllClass() throws Exception{
        Object obj = this.getPara(REGISTER_CLASS_NAME_FLAG);
        if(obj == null){
        	System.out.println("No Obj Regist!");
            return ;
        }
        
        this.remote.clearGroupClass(this.groupName);
        
        if(obj instanceof List){
            for(Iterator itr = ((List)obj).iterator();itr.hasNext();){
                if(!this.registerNativeClass(itr.next().toString())){
            	    break;
                }
            }
        }else{
            this.registerNativeClass(obj.toString());
        }
    }
    private boolean registerNativeClass(String clsName){
        String clsPath = clsName.replace('.','/')+".class";
        URL url = null;
        File f ;
        
        if(clsPath.startsWith("!")){
        	clsPath = clsPath.substring(1);
        	clsName = clsName.substring(1);
        	url = this.getClass().getClassLoader().getResource(clsPath);
        }else if(this.systemClsPath == null){
        	url = this.getClass().getClassLoader().getResource(clsPath);
        }else{
        	f = new File(this.systemClsPath,clsPath);
            
            if(!f.exists()){
            	System.out.println("File Not Exist:"+f.getAbsolutePath());
            	return true;
            }
            try{
            	url = new URL("file://"+f.getAbsolutePath());
        	}catch(Exception e){
        		e.printStackTrace();
        		return true;
        	}
        }

        if(url == null){
        	return true;
        }
        
        InputStream in = null;
        try{
            int totalLength = 0,count = 0,tempCount;
            totalLength = url.openConnection().getContentLength();
            
            in = new BufferedInputStream(url.openStream());

            byte[] clsData = new byte[totalLength];
            while(count < totalLength){
                tempCount = in.read(clsData,count,totalLength-count);
                if(tempCount < 0){
                    break;
                }
                count += tempCount;
            }
            if(count == totalLength){
                return this.registerClass(clsName, clsData);
            }
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            try{
                if(in != null){
                    in.close();
                }
            }catch(Exception er){}
        }
        return true;
    }
    
    public boolean registerClass(String clsName,byte[] clsData){
        this.initRmiConnect();
        
        if(this.remote == null){
        	System.out.println("Remote Rmi Not Connected!");
            return false;
        }
        
        try{
            boolean isSuccess = this.remote.registerClass(this.groupName, clsName, clsData);
            
            if(isSuccess){
                System.out.println("RMI REMOTE EXECUTE["+clsName+"] REGIST SUCCESS!");
                if(!this.regsitClassList.contains(clsName)){
                	this.regsitClassList.add(clsName);
                }
            }
            
            if(!isSuccess){
                System.out.println("RMI REMOTE EXECUTE["+clsName+"] REGIST FAILURE!");
            }
            
            return isSuccess;
        }catch(Exception e){
            e.printStackTrace();
            this.clear();
        }
        return false;
    }
    
    public Object remoteRmiExecute(String clsName,InfoContainer para){
    	return this.remoteRmiExecute(clsName, para, true);
    }
    public Object remoteRmiExecute(String clsName,InfoContainer para, boolean validClsRegist){        
        this.initRmiConnect();
        
        if(this.remote == null){
            return null;
        }
        if(validClsRegist && !this.regsitClassList.contains(clsName)){
            System.out.println("No Register ClassName:"+clsName);
            return null;
        }
        
        try{
            return this.remote.execute(this.groupName, clsName, para);
        }catch(Exception e){
            e.printStackTrace();
            this.clear();
            
        	this.initRmiConnect();
        	try{
        		return this.remote.execute(this.groupName, clsName, para);
            }catch(Exception ee){}
        }
        return null;
    }

    private void clear(){
         this.remote = null;
         this.regsitClassList.clear();
    }
    
    private IClsRemote remote = null;
    private void initRmiConnect(){
        if(this.remote != null){
            return ;
        }
        
        String ip = this.getStringPara(RemoteExecuteByRmiServer.IP_FLAG);
        int port = Integer.parseInt(this.getStringPara(RemoteExecuteByRmiServer.PORT_FLAG));
        String rmiName = "//" + ip + ":" + port + "/" + RemoteExecuteByRmiServer.REMOTE_EXECUTE_RMI_NAME;
        
        try{
            this.remote = (IClsRemote)Naming.lookup(rmiName);
            this.registerAllClass();
        }catch(Exception e){
            e.printStackTrace();
            this.clear();
        }
    }
}
