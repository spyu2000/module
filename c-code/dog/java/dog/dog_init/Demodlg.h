// dogdemoDlg.h : header file
//

#if !defined(AFX_DOGDEMODLG_H__3FA0D835_39E7_11D2_9F0B_00C00C1026E0__INCLUDED_)
#define AFX_DOGDEMODLG_H__3FA0D835_39E7_11D2_9F0B_00C00C1026E0__INCLUDED_

#if _MSC_VER >= 1000
#pragma once
#endif // _MSC_VER >= 1000

/////////////////////////////////////////////////////////////////////////////
// CDogdemoDlg dialog

class CDogdemoDlg : public CDialog
{
// Construction
public:
	CDogdemoDlg(CWnd* pParent = NULL);	// standard constructor

// Dialog Data
	//{{AFX_DATA(CDogdemoDlg)
	enum { IDD = IDD_DOGDEMO_DIALOG };
	DWORD	m_dwPassword;
	//}}AFX_DATA

	// ClassWizard generated virtual function overrides
	//{{AFX_VIRTUAL(CDogdemoDlg)
	protected:
	virtual void DoDataExchange(CDataExchange* pDX);	// DDX/DDV support
	//}}AFX_VIRTUAL

// Implementation
protected:
	HICON m_hIcon;

	// Generated message map functions
	//{{AFX_MSG(CDogdemoDlg)
	virtual BOOL OnInitDialog();
	afx_msg void OnSysCommand(UINT nID, LPARAM lParam);
	afx_msg void OnPaint();
	afx_msg HCURSOR OnQueryDragIcon();
	afx_msg void OnButtonCheck();
	afx_msg void OnButtonConvert();
	afx_msg void OnButtonWrite();
	afx_msg void OnButtonRead();
	afx_msg void OnButtonDisable();
	afx_msg void OnButtonCurno();
	afx_msg void OnButtonSetdogcascade();
	afx_msg void OnButtonSetnewpassword();
	//}}AFX_MSG
	DECLARE_MESSAGE_MAP()
};

//{{AFX_INSERT_LOCATION}}
// Microsoft Developer Studio will insert additional declarations immediately before the previous line.

#endif // !defined(AFX_DOGDEMODLG_H__3FA0D835_39E7_11D2_9F0B_00C00C1026E0__INCLUDED_)
