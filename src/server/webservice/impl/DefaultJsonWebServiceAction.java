package server.webservice.impl;

import org.json.JSONException;
import org.json.JSONObject;

import server.webservice.WebServiceServer;
import server.webservice.base.BaseWebServiceAction;

public class DefaultJsonWebServiceAction extends BaseWebServiceAction {


	/** ********************************��Ҫ������д���·���******************************* */
	/**
	 * ��Ҫ������д�÷���
	 * 
	 * @param cmd
	 * @param jsonPara
	 * @return
	 */
	protected void execCmd(String cmd, String jsonPara, JSONObject resultJsonObj) {
	}

	/** ********************************ͳһ��Ϊ����******************************* */
	/**
	 * ҵ���߼�������Ϊ
	 */
	public String executeAction(String cmd, String jsonPara) {
		if (WebServiceServer.getSingleInstance().isPrint()) {
			System.out.println("executeCmdAction,cmd:" + cmd + ",jsonPara:"
					+ jsonPara);
		}
		// ����һ���������,���������Ϣ�ɾ��巽������ʵ��
		JSONObject resultJsonObj = new JSONObject();
		try {
			this.jsonSetCmd(resultJsonObj, cmd);
			this.jsonSetSuccess(resultJsonObj, true);
			this.jsonSetReason(resultJsonObj, "");
		} catch (Exception e) {
			e.printStackTrace();
			this.jsonSetSuccess(resultJsonObj, false);
			this.jsonSetReason(resultJsonObj, "��ʼ����������쳣" + e.getMessage());
		}
		this.execCmd(cmd, jsonPara, resultJsonObj);

		// ��֤�������Ϊ��
		if (resultJsonObj == null) {
			resultJsonObj = new JSONObject();
			this.jsonSetCmd(resultJsonObj, cmd);
			this.jsonSetSuccess(resultJsonObj, false);
			this.jsonSetReason(resultJsonObj, "�ӿڵ����쳣,�����������Ϊnull");
		}
		return resultJsonObj.toString();
	}

	/** *********************************����Ϊ���߷���*********************************** */
	// ����Ĺ��߷���
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
