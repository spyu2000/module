/**
 * 绝密 Created on 2008-9-4 by edmund
 */
package test.spyu;

public class SpyuMsgDefinition {

	public final static int COMPLETE_SAFE_FLAG = 0xFFFFFFFF;

	
	//信息查询请求（站点）
	public final static int IN_STATION_INFO_FLAG=0x01;		
	// 最近位置空车查询消息
	public final static int IN_FREE_CAR_FLAG = 0x02;
	// 任务消息
	public final static int IN_TASK_FLAG = 0x03;
	// 设置车辆状态消息
	public final static int IN_CAR_STATUS_FLAG = 0x04;
	// 请求车辆实时信息
	public final static int IN_CAR_LOCATION_INFO_FLAG = 0x05;	
	// 同步车辆信息
	public final static int IN_CAR_BASE_INFO_FLAG = 0x06;
	
	
	// 信息查询结果消息（站点）
	public final static int OUT_STATION_INFO_FLAG = 0x01;	
	// 空车查询结果消息
	public final static int OUT_FREE_CAR_FLAG = 0x02;	
	//响应车辆实时信息请求
	public final static int OUT_CAR_LOCATION_INFO_FLAG = 0x05;
	// 车辆进出站消息
	public final static int OUT_CAR_STATION_FLAG = 0x10;

	// 设置车辆状态消息
	public final static int OUT_CAR_STATUS_FLAG = 0x11;

	// 车辆接放乘客消息
	public final static int OUT_CAR_PEOPLE_FLAG = 0x12;

	// 任务报警消息
	public final static int OUT_CAR_ALARM_FLAG = 0x80;

	// 消息确认消息
	public final static int CONFIRM_FLAG = 0xFE;

	// 心跳消息
	public final static int HEART_FLAG = 0xFF;

	
	/**
	 * 字典枚举值
	 */
	//状态
	public final static int ENUM_OUT_JOB = 0;
	public final static int ENUM_ON_JOB = 1;
	public final static int ENUM_OCCUPIED_JOB = 2;
	public final static int ENUM_VACANT_JOB = 3;
	

	//站点
	public final static int ENUM_STATION_TYPE=1;
	public final static int ENUM_STATION_OUTLET = 2;
	public final static int ENUM_STATION_DEPO = 4;
	public final static int ENUM_STATION_REST=8;
	public final static int ENUM_CHECK_POINT=16;
	

	//报警
	public final static int ENUM_ALARM_EARLY_CHECKPOINT = 1;
	public final static int ENUM_ALARM_LATER_CHECKPOINT = 2;
	public final static int ENUM_ALARM_LATER_UP_PEOPLE = 3;
	
	//进出站
	public final static int ENUM_IN_STATION = 1;	
	public final static int ENUM_OUT_STATION= 2;
	
	//接放乘客类型
	public final static int ENUM_UP_PEOPLE=1;	
	public final static int ENUM_DOWN_PEOPLE=2;
	
	//操作成功失败标志
	public final static int ENUM_OP_SUCCESS=0;	
	public final static int ENUM_OP_FAILURE=1;
	
	//图形类型
	public final static int ENUM_ROUND=1;
	public final static int ENUM_RECTANGLE=2;
	public final static int ENUM_POLYGON=3;
	
	//地图服务器与客户端之间定义的车辆状态标志
	public final static int ENUM_CLIENT_ON_JOB_STATUS=101;
	public final static int ENUM_CLIENT_OUT_JOB_STATUS=102;
	
	


}
