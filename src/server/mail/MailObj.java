/*
 * 绝密 Created on 2006-12-27 by sunny
 */
package server.mail;

import java.io.File;
import java.io.Serializable;

/**
 * @author sunny
 * 
 * 2006-12-19
 */
public class MailObj implements Serializable {
	private String[] receiverArr;
	private String head;
	private Object content;
	private File file[];
	private String storePath;
	private int state = 0;// 1：发送成功，2：email地址不对，3：网络异常或其他原因 ，（3需要持久化存储发送）
	private int prestate;// 上一次状态
	public static final int STATE_INIT = 0;// 未发送
	public static final int STATE_SUCCESS = 1;// 发送成功
	public static final int STATE_EMAIL_ERROR = 2;// Email地址错误，不持久下发
	public static final int STATE_FAIL = 3;// 3：网络异常或其他原因 持久下发

	public int getPrestate() {
		return prestate;
	}

	public void setPrestate(int prestate) {
		this.prestate = prestate;
	}

	public String getStorePath() {
		return storePath;
	}

	public void setStorePath(String storePath) {
		this.storePath = storePath;
	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.prestate = this.state;
		this.state = state;
	}

	public MailObj(String[] receiverArr, String head, Object content) {
		this(receiverArr,head,content,null);
	}
	
	/**
	 * @param receiverArr
	 * @param head
	 * @param content
	 * @param file
	 */
	public MailObj(String[] receiverArr, String head, Object content,File file[]) {
		super();
		this.receiverArr = receiverArr;
		for(int i=0;i<this.receiverArr.length;i++){
			this.receiverArr[i] = this.receiverArr[i].trim();
		}
		this.head = head;
		this.content = content;
		this.file = file;
	}

	public MailObj() {
		super();
	}

	public String getHead() {
		return head;
	}

	public void setHead(String head) {
		this.head = head;
	}

	public Object getContent() {
		return content;
	}

	public void setContent(Object content) {
		this.content = content;
	}

	public File[] getFile() {
		return file;
	}

	public void setFile(File file[]) {
		this.file = file;
	}

	public String[] getReceiverArr() {
		return receiverArr;
	}

	public void setReceiverArr(String[] receiverArr) {
		this.receiverArr = receiverArr;
	}
}
