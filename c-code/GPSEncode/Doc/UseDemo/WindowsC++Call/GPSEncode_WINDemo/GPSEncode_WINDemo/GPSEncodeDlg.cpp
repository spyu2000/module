// GPSEncodeDlg.cpp : implementation file
//

#include "stdafx.h"
#include "GPSEncode.h"
#include "GPSEncodeDlg.h"

#ifdef _DEBUG
#define new DEBUG_NEW
#undef THIS_FILE
static char THIS_FILE[] = __FILE__;
#endif

//typedef bool( __stdcall *lpEncoderFun)(double loIn, double laIn,double& loOut,double& laOut);
typedef bool( __stdcall *lpDecoderFun)(double loIn, double laIn,double& loOut,double& laOut);

lpDecoderFun decoderFun;

/////////////////////////////////////////////////////////////////////////////
// CGPSEncodeDlg dialog

CGPSEncodeDlg::CGPSEncodeDlg(CWnd* pParent /*=NULL*/)
	: CDialog(CGPSEncodeDlg::IDD, pParent)
{
	//{{AFX_DATA_INIT(CGPSEncodeDlg)
	m_oldLo = 0.0;
	m_oldLa = 0.0;
	m_newLo = 0.0;
	m_newLa = 0.0;
	m_StrOpenPath = _T("");
	m_StrSavePath = _T("");
	//}}AFX_DATA_INIT
	// Note that LoadIcon does not require a subsequent DestroyIcon in Win32
	m_hIcon = AfxGetApp()->LoadIcon(IDR_MAINFRAME);
}

void CGPSEncodeDlg::DoDataExchange(CDataExchange* pDX)
{
	CDialog::DoDataExchange(pDX);
	//{{AFX_DATA_MAP(CGPSEncodeDlg)
	DDX_Text(pDX, IDC_EDIT1, m_oldLo);
	DDX_Text(pDX, IDC_EDIT2, m_oldLa);
	DDX_Text(pDX, IDC_EDIT3, m_newLo);
	DDX_Text(pDX, IDC_EDIT4, m_newLa);
	DDX_Text(pDX, IDC_EDIT5, m_StrOpenPath);
	DDV_MaxChars(pDX, m_StrOpenPath, 1000);
	DDX_Text(pDX, IDC_EDIT6, m_StrSavePath);
	DDV_MaxChars(pDX, m_StrSavePath, 1000);
	//}}AFX_DATA_MAP
}

BEGIN_MESSAGE_MAP(CGPSEncodeDlg, CDialog)
	//{{AFX_MSG_MAP(CGPSEncodeDlg)
	ON_WM_PAINT()
	ON_WM_QUERYDRAGICON()
	ON_BN_CLICKED(IDC_BUTTON1, OnButton1)
	ON_BN_CLICKED(IDC_BUTTON2, OnButton2)
	ON_WM_CLOSE()
	ON_BN_CLICKED(IDC_BUTTON_OPEN, OnButtonOpen)
	ON_BN_CLICKED(IDC_BUTTON_SAVE, OnButtonSave)
	ON_BN_CLICKED(IDC_BUTTON_CALCALL, OnButtonCalcall)
	ON_BN_CLICKED(IDC_BUTTON3, OnButton3)
	//}}AFX_MSG_MAP
END_MESSAGE_MAP()

/////////////////////////////////////////////////////////////////////////////
// CGPSEncodeDlg message handlers

BOOL CGPSEncodeDlg::OnInitDialog()
{
	CDialog::OnInitDialog();

	// Set the icon for this dialog.  The framework does this automatically
	//  when the application's main window is not a dialog
	SetIcon(m_hIcon, TRUE);			// Set big icon
	SetIcon(m_hIcon, FALSE);		// Set small icon
	
	// TODO: Add extra initialization here
	hDll = LoadLibrary("gps_encode32.dll");
	decoderFun = (lpDecoderFun)GetProcAddress(hDll, "decode");
	return TRUE;  // return TRUE  unless you set the focus to a control
}

// If you add a minimize button to your dialog, you will need the code below
//  to draw the icon.  For MFC applications using the document/view model,
//  this is automatically done for you by the framework.

void CGPSEncodeDlg::OnPaint() 
{
	if (IsIconic())
	{
		CPaintDC dc(this); // device context for painting

		SendMessage(WM_ICONERASEBKGND, (WPARAM) dc.GetSafeHdc(), 0);

		// Center icon in client rectangle
		int cxIcon = GetSystemMetrics(SM_CXICON);
		int cyIcon = GetSystemMetrics(SM_CYICON);
		CRect rect;
		GetClientRect(&rect);
		int x = (rect.Width() - cxIcon + 1) / 2;
		int y = (rect.Height() - cyIcon + 1) / 2;

		// Draw the icon
		dc.DrawIcon(x, y, m_hIcon);
	}
	else
	{
		CDialog::OnPaint();
	}
}

// The system calls this to obtain the cursor to display while the user drags
//  the minimized window.
HCURSOR CGPSEncodeDlg::OnQueryDragIcon()
{
	return (HCURSOR) m_hIcon;
}

void CGPSEncodeDlg::OnButton1() 
{
	// TODO: Add your control notification handler code here
	UpdateData();	
	if (hDll != NULL)
	{
		if (decoderFun != NULL)
		{
			decoderFun(m_oldLo,m_oldLa,m_newLo,m_newLa);
		}
	}
	else
	{
		AfxMessageBox("dll未加载");
	}
	UpdateData(FALSE);	
}

void CGPSEncodeDlg::OnButton2() 
{
	// TODO: Add your control notification handler code here
	CDialog::DestroyWindow();
}

void CGPSEncodeDlg::OnClose() 
{
	// TODO: Add your message handler code here and/or call default
	
	FreeLibrary(hDll);
	CDialog::OnClose();
}

void CGPSEncodeDlg::OnButtonOpen() 
{
	// TODO: Add your control notification handler code here
	CFileDialog dlg(TRUE, _T("txt"),NULL, OFN_HIDEREADONLY | OFN_OVERWRITEPROMPT, _T("TXT Files (*.txt)|*.txt||"),this);

	if (dlg.DoModal() == IDOK)
	{
		m_StrOpenPath = dlg.GetPathName();
	}
	
	UpdateData(FALSE);
}

void CGPSEncodeDlg::OnButtonSave() 
{
	// TODO: Add your control notification handler code here	
	char	buf[MAX_PATH];   
	memset(buf,0,sizeof(buf));  
	BROWSEINFO       bi;   
	bi.hwndOwner   =   m_hWnd;   
	bi.pidlRoot   =   NULL;   
	bi.pszDisplayName   =   buf;   
	bi.lpszTitle   =   _T("请选择保存的目录：");   
	bi.ulFlags   =   0;   
	bi.lpfn   =   NULL;   
	bi.lParam   =   0;   
	bi.iImage     =   0;   
	LPITEMIDLIST     lp   =   SHBrowseForFolder(&bi);
	if(lp && SHGetPathFromIDList(lp,buf))
	{   
		m_StrSavePath.Format(_T("%s"),buf);
	}   
	else
	{
		AfxMessageBox(_T("目录无效!"),MB_OK,0);
	} 

	

	UpdateData(FALSE);
}

void CGPSEncodeDlg::OnButtonCalcall() 
{
	UpdateData(TRUE);
	// TODO: Add your control notification handler code here
	m_StrOpenPath.TrimLeft();
	m_StrOpenPath.TrimRight();

	m_StrOpenPath.TrimLeft();
	m_StrSavePath.TrimRight();

	if(m_StrOpenPath.GetLength() == 0)
	{
		AfxMessageBox(_T("请先导入要解密的文件"),MB_OK,0);
		return;
	}

	if(m_StrSavePath.GetLength() == 0)
	{
		AfxMessageBox(_T("请先选择保存结果路径!"),MB_OK,0);
		return;
	}

	m_StrSavePath = m_StrSavePath+"\\DecodeResult.txt";
	CStdioFile *pReadFile = new CStdioFile(m_StrOpenPath, CFile::modeRead | CFile::shareDenyNone);
	CStdioFile *pWriteFile = new CStdioFile(m_StrSavePath, CFile::modeRead | CFile::shareDenyNone | CFile::modeWrite |CFile::modeCreate);
	if (pReadFile == NULL)
	{
		AfxMessageBox(_T("打开导入文件失败，确认文件正确性!"),MB_OK,0);
		return;
	}
 
	if (pReadFile->GetLength() == 0)
 	{
 		pReadFile->Close();
 		delete pReadFile;
		AfxMessageBox(_T("导入文件为空，确认文件正确性!"),MB_OK,0);
		return;
 	}

	if(pWriteFile == NULL)
	{
		AfxMessageBox(_T("创建导出文件失败，请重试!"),MB_OK,0);
		return;	
	}

	if (hDll == NULL)
	{
		AfxMessageBox("dll未加载");
		return;
	}


	CString strTemp=_T("");

	while(pReadFile->ReadString(strTemp))
	{
		CString strLo = _T("");
		CString strLa = _T("");
		strTemp.TrimLeft();
		strTemp.TrimRight();

		int nPos = strTemp.Find(",");
		
		if(strTemp.GetLength()==0 || nPos ==-1 )
		{
			continue;
		}
		
		strLo = strTemp.Left(nPos);
		strLa = strTemp.Right(strTemp.GetLength()-nPos-1);
		double	oldLo = 0.0;
		double	oldLa = 0.0;
		double	newLo = 0.0;
		double	newLa = 0.0;
		oldLo = atof(strLo);
		oldLa = atof(strLa);

		if (decoderFun != NULL)
		{
			if(decoderFun(oldLo,oldLa,newLo,newLa))
			{
				char szRes[100];
				memset(szRes,0,sizeof(szRes));
				sprintf(szRes,"%f,%f\n",newLo,newLa);	
				pWriteFile->WriteString(szRes);
			}
		}	
	}

 	pReadFile->Close();
 	delete pReadFile;
 	pWriteFile->Close();
 	delete pWriteFile;
	CString strInfo = _T("");
	strInfo.Format("批量计算操作成功!\n结果文件路径:%s",m_StrSavePath);
	AfxMessageBox(strInfo);
}

void CGPSEncodeDlg::OnButton3() 
{
	// TODO: Add your control notification handler code here
	CDialog::DestroyWindow();
}
