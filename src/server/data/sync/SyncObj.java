package server.data.sync;

import java.io.Serializable;

import server.data.sync.container.ISyncInfo;

public class SyncObj implements Serializable {
	public static final int CLIENT_LOGIN_MSG = 2;
	public static final int DATA_SYNC_MSG = 1;
	public static final int INFO_PRINT_MSG = 3;
	
	private String source = null;
	private int msg = 0;
	private int seq = 0;
	private Serializable data = null;
	private boolean isFullDataContainer = false;
	

	public SyncObj(String source,int seq,ISyncInfo data){
		this(source, DATA_SYNC_MSG, seq, data, false);
	}
	public SyncObj(String source,int msg,int seq,Serializable data, boolean isFullDataContainer){
		this.source = source;
		this.msg = msg;
		this.seq = seq;
		this.data = data;
		this.isFullDataContainer = isFullDataContainer;
	}

	public String getSource() {
		return source;
	}
	
	public int getMsg(){
		return this.msg;
	}
	
	public int getSeq() {
		return seq;
	}

	public void setSeq(int seq) {
		this.seq = seq;
	}

	public Serializable getData() {
		return data;
	}
	
	public boolean isFullDataContainer(){
		return this.isFullDataContainer;
	}
}
