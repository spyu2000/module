// dogdemoDlg.cpp : implementation file
//

#include "stdafx.h"
#include "dogdemo.h"
#include "demodlg.h"

#ifdef _DEBUG
#define new DEBUG_NEW
#undef THIS_FILE
static char THIS_FILE[] = __FILE__;
#endif

#include "gsmh.h"
//Define the Gloable Varialbes required by the Mcirodog API
short int DogBytes, DogAddr;
unsigned long DogPassword,NewPassword;
unsigned long DogResult;
unsigned char DogCascade;
void * DogData;


/////////////////////////////////////////////////////////////////////////////
// CAboutDlg dialog used for App About

class CAboutDlg : public CDialog
{
public:
	CAboutDlg();

// Dialog Data
	//{{AFX_DATA(CAboutDlg)
	enum { IDD = IDD_ABOUTBOX };
	//}}AFX_DATA

	// ClassWizard generated virtual function overrides
	//{{AFX_VIRTUAL(CAboutDlg)
	protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support
	//}}AFX_VIRTUAL

// Implementation
protected:
	//{{AFX_MSG(CAboutDlg)
	//}}AFX_MSG
	DECLARE_MESSAGE_MAP()
};

CAboutDlg::CAboutDlg() : CDialog(CAboutDlg::IDD)
{
	//{{AFX_DATA_INIT(CAboutDlg)
	//}}AFX_DATA_INIT
}

void CAboutDlg::DoDataExchange(CDataExchange* pDX)
{
	CDialog::DoDataExchange(pDX);
	//{{AFX_DATA_MAP(CAboutDlg)
	//}}AFX_DATA_MAP
}

BEGIN_MESSAGE_MAP(CAboutDlg, CDialog)
	//{{AFX_MSG_MAP(CAboutDlg)
		// No message handlers
	//}}AFX_MSG_MAP
END_MESSAGE_MAP()

/////////////////////////////////////////////////////////////////////////////
// CDogdemoDlg dialog

CDogdemoDlg::CDogdemoDlg(CWnd* pParent /*=NULL*/)
	: CDialog(CDogdemoDlg::IDD, pParent)
{
	//{{AFX_DATA_INIT(CDogdemoDlg)
	m_dwPassword = 0;
	//}}AFX_DATA_INIT
	// Note that LoadIcon does not require a subsequent DestroyIcon in Win32
	m_hIcon = AfxGetApp()->LoadIcon(IDR_MAINFRAME);
}

void CDogdemoDlg::DoDataExchange(CDataExchange* pDX)
{
	CDialog::DoDataExchange(pDX);
	//{{AFX_DATA_MAP(CDogdemoDlg)
	//DDX_Text(pDX, IDC_EDIT_PASSWORD, m_dwPassword);
	//}}AFX_DATA_MAP
}

BEGIN_MESSAGE_MAP(CDogdemoDlg, CDialog)
	//{{AFX_MSG_MAP(CDogdemoDlg)
	ON_WM_SYSCOMMAND()
	ON_WM_PAINT()
	ON_WM_QUERYDRAGICON()
	ON_BN_CLICKED(IDC_BUTTON_CHECK, OnButtonCheck)
	ON_BN_CLICKED(IDC_BUTTON_CONVERT, OnButtonConvert)
	ON_BN_CLICKED(IDC_BUTTON_WRITE, OnButtonWrite)
	ON_BN_CLICKED(IDC_BUTTON_READ, OnButtonRead)
	ON_BN_CLICKED(IDC_BUTTON_DISABLE, OnButtonDisable)
	ON_BN_CLICKED(IDC_BUTTON_CURNO, OnButtonCurno)
	ON_BN_CLICKED(IDC_BUTTON_SETDOGCASCADE, OnButtonSetdogcascade)
	ON_BN_CLICKED(IDC_BUTTON_SETNEWPASSWORD, OnButtonSetnewpassword)
	//}}AFX_MSG_MAP
END_MESSAGE_MAP()

/////////////////////////////////////////////////////////////////////////////
// CDogdemoDlg message handlers

BOOL CDogdemoDlg::OnInitDialog()
{
	CDialog::OnInitDialog();

	// Add "About..." menu item to system menu.

	// IDM_ABOUTBOX must be in the system command range.
	ASSERT((IDM_ABOUTBOX & 0xFFF0) == IDM_ABOUTBOX);
	ASSERT(IDM_ABOUTBOX < 0xF000);

	CMenu* pSysMenu = GetSystemMenu(FALSE);
	if (pSysMenu != NULL)
	{
		CString strAboutMenu;
		strAboutMenu.LoadString(IDS_ABOUTBOX);
		if (!strAboutMenu.IsEmpty())
		{
			pSysMenu->AppendMenu(MF_SEPARATOR);
			pSysMenu->AppendMenu(MF_STRING, IDM_ABOUTBOX, strAboutMenu);
		}
	}

	// Set the icon for this dialog.  The framework does this automatically
	//  when the application's main window is not a dialog
	SetIcon(m_hIcon, TRUE);			// Set big icon
	SetIcon(m_hIcon, FALSE);		// Set small icon
	
	// TODO: Add extra initialization here
	pSysMenu->RemoveMenu(SC_MAXIMIZE,MF_BYCOMMAND);
	pSysMenu->RemoveMenu(SC_SIZE,MF_BYCOMMAND);

	//m_dwPassword = 0;
	GetDlgItem(IDC_EDIT_CURRENTPASSWORD)->SetWindowText("0");
    GetDlgItem(IDC_EDIT_CURRENTCASCADE)->SetWindowText("0");
    GetDlgItem(IDC_EDIT_NEWCASCADE)->SetWindowText("0");
    GetDlgItem(IDC_EDIT_NEWPASSWORD)->SetWindowText("0");

	return TRUE;  // return TRUE  unless you set the focus to a control
}

void CDogdemoDlg::OnSysCommand(UINT nID, LPARAM lParam)
{
	if ((nID & 0xFFF0) == IDM_ABOUTBOX)
	{
		CAboutDlg dlgAbout;
		dlgAbout.DoModal();
	}
	else
	{
		CDialog::OnSysCommand(nID, lParam);
	}
}

// If you add a minimize button to your dialog, you will need the code below
//  to draw the icon.  For MFC applications using the document/view model,
//  this is automatically done for you by the framework.

void CDogdemoDlg::OnPaint() 
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
HCURSOR CDogdemoDlg::OnQueryDragIcon()
{
	return (HCURSOR) m_hIcon;
}

void CDogdemoDlg::OnButtonCheck() 
{
	// TODO: Add your control notification handler code here
	DWORD dwStatus;
	char Message[100];

	DogCascade = GetDlgItemInt(IDC_EDIT_CURRENTCASCADE,NULL,TRUE);
	dwStatus = DogCheck();
	if (dwStatus==0)  
		strcpy(Message," Check dog succeeded.");
	else 
		wsprintf ( Message, " Check dog failed.\r Error code = %ld",dwStatus);
	GetDlgItem(IDC_STATIC_RESULT)-> SetWindowText (Message);
}

void CDogdemoDlg::OnButtonConvert() 
{
	// TODO: Add your control notification handler code here
	char ConvertData[]="112233";
	DWORD dwStatus;
	char Message[100];

	DogCascade = GetDlgItemInt(IDC_EDIT_CURRENTCASCADE,NULL,TRUE);
	DogBytes = 6;
	DogData = ConvertData;
	dwStatus = DogConvert();
	if (dwStatus==0)
	{
		wsprintf (Message, " Convert succeeded.\r Convert string=%s\r Result = %ld  (0x%lX)",ConvertData,DogResult,DogResult);
	}
	else
	{
		wsprintf (Message, " Convert failed.\r Error code = %ld", dwStatus);
	}
	GetDlgItem(IDC_STATIC_RESULT)->SetWindowText (Message);
}

void CDogdemoDlg::OnButtonWrite() 
{
	// TODO: Add your control notification handler code here
	DWORD dwStatus;
	DWORD dwData = 0x01010834;
	char szData[] = "staff.fleety.cn";
	char Message[300],msg[100];

	Message[0] = 0;

	UpdateData(TRUE);
    DogPassword = GetDlgItemInt(IDC_EDIT_CURRENTPASSWORD,NULL,TRUE);
	//DogPassword = m_dwPassword;

	DogCascade = GetDlgItemInt(IDC_EDIT_CURRENTCASCADE,NULL,TRUE);
	
	DogData = szData;
	DogAddr = 0;
	DogBytes = 15;
	dwStatus = WriteDog();
	if (dwStatus==0)
	{
		wsprintf (msg, " Write string succeeded.\r Write: \"%s\"  at: %d\r\r",szData,DogAddr);
	}
	else
	{
		wsprintf (msg, " Write string failed.\r Error code = %ld\r\r", dwStatus);
	}
	strcat (Message,msg);

	DogBytes = 4;
	DogData = & dwData;
	DogAddr = 100;
	dwStatus = WriteDog();
	if (dwStatus==0)
	{
		wsprintf (msg, " Write dword data succeeded.\r Write: %ld   at: %d\r\r",dwData,DogAddr);
	}
	else
	{
		wsprintf (msg, " Write dword data failed.\r Error code = %ld\r\r", dwStatus);
	}
	strcat (Message,msg);


	dwStatus = GetCurrentNo();
	int CurrentNo = *((int*)DogData);
	DogBytes = 4;
	DogAddr = 104;
	dwStatus = WriteDog();
	if (dwStatus==0)
	{
		wsprintf (msg, " Write dword data succeeded.\r Write: %ld   at: %d\r\r",CurrentNo,DogAddr);
	}
	else
	{
		wsprintf (msg, " Write dword data failed.\r Error code = %ld\r\r", dwStatus);
	}
	strcat (Message,msg);



	
	GetDlgItem(IDC_STATIC_RESULT)->SetWindowText (Message);
}

void CDogdemoDlg::OnButtonRead() 
{
	// TODO: Add your control notification handler code here
	DWORD dwStatus;
	DWORD dwData ;
	char szData[15] ;
	char Message[300],msg[100];

	Message[0] = 0;

	UpdateData(TRUE);
	DogPassword = GetDlgItemInt(IDC_EDIT_CURRENTPASSWORD,NULL,TRUE);
	DogCascade = GetDlgItemInt(IDC_EDIT_CURRENTCASCADE,NULL,TRUE);

	DogData = szData;
	DogAddr = 0;
	DogBytes = 15;
	szData[6] = 0;
	dwStatus = ReadDog();
	if (dwStatus==0)
	{
		wsprintf (msg, " Read string succeeded.\r Read: \"%s\"  from: %d\r\r",szData,DogAddr);
	}
	else
	{
		wsprintf (msg, " Read string failed.\r Error code = %ld\r\r", dwStatus);
	}
	strcat (Message,msg);

	DogBytes = 4;
	DogData = & dwData;
	DogAddr = 100;
	dwStatus = ReadDog();
	if (dwStatus==0)
	{
		wsprintf (msg, " Read dword data succeeded.\r Read: %ld   from: %d\r\r",dwData,DogAddr);
	}
	else
	{
		wsprintf (msg, " Read dword data failed.\r Error code = %ld\r\r", dwStatus);
	}
	strcat (Message,msg);
	
	GetDlgItem(IDC_STATIC_RESULT)->SetWindowText (Message);
	
}

void CDogdemoDlg::OnButtonDisable() 
{
	// TODO: Add your control notification handler code here
	DWORD dwStatus;
	char Message[100];

	DogCascade = GetDlgItemInt(IDC_EDIT_CURRENTCASCADE,NULL,TRUE);
	dwStatus = DisableShare();
	if (dwStatus==0)
	{
		wsprintf (Message, " Disable share succeeded.");
	}
	else
	{
		wsprintf (Message, " Disable failed.\r Error code = %ld", dwStatus);
	}
	GetDlgItem(IDC_STATIC_RESULT)->SetWindowText (Message);
	
}


void CDogdemoDlg::OnButtonCurno() 
{
	// TODO: Add your control notification handler code here
	DWORD dwStatus;
	char Message[100];
	DWORD CurrentNo;

	DogCascade = GetDlgItemInt(IDC_EDIT_CURRENTCASCADE,NULL,TRUE);
	DogData = & CurrentNo;
	dwStatus = GetCurrentNo();
	if (dwStatus==0)
	{
		wsprintf (Message, " Get current No. succeeded.\r Current No. = %u",CurrentNo);
	}
	else
	{
		wsprintf (Message, " Get current No. failed.\r Error code = %ld", dwStatus);
	}
	GetDlgItem(IDC_STATIC_RESULT)->SetWindowText (Message);
}

void CDogdemoDlg::OnButtonSetdogcascade() 
{
	// TODO: Add your control notification handler code here
	DWORD dwStatus;
	char Message[100];
	char showcascade[10];
    BYTE NewCascade;  

	DogPassword = GetDlgItemInt(IDC_EDIT_CURRENTPASSWORD,NULL,TRUE);
	DogCascade = GetDlgItemInt(IDC_EDIT_CURRENTCASCADE,NULL,TRUE);
	NewCascade = GetDlgItemInt(IDC_EDIT_NEWCASCADE,NULL,TRUE);

    DogData = &NewCascade;
	DogAddr = 0;
	DogBytes = 1;

	dwStatus = SetDogCascade();

	if (dwStatus==0)
	{
		wsprintf (Message, " Set NewCascade succeeded.");
        sprintf(showcascade,"%d",NewCascade);
	    GetDlgItem(IDC_EDIT_CURRENTCASCADE)->SetWindowText(showcascade);
	}
	else
	{
		wsprintf (Message, " Set NewCascade failed.\r Error code = %ld", dwStatus);
	}
	GetDlgItem(IDC_STATIC_RESULT)->SetWindowText (Message);


}

void CDogdemoDlg::OnButtonSetnewpassword() 
{
	// TODO: Add your control notification handler code here
	DWORD dwStatus;
	char Message[100];
	char showpassword[20];

	DogPassword = GetDlgItemInt(IDC_EDIT_CURRENTPASSWORD,NULL,TRUE);
	DogCascade = GetDlgItemInt(IDC_EDIT_CURRENTCASCADE,NULL,TRUE);
	NewPassword = GetDlgItemInt(IDC_EDIT_NEWPASSWORD,NULL,TRUE);

	dwStatus = SetPassword();

	if (dwStatus==0)
	{
		wsprintf (Message, " Set NewPassword succeeded.");
        sprintf(showpassword,"%d",NewPassword);
	    GetDlgItem(IDC_EDIT_CURRENTPASSWORD)->SetWindowText(showpassword);
	}
	else
	{
		wsprintf (Message, " Set NewPassword failed.\r Error code = %ld", dwStatus);
	}
	GetDlgItem(IDC_STATIC_RESULT)->SetWindowText (Message);
}
