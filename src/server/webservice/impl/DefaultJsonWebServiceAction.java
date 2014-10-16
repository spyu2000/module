package server.webservice.impl;

import org.json.JSONException;
import org.json.JSONObject;

import server.webservice.WebServiceServer;
import server.webservice.base.BaseWebServiceAction;

public class DefaultJsonWebServiceAction extends BaseWebServiceAction {


	/** ********************************需要子类重写以下方法******************************* */
	/**
	 * 需要子类重写该方法
	 * 
	 * @param cmd
	 * @param jsonPara
	 * @return
	 */
	protected void execCmd(String cmd, String jsonPara, JSONObject resultJsonObj) {
	}

	/** ********************************统一行为处理******************************* */
	/**
	 * 业务逻辑处理行为
	 */
	public String executeAction(String cmd, String jsonPara) {
		if (WebServiceServer.getSingleInstance().isPrint()) {
			System.out.println("executeCmdAction,cmd:" + cmd + ",jsonPara:"
					+ jsonPara);
		}
		// 创造一个结果对象,操作结果信息由具体方法设置实现
		JSONObject resultJsonObj = new JSONObject();
		try {
			this.jsonSetCmd(resultJsonObj, cmd);
			this.jsonSetSuccess(resultJsonObj, true);
			this.jsonSetReason(resultJsonObj, "");
		} catch (Exception e) {
			e.printStackTrace();
			this.jsonSetSuccess(resultJsonObj, false);
			this.jsonSetReason(resultJsonObj, "初始化结果对象异常" + e.getMessage());
		}
		this.execCmd(cmd, jsonPara, resultJsonObj);

		// 保证结果对象不为空
		if (resultJsonObj == null) {
			resultJsonObj = new JSONObject();
			this.jsonSetCmd(resultJsonObj, cmd);
			this.jsonSetSuccess(resultJsonObj, false);
			this.jsonSetReason(resultJsonObj, "接口调用异常,结果对象被设置为null");
		}
		return resultJsonObj.toString();
	}

	/** *********************************以下为工具方法*********************************** */
	// 本类的工具方法
	public void jsonSetCmd(JSONObject jsonObj, String cmd) {
		try {
			jsonObj.put(JsonCmdConstant.RESULT_JSON_CMD, cmd);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void jsonSetSuccess(JSONObject jsonObj, boolean success) {
		try {
			jsonObj.put(JsonCmdConstant.RESULT_JSON_SUCCESS, success);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void jsonSetReason(JSONObject jsonObj, String reason) {
		try {
			jsonObj.put(JsonCmdConstant.RESULT_JSON_REASON, reason);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
