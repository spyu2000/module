#ifdef _MONITOR_H
#define _MONITOR_H
#include <tlhelp32.h>
class CMonitor
{
public:
	CMonitor();
	CMonitor(CString strExePathName);
	~CMonitor();

public:
	void SetExePathName(const CString strExePathName); //���ÿ�ִ���ļ���������·��
    int StartMonitor(void);                            //��ʼ���
	int StopMonitor(void);                             //ֹͣ���

protected:
	static DWORD WINAPI MonitorThread(LPVOID lpParameter);
    void MonitorThread();

private:
	void Restart();
	
private:
	CString m_strExePathName; //����·��
	CString m_strPath;        //·��
	CString m_strExeName;      //����
    HANDLE  m_hEventHandle;   //�¼�����,�����߳�
};

#endif