package test.spyu;

import java.nio.ByteBuffer;
import java.util.Calendar;

public class Util {
	/**
	 * 设定时间结构到buff中
	 * 
	 * @param calendar
	 * @param msg
	 */
	public static void setTimeStruct(Calendar calendar, ByteBuffer msg) {
		if (calendar == null) {
			calendar = Calendar.getInstance();
		}
		msg.putShort((short) calendar.get(Calendar.YEAR));
		msg.put((byte) (calendar.get(Calendar.MONTH) + 1));
		msg.put((byte) calendar.get(Calendar.DAY_OF_MONTH));
		msg.put((byte) calendar.get(Calendar.HOUR_OF_DAY));
		msg.put((byte) calendar.get(Calendar.MINUTE));
		msg.put((byte) calendar.get(Calendar.SECOND));
	}


	/**
	 * 处理日期
	 * 
	 * @param data
	 * @return
	 */
	public static Calendar disposeDate(ByteBuffer data) {
		Calendar calendar = Calendar.getInstance();
		int year, month, day, hour, minute, second;
		year = data.getShort();
		month = (int) (data.get() & 0xFF);
		day = (int) (data.get() & 0xFF);
		hour = (int) (data.get() & 0xFF);
		minute = (int) (data.get() & 0xFF);
		second = (int) (data.get() & 0xFF);

		calendar.set(Calendar.YEAR, year);
		calendar.set(Calendar.MONTH, month - 1);
		calendar.set(Calendar.DAY_OF_MONTH, day);
		calendar.set(Calendar.HOUR_OF_DAY, hour);
		calendar.set(Calendar.MINUTE, minute);
		calendar.set(Calendar.SECOND, second);
		return calendar;
	}


	// 根据日期和时间得到oracle需要的Date类型
	public static String getInsertToOracleDateStr(String date, String time) {
		if (date == null || date.equals("")) {
			date = "0001-01-01";
		}
		if (time == null || time.equals("")) {
			time = "00:00:00";
		}
		String oracleStr = "to_date('" + date + " " + time
				+ "', 'yyyy-mm-dd hh24:mi:ss')";
		return oracleStr;

	}

	public static String getInsertToOracleDateStr(String dateTime) {
		String oracleStr = "to_date('" + dateTime
				+ "', 'yyyy-mm-dd hh24:mi:ss')";
		return oracleStr;

	}
}
