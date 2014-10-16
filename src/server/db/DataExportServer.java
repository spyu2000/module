package server.db;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.sql.ResultSet;

import com.fleety.server.BasicServer;
import com.fleety.util.pool.db.DbConnPool.DbHandle;
import com.fleety.util.pool.db.DbConnPool.StatementHandle;

public class DataExportServer extends BasicServer {

	public boolean startServer() {
		String sql = this.getStringPara("sql");

		PrintStream fout = System.out;
		String filePath = this.getStringPara("file_name");
		
		DbHandle conn = DbServer.getSingleInstance().getConn();
		try{
			if(filePath != null && filePath.trim().length() > 0){
				fout = new PrintStream(new BufferedOutputStream(new FileOutputStream(new File(filePath.trim()))));
			}
			System.out.println("Start Export Query!");
			StringBuffer strBuff = new StringBuffer(10240);
			StatementHandle stmt = conn.prepareStatement(sql);
			ResultSet sets = stmt.executeQuery();
			int colCount = sets.getMetaData().getColumnCount();
			for(int i=1;i<=colCount;i++){
				if(i>1){
					strBuff.append("\t");
				}
				strBuff.append(sets.getMetaData().getColumnName(i));
			}
			fout.println(strBuff);
			System.out.println("Process: 0");
			int index = 0;
			while(sets.next()){
				strBuff.delete(0,strBuff.length());
				index++;
				for(int i=1;i<=colCount;i++){
					if(i>1){
						strBuff.append("\t");
					}
					strBuff.append(sets.getString(i));
				}
				fout.println(strBuff);
				

				if((index % 1000) == 0){
					System.out.println("Process: "+index);
				}
			}
			
			fout.close();
			this.isRunning = true;
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			DbServer.getSingleInstance().releaseConn(conn);
		}
		
		
		return this.isRunning();
	}
}
