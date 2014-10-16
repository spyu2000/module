/**
 * ¾øÃÜ Created on 2010-1-6 by edmund
 */
package test;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import com.fleety.base.InfoContainer;
import com.fleety.track.TrackFilter;
import com.fleety.track.TrackIO;

public class SearchLastReportTime
{
	/**
	 * @param args
	 */
	public static void main(String[] args){
		try{
			File tDir = new File("/home/fleety/iflow/map/track/TRK"+sdf.format(new Date())+"/");
			File[] fs = tDir.listFiles(new FileFilter(){
				public boolean accept(File f){
					return f.getName().endsWith(".LOG");
				}
				public String getDescription(){
					return null;
				}
			});
			
			HashMap mapping = new HashMap(5000);
			String name;
			for(int i=0;i<fs.length;i++){
				name = fs[i].getName();
				name = name.replaceAll(".LOG", "");
				mapping.put(name, null);
			}
			
			
			long time = System.currentTimeMillis() - 24*60*60*1000l;
			
			for(int i=0;i<31;i++){
				dispose(time,mapping);
				time -= 24*60*60*1000l;
			}
			
			HashMap mdtMapping = new HashMap(4000);
			Class.forName("oracle.jdbc.driver.OracleDriver");
			Connection conn = DriverManager.getConnection("jdbc:oracle:thin:@222.73.85.40:1521:fleety", "iflow", "4iflow098");
			Statement stmt = conn.createStatement();
			ResultSet sets = stmt.executeQuery("select mdt_id,taxi_no from taxi_info");
			while(sets.next()){
				mdtMapping.put(sets.getString("taxi_no"), sets.getString("mdt_id"));
			}
			conn.close();
			
			String destNo,mdtId;
			for(Iterator itr = resultMapping.keySet().iterator();itr.hasNext();){
				destNo = itr.next().toString();
				
				mdtId = (String)mdtMapping.get(destNo);
				if(mdtId == null){
					continue;
				}
				
				System.out.println(mdtId+","+resultMapping.get(destNo));
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	private static HashMap resultMapping = new HashMap();
	private static String name;
	private static int count = 0;
	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
	private static void dispose(long time,HashMap mapping) throws Exception{
		String dir = "/home/fleety/iflow/map/track/TRK"+sdf.format(new Date(time));
		
		File tDir = new File(dir);
		File[] fs = tDir.listFiles(new FileFilter(){
			public boolean accept(File f){
				return f.getName().endsWith(".LOG");
			}
			public String getDescription(){
				return null;
			}
		});
		
		TrackIO track = new TrackIO();
		for(int i=0;i<fs.length;i++){
			name = fs[i].getName();
			name = name.replaceAll(".LOG", "");
			if(mapping.containsKey(name)){
				continue;
			}
			
			count = 0;
			FileInputStream in = new FileInputStream(fs[i]);
			track.readTrackRecord(in, new TrackFilter(){
				public int filterTrack(InfoContainer info){
					String kilo = info.getString(TrackIO.DEST_KILO_FLAG);
					resultMapping.put(name,
									info.getString(TrackIO.DEST_LO_FLAG)
											+ ","
											+ info.getString(TrackIO.DEST_LA_FLAG)
											+ ","+ info.getDate(TrackIO.DEST_TIME_FLAG).getTime()
											+ ","
											+ info.getString(TrackIO.DEST_STATUS_FLAG)
											+ ","
											+ info.getString(TrackIO.DEST_LOCATE_FLAG)
											+ ","
											+ (kilo == null?"-1":kilo)
											+ ","
											+ (kilo == null?"false":"true")
											);
					count ++;
					return TrackFilter.IGNORE_FLAG;
				}
			});

			if(count > 0){
				mapping.put(name, null);
			}
		}
	}
}
