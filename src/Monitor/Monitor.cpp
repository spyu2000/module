#include "Monitor.h"

#define TIME_WAIT 1000

CMonitor::CMonitor()
{
	m_strExePathName = "";
	m_hEventHandle = INVALID_HANDLE_VALUE;
}

CMonitor::CMonitor(CString strExePathName)
{
	m_strExePathName = strExePathName;
	m_hEventHandle = INVALID_HANDLE_VALUE;
}

CMonitor::~CMonitor()
{
	if (INVALID_HANDLE_VALUE != m_hEventHandle ) 
	{
		SetEvent(m_hEventHandle);
		CloseHandle(m_hEventHandle);//关闭事件对象
	}
}

CString CMonitor::SetExePathName(const CString strExePathName)
{
	m_strExePathName = strExePathName;
}

int CMonitor::StartMonitor()
{

	if (-1 == m_strExePathName.Find(".exe")) 
	{
		return 1;
	}

	CString str = "";
	CString strTemp = "";

	str = m_strExePathName;

	for (int j = str.GetLength() - 1; j > 0; j--) 
	{
		if ('\\' == str[j]) 
		{
			strTemp = str.Mid(j+1);
			m_strPath = str.Left(j+1);
			break;
		}
	}

	m_strExeName = strTemp;

	CreateThread(NULL,0,MonitorThread,this,0,NULL);

	return 0;
}

int CMonitor::StopMonitor()
{
	SetEvent(m_hEventHandle);

	return 0;
}

DWORD WINAPI CMonitor::MonitorThread(LPVOID lpParameter)
{
	((CMonitor*)lpParameter)->MonitorThread();
	
	return 0;
}

void CMonitor::MonitorThread()
{
	PROCESSENTRY32 pe32 = { sizeof(PROCESSENTRY32)};
	HANDLE hProcessSnap = INVALID_HANDLE_VALUE;
	BOOL bFind;
	CString strProssName;
	CString strName;
	
	strName = m_strExeName;

	//1秒钟循环一次
	while (WAIT_TIMEOUT == WaitForSingleObject(m_hEventHandle, TIME_WAIT)) 
	{
		strProssName = "";
		bFind = FALSE;
		hProcessSnap = INVALID_HANDLE_VALUE;
		hProcessSnap = CreateToolhelp32Snapshot(TH32CS_SNAPPROCESS, 0);
		
		if(hProcessSnap == INVALID_HANDLE_VALUE)
		{
			break;
		}
		
		if(Process32First(hProcessSnap, &pe32)) 
		{
			do 
			{
				strProssName = (LPTSTR)pe32.szExeFile;
				strProssName.MakeUpper();
				strName.MakeUpper();
				if (strProssName  == strName) 
				{
					bFind = TRUE;
					break;
				}
			} 
			while(Process32Next(hProcessSnap, &pe32));
			
			if (!bFind) //如果没有发现,重启
			{
				Restart();
			}
		}
		
		::CloseHandle(hProcessSnap);
	}
}

void CMonitor::Restart()
{
	ShellExecute(NULL, "open", m_strExePathName, NULL, m_strPath, SW_SHOWNORMAL);
}