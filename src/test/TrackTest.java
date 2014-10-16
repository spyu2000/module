/**
 * 绝密 Created on 2008-4-23 by edmund
 */
package test;

import java.awt.Rectangle;
import java.io.*;
import java.nio.ByteBuffer;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import server.track.TrackServer;
import server.track.TrackServer;
import com.fleety.base.GeneralConst;
import com.fleety.base.InfoContainer;
import com.fleety.track.ModifyFilter;
import com.fleety.track.TrackFilter;
import com.fleety.track.TrackIO;

public class TrackTest{
	public static void main(String[] argv){
		try{
			
//			NewTrackServer.getSingleInstance().getTrackFile("沪AAA", new Date(), TrackIO.VERSION_ICLOUD_FLAG, "");
			
			InfoContainer info=new InfoContainer();			
			info.setInfo(TrackServer.DEST_NO_FLAG, "沪AAA");
			info.setInfo(TrackServer.COMPANY_ID_FLAG, "1111");
			info.setInfo(TrackIO.DEST_LO_FLAG, new Float(121.11));
			info.setInfo(TrackIO.DEST_LA_FLAG, new Float(21.11));
			info.setInfo(TrackIO.DEST_TIME_FLAG, new Date(System.currentTimeMillis()));
			info.setInfo(TrackIO.DEST_STATUS_FLAG, new Integer(1));
			info.setInfo(TrackIO.DEST_ALARM_TYPE_FLAG, new Integer("0"));
			info.setInfo(TrackIO.DEST_SPEED_FLAG, new Integer(100));
			info.setInfo(TrackIO.DEST_DIRECTION_FLAG, new Integer(1));
			info.setInfo(TrackIO.DEST_OIL_FLAG, new Integer("0"));
			info.setInfo(TrackIO.DEST_KILO_FLAG, new Integer(Math.round(Float.parseFloat("10"))));
	
			TrackServer.getSingleInstance().addTrackInfo(info);
			
			TrackServer.getSingleInstance().addTrackInfo(info);
//			System.out.println(GeneralConst.YYYY_MM_DD_HH_MM_SS.format(new Date(0x011AC8A318BEl)));
//			System.out.println(Long.toHexString(GeneralConst.YYYY_MM_DD_HH_MM_SS.parse("2008-06-27 14:08:29").getTime()));
//			new TrackTest().testRead();
//			new TrackTest().testInOut(argv);
			System.out.println("run");
			TrackServer.getSingleInstance().addPara("disk_volume", "/home/fleety/iflow/xjs/data");
			TrackServer.getSingleInstance().addPara("path", "TRACK");
			TrackServer.getSingleInstance().addPara("volume_count", "6");
			TrackServer.getSingleInstance().addPara("track_class_name", "server.track.BufferTrack");
//			TrackServer.getSingleInstance().addPara("path", "c:/");
			TrackServer.getSingleInstance().addPara("auto_zip", "true");
			TrackServer.getSingleInstance().startServer();
			
			InfoContainer[] infoArr = TrackServer.getSingleInstance().getTrackInfo(
					new InfoContainer().setInfo(TrackServer.START_DATE_FLAG, new Date(System.currentTimeMillis()-1000*GeneralConst.ONE_DAY_TIME))
								.setInfo(TrackServer.END_DATE_FLAG, new Date())
								.setInfo(TrackServer.DEST_NO_FLAG, "粤B13G88")
					, new TrackFilter(){
						public int filterTrack(InfoContainer info){
//							System.out.println(GeneralConst.YYYY_MM_DD_HH_MM_SS.format(info.getDate(TrackIO.DEST_TIME_FLAG)));
							return TrackFilter.CONTINUE_FLAG;
						}
					});
			System.out.println("result Num:"+infoArr.length);
			
			Thread.sleep(10*60*1000);
//
//
//			Date startDate = GeneralConst.YYYY_MM_DD.parse(GeneralConst.YYYY_MM_DD.format(new Date(System.currentTimeMillis()-GeneralConst.ONE_DAY_TIME)));
//			Date endDate = GeneralConst.YYYY_MM_DD.parse(GeneralConst.YYYY_MM_DD.format(new Date()));
//			
//			System.out.println(new TrackTest().testDistanceCount(0, "J84-川B09255", startDate, endDate));
			
			if(false){
				RandomAccessFile channel = new RandomAccessFile(new File("c:/沪A-EM888.LOG"),"rw");
				TrackIO trackIO = new TrackIO();
				trackIO.modifyTrackRecord(channel, new ModifyFilter(){
					public void filter(RandomAccessFile accessFile,int infoFlag) throws Exception{
						if(infoFlag == TrackIO.DEST_TEMP2_FLAG.intValue()){
							
							accessFile.writeShort((int)Math.round((Math.random()*20 - 10)*100));
						}
					}
				});
			}
			
			if(true)return ;
			
			File dir = new File("D:/xjs/guangming");
			File[] dirArr = dir.listFiles(new FileFilter(){
				public boolean accept(File pathname){
					return pathname.isDirectory();
				}
			});
			
			File[] fArr;
			for(int i=0;i<dirArr.length;i++){
				fArr = dirArr[i].listFiles(new FileFilter(){
					public boolean accept(File pathname){
						if(pathname.isDirectory()){
							return false;
						}
						return pathname.getName().endsWith(".LOG");
					}
				});
				for(int j=0;j<fArr.length;j++){
					RandomAccessFile channel = new RandomAccessFile(fArr[j],"rw");
					TrackIO trackIO = new TrackIO();
					trackIO.modifyTrackRecord(channel, new ModifyFilter(){
						public void filter(RandomAccessFile accessFile,int infoFlag) throws Exception{
							if(infoFlag == TrackIO.DEST_TEMP1_FLAG.intValue()
									||infoFlag == TrackIO.DEST_TEMP2_FLAG.intValue()
									||infoFlag == TrackIO.DEST_TEMP3_FLAG.intValue()
									||infoFlag == TrackIO.DEST_TEMP4_FLAG.intValue()
									||infoFlag == TrackIO.DEST_TEMP5_FLAG.intValue()
									||infoFlag == TrackIO.DEST_TEMP6_FLAG.intValue()){
								int tmp = accessFile.readShort();
								if((tmp/100) >= 100){
									accessFile.seek(accessFile.getFilePointer()-2);
									tmp=-(tmp%1000);
									accessFile.writeShort(tmp);
								}
							}
						}
					});
					channel.close();
					System.out.println("finish:"+fArr[j].getAbsolutePath());
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	private int testDistanceCount(int mdtId,String carNo,Date startDate,Date endDate){		
		InfoContainer queryInfo = new InfoContainer();
		queryInfo.setInfo(TrackServer.DEST_NO_FLAG, carNo);
		queryInfo.setInfo(TrackServer.START_DATE_FLAG, startDate);
		queryInfo.setInfo(TrackServer.END_DATE_FLAG, endDate);
		
		DistanceFilter filter = new DistanceFilter(){			
			public int filterTrack(InfoContainer info){
				int mile = info.getInteger(TrackIO.DEST_KILO_FLAG).intValue();
				if(mile > 0){
					this.exist = true;
					
					if(mile > max){
						max = mile;
					}
					
					if(mile < min){
						min = mile;
					}
				}
				return TrackFilter.IGNORE_FLAG;
			}
		};
		TrackServer.getSingleInstance().getTrackInfo(queryInfo,filter);
		
		return filter.getMileAge();
	}

	int max=0,min=Integer.MAX_VALUE;
	Rectangle rectRegion = new Rectangle(106585800,29617100,6000,7000);
	boolean isIn = false;
	public void testRead(){		
		try{			
//			InputStream in = new FileInputStream("D:/xjs/guangming/5.1/沪B87272.LOG");

			InputStream in = new FileInputStream("E:\\workspace(Iflow)\\tianditong\\track\\TRK20091120\\沪A-C2876.LOG");

			
			TrackIO trackIO = new TrackIO();
			System.out.println("开始");

			
			
			Object[] arr = trackIO.readTrackRecord(in,null).toArray();
			boolean isFirst = true;
			float preLo = -1,preLa = -1,lo,la;
			double preDis = 0,curDis;
			int locate;
			Object temp;
			InfoContainer info;
			for(int i=0;i<arr.length;i++){
				info = (InfoContainer)arr[i];

				temp = info.getInteger(TrackIO.DEST_LOCATE_FLAG);
				locate = temp == null?0:(int)(((Integer)temp).intValue());
				
				temp = info.getDouble(TrackIO.DEST_LO_FLAG);
				lo = temp == null?0f:(float)(((Double)temp).doubleValue());
				
				temp = info.getDouble(TrackIO.DEST_LA_FLAG);
				la = temp == null?0f:(float)(((Double)temp).doubleValue());

				temp = info.getDouble(TrackIO.DEST_KILO_FLAG);
				curDis = temp == null?0f:(float)(((Double)temp).doubleValue());
				if(i == 792){
					System.out.println("ffff");
				}
				if(locate == 0){
					if(curDis <= 0){
						if(!isFirst){
							double tempDistance = Math.round(1000*Math.sqrt(Math.pow((lo-preLo)/0.010506, 2)+Math.pow((la-preLa)/0.009, 2)))/1000.0;
							if(tempDistance < 20){
								curDis = preDis + tempDistance;
							} else{
								curDis = preDis;
							}
						}else{
							curDis = preDis;
						}
					}

					isFirst = false;
					preLo = lo;
					preLa = la;
				}else{
					curDis = preDis;
				}
				
				
				preDis = curDis;
				
				System.out.println(i+","+curDis);
			}
			
			
//			System.setOut(new PrintStream(new FileOutputStream("c:/xjs.txt")));

//			System.setOut(new PrintStream(new FileOutputStream("c:/粤BFM049.LOG")));

			List trackList = trackIO.readTrackRecord(in,new TrackFilter(){
				int count = 0;
				public int filterTrack(InfoContainer info){

//					System.out.print(GeneralConst.YYYY_MM_DD_HH_MM_SS.format(info.getDate(TrackIO.DEST_TIME_FLAG))+"  ");
//					System.out.print(info.getString(TrackIO.DEST_LO_FLAG)+",");
//					System.out.print(info.getString(TrackIO.DEST_LA_FLAG)+",");

					System.out.print(GeneralConst.YYYY_MM_DD_HH_MM_SS.format(info.getDate(TrackIO.DEST_TIME_FLAG))+"  ");
					System.out.print(info.getString(TrackIO.DEST_LO_FLAG)+" ");
					System.out.print(info.getString(TrackIO.DEST_LA_FLAG)+" ");
					System.out.print(info.getString(TrackIO.EQUIPMENT_STATUS_FLAG)+" ");
//                    System.out.print(info.getString(TrackIO.DEST_ALARM_TYPE_FLAG)+"tt ");

//					System.out.print(info.getString(TrackIO.DEST_SPEED_FLAG)+" ");
					
					long speed = info.getLong(TrackIO.DEST_SPEED_FLAG).longValue();
					if(speed < 30){
						return TrackFilter.IGNORE_FLAG;
					}
//					System.out.print(info.getString(TrackIO.RECORD_TYPE_FLAG)+" ");
//					System.out.print(info.getString(TrackIO.DEST_PEOPLE_NUM_FLAG)+" ");
//					System.out.print(info.getString(TrackIO.DEST_IN_OUT_NUM_FLAG)+" ");
//					System.out.print(info.getString(TrackIO.DEST_OIL_FLAG)+" ");
//					System.out.print(GeneralConst.YYYY_MM_DD_HH_MM_SS.format(info.getDate(TrackIO.DEST_RECORD_TIME_FLAG))+" ");
//					System.out.print(info.getString(TrackIO.DEST_PEOPLE_NUM_FLAG)+" ");
//					Integer inout = info.getInteger(TrackIO.DEST_IN_OUT_NUM_FLAG);
//					System.out.print((inout!=null?Integer.toHexString(inout.intValue()):"")+" ");
//					System.out.println();

//					int lo = (int)(info.getDouble(TrackIO.DEST_LO_FLAG).doubleValue()*1000000);
//					int la = (int)(info.getDouble(TrackIO.DEST_LA_FLAG).doubleValue()*1000000);
//					
//					boolean isContain = rectRegion.contains(lo,la);
//					if(isIn){
//						if(!isContain){
//							System.out.println(GeneralConst.YYYY_MM_DD_HH_MM_SS.format(info.getDate(TrackIO.DEST_TIME_FLAG))+"  ");
//							return TrackFilter.BREAK_FLAG;
//						}
//					}else if(isContain){
//						System.out.println(GeneralConst.YYYY_MM_DD_HH_MM_SS.format(info.getDate(TrackIO.DEST_TIME_FLAG))+"  ");
//						isIn = true;
//					}
					
					
//					System.out.println(info.getString(TrackIO.DEST_STATUS_FLAG)+" "+info.getString(TrackIO.DEST_ALARM_TYPE_FLAG));
//					Integer kilo = info.getInteger(TrackIO.DEST_KILO_FLAG);
//					System.out.println(kilo+" ");
					
					count ++;
					
//					if(kilo.intValue() > 0){
//						if(max < kilo.intValue()){
//							max = kilo.intValue();
//						}
//						if(min > kilo.intValue()){
//							min = kilo.intValue();
//						}
//					}
					
					return TrackFilter.CONTINUE_FLAG;
				}
			});
			
			for(Iterator itr = trackList.iterator();itr.hasNext();){
				System.out.println(((InfoContainer)itr.next()).getString(TrackIO.DEST_SPEED_FLAG));
			}
//			System.out.close();
			System.out.println(max+" "+min+" "+(max-min));
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	
	public void readTrack() throws Exception{
		InputStream in = new FileInputStream("c:/粤BFM049.LOG");
		while(true){
			ByteBuffer buff = ByteBuffer.allocate(40);
			int count = in.read(buff.array());
			
			if(count != 40){
				return;
			}
			
			InfoContainer info = new InfoContainer();
			buff.position(0);
			addFloat(buff, info, TrackIO.DEST_LO_FLAG);
			addFloat(buff, info, TrackIO.DEST_LA_FLAG);
			addDate(buff, info, TrackIO.DEST_TIME_FLAG);
			buff.get();
			
			int status = buff.get()&0xFF;
			info.setInfo(TrackIO.DEST_STATUS_FLAG, new Integer(status&0x07));
			info.setInfo(TrackIO.DEST_ALARM_TYPE_FLAG, new Integer((status&0xF8)>>3));
			
			addUnshort(buff, info, TrackIO.DEST_SPEED_FLAG);
			addUnshort(buff, info, TrackIO.DEST_DIRECTION_FLAG);
		
			addInt(buff, info, TrackIO.DEST_KILO_FLAG);
			

			addShort(buff, info, TrackIO.DEST_TEMP1_FLAG);
			addShort(buff, info, TrackIO.DEST_TEMP2_FLAG);
			addShort(buff, info, TrackIO.DEST_TEMP3_FLAG);
			addShort(buff, info, TrackIO.DEST_TEMP4_FLAG);
			addShort(buff, info, TrackIO.DEST_TEMP5_FLAG);
			addShort(buff, info, TrackIO.DEST_TEMP6_FLAG);
			
			addShort(buff, info, TrackIO.DEST_OIL_FLAG);
			
			System.out.print(GeneralConst.YYYY_MM_DD_HH_MM_SS.format(info.getDate(TrackIO.DEST_TIME_FLAG))+"  ");
			System.out.print(info.getString(TrackIO.DEST_LO_FLAG)+" ");
			System.out.print(info.getString(TrackIO.DEST_LA_FLAG)+" ");
			System.out.print(info.getString(TrackIO.DEST_ALARM_TYPE_FLAG)+"tt ");
			System.out.print(info.getString(TrackIO.DEST_KILO_FLAG)+"\n");
		}
	}

	
	private static final void addFloat(ByteBuffer buff,InfoContainer info,Object key){
		float f = buff.getFloat();
		info.setInfo(key, new Float(f));
	}
	
	private static final void addByte(ByteBuffer buff,InfoContainer info,Object key){
		int i = buff.get()&0xFF;
		info.setInfo(key, new Integer(i));
	}
	
	private static final void addShort(ByteBuffer buff,InfoContainer info,Object key){
		int i = buff.getShort();
		info.setInfo(key, new Integer(i));
	}
	
	private static final void addUnshort(ByteBuffer buff,InfoContainer info,Object key){
		int i = buff.getShort()&0xFFFF;
		info.setInfo(key, new Integer(i));
	}
	
	private static final void addInt(ByteBuffer buff,InfoContainer info,Object key){
		int i = buff.getInt();
		info.setInfo(key, new Integer(i));
	}
	
	private static final void addByteArr(ByteBuffer buff,int arrLen,InfoContainer info,Object key){
		byte[] byteArr = new byte[arrLen];
		buff.get(byteArr);
		info.setInfo(key, byteArr);
	}
	
	private static final void addDate(ByteBuffer buff,InfoContainer info,Object key){
		long l = buff.getLong();
		info.setInfo(key, new Date(l));
	}
	
	public void testInOut(String[] argv){
		if(argv.length < 2){
			System.out.println("确认参数，输入文件夹以及输出文件夹!");
			return ;
		}
		
		File inDir = new File(argv[0]);
		File outDir = new File(argv[1]);
		
		TrackIO action = new TrackIO();
		
		File[] fs = inDir.listFiles(new FileFilter(){
			public boolean accept(File pathname){
				return pathname.getName().endsWith(".LOG");
			}
		});
		
		File f = null;
		String outPath;
		String destPath ;
		for(int i=0;i<fs.length;i++){
			f = fs[i];
			destPath = outDir.getAbsolutePath()+File.separator+inDir.getName()+"_visible"+File.separator;
			new File(destPath).mkdirs();
			
			try{
				String[] vs = action.testVersion(f);
				if(vs == null){
					continue;
				}
				if(vs[0] == null){
					action.setRecordDataLen(Integer.parseInt(vs[1],10));
				}
				byte[] data = new byte[(int)f.length()];
				FileInputStream fin = new FileInputStream(f);
				fin.read(data);
				fin.close();
				
				ByteArrayInputStream in = new ByteArrayInputStream(data);
				List trackList = action.readTrackRecord(in);
				InfoContainer[] records = new InfoContainer[trackList.size()];
				trackList.toArray(records);
				
				outPath = destPath + f.getName();
				BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(outPath));
				action.writeTrackRecord(out, records, TrackIO.VERSION_255_FLAG,
						new Integer[]{TrackIO.DEST_LO_FLAG
									,TrackIO.DEST_LA_FLAG
									,TrackIO.DEST_TIME_FLAG
									,TrackIO.DEST_SPEED_FLAG
									,TrackIO.DEST_STATUS_FLAG
									,TrackIO.DEST_KILO_FLAG
									,TrackIO.DEST_OIL_FLAG
									,TrackIO.DEST_DIRECTION_FLAG
									,TrackIO.DEST_TEMP1_FLAG
									,TrackIO.DEST_TEMP2_FLAG
									,TrackIO.DEST_TEMP3_FLAG
									,TrackIO.DEST_TEMP4_FLAG
									,TrackIO.DEST_TEMP5_FLAG
									,TrackIO.DEST_TEMP6_FLAG
									}
						);
				out.close();
				
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}
	
	private abstract class DistanceFilter implements TrackFilter{
		protected int max = Integer.MIN_VALUE;
		protected int min = Integer.MAX_VALUE;
		protected boolean exist = false;
		
		public int getMileAge(){
			return this.exist?(this.max - this.min):-1;
		}
	}
}
