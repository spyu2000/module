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
	void SetExePathName(const CString strExePathName); //设置可执行文件名，包含路径
    int StartMonitor(void);                            //开始监控
	int StopMonitor(void);                             //停止监控

protected:
	static DWORD WINAPI MonitorThread(LPVOID lpParameter);
    void MonitorThread();

private:
	void Restart();
	
private:
	CString m_strExePathName; //包含路径
	CString m_strPath;        //路径
	CString m_strExeName;      //名字
    HANDLE  m_hEventHandle;   //事件对象,用于线程
};

#endif