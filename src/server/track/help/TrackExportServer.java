package server.track.help;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.sql.ResultSet;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import server.db.DbServer;

import com.fleety.base.GeneralConst;
import com.fleety.base.InfoContainer;
import com.fleety.server.BasicServer;
import com.fleety.track.TrackFilter;
import com.fleety.track.TrackIO;
import com.fleety.util.pool.db.DbConnPool.DbHandle;
import com.fleety.util.pool.db.DbConnPool.StatementHandle;

public class TrackExportServer extends BasicServer {
	public boolean startServer() {
		try {
			String path = this.getStringPara("track_path");
			TrackIO track = new TrackIO();
			if (this.getIntegerPara("track_len") != null) {
				track.setRecordDataLen(this.getIntegerPara("track_len")
						.intValue());
			}

			Date startDate = GeneralConst.YYYY_MM_DD.parse(this
					.getStringPara("start_date"));
			Date endDate = GeneralConst.YYYY_MM_DD.parse(this
					.getStringPara("end_date"));

			BufferedWriter writer;
			ExportFilter filter = new ExportFilter();
			File dir;
			File[] fArr;
			BufferedInputStream in = null;
			Calendar cal = Calendar.getInstance(), scal = Calendar
					.getInstance();
			cal.setTime(startDate);
			String fName;
			List dataList = new LinkedList();
			String dateStr;
			int index = 0;
			while (!endDate.before(cal.getTime())) {
				long t = System.currentTimeMillis();
				index = 1;
				dir = new File(path,
						"TRK"
								+ (dateStr = GeneralConst.YYYYMMDD.format(cal
										.getTime())));
				fArr = dir.listFiles();

				scal.setTime(cal.getTime());
				writer = new BufferedWriter(new FileWriter(GeneralConst
						.getSimpleDateFormat("yyMMdd").format(cal.getTime())
						+ ".txt"));
				for (int h = 0; h < 4; h++) {
					scal.set(Calendar.HOUR_OF_DAY, h * 6);
					dataList.clear();
					for (int i = 0; i < fArr.length; i++) {
						fName = fArr[i].getName();
						if (!fName.endsWith(".LOG") || !fArr[i].isFile()) {
							continue;
						}
						in = new BufferedInputStream(new FileInputStream(
								fArr[i]));
						filter.updateWriter(
								fName.substring(0, fName.length() - 4),
								dataList, scal);
						filter.readCount = 0;
						track.readTrackRecord(in, filter);
						in.close();

						if ((i % 1000) == 0) {
							System.out.println(dateStr + " Process:" + h
									+ "/4 " + (i + 1) + "/" + fArr.length
									+ " time="
									+ (System.currentTimeMillis() - t) / 1000);
						}
					}

					String[] arr = new String[dataList.size()];
					dataList.toArray(arr);
					Arrays.sort(arr, new Comparator() {
						public int compare(Object o1, Object o2) {
							return Long.parseLong(((String) o1).split(",")[5])
									- Long.parseLong(((String) o2).split(",")[5]) >= 0 ? 1
									: -1;
						}
					});
					for (int ii = 0; ii < arr.length; ii++) {
						writer.write(index++ + "," + arr[ii]);
					}
					System.out.println("CostTime:"
							+ (System.currentTimeMillis() - t) / 1000
							+ " size=" + dataList.size());
				}
				writer.close();

				cal.add(Calendar.DAY_OF_MONTH, 1);
			}

			this.isRunning = true;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return this.isRunning();
	}

	private class ExportFilter implements TrackFilter {
		private String destNo = null;
		private Calendar cal = null;
		private StringBuffer buff = new StringBuffer(256);
		private List dataList = null;
		public int readCount = 0;

		public void updateWriter(String destNo, List dataList, Calendar cal) {
			this.destNo = destNo;
			this.cal = cal;
			this.dataList = dataList;
		}

		public int filterTrack(InfoContainer info) {
			Calendar cCal = Calendar.getInstance();
			cCal.setTime(info.getDate(TrackIO.DEST_TIME_FLAG));
			if (cCal.get(Calendar.YEAR) != cal.get(Calendar.YEAR)) {
				return TrackFilter.IGNORE_FLAG;
			}
			if (cCal.get(Calendar.MONTH) != cal.get(Calendar.MONTH)) {
				return TrackFilter.IGNORE_FLAG;
			}
			if (cCal.get(Calendar.DATE) != cal.get(Calendar.DATE)) {
				return TrackFilter.IGNORE_FLAG;
			}
			if (cCal.get(Calendar.HOUR_OF_DAY) < cal.get(Calendar.HOUR_OF_DAY)) {
				return TrackFilter.IGNORE_FLAG;
			}
			if (cCal.get(Calendar.HOUR_OF_DAY) > cal.get(Calendar.HOUR_OF_DAY) + 5) {
				return TrackFilter.BREAK_FLAG;
			}
			int status = info.getInteger(TrackIO.DEST_STATUS_FLAG).intValue();
			if (status == 3) {
				return TrackFilter.IGNORE_FLAG;
			}

			buff.delete(0, buff.length());
			buff.append(destNo);
			buff.append(",");
			buff.append(info.getString(TrackIO.DEST_LA_FLAG));
			buff.append(",");
			buff.append(info.getString(TrackIO.DEST_LO_FLAG));
			buff.append(",");
			buff.append(info.getString(TrackIO.DEST_SPEED_FLAG));
			buff.append(",");
			buff.append(info.getInteger(TrackIO.DEST_DIRECTION_FLAG).intValue());
			buff.append(",");
			buff.append(cCal.getTimeInMillis() / 1000);
			buff.append(",");
			if (status != 1) {
				status = 0;
			}
			buff.append(status);
			buff.append("\n");

			this.dataList.add(buff.toString());

			return TrackFilter.IGNORE_FLAG;
		}
	}

	public static void main(String[] args) throws Exception {
		if (args.length < 3) {
			System.out
					.println("缺少参数：轨迹路径 开始日期 结束日期 轨迹长度(可选，默认26)");
			return;
		}
		String trackLen = "26";
		if (args.length >= 4) {
			trackLen = args[3];
		}
		System.out.println("轨迹路径:" + args[0] + " 开始日期:" + args[1] + " 结束日期:"
				+ args[2] + " 轨迹长度:" + trackLen);

		TrackExportServer server = new TrackExportServer();
		server.addPara("track_path", args[0]);
		server.addPara("track_len", trackLen);
		server.addPara("start_date", args[1]);
		server.addPara("end_date", args[2]);
		server.startServer();
	}
}
