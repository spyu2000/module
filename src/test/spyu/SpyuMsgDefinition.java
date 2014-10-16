/**
 * ���� Created on 2008-9-4 by edmund
 */
package test.spyu;

public class SpyuMsgDefinition {

	public final static int COMPLETE_SAFE_FLAG = 0xFFFFFFFF;

	
	//��Ϣ��ѯ����վ�㣩
	public final static int IN_STATION_INFO_FLAG=0x01;		
	// ���λ�ÿճ���ѯ��Ϣ
	public final static int IN_FREE_CAR_FLAG = 0x02;
	// ������Ϣ
	public final static int IN_TASK_FLAG = 0x03;
	// ���ó���״̬��Ϣ
	public final static int IN_CAR_STATUS_FLAG = 0x04;
	// ������ʵʱ��Ϣ
	public final static int IN_CAR_LOCATION_INFO_FLAG = 0x05;	
	// ͬ��������Ϣ
	public final static int IN_CAR_BASE_INFO_FLAG = 0x06;
	
	
	// ��Ϣ��ѯ�����Ϣ��վ�㣩
	public final static int OUT_STATION_INFO_FLAG = 0x01;	
	// �ճ���ѯ�����Ϣ
	public final static int OUT_FREE_CAR_FLAG = 0x02;	
	//��Ӧ����ʵʱ��Ϣ����
	public final static int OUT_CAR_LOCATION_INFO_FLAG = 0x05;
	// ��������վ��Ϣ
	public final static int OUT_CAR_STATION_FLAG = 0x10;

	// ���ó���״̬��Ϣ
	public final static int OUT_CAR_STATUS_FLAG = 0x11;

	// �����ӷų˿���Ϣ
	public final static int OUT_CAR_PEOPLE_FLAG = 0x12;

	// ���񱨾���Ϣ
	public final static int OUT_CAR_ALARM_FLAG = 0x80;

	// ��Ϣȷ����Ϣ
	public final static int CONFIRM_FLAG = 0xFE;

	// ������Ϣ
	public final static int HEART_FLAG = 0xFF;

	
	/**
	 * �ֵ�ö��ֵ
	 */
	//״̬
	public final static int ENUM_OUT_JOB = 0;
	public final static int ENUM_ON_JOB = 1;
	public final static int ENUM_OCCUPIED_JOB = 2;
	public final static int ENUM_VACANT_JOB = 3;
	

	//վ��
	public final static int ENUM_STATION_TYPE=1;
	public final static int ENUM_STATION_OUTLET = 2;
	public final static int ENUM_STATION_DEPO = 4;
	public final static int ENUM_STATION_REST=8;
	public final static int ENUM_CHECK_POINT=16;
	

	//����
	public final static int ENUM_ALARM_EARLY_CHECKPOINT = 1;
	public final static int ENUM_ALARM_LATER_CHECKPOINT = 2;
	public final static int ENUM_ALARM_LATER_UP_PEOPLE = 3;
	
	//����վ
	public final static int ENUM_IN_STATION = 1;	
	public final static int ENUM_OUT_STATION= 2;
	
	//�ӷų˿�����
	public final static int ENUM_UP_PEOPLE=1;	
	public final static int ENUM_DOWN_PEOPLE=2;
	
	//�����ɹ�ʧ�ܱ�־
	public final static int ENUM_OP_SUCCESS=0;	
	public final static int ENUM_OP_FAILURE=1;
	
	//ͼ������
	public final static int ENUM_ROUND=1;
	public final static int ENUM_RECTANGLE=2;
	public final static int ENUM_POLYGON=3;
	
	//��ͼ��������ͻ���֮�䶨��ĳ���״̬��־
	public final static int ENUM_CLIENT_ON_JOB_STATUS=101;
	public final static int ENUM_CLIENT_OUT_JOB_STATUS=102;
	
	


}
