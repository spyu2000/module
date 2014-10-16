package server.sync;

import java.sql.ResultSet;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.TimerTask;

import server.db.DbServer;
import server.mail.MailServer;
import server.notify.INotifyServer;
import server.threadgroup.PoolInfo;
import server.threadgroup.ThreadPoolGroupServer;

import com.fleety.base.FleetyThread;
import com.fleety.base.GeneralConst;
import com.fleety.base.InfoContainer;
import com.fleety.server.BasicServer;
import com.fleety.server.ServerContainer;
import com.fleety.util.pool.db.DbConnPool.DbHandle;
import com.fleety.util.pool.db.DbConnPool.StatementHandle;
import com.fleety.util.pool.thread.BasicTask;
import com.fleety.util.pool.thread.ThreadPool;
import com.fleety.util.pool.timer.FleetyTimerTask;

public class DbSyncServer extends BasicServer {
	public static final String SRC_DB_FLAG = "src_db";
	public static final String DEST_DB_FLAG = "dest_db";
	
	//物化视图日志名($v_..)，目标表名($t_..)，主键（$k_..);.......
	public static final String SYNC_TABLE_FLAG = "sync_table";
	public static final String SYNC_THREAD_NUM_FLAG = "thread_num";
	public static final String SYNC_INTERVAL_FLAG = "sync_interval";
	public static final String FILTER_NAME_FLAG = "sync_filter";
	
	private static final String timerName = "sync_timer_name";
	
    private List taskList = null;
	private ISyncFilter filter = null;
	private boolean isPrint = false;
	
	public boolean startServer() {
		String tempStr;
		
		try{
			tempStr = this.getStringPara("is_print");
			if(tempStr != null && tempStr.trim().equals("true")){
				this.isPrint = true;
			}
			tempStr = this.getStringPara(FILTER_NAME_FLAG);
			if(tempStr != null && tempStr.trim().length() > 0){
				this.filter = (ISyncFilter)Class.forName(tempStr.trim()).newInstance();
			}
			
			List syncTableList = null;
			Object syncTableInfo = this.getPara(SYNC_TABLE_FLAG);
			if(syncTableInfo instanceof List){
				syncTableList = (List)syncTableInfo;
			}else{
				syncTableList = new ArrayList();
				syncTableList.add(syncTableInfo);
			}
			this.taskList =  new ArrayList();
			String syncTable;
			for(Iterator itr = syncTableList.iterator();itr.hasNext();){
				syncTable = (String)itr.next();
				String[] syncTables = syncTable.split(";");
	            for(int i = 0; i < syncTables.length;i++){
	                 String[] spiltTable = syncTables[i].split(",");
	                 String mvTable = "";
	                 String destTable = "";
	                 String[] keys = null;
	                 List keyl = new ArrayList();
	                 for(int j = 0; j < spiltTable.length;j++){
	                     if(spiltTable[j].startsWith("$v_")){
	                         mvTable = spiltTable[j].substring(3, spiltTable[j].length());
	                     }else if(spiltTable[j].startsWith("$t_")){
	                         destTable = spiltTable[j].substring(3, spiltTable[j].length());
	                     }else if(spiltTable[j].startsWith("$k_")){
	                         keyl.add(spiltTable[j].substring(3, spiltTable[j].length()));
	                     }                     
	                 }   
	                 if(!"".equals(mvTable) && !"".equals(destTable) && keyl != null && keyl.size() > 0){
	                     keys = new String[keyl.size()];
	                     keys = (String[])keyl.toArray(keys);
	                     TableSyncTask task = new TableSyncTask(mvTable,destTable,keys);
	                     this.taskList.add(task);
	                 }
	            }
            }

			int threadNum = 1;
			if(this.getIntegerPara(SYNC_THREAD_NUM_FLAG) != null){
				threadNum = this.getIntegerPara(SYNC_THREAD_NUM_FLAG).intValue();
			}
			threadNum = Math.max(Math.min(threadNum, 20),1);
			PoolInfo pInfo = new PoolInfo();
			pInfo.taskCapacity = this.taskList.size();
			pInfo.poolType = ThreadPool.SINGLE_TASK_LIST_POOL;
			pInfo.workersNumber = threadNum;
    		ThreadPoolGroupServer.getSingleInstance().createThreadPool(timerName, pInfo);
    		ThreadPoolGroupServer.getSingleInstance().createTimerPool(timerName,false).schedule(new FleetyTimerTask(){
				public void run(){		
				    for(int k = 0; k < taskList.size();k++){
				        ThreadPoolGroupServer.getSingleInstance().getThreadPool(timerName).addTask((TableSyncTask)taskList.get(k));
				    }                      				       
				}
			}, 1000, this.getIntegerPara(SYNC_INTERVAL_FLAG).intValue()*1000l);
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public void stopServer(){
		super.stopServer();
		ThreadPoolGroupServer.getSingleInstance().removeThreadPool(timerName);
		ThreadPoolGroupServer.getSingleInstance().destroyTimerPool(timerName);
		this.taskList.clear();
	}
	
	private class TableSyncTask extends BasicTask{
		private String srcMvLogName = null;
		private String destTableName = null;
		private String[] keys = null;
		
		public TableSyncTask(String srcMvLogName,String destTableName,String[] keys){
			this.srcMvLogName = srcMvLogName;
			this.destTableName = destTableName;
			this.keys = keys;
		}
		public boolean execute() throws Exception{
			String srcDbServerName = DbSyncServer.this.getStringPara(SRC_DB_FLAG);
			String destDbServerName = DbSyncServer.this.getStringPara(DEST_DB_FLAG);
			DbServer dbServer = null;
			DbHandle destConn = null;
			DbServer dbServerYw = null;
			DbHandle srcConn = null;
			
			try{
		        // 业务库
		        dbServerYw = ((DbServer) ServerContainer.getSingleInstance().getServer(srcDbServerName));
		        if(dbServerYw == null){
		        	System.out.println("无法发现名为["+srcDbServerName+"]的业务库数据库连接服务!");
		        	return false;
		        }
		        srcConn = dbServerYw.getConnWithUseTime(0);
		        if(srcConn == null){
		        	System.out.println("无法从名为["+srcDbServerName+"]的业务库数据库连接服务中获取到连接!");
		        	INotifyServer.getSingleInstance().notifyInfo("DbSyncServer Error:","无法从名为["+srcDbServerName+"]的业务库数据库连接服务中获取到连接!",INotifyServer.ERROR_LEVEL);
		        	return false;
		        }
		        
			    // 备份库
		        dbServer = ((DbServer) ServerContainer.getSingleInstance().getServer(destDbServerName));
		        if(dbServer == null){
		        	System.out.println("无法发现名为["+destDbServerName+"]的备份库数据库连接服务!");
		        	return false;
		        }
		        destConn = dbServer.getConnWithUseTime(0);
		        if(destConn == null){
		        	System.out.println("无法从名为["+destDbServerName+"]的备份库数据库连接服务中获取到连接!");
		        	INotifyServer.getSingleInstance().notifyInfo("DbSyncServer Error:","无法从名为["+destDbServerName+"]的备份库数据库连接服务中获取到连接!",INotifyServer.ERROR_LEVEL);
		        	return false;
		        }
		        
		        syncData(srcMvLogName,destTableName,keys,srcConn,destConn);
	        }catch(Exception e){
	        	e.printStackTrace();
	        	INotifyServer.getSingleInstance().notifyInfo("DbSyncServer Exception:",FleetyThread.getStrByException(e),INotifyServer.ERROR_LEVEL);
	        	return false;
	        }finally{
	        	if(dbServer != null){
	        		dbServer.releaseConn(destConn);
	        	}
	        	if(dbServerYw != null){
	        		dbServerYw.releaseConn(srcConn);
	        	}
	        }
			return true;
		}
	}
	

	   private void syncData(String mvLogName,String destTable,String[] keys,DbHandle srcConn,DbHandle destConn) throws Exception{
	        for(int i=0;i<keys.length;i++){
	            keys[i] = keys[i].toUpperCase();
	        }
	        StatementHandle srcStmt = srcConn.createStatement();
	        StatementHandle srcDStmt = srcConn.prepareStatement("delete from "+mvLogName+" where SEQUENCE$$ <= ?");
	        ResultSet srcSets = srcStmt.executeQuery(" select * from "+mvLogName+" order by SEQUENCE$$ asc ");
	        int colNum = srcSets.getMetaData().getColumnCount();
	        int opionalNum = 6; 
	        String colName;
	        
	        String iSql = "insert into "+destTable +" values(";
	        String uSql = "update "+destTable +" set ";
	        String dSql = "delete from "+destTable+" where ";
	        int[] keyTypeArr = new int[keys.length];
	        String[] colNameArr = new String[colNum-opionalNum];
	        int[] colTypeArr = new int[colNum-opionalNum];
	        for(int i=0;i<colNum-opionalNum;i++){
	            if(i > 0){
	                iSql += ",";
	                uSql += ",";
	            }
	            colName = srcSets.getMetaData().getColumnName(i+1);
	            colNameArr[i] = colName;
	            colTypeArr[i] = srcSets.getMetaData().getColumnType(i+1);
	            if(colTypeArr[i] == Types.NUMERIC && srcSets.getMetaData().getScale(i+1) == 0){
	                colTypeArr[i] = Types.INTEGER;
	            }
	            
	            for(int j=0;j<keys.length;j++){
	                if(keys[j].trim().equalsIgnoreCase(colName)){
	                    keyTypeArr[j] = colTypeArr[i];
	                }
	            }

	            iSql += "?";
	            uSql += colName+"=?";
	        }
	        iSql += ")";
	        uSql += " where ";
	        for(int i=0;i<keys.length;i++){
	            if(i > 0){
	                uSql += " and ";
	                dSql += " and ";
	            }
	            uSql += keys[i]+"=?";
	            dSql += keys[i]+"=?";
	        }
	        
	        StatementHandle destIStmt = destConn.prepareStatement(iSql);
	        StatementHandle destUStmt = destConn.prepareStatement(uSql);
	        StatementHandle destDStmt = destConn.prepareStatement(dSql);

	        long curSeq = -1;
	        int count = 0,batchNum = 0;
	        String action;
	        String updateOld;
	        ArrayList dataList = new ArrayList(50);
	        InfoContainer dataInfo,oldKeyValue;
	        int n = 0;
	        HashMap map = new HashMap();
	        HashMap oldKey = new HashMap();
	        while(srcSets.next()){
	            n++;
	            action = srcSets.getString("DMLTYPE$$");
	            updateOld = srcSets.getString("OLD_NEW$$");
	            if(this.isPrint){
	            	System.out.println(destTable+"all seq,action,keys"+"sequence:"+new Long(srcSets.getLong("SEQUENCE$$"))+",action:"+action+",keys"+getKeys(srcSets,keys,keyTypeArr));
	            }
	            if(action == null){
	                System.out.println(destTable+"特别注意：action is null:"+new Long(srcSets.getLong("SEQUENCE$$")));
	                continue;
	            }
	            action = action.toUpperCase();

	            //如果是旧更新对象，把主键的值信息放置在InfoContainer中
	            if(action.equalsIgnoreCase("U") && !updateOld.equalsIgnoreCase("N")){
	                oldKeyValue = new InfoContainer();
	                this.initDataInfo(srcSets, oldKeyValue, keys, keyTypeArr);
	                oldKey.put(srcSets.getString("M_ROW$$"), oldKeyValue);
	                continue;
	            }

	            count ++;
	            dataInfo = new InfoContainer();
	            dataInfo.setInfo("SEQUENCE$$", new Long(srcSets.getLong("SEQUENCE$$")));
	            dataInfo.setInfo("DMLTYPE$$", action);
	            this.initDataInfo(srcSets,dataInfo,colNameArr,colTypeArr);
	            
	            //如果是新更新对象，需要设置其条件keys来源于旧对象
	            oldKeyValue = null;
	            if(action.equalsIgnoreCase("U") && updateOld.equalsIgnoreCase("N")){
	                oldKeyValue = (InfoContainer)oldKey.remove(srcSets.getString("M_ROW$$"));
	            }
	            String keyNst="";
	            String keyOst="";
	            for(int i=0;i<keys.length;i++){
	                if(oldKeyValue == null){
	                    dataInfo.setInfo(this.getFieldNameAsKey(keys[i]), dataInfo.getInfo(keys[i]));
	                }else{
	                    dataInfo.setInfo(this.getFieldNameAsKey(keys[i]), oldKeyValue.getInfo(keys[i]));
	                    if(i > 0){
	                        keyNst+="\t";
	                        keyOst+="\t";
	                    }
	                    keyNst+=dataInfo.getInfo(keys[i]);
	                    keyOst+=oldKeyValue.getInfo(keys[i]);
	                }
	            }
	            if(!keyNst.equals(keyOst)){
	            	if(this.isPrint){
	            		System.out.println(destTable+"特别注意：new key is not old:"+keyOst+","+keyNst);
	                }
	                map.put(keyNst, null);
	            }
	            //filter决定是否需要同步
	            if(!this.filter.isSync(mvLogName,dataInfo)){
	                continue;
	            }
	            
	            dataList.add(dataInfo);
	            
	            if((count % 50) != 0){
	                continue;
	            }
	            
	            
	            curSeq = Math.max(curSeq,this.disposeDataList(destTable,destConn,destIStmt,destUStmt,destDStmt,dataList,keys,keyTypeArr,colNameArr,colTypeArr,map));

	            batchNum++;
	            if((batchNum % 10) == 0){
	                this.submitData(destIStmt, destUStmt, destDStmt);
	                map = new HashMap();
	                srcDStmt.setLong(1, curSeq);
	                srcDStmt.execute();
	                curSeq = -1;
	            }
	        }
	        
	         curSeq = Math.max(curSeq,this.disposeDataList(destTable,destConn,destIStmt,destUStmt,destDStmt,dataList,keys,keyTypeArr,colNameArr,colTypeArr,map));
	         if(curSeq > 0){
	            this.submitData(destIStmt, destUStmt, destDStmt);
	            map = new HashMap();
	            srcDStmt.setLong(1, curSeq);
	            srcDStmt.execute();
	        }
 
	        System.out.println("SyncNum="+n);
	    }
	    
	    private void submitData(StatementHandle destIStmt,StatementHandle destUStmt,StatementHandle destDStmt) throws Exception{
	        destIStmt.executeBatch();
	        destUStmt.executeBatch();
	        destDStmt.executeBatch();
	    }
	    
	    private long disposeDataList(String destTableName,DbHandle destConn,StatementHandle destIStmt,StatementHandle destUStmt,StatementHandle destDStmt,ArrayList dataList,String[] keys,int[] keyTypeArr,String[] colNameArr,int[] colTypeArr,HashMap mapping) throws Exception{
	        if(dataList.size() == 0){
	            return -1;
	        }
	        InfoContainer dataInfo;

	        StringBuffer sql = new StringBuffer(1024);
	        sql.append("select ");
	        for(int i=0;i<keys.length;i++){
	            if(i > 0){
	                sql.append(",");
	            }
	            sql.append(keys[i]);
	        }
	        sql.append(" from ");
	        sql.append(destTableName);
	        sql.append(" where ");

	        for(int i=0;i<dataList.size();i++){
	            dataInfo = (InfoContainer)dataList.get(i);
	            if(i > 0){
	                sql.append(" or ");
	            }
	            sql.append("(");
	            for(int j=0;j<keys.length;j++){
	                if(j > 0){
	                    sql.append(" and ");
	                }
	                sql.append(keys[j]);
	                sql.append("= ? ");
	            }
	            sql.append(")");
	        }
	        
	        String keyStr;
	        StatementHandle destQStmt = destConn.prepareStatement(sql.toString());
	        int num = 0;
	        for(int i=0;i<dataList.size();i++){
	            dataInfo = (InfoContainer)dataList.get(i);
	            this.setParaInfo(num, keys, keyTypeArr, destQStmt, dataInfo);
	            num = num+keys.length;
	        }
	        ResultSet qSets = destQStmt.executeQuery();
	        
	        while(qSets.next()){
	            keyStr = qSets.getString(keys[0]);
	            for(int j=1;j<keys.length;j++){
	                keyStr += "\t"+qSets.getString(keys[j]);
	            }
	            mapping.put(keyStr, null);
	        }
	        destQStmt.close();
	        qSets.close();
	        
	        String action;
	        long curSeq = -1;
	        boolean isUpdate;
	        for(int i=0;i<dataList.size();i++){
	            dataInfo = (InfoContainer)dataList.get(i);
	            curSeq = dataInfo.getLong("SEQUENCE$$").longValue();
	            action = dataInfo.getString("DMLTYPE$$");

	            if(action.equals("D")){
	            	//下面是作为条件的主键条件设置,虽然不管如何，值不会有差异，但它的确是作为条件的主键key值
	                this.setParaInfo(0, keys, keyTypeArr, destDStmt, dataInfo, true);
	                destDStmt.addBatch();
	                keyStr = dataInfo.getString(keys[0]);
	                for(int j=1;j<keys.length;j++){
	                    keyStr += "\t"+dataInfo.getString(keys[j]);
	                }
	                mapping.remove(keyStr);
	                if(this.isPrint){
	                	System.out.println("删除---------"+keyStr);
	                }
	                
	                this.submitData(destIStmt, destUStmt, destDStmt);
	            }else{
	                isUpdate = false;
	                keyStr = dataInfo.getString(keys[0]);
	                for(int j=1;j<keys.length;j++){
	                    keyStr += "\t"+dataInfo.getString(keys[j]);
	                }
	                if(mapping.containsKey(keyStr)){
	                    isUpdate = true;
	                }else{
	                    mapping.put(keyStr, null);
	                }
	                
	                if(isUpdate){
	                    this.setParaInfo(0, colNameArr, colTypeArr, destUStmt, dataInfo);
	                    //下面是作为条件的主键条件设置
	                    this.setParaInfo(colNameArr.length, keys, keyTypeArr, destUStmt, dataInfo, true);
	                    destUStmt.addBatch();
	                    if(this.isPrint){
	                    	System.out.println("更新---------"+keyStr);
	                    }
	                }else{
	                    this.setParaInfo(0, colNameArr, colTypeArr, destIStmt, dataInfo);
	                    destIStmt.addBatch();
	                    if(this.isPrint){
	                    	System.out.println("插入---------"+keyStr);
	                    }
	                }
	            }
	        }
	        dataList.clear();
	        return curSeq;
	    }
	    
	    private void setParaInfo(int offsetIndex,String[] keys,int[] keyTypeArr,StatementHandle stmt,InfoContainer dataInfo) throws Exception{
	        this.setParaInfo(offsetIndex, keys, keyTypeArr, stmt, dataInfo, false);
	    }
	    private void setParaInfo(int offsetIndex,String[] keys,int[] keyTypeArr,StatementHandle stmt,InfoContainer dataInfo,boolean isKey) throws Exception{
	        String key;
	        for(int i=0;i<keys.length;i++){
	            //如果是作为条件的主键，需要通过主键的额外类型key获取对象。
	            if(isKey){
	                key = this.getFieldNameAsKey(keys[i]);
	            }else{
	                key = keys[i];
	            }
	            
	            switch(keyTypeArr[i]){
	                case Types.BIT:
	                case Types.SMALLINT:
	                case Types.INTEGER:
	                case Types.TINYINT:
	                    stmt.setInt(offsetIndex+i+1, dataInfo.getInteger(key).intValue());
	                    break;
	                case Types.CHAR:
	                case Types.VARCHAR:
	                    stmt.setString(offsetIndex+i+1, dataInfo.getString(key));
	                    break;
	                case Types.DATE:
	                case Types.TIMESTAMP:
	                    stmt.setTimestamp(offsetIndex+i+1, (Timestamp)dataInfo.getInfo(key));
	                    break;
	                case Types.FLOAT:
	                    stmt.setFloat(offsetIndex+i+1, ((Float)dataInfo.getInfo(key)).floatValue());
	                    break;
	                case Types.DOUBLE:
	                case Types.NUMERIC:
	                    stmt.setDouble(offsetIndex+i+1, ((Double)dataInfo.getInfo(key)).doubleValue());
	                    break;
	                default:
	                    stmt.setString(offsetIndex+i+1, null);
	            }
	        }
	    }
	    
	    private void initDataInfo(ResultSet sets,InfoContainer dataInfo,String[] keys,int[] keyTypeArr) throws Exception{
	        for(int i=0;i<keys.length;i++){
	            switch(keyTypeArr[i]){
	                case Types.BIT:
	                case Types.SMALLINT:
	                case Types.INTEGER:
	                case Types.TINYINT:
	                    dataInfo.setInfo(keys[i],new Integer(sets.getInt(keys[i])));
	                    break;
	                case Types.CHAR:
	                case Types.VARCHAR:
	                    dataInfo.setInfo(keys[i],sets.getString(keys[i]));
	                    break;
	                case Types.DATE:
	                case Types.TIMESTAMP:
	                    dataInfo.setInfo(keys[i],sets.getTimestamp(keys[i]));
	                    break;
	                case Types.FLOAT:
	                    dataInfo.setInfo(keys[i],new Float(sets.getFloat(keys[i])));
	                    break;
	                case Types.DOUBLE:
	                case Types.NUMERIC:
	                    dataInfo.setInfo(keys[i],new Double(sets.getDouble(keys[i])));
	                    break;
	            }
	        }
	    }
	    private String getFieldNameAsKey(String fieldName){
	        return "KEY\t"+fieldName;
	    }
	    
	    private String getKeys(ResultSet sets,String[] keys,int[] keyTypeArr) throws Exception{
	        String key ="";
	        for(int i=0;i<keys.length;i++){
	            try{
	            switch(keyTypeArr[i]){
	                case Types.BIT:
	                case Types.SMALLINT:
	                case Types.INTEGER:
	                case Types.TINYINT:
	                    key+=new Integer(sets.getInt(keys[i]));
	                    break;
	                case Types.CHAR:
	                case Types.VARCHAR:
	                    key+=sets.getString(keys[i]);
	                    break;
	                case Types.DATE:
	                case Types.TIMESTAMP:
	                    key+=sets.getTimestamp(keys[i]);
	                    break;
	                case Types.FLOAT:
	                    key+=new Float(sets.getFloat(keys[i]));
	                    break;
	                case Types.DOUBLE:
	                case Types.NUMERIC:
	                    key+=new Double(sets.getDouble(keys[i]));
	                    break;
	            }
	            if(i < keys.length-1){
	                key+=",";
	            }
	            }catch(Exception e){
	                System.out.println(keys[i]);
	                throw e;
	            }
	        }
	        return key;
	    }
}
