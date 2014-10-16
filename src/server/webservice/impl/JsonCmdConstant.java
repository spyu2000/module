package server.webservice.impl;

public class JsonCmdConstant {

	// 默认方法名称
	public final static String WEB_SERVICE_METHOD_NAME = "executeCmd";

	// 返回结果消息壳
	public final static String RESULT_JSON_CMD = "cmd";

	public final static String RESULT_JSON_SUCCESS = "success";

	public final static String RESULT_JSON_REASON = "reason";

	// 询问调度工作是否正常，没有参数
	public final static String CMD_JSON_CHECK = "cmd_check_working";

	// 发送通知消息给调度班长
	public final static String CMD_JSON_NOTICE_TO_ADMIN = "cmd_notice_to_admin";

	/**
	 * 消息内容字段定义
	 */
	// 工作检测消息内容标志
	public final static String FLAG_JSON_CHECK_CMD_WORK_STATUS= "work_status";

	// 发送通知消息内容标志
	public final static String FLAG_JSON_NOTICE_CMD_MESSAGE = "message_content";
}
