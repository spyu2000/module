package server.net.updator;

public interface IUpdator {
	public static final int U_VERSION_COMPARE_MSG = 1;
	public static final int U_FILE_DOWNLOAD_MSG = 2;
	
	public static final int D_VERSION_COMPARE_RES = 1;
	public static final int D_FILE_DOWNLOAD_RES = 2;
	
	/**
	 * �õ���ǰ����ϵͳ�İ汾
	 * @return
	 */
	public String getVersion();
	
	/**
	 * ����������
	 */
	public void startMainProcess();
}
