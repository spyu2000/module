package server.flow.task.sql;

import java.sql.Timestamp;

import com.fleety.util.pool.db.DbConnPool.DbHandle;
import com.fleety.util.pool.db.DbConnPool.StatementHandle;

import server.flow.IFlow;

public class TaskSql implements IFlow{
	private String sql = null;
	private String psql = null;
	private SqlParaInfo[] paraInfo = null;
	
	public TaskSql(String sql){
		this(sql,null,null);
	}
	public TaskSql(String psql,SqlParaInfo[] paraInfo){
		this(null,psql,paraInfo);
	}
	public TaskSql(String sql,String psql,SqlParaInfo[] paraInfo){
		this.sql = sql;
	}
	
	public void setSql(String sql){
		this.sql = sql;
	}
	
	public String getSql(){
		return this.sql;
	}
	
	public void execute(DbHandle conn) throws Exception{
		if(this.psql == null){
			return ;
		}
		
		SqlParaInfo pInfo;
		StatementHandle stmt = conn.prepareStatement(this.psql);
		for(int i=0;i<this.paraInfo.length;i++){
			pInfo = this.paraInfo[i];

			if(pInfo.isInt()){
				stmt.setInt(i+1, ((Integer)pInfo.getValue()).intValue());
			}else if(pInfo.isBoolean()){
				stmt.setBoolean(i+1, ((Boolean)pInfo.getValue()).booleanValue());
			}else if(pInfo.isDouble()){
				stmt.setDouble(i+1, ((Double)pInfo.getValue()).doubleValue());
			}else if(pInfo.isString()){
				stmt.setString(i+1, (String)pInfo.getValue());
			}else if(pInfo.isTimestamp()){
				stmt.setTimestamp(i+1, (Timestamp)pInfo.getValue());
			}else{
				throw new Exception("Error Parameter Type");
			}
		}
		stmt.execute();
	}
}
